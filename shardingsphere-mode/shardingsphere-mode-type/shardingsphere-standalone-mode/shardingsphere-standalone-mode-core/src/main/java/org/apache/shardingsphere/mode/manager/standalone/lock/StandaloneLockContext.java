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

import org.apache.shardingsphere.infra.lock.LockScope;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.lock.AbstractLockContext;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockDefinition;

/**
 * Standalone lock context.
 */
public final class StandaloneLockContext extends AbstractLockContext {
    
    private final ShardingSphereLock standaloneLock = new ShardingSphereStandaloneLock();
    
    @Override
    public ShardingSphereLock getLock(final LockScope lockScope) {
        return standaloneLock;
    }
    
    @Override
    protected boolean tryLock(final DatabaseLockDefinition lockDefinition) {
        return standaloneLock.tryLock(lockDefinition.getLockNameDefinition().getDatabaseName());
    }
    
    @Override
    protected boolean tryLock(final DatabaseLockDefinition lockDefinition, final long timeoutMilliseconds) {
        return standaloneLock.tryLock(lockDefinition.getLockNameDefinition().getDatabaseName(), timeoutMilliseconds);
    }
    
    @Override
    protected void releaseLock(final DatabaseLockDefinition lockDefinition) {
        standaloneLock.releaseLock(lockDefinition.getLockNameDefinition().getDatabaseName());
    }
    
    @Override
    protected boolean isLocked(final DatabaseLockDefinition lockDefinition) {
        return standaloneLock.isLocked(lockDefinition.getLockNameDefinition().getDatabaseName());
    }
}
