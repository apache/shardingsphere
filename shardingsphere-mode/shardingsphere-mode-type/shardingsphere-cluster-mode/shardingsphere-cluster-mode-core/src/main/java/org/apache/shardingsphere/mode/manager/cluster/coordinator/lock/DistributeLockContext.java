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
import org.apache.shardingsphere.mode.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;

import java.util.Collection;
import java.util.EnumMap;
import java.util.Map;

/**
 * Distribute lock context.
 */
public final class DistributeLockContext implements LockContext {
    
    static {
        ShardingSphereServiceLoader.register(ShardingSphereLockManager.class);
    }
    
    private final Map<LockType, ShardingSphereLockManager> lockManagers = new EnumMap<>(LockType.class);
    
    private final ClusterPersistRepository repository;
    
    private final ComputeNodeInstance currentInstance;
    
    public DistributeLockContext(final ClusterPersistRepository repository, final ComputeNodeInstance currentInstance) {
        this.repository = repository;
        this.currentInstance = currentInstance;
        loadLockManager();
    }
    
    private void loadLockManager() {
        for (ShardingSphereLockManager each : ShardingSphereServiceLoader.getServiceInstances(ShardingSphereLockManager.class)) {
            if (lockManagers.containsKey(each.getLockType())) {
                continue;
            }
            lockManagers.put(each.getLockType(), each);
        }
    }
    
    @Override
    public void initLockState(final InstanceContext instanceContext) {
        Collection<ComputeNodeInstance> computeNodeInstances = instanceContext.getComputeNodeInstances();
        for (ShardingSphereLockManager each : lockManagers.values()) {
            each.initLocksState(repository, currentInstance, computeNodeInstances);
        }
    }
    
    @Override
    public synchronized ShardingSphereLock getOrCreateDatabaseLock(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Get or create database lock args database name can not be null.");
        return lockManagers.get(LockType.DATABASE).getOrCreateLock(databaseName);
    }
    
    @Override
    public ShardingSphereLock getDatabaseLock(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Get database lock args database name can not be null.");
        return lockManagers.get(LockType.DATABASE).getLock(databaseName);
    }
    
    @Override
    public boolean isLockedDatabase(final String databaseName) {
        Preconditions.checkNotNull(databaseName, "Is locked database args database name can not be null.");
        return lockManagers.get(LockType.DATABASE).isLocked(databaseName);
    }
}
