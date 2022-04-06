/*
 * Copyright © 2012-2014 Cask Data, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package co.cask.coopr.layout;

import co.cask.coopr.cluster.Cluster;
import co.cask.coopr.cluster.Node;
import co.cask.coopr.cluster.NodeProperties;
import co.cask.coopr.http.request.ClusterCreateRequest;
import co.cask.coopr.layout.change.ClusterLayoutChange;
import co.cask.coopr.layout.change.ClusterLayoutTracker;
import co.cask.coopr.scheduler.task.NodeService;
import co.cask.coopr.spec.HardwareType;
import co.cask.coopr.spec.ImageType;
import co.cask.coopr.spec.Provider;
import co.cask.coopr.spec.service.Service;
import co.cask.coopr.spec.template.ClusterTemplate;
import co.cask.coopr.spec.template.Compatibilities;
import co.cask.coopr.store.entity.EntityStoreService;
import co.cask.coopr.store.entity.EntityStoreView;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import com.google.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * The solver takes a cluster template, a number of machines, and figures out what services to put on what hardware
 * and images in order to satisfy the cluster constraints.
 *
 * TODO: add ability to grow/shrink, making sure services dont move.
 * TODO: add ability to add/remove services from an existing cluster
 * TODO: refactor into cleaner pieces with pluggable pieces for constraints
 */
public class Solver {
  private static final Logger LOG  = LoggerFactory.getLogger(Solver.class);
  private final EntityStoreService entityStoreService;
  private final ClusterLayoutUpdater updater;

  @Inject
  private Solver(EntityStoreService entityStoreService, ClusterLayoutUpdater updater) {
    this.entityStoreService = entityStoreService;
    this.updater = updater;
  }

  /**
   * Add services to a cluster, returning which nodes were affected by the change or null if there was no way to
   * add the services to the cluster.
   *
   * @param cluster Cluster to add the services to.
   * @param clusterNodes Nodes in the cluster.
   * @param servicesToAdd Services to add to the cluster.
   * @return Nodes that need to have services added to them.
   * @throws Exception
   */
  public Set<Node> addServicesToCluster(Cluster cluster, Set<Node> clusterNodes,
                                        Set<String> servicesToAdd) throws Exception {
    EntityStoreView entityStore = entityStoreService.getView(cluster.getAccount());
    Map<String, Service> serviceMap = getServiceMap(Sets.union(cluster.getServices(), servicesToAdd), entityStore);
    validateServiceCompatibilities(cluster.getClusterTemplate().getCompatibilities(), servicesToAdd);
    validateServiceDependencies(serviceMap);

    ClusterLayoutTracker tracker = updater.addServicesToCluster(cluster, clusterNodes, servicesToAdd);
    if (tracker == null) {
      return null;
    }

    Set<Node> changedNodes = Sets.newHashSet();
    for (ClusterLayoutChange change : tracker.getChanges()) {
      changedNodes.addAll(change.applyChange(cluster, clusterNodes, serviceMap));
    }
    return changedNodes;
  }

  /**
   * Validate whether or not a set of services are allowed to be added to a cluster.
   *
   * @param cluster Cluster to check addition of services to.
   * @param servicesToAdd Services to add to the cluster
   * @throws IOException
   */
  public void validateServicesToAdd(Cluster cluster, Set<String> servicesToAdd) throws IOException {
    EntityStoreView entityStore = entityStoreService.getView(cluster.getAccount());
    Map<String, Service> serviceMap = getServiceMap(Sets.union(cluster.getServices(), servicesToAdd), entityStore);
    validateServicesToAdd(cluster, servicesToAdd, serviceMap);
  }

  private void validateServicesToAdd(Cluster cluster, Set<String> servicesToAdd, Map<String, Service> serviceMap)
     {
    validateServiceCompatibilities(cluster.getClusterTemplate().getCompatibilities(), servicesToAdd);
    validateServiceDependencies(serviceMap);
  }

