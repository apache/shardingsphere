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
import org.apache.curator.framework.recipes.locks.InterProcessSemaphoreMutex;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.zookeeper.handler.CuratorZookeeperExceptionHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Zookeeper internal lock holder.
 */
@RequiredArgsConstructor
public final class ZookeeperInternalLockHolder implements InternalLockHolder {
    
    private final Map<String, ZookeeperInternalLock> locks = new LinkedHashMap<>();
    
    private final CuratorFramework client;
    
    @Override
    public InternalLock getInternalLock(final String lockKey) {
        ZookeeperInternalLock result = locks.get(lockKey);
        if (Objects.isNull(result)) {
            result = new ZookeeperInternalLock(new InterProcessSemaphoreMutex(client, lockKey));
            locks.put(lockKey, result);
        }
        return result;
    }
    
    /**
     * Zookeeper internal lock.
     */
    @RequiredArgsConstructor
    public static class ZookeeperInternalLock implements InternalLock {
        
        private final InterProcessLock lock;
        
        @Override
        public boolean tryLock(final long timeout) {
            try {
                return lock.acquire(timeout, TimeUnit.MILLISECONDS);
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
    }
}
