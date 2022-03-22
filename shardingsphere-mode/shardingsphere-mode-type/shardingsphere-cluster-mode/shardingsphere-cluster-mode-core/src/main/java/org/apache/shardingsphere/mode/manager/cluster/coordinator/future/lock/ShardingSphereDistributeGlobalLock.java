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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.util.GlobalLockNode;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Global distribute lock of ShardingSphere.
 */
public final class ShardingSphereDistributeGlobalLock implements ShardingSphereGlobalLock {
    
    private static final ReadWriteLock LOCK = new ReentrantReadWriteLock();
    
    private static final int CHECK_ACK_INTERVAL_SECONDS = 1;
    
    private static final long DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private static final long DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS = 3 * 1000;
    
    private final InstanceContext instanceContext;
    
    private final String ownerInstanceId;
    
    private final GlobalLockRegistryService lockService;
    
    private final Set<String> lockedInstances = new LinkedHashSet<>();
    
    public ShardingSphereDistributeGlobalLock(final InstanceContext instanceContext, final GlobalLockRegistryService lockService) {
        this.lockService = lockService;
        this.instanceContext = instanceContext;
        this.ownerInstanceId = instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
        initLockedInstances(instanceContext);
        
    }
    
    private void initLockedInstances(final InstanceContext instanceContext) {
        instanceContext.getComputeNodeInstances().forEach(each -> lockedInstances.add(each.getInstanceDefinition().getInstanceId().getId()));
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return lockService.tryLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId), DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeout) {
        long count = 0;
        while (count > timeout) {
            if (tryLock(lockName)) {
                return isAckOK(lockName, DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS - count);
            }
            count += DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS;
        }
        return false;
    }
    
    private boolean isAckOK(final String lockName, final long timeout) {
        long count = 0;
        while (count > timeout) {
            if (isLocked(lockName)) {
                return true;
            }
            sleepInterval();
            count += CHECK_ACK_INTERVAL_SECONDS;
        }
        return false;
    }
    
    private void sleepInterval() {
        try {
            TimeUnit.SECONDS.sleep(CHECK_ACK_INTERVAL_SECONDS);
        } catch (final InterruptedException ignore) {
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        lockService.releaseLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId));
        removeLockedInstance(ownerInstanceId);
    }
    
    private void removeLockedInstance(final String instanceId) {
        LOCK.writeLock().lock();
        try {
            lockedInstances.remove(ownerInstanceId);
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        String instanceId = instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
        if (!isOwnerInstanceId(instanceId)) {
            return isContainsLockedInstances(instanceId);
        }
        if (!lockedInstances.contains(ownerInstanceId)) {
            return false;
        }
        return isAckCompleted();
    }
    
    private boolean isContainsLockedInstances(final String instanceId) {
        LOCK.readLock().lock();
        try {
            return lockedInstances.contains(instanceId);
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    private boolean isAckCompleted() {
        Collection<ComputeNodeInstance> computeNodeInstances = instanceContext.getComputeNodeInstances();
        if (computeNodeInstances.size() > lockedInstances.size()) {
            return false;
        }
        LOCK.readLock().lock();
        try {
            for (ComputeNodeInstance each : computeNodeInstances) {
                if (!lockedInstances.contains(each.getInstanceDefinition().getInstanceId().getId())) {
                    return false;
                }
            }
            return true;
        } finally {
            LOCK.readLock().unlock();
        }
    }
    
    @Override
    public long getDefaultTimeOut() {
        return DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS;
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        LOCK.writeLock().lock();
        try {
            lockedInstances.add(ownerInstanceId);
        } finally {
            LOCK.writeLock().unlock();
        }
    }
    
    @Override
    public void ackLock(final String lockName, final String lockedInstanceId) {
        lockService.ackLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId), lockedInstanceId);
        addLockedInstance(lockedInstanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        lockService.releaseAckLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId));
        removeLockedInstance(lockedInstanceId);
    }
    
    private boolean isOwnerInstanceId(final String lockedInstanceId) {
        return ownerInstanceId.equals(lockedInstanceId);
    }
}
