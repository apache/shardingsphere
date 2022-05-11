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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.InterMutexReentrantLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeServiceFactory;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

/**
 * Standard distribute lock of ShardingSphere.
 */
@RequiredArgsConstructor
public final class ShardingSphereStandardLock implements ShardingSphereLock {
    
    private final LockNodeService lockNodeService = LockNodeServiceFactory.getInstance().getLockNodeService(LockNodeType.STANDARD);
    
    private final InterMutexReentrantLock interLock;
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return interLock.tryLock(lockNodeService.generateLocksName(lockName), TimeoutMilliseconds.MAX_TRY_LOCK);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        interLock.releaseLock(lockNodeService.generateLocksName(lockName));
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        throw new UnsupportedOperationException();
    }
}
