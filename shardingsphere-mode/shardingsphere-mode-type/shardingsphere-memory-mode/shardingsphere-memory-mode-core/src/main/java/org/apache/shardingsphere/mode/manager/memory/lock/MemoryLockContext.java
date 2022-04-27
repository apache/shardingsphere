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

package org.apache.shardingsphere.mode.manager.memory.lock;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Memory lock context.
 */
public final class MemoryLockContext implements LockContext {
    
    private final Map<String, ShardingSphereLock> locks = new ConcurrentHashMap<>();
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        throw new UnsupportedOperationException("Lock context init lock state not supported in memory mode");
    }
    
    @Override
    public boolean tryLockWriteDatabase(final String databaseName, final long timeoutMillis) {
        ShardingSphereLock lock = getOrCreateGlobalLock(databaseName);
        return lock.tryLock(databaseName, timeoutMillis);
    }
    
    @Override
    public void releaseLockWriteDatabase(final String databaseName) {
        ShardingSphereLock lock = getOrCreateGlobalLock(databaseName);
        lock.releaseLock(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        ShardingSphereLock shardingSphereLock = locks.get(databaseName);
        return null != shardingSphereLock && shardingSphereLock.isLocked(databaseName);
    }
    
    @Override
    public ShardingSphereLock getOrCreateGlobalLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get or create global lock args lock name can not be null.");
        ShardingSphereLock result = locks.get(lockName);
        if (null != result) {
            return result;
        }
        synchronized (locks) {
            result = locks.get(lockName);
            if (null != result) {
                return result;
            }
            result = new ShardingSphereNonReentrantLock(new ReentrantLock());
            locks.put(lockName, result);
            return result;
        }
    }
    
    @Override
    public ShardingSphereLock getGlobalLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get global lock args lock name can not be null.");
        return locks.get(lockName);
    }
}
