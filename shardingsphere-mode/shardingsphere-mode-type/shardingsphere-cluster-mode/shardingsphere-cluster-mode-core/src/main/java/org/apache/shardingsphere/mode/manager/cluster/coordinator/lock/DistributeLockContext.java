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
import org.apache.shardingsphere.infra.lock.LockType;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereDistributeMutexLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereMutexLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Distribute lock context.
 */
public final class DistributeLockContext implements LockContext {
    
    private final Map<LockType, ShardingSphereDistributeLockManager> lockManagers = new EnumMap<>(LockType.class);
    
    private final ClusterPersistRepository repository;
    
    private final ComputeNodeInstance currentInstance;
    
    private ShardingSphereDistributeMutexLock mutexLock;
    
    public DistributeLockContext(final ClusterPersistRepository repository, final ComputeNodeInstance currentInstance) {
        this.repository = repository;
        this.currentInstance = currentInstance;
        loadLockManager();
    }
    
    private void loadLockManager() {
        for (ShardingSphereDistributeLockManager each : ShardingSphereLockManagerFactory.getAllInstances()) {
            if (lockManagers.containsKey(each.getLockType())) {
                continue;
            }
            lockManagers.put(each.getLockType(), each);
        }
    }
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        Collection<ComputeNodeInstance> computeNodeInstances = instanceContext.getComputeNodeInstances();
        for (ShardingSphereDistributeLockManager each : lockManagers.values()) {
            each.initLocksState(repository, currentInstance, computeNodeInstances);
        }
        initMutexLock(computeNodeInstances);
    }
    
    private void initMutexLock(final Collection<ComputeNodeInstance> computeNodeInstances) {
        LockNodeService lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.MUTEX);
        ShardingSphereMutexLockHolder lockHolder = new ShardingSphereMutexLockHolder(repository, currentInstance, computeNodeInstances);
        mutexLock = new ShardingSphereDistributeMutexLock(lockNodeService, lockHolder);
    }
    
    @Override
    public synchronized boolean tryLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Try lock write database args database name can not be null.");
        return lockManagers.get(LockType.DATABASE).getOrCreateLock(databaseName).tryLock(databaseName);
    }
    
    @Override
    public void releaseLockWriteDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Try lock write database args database name can not be null.");
        lockManagers.get(LockType.DATABASE).getOrCreateLock(databaseName).releaseLock(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        return lockManagers.get(LockType.DATABASE).isLocked(databaseName);
    }
    
    @Override
    public synchronized ShardingSphereLock getMutexLock(final String lockName) {
        Preconditions.checkNotNull(lockName, "Get global lock args lock name can not be null.");
        return lockManagers.get(LockType.GENERAL).getOrCreateLock(lockName);
    }
}
