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

import co.cask.coopr.cluster.Node;
import co.cask.coopr.spec.service.Service;
import co.cask.coopr.spec.template.ClusterTemplate;
import co.cask.coopr.spec.template.Compatibilities;
import co.cask.coopr.spec.template.Constraints;
import co.cask.coopr.spec.template.ServiceConstraint;
import com.google.common.base.Objects;
import com.google.common.collect.HashMultiset;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import com.google.common.collect.Sets;

import java.util.Map;
import java.util.Set;

/**
 * Class describing the layout of a cluster, giving a mapping of {@link NodeLayout} to how many nodes use that layout.
 */
public class ClusterLayout {
  private final Constraints constraints;
  private final Multiset<NodeLayout> layout;
  private final Multiset<String> serviceCounts;

  public ClusterLayout(Constraints constraints, Multiset<NodeLayout> layout) {
    this.constraints = constraints;
    this.layout = ImmutableMultiset.copyOf(layout);
    this.serviceCounts = HashMultiset.create();
    for (Multiset.Entry<NodeLayout> entry : layout.entrySet()) {
      for (String service : entry.getElement().getServiceNames()) {
        serviceCounts.add(service, entry.getCount());
      }
    }
  }

  /**
   * Returns the cluster layout, which maps each node layout to how many nodes have that layout.
   *
   * @return Layout of the cluster.
   */
  public Multiset<NodeLayout> getLayout() {
    return layout;
  }

  public Constraints getConstraints() {
    return constraints;
  }

  /**
   * Returns whether or not the cluster layout is valid based on the constraints it has.
   *
   * @return True if the cluster layout is valid, false if not.
   */
  public boolean isValid() {
    return satisfiesConstraints(constraints);
  }

  public boolean isCompatibleWithTemplate(ClusterTemplate template) {
    // check all services are compatible
    Compatibilities compatibilities = template.getCompatibilities();
    if (!compatibilities.compatibleWithServices(serviceCounts.elementSet())) {
      return false;
    }
    // check all image types and hardware types are compatible
    Set<String> imageTypes = Sets.newHashSet();
    Set<String> hardwareTypes = Sets.newHashSet();
    for (NodeLayout nodeLayout : layout.elementSet()) {
      imageTypes.add(nodeLayout.getImageTypeName());
      hardwareTypes.add(nodeLayout.getHardwareTypeName());
    }
    // check all hardware types are compatible
    if (!compatibilities.compatibleWithImageTypes(imageTypes) ||
      !compatibilities.compatibleWithHardwareTypes(hardwareTypes)) {
      return false;
    }

    return satisfiesConstraints(template.getConstraints());
  }

  /**
   * Derive a ClusterLayout from a set of {@link Node}s and some {@link Constraints}.
   *
   * @param clusterNodes Nodes to derive the layout from.
   * @param constraints Constraints for the cluster layout.
   * @return ClusterLayout derived from the nodes.
   */
  public static ClusterLayout fromNodes(Set<Node> clusterNodes, Constraints constraints) {
    Multiset<NodeLayout> nodeLayoutCounts = HashMultiset.create();
    for (Node node : clusterNodes) {
      Set<String> nodeServices = Sets.newHashSet();
      for (Service service : node.getServices()) {
        nodeServices.add(service.getName());
      }
      String hardwareType = node.getProperties().getHardwaretype();
      String imageType = node.getProperties().getImagetype();
      nodeLayoutCounts.add(new NodeLayout(hardwareType, imageType, nodeServices));
    }
    return new ClusterLayout(constraints, nodeLayoutCounts);
  }

  private boolean satisfiesConstraints(Constraints constraints) {
    // check node layouts
    Set<String> clusterServices = serviceCounts.elementSet();
    for (NodeLayout nodeLayout : layout.elementSet()) {
      if (!nodeLayout.satisfiesConstraints(constraints, clusterServices)) {
        return false;
      }
    }

    // check service counts
    Map<String, ServiceConstraint> serviceConstraints = constraints.getServiceConstraints();
    for (Multiset.Entry<String> entry : serviceCounts.entrySet()) {
      ServiceConstraint constraint = serviceConstraints.get(entry.getElement());
      if (constraint != null) {
        int serviceCount = entry.getCount();
        // TODO: ratio constraint
        if (serviceCount < constraint.getMinCount() || serviceCount > constraint.getMaxCount()
          || serviceCount > layout.size()) {
          return false;
        }
      }
    }
    return true;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ClusterLayout)) {
      return false;
    }

    ClusterLayout that = (ClusterLayout) o;

    return Objects.equal(constraints, that.constraints) &&
      Objects.equal(layout, that.layout) &&
      Objects.equal(serviceCounts, that.serviceCounts);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(constraints, layout, serviceCounts);
  }
}
