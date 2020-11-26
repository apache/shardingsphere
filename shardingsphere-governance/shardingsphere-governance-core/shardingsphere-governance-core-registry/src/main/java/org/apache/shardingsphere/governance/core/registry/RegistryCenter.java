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

package org.apache.shardingsphere.governance.core.registry;

import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.governance.core.event.model.lock.GlobalLockAddedEvent;
import org.apache.shardingsphere.governance.core.registry.instance.GovernanceInstance;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;

import java.util.Collection;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private final RegistryCenterNode node;
    
    private final RegistryRepository repository;
    
    private final GovernanceInstance instance;

    public RegistryCenter(final RegistryRepository registryRepository) {
        node = new RegistryCenterNode();
        repository = registryRepository;
        instance = GovernanceInstance.getInstance();
        GovernanceEventBus.getInstance().register(this);
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        repository.persistEphemeral(node.getProxyNodePath(instance.getInstanceId()), "");
    }
    
    /**
     * Initialize data nodes.
     */
    public void persistDataNodes() {
        repository.persist(node.getDataNodesPath(), "");
    }
    
    /**
     * Persist instance data.
     * @param instanceData instance data
     */
    public void persistInstanceData(final String instanceData) {
        repository.persist(node.getProxyNodePath(instance.getInstanceId()), instanceData);
    }
    
    /**
     * Load instance data.
     * @return instance data
     */
    public String loadInstanceData() {
        return repository.get(node.getProxyNodePath(instance.getInstanceId()));
    }
    
    /**
     * Load disabled data sources.
     * 
     * @param schemaName schema name
     * @return Collection of disabled data sources
     */
    public Collection<String> loadDisabledDataSources(final String schemaName) {
        return loadDataSourcesBySchemaName(schemaName).stream().filter(each -> !Strings.isNullOrEmpty(getDataSourceNodeData(schemaName, each))
                && RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(getDataSourceNodeData(schemaName, each))).collect(Collectors.toList());
    }
    
    private Collection<String> loadDataSourcesBySchemaName(final String schemaName) {
        return repository.getChildrenKeys(node.getSchemaPath(schemaName));
    }
    
    private String getDataSourceNodeData(final String schemaName, final String dataSourceName) {
        return repository.get(node.getDataSourcePath(schemaName, dataSourceName));
    }
    
    /**
     * Lock instance after global lock added.
     * 
     * @param event global lock added event
     */
    @Subscribe
    public synchronized void lock(final GlobalLockAddedEvent event) {
        if (Optional.of(event).isPresent()) {
            persistInstanceData(RegistryCenterNodeStatus.LOCKED.toString());
        }
    }
}
