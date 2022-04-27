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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.TimeoutMilliseconds;

/**
 * Sharding sphere sequenced semaphore lock.
 */
@RequiredArgsConstructor
public final class ShardingSphereSequencedSemaphoreLock implements ShardingSphereLock {
    
    private final LockRegistryService lockService;
    
    private final LockNodeService lockNode;
    
    @Override
    public boolean tryLock(final String lockName) {
        return tryLock(lockName, TimeoutMilliseconds.DEFAULT_REGISTRY);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMillis) {
        return lockService.tryLock(lockNode.getSequenceNodePath(), timeoutMillis);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        lockService.releaseLock(lockNode.getSequenceNodePath());
    }
    
    @Override
    public boolean isLocked(final String lockName) {
        return false;
    }
}
