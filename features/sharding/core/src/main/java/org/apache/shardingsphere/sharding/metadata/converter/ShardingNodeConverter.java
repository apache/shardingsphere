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

package org.apache.shardingsphere.sharding.metadata.converter;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingNodeConverter {
    
    private static final String TABLES = "tables";
    
    private static final String AUTO_TABLES = "auto_tables";
    
    private static final String BINDING_TABLES = "binding_tables";
    
    private static final String BROADCAST_TABLES = "broadcast_tables";
    
    private static final String DEFAULT_STRATEGY = "default_strategy";
    
    private static final String DEFAULT_DATABASE_STRATEGY = "default_database_strategy";
    
    private static final String DEFAULT_TABLE_STRATEGY = "default_table_strategy";
    
    private static final String DEFAULT_KEY_GENERATE_STRATEGY = "default_key_generate_strategy";
    
    private static final String DEFAULT_AUDIT_STRATEGY = "default_audit_strategy";
    
    private static final String DEFAULT_SHARDING_COLUMN = "default_sharding_column";
    
    private static final String SHARDING_ALGORITHMS = "sharding_algorithms";
    
    private static final String KEY_GENERATORS = "key_generators";
    
    private static final String AUDITORS = "auditors";
    
    private static final String SHARDING_CACHE = "sharding_cache";
    
    private static final String TABLE_NAME = "table_%s";
    
    private static final String AUTO_TABLE_NAME = "auto_table_%s";
    
    private static final String BINDING_TABLE_NAME = "binding_table_%s";
    
    /**
     * Get table name path.
     * 
     * @param tableName table name
     * @return table name path
     */
    public static String getTableNamePath(final String tableName) {
        return String.join("/", TABLES, String.format(TABLE_NAME, tableName));
    }
    
    /**
     * Get auto table name path.
     * 
     * @param tableName table name
     * @return auto table name path
     */
    public static String getAutoTableNamePath(final String tableName) {
        return String.join("/", AUTO_TABLES, String.format(AUTO_TABLE_NAME, tableName));
    }
    
    /**
     * Get binding table name path.
     * 
     * @param tableName table name
     * @return binding table name path
     */
    public static String getBindingTableNamePath(final String tableName) {
        return String.join("/", BINDING_TABLES, String.format(BINDING_TABLE_NAME, tableName));
    }
    
    /**
     * Get broadcast tables path.
     * 
     * @return broadcast tables path
     */
    public static String getBroadcastTablesPath() {
        return String.join("/", BROADCAST_TABLES);
    }
    
    /**
     * Get default database strategy path.
     * 
     * @return default database strategy path
     */
    public static String getDefaultDatabaseStrategyPath() {
        return String.join("/", DEFAULT_STRATEGY, DEFAULT_DATABASE_STRATEGY);
    }
    
    /**
     * Get default table strategy path.
     * 
     * @return default table strategy path
     */
    public static String getDefaultTableStrategyPath() {
        return String.join("/", DEFAULT_STRATEGY, DEFAULT_TABLE_STRATEGY);
    }
    
    /**
     * Get default key generate strategy path.
     * 
     * @return default key generate path
     */
    public static String getDefaultKeyGenerateStrategyPath() {
        return String.join("/", DEFAULT_STRATEGY, DEFAULT_KEY_GENERATE_STRATEGY);
    }
    
    /**
     * Get default audit strategy path.
     * 
     * @return default audit strategy path
     */
    public static String getDefaultAuditStrategyPath() {
        return String.join("/", DEFAULT_STRATEGY, DEFAULT_AUDIT_STRATEGY);
    }
    
    /**
     * Get default sharding column path.
     * 
     * @return default sharding column path
     */
    public static String getDefaultShardingColumnPath() {
        return String.join("/", DEFAULT_STRATEGY, DEFAULT_SHARDING_COLUMN);
    }
    
    /**
     * Get sharding algorithm path.
     * 
     * @param shardingAlgorithmName sharding algorithm name
     * @return sharding algorithm path
     */
    public static String getShardingAlgorithmPath(final String shardingAlgorithmName) {
        return String.join("/", SHARDING_ALGORITHMS, shardingAlgorithmName);
    }
    
    /**
     * Get key generator path.
     * 
     * @param keyGeneratorName key generator name
     * @return key generator path
     */
    public static String getKeyGeneratorPath(final String keyGeneratorName) {
        return String.join("/", KEY_GENERATORS, keyGeneratorName);
    }
    
    /**
     * Get auditor path.
     * 
     * @param auditorName auditor name
     * @return auditor path
     */
    public static String getAuditorPath(final String auditorName) {
        return String.join("/", AUDITORS, auditorName);
    }
    
    /**
     * Get sharding cache path.
     * 
     * @return sharding cache path
     */
    public static String getShardingCachePath() {
        return String.join("/", SHARDING_CACHE);
    }
}
