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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.ReadwriteSplittingFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service.ReadwriteSplittingStatusWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;
import java.util.function.Consumer;

/**
 * Tool handler for readwrite-splitting storage-unit status workflow planning.
 */
public final class PlanReadwriteSplittingStatusToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ReadwriteSplittingStatusWorkflowPlanningService planningService;
    
    public PlanReadwriteSplittingStatusToolHandler() {
        planningService = new ReadwriteSplittingStatusWorkflowPlanningService();
    }
    
    PlanReadwriteSplittingStatusToolHandler(final ReadwriteSplittingStatusWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return ReadwriteSplittingFeatureDefinition.PLAN_STATUS_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        rejectOperationTypeAlias(toolCall.getArguments());
        ReadwriteSplittingStatusWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ReadwriteSplittingStatusWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.plan(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ReadwriteSplittingStatusWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, request::setStorageUnit);
        applyStringArgument(workflowPlanningArguments, ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, request::setTargetStatus);
    }
    
    private void applyStructuredIntentEvidence(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, request::setStorageUnit);
        applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, request::setTargetStatus);
    }
    
    private void rejectOperationTypeAlias(final Map<String, Object> arguments) {
        if (arguments.containsKey(WorkflowFieldNames.OPERATION_TYPE)) {
            throw new MCPInvalidRequestException("operation_type is not supported for readwrite-splitting status. Use target_status instead.");
        }
    }
    
    private void applyStringField(final Map<String, Object> values, final String fieldName, final Consumer<String> consumer) {
        Object value = values.get(fieldName);
        if (null != value) {
            consumer.accept(String.valueOf(value));
        }
    }
    
    private void applyStringArgument(final WorkflowPlanningArguments workflowPlanningArguments, final String fieldName, final Consumer<String> consumer) {
        final String value = workflowPlanningArguments.getStringArgument(fieldName);
        if (!value.isEmpty()) {
            consumer.accept(value);
        }
    }
}
