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
    
    private static final RuleRootNodePath ROOT_NODE_PATH = new RuleRootNodePath("sharding");
    
    private static final NamedRuleItemNodePath TABLE_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "tables");
    
    private static final NamedRuleItemNodePath AUTO_TABLE_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "auto_tables");
    
    private static final NamedRuleItemNodePath BINDING_TABLE_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "binding_tables");
    
    private static final NamedRuleItemNodePath ALGORITHM_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "algorithms");
    
    private static final NamedRuleItemNodePath KEY_GENERATOR_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "key_generators");
    
    private static final NamedRuleItemNodePath AUDITOR_NODE_PATH = new NamedRuleItemNodePath(ROOT_NODE_PATH, "auditors");
    
    private static final UniqueRuleItemNodePath DEFAULT_DATABASE_STRATEGY_NODE_PATH =
            new UniqueRuleItemNodePath(ROOT_NODE_PATH, DEFAULT_STRATEGIES_NODE, "default_database_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_TABLE_STRATEGY_NODE_PATH = new UniqueRuleItemNodePath(ROOT_NODE_PATH, DEFAULT_STRATEGIES_NODE, "default_table_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_KEY_GENERATE_STRATEGY_NODE_PATH =
            new UniqueRuleItemNodePath(ROOT_NODE_PATH, DEFAULT_STRATEGIES_NODE, "default_key_generate_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_AUDIT_STRATEGY_NODE_PATH = new UniqueRuleItemNodePath(ROOT_NODE_PATH, DEFAULT_STRATEGIES_NODE, "default_audit_strategy");
    
    private static final UniqueRuleItemNodePath DEFAULT_SHARDING_COLUMN_NODE_PATH =
            new UniqueRuleItemNodePath(ROOT_NODE_PATH, DEFAULT_STRATEGIES_NODE, "default_sharding_column");
    
    private static final UniqueRuleItemNodePath SHARDING_CACHE_NODE_PATH = new UniqueRuleItemNodePath(ROOT_NODE_PATH, "sharding_cache");
    
    /**
     * Get rule root node path.
     *
     * @return rule root node path
     */
    public static RuleRootNodePath getRuleRootNodePath() {
        return ROOT_NODE_PATH;
    }
    
    /**
     * Get table node path.
     *
     * @return table node path
     */
    public static NamedRuleItemNodePath getTableNodePath() {
        return TABLE_NODE_PATH;
    }
    
    /**
     * Get auto table node path.
     *
     * @return auto table node path
     */
    public static NamedRuleItemNodePath getAutoTableNodePath() {
        return AUTO_TABLE_NODE_PATH;
    }
    
    /**
     * Get binding table node path.
     *
     * @return binding table node path
     */
    public static NamedRuleItemNodePath getBindingTableNodePath() {
        return BINDING_TABLE_NODE_PATH;
    }
    
    /**
     * Get algorithm node path.
     *
     * @return algorithm node path
     */
    public static NamedRuleItemNodePath getAlgorithmNodePath() {
        return ALGORITHM_NODE_PATH;
    }
    
    /**
     * Get key generator node path.
     *
     * @return key generator node path
     */
    public static NamedRuleItemNodePath getKeyGeneratorNodePath() {
        return KEY_GENERATOR_NODE_PATH;
    }
    
    /**
     * Get auditor node path.
     *
     * @return auditor node path
     */
    public static NamedRuleItemNodePath getAuditorNodePath() {
        return AUDITOR_NODE_PATH;
    }
    
    /**
     * Get default database strategy node path.
     *
     * @return default database strategy node path
     */
    public static UniqueRuleItemNodePath getDefaultDatabaseStrategyNodePath() {
        return DEFAULT_DATABASE_STRATEGY_NODE_PATH;
    }
    
    /**
     * Get default table strategy node path.
     *
     * @return default table strategy node path
     */
    public static UniqueRuleItemNodePath getDefaultTableStrategyNodePath() {
        return DEFAULT_TABLE_STRATEGY_NODE_PATH;
    }
    
    /**
     * Get default key generate strategy node path.
     *
     * @return default key generate strategy node path
     */
    public static UniqueRuleItemNodePath getDefaultKeyGenerateStrategyNodePath() {
        return DEFAULT_KEY_GENERATE_STRATEGY_NODE_PATH;
    }
    
    /**
     * Get default audit strategy node path.
     *
     * @return default table strategy node path
     */
    public static UniqueRuleItemNodePath getDefaultAuditStrategyNodePath() {
        return DEFAULT_AUDIT_STRATEGY_NODE_PATH;
    }
    
    /**
     * Get default sharding column node path.
     *
     * @return default sharding column node path
     */
    public static UniqueRuleItemNodePath getDefaultShardingColumnNodePath() {
        return DEFAULT_SHARDING_COLUMN_NODE_PATH;
    }
    
    /**
     * Get sharding cache node path.
     *
     * @return sharding cache node path
     */
    public static UniqueRuleItemNodePath getShardingCacheNodePath() {
        return SHARDING_CACHE_NODE_PATH;
    }
}
