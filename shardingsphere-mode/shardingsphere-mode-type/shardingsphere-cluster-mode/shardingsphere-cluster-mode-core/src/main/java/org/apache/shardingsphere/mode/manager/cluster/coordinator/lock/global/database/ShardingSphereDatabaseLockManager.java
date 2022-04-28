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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.lock.LockType;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database.event.DatabaseAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database.event.DatabaseAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database.event.DatabaseLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.database.event.DatabaseLockedEvent;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Database lock manager of ShardingSphere.
 */
public final class ShardingSphereDatabaseLockManager implements ShardingSphereLockManager {
    
    private final Map<String, ShardingSphereDatabaseLock> locks;
    
    private final LockNodeService lockNodeService;
    
    private ClusterPersistRepository clusterRepository;
    
    private ComputeNodeInstance currentInstance;
    
    private Collection<ComputeNodeInstance> computeNodeInstances;
    
    public ShardingSphereDatabaseLockManager() {
        locks = new ConcurrentHashMap<>();
        lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(getLockType());
    }
    
    @Override
    public void initLocksState(final PersistRepository repository, final ComputeNodeInstance currentInstance, final Collection<ComputeNodeInstance> computeNodeInstances) {
        clusterRepository = (ClusterPersistRepository) repository;
        this.currentInstance = currentInstance;
        this.computeNodeInstances = computeNodeInstances;
        ShardingSphereEventBus.getInstance().register(this);
        synchronizeGlobalLock();
    }
    
    private void synchronizeGlobalLock() {
        Collection<String> allGlobalLock = clusterRepository.getChildrenKeys(lockNodeService.getGlobalLocksNodePath());
        if (allGlobalLock.isEmpty()) {
            clusterRepository.persist(lockNodeService.getGlobalLocksNodePath(), "");
            clusterRepository.persist(lockNodeService.getGlobalLockedAckNodePath(), "");
            return;
        }
        for (String each : allGlobalLock) {
            Optional<String> databaseLock = lockNodeService.parseGlobalLocksNodePath(each);
            databaseLock.ifPresent(database -> locks.put(database, crateDatabaseLock()));
        }
    }
    
    private ShardingSphereDatabaseLock crateDatabaseLock() {
        return new ShardingSphereDatabaseLock(clusterRepository, currentInstance, computeNodeInstances);
    }
    
    @Override
    public ShardingSphereLock getOrCreateLock(final String lockName) {
        ShardingSphereDatabaseLock result = locks.get(lockName);
        if (null != result) {
            return result;
        }
        result = crateDatabaseLock();
        locks.put(lockName, result);
        return result;
    }
    
    @Override
    public ShardingSphereLock getLock(final String lockName) {
        return locks.get(lockName);
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        if (locks.isEmpty()) {
            return false;
        }
        ShardingSphereGlobalLock lock = locks.get(lockName);
        if (null != lock) {
            return lock.isLocked(lockName);
        }
        return false;
    }
    
    /**
     * Locked.
     *
     * @param event database locked event
     */
    @Subscribe
    public synchronized void locked(final DatabaseLockedEvent event) {
        String database = event.getDatabase();
        ShardingSphereDatabaseLock lock = locks.get(database);
        if (null == lock) {
            lock = crateDatabaseLock();
            locks.put(database, lock);
        }
        lock.ackLock(database, getCurrentInstanceId());
    }
    
    /**
     * Lock released.
     *
     * @param event database lock released event
     */
    @Subscribe
    public synchronized void lockReleased(final DatabaseLockReleasedEvent event) {
        String database = event.getDatabase();
        getOptionalLock(database).ifPresent(lock -> lock.releaseAckLock(database, getCurrentInstanceId()));
    }
    
    /**
     * Ack locked.
     *
     * @param event database ack locked event
     */
    @Subscribe
    public synchronized void ackLocked(final DatabaseAckLockedEvent event) {
        getOptionalLock(event.getDatabase()).ifPresent(lock -> lock.addLockedInstance(event.getLockedInstance()));
    }
    
    /**
     * Ack lock released.
     *
     * @param event database ack lock released event
     */
    @Subscribe
    public synchronized void ackLockReleased(final DatabaseAckLockReleasedEvent event) {
        getOptionalLock(event.getDatabase()).ifPresent(lock -> lock.removeLockedInstance(event.getLockedInstance()));
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
    }
    
    private Optional<ShardingSphereDatabaseLock> getOptionalLock(final String databaseName) {
        return Optional.ofNullable(locks.get(databaseName));
    }
    
    @Override
    public LockType getLockType() {
        return LockType.DATABASE;
    }
}
