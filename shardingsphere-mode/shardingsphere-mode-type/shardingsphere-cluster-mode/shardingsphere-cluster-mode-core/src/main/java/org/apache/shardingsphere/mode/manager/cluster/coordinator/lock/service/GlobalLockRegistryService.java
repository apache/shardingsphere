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
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepositoryException;

import java.util.Collection;

/**
 * Global lock registry service.
 */
public final class GlobalLockRegistryService {
    
    private final ClusterPersistRepository repository;
    
    public GlobalLockRegistryService(final ClusterPersistRepository repository) {
        this.repository = repository;
    }
    
    /**
     * Init global lock root patch.
     */
    public void initGlobalLockRoot() {
        repository.persist(GlobalLockNode.getGlobalLocksNodePath(), "");
        repository.persist(GlobalLockNode.getGlobalAckNodePath(), "");
    }
    
    /**
     * Synchronize all global locks.
     *
     * @return all global locks
     */
    public Collection<String> synchronizeAllGlobalLock() {
        return repository.getChildrenKeys(GlobalLockNode.getGlobalLocksNodePath());
    }
    
    /**
     * Try to get lock.
     *
     * @param lockName lock name
     * @return true if get the lock, false if not
     */
    public boolean tryLock(final String lockName) {
        try {
            repository.persistEphemeral(lockName, LockState.LOCKED.name());
            return true;
        } catch (final ClusterPersistRepositoryException ignored) {
            return false;
        }
    }
    
    /**
     * Release lock.
     *
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        repository.delete(lockName);
    }
    
    /**
     * Ack lock.
     *
     * @param lockName lock name
     * @param lockValue lock value
     */
    public void ackLock(final String lockName, final String lockValue) {
        repository.persistEphemeral(lockName, lockValue);
    }
    
    /**
     * Release ack lock.
     *
     * @param lockName lock name
     */
    public void releaseAckLock(final String lockName) {
        repository.delete(lockName);
    }
}
