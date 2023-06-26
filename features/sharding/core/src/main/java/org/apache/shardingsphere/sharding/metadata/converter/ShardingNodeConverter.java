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
import org.apache.shardingsphere.infra.metadata.nodepath.item.UniqueRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.nodepath.item.NamedRuleItemNodePath;
import org.apache.shardingsphere.infra.metadata.nodepath.RuleRootNodePath;

/**
 * Sharding node converter.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingNodeConverter {
    
    private static final String DEFAULT_STRATEGIES_NODE = "default_strategies";
    
    private static final RuleRootNodePath ROOT_NODE_CONVERTER = new RuleRootNodePath("sharding");
    
    private static final NamedRuleItemNodePath TABLE_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "tables");
    
    private static final NamedRuleItemNodePath AUTO_TABLE_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "auto_tables");
    
    private static final NamedRuleItemNodePath BINDING_TABLE_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "binding_tables");
    
    private static final NamedRuleItemNodePath ALGORITHM_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "algorithms");
    
    private static final NamedRuleItemNodePath KEY_GENERATOR_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "key_generators");
    
    private static final NamedRuleItemNodePath AUDITOR_NODE_CONVERTER = new NamedRuleItemNodePath(ROOT_NODE_CONVERTER, "auditors");
    
    private static final UniqueRuleItemNodePath DEFAULT_DATABASE_STRATEGY_NODE_CONVERTER =
            new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_database_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_TABLE_STRATEGY_NODE_CONVERTER = new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_table_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_KEY_GENERATE_STRATEGY_NODE_CONVERTER =
            new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_key_generate_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_AUDIT_STRATEGY_NODE_CONVERTER = new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_audit_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_SHARDING_COLUMN_NODE_CONVERTER =
            new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, DEFAULT_STRATEGIES_NODE, "default_sharding_column");
    
    private static final UniqueRuleItemNodePath SHARDING_CACHE_NODE_CONVERTER = new UniqueRuleItemNodePath(ROOT_NODE_CONVERTER, "sharding_cache");
    
    /**
     * Get rule root node converter.
     *
     * @return rule root node converter
     */
    public static RuleRootNodePath getRuleRootNodeConverter() {
        return ROOT_NODE_CONVERTER;
    }
    
    /**
     * Get table node converter.
     *
     * @return table node converter
     */
    public static NamedRuleItemNodePath getTableNodeConverter() {
        return TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get auto table node converter.
     *
     * @return auto table node converter
     */
    public static NamedRuleItemNodePath getAutoTableNodeConverter() {
        return AUTO_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get binding table node converter.
     *
     * @return binding table node converter
     */
    public static NamedRuleItemNodePath getBindingTableNodeConverter() {
        return BINDING_TABLE_NODE_CONVERTER;
    }
    
    /**
     * Get algorithm node converter.
     *
     * @return algorithm node converter
     */
    public static NamedRuleItemNodePath getAlgorithmNodeConverter() {
        return ALGORITHM_NODE_CONVERTER;
    }
    
    /**
     * Get key generator node converter.
     *
     * @return key generator node converter
     */
    public static NamedRuleItemNodePath getKeyGeneratorNodeConverter() {
        return KEY_GENERATOR_NODE_CONVERTER;
    }
    
    /**
     * Get auditor node converter.
     *
     * @return auditor node converter
     */
    public static NamedRuleItemNodePath getAuditorNodeConverter() {
        return AUDITOR_NODE_CONVERTER;
    }
    
    /**
     * Get default database strategy node converter.
     *
     * @return default database strategy node converter
     */
    public static UniqueRuleItemNodePath getDefaultDatabaseStrategyNodeConverter() {
        return DEFAULT_DATABASE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default table strategy node converter.
     *
     * @return default table strategy node converter
     */
    public static UniqueRuleItemNodePath getDefaultTableStrategyNodeConverter() {
        return DEFAULT_TABLE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default key generate strategy node converter.
     *
     * @return default key generate strategy node converter
     */
    public static UniqueRuleItemNodePath getDefaultKeyGenerateStrategyNodeConverter() {
        return DEFAULT_KEY_GENERATE_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default audit strategy node converter.
     *
     * @return default table strategy node converter
     */
    public static UniqueRuleItemNodePath getDefaultAuditStrategyNodeConverter() {
        return DEFAULT_AUDIT_STRATEGY_NODE_CONVERTER;
    }
    
    /**
     * Get default sharding column node converter.
     *
     * @return default sharding column node converter
     */
    public static UniqueRuleItemNodePath getDefaultShardingColumnNodeConverter() {
        return DEFAULT_SHARDING_COLUMN_NODE_CONVERTER;
    }
    
    /**
     * Get sharding cache node converter.
     *
     * @return sharding cache node converter
     */
    public static UniqueRuleItemNodePath getShardingCacheNodeConverter() {
        return SHARDING_CACHE_NODE_CONVERTER;
    }
}
