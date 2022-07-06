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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.distributed;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager.internal.ShardingSphereInternalLockHolder;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

/**
 * Distribute standard lock of ShardingSphere.
 */
@RequiredArgsConstructor
public final class ShardingSphereDistributedStandardLock implements ShardingSphereLock {
    
    private final LockNodeService lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.DISTRIBUTED);
    
    private final ShardingSphereInternalLockHolder lockHolder;
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
        
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return lockHolder.getOrCreateInterReentrantMutexLock(lockNodeService.generateLocksName(lockName)).tryLock(timeoutMillis);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        lockHolder.getOrCreateInterReentrantMutexLock(lockNodeService.generateLocksName(lockName)).unlock();
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        throw new UnsupportedOperationException();
    }
}
