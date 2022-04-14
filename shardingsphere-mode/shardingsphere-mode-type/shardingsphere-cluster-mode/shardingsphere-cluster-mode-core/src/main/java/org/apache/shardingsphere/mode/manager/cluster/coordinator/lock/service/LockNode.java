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
    
    private static final String LOCK_LEVEL_SCHEMA = "schema";
    
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
     * Get global schema locks node path.
     *
     * @return global schema lock node path
     */
    public static String getGlobalSchemaLocksNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_GLOBAL, LOCK_LEVEL_SCHEMA, LOCKS_NODE);
    }
    
    /**
     * Get global schema locked ack node path.
     *
     * @return global schema locked ack node path
     */
    public static String getGlobalSchemaLockedAckNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE_GLOBAL, LOCK_LEVEL_SCHEMA, LOCKED_ACK_NODE);
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
     * Generate global schema locks name.
     *
     * @param schema schema
     * @param instanceId instance id
     * @return global schema locks name
     */
    public static String generateGlobalSchemaLocksName(final String schema, final String instanceId) {
        return getGlobalSchemaLocksNodePath() + "/" + LockNodeUtil.generateSchemaLockName(schema, instanceId);
    }
    
    /**
     * Generate global schema ack lock name.
     *
     * @param schema schema
     * @param lockedInstanceId locked instance id
     * @return global schema ack lock name
     */
    public static String generateGlobalSchemaAckLockName(final String schema, final String lockedInstanceId) {
        return getGlobalSchemaLockedAckNodePath() + "/" + LockNodeUtil.generateSchemaLockName(schema, lockedInstanceId);
    }
    
    /**
     * Generate global schema Lock released node path.
     *
     * @param schema schema
     * @param instanceId instance id
     * @return global schema Lock released name
     */
    public static String generateGlobalSchemaLockReleasedNodePath(final String schema, final String instanceId) {
        return getGlobalSchemaLocksNodePath() + "/" + LockNodeUtil.generateSchemaLockName(schema, instanceId) + "/leases";
    }
    
    /**
     * Parse global schema Locks node path.
     *
     * @param nodePath locks node path
     * @return global schema locked node path
     */
    public static Optional<String> parseGlobalSchemaLocksNodePath(final String nodePath) {
        // TODO "(.+)/leases/(.+)$"
        Pattern pattern = Pattern.compile(getGlobalSchemaLocksNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Parse global schema locked ack node path.
     *
     * @param nodePath locked ack node path
     * @return global schema locked ack node path
     */
    public static Optional<String> parseGlobalSchemaLockedAckNodePath(final String nodePath) {
        Pattern pattern = Pattern.compile(getGlobalSchemaLockedAckNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(nodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
