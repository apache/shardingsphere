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

package org.apache.shardingsphere.mode.repository.cluster.zookeeper.lock;

import lombok.RequiredArgsConstructor;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.locks.InterProcessLock;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.handler.CuratorZookeeperExceptionHandler;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Zookeeper internal lock holder.
 */
@RequiredArgsConstructor
public class ZookeeperInternalLockHolder {
    
    private final Map<String, ZookeeperInternalLock> locks = new ConcurrentHashMap<>();
    
    private final CuratorFramework client;
    
    /**
     * Get global lock.
     *
     * @param lockName lock name
     * @return global lock
     */
    public Lock getGlobalLock(final String lockName) {
        ZookeeperInternalLock lock = locks.get(lockName);
        if (Objects.isNull(lock)) {
            lock = new ZookeeperInternalLock(new InterProcessSemaphoreMutex(client, lockName));
            locks.put(lockName, lock);
        }
        return lock;
    }
    
    /**
     * Get standard lock.
     *
     * @param lockName lock name
     * @return standard lock
     */
    public Lock getStandardLock(final String lockName) {
        ZookeeperInternalLock lock = locks.get(lockName);
        if (Objects.isNull(lock)) {
            lock = new ZookeeperInternalLock(new InterProcessMutex(client, lockName));
            locks.put(lockName, lock);
        }
        return lock;
    }
    
    /**
     * Zookeeper internal lock.
     */
    @RequiredArgsConstructor
    public static class ZookeeperInternalLock implements Lock {
        
        private final InterProcessLock lock;
        
        @Override
        public void lock() {
            try {
                lock.acquire();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                CuratorZookeeperExceptionHandler.handleException(ex);
            }
        }
        
        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean tryLock(final long time, final TimeUnit timeUnit) {
            try {
                return lock.acquire(time, timeUnit);
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                CuratorZookeeperExceptionHandler.handleException(ex);
            }
            return false;
        }
        
        @Override
        public void unlock() {
            try {
                lock.release();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                CuratorZookeeperExceptionHandler.handleException(ex);
            }
        }
        
        @Override
        public void lockInterruptibly() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public Condition newCondition() {
            throw new UnsupportedOperationException();
        }
    }
}
