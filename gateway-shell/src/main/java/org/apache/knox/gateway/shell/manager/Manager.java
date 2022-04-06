/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.knox.gateway.shell.manager;

import org.apache.knox.gateway.shell.KnoxSession;

import java.util.List;

/**
 * Manages topology-related resources
 */
public class Manager {

  public static List<String> listDescriptors(KnoxSession session) throws Exception {
    return (new ListDescriptorsRequest(session)).execute();
  }


  public static void deployDescriptor(KnoxSession session, String name, String descriptorFileName) throws Exception {
    (new DeployResourceRequest(session, ResourceType.Descriptor, name, descriptorFileName)).execute();
  }


  public static void undeployDescriptor(KnoxSession session, String name) throws Exception {
    (new UndeployResourceRequest(session, ResourceType.Descriptor, name)).execute();
  }


  public static List<String> listProviderConfigurations(KnoxSession session) throws Exception {
    return (new ListProviderConfigurationsRequest(session)).execute();
  }


  public static void deployProviderConfiguration(KnoxSession session,
                                                 String name,
                                                 String providerConfigFileName) throws Exception {
    (new DeployResourceRequest(session, ResourceType.ProviderConfiguration, name, providerConfigFileName)).execute();
  }


  public static void undeployProviderConfiguration(KnoxSession session, String name) throws Exception {
    (new UndeployResourceRequest(session, ResourceType.ProviderConfiguration, name)).execute();
  }


  public static List<String> listTopologies(KnoxSession session) throws Exception {
    return (new ListTopologiesRequest(session)).execute();
  }

}
