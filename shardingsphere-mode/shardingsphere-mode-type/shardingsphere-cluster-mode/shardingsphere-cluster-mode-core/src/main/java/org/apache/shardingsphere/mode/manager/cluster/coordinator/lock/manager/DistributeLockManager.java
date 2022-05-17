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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.ShardingSphereDistributeDatabaseLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.InterReentrantMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereDistributeMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

/**
 * Distribute lock manager.
 */
public final class DistributeLockManager implements ShardingSphereLockManager {
    
    private InterReentrantMutexLock sequencedLock;
    
    private ShardingSphereDistributeMutexLock mutexLock;
    
    private ShardingSphereDistributeDatabaseLock databaseLock;
    
    @Override
    public void init(final ShardingSphereInterMutexLockHolder lockHolder) {
        sequencedLock = lockHolder.getInterReentrantMutexLock("/lock/sequence");
        mutexLock = new ShardingSphereDistributeMutexLock(lockHolder);
        databaseLock = new ShardingSphereDistributeDatabaseLock(lockHolder);
    }
    
    @Override
    public ShardingSphereLock getMutexLock() {
        return mutexLock;
    }
    
    @Override
    public boolean lockWrite(final String databaseName) {
        return tryLockWrite(databaseName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLockWrite(final String databaseName, final long timeoutMilliseconds) {
        return innerTryLock(databaseName, timeoutMilliseconds);
    }
    
    private synchronized boolean innerTryLock(final String databaseName, final long timeoutMilliseconds) {
        Preconditions.checkNotNull(databaseName, "Try Lock write for databaseName args database name can not be null.");
        if (!sequencedLock.tryLock(TimeoutMilliseconds.DEFAULT_REGISTRY)) {
            return false;
        }
        try {
            return databaseLock.tryLock(databaseName, timeoutMilliseconds - TimeoutMilliseconds.DEFAULT_REGISTRY);
        } finally {
            sequencedLock.unlock();
        }
    }
    
    @Override
    public void releaseLockWrite(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Release lock write args database name can not be null.");
        databaseLock.releaseLock(databaseName);
    }
    
    @Override
    public boolean isLocked(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        return databaseLock.isLocked(databaseName);
    }
}
