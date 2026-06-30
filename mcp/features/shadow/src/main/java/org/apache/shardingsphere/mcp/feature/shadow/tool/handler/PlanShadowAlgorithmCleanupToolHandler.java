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

package org.apache.shardingsphere.mcp.feature.shadow.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowAlgorithmCleanupWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Tool handler for shadow algorithm cleanup workflow planning.
 */
public final class PlanShadowAlgorithmCleanupToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ShadowWorkflowPlanningService planningService;
    
    public PlanShadowAlgorithmCleanupToolHandler() {
        planningService = new ShadowWorkflowPlanningService();
    }
    
    PlanShadowAlgorithmCleanupToolHandler(final ShadowWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return ShadowFeatureDefinition.PLAN_ALGORITHM_CLEANUP_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        ShadowAlgorithmCleanupWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ShadowAlgorithmCleanupWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.planAlgorithmCleanup(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ShadowAlgorithmCleanupWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.ALGORITHM_NAME_FIELD, request::setAlgorithmName);
    }
    
    private void applyStructuredIntentEvidence(final ShadowAlgorithmCleanupWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.ALGORITHM_NAME_FIELD, request::setAlgorithmName);
    }
    
    private void applyStringField(final Map<String, Object> values, final String fieldName, final Consumer<String> consumer) {
        Object value = values.get(fieldName);
        if (null != value) {
            consumer.accept(String.valueOf(value));
        }
    }
    
    private void applyStringArgument(final WorkflowPlanningArguments workflowPlanningArguments, final String fieldName, final Consumer<String> consumer) {
        String value = workflowPlanningArguments.getStringArgument(fieldName);
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }
}
