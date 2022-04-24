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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.DatabaseLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.event.DatabaseLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;

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
        Collection<String> allGlobalLock = lockRegistryService.getAllGlobalDatabaseLocks();
        if (allGlobalLock.isEmpty()) {
            lockRegistryService.initGlobalLockRoot();
            return;
        }
        for (String each : allGlobalLock) {
            Optional<String> databaseLock = LockNode.parseGlobalDatabaseLocksNodePath(each);
            databaseLock.ifPresent(database -> globalLocks.put(database, crateGlobalLock()));
        }
    }
    
    @Override
    public synchronized ShardingSphereLock getOrCreateDatabaseLock(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Get or create database lock args database name can not be null.");
        ShardingSphereGlobalLock result = globalLocks.get(databaseName);
        if (null != result) {
            return result;
        }
        result = crateGlobalLock();
        globalLocks.put(databaseName, result);
        return result;
    }
    
    private ShardingSphereGlobalLock crateGlobalLock() {
        return new ShardingSphereDistributeGlobalLock(lockRegistryService, currentInstance, computeNodeInstances);
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
    }
    
    @Override
    public ShardingSphereLock getDatabaseLock(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Get database lock args database name can not be null.");
        return globalLocks.get(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        if (globalLocks.isEmpty()) {
            return false;
        }
        ShardingSphereGlobalLock shardingSphereGlobalLock = globalLocks.get(databaseName);
        if (null != shardingSphereGlobalLock) {
            return shardingSphereGlobalLock.isLocked(databaseName);
        }
        return false;
    }
    
    private Optional<ShardingSphereGlobalLock> getGlobalLock(final String databaseName) {
        return Optional.ofNullable(globalLocks.get(databaseName));
    }
    
    /**
     * Locked event.
     *
     * @param event locked event
     */
    @Subscribe
    public synchronized void renew(final DatabaseLockedEvent event) {
        String database = event.getDatabase();
        ShardingSphereGlobalLock globalLock = globalLocks.get(database);
        if (null == globalLock) {
            globalLock = crateGlobalLock();
            globalLocks.put(database, globalLock);
        }
        globalLock.ackLock(database, getCurrentInstanceId());
    }
    
    /**
     * Lock released event.
     *
     * @param event lock released event
     */
    @Subscribe
    public synchronized void renew(final DatabaseLockReleasedEvent event) {
        String database = event.getDatabase();
        getGlobalLock(database).ifPresent(lock -> lock.releaseAckLock(database, getCurrentInstanceId()));
    }
    
    /**
     * Ack locked event.
     *
     * @param event ack locked event
     */
    @Subscribe
    public synchronized void renew(final AckLockedEvent event) {
        getGlobalLock(event.getDatabase()).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.addLockedInstance(event.getLockedInstance()));
    }
    
    /**
     * Ack lock released event.
     *
     * @param event ack lock released event.
     */
    @Subscribe
    public synchronized void renew(final AckLockReleasedEvent event) {
        getGlobalLock(event.getDatabase()).ifPresent(shardingSphereGlobalLock -> shardingSphereGlobalLock.removeLockedInstance(event.getLockedInstance()));
    }
}
