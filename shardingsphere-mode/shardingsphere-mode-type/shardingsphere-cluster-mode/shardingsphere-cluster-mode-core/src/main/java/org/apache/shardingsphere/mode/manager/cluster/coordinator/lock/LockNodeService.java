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

import org.apache.shardingsphere.infra.lock.LockType;

import java.util.Optional;

/**
 * Lock node service.
 */
public interface LockNodeService {
    
    String PATH_DELIMITER = "/";
    
    String LOCK_ROOT = "lock";
    
    String LOCKS_NODE = "locks";
    
    /**
     * Get sequence node path.
     *
     * @return sequence node path
     */
    String getSequenceNodePath();
    
    /**
     * Get global locks node path.
     *
     * @return global lock node path
     */
    String getGlobalLocksNodePath();
    
    /**
     * Get global locked ack node path.
     *
     * @return global locked ack node path
     */
    String getGlobalLockedAckNodePath();
    
    /**
     * Generate global locks name.
     *
     * @param locksName locks name
     * @return global locks name
     */
    String generateGlobalLocksName(String locksName);
    
    /**
     * Generate ack lock name.
     *
     * @param ackLockName ack lock name
     * @param lockedInstanceId locked instance id
     * @return global ack lock name
     */
    String generateGlobalAckLockName(String ackLockName, String lockedInstanceId);
    
    /**
     * Parse global Locks node path.
     *
     * @param nodePath locks node path
     * @return global locked node path
     */
    Optional<String> parseGlobalLocksNodePath(String nodePath);
    
    /**
     * Parse global locked ack node path.
     *
     * @param nodePath locked ack node path
     * @return global locked ack node path
     */
    Optional<String> parseGlobalLockedAckNodePath(String nodePath);
    
    /**
     * Get lock type.
     *
     * @return lock type
     */
    LockType getLockType();
}
