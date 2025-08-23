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

package org.apache.shardingsphere.mode.repository.cluster.core.lock.type;

import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.props.DefaultLockPropertyKey;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.props.DefaultLockTypedProperties;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;
import org.apache.shardingsphere.mode.retry.RetryExecutor;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Default distributed lock.
 */
public final class DefaultDistributedLock implements DistributedLock {
    
    private final String lockKey;
    
    private final ClusterPersistRepository client;
    
    private final String instanceId;
    
    private final Map<Thread, LockData> threadData = new ConcurrentHashMap<>();
    
    public DefaultDistributedLock(final String lockKey, final ClusterPersistRepository client, final DefaultLockTypedProperties props) {
        this.lockKey = lockKey;
        this.client = client;
        instanceId = props.getValue(DefaultLockPropertyKey.INSTANCE_ID);
    }
    
    @Override
    public boolean tryLock(final long timeoutMillis) {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        if (null != lockData) {
            lockData.increment();
            return true;
        }
        if (!new RetryExecutor(timeoutMillis, 100L).execute(this::persist, instanceId)) {
            return false;
        }
        threadData.put(currentThread, new LockData());
        return true;
    }
    
    private boolean persist(final String value) {
        return client.persistExclusiveEphemeral(lockKey, value);
    }
    
    @Override
    public void unlock() {
        Thread currentThread = Thread.currentThread();
        LockData lockData = threadData.get(currentThread);
        ShardingSpherePreconditions.checkNotNull(lockData, () -> new IllegalMonitorStateException(String.format("You do not own the lock: %s.", lockKey)));
        int newLockCount = lockData.lockCount.decrementAndGet();
        if (newLockCount > 0) {
            return;
        }
        try {
            client.delete(lockKey);
        } finally {
            threadData.remove(currentThread);
        }
    }
    
    private static final class LockData {
        
        private final AtomicInteger lockCount = new AtomicInteger(1);
        
        private void increment() {
            lockCount.incrementAndGet();
        }
    }
}
