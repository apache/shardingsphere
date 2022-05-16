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

/**
 * Lock node service.
 */
public interface LockNodeService {
    
    String PATH_DELIMITER = "/";
    
    String LOCK_ROOT = "lock";
    
    String LOCKS_NODE = "locks";
    
    String LOCKED_ACK_NODE = "ack";
    
    /**
     * Get sequence node path.
     *
     * @return sequence node path
     */
    String getSequenceNodePath();
    
    /**
     * Get locks node path.
     *
     * @return locks node path
     */
    String getLocksNodePath();
    
    /**
     * Generate locks name.
     *
     * @param locksName locks name
     * @return locks name
     */
    String generateLocksName(String locksName);
    
    /**
     * Generate ack lock name.
     *
     * @param lockName lock name
     * @param lockedInstanceId locked instance id
     * @return ack lock name
     */
    String generateAckLockName(String lockName, String lockedInstanceId);
    
    /**
     * Parse Locks node path.
     *
     * @param nodePath locks node path
     * @return locked node path
     */
    Optional<String> parseLocksNodePath(String nodePath);
    
    /**
     * Parse locks ack node path.
     *
     * @param nodePath node path
     * @return locks ack node path
     */
    Optional<String> parseLocksAckNodePath(String nodePath);
    
    /**
     * Get type.
     *
     * @return lock node type
     */
    LockNodeType getType();
}
