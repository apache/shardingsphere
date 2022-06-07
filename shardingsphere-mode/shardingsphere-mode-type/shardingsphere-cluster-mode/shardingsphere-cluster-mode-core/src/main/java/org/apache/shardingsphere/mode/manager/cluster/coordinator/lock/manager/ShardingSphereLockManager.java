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

package org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.manager;

import org.apache.shardingsphere.infra.lock.LockMode;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.lock.mutex.ShardingSphereInterMutexLockHolder;
import org.apache.shardingsphere.spi.annotation.SingletonSPI;
import org.apache.shardingsphere.spi.type.required.RequiredSPI;

import java.util.Set;

/**
 * Lock manager of ShardingSphere.
 */
@SingletonSPI
public interface ShardingSphereLockManager extends RequiredSPI {
    
    /**
     * Init lock manager.
     *
     * @param lockHolder lock holder
     */
    void init(ShardingSphereInterMutexLockHolder lockHolder);
    
    /**
     * Get distributed lock.
     *
     * @return distributed lock
     */
    ShardingSphereLock getDistributedLock();
    
    /**
     * Try lock for database.
     *
     * @param databaseName database name
     * @param lockMode lock mode
     * @return is locked or not
     */
    boolean tryLock(String databaseName, LockMode lockMode);
    
    /**
     * Try lock write for database.
     *
     * @param databaseName database name
     * @param lockMode lock mode
     * @param timeoutMilliseconds timeout milliseconds
     * @return is locked or not
     */
    boolean tryLock(String databaseName, LockMode lockMode, long timeoutMilliseconds);
    
    /**
     * Try lock for schemas.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     * @param lockMode lock mode
     * @return is locked or not
     */
    default boolean tryLock(String databaseName, Set<String> schemaNames, LockMode lockMode) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Try lock for schemas.
     *
     * @param databaseName database name
     * @param schemaNames schema names
     * @param lockMode lock mode
     * @param timeoutMilliseconds timeout milliseconds
     * @return is locked or not
     */
    default boolean tryLock(String databaseName, Set<String> schemaNames, LockMode lockMode, long timeoutMilliseconds) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Release lock for database.
     *
     * @param databaseName database name
     */
    void releaseLock(String databaseName);
    
    /**
     * Release lock for schemas.
     *
     * @param databaseName database name
     * @param schemaName schema name
     */
    default void releaseLock(String databaseName, String schemaName) {
        throw new UnsupportedOperationException();
    }
    
    /**
     * Is locked database.
     *
     * @param databaseName database name
     * @return is locked or not
     */
    boolean isLocked(String databaseName);
    
    /**
     * Is locked schema.
     *
     * @param databaseName database name
     * @param schemaName schema name
     * @return is locked or not
     */
    default boolean isLocked(String databaseName, String schemaName) {
        throw new UnsupportedOperationException();
    }
}
