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

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.io.Closeable;
import java.util.Map;

/**
 * Meta data contexts.
 */
public interface MetaDataContexts extends Closeable {
    
    /**
     * Get database types.
     *
     * @return database types
     */
    Map<String, DatabaseType> getDatabaseTypes();
    
    /**
     * Get database type.
     *
     * @param schemaName  schema name
     * @return database type
     */
    DatabaseType getDatabaseType(String schemaName);
    
    /**
     * Get mata data map.
     *
     * @return mata data map
     */
    Map<String, ShardingSphereMetaData> getMetaDataMap();
    
    /**
     * Get default mata data.
     *
     * @return default mata data
     */
    ShardingSphereMetaData getDefaultMetaData();
    
    /**
     * Get executor engine.
     * 
     * @return executor engine
     */
    ExecutorEngine getExecutorEngine();
    
    /**
     * Get authentication.
     * 
     * @return authentication
     */
    Authentication getAuthentication();
    
    /**
     * Get configuration properties.
     *
     * @return configuration properties
     */
    ConfigurationProperties getProps();
}
