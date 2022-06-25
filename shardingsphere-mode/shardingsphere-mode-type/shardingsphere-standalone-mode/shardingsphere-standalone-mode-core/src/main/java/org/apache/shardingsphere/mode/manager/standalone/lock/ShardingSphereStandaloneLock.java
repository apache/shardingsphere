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
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standalone mutex lock of ShardingSphere.
 */
public final class ShardingSphereStandaloneLock implements ShardingSphereLock {
    
    private static final long DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private final Map<String, ReentrantLock> locks = new ConcurrentHashMap<>();
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return innerTryLock(lockName, timeoutMillis);
    }
    
    private synchronized boolean innerTryLock(final String lockName, final long timeoutMillis) {
        Preconditions.checkNotNull(lockName, "Try lock args lockName name can not be null.");
        ReentrantLock lock = locks.get(lockName);
        if (null == lock) {
            lock = new StandaloneMutexLock();
            locks.put(lockName, lock);
        }
        return innerTryLock(lock, timeoutMillis);
    }
    
    private boolean innerTryLock(final ReentrantLock lock, final long timeoutMillis) {
        try {
            return lock.tryLock(timeoutMillis, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignored) {
            return false;
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Release lock args lockName name can not be null.");
        locks.get(lockName).unlock();
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        Preconditions.checkNotNull(lockName, "Is locked args lockName name can not be null.");
        if (locks.isEmpty()) {
            return false;
        }
        ReentrantLock lock = locks.get(lockName);
        if (null == lock) {
            return false;
        }
        return lock.isLocked();
    }
}
