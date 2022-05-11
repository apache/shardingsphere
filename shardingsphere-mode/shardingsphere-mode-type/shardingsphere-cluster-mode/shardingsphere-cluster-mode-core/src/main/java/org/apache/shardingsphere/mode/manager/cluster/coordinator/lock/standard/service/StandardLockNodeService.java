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

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;

import java.util.Optional;

public final class StandardLockNodeService implements LockNodeService {
    
    private static final String LOCK_SCOPE_STANDARD = "standard";
    
    @Override
    public String getSequenceNodePath() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String getLocksNodePath() {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + LOCK_SCOPE_STANDARD + PATH_DELIMITER + LOCKS_NODE;
    }
    
    @Override
    public String generateLocksName(final String locksName) {
        return getLocksNodePath() + "/" + locksName;
    }
    
    @Override
    public String getLockedAckNodePath() {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public String generateAckLockName(final String ackLockName, final String lockedInstanceId) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Optional<String> parseLocksNodePath(final String nodePath) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public Optional<String> parseLockedAckNodePath(final String nodePath) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public LockNodeType getType() {
        return LockNodeType.STANDARD;
    }
}
