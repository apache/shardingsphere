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
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLock;
import org.apache.shardingsphere.mode.repository.cluster.lock.InternalLockHolder;

import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Etcd internal lock holder.
 */
@RequiredArgsConstructor
@Slf4j
public final class EtcdInternalLockHolder implements InternalLockHolder {
    
    private final Map<String, EtcdInternalLock> locks = new ConcurrentHashMap<>();
    
    private final Client client;
    
    private final EtcdProperties etcdProps;
    
    @Override
    public synchronized InternalLock getInternalLock(final String lockKey) {
        EtcdInternalLock result = locks.get(lockKey);
        if (Objects.isNull(result)) {
            result = createLock(lockKey);
            locks.put(lockKey, result);
        }
        return result;
    }
    
    private EtcdInternalLock createLock(final String lockKey) {
        try {
            long leaseId = client.getLeaseClient().grant(etcdProps.getValue(EtcdPropertyKey.TIME_TO_LIVE_SECONDS)).get().getID();
            return new EtcdInternalLock(client.getLockClient(), lockKey, leaseId);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            log.error("Create etcd internal lock failed, lockName:{}", lockKey, ex);
        }
        return null;
    }
    
    /**
     * Etcd internal lock.
     */
    @RequiredArgsConstructor
    private static class EtcdInternalLock implements InternalLock {
        
        private final io.etcd.jetcd.Lock lock;
        
        private final String lockKey;
        
        private final long leaseId;
        
        @Override
        public boolean tryLock(final long timeout) {
            try {
                this.lock.lock(ByteSequence.from(lockKey, StandardCharsets.UTF_8), leaseId).get(timeout, TimeUnit.MILLISECONDS);
                return true;
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Etcd internal lock try lock failed", ex);
                return false;
            }
        }
        
        @Override
        public void unlock() {
            try {
                lock.unlock(ByteSequence.from(lockKey, StandardCharsets.UTF_8)).get();
                // CHECKSTYLE:OFF
            } catch (final Exception ex) {
                // CHECKSTYLE:ON
                log.error("Etcd internal lock unlock failed", ex);
            }
        }
    }
}
