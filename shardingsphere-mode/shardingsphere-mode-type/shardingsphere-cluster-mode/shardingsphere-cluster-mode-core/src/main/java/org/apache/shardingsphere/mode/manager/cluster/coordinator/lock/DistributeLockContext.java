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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.GlobalLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.GlobalLockRegistryService;
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
    
    private final GlobalLockRegistryService globalLockService;
    
    private volatile ComputeNodeInstance currentInstance;
    
    private volatile Collection<ComputeNodeInstance> computeNodeInstances;
    
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
        return new ShardingSphereDistributeGlobalLock(currentInstance, ownerInstanceId, globalLockService, computeNodeInstances);
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
    }
    
    @Override
    public Optional<ShardingSphereLock> getSchemaLock(final String schemaName) {
        if (null == schemaName) {
            return Optional.empty();
        }
        return Optional.ofNullable(globalLocks.get(schemaName));
    }
    
    @Override
    public synchronized boolean isLockedSchema(final String schemaName) {
        Preconditions.checkNotNull(schemaName, "Is locked schema args schema name can not be null.");
        return getGlobalLock(schemaName).map(shardingSphereGlobalLock -> shardingSphereGlobalLock.isLocked(schemaName)).orElse(false);
    }
    
    private Optional<ShardingSphereGlobalLock> getGlobalLock(final String schemaName) {
        return Optional.ofNullable(globalLocks.get(schemaName));
    }
    
    /**
     * Synchronize global lock.
     *
     * @param instanceContext instance context
     */
    public void synchronizeGlobalLock(final InstanceContext instanceContext) {
        init(instanceContext);
        Collection<String> allGlobalLock = globalLockService.synchronizeAllGlobalLock();
        if (allGlobalLock.isEmpty()) {
            globalLockService.initGlobalLockRoot();
            return;
        }
        for (String each : allGlobalLock) {
            String[] schemaInstanceId = LockNodeUtil.parseLockName(each);
            globalLocks.put(schemaInstanceId[0], crateGlobalLock(schemaInstanceId[1]));
        }
    }
    
    private void init(final InstanceContext instanceContext) {
        currentInstance = instanceContext.getInstance();
        computeNodeInstances = instanceContext.getComputeNodeInstances();
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    private boolean isSameInstanceId(final String instanceId) {
        return getCurrentInstanceId().equals(instanceId);
    }
    
    /**
     * Locked event.
     *
     * @param event locked event
     */
    @Subscribe
    public synchronized void renew(final GlobalLockedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            return;
        }
        Optional<ShardingSphereGlobalLock> globalLock = getGlobalLock(schema);
        ShardingSphereGlobalLock lock;
        if (globalLock.isPresent()) {
            lock = globalLock.get();
        } else {
            lock = crateGlobalLock(ownerInstanceId);
            globalLocks.put(schema, lock);
        }
        lock.ackLock(schema, getCurrentInstanceId());
    }
    
    /**
     * Lock released event.
     *
     * @param event lock released event
     */
    @Subscribe
    public synchronized void renew(final GlobalLockReleasedEvent event) {
        String schema = event.getSchema();
        String ownerInstanceId = event.getOwnerInstanceId();
        if (isSameInstanceId(ownerInstanceId)) {
            ShardingSphereGlobalLock shardingSphereGlobalLock = globalLocks.get(schema);
            if (null == shardingSphereGlobalLock) {
                return;
            }
            shardingSphereGlobalLock.releaseLockedState(schema);
            globalLocks.remove(schema);
            return;
        }
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> {
            shardingSphereGlobalLock.releaseAckLock(schema, getCurrentInstanceId());
            globalLocks.remove(schema);
        });
    }
    
    /**
     * Ack locked event.
     *
     * @param event ack locked event
     */
    @Subscribe
    public synchronized void renew(final GlobalAckLockedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
    
    /**
     * Ack lock released event.
     *
     * @param event ack lock released event.
     */
    @Subscribe
    public synchronized void renew(final GlobalAckLockReleasedEvent event) {
        String schema = event.getSchema();
        String lockedInstanceId = event.getLockedInstanceId();
        if (isSameInstanceId(lockedInstanceId)) {
            globalLocks.remove(schema);
            return;
        }
        getGlobalLock(schema).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(lockedInstanceId));
    }
}
