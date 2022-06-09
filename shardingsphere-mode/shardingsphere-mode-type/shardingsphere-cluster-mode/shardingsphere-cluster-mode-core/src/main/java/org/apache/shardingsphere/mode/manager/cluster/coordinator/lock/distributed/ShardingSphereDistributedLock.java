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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed;

import com.google.common.eventbus.Subscribe;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.InterMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.event.DistributedLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

import java.util.Optional;

/**
 * Distribute mutex lock of ShardingSphere.
 */
@Slf4j
public final class ShardingSphereDistributedLock implements ShardingSphereLock {
    
    private final LockNodeService lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.DISTRIBUTED);
    
    private final ShardingSphereInterMutexLockHolder lockHolder;
    
    public ShardingSphereDistributedLock(final ShardingSphereInterMutexLockHolder lockHolder) {
        this.lockHolder = lockHolder;
        ShardingSphereEventBus.getInstance().register(this);
        syncDistributedLockStatus();
    }
    
    private void syncDistributedLockStatus() {
        lockHolder.synchronizeLock(lockNodeService);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerTryLock(lockName, timeoutMillis);
    }
    
    private boolean innerTryLock(final String lockName, final long timeoutMillis) {
        return lockHolder.getOrCreateInterMutexLock(lockNodeService.generateLocksName(lockName)).tryLock(timeoutMillis);
    }
    
    private Optional<InterMutexLock> getInterMutexLock(final String lockName) {
        return lockHolder.getInterMutexLock(lockNodeService.generateLocksName(lockName));
    }
    
    @Override
    public void releaseLock(final String lockName) {
        getInterMutexLock(lockName).ifPresent(InterMutexLock::unlock);
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return getInterMutexLock(lockName).map(InterMutexLock::isLocked).orElse(false);
    }
    
    /**
     * Distributed locked.
     *
     * @param event distributed locked event
     */
    @Subscribe
    public synchronized void locked(final DistributedLockedEvent event) {
        String lockName = event.getLockedName();
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        InterMutexLock interMutexLock = lockHolder.getOrCreateInterMutexLock(lockNodeService.generateLocksName(lockName));
        interMutexLock.ackLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId);
    }
    
    /**
     * Distributed lock released.
     *
     * @param event distributed lock released event
     */
    @Subscribe
    public synchronized void lockReleased(final DistributedLockReleasedEvent event) {
        String lockName = event.getLockedName();
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        getInterMutexLock(lockName).ifPresent(mutexLock -> mutexLock.releaseAckLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId));
    }
    
    /**
     * Distributed ack locked.
     *
     * @param event distributed ack locked event
     */
    @Subscribe
    public synchronized void ackLocked(final DistributedAckLockedEvent event) {
        getInterMutexLock(event.getLockName()).ifPresent(mutexLock -> mutexLock.addLockedInstance(event.getLockedInstance()));
    }
    
    /**
     * Distributed ack lock released.
     *
     * @param event distributed ack lock released event
     */
    @Subscribe
    public synchronized void ackLockReleased(final DistributedAckLockReleasedEvent event) {
        getInterMutexLock(event.getLockName()).ifPresent(mutexLock -> mutexLock.removeLockedInstance(event.getLockedInstance()));
    }
}
