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

package org.apache.shardingsphere.mcp.feature.mask.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for mask workflow planning.
 */
public final class PlanMaskRuleToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final MaskWorkflowPlanningService planningService = new MaskWorkflowPlanningService();
    
    private final MaskAlgorithmPropertyTemplateService propertyTemplateService = new MaskAlgorithmPropertyTemplateService();
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return MaskFeatureDefinition.PLAN_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        WorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(toolCall.getArguments(), this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyUserOverrides);
        WorkflowContextSnapshot snapshot = planningService.plan(workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getMetadataQueryFacade(),
                workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final WorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ALGORITHM_TYPE));
        request.getPrimaryAlgorithmProperties().putAll(workflowPlanningArguments.getMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES));
    }
    
    private void applyStructuredIntentEvidence(final WorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        Object fieldSemantics = structuredIntentEvidence.get(WorkflowFieldNames.FIELD_SEMANTICS);
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
    
    private void applyUserOverrides(final WorkflowRequest request, final Map<String, Object> userOverrides) {
        Object algorithmType = userOverrides.get(WorkflowFieldNames.ALGORITHM_TYPE);
        if (null != algorithmType && !String.valueOf(algorithmType).trim().isEmpty()) {
            request.setAlgorithmType(String.valueOf(algorithmType).trim());
        }
    }
}
