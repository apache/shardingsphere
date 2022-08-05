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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.lock.manager.LockPersistService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

/**
 * Cluster lock persist service.
 */
@RequiredArgsConstructor
public final class ClusterLockPersistService implements LockPersistService {
    
    private final ClusterPersistRepository repository;
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        try {
            return repository.getInternalMutexLock(lockName).tryLock(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignore) {
            return false;
        }
    }
    
    @Override
    public void unLock(final String lockName) {
        repository.delete(LockNodeUtil.generateLockLeasesNodePath(lockName));
    }
    
    /**
     * Acquire ack locked instances.
     *
     * @param ackLockName ack lock name
     * @return ack locked instances
     * @deprecated remove me when the distributed lock refactoring was completed
     */
    @Deprecated
    public Collection<String> acquireAckLockedInstances(final String ackLockName) {
        return repository.getChildrenKeys(ackLockName);
    }
    
    /**
     * Release lock.
     *
     * @param lockName lock name
     * @deprecated remove me when the distributed lock refactoring was completed
     */
    @Deprecated
    public void releaseLock(final String lockName) {
        repository.getInternalMutexLock(lockName).unlock();
    }
    
    /**
     * Ack lock.
     *
     * @param lockName lock name
     * @param lockValue lock value
     * @deprecated remove me when the distributed lock refactoring was completed
     */
    @Deprecated
    public void ackLock(final String lockName, final String lockValue) {
        repository.persistEphemeral(lockName, lockValue);
    }
    
    /**
     * Release ack lock.
     *
     * @param lockName lock name
     * @deprecated remove me when the distributed lock refactoring was completed
     */
    @Deprecated
    public void releaseAckLock(final String lockName) {
        repository.delete(lockName);
    }
}
