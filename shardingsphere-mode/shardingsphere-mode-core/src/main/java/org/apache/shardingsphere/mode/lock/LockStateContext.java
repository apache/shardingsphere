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

package org.apache.shardingsphere.mode.lock;

import org.apache.shardingsphere.infra.lock.LockDefinition;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lock state context.
 */
public final class LockStateContext {
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final AtomicInteger lockCounter = new AtomicInteger(0);
    
    private final Map<String, Boolean> lockStates = new LinkedHashMap<>();
    
    /**
     * Register lock state.
     *
     * @param lockDefinition lock definition
     */
    public void register(final LockDefinition lockDefinition) {
        lock.writeLock().lock();
        try {
            Boolean isLocked = lockStates.get(lockDefinition.getLockKey());
            if (null == isLocked || !isLocked) {
                lockStates.put(lockDefinition.getLockKey(), true);
                lockCounter.incrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Un-register lock state.
     *
     * @param lockDefinition lock definition
     */
    public void unregister(final LockDefinition lockDefinition) {
        lock.writeLock().lock();
        try {
            Boolean isLocked = lockStates.get(lockDefinition.getLockKey());
            if (null != isLocked && isLocked) {
                lockStates.put(lockDefinition.getLockKey(), false);
                lockCounter.decrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    /**
     * Is locked.
     *
     * @param lockDefinition lock definition
     * @return is locked or not
     */
    public boolean isLocked(final LockDefinition lockDefinition) {
        if (isExistLock()) {
            return false;
        }
        lock.readLock().lock();
        try {
            Boolean isLocked = lockStates.get(lockDefinition.getLockKey());
            return null != isLocked && isLocked;
        } finally {
            lock.readLock().unlock();
        }
    }
    
    private boolean isExistLock() {
        return 0 == lockCounter.get();
    }
}
