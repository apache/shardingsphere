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

package org.apache.shardingsphere.mcp.feature.shadow;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

/**
 * Shadow MCP feature definition.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowFeatureDefinition {
    
    public static final String PLAN_RULE_TOOL_NAME = "database_gateway_plan_shadow_rule";
    
    public static final String PLAN_DEFAULT_ALGORITHM_TOOL_NAME = "database_gateway_plan_default_shadow_algorithm";
    
    public static final String PLAN_ALGORITHM_CLEANUP_TOOL_NAME = "database_gateway_plan_shadow_algorithm_cleanup";
    
    public static final String PLAN_RULE_PROMPT_NAME = "plan_shadow_rule";
    
    public static final String PLAN_DEFAULT_ALGORITHM_PROMPT_NAME = "plan_default_shadow_algorithm";
    
    public static final String PLAN_ALGORITHM_CLEANUP_PROMPT_NAME = "plan_shadow_algorithm_cleanup";
    
    public static final WorkflowKind RULE_WORKFLOW_KIND = WorkflowKind.valueOf("shadow.rule");
    
    public static final WorkflowKind DEFAULT_ALGORITHM_WORKFLOW_KIND = WorkflowKind.valueOf("shadow.default");
    
    public static final WorkflowKind ALGORITHM_CLEANUP_WORKFLOW_KIND = WorkflowKind.valueOf("shadow.cleanup");
    
    public static final String RULE_FIELD = "rule";
    
    public static final String SOURCE_STORAGE_UNIT_FIELD = "source_storage_unit";
    
    public static final String SHADOW_STORAGE_UNIT_FIELD = "shadow_storage_unit";
    
    public static final String TABLE_FIELD = "table";
    
    public static final String ALGORITHM_NAME_FIELD = "algorithm_name";
    
    public static final String ALGORITHM_TYPE_FIELD = "algorithm_type";
    
    public static final String ALGORITHM_PROPERTIES_FIELD = "algorithm_properties";
    
    public static final String DEFAULT_ALGORITHM_TYPE = "SQL_HINT";
    
    public static final String RULES_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/rules";
    
    public static final String RULE_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/rules/{rule}";
    
    public static final String TABLE_RULES_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/table-rules";
    
    public static final String TABLE_RULE_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/tables/{table}/rules";
    
    public static final String ALGORITHMS_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/algorithms";
    
    public static final String DEFAULT_ALGORITHM_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/default-algorithm";
    
    public static final String RULE_COUNT_RESOURCE_URI = "shardingsphere://features/shadow/databases/{database}/rule-count";
    
    public static final String ALGORITHM_PLUGINS_RESOURCE_URI = "shardingsphere://features/shadow/algorithm-plugins";
    
    public static final String STORAGE_UNITS_RESOURCE_URI = "shardingsphere://databases/{database}/storage-units";
    
    public static final String SINGLE_TABLES_RESOURCE_URI = "shardingsphere://databases/{database}/single-tables";
    
    public static final String SINGLE_TABLE_RESOURCE_URI = "shardingsphere://databases/{database}/single-tables/{table}";
    
}
