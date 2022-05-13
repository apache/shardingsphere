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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.InterMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.InterReentrantMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.MutexLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.ReentrantMutexLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * General lock of ShardingSphere.
 */
public final class ShardingSphereGeneralLock implements ShardingSphereGlobalLock {
    
    private final LockNodeService lockNodeService;
    
    private final InterReentrantMutexLock sequencedLock;
    
    private final InterMutexLock innerLock;
    
    public ShardingSphereGeneralLock(final ClusterPersistRepository clusterRepository, final LockNodeService lockNodeService, final ComputeNodeInstance currentInstance,
                                     final Collection<ComputeNodeInstance> computeNodeInstances) {
        this.lockNodeService = lockNodeService;
        sequencedLock = new InterReentrantMutexLock(new ReentrantMutexLockRegistryService(clusterRepository));
        innerLock = new InterMutexLock(new MutexLockRegistryService(clusterRepository), currentInstance, computeNodeInstances);
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
        if (!sequencedLock.tryLock(lockNodeService.getSequenceNodePath(), TimeoutMilliseconds.DEFAULT_REGISTRY)) {
            return false;
        }
        try {
            return innerLock.tryLock(lockNodeService.generateLocksName(lockName), timeoutMillis);
        } finally {
            sequencedLock.releaseLock(lockNodeService.getSequenceNodePath());
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        innerLock.releaseLock(lockNodeService.generateLocksName(lockName));
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return innerLock.isLocked(lockName);
    }
    
    @Override
    public void ackLock(final String lockName, final String instanceId) {
        innerLock.ackLock(lockNodeService.generateAckLockName(lockName, instanceId), instanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String instanceId) {
        innerLock.releaseAckLock(lockNodeService.generateAckLockName(lockName, instanceId), instanceId);
    }
    
    @Override
    public void addLockedInstance(final String instanceId) {
        innerLock.addLockedInstance(instanceId);
    }
    
    @Override
    public void removeLockedInstance(final String instanceId) {
        innerLock.removeLockedInstance(instanceId);
    }
}
