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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.feature.shadow.ShadowFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.shadow.tool.model.ShadowRuleWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.shadow.tool.service.ShadowWorkflowPlanningService;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Tool handler for shadow rule workflow planning.
 */
public final class PlanShadowRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final ShadowWorkflowPlanningService planningService = new ShadowWorkflowPlanningService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return ShadowFeatureDefinition.PLAN_RULE_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        ShadowRuleWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ShadowRuleWorkflowRequest::new, arguments,
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.planRule(requestContext.getWorkflowSessionContext(), requestContext.getQueryFacade(), request);
        return new MCPMapPayload(WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, snapshot.getRequest()));
    }
    
    private void bindFeatureArguments(final ShadowRuleWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.RULE_FIELD, request::setRuleName);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD, request::setSourceStorageUnit);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD, request::setShadowStorageUnit);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.TABLE_FIELD, request::setTableName);
        workflowPlanningArguments.applyStringArgument(ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, request::setAlgorithmType);
        request.putAlgorithmProperties(workflowPlanningArguments.getMapArgument(ShadowFeatureDefinition.ALGORITHM_PROPERTIES_FIELD));
    }
    
    private void applyStructuredIntentEvidence(final ShadowRuleWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.RULE_FIELD, request::setRuleName);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.SOURCE_STORAGE_UNIT_FIELD, request::setSourceStorageUnit);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.SHADOW_STORAGE_UNIT_FIELD, request::setShadowStorageUnit);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.TABLE_FIELD, request::setTableName);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ShadowFeatureDefinition.ALGORITHM_TYPE_FIELD, request::setAlgorithmType);
        applyMapField(structuredIntentEvidence, request);
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
