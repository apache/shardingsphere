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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.LockLevel;
import org.apache.shardingsphere.infra.lock.LockNameDefinition;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.manager.lock.DatabaseLockNameDefinition;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;

/**
 * Distributed lock context.
 */
@RequiredArgsConstructor
public final class DistributedLockContext implements LockContext {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereLockManager.class);
    }
    
    private final ClusterPersistRepository repository;
    
    private ShardingSphereLockManager lockManager;
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        loadLockManager(new ShardingSphereInterMutexLockHolder(repository, instanceContext.getInstance(), instanceContext.getComputeNodeInstances()));
    }
    
    private void loadLockManager(final ShardingSphereInterMutexLockHolder lockHolder) {
        lockManager = RequiredSPIRegistry.getRegisteredService(ShardingSphereLockManager.class);
        lockManager.init(lockHolder);
    }
    
    @Override
    public ShardingSphereLock getLock() {
        return lockManager.getDistributedLock();
    }
    
    @Override
    public boolean tryLock(final LockNameDefinition lockNameDefinition) {
        LockLevel lockLevel = lockNameDefinition.getLockLevel();
        switch (lockLevel) {
            case DATABASE:
                DatabaseLockNameDefinition lockDefinition = (DatabaseLockNameDefinition) lockNameDefinition;
                return lockManager.tryLock(lockDefinition.getDatabaseName(), lockDefinition.getLockMode());
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
                return lockManager.tryLock(lockDefinition.getDatabaseName(), lockDefinition.getLockMode(), timeoutMilliseconds);
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
                lockManager.releaseLock(lockDefinition.getDatabaseName());
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
                return lockManager.isLocked(lockDefinition.getDatabaseName());
            case SCHEMA:
            case TABLE:
            default:
                throw new UnsupportedOperationException();
        }
    }
}
