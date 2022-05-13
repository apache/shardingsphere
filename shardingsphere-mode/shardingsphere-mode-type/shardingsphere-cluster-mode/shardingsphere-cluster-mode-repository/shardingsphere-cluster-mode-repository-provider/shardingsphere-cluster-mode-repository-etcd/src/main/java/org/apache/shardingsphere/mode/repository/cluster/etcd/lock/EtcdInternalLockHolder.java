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

package org.apache.shardingsphere.mode.repository.cluster.etcd.lock;

import io.etcd.jetcd.ByteSequence;
import io.etcd.jetcd.Client;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdProperties;
import org.apache.shardingsphere.mode.repository.cluster.etcd.props.EtcdPropertyKey;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * Etcd internal lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public class EtcdInternalLockHolder {
    
    private final Map<String, EtcdInternalLock> locks = new ConcurrentHashMap<>();
    
    private final Client client;
    
    private final EtcdProperties etcdProps;
    
    /**
     * Get internal mutex lock.
     *
     * @param lockName lock name
     * @return internal mutex lock
     */
    public Lock getInternalMutexLock(final String lockName) {
        return getInternalReentrantMutexLock(lockName);
    }
    
    /**
     * Get internal reentrant mutex lock.
     *
     * @param lockName lock name
     * @return internal reentrant mutex lock
     */
    public Lock getInternalReentrantMutexLock(final String lockName) {
        EtcdInternalLock result = locks.get(lockName);
        if (Objects.isNull(result)) {
            result = createLock(lockName);
            locks.put(lockName, result);
        }
        return result;
    }
    
    private EtcdInternalLock createLock(final String lockName) {
        try {
            long leaseId = client.getLeaseClient().grant(etcdProps.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).get().getID();
            return new EtcdInternalLock(client.getLockClient(), lockName, leaseId);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("EtcdRepository tryLock error, lockName:{}", lockName, ex);
        }
        return null;
    }
    
    /**
     * Etcd internal lock.
     */
    @RequiredArgsConstructor
    private static class EtcdInternalLock implements Lock {
        
        private final io.etcd.jetcd.Lock lock;
        
        private final String lockName;
        
        private final long leaseId;
        
        @Override
        public void lock() {
            try {
                this.lock.lock(ByteSequence.from(lockName, StandardCharsets.UTF_8), leaseId).get();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository tryLock error, lockName:{}", lockName, ex);
            }
        }
        
        @Override
        public boolean tryLock() {
            throw new UnsupportedOperationException();
        }
        
        @Override
        public boolean tryLock(final long time, final TimeUnit timeUnit) {
            try {
                this.lock.lock(ByteSequence.from(lockName, StandardCharsets.UTF_8), leaseId).get(time, timeUnit);
                return true;
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository tryLock error, lockName:{}", lockName, ex);
                return false;
            }
        }
        
        @Override
        public void unlock() {
            try {
                lock.unlock(ByteSequence.from(lockName, StandardCharsets.UTF_8)).get();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("EtcdRepository unlock error, lockName:{}", lockName, ex);
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
