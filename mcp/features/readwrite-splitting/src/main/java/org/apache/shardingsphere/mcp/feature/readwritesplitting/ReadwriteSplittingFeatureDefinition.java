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

package org.apache.shardingsphere.mcp.feature.readwritesplitting;

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

/**
 * Readwrite-splitting MCP feature definition.
 */
public final class ReadwriteSplittingFeatureDefinition {
    
    public static final String PLAN_RULE_TOOL_NAME = "database_gateway_plan_readwrite_splitting_rule";
    
    public static final String PLAN_STATUS_TOOL_NAME = "database_gateway_plan_readwrite_splitting_status";
    
    public static final String PLAN_RULE_PROMPT_NAME = "plan_readwrite_splitting_rule";
    
    public static final String PLAN_STATUS_PROMPT_NAME = "plan_readwrite_splitting_status";
    
    public static final WorkflowKind RULE_WORKFLOW_KIND = WorkflowKind.valueOf("readwrite.rule");
    
    public static final WorkflowKind STATUS_WORKFLOW_KIND = WorkflowKind.valueOf("readwrite.status");
    
    public static final String RULE_FIELD = "rule";
    
    public static final String WRITE_STORAGE_UNIT_FIELD = "write_storage_unit";
    
    public static final String READ_STORAGE_UNITS_FIELD = "read_storage_units";
    
    public static final String TRANSACTIONAL_READ_QUERY_STRATEGY_FIELD = "transactional_read_query_strategy";
    
    public static final String LOAD_BALANCER_TYPE_FIELD = "load_balancer_type";
    
    public static final String LOAD_BALANCER_PROPERTIES_FIELD = "load_balancer_properties";
    
    public static final String STORAGE_UNIT_FIELD = "storage_unit";
    
    public static final String TARGET_STATUS_FIELD = "target_status";
    
    public static final String RULES_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/{database}/rules";
    
    public static final String RULE_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}";
    
    public static final String STATUS_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/{database}/status";
    
    public static final String RULE_STATUS_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/{database}/rules/{rule}/status";
    
    public static final String RULE_COUNT_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/databases/{database}/rule-count";
    
    public static final String LOAD_BALANCE_ALGORITHM_PLUGINS_RESOURCE_URI = "shardingsphere://features/readwrite-splitting/load-balance-algorithm-plugins";
    
    private ReadwriteSplittingFeatureDefinition() {
    }
}
