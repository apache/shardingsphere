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

package org.apache.shardingsphere.sharding.spring.namespace.tag.rule;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

/**
 * Sharding rule bean definition tag.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingRuleBeanDefinitionTag {
    
    public static final String ROOT_TAG = "rule";
    
    public static final String TABLE_RULES_TAG = "table-rules";
    
    public static final String AUTO_TABLE_RULES_TAG = "auto-table-rules";
    
    public static final String TABLE_RULE_TAG = "table-rule";
    
    public static final String AUTO_TABLE_RULE_TAG = "auto-table-rule";
    
    public static final String BINDING_TABLE_RULES_TAG = "binding-table-rules";
    
    public static final String BINDING_TABLE_RULE_TAG = "binding-table-rule";
    
    public static final String LOGIC_TABLE_ATTRIBUTE = "logic-table";
    
    public static final String LOGIC_TABLES_ATTRIBUTE = "logic-tables";
    
    public static final String BROADCAST_TABLE_RULES_TAG = "broadcast-table-rules";
    
    public static final String BROADCAST_TABLE_RULE_TAG = "broadcast-table-rule";
    
    public static final String TABLE_ATTRIBUTE = "table";
    
    public static final String ACTUAL_DATA_NODES_ATTRIBUTE = "actual-data-nodes";
    
    public static final String ACTUAL_DATA_SOURCES_ATTRIBUTE = "actual-data-sources";
    
    public static final String DATABASE_STRATEGY_REF_ATTRIBUTE = "database-strategy-ref";
    
    public static final String TABLE_STRATEGY_REF_ATTRIBUTE = "table-strategy-ref";
    
    public static final String SHARDING_STRATEGY_REF_ATTRIBUTE = "sharding-strategy-ref";
    
    public static final String DEFAULT_DATABASE_STRATEGY_REF_ATTRIBUTE = "default-database-strategy-ref";
    
    public static final String DEFAULT_TABLE_STRATEGY_REF_ATTRIBUTE = "default-table-strategy-ref";
    
    public static final String STRATEGY_REF_ATTRIBUTE = "key-generate-strategy-ref";
    
    public static final String DEFAULT_KEY_GENERATE_STRATEGY_REF_ATTRIBUTE = "default-key-generate-strategy-ref";
}
