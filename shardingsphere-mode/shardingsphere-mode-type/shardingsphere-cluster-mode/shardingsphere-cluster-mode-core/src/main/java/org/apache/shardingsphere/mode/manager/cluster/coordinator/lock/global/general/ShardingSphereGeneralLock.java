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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.general;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.ShardingSphereDistributeGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.ShardingSphereSequencedSemaphoreLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * General lock of ShardingSphere.
 */
public final class ShardingSphereGeneralLock implements ShardingSphereGlobalLock {
    
    private final LockNodeService lockNodeService;
    
    private final ShardingSphereLock sequencedSemaphoreLock;
    
    private final ShardingSphereDistributeGlobalLock innerDistributeGlobalLock;
    
    public ShardingSphereGeneralLock(final ClusterPersistRepository clusterRepository, final LockNodeService lockNodeService, final ComputeNodeInstance currentInstance,
                                     final Collection<ComputeNodeInstance> computeNodeInstances) {
        LockRegistryService lockRegistryService = new GlobalLockRegistryService(clusterRepository);
        this.lockNodeService = lockNodeService;
        innerDistributeGlobalLock = new ShardingSphereDistributeGlobalLock(lockRegistryService, currentInstance, computeNodeInstances);
        sequencedSemaphoreLock = new ShardingSphereSequencedSemaphoreLock(lockRegistryService, lockNodeService);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerTryLock(lockName, Math.max(timeoutMillis, TimeoutMilliseconds.MIN_TRY_LOCK));
    }
    
    private synchronized boolean innerTryLock(final String lockName, final long timeoutMillis) {
        if (!sequencedSemaphoreLock.tryLock(lockName, TimeoutMilliseconds.MIN_TRY_LOCK)) {
            return false;
        }
        try {
            return innerDistributeGlobalLock.tryLock(lockNodeService.generateLocksName(lockName), timeoutMillis);
        } finally {
            sequencedSemaphoreLock.releaseLock(lockName);
        }
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
    public void ackLock(final String lockName, final String instanceId) {
        innerDistributeGlobalLock.ackLock(lockNodeService.generateAckLockName(lockName, instanceId), instanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String instanceId) {
        innerDistributeGlobalLock.releaseAckLock(lockNodeService.generateAckLockName(lockName, instanceId), instanceId);
    }
    
    @Override
    public void addLockedInstance(final String instanceId) {
        innerDistributeGlobalLock.addLockedInstance(instanceId);
    }
    
    @Override
    public void removeLockedInstance(final String instanceId) {
        innerDistributeGlobalLock.removeLockedInstance(instanceId);
    }
}
