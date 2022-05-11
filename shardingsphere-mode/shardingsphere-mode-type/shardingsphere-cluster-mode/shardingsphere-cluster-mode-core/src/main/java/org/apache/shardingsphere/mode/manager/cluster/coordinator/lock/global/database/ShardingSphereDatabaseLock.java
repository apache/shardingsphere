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

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.ShardingSphereDistributeGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * Database lock of ShardingSphere.
 */
public final class ShardingSphereDatabaseLock implements ShardingSphereGlobalLock {
    
    private final LockNodeService lockNodeService;
    
    private final ShardingSphereDistributeGlobalLock innerDistributeGlobalLock;
    
    public ShardingSphereDatabaseLock(final ClusterPersistRepository clusterRepository, final LockNodeService lockNodeService, final ComputeNodeInstance currentInstance,
                                      final Collection<ComputeNodeInstance> computeNodeInstances) {
        this.lockNodeService = lockNodeService;
        innerDistributeGlobalLock = new ShardingSphereDistributeGlobalLock(new GlobalLockRegistryService(clusterRepository), currentInstance, computeNodeInstances);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerDistributeGlobalLock.tryLock(lockNodeService.generateLocksName(lockName), timeoutMillis);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        innerDistributeGlobalLock.releaseLock(lockNodeService.generateLocksName(lockName));
    }
    
    @Override
    public boolean isLocked() {
        return innerDistributeGlobalLock.isLocked();
    }
    
    @Override
    public void ackLock(final String lockName, final String lockedInstanceId) {
        innerDistributeGlobalLock.ackLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        innerDistributeGlobalLock.releaseAckLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId);
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        innerDistributeGlobalLock.addLockedInstance(lockedInstanceId);
    }
    
    @Override
    public void removeLockedInstance(final String lockedInstanceId) {
        innerDistributeGlobalLock.removeLockedInstance(lockedInstanceId);
    }
}
