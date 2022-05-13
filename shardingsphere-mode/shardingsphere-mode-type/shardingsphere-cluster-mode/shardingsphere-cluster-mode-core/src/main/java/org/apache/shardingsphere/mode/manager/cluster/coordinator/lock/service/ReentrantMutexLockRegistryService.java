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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.Collection;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

/**
 * Reentrant mutex lock registry service.
 */
@RequiredArgsConstructor
public final class ReentrantMutexLockRegistryService implements LockRegistryService {
    
    private final ClusterPersistRepository repository;
    
    @Override
    public Collection<String> acquireAckLockedInstances(final String lockName) {
        return new LinkedList<>();
    }
    
    @Override
    public Lock getInternalLock(final String lockName) {
        return repository.getInternalReentrantMutexLock(lockName);
    }
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        try {
            return repository.getInternalReentrantMutexLock(lockName).tryLock(timeoutMilliseconds, TimeUnit.MILLISECONDS);
        } catch (final InterruptedException ignore) {
            return false;
        }
    }
    
    @Override
    public void releaseLock(final String lockName) {
        repository.getInternalReentrantMutexLock(lockName).unlock();
    }
    
    @Override
    public void removeLock(final String lockName) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void ackLock(final String lockName, final String lockValue) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void releaseAckLock(final String lockName) {
        throw new UnsupportedOperationException();
    }
}
