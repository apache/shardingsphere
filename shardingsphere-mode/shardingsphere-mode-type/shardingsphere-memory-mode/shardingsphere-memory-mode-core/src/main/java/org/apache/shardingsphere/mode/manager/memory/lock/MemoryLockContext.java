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

import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;

import java.util.Set;

/**
 * Memory lock context.
 */
public final class MemoryLockContext implements LockContext {
    
    private final ShardingSphereLock mutexLock = new ShardingSphereMemoryMutexLock();
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        throw new UnsupportedOperationException("Lock context init lock state not supported in memory mode");
    }
    
    @Override
    public ShardingSphereLock getMutexLock() {
        return mutexLock;
    }
    
    @Override
    public boolean lockWrite(final String databaseName) {
        return mutexLock.tryLock(databaseName);
    }
    
    @Override
    public boolean lockWrite(final String databaseName, final Set<String> schemaNames) {
        // TODO when the lock structure adjustment is completed
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean tryLockWrite(final String databaseName, final long timeoutMilliseconds) {
        return mutexLock.tryLock(databaseName, timeoutMilliseconds);
    }
    
    @Override
    public boolean tryLockWrite(final String databaseName, final Set<String> schemaNames, final long timeoutMilliseconds) {
        // TODO when the lock structure adjustment is completed
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void releaseLockWrite(final String databaseName) {
        mutexLock.releaseLock(databaseName);
    }
    
    @Override
    public void releaseLockWrite(final String databaseName, final String schemaName) {
        // TODO when the lock structure adjustment is completed
        throw new UnsupportedOperationException();
    }
    
    @Override
    public boolean isLocked(final String databaseName) {
        return mutexLock.isLocked(databaseName);
    }
    
    @Override
    public boolean isLocked(final String databaseName, final String schemaName) {
        // TODO when the lock structure adjustment is completed
        throw new UnsupportedOperationException();
    }
}
