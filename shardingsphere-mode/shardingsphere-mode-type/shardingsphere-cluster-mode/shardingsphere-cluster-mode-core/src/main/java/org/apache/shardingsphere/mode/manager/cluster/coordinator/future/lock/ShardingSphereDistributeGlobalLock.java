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

import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.GlobalLockNode;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.future.lock.service.LockState;

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
    
    // TODO it's less than CHECK_ACK_INTERVAL_MILLISECONDS, is it OK?
    private static final long DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS = 2 * 100;
    
    private final InstanceContext instanceContext;
    
    private final String ownerInstanceId;
    
    private final AtomicReference<LockState> synchronizedLockState;
    
    private final GlobalLockRegistryService lockService;
    
    private final Set<String> lockedInstances = new CopyOnWriteArraySet<>();
    
    public ShardingSphereDistributeGlobalLock(final InstanceContext instanceContext, final String ownerInstanceId, final GlobalLockRegistryService lockService) {
        this.instanceContext = instanceContext;
        this.ownerInstanceId = ownerInstanceId;
        this.lockService = lockService;
        synchronizedLockState = new AtomicReference<>(isOwnerInstanceId(getCurrentInstanceId()) ? LockState.INITIALIZATION : LockState.LOCKED);
        initLockedInstances(instanceContext);
    }
    
    private String getCurrentInstanceId() {
        return instanceContext.getInstance().getInstanceDefinition().getInstanceId().getId();
    }
    
    private boolean isOwnerInstanceId(final String lockedInstanceId) {
        return ownerInstanceId.equals(lockedInstanceId);
    }
    
    private void initLockedInstances(final InstanceContext instanceContext) {
        instanceContext.getComputeNodeInstances().forEach(each -> lockedInstances.add(each.getInstanceDefinition().getInstanceId().getId()));
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return innerTryLock(lockName, DEFAULT_REGISTRY_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeout) {
        return innerTryLock(lockName, timeout);
    }
    
    private boolean innerTryLock(final String lockName, final long timeout) {
        if (LockState.LOCKED == synchronizedLockState.get()) {
            log.info("innerTryLock, already locked, lockName={}", lockName);
            return false;
        }
        long count = 0;
        do {
            if (lockService.tryLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId))) {
                if (isAckOK(timeout - count)) {
                    boolean result = synchronizedLockState.compareAndSet(LockState.INITIALIZATION, LockState.LOCKED);
                    log.info("innerTryLock, result={}, lockName={}, lockState={}, globalLock.hashCode={}", result, lockName, synchronizedLockState.get(), hashCode());
                    return result;
                }
            }
            sleepInterval();
            count += CHECK_ACK_INTERVAL_MILLISECONDS;
        } while (timeout > count);
        // TODO if lock state set to UNLOCKED, the next CAS in tryLock might be failed
        synchronizedLockState.compareAndSet(LockState.INITIALIZATION, LockState.UNLOCKED);
        log.info("innerTryLock timeout, lockName={}", lockName);
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
        log.info("isAckOK timeout");
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
        log.info("releaseLock, lockName={}", lockName);
        if (LockState.LOCKED != synchronizedLockState.get()) {
            log.info("releaseLock, state is not locked, ignore, lockName={}", lockName);
            return;
        }
        lockService.releaseLock(GlobalLockNode.generateSchemaLockName(lockName, ownerInstanceId));
        String currentInstanceId = getCurrentInstanceId();
        if (isOwnerInstanceId(currentInstanceId)) {
            lockedInstances.remove(ownerInstanceId);
            synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
            return;
        }
        releaseAckLock(lockName, currentInstanceId);
        synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return LockState.LOCKED == synchronizedLockState.get();
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
    public void ackLock(final String lockName, final String lockedInstanceId) {
        lockService.ackLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId), lockedInstanceId);
        lockedInstances.add(lockedInstanceId);
    }
    
    @Override
    public void releaseAckLock(final String lockName, final String lockedInstanceId) {
        lockService.releaseAckLock(GlobalLockNode.generateSchemaAckLockName(lockName, lockedInstanceId));
        lockedInstances.remove(lockedInstanceId);
        synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
    }
    
    @Override
    public void addLockedInstance(final String lockedInstanceId) {
        lockedInstances.add(ownerInstanceId);
    }
    
    @Override
    public void releaseLockedState(final String lockName) {
        if (isLocked(lockName)) {
            synchronizedLockState.compareAndSet(LockState.LOCKED, LockState.UNLOCKED);
        }
    }
}
