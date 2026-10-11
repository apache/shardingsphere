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

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.mask.MaskFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.List;
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
        WorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(arguments, this::bindFeatureArguments);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext.getWorkflowSessionContext(), requestContext.getMetadataQueryFacade(),
                requestContext.getQueryFacade(), request);
        return new MCPMapPayload(buildPlanResponse(snapshot));
    }
    
    private Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        WorkflowRequest request = snapshot.getRequest();
        Map<String, Object> result = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, request);
        Map<String, String> maskedProperties = WorkflowArtifactMaskUtils.maskPropertyMap(
                request.getAlgorithmProperties("primary"), createPropertyRequirements(snapshot, request), request, "primary");
        result.put("masked_property_preview", Map.of("primary", maskedProperties));
        return result;
    }
    
    private List<AlgorithmPropertyRequirement> createPropertyRequirements(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        return snapshot.getPropertyRequirements().isEmpty()
                ? propertyTemplateService.findRequirements(request.getAlgorithmType())
                : snapshot.getPropertyRequirements().stream().filter(each -> "primary".equals(each.getAlgorithmRole())).toList();
    }
    
    private void bindFeatureArguments(final WorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ALGORITHM_TYPE));
        request.getPrimaryAlgorithmProperties().putAll(workflowPlanningArguments.getAlgorithmPropertyMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES, "primary"));
        request.getPrimaryAlgorithmSecretReferences().putAll(workflowPlanningArguments.getSecretReferenceMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES));
    }
    
}
