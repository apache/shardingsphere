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
    
    private static final String LOCK_SCOPE = "global";
    
    private static final String LOCKS_NODE = "locks";
    
    private static final String LOCKED_ACK_NODE = "ack";
    
    /**
     * Get lock root node path.
     *
     * @return lock root node path
     */
    public static String getLockRootNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCKS_NODE);
    }
    
    /**
     * Get lock node path.
     *
     * @param lockName lock name
     * @return lock node path
     */
    public static String getLockNodePath(final String lockName) {
        return Joiner.on("/").join("", LOCK_ROOT, LOCKS_NODE, lockName);
    }
    
    /**
     * Get locks node path.
     *
     * @return locks node path
     */
    public static String getGlobalLocksNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE, LOCKS_NODE);
    }
    
    /**
     * Get ack node path.
     *
     * @return ack node path
     */
    public static String getGlobalAckNodePath() {
        return Joiner.on("/").join("", LOCK_ROOT, LOCK_SCOPE, LOCKED_ACK_NODE);
    }
    
    /**
     * Generate schema lock name.
     *
     * @param schema schema
     * @param instanceId instance id
     * @return schema lock name
     */
    public static String generateSchemaLockName(final String schema, final String instanceId) {
        return getGlobalLocksNodePath() + "/" + LockNodeUtil.generateSchemaLockName(schema, instanceId);
    }
    
    /**
     * Generate schema ack lock name.
     *
     * @param schema schema
     * @param lockedInstanceId locked instance id
     * @return schema ack lock name
     */
    public static String generateSchemaAckLockName(final String schema, final String lockedInstanceId) {
        return getGlobalAckNodePath() + "/" + LockNodeUtil.generateSchemaLockName(schema, lockedInstanceId);
    }
    
    /**
     * Get lock name by lock node path.
     *
     * @param lockNodePath lock node path
     * @return lock name
     */
    public static Optional<String> getLockName(final String lockNodePath) {
        Pattern pattern = Pattern.compile(getLockRootNodePath() + "/" + "(.+)/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(lockNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get locked key name by locks node path.
     *
     * @param locksNodePath locks node path
     * @return schema name
     */
    public static Optional<String> getLockedName(final String locksNodePath) {
        Pattern pattern = Pattern.compile(getGlobalLocksNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(locksNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
    
    /**
     * Get ack locked key name by ack node path.
     *
     * @param ackNodePath ack node path
     * @return locked instance id
     */
    public static Optional<String> getAckLockedName(final String ackNodePath) {
        Pattern pattern = Pattern.compile(getGlobalAckNodePath() + "/(.+)$", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(ackNodePath);
        return matcher.find() ? Optional.of(matcher.group(1)) : Optional.empty();
    }
}
