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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.lock.LockState;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Exclusive lock.
 */
@Slf4j
@RequiredArgsConstructor
public final class ExclusiveInternalLock implements InternalLock, LockAckAble {
    
    private final String lockName;
    
    private final InternalLock sequencedInternalLock;
    
    private final LockRegistryService lockService;
    
    private final ComputeNodeInstance currentInstance;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances;
    
    private final AtomicBoolean isOwner = new AtomicBoolean(false);
    
    private final AtomicReference<LockState> synchronizedLockState = new AtomicReference<>(LockState.UNLOCKED);
    
    private final Set<String> lockedInstances = new CopyOnWriteArraySet<>();
    
    @Override
    public boolean tryLock() {
        return tryLock(TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        if (!sequencedInternalLock.tryLock(TimeoutMilliseconds.DEFAULT_REGISTRY)) {
            log.debug("Exclusive lock acquire sequenced failed, lock name: {}", lockName);
            return false;
        }
        try {
            long timeoutMilliseconds = Math.max(timeoutMillis, TimeoutMilliseconds.MIN_TRY_LOCK);
            log.debug("Exclusive lock acquire sequenced success, lock name: {}, timeout milliseconds: {}ms", lockName, timeoutMilliseconds);
            return innerTryLock(lockName, timeoutMilliseconds);
        } finally {
            sequencedInternalLock.unlock();
            log.debug("Exclusive lock release sequenced success, database name: {}", lockName);
        }
    }
    
    private boolean innerTryLock(final String lockName, final long timeout) {
        if (!synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKING)) {
            log.debug("Exclusive lock try Lock set lock state failed, lock name: {}, lock state: {}", lockName, synchronizedLockState.get().name());
            return false;
        }
        if (!isOwner.compareAndSet(false, true)) {
            log.debug("Exclusive lock try Lock set lock owner failed, lock name: {}, lock is owner: {}", lockName, isOwner.get());
            return false;
        }
        if (acquire(lockName, timeout)) {
            if (synchronizedLockState.compareAndSet(LockState.LOCKING, LockState.LOCKED)) {
                log.debug("Exclusive lock try Lock acquire lock success, lock name: {}", lockName);
                return true;
            }
        }
        reSetLockState();
        log.debug("Inter mutex lock try Lock acquire lock failed, lock name: {}", lockName);
        return false;
    }
    
    private boolean acquire(final String lockName, final long timeout) {
        long acquireStart = System.currentTimeMillis();
        boolean isLocked = lockService.tryLock(lockName, timeout);
        if (isLocked) {
            lockedInstances.add(currentInstance.getCurrentInstanceId());
            long acquireEnd = System.currentTimeMillis();
            long acquireExpend = acquireEnd - acquireStart;
            log.debug("inter mutex lock acquire lock success then await for ack, lock name: {}, expend time millis {}ms", lockName, acquireExpend);
            if (isAckOK(timeout - acquireExpend)) {
                long ackExpend = System.currentTimeMillis() - acquireEnd;
                log.debug("inter mutex lock acquire lock success and ack success, lock name: {}, expend time millis {}ms", lockName, ackExpend);
                return true;
            } else {
                lockService.releaseLock(lockName);
                return false;
            }
        }
        log.debug("inter mutex lock acquire lock timeout. lock name: {}, timeout millis {}ms", lockName, timeout);
        return false;
    }
    
    private boolean isAckOK(final long timeout) {
        long expend = 0;
        do {
            if (isAckCompleted(expend)) {
                return true;
            }
            TimeoutMilliseconds.sleepInterval(TimeoutMilliseconds.DEFAULT_REGISTRY);
            expend += TimeoutMilliseconds.DEFAULT_REGISTRY;
        } while (timeout > expend);
        log.debug("inter mutex ack lock timeout, timeout millis {}ms", timeout);
        return false;
    }
    
    private boolean isAckCompleted(final long expend) {
        if (expend > TimeoutMilliseconds.MAX_ACK_EXPEND) {
            lockedInstances.addAll(lockService.acquireAckLockedInstances(LockNodeUtil.generateAckPathName(lockName)));
        }
        if (computeNodeInstances.size() > lockedInstances.size()) {
            return false;
        }
        for (ComputeNodeInstance each : computeNodeInstances) {
            if (!lockedInstances.contains(each.getMetaData().getId())) {
                return false;
            }
        }
        return true;
    }
    
    @Override
    public void unlock() {
        LockState lockState = synchronizedLockState.get();
        if (LockState.LOCKED == lockState) {
            log.debug("inter mutex lock unlock. lock name: {}", lockName);
            if (isOwner.get()) {
                lockService.releaseLock(lockName);
                log.debug("inter mutex lock owner lock release lock success. lock name: {}", lockName);
            } else {
                lockService.removeLock(lockName);
                log.debug("inter mutex lock not owner remove lock success. lock name: {}", lockName);
            }
            reSetLockState();
            return;
        }
        log.debug("inter mutex lock ignore unlock, lock name: {} lock state: {}", lockName, lockState);
    }
    
    @Override
    public boolean isLocked() {
        return LockState.LOCKED == synchronizedLockState.get();
    }
    
    @Override
    public void ackLock(final String ackLockName, final String lockedInstanceId) {
        LockState lockState = synchronizedLockState.get();
        boolean owner = isOwner.get();
        if (!owner && LockState.UNLOCKED == lockState) {
            lockService.ackLock(ackLockName, lockedInstanceId);
            lockedInstances.add(lockedInstanceId);
            synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKED);
            log.debug("inter mutex lock ack lock success, ack lock name: {}", ackLockName);
        }
        log.debug("inter mutex lock ignore ack lock, ack lock name: {}, lock state: {}, lock owner: {}", ackLockName, lockState, owner);
    }
    
    @Override
    public void releaseAckLock(final String ackLockName, final String lockedInstanceId) {
        boolean owner = isOwner.get();
        if (!owner) {
            lockService.releaseAckLock(ackLockName);
            log.debug("inter mutex lock not owner release ack lock success, ack lock name: {}, locked instanceId: {}", ackLockName, lockedInstanceId);
        }
        reSetLockState();
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        lockedInstances.add(lockedInstanceId);
        log.debug("inter mutex lock add locked instance id, id: {}", lockedInstanceId);
    }
    
    @Override
    public void removeLockedInstance(final String lockedInstanceId) {
        lockedInstances.remove(lockedInstanceId);
        log.debug("inter mutex lock remove locked instance id, id: {}", lockedInstanceId);
    }
    
    @Override
    public void reSetLockState() {
        lockedInstances.clear();
        isOwner.set(false);
        synchronizedLockState.set(LockState.UNLOCKED);
    }
}
