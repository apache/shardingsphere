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

package org.apache.shardingsphere.mcp.feature.broadcast.tool.handler;

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.broadcast.BroadcastFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.model.BroadcastWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.broadcast.tool.service.BroadcastWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for broadcast workflow planning.
 */
public final class PlanBroadcastRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final BroadcastWorkflowPlanningService planningService = new BroadcastWorkflowPlanningService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return BroadcastFeatureDefinition.PLAN_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        BroadcastWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(BroadcastWorkflowRequest::new, arguments,
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext.getWorkflowSessionContext(), requestContext.getQueryFacade(), request);
        return new MCPMapPayload(WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final BroadcastWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setTables(workflowPlanningArguments.getStringArgument(BroadcastFeatureDefinition.TABLES_FIELD));
    }
    
    private void applyStructuredIntentEvidence(final BroadcastWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        Object tables = structuredIntentEvidence.get(BroadcastFeatureDefinition.TABLES_FIELD);
        if (null != tables) {
            request.setTables(String.valueOf(tables));
        }
    }
}
