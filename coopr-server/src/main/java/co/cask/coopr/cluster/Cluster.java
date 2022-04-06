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
package co.cask.coopr.cluster;

import co.cask.coopr.account.Account;
import co.cask.coopr.spec.NamedEntity;
import co.cask.coopr.spec.Provider;
import co.cask.coopr.spec.template.ClusterTemplate;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.JsonObject;

import java.util.Set;

/**
 * Represents a cluster of machines.
 */
public final class Cluster extends NamedEntity {

  /**
   * Cluster status.
   */
  public enum Status {
    /**
     * Some job is currently running on the cluster.
     */
    PENDING,
    /**
     * All jobs completed successfully, cluster is ready to use.
     */
    ACTIVE,
    /**
     * Cluster creation did not complete, cluster may not be ready to use.
     */
    INCOMPLETE,
    /**
     * Cluster used to be ACTIVE, but a previous operation failed leaving it in an inconsistent state.
     */
    INCONSISTENT,
    /**
     * The cluster no longer exists.
     */
    TERMINATED;

    public static final Set<Status> CONFIGURABLE_STATES = ImmutableSet.of(Status.ACTIVE, Status.INCONSISTENT);
    public static final Set<Status> SERVICE_ACTIONABLE_STATES = ImmutableSet.of(Status.ACTIVE);
  }

  private final String id;
  private final String description;
  private final long createTime;
  private long expireTime;
  private Provider provider;
  private ClusterTemplate clusterTemplate;
  private Set<String> nodes;
  private Set<String> services;
  private String latestJobId;
  private Account account;
  private Status status;
  private JsonObject config;

  private Cluster(String id, Account account, String name, Long createTime,
                  Long expireTime, String description, Provider provider,
                  ClusterTemplate clusterTemplate, Set<String> nodes,
                  Set<String> services, JsonObject config, Cluster.Status status, String latestJobId) {
    super(name);
    Preconditions.checkArgument(account != null, "cluster account must be specified.");
    this.id = id;
    this.account = account;
    this.description = description;
    this.createTime = createTime == null ? System.currentTimeMillis() : createTime;
    this.expireTime = expireTime == null ? 0 : expireTime;
    this.provider = provider;
    this.clusterTemplate = clusterTemplate;
    this.nodes = nodes == null ? Sets.<String>newHashSet() : Sets.newHashSet(nodes);
    this.services = services == null ? Sets.<String>newHashSet() : Sets.newHashSet(services);
    this.latestJobId = latestJobId;
    this.status = status == null ? Status.PENDING : status;
    this.config = config == null ? new JsonObject() : config;
  }

  /**
   * Get the id of the cluster.
   *
   * @return Id of the cluster.
   */
  public String getId() {
    return id;
  }

  /**
   * Get the account of the owner of the cluster.
   *
   * @return Account of the owner of the cluster.
   */
  public Account getAccount() {
    return account;
  }

  /**
   * Get the description of the cluster.
   *
   * @return Description of the cluster.
   */
  public String getDescription() {
    return description;
  }

  /**
   * Get the timestamp in milliseconds of when the cluster was created. Create time refers to when the server began
   * creating the cluster, and not the time when the actual cluster create operation completed.
   *
   * @return Timestamp in milliseconds of when the cluster was created.
   */
  public long getCreateTime() {
    return createTime;
  }

  /**
   * Get the timestamp in milliseconds of when the cluster will expire. An expire time of 0 indicates the cluster
   * will never expire.
   *
   * @return Timestamp in milliseconds of when the cluster will expire, with 0 meaning never.
   */
  public long getExpireTime() {
    return expireTime;
  }

  /**
   * Get the set of node ids in the cluster.
   *
   * @return Set of node ids in the cluster.
   */
  public Set<String> getNodeIDs() {
    return nodes;
  }

  /**
   * Get the {@link Provider} used to create and delete machines for the cluster. It should be noted that this is a
   * copy a provider, meaning it may get out of sync with the current instance of the provider.
   *
   * @return Provider used to create and delete machines for the cluster.
   */
  public Provider getProvider() {
    return provider;
  }

  /**
   * Get the {@link ClusterTemplate} used with the cluster. It should be noted that the cluster keeps its own copy
   * of the cluster template, so it is possible for the template to get out of sync with the most recent version of
   * the template.
   *
   * @return Cluster template to be used with the cluster.
   */
  public ClusterTemplate getClusterTemplate() {
    return clusterTemplate;
  }

  /**
   * Get the names of services placed on the cluster.
   *
   * @return Set of service names placed on the cluster.
   */
  public Set<String> getServices() {
    return services;
  }

  /**
   * Get the id of the most recent job performed, or being performed, on the cluster.
   *
   * @return Id of the most recent job performed, or being performed, on the cluster.
   */
  public String getLatestJobId() {
    return latestJobId;
  }

  /**
   * Set the latest job of the cluster.
   *
   * @param jobId Id of the latest cluster job.
   */
  public void setLatestJobId(String jobId) {
    latestJobId = jobId;
  }

  /**
   * Get the {@link Status} of the cluster.
   *
   * @return Status of the cluster.
   */
  public Status getStatus() {
    return status;
  }

  /**
   * Get the cluster config json object.
   *
   * @return Cluster config json object.
   */
  public JsonObject getConfig() {
    return config;
  }

