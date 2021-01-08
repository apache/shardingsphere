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
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.instance.GovernanceInstance;
import org.apache.shardingsphere.governance.repository.api.RegistryRepository;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;

import java.util.Collection;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private static final int CHECK_RETRY_MAXIMUM = 5;
    
    private static final int CHECK_RETRY_INTERVAL_SECONDS = 3;
    
    private final RegistryCenterNode node;
    
    private final RegistryRepository repository;
    
    private final GovernanceInstance instance;
    
    private final LockNode lockNode;
    
    public RegistryCenter(final RegistryRepository registryRepository) {
        node = new RegistryCenterNode();
        repository = registryRepository;
        instance = GovernanceInstance.getInstance();
        lockNode = new LockNode();
        registryRepository.initLock(lockNode.getGlobalLockNodePath());
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Persist data source disabled state.
     *
     * @param event data source disabled event
     */
    @Subscribe
    public synchronized void renew(final DataSourceDisabledEvent event) {
        String value = event.isDisabled() ? RegistryCenterNodeStatus.DISABLED.toString() : "";
        repository.persist(node.getDataSourcePath(event.getSchemaName(), event.getDataSourceName()), value);
    }
    
    /**
     * Persist primary data source state.
     *
     * @param event primary data source event
     */
    @Subscribe
    public synchronized void renew(final PrimaryDataSourceEvent event) {
        repository.persist(node.getPrimaryDataSourcePath(event.getSchemaName(), event.getGroupName()), event.getDataSourceName());
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
     * Initialize primary nodes.
     */
    public void persistPrimaryNodes() {
        repository.persist(node.getPrimaryNodesPath(), "");
    }
    
    /**
     * Persist instance data.
     * 
     * @param instanceData instance data
     */
    public void persistInstanceData(final String instanceData) {
        repository.persist(node.getProxyNodePath(instance.getInstanceId()), instanceData);
    }
    
    /**
     * Load instance data.
     * 
     * @return instance data
     */
    public String loadInstanceData() {
        return repository.get(node.getProxyNodePath(instance.getInstanceId()));
    }
    
    /**
     * Load instance data.
     * 
     * @param instanceId instance id
     * @return instance data
     */
    public String loadInstanceData(final String instanceId) {
        return repository.get(node.getProxyNodePath(instanceId));
    }
    
    /**
     * Load all instances.
     * 
     * @return collection of all instances
     */
    public Collection<String> loadAllInstances() {
        return repository.getChildrenKeys(node.getProxyNodesPath());
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
     * Try to get global lock.
     *
     * @param timeout the maximum time in milliseconds to acquire lock
     * @param timeUnit time unit
     * @return true if get the lock, false if not
     */
    public boolean tryGlobalLock(final long timeout, final TimeUnit timeUnit) {
        return repository.tryLock(timeout, timeUnit);
    }
    
    /**
     * Release global lock.
     */
    public void releaseGlobalLock() {
        repository.releaseLock();
        repository.delete(lockNode.getGlobalLockNodePath());
    }
    
    /**
     * Check lock state.
     *
     * @return true if all instances were locked, else false
     */
    public boolean checkLock() {
        return checkOrRetry(this.loadAllInstances());
    }
    
    private boolean checkOrRetry(final Collection<String> instanceIds) {
        for (int i = 0; i < CHECK_RETRY_MAXIMUM; i++) {
            if (check(instanceIds)) {
                return true;
            }
            try {
                Thread.sleep(CHECK_RETRY_INTERVAL_SECONDS * 1000L);
                // CHECKSTYLE:OFF
            } catch (final InterruptedException ex) {
                // CHECKSTYLE:ON
            }
        }
        return false;
    }
    
    private boolean check(final Collection<String> instanceIds) {
        for (String each : instanceIds) {
            if (!RegistryCenterNodeStatus.LOCKED.toString().equalsIgnoreCase(this.loadInstanceData(each))) {
                return false;
            }
        }
        return true;
    }
}
