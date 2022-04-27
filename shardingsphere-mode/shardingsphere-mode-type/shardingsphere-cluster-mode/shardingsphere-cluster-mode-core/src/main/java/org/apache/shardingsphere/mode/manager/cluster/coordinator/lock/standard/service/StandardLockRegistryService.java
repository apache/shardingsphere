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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.standard.service;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockRegistryService;
import org.apache.shardingsphere.mode.repository.cluster.ClusterPersistRepository;

import java.util.concurrent.TimeUnit;

/**
 * Standard lock registry service.
 */
@RequiredArgsConstructor
public final class StandardLockRegistryService implements LockRegistryService {
    
    private static final String PATH_DELIMITER = "/";
    
    private static final String LOCK_ROOT = "lock";
    
    private static final String LOCKS_NODE = "locks";
    
    private static final String LOCK_SCOPE_STANDARD = "standard";
    
    private final ClusterPersistRepository repository;
    
    @Override
    public boolean tryLock(final String lockName, final long timeoutMilliseconds) {
        return repository.tryLock(generateStandardLockName(lockName), timeoutMilliseconds, TimeUnit.MILLISECONDS);
    }
    
    @Override
    public void releaseLock(final String lockName) {
        repository.releaseLock(generateStandardLockName(lockName));
    }
    
    private String generateStandardLockName(final String lockName) {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + LOCK_SCOPE_STANDARD + PATH_DELIMITER + LOCKS_NODE + PATH_DELIMITER + lockName;
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
