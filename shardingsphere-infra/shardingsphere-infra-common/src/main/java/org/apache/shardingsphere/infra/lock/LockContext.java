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

import java.util.Set;

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
     * Get or create mutex lock.
     *
     * @return mutex lock
     */
    ShardingSphereLock getMutexLock();
    
    /**
     * Lock write for database.
     *
     * @param databaseName database name
     * @return is locked or not
     */
    boolean lockWrite(String databaseName);
    
    /**
     * Lock write for schemas.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     * @return is locked or not
     */
    boolean lockWrite(String databaseName, Set<String> schemaNames);
    
    /**
     * Try Lock write for database.
     *
     * @param databaseName database name
     * @param timeoutMilliseconds timeout milliseconds
     * @return is locked or not
     */
    boolean tryLockWrite(String databaseName, long timeoutMilliseconds);
    
    /**
     * Try lock write for schemas.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     * @param timeoutMilliseconds timeout milliseconds
     * @return is locked or not
     */
    boolean tryLockWrite(String databaseName, Set<String> schemaNames, long timeoutMilliseconds);
    
    /**
     * Release lock write of database.
     *
     * @param databaseName database name
     */
    void releaseLockWrite(String databaseName);
    
    /**
     * Release lock write for schemas.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    void releaseLockWrite(String databaseName, String schemaName);
    
    /**
     *  Is locked database.
     *
     * @param databaseName database name
     * @return is locked database or not
     */
    boolean isLocked(String databaseName);
    
    /**
     * Is locked schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return is locked or not
     */
    boolean isLocked(String databaseName, String schemaName);
}
