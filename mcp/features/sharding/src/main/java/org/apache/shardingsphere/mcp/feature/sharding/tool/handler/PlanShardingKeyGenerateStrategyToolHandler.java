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
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingKeyGenerateStrategyWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingKeyGenerateStrategyWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

/**
 * Tool handler for sharding key generate strategy planning.
 */
public final class PlanShardingKeyGenerateStrategyToolHandler extends AbstractShardingPlanningToolHandler {
    
    private final ShardingPlanningRequestBinder requestBinder = new ShardingPlanningRequestBinder();
    
    private final ShardingKeyGenerateStrategyWorkflowPlanningService planningService;
    
    public PlanShardingKeyGenerateStrategyToolHandler() {
        planningService = new ShardingKeyGenerateStrategyWorkflowPlanningService();
    }
    
    PlanShardingKeyGenerateStrategyToolHandler(final ShardingKeyGenerateStrategyWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
    @Override
    public String getToolName() {
        return ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME;
    }
    
    @Override
    protected ShardingWorkflowRequest bindRequest(final MCPToolCall toolCall) {
        return requestBinder.bindKeyGenerateStrategy(toolCall.getArguments()).toWorkflowRequest();
    }
    
    @Override
    protected WorkflowContextSnapshot plan(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall, final ShardingWorkflowRequest request) {
        return planningService.plan(workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(),
                new ShardingKeyGenerateStrategyWorkflowRequest(request));
    }
}
