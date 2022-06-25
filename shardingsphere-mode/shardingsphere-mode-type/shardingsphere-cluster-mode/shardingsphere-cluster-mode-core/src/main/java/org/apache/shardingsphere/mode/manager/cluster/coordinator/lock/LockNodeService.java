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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock;

import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeType;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lock node service.
 */
public interface LockNodeService {
    
    String PATH_DELIMITER = "/";
    
    String LOCK_ROOT = "lock";
    
    String LOCKS_NODE = "locks";
    
    String LOCKED_ACK_NODE = "ack";
    
    /**
     * Get locks node path.
     *
     * @return locks node path
     */
    default String getLocksNodePath() {
        return PATH_DELIMITER + LOCK_ROOT + PATH_DELIMITER + getLockTypeName() + PATH_DELIMITER + LOCKS_NODE;
    }
    
    /**
     * Generate locks name.
     *
     * @param locksName locks name
     * @return locks name
     */
    default String generateLocksName(String locksName) {
        return getLocksNodePath() + "/" + locksName;
    }
    
    /**
     * Generate freeze lock name.
     *
     * @param lockName lock name
     * @return freeze lock name
     */
    default String generateFreezeLockName(String lockName) {
        return getLocksNodePath() + "/" + lockName + "/freeze";
    }
    
    /**
     * Generate ack lock name.
     *
     * @param lockName lock name
     * @param lockedInstanceId locked instance id
     * @return ack lock name
     */
    default String generateAckLockName(String lockName, String lockedInstanceId) {
        return getLocksNodePath() + "/" + lockName + "/" + LOCKED_ACK_NODE + "/" + lockedInstanceId;
    }
    
    /**
     * Parse Locks node path.
     *
     * @param nodePath locks node path
     * @return locked node path
     */
    default Optional<String> parseLocksNodePath(String nodePath) {
        Pattern pattern = Pattern.compile(getLocksNodePath() + "/" + "(.+)/leases/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Parse locks ack node path.
     *
     * @param nodePath node path
     * @return locks ack node path
     */
    default Optional<String> parseLocksAckNodePath(String nodePath) {
        Pattern pattern = Pattern.compile(getLocksNodePath() + "/" + "(.+)/ack/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1) + "#@#" + matcher.group(2)) : Optional.empty();
    }
    
    /**
     * Get type.
     *
     * @return lock node type
     */
    LockNodeType getType();
    
    /**
     * Get lock type name.
     *
     * @return type name
     */
    String getLockTypeName();
}
