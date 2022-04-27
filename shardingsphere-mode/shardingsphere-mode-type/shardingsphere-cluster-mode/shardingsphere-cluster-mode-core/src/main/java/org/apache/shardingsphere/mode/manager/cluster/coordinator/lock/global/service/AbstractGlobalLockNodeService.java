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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.global.service;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.LockNodeService;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Abstract global lock node service.
 */
public abstract class AbstractGlobalLockNodeService implements LockNodeService {
    
    protected static final String LOCK_SCOPE_GLOBAL = "global";
    
    protected static final String LOCKED_ACK_NODE = "ack";
    
    @Override
    public String getGlobalLocksNodePath() {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + LOCK_SCOPE_GLOBAL + PATH_DELIMITER + getLockLevel() + PATH_DELIMITER + LOCKS_NODE;
    }
    
    @Override
    public String getGlobalLockedAckNodePath() {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + LOCK_SCOPE_GLOBAL + PATH_DELIMITER + getLockLevel() + PATH_DELIMITER + LOCKED_ACK_NODE;
    }
    
    @Override
    public String generateGlobalLocksName(final String locksName) {
        return getGlobalLocksNodePath() + "/" + locksName;
    }
    
    @Override
    public String generateGlobalAckLockName(final String ackLockName, final String lockedInstanceId) {
        return getGlobalLockedAckNodePath() + "/" + LockNodeUtil.generateAckLockedName(ackLockName, lockedInstanceId);
    }
    
    @Override
    public Optional<String> parseGlobalLocksNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getGlobalLocksNodePath() + "/" + "(.+)/leases/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    @Override
    public Optional<String> parseGlobalLockedAckNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getGlobalLockedAckNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    protected abstract String getLockLevel();
}
