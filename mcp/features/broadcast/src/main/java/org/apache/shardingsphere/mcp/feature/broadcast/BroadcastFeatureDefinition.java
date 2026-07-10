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

package org.apache.shardingsphere.mcp.feature.broadcast;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowKindDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;

/**
 * Broadcast MCP feature definition.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class BroadcastFeatureDefinition {
    
    public static final String PLAN_TOOL_NAME = "database_gateway_plan_broadcast_rule";
    
    public static final String PLAN_PROMPT_NAME = "plan_broadcast_rule";
    
    public static final WorkflowKind WORKFLOW_KIND = WorkflowKind.valueOf(WorkflowKindDescriptors.BROADCAST_RULE);
    
    public static final String TABLES_FIELD = "tables";
    
    public static final String RULES_RESOURCE_URI = "shardingsphere://features/broadcast/databases/{database}/rules";
    
    public static final String TABLE_RULE_RESOURCE_URI = "shardingsphere://features/broadcast/databases/{database}/tables/{table}/rule";
    
    public static final String RULE_COUNT_RESOURCE_URI = "shardingsphere://features/broadcast/databases/{database}/rule-count";
}
