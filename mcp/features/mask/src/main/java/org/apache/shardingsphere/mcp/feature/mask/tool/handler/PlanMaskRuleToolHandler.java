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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.mask.tool.service.MaskWorkflowPlanningService;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.descriptor.WorkflowPlanningToolDescriptorFactory;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRequestBinder;

import java.util.List;
import java.util.Map;

/**
 * Tool handler for mask workflow planning.
 */
public final class PlanMaskRuleToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = WorkflowPlanningToolDescriptorFactory.create("plan_mask_rule",
            List.of(
                    new MCPToolFieldDefinition("user_overrides", new MCPToolValueDefinition(Type.OBJECT, "Optional user overrides for mask algorithm fields.", null), false),
                    new MCPToolFieldDefinition("algorithm_type", new MCPToolValueDefinition(Type.STRING, "Primary mask algorithm type override.", null), false),
                    new MCPToolFieldDefinition("primary_algorithm_properties", new MCPToolValueDefinition(Type.OBJECT, "Primary algorithm properties.", null), false)));
    
    private final MaskWorkflowPlanningService planningService;
    
    private final MaskAlgorithmPropertyTemplateService propertyTemplateService;
    
    public PlanMaskRuleToolHandler() {
        this(new MaskWorkflowPlanningService(), new MaskAlgorithmPropertyTemplateService());
    }
    
    PlanMaskRuleToolHandler(final MaskWorkflowPlanningService planningService, final MaskAlgorithmPropertyTemplateService propertyTemplateService) {
        this.planningService = planningService;
        this.propertyTemplateService = propertyTemplateService;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        WorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(arguments, this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyUserOverrides);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext, sessionId, request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final WorkflowRequest request, final MCPToolArguments toolArguments) {
        request.setAlgorithmType(toolArguments.getStringArgument("algorithm_type"));
        request.getPrimaryAlgorithmProperties().putAll(toolArguments.getMapArgument("primary_algorithm_properties"));
    }
    
    private void applyStructuredIntentEvidence(final WorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        Object fieldSemantics = structuredIntentEvidence.get("field_semantics");
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
    
    private void applyUserOverrides(final WorkflowRequest request, final Map<String, Object> userOverrides) {
        Object algorithmType = userOverrides.get("algorithm_type");
        if (null != algorithmType && !String.valueOf(algorithmType).trim().isEmpty()) {
            request.setAlgorithmType(String.valueOf(algorithmType).trim());
        }
    }
}