  /**
   * Given a {@link Cluster} and {@link ClusterCreateRequest}, return a mapping of node id to {@link Node} describing
   * how the cluster should be laid out. If multiple possible cluster layouts are possible, one will be chosen
   * deterministically.
   *
   * @param cluster Cluster to solve a layout for.
   * @param request Request to create a cluster containing cluster settings to use.
   * @return Mapping of node id to node for all nodes in the cluster.
   * @throws Exception
   */
  public Map<String, Node> solveClusterNodes(Cluster cluster, ClusterCreateRequest request) throws Exception {
    EntityStoreView entityStore = entityStoreService.getView(cluster.getAccount());
    ClusterTemplate template = cluster.getClusterTemplate();
    Provider provider = cluster.getProvider();
    String providerName = provider.getName();

    // make sure there are hardware types that can be used
    String requiredHardwareType = request.getHardwareType();
    if (requiredHardwareType == null || requiredHardwareType.isEmpty()) {
      // this can be null too, which means no cluster wide required type
      requiredHardwareType = template.getClusterDefaults().getHardwaretype();
    }
    if (requiredHardwareType != null && requiredHardwareType.isEmpty()) {
      requiredHardwareType = null;
    }
    Map<String, String> hardwareTypeFlavors =
      getHardwareTypeMap(providerName, template, requiredHardwareType, entityStore);
    if (hardwareTypeFlavors.isEmpty()) {
      throw new IllegalArgumentException("no hardware types are available to use with template "
                                           + template.getName() + " and provider " + providerName);
    }

    // TODO: horribly ugly... just get the ImageType object instead of treating flavor/image specially
    // make sure there are image types that can be used
    String requiredImageType = request.getImageType();
    if (requiredImageType == null || requiredImageType.isEmpty()) {
      // this can be null too, which means no cluster wide required type
      requiredImageType = template.getClusterDefaults().getImagetype();
    }
    if (requiredImageType != null && requiredImageType.isEmpty()) {
      requiredImageType = null;
    }
    Map<String, Map<String, String>> imageTypeMap =
      getImageTypeMap(providerName, template, requiredImageType, entityStore);
    if (imageTypeMap.isEmpty()) {
      throw new IllegalArgumentException("no image types are available to use with template "
                                           + template.getName() + " and provider " + providerName);
    }

    // make sure the services to place on the cluster are all valid
    Set<String> serviceNames = request.getServices();
    if (serviceNames == null || serviceNames.isEmpty()) {
      serviceNames = template.getClusterDefaults().getServices();
    }
    validateServiceCompatibilities(template.getCompatibilities(), serviceNames);

    Map<String, Service> serviceMap = getServiceMap(serviceNames, entityStore);
    validateServiceDependencies(serviceMap);
    cluster.setServices(serviceNames);

    // TODO: move building of node properties to NodeService or Node or some place more sensible
    String dnsSuffix = request.getDnsSuffix();
    if (dnsSuffix == null || dnsSuffix.isEmpty()) {
      dnsSuffix = template.getClusterDefaults().getDnsSuffix();
    }

    Map<String, Node> nodes =
      solveConstraints(cluster.getId(), template, request.getName(), request.getNumMachines(), hardwareTypeFlavors,
                       imageTypeMap, serviceNames, serviceMap, dnsSuffix);

    // Update cluster object
    // TODO: this should happen outside Solver.
    cluster.setNodes(nodes == null ? ImmutableSet.<String>of() : nodes.keySet());

    return nodes;
  }

  // get a mapping of service name to service object for fast lookup later. Also check that each service actually
  // exists.
  private Map<String, Service> getServiceMap(Set<String> serviceNames, EntityStoreView entityStore) throws IOException {
    Map<String, Service> map = Maps.newHashMap();
    for (String serviceName : serviceNames) {
      Service service = entityStore.getService(serviceName);
      if (service == null) {
        throw new IllegalArgumentException("service " + serviceName + " does not exist");
      }
      map.put(serviceName, entityStore.getService(serviceName));
    }
    return map;
  }

