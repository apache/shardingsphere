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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockState;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Inter mutex lock.
 */
@Slf4j
@RequiredArgsConstructor
public final class InterMutexLock implements MutexLock, LockAckAble {
    
    private final String lockName;
    
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
        return innerTryLock(lockName, Math.max(timeoutMillis, TimeoutMilliseconds.MIN_TRY_LOCK));
    }
    
    private boolean innerTryLock(final String lockName, final long timeout) {
        if (!synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKING)) {
            log.debug("Inner try Lock failed, lockName: {} is locking", lockName);
            return false;
        }
        if (!isOwner.compareAndSet(false, true)) {
            log.debug("Inner try Lock set owner failed, lockName: {}", lockName);
            return false;
        }
        if (acquire(lockName, timeout)) {
            if (synchronizedLockState.compareAndSet(LockState.LOCKING, LockState.LOCKED)) {
                log.debug("Inner try Lock locked success, lockName: {}", lockName);
                return true;
            }
        }
        reSetLock();
        log.debug("Inner try Lock locked failed, lockName: {}", lockName);
        return false;
    }
    
    private boolean acquire(final String lockName, final long timeout) {
        long consumeTime = 0;
        String currentInstanceId = getCurrentInstanceId();
        do {
            boolean isLocked = lockService.tryLock(lockName, TimeoutMilliseconds.DEFAULT_REGISTRY);
            consumeTime += TimeoutMilliseconds.DEFAULT_REGISTRY;
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
    
    private void reSetLock() {
        isOwner.set(false);
        synchronizedLockState.set(LockState.UNLOCKED);
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
            count += TimeoutMilliseconds.CHECK_ACK_INTERVAL;
        } while (timeout > count);
        log.debug("is lock ack OK timeout");
        return false;
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
    
    private void sleepInterval() {
        try {
            TimeUnit.MILLISECONDS.sleep(TimeoutMilliseconds.CHECK_ACK_INTERVAL);
        } catch (final InterruptedException ignore) {
        }
    }
    
    @Override
    public void unlock() {
        if (LockState.LOCKED == synchronizedLockState.get()) {
            log.debug("release lock, lockName={}", lockName);
            String currentInstanceId = getCurrentInstanceId();
            if (isOwner.get()) {
                lockService.releaseLock(lockName);
                lockedInstances.remove(currentInstanceId);
                reSetLock();
                return;
            }
            lockService.removeLock(lockName);
            releaseAckLock(lockName, currentInstanceId);
        }
        log.debug("release lock, state is not locked, ignore, lockName={}", lockName);
    }
    
    @Override
    public boolean isLocked() {
        return LockState.LOCKED == synchronizedLockState.get();
    }
    
    @Override
    public void ackLock(final String ackLockName, final String lockedInstanceId) {
        if (!isOwner.get() && LockState.UNLOCKED == synchronizedLockState.get()) {
            lockService.ackLock(ackLockName, lockedInstanceId);
            lockedInstances.add(lockedInstanceId);
            synchronizedLockState.compareAndSet(LockState.UNLOCKED, LockState.LOCKED);
        }
    }
    
    @Override
    public void releaseAckLock(final String ackLockName, final String lockedInstanceId) {
        if (!isOwner.get()) {
            lockService.releaseAckLock(ackLockName);
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
