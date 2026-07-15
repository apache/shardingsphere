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

package org.apache.shardingsphere.mcp.feature.sharding;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowKindDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

/**
 * Sharding MCP feature definition.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShardingFeatureDefinition {
    
    public static final WorkflowKind TABLE_RULE_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_TABLE_RULE);
    
    public static final WorkflowKind TABLE_REFERENCE_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_TABLE_REFERENCE);
    
    public static final WorkflowKind DEFAULT_STRATEGY_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_DEFAULT_STRATEGY);
    
    public static final WorkflowKind KEY_GENERATOR_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_KEY_GENERATOR);
    
    public static final WorkflowKind KEY_GENERATE_STRATEGY_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_KEY_GENERATE_STRATEGY);
    
    public static final WorkflowKind COMPONENT_CLEANUP_WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.SHARDING_COMPONENT_CLEANUP);
    
    public static final String PLAN_TABLE_RULE_TOOL_NAME = "database_gateway_plan_sharding_table_rule";
    
    public static final String PLAN_TABLE_REFERENCE_TOOL_NAME = "database_gateway_plan_sharding_table_reference_rule";
    
    public static final String PLAN_DEFAULT_STRATEGY_TOOL_NAME = "database_gateway_plan_sharding_default_strategy";
    
    public static final String PLAN_KEY_GENERATOR_TOOL_NAME = "database_gateway_plan_sharding_key_generator";
    
    public static final String PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME = "database_gateway_plan_sharding_key_generate_strategy";
    
    public static final String PLAN_COMPONENT_CLEANUP_TOOL_NAME = "database_gateway_plan_sharding_rule_component_cleanup";
    
    public static final String PLAN_TABLE_RULE_PROMPT_NAME = "plan_sharding_table_rule";
    
    public static final String PLAN_TABLE_REFERENCE_PROMPT_NAME = "plan_sharding_table_reference_rule";
    
    public static final String PLAN_DEFAULT_STRATEGY_PROMPT_NAME = "plan_sharding_default_strategy";
    
    public static final String PLAN_KEY_GENERATOR_PROMPT_NAME = "plan_sharding_key_generator";
    
    public static final String PLAN_KEY_GENERATE_STRATEGY_PROMPT_NAME = "plan_sharding_key_generate_strategy";
    
    public static final String PLAN_COMPONENT_CLEANUP_PROMPT_NAME = "plan_sharding_rule_component_cleanup";
    
    public static final String ALGORITHM_PLUGINS_RESOURCE_URI = "shardingsphere://features/sharding/algorithm-plugins";
    
    public static final String KEY_GENERATE_ALGORITHM_PLUGINS_RESOURCE_URI = "shardingsphere://features/sharding/key-generate-algorithm-plugins";
    
    public static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/algorithms";
    
    public static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/table-rules";
    
    public static final String TABLE_RULE_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/tables/{table}/table-rule";
    
    public static final String TABLE_NODES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/table-nodes";
    
    public static final String TABLE_NODE_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/tables/{table}/nodes";
    
    public static final String TABLE_REFERENCE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/table-reference-rules";
    
    public static final String TABLE_REFERENCE_RULE_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/table-reference-rules/{rule}";
    
    public static final String DEFAULT_STRATEGY_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/default-strategy";
    
    public static final String KEY_GENERATORS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/key-generators";
    
    public static final String KEY_GENERATOR_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}";
    
    public static final String KEY_GENERATE_STRATEGIES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/key-generate-strategies";
    
    public static final String KEY_GENERATE_STRATEGY_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/key-generate-strategies/{strategy}";
    
    public static final String AUDITORS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/auditors";
    
    public static final String UNUSED_ALGORITHMS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/unused-algorithms";
    
    public static final String UNUSED_KEY_GENERATORS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/unused-key-generators";
    
    public static final String UNUSED_AUDITORS_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/unused-auditors";
    
    public static final String ALGORITHM_USED_TABLE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/algorithms/{algorithm}/table-rules";
    
    public static final String KEY_GENERATOR_USED_TABLE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/key-generators/{keyGenerator}/table-rules";
    
    public static final String AUDITOR_USED_TABLE_RULES_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/auditors/{auditor}/table-rules";
    
    public static final String RULE_COUNT_RESOURCE_URI = "shardingsphere://features/sharding/databases/{database}/rule-count";
    
    public static final String TABLE_FIELD = "table";
    
    public static final String RULE_FIELD = "rule";
    
    public static final String KEY_GENERATOR_FIELD = "keyGenerator";
    
    public static final String STRATEGY_FIELD = "strategy";
    
    public static final String ALGORITHM_FIELD = "algorithm";
    
    public static final String AUDITOR_FIELD = "auditor";
    
}
