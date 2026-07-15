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

import org.apache.shardingsphere.mcp.feature.sharding.ShardingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.sharding.tool.model.ShardingWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.sharding.tool.service.ShardingWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;

import java.util.Map;

/**
 * Tool handler for sharding key generate strategy planning.
 */
public final class PlanShardingKeyGenerateStrategyToolHandler extends AbstractShardingPlanningToolHandler {
    
    private final ShardingPlanningRequestBinder requestBinder = new ShardingPlanningRequestBinder();
    
    private final ShardingWorkflowPlanningService planningService = new ShardingWorkflowPlanningService();
    
    @Override
    public String getToolName() {
        return ShardingFeatureDefinition.PLAN_KEY_GENERATE_STRATEGY_TOOL_NAME;
    }
    
    @Override
    protected ShardingWorkflowRequest bindRequest(final Map<String, Object> arguments) {
        return requestBinder.bindKeyGenerateStrategy(arguments);
    }
    
    @Override
    protected WorkflowContextSnapshot plan(final MCPWorkflowRequestContext workflowContext, final ShardingWorkflowRequest request) {
        return planningService.planKeyGenerateStrategy(
                workflowContext.getWorkflowSessionContext(), workflowContext.getQueryFacade(), request);
    }
}
