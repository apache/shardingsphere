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

import java.util.Optional;

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
     * Get or create schema lock.
     *
     * @param schemaName schema name
     * @return schema lock
     */
    ShardingSphereLock getOrCreateSchemaLock(String schemaName);
    
    /**
     * Get schema lock.
     *
     * @param schemaName schema name
     * @return schema lock
     */
    Optional<ShardingSphereLock> getSchemaLock(String schemaName);
    
    /**
     *  Is locked schema.
     *
     * @param schemaName schema name
     * @return is locked schema or not
     */
    boolean isLockedSchema(String schemaName);
}
