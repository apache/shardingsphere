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

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Tool handler for readwrite-splitting storage-unit status workflow planning.
 */
public final class PlanReadwriteSplittingStatusToolHandler implements MCPToolHandler<MCPWorkflowHandlerContext> {
    
    private final ReadwriteSplittingStatusWorkflowPlanningService planningService = new ReadwriteSplittingStatusWorkflowPlanningService();
    
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
        ReadwriteSplittingStatusWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(ReadwriteSplittingStatusWorkflowRequest::new, toolCall.getArguments(),
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        request.setOperationType("");
        WorkflowContextSnapshot snapshot = planningService.plan(
                workflowContext.getWorkflowSessionContext(), workflowContext.getDatabaseContext().getQueryFacade(), toolCall.getSessionId(), request);
        return new MCPMapResponse(createPlanResponse(snapshot));
    }
    
    private Map<String, Object> createPlanResponse(final WorkflowContextSnapshot snapshot) {
        Map<String, Object> result = WorkflowPlanPayloadBuilder.buildRuleDistSQLOnly(snapshot, snapshot.getRequest());
        if (snapshot.getRequest() instanceof final ReadwriteSplittingStatusWorkflowRequest request) {
            replaceIntentInference(result, request);
            replaceArgumentProvenance(result, request);
            replaceDistSQLArtifacts(result);
        }
        return result;
    }
    
    private void replaceIntentInference(final Map<String, Object> result, final ReadwriteSplittingStatusWorkflowRequest request) {
        Object value = result.get("intent_inference");
        if (!(value instanceof Map<?, ?>)) {
            return;
        }
        Map<String, Object> intentInference = copyFields(value);
        intentInference.remove(WorkflowFieldNames.OPERATION_TYPE);
        if (!request.getTargetStatus().isEmpty()) {
            intentInference.put(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, request.getTargetStatus());
        }
        intentInference.put("inferred_values", copyStatusFields(intentInference.get("inferred_values")));
        result.put("intent_inference", intentInference);
    }
    
    private void replaceArgumentProvenance(final Map<String, Object> result, final ReadwriteSplittingStatusWorkflowRequest request) {
        Object value = result.get("argument_provenance");
        if (!(value instanceof Map<?, ?>)) {
            return;
        }
        Map<String, Object> argumentProvenance = copyFields(value);
        argumentProvenance.remove(WorkflowFieldNames.OPERATION_TYPE);
        if (!request.getTargetStatus().isEmpty()) {
            argumentProvenance.put(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, isInferredTargetStatus(result) ? "inferred_from_intent" : "user_provided");
        }
        result.put("argument_provenance", argumentProvenance);
    }
    
    private boolean isInferredTargetStatus(final Map<String, Object> result) {
        Object intentInference = result.get("intent_inference");
        if (!(intentInference instanceof Map<?, ?>)) {
            return false;
        }
        Object inferredValues = ((Map<?, ?>) intentInference).get("inferred_values");
        return inferredValues instanceof Map<?, ?> && ((Map<?, ?>) inferredValues).containsKey(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD);
    }
    
    private void replaceDistSQLArtifacts(final Map<String, Object> result) {
        Object value = result.get("distsql_artifacts");
        if (!(value instanceof List<?>)) {
            return;
        }
        List<Object> artifacts = new LinkedList<>();
        for (Object each : (List<?>) value) {
            artifacts.add(each instanceof Map<?, ?> ? copyStatusFields(each) : each);
        }
        result.put("distsql_artifacts", artifacts);
    }
    
    private Map<String, Object> copyStatusFields(final Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?>) {
            ((Map<?, ?>) value).forEach((key, mapValue) -> result.put(resolveStatusFieldName(key), mapValue));
        }
        return result;
    }
    
    private Map<String, Object> copyFields(final Object value) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (value instanceof Map<?, ?>) {
            ((Map<?, ?>) value).forEach((key, mapValue) -> result.put(String.valueOf(key), mapValue));
        }
        return result;
    }
    
    private String resolveStatusFieldName(final Object fieldName) {
        return WorkflowFieldNames.OPERATION_TYPE.equals(String.valueOf(fieldName)) ? ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD : String.valueOf(fieldName);
    }
    
    private void bindFeatureArguments(final ReadwriteSplittingStatusWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, request::setStorageUnit);
        workflowPlanningArguments.applyStringArgument(ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, request::setTargetStatus);
    }
    
    private void applyStructuredIntentEvidence(final ReadwriteSplittingStatusWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.RULE_FIELD, request::setRuleName);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.STORAGE_UNIT_FIELD, request::setStorageUnit);
        WorkflowRequestBinder.applyStringField(structuredIntentEvidence, ReadwriteSplittingFeatureDefinition.TARGET_STATUS_FIELD, request::setTargetStatus);
    }
    
}
