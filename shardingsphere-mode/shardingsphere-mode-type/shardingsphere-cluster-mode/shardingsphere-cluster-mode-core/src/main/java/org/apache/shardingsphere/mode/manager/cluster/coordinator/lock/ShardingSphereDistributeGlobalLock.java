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

import lombok.RequiredArgsConstructor;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Global distribute lock of ShardingSphere.
 */
@Slf4j
@RequiredArgsConstructor
public final class ShardingSphereDistributeGlobalLock implements ShardingSphereGlobalLock {
    
    private static final int CHECK_ACK_INTERVAL_MILLISECONDS = 1000;
    
    private static final long MAX_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private static final long MIN_TRY_LOCK_TIMEOUT_MILLISECONDS = 200;
    
    private static final long DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS = 50;
    
    private final LockRegistryService lockService;
    
    private final ComputeNodeInstance currentInstance;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances;
    
    private final AtomicBoolean isOwner = new AtomicBoolean(false);
    
    private final AtomicReference<LockState> synchronizedLockState = new AtomicReference<>(LockState.UNLOCKED);
    
    private final Set<String> lockedInstances = new CopyOnWriteArraySet<>();
    
    @Override
    public boolean tryLock(final String lockName) {
        return innerTryLock(lockName, MAX_TRY_LOCK_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerTryLock(lockName, timeoutMillis);
    }
    
    private synchronized boolean innerTryLock(final String lockName, final long timeout) {
        long actualTimeout = Math.max(timeout, MIN_TRY_LOCK_TIMEOUT_MILLISECONDS);
        boolean isAcquired = acquireToken();
        if (!isAcquired) {
            return false;
        }
        try {
            if (synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKING) && isOwner.compareAndSet(false, true)) {
                return acquire(lockName, actualTimeout) ? synchronizedLockState.compareAndSet(LockState.LOCKING, LockState.LOCKED)
                        : isOwner.compareAndSet(true, false) && synchronizedLockState.compareAndSet(LockState.LOCKING, LockState.UNLOCKED);
            }
            log.debug("innerTryLock locking, lockName={}", lockName);
            return false;
        } finally {
            releaseToken();
        }
    }
    
    private boolean acquireToken() {
        return lockService.tryGlobalLock(LockNode.generateLockTokenNodePath(), DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS);
    }
    
    private void releaseToken() {
        lockService.releaseGlobalLock(LockNode.generateLockTokenNodePath(), true);
    }
    
    private boolean acquire(final String lockName, final long timeout) {
        long consumeTime = 0;
        String currentInstanceId = getCurrentInstanceId();
        do {
            boolean isLocked = lockService.tryGlobalLock(LockNode.generateGlobalSchemaLocksName(lockName), DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS);
            consumeTime += DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS;
            if (isLocked) {
                lockedInstances.add(currentInstanceId);
                if (isAckOK(timeout - consumeTime)) {
                    return true;
                } else {
                    lockedInstances.remove(currentInstanceId);
                    return false;
                }
            }
        } while (timeout > consumeTime);
        log.debug("inner try lock acquire timeout, lockName={}", lockName);
        return false;
    }
    
    private String getCurrentInstanceId() {
        return currentInstance.getInstanceDefinition().getInstanceId().getId();
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
        log.debug("is lock ack OK timeout");
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
        if (LockState.LOCKED == synchronizedLockState.get()) {
            log.debug("release lock, lockName={}", lockName);
            String currentInstanceId = getCurrentInstanceId();
            if (isOwner.get()) {
                lockService.releaseGlobalLock(LockNode.generateGlobalSchemaLocksName(lockName), true);
                isOwner.compareAndSet(true, false);
                lockedInstances.remove(currentInstanceId);
                synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
                return;
            }
            lockService.releaseGlobalLock(LockNode.generateGlobalSchemaLockReleasedNodePath(lockName), false);
            releaseAckLock(lockName, currentInstanceId);
        }
        log.debug("release lock, state is not locked, ignore, lockName={}", lockName);
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
    public void ackLock(final String lockName, final String lockedInstanceId) {
        if (!isOwner.get() && LockState.UNLOCKED == synchronizedLockState.get()) {
            lockService.ackLock(LockNode.generateGlobalSchemaAckLockName(lockName, lockedInstanceId), lockedInstanceId);
            lockedInstances.add(lockedInstanceId);
            synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKED);
        }
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        if (!isOwner.get()) {
            lockService.releaseAckLock(LockNode.generateGlobalSchemaAckLockName(lockName, lockedInstanceId));
        } else {
            isOwner.compareAndSet(true, false);
        }
        lockedInstances.remove(getCurrentInstanceId());
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
}
