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

package org.apache.shardingsphere.mode.repository.cluster.core.lock;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.DefaultDistributedLock;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.type.props.DefaultLockTypedProperties;
import org.apache.shardingsphere.mode.repository.cluster.lock.DistributedLock;

import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Distributed lock holder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class DistributedLockHolder {
    
    private static final Map<String, DistributedLock> LOCKS = new ConcurrentHashMap<>();
    
    /**
     * Get distributed lock.
     *
     * @param lockKey lock key
     * @param repository repository
     * @return got distributed lock
     */
    public static DistributedLock getDistributedLock(final String lockKey, final ClusterPersistRepository repository) {
        return LOCKS.computeIfAbsent(lockKey, key -> loadDistributedLock(lockKey, repository));
    }
    
    private static DistributedLock loadDistributedLock(final String lockKey, final ClusterPersistRepository repository) {
        return repository.getDistributedLock(lockKey).orElseGet(() -> new DefaultDistributedLock(lockKey, repository, new DefaultLockTypedProperties(new Properties())));
    }
}
