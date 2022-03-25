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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Non reentrant lock implemented ShardingSphereLock.
 */
@RequiredArgsConstructor
public final class ShardingSphereNonReentrantLock implements ShardingSphereLock {
    
    private static final long DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS = 3 * 60 * 1000;
    
    private final Lock innerLock;
    
    private volatile boolean locked;
    
    @Override
    public boolean tryLock(final String lockName) {
        return innerTryLock(DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeout) {
        return innerTryLock(timeout);
    }
    
    private synchronized boolean innerTryLock(final long timeout) {
        try {
            if (innerLock.tryLock(timeout, TimeUnit.MILLISECONDS)) {
                locked = true;
            }
            return false;
        } catch (final InterruptedException ignored) {
            return false;
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        innerLock.unlock();
        locked = false;
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return locked;
    }
    
    @Override
    public long getDefaultTimeOut() {
        return DEFAULT_TRY_LOCK_TIMEOUT_MILLISECONDS;
    }
}
