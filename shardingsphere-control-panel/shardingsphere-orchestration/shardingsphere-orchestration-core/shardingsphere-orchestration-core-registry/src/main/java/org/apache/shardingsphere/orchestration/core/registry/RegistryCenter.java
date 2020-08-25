/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.orchestration.core.registry;

import com.google.common.base.Strings;
import org.apache.shardingsphere.orchestration.repository.api.RegistryRepository;
import org.apache.shardingsphere.orchestration.core.registry.instance.OrchestrationInstance;

import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private final RegistryCenterNode node;
    
    private final RegistryRepository repository;
    
    private final OrchestrationInstance instance;

    public RegistryCenter(final RegistryRepository registryRepository) {
        node = new RegistryCenterNode();
        repository = registryRepository;
        instance = OrchestrationInstance.getInstance();
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        repository.persistEphemeral(node.getInstancesNodeFullPath(instance.getInstanceId()), "state: " + RegistryCenterNodeStatus.ONLINE);
    }
    
    /**
     * Initialize data sources node.
     */
    public void persistDataSourcesNode() {
        repository.persist(node.getDataSourcesNodeFullRootPath(), "");
    }
    
    /**
     * Persist instance data.
     * @param instanceData instance data
     */
    public void persistInstanceData(final String instanceData) {
        repository.persist(node.getInstancesNodeFullPath(instance.getInstanceId()), instanceData);
    }
    
    /**
     * Load instance data.
     * @return instance data
     */
    public String loadInstanceData() {
        return repository.get(node.getInstancesNodeFullPath(instance.getInstanceId()));
    }
    
    /**
     * Load all data sources nodes.
     *
     * @return Collection of all data sources nodes
     */
    public Collection<String> loadAllDataSourcesNodes() {
        return repository.getChildrenKeys(node.getDataSourcesNodeFullRootPath());
    }
    
    /**
     * Get data sources node data.
     *
     * @param schemaDataSourceName schema name and data source name
     * @return data sources node data
     */
    public String getDataSourcesNodeData(final String schemaDataSourceName) {
        return repository.get(node.getDataSourcesNodeFullPath(schemaDataSourceName));
    }
    
    /**
     * Load disabled data sources.
     *
     * @return Collection of disabled data sources
     */
    public Collection<String> loadDisabledDataSources() {
        return loadAllDataSourcesNodes().stream().filter(each -> !Strings.isNullOrEmpty(getDataSourcesNodeData(each))
                && RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(getDataSourcesNodeData(each))).collect(Collectors.toList());
    }
}
