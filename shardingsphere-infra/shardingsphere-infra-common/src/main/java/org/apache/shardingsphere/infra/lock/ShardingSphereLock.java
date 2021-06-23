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

package org.apache.shardingsphere.infra.lock;

/**
 * ShardingSphere lock.
 */
public interface ShardingSphereLock {
    
    /**
     * Try to lock.
     *
     * @param lockName lock name
     * @return true if get the lock, false if not
     */
    boolean tryLock(String lockName);
    
    /**
     * Try to lock with time out.
     * 
     * @param lockName lock name
     * @param timeout the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    boolean tryLock(String lockName, long timeout);
    
    /**
     * Release lock.
     * 
     * @param lockName lock name
     */
    void releaseLock(String lockName);
    
    /**
     * Check whether resource is locked in current instance.
     * 
     * @param lockName lock name
     * @return true if locked, false if not
     */
    boolean isLocked(String lockName);
    
    /**
     * Check whether resource is released in all instances.
     * 
     * @param lockName lock name
     * @return true if released, false if not
     */
    boolean isReleased(String lockName);
    
    /**
     * Get default lock time out milliseconds.
     * 
     * @return default lock time out milliseconds
     */
    long getDefaultTimeOut();
}
