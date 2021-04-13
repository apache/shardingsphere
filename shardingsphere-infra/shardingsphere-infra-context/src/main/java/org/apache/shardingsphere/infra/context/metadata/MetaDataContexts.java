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

package org.apache.shardingsphere.infra.context.metadata;

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.lock.ShardingSphereLock;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.optimize.context.CalciteContextFactory;
import org.apache.shardingsphere.infra.state.StateContext;

import java.io.Closeable;
import java.util.Collection;
import java.util.Map;
import java.util.Optional;

/**
 * Meta data contexts.
 */
public interface MetaDataContexts extends Closeable {
    
    /**
     * Get all schema names.
     * 
     * @return all schema names
     */
    Collection<String> getAllSchemaNames();
    
    /**
     * Get mata data map.
     *
     * @return mata data map
     */
    Map<String, ShardingSphereMetaData> getMetaDataMap();
    
    /**
     * Get mata data.
     *
     * @param schemaName schema name
     * @return mata data
     */
    ShardingSphereMetaData getMetaData(String schemaName);
    
    /**
     * Get default mata data.
     *
     * @return default mata data
     */
    ShardingSphereMetaData getDefaultMetaData();
    
    /**
     * Get global rule meta data.
     * 
     * @return global rule meta data
     */
    ShardingSphereRuleMetaData getGlobalRuleMetaData();
    
    /**
     * Get executor engine.
     * 
     * @return executor engine
     */
    ExecutorEngine getExecutorEngine();
    
    /**
     * Get calcite context factory.
     *
     * @return calcite context factory
     */
    CalciteContextFactory getCalciteContextFactory();
    
    /**
     * Get users.
     * 
     * @return users
     */
    ShardingSphereUsers getUsers();
    
    /**
     * Get configuration properties.
     *
     * @return configuration properties
     */
    ConfigurationProperties getProps();
    
    /**
     * Get lock.
     * 
     * @return lock
     */
    Optional<ShardingSphereLock> getLock();
    
    /**
     * Get state context.
     * 
     * @return state context
     */
    StateContext getStateContext();
}
