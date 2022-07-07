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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.state;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Lock state context.
 */
public final class ShardingSphereLockStateContext implements LockStateContext {
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    
    private final AtomicInteger lockCounter = new AtomicInteger(0);
    
    private final Map<String, Set<String>> lockStates = new LinkedHashMap<>();
    
    @Override
    public void register(final String databaseName) {
        lock.writeLock().lock();
        try {
            if (lockStates.computeIfAbsent(databaseName, locks -> new LinkedHashSet<>()).add("@all")) {
                lockCounter.incrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void unregister(final String databaseName) {
        lock.writeLock().lock();
        try {
            if (lockStates.get(databaseName).remove("@all")) {
                lockCounter.decrementAndGet();
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean isLocked(final String databaseName) {
        if (0 == lockCounter.get()) {
            return false;
        }
        lock.readLock().lock();
        try {
            Set<String> locks = lockStates.get(databaseName);
            return null != locks && !locks.isEmpty();
        } finally {
            lock.readLock().unlock();
        }
    }
}
