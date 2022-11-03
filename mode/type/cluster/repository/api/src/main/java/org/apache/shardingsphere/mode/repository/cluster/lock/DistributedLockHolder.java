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

import org.apache.shardingsphere.infra.util.props.TypedProperties;

import java.util.HashMap;
import java.util.Map;

/**
 * Distributed lock holder.
 */
public final class DistributedLockHolder {
    
    private final DistributedLockCreator<Object, TypedProperties<?>> creator;
    
    private final Object client;
    
    private final TypedProperties<?> props;
    
    private final Map<String, DistributedLock> locks;
    
    @SuppressWarnings("unchecked")
    public DistributedLockHolder(final String type, final Object client, final TypedProperties<?> props) {
        creator = DistributedLockCreatorFactory.newInstance(type);
        this.client = client;
        this.props = props;
        locks = new HashMap<>();
    }
    
    /**
     * Get distributed lock.
     * 
     * @param lockKey lock key
     * @return distributed lock
     */
    public synchronized DistributedLock getDistributedLock(final String lockKey) {
        DistributedLock result = locks.get(lockKey);
        if (null == result) {
            result = creator.create(lockKey, client, props);
            locks.put(lockKey, result);
        }
        return result;
    }
}
