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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for mask workflow planning.
 */
public final class PlanMaskRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final MaskWorkflowPlanningService planningService = new MaskWorkflowPlanningService();
    
    private final MaskAlgorithmPropertyTemplateService propertyTemplateService = new MaskAlgorithmPropertyTemplateService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return MaskFeatureDefinition.PLAN_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        WorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(arguments, this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext.getWorkflowSessionContext(), requestContext.getMetadataQueryFacade(),
                requestContext.getQueryFacade(), request);
        return new MCPMapPayload(new MaskWorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final WorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ALGORITHM_TYPE));
        request.getPrimaryAlgorithmProperties().putAll(workflowPlanningArguments.getAlgorithmPropertyMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES, "primary"));
        request.getPrimaryAlgorithmSecretReferences().putAll(workflowPlanningArguments.getSecretReferenceMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES));
    }
    
    private void applyStructuredIntentEvidence(final WorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        Object fieldSemantics = structuredIntentEvidence.get(WorkflowFieldNames.FIELD_SEMANTICS);
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
}
