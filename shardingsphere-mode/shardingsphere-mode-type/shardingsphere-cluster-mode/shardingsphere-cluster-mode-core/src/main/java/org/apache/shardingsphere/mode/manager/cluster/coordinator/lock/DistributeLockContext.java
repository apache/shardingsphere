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
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.type.required.RequiredSPIRegistry;

import java.util.Set;

/**
 * Distribute lock context.
 */
@RequiredArgsConstructor
public final class DistributeLockContext implements LockContext {
    
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
    public ShardingSphereLock getMutexLock() {
        return lockManager.getMutexLock();
    }
    
    @Override
    public boolean lockWrite(final String databaseName) {
        return lockManager.lockWrite(databaseName);
    }
    
    @Override
    public boolean lockWrite(final String databaseName, final Set<String> schemaNames) {
        return lockManager.lockWrite(databaseName, schemaNames);
    }
    
    @Override
    public boolean tryLockWrite(final String databaseName, final long timeoutMilliseconds) {
        return lockManager.tryLockWrite(databaseName, timeoutMilliseconds);
    }
    
    @Override
    public boolean tryLockWrite(final String databaseName, final Set<String> schemaNames, final long timeoutMilliseconds) {
        return lockManager.tryLockWrite(databaseName, schemaNames, timeoutMilliseconds);
    }
    
    @Override
    public void releaseLockWrite(final String databaseName) {
        lockManager.releaseLockWrite(databaseName);
    }
    
    @Override
    public void releaseLockWrite(final String databaseName, final String schemaName) {
        lockManager.releaseLockWrite(databaseName, schemaName);
    }
    
    @Override
    public boolean isLocked(final String databaseName) {
        return lockManager.isLocked(databaseName);
    }
    
    @Override
    public boolean isLocked(final String databaseName, final String schema) {
        return lockManager.isLocked(databaseName, schema);
    }
}
