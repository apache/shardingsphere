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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.lock.LockContext;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.database.ShardingSphereDistributeDatabaseLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereDistributeMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;

/**
 * Distribute lock context.
 */
public final class DistributeLockContext implements LockContext {
    
    private final ClusterPersistRepository repository;
    
    private ShardingSphereDistributeMutexLock mutexLock;
    
    private ShardingSphereDistributeDatabaseLock databaseLock;
    
    public DistributeLockContext(final ClusterPersistRepository repository) {
        this.repository = repository;
    }
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        ComputeNodeInstance currentInstance = instanceContext.getInstance();
        Collection<ComputeNodeInstance> computeNodeInstances = instanceContext.getComputeNodeInstances();
        ShardingSphereInterMutexLockHolder lockHolder = new ShardingSphereInterMutexLockHolder(repository, currentInstance, computeNodeInstances);
        initMutexLock(lockHolder);
        initDatabaseLock(lockHolder);
    }
    
    private void initDatabaseLock(final ShardingSphereInterMutexLockHolder lockHolder) {
        databaseLock = new ShardingSphereDistributeDatabaseLock(lockHolder);
    }
    
    private void initMutexLock(final ShardingSphereInterMutexLockHolder lockHolder) {
        mutexLock = new ShardingSphereDistributeMutexLock(lockHolder);
    }
    
    @Override
    public synchronized boolean tryLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Try lock write database args database name can not be null.");
        return databaseLock.tryLock(databaseName);
    }
    
    @Override
    public void releaseLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Try lock write database args database name can not be null.");
        databaseLock.releaseLock(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        return databaseLock.isLocked(databaseName);
    }
    
    @Override
    public ShardingSphereLock getMutexLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get mutex lock args lock name can not be null.");
        return mutexLock;
    }
}
