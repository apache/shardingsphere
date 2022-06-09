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

import java.util.Collection;

/**
 * Lock registry service.
 */
public interface LockRegistryService {
    
    /**
     * Acquire ack locked instances.
     *
     * @param lockName lock name
     * @return ack locked instances
     */
    Collection<String> acquireAckLockedInstances(String lockName);
    
    /**
     * Try lock.
     *
     * @param lockName lock name
     * @param timeoutMilliseconds the maximum time in milliseconds to acquire lock
     * @return is locked or not
     */
    boolean tryLock(String lockName, long timeoutMilliseconds);
    
    /**
     * Release lock.
     *
     * @param lockName lock name
     */
    void releaseLock(String lockName);
    
    /**
     * Remove lock.
     *
     * @param lockName lock name
     */
    void removeLock(String lockName);
    
    /**
     * Ack lock.
     *
     * @param lockName lock name
     * @param lockValue lock value
     */
    void ackLock(String lockName, String lockValue);
    
    /**
     * Release ack lock.
     *
     * @param lockName lock name
     */
    void releaseAckLock(String lockName);
}
