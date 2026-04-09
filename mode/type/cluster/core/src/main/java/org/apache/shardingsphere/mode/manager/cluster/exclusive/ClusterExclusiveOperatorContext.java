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

package org.apache.shardingsphere.mode.manager.cluster.exclusive;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.exclusive.ExclusiveLockHandle;
import org.apache.shardingsphere.mode.exclusive.ExclusiveOperatorContext;
import org.apache.shardingsphere.mode.retry.RetryExecutor;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.DistributedLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;

import java.util.Collection;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Cluster exclusive operator context.
 */
@RequiredArgsConstructor
public final class ClusterExclusiveOperatorContext implements ExclusiveOperatorContext {
    
    private static final long RETRY_INTERVAL_MILLIS = 50L;
    
    private final ClusterPersistRepository repository;
    
    private final Collection<String> exclusiveOperationKeys = new CopyOnWriteArraySet<>();
    
    @Override
    public Optional<ExclusiveLockHandle> start(final String operationKey, final long timeoutMillis) {
        final long startTimeMillis = System.currentTimeMillis();
        if (!new RetryExecutor(timeoutMillis, RETRY_INTERVAL_MILLIS).execute(arg -> exclusiveOperationKeys.add(operationKey), null)) {
            return Optional.empty();
        }
        final DistributedLock distributedLock = DistributedLockHolder.getDistributedLock(operationKey, repository);
        if (!distributedLock.tryLock(getRemainingTimeoutMillis(startTimeMillis, timeoutMillis))) {
            exclusiveOperationKeys.remove(operationKey);
            return Optional.empty();
        }
        return Optional.of(new ClusterExclusiveLockHandle(operationKey, distributedLock, exclusiveOperationKeys));
    }
    
    private long getRemainingTimeoutMillis(final long startTimeMillis, final long timeoutMillis) {
        if (timeoutMillis < 0L) {
            return timeoutMillis;
        }
        return Math.max(0L, timeoutMillis - (System.currentTimeMillis() - startTimeMillis));
    }
    
    private static final class ClusterExclusiveLockHandle implements ExclusiveLockHandle {
        
        private final String operationKey;
        
        private final DistributedLock distributedLock;
        
        private final Collection<String> exclusiveOperationKeys;
        
        private final AtomicBoolean closed = new AtomicBoolean();
        
        private ClusterExclusiveLockHandle(final String operationKey, final DistributedLock distributedLock, final Collection<String> exclusiveOperationKeys) {
            this.operationKey = operationKey;
            this.distributedLock = distributedLock;
            this.exclusiveOperationKeys = exclusiveOperationKeys;
        }
        
        @Override
        public void close() {
            if (!closed.compareAndSet(false, true)) {
                return;
            }
            try {
                distributedLock.unlock();
            } finally {
                exclusiveOperationKeys.remove(operationKey);
            }
        }
    }
}
