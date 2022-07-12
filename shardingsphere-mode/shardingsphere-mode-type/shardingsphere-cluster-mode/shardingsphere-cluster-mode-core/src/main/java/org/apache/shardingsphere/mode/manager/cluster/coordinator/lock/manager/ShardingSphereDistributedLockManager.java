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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager;

import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.lock.LockMode;
import org.apache.shardingsphere.infra.lock.LockNameDefinition;
import org.apache.shardingsphere.infra.lock.LockScope;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.ShardingSphereDistributedDatabaseLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.ShardingSphereDistributedGlobalLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed.ShardingSphereDistributedStandardLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.state.LockStateContext;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.state.LockStateContextFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.internal.ShardingSphereInternalLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockDefinition;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockNameDefinition;

/**
 * Distribute lock manager of ShardingSphere.
 */
@Slf4j
public final class ShardingSphereDistributedLockManager implements ShardingSphereLockManager {
    
    private LockStateContext lockStateContext;
    
    private ShardingSphereLock standardDistributedLock;
    
    private ShardingSphereLock globalDistributedLock;
    
    private ShardingSphereLock databaseLock;
    
    @Override
    public void init(final ShardingSphereInternalLockHolder lockHolder, final EventBusContext eventBusContext) {
        lockStateContext = LockStateContextFactory.getLockStateContext();
        standardDistributedLock = new ShardingSphereDistributedStandardLock(lockHolder);
        globalDistributedLock = new ShardingSphereDistributedGlobalLock(lockHolder, eventBusContext);
        databaseLock = new ShardingSphereDistributedDatabaseLock(lockHolder, lockStateContext, eventBusContext);
    }
    
    @Override
    public ShardingSphereLock getDistributedLock(final LockScope lockScope) {
        switch (lockScope) {
            case STANDARD:
                return standardDistributedLock;
            case GLOBAL:
                return globalDistributedLock;
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    @Override
    public boolean tryLock(final DatabaseLockDefinition lockNameDefinition) {
        Preconditions.checkNotNull(lockNameDefinition, "Try Lock for database arg lock name definition can not be null.");
        return tryLock(lockNameDefinition, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final DatabaseLockDefinition lockDefinition, final long timeoutMilliseconds) {
        Preconditions.checkNotNull(lockDefinition, "Try Lock for database arg lock definition can not be null.");
        return innerTryLock(lockDefinition.getLockMode(), lockDefinition.getLockNameDefinition(), timeoutMilliseconds);
    }
    
    private synchronized boolean innerTryLock(final LockMode lockMode, final DatabaseLockNameDefinition lockNameDefinition, final long timeoutMilliseconds) {
        Preconditions.checkNotNull(lockNameDefinition, "Try Lock for database arg database name can not be null.");
        Preconditions.checkNotNull(lockMode, "Try Lock for database args lock mode can not be null.");
        switch (lockMode) {
            case READ:
                return innerDatabaseTryLock(lockNameDefinition.getDatabaseName(), timeoutMilliseconds);
            case WRITE:
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private boolean innerDatabaseTryLock(final String databaseName, final long timeoutMilliseconds) {
        if (databaseLock.tryLock(databaseName, timeoutMilliseconds - TimeoutMilliseconds.DEFAULT_REGISTRY)) {
            log.debug("Distribute database lock acquire sequenced success, database name: {}", databaseName);
            return true;
        }
        log.debug("Distribute database lock acquire sequenced failed, database name: {}", databaseName);
        return false;
    }
    
    @Override
    public void releaseLock(final DatabaseLockDefinition lockDefinition) {
        Preconditions.checkNotNull(lockDefinition, "Try Lock for database arg lock definition can not be null.");
        String databaseName = lockDefinition.getLockNameDefinition().getDatabaseName();
        Preconditions.checkNotNull(databaseName, "Release lock write args database name can not be null.");
        databaseLock.releaseLock(databaseName);
    }
    
    @Override
    public boolean isLocked(final DatabaseLockDefinition lockDefinition) {
        Preconditions.checkNotNull(lockDefinition, "Try Lock for database arg lock definition can not be null.");
        LockNameDefinition lockNameDefinition = lockDefinition.getLockNameDefinition();
        Preconditions.checkNotNull(lockNameDefinition, "Is locked database args lock name definition can not be null.");
        return lockStateContext.isLocked(lockNameDefinition);
    }
}
