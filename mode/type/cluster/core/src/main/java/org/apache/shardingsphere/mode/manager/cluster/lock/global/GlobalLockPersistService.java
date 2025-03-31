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

package org.apache.shardingsphere.mode.manager.cluster.lock.global;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.repository.cluster.core.lock.DistributedLockHolder;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Global lock persist service.
 */
@RequiredArgsConstructor
public final class GlobalLockPersistService {
    
    private final ClusterPersistRepository repository;
    
    /**
     * Try lock.
     *
     * @param lockDefinition lock definition
     * @param timeoutMillis timeout millis
     * @return is locked or not
     */
    public boolean tryLock(final GlobalLockDefinition lockDefinition, final long timeoutMillis) {
        return DistributedLockHolder.getDistributedLock(lockDefinition.getLockKey(), repository).tryLock(timeoutMillis);
    }
    
    /**
     * Unlock.
     *
     * @param lockDefinition lock definition
     */
    public void unlock(final GlobalLockDefinition lockDefinition) {
        DistributedLockHolder.getDistributedLock(lockDefinition.getLockKey(), repository).unlock();
    }
}
