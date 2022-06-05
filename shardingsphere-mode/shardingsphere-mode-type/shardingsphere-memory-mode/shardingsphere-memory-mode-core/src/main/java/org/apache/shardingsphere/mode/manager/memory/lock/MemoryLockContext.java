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

package org.apache.shardingsphere.mode.manager.memory.lock;

import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockMode;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

/**
 * Memory lock context.
 */
public final class MemoryLockContext implements LockContext {
    
    private final ShardingSphereLock memoryLock = new ShardingSphereMemoryLock();
    
    @Override
    public ShardingSphereLock getLock() {
        return memoryLock;
    }
    
    @Override
    public boolean tryLock(final String databaseName, final LockMode lockMode) {
        return memoryLock.tryLock(databaseName);
    }
    
    @Override
    public boolean tryLock(final String databaseName, final LockMode lockMode, final long timeoutMilliseconds) {
        return memoryLock.tryLock(databaseName, timeoutMilliseconds);
    }
    
    @Override
    public void releaseLock(final String databaseName) {
        memoryLock.releaseLock(databaseName);
    }
    
    @Override
    public boolean isLocked(final String databaseName) {
        return memoryLock.isLocked(databaseName);
    }
}