  /**
   * Set the expire time of the cluster as a timestamp in milliseconds. An expire time of 0 means the cluster will
   * not expire. Only sets the expire time in this Java object. A separate call must
   * be made to persistently store changes in expiration time.
   *
   * @param expireTime Expire time of the cluster as a timestamp in milliseconds, with 0 meaning no expiration.
   */
  public void setExpireTime(long expireTime) {
    this.expireTime = expireTime;
  }

  /**
   * Set the status of the cluster. Only sets the status in this Java object. A separate call must be made to
   * persistently store changes in status.
   *
   * @param status Status of the cluster to set.
   */
  public void setStatus(Status status) {
    this.status = status;
  }

  /**
   * Set the provider to use for creating and deleting machines. Only sets the provider in this Java object.
   * A separate call must be made to persistently store changes in provider.
   *
   * @param provider Provider to use for creating and deleting machines.
   */
  public void setProvider(Provider provider) {
    this.provider = provider;
  }

  /**
   * Set the cluster template to use for cluster operations. Only sets the template in this Java object.
   * A separate call must be made to persistently store changes in template.
   *
   * @param clusterTemplate Cluster template to use for cluster operations.
   */
  public void setClusterTemplate(ClusterTemplate clusterTemplate) {
    this.clusterTemplate = clusterTemplate;
  }

  /**
   * Set the ids of the nodes in the cluster. Only sets the node ids in this Java object.
   * A separate call must be made to persistently store changes in node ids.
   *
   * @param nodes Set of ids of the nodes in the cluster.
   */
  public void setNodes(Set<String> nodes) {
    this.nodes = ImmutableSet.copyOf(nodes);
  }

  /**
   * Set the names of the services on the cluster. Only sets the services in this Java object.
   * A separate call must be made to persistently store changes in services.
   *
   * @param services Names of services placed on the cluster.
   */
  public void setServices(Set<String> services) {
    this.services = ImmutableSet.copyOf(services);
  }

  /**
   * Set the config for the cluster. Only sets the config in this Java object.
   * A separate call must be made to persistently store changes in config.
   *
   * @param config Cluster configuration to use.
   */
  public void setConfig(JsonObject config) {
    this.config = config;
  }

  /**
   * Get a builder for creating a cluster object.
   *
   * @return Builder for creating a cluster object.
   */
  public static Builder builder() {
    return new Builder();
  }

  /**
   * Builds a cluster object.
   */
  public static class Builder {
    private String name;
    private String id;
    private String description;
    private long createTime;
    private long expireTime;
    private Provider provider;
    private ClusterTemplate clusterTemplate;
    private Set<String> nodes;
    private Set<String> services;
    private String latestJobId;
    private Account account;
    private Status status;
    private JsonObject config;

    public Builder setName(String name) {
      this.name = name;
      return this;
    }

    public Builder setID(String id) {
      this.id = id;
      return this;
    }

    public Builder setDescription(String description) {
      this.description = description;
      return this;
    }

    public Builder setCreateTime(long createTime) {
      this.createTime = createTime;
      return this;
    }

    public Builder setExpireTime(long expireTime) {
      this.expireTime = expireTime;
      return this;
    }

    public Builder setProvider(Provider provider) {
      this.provider = provider;
      return this;
    }

    public Builder setClusterTemplate(ClusterTemplate clusterTemplate) {
      this.clusterTemplate = clusterTemplate;
      return this;
    }

    public Builder setNodes(Set<String> nodes) {
      this.nodes = nodes;
      return this;
    }

    public Builder setServices(Set<String> services) {
      this.services = services;
      return this;
    }

    public Builder setLatestJobID(String latestJobID) {
      this.latestJobId = latestJobID;
      return this;
    }

    public Builder setAccount(Account account) {
      this.account = account;
      return this;
    }

    public Builder setStatus(Status status) {
      this.status = status;
      return this;
    }

    public Builder setConfig(JsonObject config) {
      this.config = config;
      return this;
    }

    public Cluster build() {
      return new Cluster(id, account, name, createTime, expireTime, description, provider, clusterTemplate,
                         nodes, services, config, status, latestJobId);
    }
  }

  @Override
  public String toString() {
    return Objects.toStringHelper(this)
      .add("id", id)
      .add("account", account)
      .add("name", getName())
      .add("description", description)
      .add("createTime", createTime)
      .add("expireTime", expireTime)
      .add("provider", provider)
      .add("clusterTemplate", clusterTemplate)
      .add("nodes", nodes)
      .add("services", services)
      .add("latestJobId", latestJobId)
      .add("status", status)
      .add("config", config)
      .toString();
  }

  @Override
  public boolean equals(Object o) {
    if (!(o instanceof Cluster)) {
      return false;
    }
    Cluster other = (Cluster) o;
    return Objects.equal(id, other.id) &&
      Objects.equal(name, other.name) &&
      Objects.equal(description, other.description) &&
      Objects.equal(createTime, other.createTime) &&
      Objects.equal(expireTime, other.expireTime) &&
      Objects.equal(provider, other.provider) &&
      Objects.equal(clusterTemplate, other.clusterTemplate) &&
      Objects.equal(nodes, other.nodes) &&
      Objects.equal(services, other.services) &&
      Objects.equal(latestJobId, other.latestJobId) &&
      Objects.equal(config, other.config);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(id, name, description, createTime, expireTime, provider, clusterTemplate,
                            nodes, services, latestJobId, config);
  }
}
