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
package co.cask.coopr.scheduler.callback;

import co.cask.coopr.cluster.Node;
import co.cask.coopr.common.conf.Configuration;
import co.cask.coopr.common.conf.Constants;
import co.cask.coopr.scheduler.ClusterAction;
import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.config.SocketConfig;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.Set;

/**
 * Executes before and after hooks by sending an HTTP POST request to some configurable endpoints, with the post body
 * containing the cluster and job objects, assuming there is a valid url assigned to the start, success, and/or failure
 * urls. If no url is specified, no request will be sent. Additionally, trigger actions can be configured so that
 * the HTTP POST request is sent only for specific cluster actions. This is done by specifying a comma separated list
 * of {@link ClusterAction}s in the configuration for start, success, and/or triggers.
 */
public class HttpPostClusterCallback implements ClusterCallback {
  private static final Logger LOG = LoggerFactory.getLogger(HttpPostClusterCallback.class);
  private final Gson gson = new Gson();
  private String onStartUrl;
  private String onSuccessUrl;
  private String onFailureUrl;
  private Set<ClusterAction> startTriggerActions;
  private Set<ClusterAction> successTriggerActions;
  private Set<ClusterAction> failureTriggerActions;
  private HttpClient httpClient;

  public void initialize(Configuration conf) {
    this.onStartUrl = conf.get(Constants.HttpCallback.START_URL);
    this.onSuccessUrl = conf.get(Constants.HttpCallback.SUCCESS_URL);
    this.onFailureUrl = conf.get(Constants.HttpCallback.FAILURE_URL);
    this.startTriggerActions = parseActionsString(conf.get(Constants.HttpCallback.START_TRIGGERS,
                                                            Constants.HttpCallback.DEFAULT_START_TRIGGERS));
    this.successTriggerActions = parseActionsString(conf.get(Constants.HttpCallback.SUCCESS_TRIGGERS,
                                                           Constants.HttpCallback.DEFAULT_SUCCESS_TRIGGERS));
    this.failureTriggerActions = parseActionsString(conf.get(Constants.HttpCallback.FAILURE_TRIGGERS,
                                                             Constants.HttpCallback.DEFAULT_FAILURE_TRIGGERS));
    if (onStartUrl != null) {
      LOG.debug("before hook will be triggered on actions {}", Joiner.on(',').join(startTriggerActions));
    }
    if (onSuccessUrl != null) {
      LOG.debug("after hook will be triggered on actions {}", Joiner.on(',').join(successTriggerActions));
    }
    if (onFailureUrl != null) {
      LOG.debug("after hook will be triggered on actions {}", Joiner.on(',').join(failureTriggerActions));
    }

    int maxConnections = conf.getInt(Constants.HttpCallback.MAX_CONNECTIONS,
                                     Constants.HttpCallback.DEFAULT_MAX_CONNECTIONS);
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultMaxPerRoute(maxConnections);
    connectionManager.setMaxTotal(maxConnections);

    SocketConfig socketConfig = SocketConfig.custom()
      .setSoTimeout(conf.getInt(Constants.HttpCallback.SOCKET_TIMEOUT,
                                Constants.HttpCallback.DEFAULT_SOCKET_TIMEOUT))
      .build();
    connectionManager.setDefaultSocketConfig(socketConfig);
    this.httpClient = HttpClientBuilder.create().setConnectionManager(connectionManager).build();
  }

  private Set<ClusterAction> parseActionsString(String actionsStr) {
    if (actionsStr == null) {
      return ImmutableSet.of();
    }

    Iterator<String> actionIter = Splitter.on(',').split(actionsStr).iterator();
    Set<ClusterAction> actions = Sets.newHashSet();
    while (actionIter.hasNext()) {
      String actionStr = actionIter.next();
      try {
        ClusterAction action = ClusterAction.valueOf(actionStr.toUpperCase());
        actions.add(action);
      } catch (IllegalArgumentException e) {
        LOG.warn("Unknown cluster action " + actionStr + ". Hooks will not be executed for that action");
      }
    }
    return actions;
  }

  public boolean onStart(CallbackData data, CallbackContext context) {
    ClusterAction jobAction = data.getJob().getClusterAction();
    if (startTriggerActions.contains(jobAction)) {
      if (onStartUrl != null) {
        LOG.debug("sending request to {} before performing {} on cluster {}",
                  onStartUrl, jobAction, data.getCluster().getId());
        sendPost(onStartUrl, data, context);
      }
    }
    return true;
  }

  public void onSuccess(CallbackData data, CallbackContext context) {
    ClusterAction jobAction = data.getJob().getClusterAction();
    if (successTriggerActions.contains(data.getJob().getClusterAction())) {
      if (onSuccessUrl != null) {
        LOG.debug("{} completed successfully on cluster {}, sending request to {}",
                  jobAction, data.getCluster().getId(), onSuccessUrl);
        sendPost(onSuccessUrl, data, context);
      }
    }
  }

  @Override
  public void onFailure(CallbackData data, CallbackContext context) {
    ClusterAction jobAction = data.getJob().getClusterAction();
    if (failureTriggerActions.contains(data.getJob().getClusterAction())) {
      if (onFailureUrl != null) {
        LOG.debug("{} failed on cluster {}, sending request to {}",
                  jobAction, data.getCluster().getId(), onFailureUrl);
        sendPost(onFailureUrl, data, context);
      }
    }
  }

  private void sendPost(String url, CallbackData data, CallbackContext context) {
    HttpPost post = new HttpPost(url);
    Set<Node> nodes;
    try {
      nodes = context.getClusterStoreView().getClusterNodes(data.getCluster().getId());
    } catch (Exception e) {
      LOG.error("Unable to fetch nodes for cluster {}, not sending post request.", data.getCluster().getId());
      return;
    }

    try {
      JsonObject body = new JsonObject();
      body.add("cluster", gson.toJsonTree(data.getCluster()));
      body.add("job", gson.toJsonTree(data.getJob()));
      body.add("nodes", gson.toJsonTree(nodes));
      post.setEntity(new StringEntity(gson.toJson(body)));
      httpClient.execute(post);
    } catch (UnsupportedEncodingException e) {
      LOG.warn("Exception setting http post body", e);
    } catch (ClientProtocolException e) {
      LOG.warn("Exception executing http post callback to " + url, e);
    } catch (IOException e) {
      LOG.warn("Exception executing http post callback to " + url, e);
    } catch (Exception e) {
      LOG.warn("Exception executing http post callback to " + url, e);
    } finally {
      post.releaseConnection();
    }
  }
}
