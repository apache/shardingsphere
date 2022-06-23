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
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.manager.lock.AbstractLockContext;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockNameDefinition;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;

/**
 * Distributed lock context.
 */
@RequiredArgsConstructor
public final class DistributedLockContext extends AbstractLockContext {
    
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
    protected boolean tryLock(final DatabaseLockNameDefinition lockNameDefinition) {
        return lockManager.tryLock(lockNameDefinition.getDatabaseName(), lockNameDefinition.getLockMode());
    }
    
    @Override
    protected boolean tryLock(final DatabaseLockNameDefinition lockNameDefinition, final long timeoutMilliseconds) {
        return lockManager.tryLock(lockNameDefinition.getDatabaseName(), lockNameDefinition.getLockMode(), timeoutMilliseconds);
    }
    
    @Override
    protected void releaseLock(final DatabaseLockNameDefinition lockNameDefinition) {
        lockManager.releaseLock(lockNameDefinition.getDatabaseName());
    }
    
    @Override
    protected boolean isLocked(final DatabaseLockNameDefinition lockNameDefinition) {
        return lockManager.isLocked(lockNameDefinition.getDatabaseName());
    }
}