  // get a mapping of hardware type name to flavor that can be used with the given provider and cluster template.
  private Map<String, String> getHardwareTypeMap(String providerName, ClusterTemplate template,
                                                 String requiredHardwareType, EntityStoreView entityStore)
    throws Exception {
    Map<String, String> flavorMap = Maps.newHashMap();

    Compatibilities compatibilities = template.getCompatibilities();
    if (requiredHardwareType != null) {
      addProviderFlavor(flavorMap, providerName, compatibilities,
                        entityStore.getHardwareType(requiredHardwareType));
      return flavorMap;
    }

    for (HardwareType hardwareType : entityStore.getAllHardwareTypes()) {
      addProviderFlavor(flavorMap, providerName, compatibilities, hardwareType);
    }

    return flavorMap;
  }

  // Adds an hardwareType name -> flavor entry to the map given a hardware type, the name of the provider, and
  // a whitelist of allowed hardware types.  If the flavor does not exist for the provider and whitelisted
  // hardware type, nothing is added.
  private void addProviderFlavor(Map<String, String> map, String providerName,
                                 Compatibilities compatibilities, HardwareType hardwareType) {
    if (hardwareType != null) {
      Map<String, Map<String, String>> providerMap = hardwareType.getProviderMap();
      String name = hardwareType.getName();
      // empty allowed types means all types are allowed
      if (compatibilities.compatibleWithHardwareType(name) && providerMap.containsKey(providerName)) {
        String flavor = providerMap.get(providerName).get("flavor");
        if (flavor != null) {
          map.put(name, flavor);
        }
      }
    }
  }

  // get a mapping of image type name to provider properties for that image type
  private Map<String, Map<String, String>> getImageTypeMap(String providerName, ClusterTemplate template,
                                              String requiredImageType, EntityStoreView entityStore) throws Exception {
    Map<String, Map<String, String>> imageMap = Maps.newHashMap();

    Compatibilities compatibilities = template.getCompatibilities();
    if (requiredImageType != null) {
      addProviderImage(imageMap, providerName, compatibilities, entityStore.getImageType(requiredImageType));
      return imageMap;
    }

    for (ImageType imageType : entityStore.getAllImageTypes()) {
      addProviderImage(imageMap, providerName, compatibilities, imageType);
    }

    return imageMap;
  }

  private void addProviderImage(Map<String, Map<String, String>> map, String providerName,
                                Compatibilities compatibilities, ImageType imageType) {
    if (imageType != null) {
      Map<String, Map<String, String>> providerMap = imageType.getProviderMap();
      String name = imageType.getName();
      // empty allowed types means all types are allowed
      if (compatibilities.compatibleWithImageType(name) && providerMap.containsKey(providerName)) {
        Map<String, String> providerProperties = providerMap.get(providerName);
        String image = providerProperties.get("image");
        if (image != null) {
          map.put(name, providerProperties);
        }
      }
    }
  }

  private void validateServiceCompatibilities(Compatibilities compatibilities, Set<String> services) {
    if (!compatibilities.compatibleWithServices(services)) {
      Set<String> incompatibleServices = Sets.difference(services, compatibilities.getServices());
      if (!incompatibleServices.isEmpty()) {
        String incompatibleStr = Joiner.on(',').join(incompatibleServices);
        throw new IllegalArgumentException(incompatibleStr + " are incompatible with the cluster");
      }
    }
  }

