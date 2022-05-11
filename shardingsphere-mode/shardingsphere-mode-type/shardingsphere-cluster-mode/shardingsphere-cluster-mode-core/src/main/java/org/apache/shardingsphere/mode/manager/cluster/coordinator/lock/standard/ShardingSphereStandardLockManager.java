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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.standard;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.infra.lock.LockType;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.ShardingSphereLockManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.standard.service.StandardLockRegistryService;
import org.apache.shardingsphere.mode.persist.PersistRepository;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class ShardingSphereStandardLockManager implements ShardingSphereLockManager {
    
    private final Map<String, ShardingSphereStandardLock> locks;
    
    private final LockNodeService lockNodeService;
    
    private ClusterPersistRepository clusterRepository;
    
    public ShardingSphereStandardLockManager() {
        locks = new ConcurrentHashMap<>();
        lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(getLockType());
    }
    
    @Override
    public void initLocksState(final PersistRepository repository, final ComputeNodeInstance instance, final Collection<ComputeNodeInstance> computeNodeInstances) {
        clusterRepository = (ClusterPersistRepository) repository;
    }
    
    private ShardingSphereStandardLock createGeneralLock() {
        return new ShardingSphereStandardLock(new StandardLockRegistryService(clusterRepository), lockNodeService);
    }
    
    @Override
    public ShardingSphereLock getOrCreateLock(final String lockName) {
        ShardingSphereStandardLock result = locks.get(lockName);
        if (null != result) {
            return result;
        }
        result = createGeneralLock();
        locks.put(lockName, result);
        return result;
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        if (locks.isEmpty()) {
            return false;
        }
        ShardingSphereLock lock = locks.get(lockName);
        if (null != lock) {
            return lock.isLocked();
        }
        return false;
    }
    
    @Override
    public LockType getLockType() {
        return LockType.STANDARD;
    }
}
