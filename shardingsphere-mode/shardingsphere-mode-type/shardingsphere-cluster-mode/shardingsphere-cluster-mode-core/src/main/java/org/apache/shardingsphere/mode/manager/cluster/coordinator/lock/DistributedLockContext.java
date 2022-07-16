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
import org.apache.shardingsphere.infra.eventbus.EventBusContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockScope;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.internal.ShardingSphereInternalLockHolder;
import org.apache.shardingsphere.mode.manager.lock.AbstractLockContext;
import org.apache.shardingsphere.mode.manager.lock.definition.DatabaseLockDefinition;
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
        loadLockManager(new ShardingSphereInternalLockHolder(repository, instanceContext.getInstance(), instanceContext.getAllClusterInstances()), instanceContext.getEventBusContext());
    }
    
    private void loadLockManager(final ShardingSphereInternalLockHolder lockHolder, final EventBusContext eventBusContext) {
        lockManager = RequiredSPIRegistry.getRegisteredService(ShardingSphereLockManager.class);
        lockManager.init(lockHolder, eventBusContext);
    }
    
    @Override
    public ShardingSphereLock getLock(final LockScope lockScope) {
        return lockManager.getDistributedLock(lockScope);
    }
    
    @Override
    protected boolean tryLock(final DatabaseLockDefinition lockDefinition) {
        return lockManager.tryLock(lockDefinition);
    }
    
    @Override
    protected boolean tryLock(final DatabaseLockDefinition lockDefinition, final long timeoutMilliseconds) {
        return lockManager.tryLock(lockDefinition, timeoutMilliseconds);
    }
    
    @Override
    protected void releaseLock(final DatabaseLockDefinition lockDefinition) {
        lockManager.releaseLock(lockDefinition);
    }
    
    @Override
    protected boolean isLocked(final DatabaseLockDefinition lockDefinition) {
        return lockManager.isLocked(lockDefinition);
    }
}
