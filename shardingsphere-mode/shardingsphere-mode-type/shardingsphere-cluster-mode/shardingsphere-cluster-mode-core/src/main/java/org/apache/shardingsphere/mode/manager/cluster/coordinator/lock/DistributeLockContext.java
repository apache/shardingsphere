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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock;

import com.google.common.base.Preconditions;
import com.google.common.eventbus.Subscribe;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.AckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.LockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.LockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distribute lock context.
 */
@RequiredArgsConstructor
public final class DistributeLockContext implements LockContext {
    
    private final Map<String, ShardingSphereGlobalLock> globalLocks = new ConcurrentHashMap<>();
    
    private final LockRegistryService lockRegistryService;
    
    private volatile ComputeNodeInstance currentInstance;
    
    private volatile Collection<ComputeNodeInstance> computeNodeInstances;
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        register(instanceContext);
        synchronizeGlobalLock();
    }
    
    private void register(final InstanceContext instanceContext) {
        currentInstance = instanceContext.getInstance();
        computeNodeInstances = instanceContext.getComputeNodeInstances();
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    private void synchronizeGlobalLock() {
        Collection<String> allGlobalLock = lockRegistryService.getAllGlobalSchemaLocks();
        if (allGlobalLock.isEmpty()) {
            lockRegistryService.initGlobalLockRoot();
            return;
        }
        for (String each : allGlobalLock) {
            Optional<String> schemaLock = LockNode.parseGlobalSchemaLocksNodePath(each);
            if (schemaLock.isPresent()) {
                String[] schemaInstanceId = LockNodeUtil.parseSchemaLockName(schemaLock.get());
                globalLocks.put(schemaInstanceId[0], crateGlobalLock(schemaInstanceId[1]));
            }
        }
    }
    
    @Override
    public synchronized ShardingSphereLock getOrCreateSchemaLock(final String schemaName) {
        Preconditions.checkNotNull(schemaName, "Get or create schema lock args schema name can not be null.");
        ShardingSphereGlobalLock result = globalLocks.get(schemaName);
        if (null != result) {
            return result;
        }
        result = crateGlobalLock(getCurrentInstanceId());
        globalLocks.put(schemaName, result);
        return result;
    }
    
    private ShardingSphereGlobalLock crateGlobalLock(final String ownerInstanceId) {
        return new ShardingSphereDistributeGlobalLock(lockRegistryService, currentInstance, computeNodeInstances, ownerInstanceId);
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
    }
    
    @Override
    public ShardingSphereLock getSchemaLock(final String schemaName) {
        Preconditions.checkNotNull(schemaName, "Get schema lock args schema name can not be null.");
        return globalLocks.get(schemaName);
    }
    
    @Override
    public boolean isLockedSchema(final String schemaName) {
        Preconditions.checkNotNull(schemaName, "Is locked schema args schema name can not be null.");
        if (globalLocks.isEmpty()) {
            return false;
        }
        ShardingSphereGlobalLock shardingSphereGlobalLock = globalLocks.get(schemaName);
        if (null != shardingSphereGlobalLock) {
            return shardingSphereGlobalLock.isLocked(schemaName);
        }
        return false;
    }
    
    private Optional<ShardingSphereGlobalLock> getGlobalLock(final String schemaName) {
        return Optional.ofNullable(globalLocks.get(schemaName));
    }
    
    private boolean isOwnerInstance(final String ownerInstanceId) {
        return getCurrentInstanceId().equals(ownerInstanceId);
    }
    
    /**
     * Locked event.
     *
     * @param event locked event
     */
    @Subscribe
    public synchronized void renew(final LockedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isOwnerInstance(ownerInstanceId)) {
            return;
        }
        ShardingSphereGlobalLock globalLock = globalLocks.get(schema);
        if (null == globalLock) {
            globalLock = crateGlobalLock(ownerInstanceId);
            globalLocks.put(schema, globalLock);
        }
        globalLock.ackLock(schema, getCurrentInstanceId());
        globalLock.refreshOwner(ownerInstanceId);
    }
    
    /**
     * Lock released event.
     *
     * @param event lock released event
     */
    @Subscribe
    public synchronized void renew(final LockReleasedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isOwnerInstance(ownerInstanceId)) {
            return;
        }
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> {
            shardingSphereGlobalLock.releaseAckLock(schema, getCurrentInstanceId());
        });
    }
    
    /**
     * Ack locked event.
     *
     * @param event ack locked event
     */
    @Subscribe
    public synchronized void renew(final AckLockedEvent event) {
        getGlobalLock(event.getSchema()).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(event.getLockedInstance()));
    }
    
    /**
     * Ack lock released event.
     *
     * @param event ack lock released event.
     */
    @Subscribe
    public synchronized void renew(final AckLockReleasedEvent event) {
        getGlobalLock(event.getSchema()).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.removeLockedInstance(event.getLockedInstance()));
    }
}