  /**
   * Given a map of service name to {@link Service}, validate that the service dependency requirements for all services
   * in the map are satisfied by some other service in the map, throwing an {@link IllegalArgumentException} if they
   * are not.
   *
   * @param serviceMap Map of service name to {@link Service} to check all dependency requirements for.
   */
  private void validateServiceDependencies(Map<String, Service> serviceMap) {
    // gather all services that will be provided on the cluster. This is every service plus any service they provide.
    Set<String> providedServices = Sets.newHashSet();
    for (Service service : serviceMap.values()) {
      providedServices.addAll(service.getDependencies().getProvides());
    }
    providedServices = Sets.union(serviceMap.keySet(), providedServices);

    // check dependencies
    boolean dependenciesSatisfied = true;
    StringBuilder errMsg = new StringBuilder();
    for (Service service : serviceMap.values()) {
      for (String serviceDependency : service.getDependencies().getRequiredServices()) {
        if (!providedServices.contains(serviceDependency)) {
          if (!dependenciesSatisfied) {
            errMsg.append("\n");
          }
          errMsg.append(service.getName());
          errMsg.append(" requires ");
          errMsg.append(serviceDependency);
          errMsg.append(", which is not on the cluster or in the list of services to add.");
          dependenciesSatisfied = false;
        }
      }
    }
    if (!dependenciesSatisfied) {
      throw new IllegalArgumentException(errMsg.toString());
    }

    boolean hasConflicts = false;
    errMsg = new StringBuilder();
    for (Service service : serviceMap.values()) {
      for (String conflictingService : service.getDependencies().getConflicts()) {
        if (serviceMap.keySet().contains(conflictingService)) {
          if (hasConflicts) {
            errMsg.append("\n");
          }
          errMsg.append(service.getName());
          errMsg.append(" conflicts with ");
          errMsg.append(conflictingService);
          errMsg.append(".");
          hasConflicts = true;
        }
      }
    }
    if (hasConflicts) {
      throw new IllegalArgumentException(errMsg.toString());
    }
  }

  // solves for a valid cluster layout based on the constraints. First finds all possible node layouts that can be
  // used in the cluster based on the services that need to be on the cluster and constraints. Then searches for a
  // valid number of each node layout based on the constraints.
  static Map<String, Node> solveConstraints(String clusterId, ClusterTemplate clusterTemplate, String clusterName,
                                            int numMachines,
                                            Map<String, String> hardwareTypeMap,
                                            Map<String, Map<String, String>> imageTypeMap,
                                            Set<String> serviceNames,
                                            Map<String, Service> serviceMap,
                                            String dnsSuffix) {
    NodeLayoutGenerator nodeLayoutGenerator =
      new NodeLayoutGenerator(clusterTemplate, serviceNames, hardwareTypeMap.keySet(), imageTypeMap.keySet());

    // We need to deterministically choose the same cluster.  Nodelayouts earlier in the traversal order are
    // preferred.
    List<NodeLayout> traversalOrder = nodeLayoutGenerator.generateNodeLayoutPreferences();

    long start = System.nanoTime();
    ClusterLayoutFinder layoutFinder =
      new ClusterLayoutFinder(traversalOrder, clusterTemplate, serviceNames, numMachines);
    int[] clusterlayout = layoutFinder.findValidNodeCounts();
    long dur = (System.nanoTime() - start) / 1000000;
    LOG.debug("took {} ms to find cluster layout", dur);

    if (clusterlayout == null) {
      return null;
    }

    Map<String, Node> clusterNodes = Maps.newHashMap();
    int nodeNum = 1000;
    for (int i = 0; i < clusterlayout.length; i++) {
      NodeLayout nodeLayout = traversalOrder.get(i);
      for (int j = 0; j < clusterlayout[i]; j++) {
        String nodeId = UUID.randomUUID().toString();
        Set<Service> nodeServices = Sets.newHashSet();
        for (String serviceName : nodeLayout.getServiceNames()) {
          nodeServices.add(serviceMap.get(serviceName));
        }
        String hardwaretype = nodeLayout.getHardwareTypeName();
        String imagetype = nodeLayout.getImageTypeName();
        String imageId = imageTypeMap.get(imagetype).get("image");
        // TODO: temporary workaround, need to refactor the task json
        String sshUser = imageTypeMap.get(imagetype).get("sshuser");
        String hostname = NodeService.createHostname(clusterName, clusterId, nodeNum, dnsSuffix);
        String flavor = hardwareTypeMap.get(hardwaretype);
        // TODO: these should be proper fields and logic for populating node properties should not be in the solver.
        NodeProperties nodeProperties = NodeProperties.builder()
          .setHostname(hostname)
          .setNodenum(nodeNum)
          .setHardwaretype(hardwaretype)
          .setImagetype(imagetype)
          .setFlavor(flavor)
          .setImage(imageId)
          .setSSHUser(sshUser)
          .setServices(nodeServices)
          .build();
        nodeNum++;
        clusterNodes.put(nodeId, new Node(nodeId, clusterId, nodeServices, nodeProperties));
      }
    }
    return clusterNodes;
  }
}
