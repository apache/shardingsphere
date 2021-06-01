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

package org.apache.shardingsphere.governance.core.lock.service;

import com.google.common.base.Joiner;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Lock node.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class LockNode {
    
    private static final String LOCK_NODE_ROOT = "lock";
    
    private static final String LOCKS_NODE = "locks";
    
    private static final String LOCKED_ACK_NODE = "ack";
    
    /**
     * Get lock root node path.
     * 
     * @return lock root node path
     */
    public static String getLockRootNodePath() {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, LOCKS_NODE);
    }
    
    /**
     * Get lock node path.
     * 
     * @param lockName lock name
     * @return lock node path
     */
    public static String getLockNodePath(final String lockName) {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, LOCKS_NODE, lockName);
    }
    
    /**
     * Get locked ack root node path.
     * 
     * @return locked ack root node path
     */
    public static String getLockedAckRootNodePah() {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, LOCKED_ACK_NODE);
    }
    
    /**
     * Get locked ack node path.
     * 
     * @param ackLockName ack lock name
     * @return locked ack node path
     */
    public static String getLockedAckNodePath(final String ackLockName) {
        return Joiner.on("/").join("", LOCK_NODE_ROOT, LOCKED_ACK_NODE, ackLockName);
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
}
