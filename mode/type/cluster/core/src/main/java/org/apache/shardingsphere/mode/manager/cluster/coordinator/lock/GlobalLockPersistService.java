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
import org.apache.shardingsphere.mode.lock.GlobalLockDefinition;
import org.apache.shardingsphere.mode.lock.LockPersistService;
import org.apache.shardingsphere.mode.repository.cluster.lock.holder.DistributedLockHolder;

/**
 * Global lock persist service.
 */
@RequiredArgsConstructor
public final class GlobalLockPersistService implements LockPersistService<GlobalLockDefinition> {
    
    private final DistributedLockHolder distributedLockHolder;
    
    @Override
    public boolean tryLock(final GlobalLockDefinition lockDefinition, final long timeoutMillis) {
        return distributedLockHolder.getDistributedLock(lockDefinition.getLockKey()).tryLock(timeoutMillis);
    }
    
    @Override
    public void unlock(final GlobalLockDefinition lockDefinition) {
        distributedLockHolder.getDistributedLock(lockDefinition.getLockKey()).unlock();
    }
}
