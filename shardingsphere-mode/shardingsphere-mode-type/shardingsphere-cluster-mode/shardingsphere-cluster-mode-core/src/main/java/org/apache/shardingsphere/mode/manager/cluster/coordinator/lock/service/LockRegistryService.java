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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service;

import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.concurrent.TimeUnit;

/**
 * Lock registry service.
 */
public final class LockRegistryService {
    
    private final ClusterPersistRepository repository;
    
    public LockRegistryService(final ClusterPersistRepository repository) {
        this.repository = repository;
        initLockNode();
    }
    
    private void initLockNode() {
        repository.persist(LockNode.getLockRootNodePath(), "");
    }
    
    /**
     * Try to get lock.
     *
     * @param lockName lock name
     * @param timeoutMilliseconds the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        return repository.tryLock(LockNode.getLockNodePath(lockName), timeoutMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Release lock.
     * 
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        repository.releaseLock(LockNode.getLockNodePath(lockName));
    }
}
