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

package org.apache.shardingsphere.infra.context.schema;

import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorKernel;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;

import java.io.Closeable;
import java.util.Map;

/**
 * Schema contexts.
 */
public interface SchemaContexts extends Closeable {
    
    /**
     * Get database type.
     *
     * @return database type
     */
    DatabaseType getDatabaseType();
    
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
     * Get executor kernel.
     * 
     * @return executor kernel
     */
    ExecutorKernel getExecutorKernel();
    
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
    
    /**
     * Is circuit break or not.
     * 
     * @return is circuit break or not
     */
    boolean isCircuitBreak();
}
