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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockState;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global distribute lock of ShardingSphere.
 */
@Slf4j
public final class ShardingSphereDistributeGlobalLock implements ShardingSphereGlobalLock {
    
    private static final int CHECK_ACK_INTERVAL_MILLISECONDS = 1000;
    
    private static final long DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private static final long DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS = 2 * 100;
    
    private final ComputeNodeInstance currentInstance;
    
    private final AtomicReference<String> ownerInstanceId;
    
    private final AtomicReference<LockState> synchronizedLockState;
    
    private final LockRegistryService lockService;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances;
    
    private final Set<String> lockedInstances = new CopyOnWriteArraySet<>();
    
    public ShardingSphereDistributeGlobalLock(final LockRegistryService lockService, final ComputeNodeInstance currentInstance, final Collection<ComputeNodeInstance> computeNodeInstances,
                                              final String ownerInstanceId) {
        this.lockService = lockService;
        this.currentInstance = currentInstance;
        this.computeNodeInstances = computeNodeInstances;
        if (ownerInstanceId.equals(getCurrentInstanceId())) {
            synchronizedLockState = new AtomicReference<>(LockState.UNLOCKED);
            this.ownerInstanceId = new AtomicReference<>();
        } else {
            synchronizedLockState = new AtomicReference<>(LockState.LOCKED);
            this.ownerInstanceId = new AtomicReference<>(ownerInstanceId);
        }
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
    }
    
    private boolean isOwnerInstanceId(final String lockedInstanceId) {
        return ownerInstanceId.get().equals(lockedInstanceId);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return innerTryLock(lockName, DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeout) {
        return innerTryLock(lockName, timeout);
    }
    
    private synchronized boolean innerTryLock(final String lockName, final long timeout) {
        if (LockState.LOCKED == synchronizedLockState.get()) {
            log.debug("innerTryLock, already locked, lockName={}", lockName);
            return false;
        }
        long count = 0;
        do {
            String currentInstanceId = getCurrentInstanceId();
            boolean isLocked = lockService.tryGlobalLock(LockNode.generateGlobalSchemaLocksName(lockName, currentInstanceId), DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS);
            count += DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS;
            if (isLocked) {
                lockedInstances.add(currentInstanceId);
                if (isAckOK(timeout - count)) {
                    boolean result = synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKED);
                    log.debug("innerTryLock, result={}, lockName={}, lockState={}, globalLock.hashCode={}", result, lockName, synchronizedLockState.get(), hashCode());
                    ownerInstanceId.set(currentInstanceId);
                    return result;
                } else {
                    lockedInstances.remove(currentInstanceId);
                    return false;
                }
            }
        } while (timeout > count);
        log.debug("innerTryLock timeout, lockName={}", lockName);
        return false;
    }
    
    private boolean isAckOK(final long timeout) {
        long count = 0;
        do {
            if (isAckCompleted()) {
                return true;
            }
            sleepInterval();
            count += CHECK_ACK_INTERVAL_MILLISECONDS;
        } while (timeout > count);
        log.debug("isAckOK timeout");
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
        log.debug("releaseLock, lockName={}", lockName);
        if (LockState.LOCKED != synchronizedLockState.get()) {
            log.debug("releaseLock, state is not locked, ignore, lockName={}", lockName);
            return;
        }
        String currentInstanceId = getCurrentInstanceId();
        if (isOwnerInstanceId(currentInstanceId)) {
            lockService.releaseGlobalLock(LockNode.generateGlobalSchemaLocksName(lockName, this.ownerInstanceId.get()), true);
            lockedInstances.remove(this.ownerInstanceId.get());
            this.ownerInstanceId.set("");
            synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
            return;
        }
        lockService.releaseGlobalLock(LockNode.generateGlobalSchemaLockReleasedNodePath(lockName, this.ownerInstanceId.get()), false);
        ownerInstanceId.set("");
        releaseAckLock(lockName, currentInstanceId);
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return LockState.LOCKED == synchronizedLockState.get();
    }
    
    private boolean isAckCompleted() {
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
    public void ackLock(final String lockName, final String lockedInstanceId) {
        lockService.ackLock(LockNode.generateGlobalSchemaAckLockName(lockName, lockedInstanceId), lockedInstanceId);
        lockedInstances.add(lockedInstanceId);
        synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKED);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        lockService.releaseAckLock(LockNode.generateGlobalSchemaAckLockName(lockName, lockedInstanceId));
        lockedInstances.remove(lockedInstanceId);
        synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        lockedInstances.add(lockedInstanceId);
    }
    
    @Override
    public void removeLockedInstance(final String lockedInstanceId) {
        lockedInstances.remove(lockedInstanceId);
    }
    
    @Override
    public void releaseLockedState(final String lockName) {
        if (isLocked(lockName)) {
            synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
        }
    }
    
    @Override
    public void refreshOwner(final String ownerInstanceId) {
        this.ownerInstanceId.set(ownerInstanceId);
    }
}
