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
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowHandlerContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.Consumer;

/**
 * Tool handler for shadow rule workflow planning.
 */
public final class PlanShadowRuleToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ShadowWorkflowPlanningService planningService;
    
    public PlanShadowRuleToolHandler() {
        planningService = new ShadowWorkflowPlanningService();
    }
    
    PlanShadowRuleToolHandler(final ShadowWorkflowPlanningService planningService) {
        this.planningService = planningService;
    }
    
    @Override
    public Class<MCPWorkflowHandlerContext> getContextType() {
        return MCPWorkflowHandlerContext.class;
    }
    
    @Override
    public String getToolName() {
        return ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME;
    }
    
    @Override
    public MCPResponse handle(final MCPWorkflowHandlerContext workflowContext, final MCPToolCall toolCall) {
        ShadowRuleWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ShadowRuleWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyUserOverrides);
        WorkflowContextSnapshot snapshot = planningService.planRule(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder().buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final ShadowRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD, request::setSourceStorageUnit);
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD, request::setShadowStorageUnit);
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.TABLE_FIELD, request::setTableName);
        applyStringArgument(workflowPlanningArguments, ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, request::setAlgorithmType);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument(ShadowFeatureDefinition.ALGORITHM_PROPERTIES_FIELD));
    }
    
    private void applyStructuredIntentEvidence(final ShadowRuleWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.RULE_FIELD, request::setRuleName);
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD, request::setSourceStorageUnit);
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD, request::setShadowStorageUnit);
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.TABLE_FIELD, request::setTableName);
        applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, request::setAlgorithmType);
        applyMapField(structuredIntentEvidence, request);
    }
    
    private void applyUserOverrides(final ShadowRuleWorkflowRequest request, final Map<String, Object> userOverrides) {
        applyStructuredIntentEvidence(request, userOverrides);
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
    
    private void applyMapField(final Map<String, Object> values, final ShadowRuleWorkflowRequest request) {
        Object value = values.get(ShadowFeatureDefinition.ALGORITHM_PROPERTIES_FIELD);
        if (value instanceof Map) {
            request.putAlgorithmProperties(createStringMap((Map<?, ?>) value));
        }
    }
    
    private Map<String, String> createStringMap(final Map<?, ?> values) {
        Map<String, String> result = new LinkedHashMap<>(values.size(), 1F);
        for (Entry<?, ?> entry : values.entrySet()) {
            result.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return result;
    }
}
