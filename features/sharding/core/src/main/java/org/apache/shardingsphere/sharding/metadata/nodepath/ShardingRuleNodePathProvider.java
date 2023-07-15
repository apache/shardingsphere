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

package org.apache.shardingsphere.sharding.metadata.nodepath;

import org.apache.shardingsphere.infra.metadata.nodepath.RuleNodePath;
import org.apache.shardingsphere.mode.spi.RuleNodePathProvider;

import java.util.Arrays;

/**
 * Sharding rule node path provider.
 */
public final class ShardingRuleNodePathProvider implements RuleNodePathProvider {
    
    public static final String RULE_TYPE = "sharding";
    
    public static final String TABLES = "tables";
    
    public static final String AUTO_TABLES = "auto_tables";
    
    public static final String BINDING_TABLES = "binding_tables";
    
    public static final String ALGORITHMS = "algorithms";
    
    public static final String KEY_GENERATORS = "key_generators";
    
    public static final String AUDITORS = "auditors";
    
    public static final String DEFAULT_DATABASE_STRATEGY = "default_database_strategy";
    
    public static final String DEFAULT_TABLE_STRATEGY = "default_table_strategy";
    
    public static final String DEFAULT_KEY_GENERATE_STRATEGY = "default_key_generate_strategy";
    
    public static final String DEFAULT_AUDIT_STRATEGY = "default_audit_strategy";
    
    public static final String DEFAULT_SHARDING_COLUMN = "default_sharding_column";
    
    public static final String SHARDING_CACHE = "sharding_cache";
    
    private static final String DEFAULT_STRATEGIES_PREFIX = "default_strategies.";
    
    private static final RuleNodePath INSTANCE = new RuleNodePath(RULE_TYPE,
            Arrays.asList(TABLES, AUTO_TABLES, BINDING_TABLES, ALGORITHMS, KEY_GENERATORS, AUDITORS),
            Arrays.asList(DEFAULT_STRATEGIES_PREFIX + DEFAULT_DATABASE_STRATEGY, DEFAULT_STRATEGIES_PREFIX + DEFAULT_TABLE_STRATEGY,
                    DEFAULT_STRATEGIES_PREFIX + DEFAULT_KEY_GENERATE_STRATEGY, DEFAULT_STRATEGIES_PREFIX + DEFAULT_AUDIT_STRATEGY, DEFAULT_STRATEGIES_PREFIX + DEFAULT_SHARDING_COLUMN,
                    SHARDING_CACHE));
    
    @Override
    public RuleNodePath getRuleNodePath() {
        return INSTANCE;
    }
}
