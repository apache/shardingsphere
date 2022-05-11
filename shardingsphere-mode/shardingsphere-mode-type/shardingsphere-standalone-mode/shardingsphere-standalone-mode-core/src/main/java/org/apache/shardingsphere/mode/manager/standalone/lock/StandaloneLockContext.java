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

package org.apache.shardingsphere.mode.manager.standalone.lock;

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standalone lock context.
 */
public final class StandaloneLockContext implements LockContext {
    
    private final Map<String, ShardingSphereLock> locks = new ConcurrentHashMap<>();
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        throw new UnsupportedOperationException("Lock context init lock state not supported in standalone mode");
    }
    
    @Override
    public synchronized boolean tryLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Try lock write database args database name can not be null.");
        return getGlobalLock(databaseName).tryLock(databaseName);
    }
    
    @Override
    public void releaseLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Release lock write database args database name can not be null.");
        getGlobalLock(databaseName).releaseLock(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        ShardingSphereLock shardingSphereLock = locks.get(databaseName);
        return null != shardingSphereLock && shardingSphereLock.isLocked(databaseName);
    }
    
    @Override
    public ShardingSphereLock getGlobalLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get global lock args lock name can not be null.");
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
    public ShardingSphereLock getStandardLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get standard lock args lock name can not be null.");
        ShardingSphereLock result = locks.get(lockName);
        if (null != result) {
            return result;
        }
        synchronized (locks) {
            result = locks.get(lockName);
            if (null != result) {
                return result;
            }
            result = new ShardingSphereReentrantLock(new ReentrantLock());
            locks.put(lockName, result);
            return result;
        }
    }
}
