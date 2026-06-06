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

package org.apache.shardingsphere.mcp.feature.sharding.tool.handler;

import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

/**
 * Tool handler for sharding table rule planning.
 */
public final class PlanShardingTableRuleToolHandler extends AbstractShardingPlanningToolHandler {
    
    public PlanShardingTableRuleToolHandler() {
    }
    
    PlanShardingTableRuleToolHandler(final ShardingWorkflowPlanningService planningService) {
        super(planningService);
    }
    
    @Override
    public String getToolName() {
        return ShardingFeatureDefinition.PLAN_TABLE_RULE_TOOL_NAME;
    }
    
    @Override
    protected WorkflowContextSnapshot plan(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall, final ShardingWorkflowRequest request) {
        return getPlanningService().planTableRule(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
    }
}
