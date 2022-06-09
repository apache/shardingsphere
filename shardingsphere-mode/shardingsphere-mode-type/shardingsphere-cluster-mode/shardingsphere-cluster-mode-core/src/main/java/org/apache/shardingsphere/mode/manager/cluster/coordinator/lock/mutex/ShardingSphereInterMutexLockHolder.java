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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex;

import org.apache.shardingsphere.infra.instance.ComputeNodeInstance;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.MutexLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Inter mutex lock holder of ShardingSphere.
 */
public final class ShardingSphereInterMutexLockHolder {
    
    private final Map<String, MutexLock> mutexLocks = new LinkedHashMap<>();
    
    private final ClusterPersistRepository clusterRepository;
    
    private final ComputeNodeInstance currentInstance;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances;
    
    private final LockRegistryService mutexLockRegistryService;
    
    public ShardingSphereInterMutexLockHolder(final ClusterPersistRepository repository, final ComputeNodeInstance instance, final Collection<ComputeNodeInstance> nodeInstances) {
        clusterRepository = repository;
        currentInstance = instance;
        computeNodeInstances = nodeInstances;
        mutexLockRegistryService = new MutexLockRegistryService(clusterRepository);
    }
    
    /**
     * Get or create inter mutex Lock.
     *
     * @param locksName locks name
     * @return inter mutex lock
     */
    public synchronized InterMutexLock getOrCreateInterMutexLock(final String locksName) {
        MutexLock result = mutexLocks.get(locksName);
        if (null == result) {
            result = createInterMutexLock(locksName);
            mutexLocks.put(locksName, result);
        }
        return (InterMutexLock) result;
    }
    
    private InterMutexLock createInterMutexLock(final String locksName) {
        InterReentrantMutexLock interReentrantMutexLock = createInterReentrantMutexLock(LockNodeUtil.generateLockSequenceNodePath(locksName));
        return new InterMutexLock(locksName, interReentrantMutexLock, mutexLockRegistryService, currentInstance, computeNodeInstances);
    }
    
    private InterReentrantMutexLock createInterReentrantMutexLock(final String lockNodePath) {
        return new InterReentrantMutexLock(clusterRepository.getInternalReentrantMutexLock(lockNodePath));
    }
    
    /**
     * Get inter mutex Lock.
     *
     * @param locksName locks name
     * @return inter mutex lock
     */
    public Optional<InterMutexLock> getInterMutexLock(final String locksName) {
        if (mutexLocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable((InterMutexLock) mutexLocks.get(locksName));
    }
    
    /**
     * Get or create inter reentrant mutex lock.
     *
     * @param locksName locks name
     * @return inter reentrant mutex lock
     */
    public synchronized InterReentrantMutexLock getOrCreateInterReentrantMutexLock(final String locksName) {
        MutexLock result = mutexLocks.get(locksName);
        if (null == result) {
            result = createInterReentrantMutexLock(locksName);
            mutexLocks.put(locksName, result);
        }
        return (InterReentrantMutexLock) result;
    }
    
    /**
     * Get inter reentrant mutex Lock.
     *
     * @param locksName locks name
     * @return inter mutex lock
     */
    public Optional<InterReentrantMutexLock> getInterReentrantMutexLock(final String locksName) {
        if (mutexLocks.isEmpty()) {
            return Optional.empty();
        }
        return Optional.ofNullable((InterReentrantMutexLock) mutexLocks.get(locksName));
    }
    
    /**
     * Synchronize lock.
     *
     * @param lockNodeService lock node service
     */
    public void synchronizeLock(final LockNodeService lockNodeService) {
        Collection<String> allGlobalLock = clusterRepository.getChildrenKeys(lockNodeService.getLocksNodePath());
        if (allGlobalLock.isEmpty()) {
            if (LockNodeType.DISTRIBUTED == lockNodeService.getType()) {
                return;
            }
            clusterRepository.persist(lockNodeService.getLocksNodePath(), "");
            return;
        }
        for (String each : allGlobalLock) {
            Optional<String> generalLock = lockNodeService.parseLocksNodePath(each);
            generalLock.ifPresent(optional -> mutexLocks.put(optional, createInterMutexLock(optional)));
        }
    }
    
    /**
     * Get current instance id.
     *
     * @return current instance id
     */
    public String getCurrentInstanceId() {
        return currentInstance.getCurrentInstanceId();
    }
}
