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
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.ShardingSphereDistributeGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.ShardingSphereSequencedSemaphoreLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.service.GeneralLockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.service.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

public final class ShardingSphereGeneralLock implements ShardingSphereGlobalLock {
    
    private final LockNodeService lockNodeService;
    
    private final ShardingSphereSequencedSemaphoreLock sequencedSemaphoreLock;
    
    private final ShardingSphereDistributeGlobalLock innerDistributeGlobalLock;
    
    public ShardingSphereGeneralLock(final ClusterPersistRepository clusterRepository, final ComputeNodeInstance currentInstance, final Collection<ComputeNodeInstance> computeNodeInstances) {
        LockRegistryService lockRegistryService = new GlobalLockRegistryService(clusterRepository);
        lockNodeService = new GeneralLockNodeService();
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
        boolean isAcquired = sequencedSemaphoreLock.tryLock(lockName, TimeoutMilliseconds.MIN_TRY_LOCK);
        if (!isAcquired) {
            return false;
        }
        try {
            return innerDistributeGlobalLock.tryLock(lockName, timeoutMillis);
        } finally {
            sequencedSemaphoreLock.releaseLock(lockName);
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        innerDistributeGlobalLock.releaseLock(lockNodeService.generateGlobalLocksName(lockName));
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return innerDistributeGlobalLock.isLocked(lockName);
    }
    
    @Override
    public void ackLock(final String lockName, final String instanceId) {
        innerDistributeGlobalLock.ackLock(lockNodeService.generateGlobalAckLockName(lockName, instanceId), instanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String instanceId) {
        innerDistributeGlobalLock.releaseAckLock(lockNodeService.generateGlobalAckLockName(lockName, instanceId), instanceId);
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
