/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.knox.gateway.services.registry;

import java.util.Set;

import org.apache.knox.gateway.service.definition.ServiceDefinitionChangeListener;
import org.apache.knox.gateway.service.definition.ServiceDefinitionPair;

public interface ServiceDefinitionRegistry {

  ServiceDefEntry getMatchingService(String urlPattern);

  Set<ServiceDefinitionPair> getServiceDefinitions();

  void saveServiceDefinition(ServiceDefinitionPair serviceDefinition) throws ServiceDefinitionRegistryException;

  void saveOrUpdateServiceDefinition(ServiceDefinitionPair serviceDefinition) throws ServiceDefinitionRegistryException;

  void deleteServiceDefinition(String name, String role, String version) throws ServiceDefinitionRegistryException;

  void addServiceDefinitionChangeListener(ServiceDefinitionChangeListener listener);

}
