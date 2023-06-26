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
import org.apache.shardingsphere.infra.metadata.converter.RuleDefaultItemNodeConverter;
import org.apache.shardingsphere.infra.metadata.converter.RuleItemNodeConverter;
import org.apache.shardingsphere.infra.metadata.converter.RuleRootNodeConverter;

/**
 * Sharding node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingNodeConverter {
    
    private static final String DEFAULT_STRATEGIES_NODE = "default_strategies";
    
    private static final RuleRootNodeConverter ROOT_NODE_CONVERTER = new RuleRootNodeConverter("sharding");
    
    private static final RuleItemNodeConverter TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "tables");
    
    private static final RuleItemNodeConverter AUTO_TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "auto_tables");
    
    private static final RuleItemNodeConverter BINDING_TABLE_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "binding_tables");
    
    private static final RuleItemNodeConverter ALGORITHM_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "algorithms");
    
    private static final RuleItemNodeConverter KEY_GENERATOR_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "key_generators");
    
    private static final RuleItemNodeConverter AUDITOR_NODE_CONVERTER = new RuleItemNodeConverter(ROOT_NODE_CONVERTER, "auditors");
    
    private static final RuleDefaultItemNodeConverter DEFAULT_DATABASE_STRATEGY_NODE_CONVERTER
            = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_database_strategy");
    
    private static final RuleDefaultItemNodeConverter DEFAULT_TABLE_STRATEGY_NODE_CONVERTER = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_table_strategy");
    
    private static final RuleDefaultItemNodeConverter DEFAULT_KEY_GENERATE_STRATEGY_NODE_CONVERTER
            = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_key_generate_strategy");
    
    private static final RuleDefaultItemNodeConverter DEFAULT_AUDIT_STRATEGY_NODE_CONVERTER = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_audit_strategy");
    
    private static final RuleDefaultItemNodeConverter DEFAULT_SHARDING_COLUMN_NODE_CONVERTER
            = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_sharding_column");
    
    private static final RuleDefaultItemNodeConverter SHARDING_CACHE_NODE_CONVERTER = new RuleDefaultItemNodeConverter(ROOT_NODE_CONVERTER, "sharding_cache");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodeConverter getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get table node converter.
     *
     * @return table node converter
     */
    public static RuleItemNodeConverter getTableNodeConverter() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get auto table node converter.
     *
     * @return auto table node converter
     */
    public static RuleItemNodeConverter getAutoTableNodeConverter() {
        return AUTO_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get binding table node converter.
     *
     * @return binding table node converter
     */
    public static RuleItemNodeConverter getBindingTableNodeConverter() {
        return BINDING_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get algorithm node converter.
     *
     * @return algorithm node converter
     */
    public static RuleItemNodeConverter getAlgorithmNodeConverter() {
        return ALGORITHM_NODE_CONVERTER;
    }
    
    /**
     * Get key generator node converter.
     *
     * @return key generator node converter
     */
    public static RuleItemNodeConverter getKeyGeneratorNodeConverter() {
        return KEY_GENERATOR_NODE_CONVERTER;
    }
    
    /**
     * Get auditor node converter.
     *
     * @return auditor node converter
     */
    public static RuleItemNodeConverter getAuditorNodeConverter() {
        return AUDITOR_NODE_CONVERTER;
    }
    
    /**
     * Get default database strategy node converter.
     *
     * @return default database strategy node converter
     */
    public static RuleDefaultItemNodeConverter getDefaultDatabaseStrategyNodeConverter() {
        return DEFAULT_DATABASE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default table strategy node converter.
     *
     * @return default table strategy node converter
     */
    public static RuleDefaultItemNodeConverter getDefaultTableStrategyNodeConverter() {
        return DEFAULT_TABLE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default key generate strategy node converter.
     *
     * @return default key generate strategy node converter
     */
    public static RuleDefaultItemNodeConverter getDefaultKeyGenerateStrategyNodeConverter() {
        return DEFAULT_KEY_GENERATE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default audit strategy node converter.
     *
     * @return default table strategy node converter
     */
    public static RuleDefaultItemNodeConverter getDefaultAuditStrategyNodeConverter() {
        return DEFAULT_AUDIT_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default sharding column node converter.
     *
     * @return default sharding column node converter
     */
    public static RuleDefaultItemNodeConverter getDefaultShardingColumnNodeConverter() {
        return DEFAULT_SHARDING_COLUMN_NODE_CONVERTER;
    }
    
    /**
     * Get sharding cache node converter.
     *
     * @return sharding cache node converter
     */
    public static RuleDefaultItemNodeConverter getShardingCacheNodeConverter() {
        return SHARDING_CACHE_NODE_CONVERTER;
    }
}
