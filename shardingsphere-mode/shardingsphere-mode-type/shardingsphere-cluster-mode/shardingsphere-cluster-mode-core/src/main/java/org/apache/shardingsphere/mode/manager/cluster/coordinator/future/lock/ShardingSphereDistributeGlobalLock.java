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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockNode;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;

/**
 * Global distribute lock of ShardingSphere.
 */
public final class ShardingSphereDistributeGlobalLock implements ShardingSphereGlobalLock {
    
    private static final int CHECK_ACK_INTERVAL_MILLISECONDS = 1000;
    
    private static final long DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private static final long DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS = 2 * 100;
    
    private final InstanceContext instanceContext;
    
    private final String ownerInstanceId;
    
    private final GlobalLockRegistryService lockService;
    
    private final Set<String> lockedInstances = new CopyOnWriteArraySet<>();
    
    public ShardingSphereDistributeGlobalLock(final InstanceContext instanceContext, final String ownerInstanceId, final GlobalLockRegistryService lockService) {
        this.instanceContext = instanceContext;
        this.ownerInstanceId = ownerInstanceId;
        this.lockService = lockService;
        initLockedInstances(instanceContext);
    }
    
    private void initLockedInstances(final InstanceContext instanceContext) {
        instanceContext.getComputeNodeInstances().forEach(each -> lockedInstances.add(each.getInstanceDefinition().getInstanceId().getId()));
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        if (isLocked(lockName)) {
            return true;
        }
        return lockService.tryLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId));
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeout) {
        long count = 0;
        do {
            if (tryLock(lockName)) {
                return isAckOK(lockName, DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS - count);
            }
            count += DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS;
        } while (timeout > count);
        return false;
    }
    
    private boolean isAckOK(final String lockName, final long timeout) {
        long count = 0;
        do {
            if (isLocked(lockName)) {
                return true;
            }
            sleepInterval();
            count += CHECK_ACK_INTERVAL_MILLISECONDS;
        } while (timeout > count);
        return false;
    }
    
    private void sleepInterval() {
        try {
            TimeUnit.MILLISECONDS.sleep(CHECK_ACK_INTERVAL_MILLISECONDS);
        } catch (final InterruptedException ignore) {
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        if (!isLocked(lockName)) {
            return;
        }
        lockService.releaseLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId));
        String currentInstanceId = getCurrentInstanceId();
        if (isOwnerInstanceId(currentInstanceId)) {
            lockedInstances.remove(ownerInstanceId);
            return;
        }
        releaseAckLock(lockName, currentInstanceId);
    }
    
    private String getCurrentInstanceId() {
        return instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        String instanceId = getCurrentInstanceId();
        if (!isOwnerInstanceId(instanceId)) {
            return lockedInstances.contains(instanceId);
        }
        if (!lockedInstances.contains(ownerInstanceId)) {
            return false;
        }
        return isAckCompleted();
    }
    
    private boolean isAckCompleted() {
        Collection<ComputeNodeInstance> computeNodeInstances = instanceContext.getComputeNodeInstances();
        if (computeNodeInstances.size() > lockedInstances.size()) {
            return false;
        }
        for (ComputeNodeInstance each : computeNodeInstances) {
            if (!lockedInstances.contains(each.getInstanceDefinition().getInstanceId().getId())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public long getDefaultTimeOut() {
        return DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS;
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        lockedInstances.add(ownerInstanceId);
    }
    
    @Override
    public void ackLock(final String lockName, final String lockedInstanceId) {
        lockService.ackLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId), lockedInstanceId);
        lockedInstances.add(lockedInstanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        lockService.releaseAckLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId));
        lockedInstances.remove(lockedInstanceId);
    }
    
    private boolean isOwnerInstanceId(final String lockedInstanceId) {
        return ownerInstanceId.equals(lockedInstanceId);
    }
}
