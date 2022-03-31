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

package org.apache.shardingsphere.mode.manager.standalone.lock;

import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.lock.LockContext;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Standalone lock context.
 */
public final class StandaloneLockContext implements LockContext {
    
    private final Map<String, ShardingSphereLock> locks = new ConcurrentHashMap<>();
    
    @Override
    public ShardingSphereLock getOrCreateSchemaLock(final String schemaName) {
        ShardingSphereLock result = locks.get(schemaName);
        if (null != result) {
            return result;
        }
        synchronized (locks) {
            result = locks.get(schemaName);
            if (null != result) {
                return result;
            }
            result = new ShardingSphereNonReentrantLock(new ReentrantLock());
            locks.put(schemaName, result);
            return result;
        }
    }
    
    @Override
    public Optional<ShardingSphereLock> getSchemaLock(final String schemaName) {
        return Optional.ofNullable(locks.get(schemaName));
    }
    
    @Override
    public boolean isLockedSchema(final String schemaName) {
        ShardingSphereLock shardingSphereLock = locks.get(schemaName);
        return null != shardingSphereLock && shardingSphereLock.isLocked(schemaName);
    }
}
