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
import io.etcd.jetcd.Lock;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;

import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

/**
 * Etcd distributed lock.
 */
@RequiredArgsConstructor
public final class EtcdDistributedLock implements DistributedLock {
    
    private final Lock lock;
    
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
        }
    }
}
