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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.state.LockStateContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.event.DatabaseAckLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.event.DatabaseAckLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.event.DatabaseLockReleasedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.event.DatabaseLockedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.InterMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

import java.util.Optional;

/**
 * Distribute database lock of ShardingSphere.
 */
public final class ShardingSphereDistributedDatabaseLock implements ShardingSphereLock {
    
    private final LockNodeService lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.DATABASE);
    
    private final ShardingSphereInterMutexLockHolder lockHolder;
    
    private final LockStateContext lockStateContext;
    
    public ShardingSphereDistributedDatabaseLock(final ShardingSphereInterMutexLockHolder lockHolder, final LockStateContext lockStateContext) {
        this.lockHolder = lockHolder;
        this.lockStateContext = lockStateContext;
        ShardingSphereEventBus.getInstance().register(this);
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
        if (lockHolder.getOrCreateInterMutexLock(lockNodeService.generateLocksName(lockName)).tryLock(timeoutMillis)) {
            lockStateContext.register(lockName);
            return true;
        }
        return false;
    }
    
    private Optional<InterMutexLock> getInterMutexLock(final String lockName) {
        return lockHolder.getInterMutexLock(lockNodeService.generateLocksName(lockName));
    }
    
    @Override
    public void releaseLock(final String lockName) {
        Optional<InterMutexLock> interMutexLock = getInterMutexLock(lockName);
        if (interMutexLock.isPresent()) {
            interMutexLock.get().unlock();
            lockStateContext.unregister(lockName);
        }
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return getInterMutexLock(lockName).map(InterMutexLock::isLocked).orElse(false);
    }
    
    /**
     * Database locked.
     *
     * @param event database locked event
     */
    @Subscribe
    public synchronized void locked(final DatabaseLockedEvent event) {
        String database = event.getDatabase();
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        InterMutexLock interMutexLock = lockHolder.getOrCreateInterMutexLock(lockNodeService.generateLocksName(database));
        interMutexLock.ackLock(lockNodeService.generateAckLockName(database, lockedInstanceId), lockedInstanceId);
        lockStateContext.register(database);
    }
    
    /**
     * Database lock released.
     *
     * @param event database lock released event
     */
    @Subscribe
    public synchronized void lockReleased(final DatabaseLockReleasedEvent event) {
        String database = event.getDatabase();
        String lockedInstanceId = lockHolder.getCurrentInstanceId();
        Optional<InterMutexLock> interMutexLock = getInterMutexLock(database);
        if (interMutexLock.isPresent()) {
            interMutexLock.get().releaseAckLock(lockNodeService.generateAckLockName(database, lockedInstanceId), lockedInstanceId);
            lockStateContext.unregister(database);
        }
    }
    
    /**
     * Database ack locked.
     *
     * @param event database ack locked event
     */
    @Subscribe
    public synchronized void ackLocked(final DatabaseAckLockedEvent event) {
        getInterMutexLock(event.getDatabase()).ifPresent(mutexLock -> mutexLock.addLockedInstance(event.getLockedInstance()));
    }
    
    /**
     * Database ack lock released.
     *
     * @param event database ack lock released event
     */
    @Subscribe
    public synchronized void ackLockReleased(final DatabaseAckLockReleasedEvent event) {
        getInterMutexLock(event.getDatabase()).ifPresent(mutexLock -> mutexLock.removeLockedInstance(event.getLockedInstance()));
    }
}
