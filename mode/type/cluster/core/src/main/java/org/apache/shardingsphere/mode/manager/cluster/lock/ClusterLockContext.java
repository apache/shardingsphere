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

package org.apache.shardingsphere.mode.manager.cluster.lock;

import org.apache.shardingsphere.mode.lock.LockContext;
import org.apache.shardingsphere.mode.lock.LockDefinition;
import org.apache.shardingsphere.mode.manager.cluster.lock.global.GlobalLockDefinition;
import org.apache.shardingsphere.mode.manager.cluster.lock.global.GlobalLockPersistService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

/**
 * Cluster lock context.
 */
public final class ClusterLockContext implements LockContext {
    
    private final GlobalLockPersistService globalLockPersistService;
    
    public ClusterLockContext(final ClusterPersistRepository repository) {
        globalLockPersistService = new GlobalLockPersistService(repository);
    }
    
    @Override
    public boolean tryLock(final LockDefinition lockDefinition, final long timeoutMillis) {
        return globalLockPersistService.tryLock((GlobalLockDefinition) lockDefinition, timeoutMillis);
    }
    
    @Override
    public void unlock(final LockDefinition lockDefinition) {
        globalLockPersistService.unlock((GlobalLockDefinition) lockDefinition);
    }
}
