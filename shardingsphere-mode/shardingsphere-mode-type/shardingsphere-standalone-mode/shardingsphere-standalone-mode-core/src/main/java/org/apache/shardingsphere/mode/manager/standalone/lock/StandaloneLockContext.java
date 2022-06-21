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

import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockLevel;
import org.apache.shardingsphere.infra.lock.LockNameDefinition;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.lock.DatabaseLockNameDefinition;

/**
 * Standalone lock context.
 */
public final class StandaloneLockContext implements LockContext {
    
    private final ShardingSphereLock standaloneLock = new ShardingSphereStandaloneLock();
    
    @Override
    public ShardingSphereLock getLock() {
        return standaloneLock;
    }
    
    @Override
    public boolean tryLock(final LockNameDefinition lockNameDefinition) {
        LockLevel lockLevel = lockNameDefinition.getLockLevel();
        switch (lockLevel) {
            case DATABASE:
                DatabaseLockNameDefinition lockDefinition = (DatabaseLockNameDefinition) lockNameDefinition;
                return standaloneLock.tryLock(lockDefinition.getDatabaseName());
            case SCHEMA:
            case TABLE:
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public boolean tryLock(final LockNameDefinition lockNameDefinition, final long timeoutMilliseconds) {
        LockLevel lockLevel = lockNameDefinition.getLockLevel();
        switch (lockLevel) {
            case DATABASE:
                DatabaseLockNameDefinition lockDefinition = (DatabaseLockNameDefinition) lockNameDefinition;
                return standaloneLock.tryLock(lockDefinition.getDatabaseName(), timeoutMilliseconds);
            case SCHEMA:
            case TABLE:
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public void releaseLock(final LockNameDefinition lockNameDefinition) {
        LockLevel lockLevel = lockNameDefinition.getLockLevel();
        switch (lockLevel) {
            case DATABASE:
                DatabaseLockNameDefinition lockDefinition = (DatabaseLockNameDefinition) lockNameDefinition;
                standaloneLock.releaseLock(lockDefinition.getDatabaseName());
                break;
            case SCHEMA:
            case TABLE:
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public boolean isLocked(final LockNameDefinition lockNameDefinition) {
        LockLevel lockLevel = lockNameDefinition.getLockLevel();
        switch (lockLevel) {
            case DATABASE:
                DatabaseLockNameDefinition lockDefinition = (DatabaseLockNameDefinition) lockNameDefinition;
                return standaloneLock.isLocked(lockDefinition.getDatabaseName());
            case SCHEMA:
            case TABLE:
            default:
                throw new UnsupportedOperationException();
        }
    }
}
