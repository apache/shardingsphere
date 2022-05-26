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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.service.MutexLockRegistryService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Inter mutex lock holder of ShardingSphere.
 */
public final class ShardingSphereInterMutexLockHolder {
    
    private final Map<String, InterMutexLock> interMutexLocks = new LinkedHashMap<>();
    
    private final Map<String, InterReentrantMutexLock> interReentrantMutexLocks = new LinkedHashMap<>();
    
    private final ClusterPersistRepository repository;
    
    private final LockRegistryService mutexLockRegistryService;
    
    private final ComputeNodeInstance currentInstance;
    
    private final Collection<ComputeNodeInstance> computeNodeInstances;
    
    public ShardingSphereInterMutexLockHolder(final ClusterPersistRepository repository, final ComputeNodeInstance instance, final Collection<ComputeNodeInstance> nodeInstances) {
        this.repository = repository;
        mutexLockRegistryService = new MutexLockRegistryService(repository);
        currentInstance = instance;
        computeNodeInstances = nodeInstances;
    }
    
    /**
     * Get or create inter mutex Lock.
     *
     * @param locksName locks name
     * @return inter mutex lock
     */
    public synchronized InterMutexLock getOrCreateInterMutexLock(final String locksName) {
        InterMutexLock result = interMutexLocks.get(locksName);
        if (null == result) {
            result = createInterMutexLock(locksName);
            interMutexLocks.put(locksName, result);
        }
        return result;
    }
    
    private InterMutexLock createInterMutexLock(final String locksName) {
        InterReentrantMutexLock interReentrantMutexLock = getInterReentrantMutexLock(locksName + "/sequence");
        return new InterMutexLock(locksName, interReentrantMutexLock, mutexLockRegistryService, currentInstance, computeNodeInstances);
    }
    
    /**
     * Get inter mutex Lock.
     *
     * @param locksName locks name
     * @return inter mutex lock
     */
    public InterMutexLock getInterMutexLock(final String locksName) {
        return interMutexLocks.get(locksName);
    }
    
    /**
     * Get inter reentrant mutex lock.
     *
     * @param locksName locks name
     * @return inter reentrant mutex lock
     */
    public InterReentrantMutexLock getInterReentrantMutexLock(final String locksName) {
        InterReentrantMutexLock result = interReentrantMutexLocks.get(locksName);
        if (null == result) {
            result = new InterReentrantMutexLock(repository.getInternalReentrantMutexLock(locksName));
            interReentrantMutexLocks.put(locksName, result);
        }
        return result;
    }
    
    /**
     * Synchronize mutex lock.
     *
     * @param lockNodeService lock node service
     */
    public void synchronizeMutexLock(final LockNodeService lockNodeService) {
        Collection<String> allGlobalLock = repository.getChildrenKeys(lockNodeService.getLocksNodePath());
        if (allGlobalLock.isEmpty()) {
            if (LockNodeType.MUTEX == lockNodeService.getType()) {
                return;
            }
            repository.persist(lockNodeService.getLocksNodePath(), "");
            return;
        }
        for (String each : allGlobalLock) {
            Optional<String> generalLock = lockNodeService.parseLocksNodePath(each);
            generalLock.ifPresent(optional -> interMutexLocks.put(optional, createInterMutexLock(optional)));
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
