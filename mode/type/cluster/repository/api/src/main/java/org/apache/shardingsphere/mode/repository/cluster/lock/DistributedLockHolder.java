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

package org.apache.shardingsphere.mode.repository.cluster.lock;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.util.props.TypedProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Distributed lock holder.
 */
@RequiredArgsConstructor
public final class DistributedLockHolder {
    
    private final Map<String, DistributedLock> locks = new HashMap<>();
    
    private final Object client;
    
    private final TypedProperties<?> props;
    
    /**
     * Get distributed lock.
     * 
     * @param lockKey lock key
     * @param type type
     * @return distributed lock
     */
    @SuppressWarnings("unchecked")
    public synchronized DistributedLock getDistributedLock(final String lockKey, final String type) {
        DistributedLock result = locks.get(lockKey);
        if (null == result) {
            result = DistributedLockCreatorFactory.newInstance(type).create(lockKey, client, props);
            locks.put(lockKey, result);
        }
        return result;
    }
}
