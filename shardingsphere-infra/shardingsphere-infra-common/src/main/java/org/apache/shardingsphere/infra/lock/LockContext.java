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

import org.apache.shardingsphere.infra.instance.InstanceContext;

/**
 * Lock context.
 */
public interface LockContext {
    
    /**
     * Init lock state.
     *
     * @param instanceContext instance context
     */
    void initLockState(InstanceContext instanceContext);
    
    /**
     * Try lock write database.
     *
     * @param databaseName database name
     * @return is write locked or not
     */
    boolean tryLockWriteDatabase(String databaseName);
    
    /**
     * Release lock write of database.
     *
     * @param databaseName database name
     */
    void releaseLockWriteDatabase(String databaseName);
    
    /**
     *  Is locked database.
     *
     * @param databaseName database name
     * @return is locked database or not
     */
    boolean isLockedDatabase(String databaseName);
    
    /**
     * Get or create global lock.
     *
     * @param lockName lock name
     * @return global lock
     */
    ShardingSphereLock getGlobalLock(String lockName);
    
    /**
     * Get or create standard lock.
     *
     * @param lockName lock name
     * @return standard lock
     */
    ShardingSphereLock getStandardLock(String lockName);
}
