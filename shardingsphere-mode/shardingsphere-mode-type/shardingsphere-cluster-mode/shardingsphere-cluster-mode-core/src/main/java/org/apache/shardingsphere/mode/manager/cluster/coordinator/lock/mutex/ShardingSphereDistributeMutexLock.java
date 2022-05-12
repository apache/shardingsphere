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

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.event.MutexLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

public final class ShardingSphereDistributeMutexLock implements ShardingSphereLock {
    
    private final LockNodeService lockNodeService;
    
    private final MutexLock sequencedLock;
    
    private final ShardingSphereMutexLockHolder lockHolder;
    
    public ShardingSphereDistributeMutexLock(final LockNodeService lockNodeService, final ShardingSphereMutexLockHolder lockHolder) {
        this.lockNodeService = lockNodeService;
        this.lockHolder = lockHolder;
        this.sequencedLock = lockHolder.getInterReentrantMutexLock(lockNodeService.getSequenceNodePath());
        ShardingSphereEventBus.getInstance().register(this);
        lockHolder.synchronizeMutexLock(lockNodeService);
    }
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerTryLock(lockName, timeoutMillis);
    }
    
    private synchronized boolean innerTryLock(final String lockName, final long timeoutMillis) {
        if (!sequencedLock.tryLock(TimeoutMilliseconds.DEFAULT_REGISTRY)) {
            return false;
        }
        try {
            return getInterMutexLock(lockName).tryLock(timeoutMillis);
        } finally {
            sequencedLock.unlock();
        }
    }
    
    private InterMutexLock getInterMutexLock(final String locksName) {
        return (InterMutexLock) lockHolder.getInterMutexLock(lockNodeService.generateLocksName(locksName));
    }
    
    @Override
    public void releaseLock(final String lockName) {
        getInterMutexLock(lockName).unlock();
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return getInterMutexLock(lockName).isLocked();
    }
    
    /**
     * Mutex locked.
     *
     * @param event mutex locked event
     */
    @Subscribe
    public synchronized void locked(final MutexLockedEvent event) {
        String lockName = event.getLockedName();
        InterMutexLock interMutexLock = getInterMutexLock(lockName);
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        interMutexLock.ackLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId);
    }
    
    /**
     * Mutex lock released.
     *
     * @param event mutex lock released event
     */
    @Subscribe
    public synchronized void lockReleased(final MutexLockReleasedEvent event) {
        String lockName = event.getLockedName();
        InterMutexLock interMutexLock = getInterMutexLock(lockName);
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        interMutexLock.releaseAckLock(lockNodeService.generateAckLockName(lockName, lockedInstanceId), lockedInstanceId);
    }
    
    /**
     * Mutex ack locked.
     *
     * @param event mutex ack locked event
     */
    @Subscribe
    public synchronized void ackLocked(final MutexAckLockedEvent event) {
        String lockName = event.getLockName();
        InterMutexLock interMutexLock = getInterMutexLock(lockName);
        interMutexLock.addLockedInstance(lockHolder.getCurrentInstanceId());
    }
    
    /**
     * Mutex ack lock released.
     *
     * @param event mutex ack lock released event
     */
    @Subscribe
    public synchronized void ackLockReleased(final MutexAckLockReleasedEvent event) {
        String lockName = event.getLockName();
        InterMutexLock interMutexLock = getInterMutexLock(lockName);
        interMutexLock.removeLockedInstance(lockHolder.getCurrentInstanceId());
    }
}
