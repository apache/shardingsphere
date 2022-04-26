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

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.util.LockNodeUtil;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Global lock node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockNode {
    
    private static final String LOCK_ROOT = "lock";
    
    private static final String LOCK_SCOPE_STANDARD = "standard";
    
    private static final String LOCK_SCOPE_GLOBAL = "global";
    
    private static final String LOCK_TOKEN = "token";
    
    private static final String LOCK_LEVEL_DATABASE = "database";
    
    private static final String LOCKS_NODE = "locks";
    
    private static final String LOCKED_ACK_NODE = "ack";
    
    /**
     * Get standard locks node path.
     *
     * @return standard locks node path
     */
    public static String getStandardLocksNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_STANDARD, LOCKS_NODE);
    }
    
    /**
     * Get global database locks node path.
     *
     * @return global database lock node path
     */
    public static String getGlobalDatabaseLocksNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_GLOBAL, LOCK_LEVEL_DATABASE, LOCKS_NODE);
    }
    
    /**
     * Get global database locked ack node path.
     *
     * @return global database locked ack node path
     */
    public static String getGlobalDatabaseLockedAckNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_GLOBAL, LOCK_LEVEL_DATABASE, LOCKED_ACK_NODE);
    }
    
    /**
     * Generate standard lock name.
     *
     * @param lockName lock name
     * @return standard lock name
     */
    public static String generateStandardLockName(final String lockName) {
        return getStandardLocksNodePath() + "/" + lockName;
    }
    
    /**
     * Generate global database locks name.
     *
     * @param database database
     * @return global database locks name
     */
    public static String generateGlobalDatabaseLocksName(final String database) {
        return getGlobalDatabaseLocksNodePath() + "/" + database;
    }
    
    /**
     * Generate global database ack lock name.
     *
     * @param database database
     * @param lockedInstanceId locked instance id
     * @return global database ack lock name
     */
    public static String generateGlobalDatabaseAckLockName(final String database, final String lockedInstanceId) {
        return getGlobalDatabaseLockedAckNodePath() + "/" + LockNodeUtil.generateDatabaseLockName(database, lockedInstanceId);
    }
    
    /**
     * Generate global database Lock released node path.
     *
     * @param database database
     * @return global database Lock released name
     */
    public static String generateGlobalDatabaseLockReleasedNodePath(final String database) {
        return getGlobalDatabaseLocksNodePath() + "/" + database + "/leases";
    }
    
    /**
     * Get lock token node path.
     *
     * @return lock token node path
     */
    public static String generateLockTokenNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_GLOBAL, LOCK_TOKEN);
    }
    
    /**
     * Parse global database Locks node path.
     *
     * @param nodePath locks node path
     * @return global database locked node path
     */
    public static Optional<String> parseGlobalDatabaseLocksNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getGlobalDatabaseLocksNodePath() + "/" + "(.+)/leases/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Parse global database locked ack node path.
     *
     * @param nodePath locked ack node path
     * @return global database locked ack node path
     */
    public static Optional<String> parseGlobalDatabaseLockedAckNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getGlobalDatabaseLockedAckNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
