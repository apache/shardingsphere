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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.handler;

import org.apache.shardingsphere.mcp.api.protocol.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.workflow.MCPWorkflowRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.Map;

/**
 * Tool handler for encrypt workflow planning.
 */
public final class PlanEncryptRuleToolHandler implements MCPToolHandler<MCPWorkflowRequestContext> {
    
    private final EncryptWorkflowPlanningService planningService = new EncryptWorkflowPlanningService();
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService = new EncryptAlgorithmPropertyTemplateService();
    
    @Override
    public Class<MCPWorkflowRequestContext> getContextType() {
        return MCPWorkflowRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return EncryptFeatureDefinition.PLAN_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPWorkflowRequestContext workflowContext, final Map<String, Object> arguments) {
        EncryptWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(EncryptWorkflowRequest::new, arguments,
                this::bindFeatureArguments, this::applyStructuredIntentEvidence);
        WorkflowContextSnapshot snapshot = planningService.plan(workflowContext.getWorkflowSessionContext(), workflowContext.getMetadataQueryFacade(),
                workflowContext.getQueryFacade(), request);
        return new MCPMapPayload(new EncryptWorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final EncryptWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ALGORITHM_TYPE));
        request.getOptions().setAssistedQueryAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_TYPE));
        request.getOptions().setLikeQueryAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.LIKE_QUERY_ALGORITHM_TYPE));
        request.getOptions().setCipherColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.CIPHER_COLUMN_NAME));
        request.getOptions().setAssistedQueryColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ASSISTED_QUERY_COLUMN_NAME));
        request.getOptions().setLikeQueryColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.LIKE_QUERY_COLUMN_NAME));
        request.getPrimaryAlgorithmProperties().putAll(
                workflowPlanningArguments.getAlgorithmPropertyMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES, EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY));
        request.getPrimaryAlgorithmSecretReferences().putAll(workflowPlanningArguments.getSecretReferenceMapArgument(WorkflowFieldNames.PRIMARY_ALGORITHM_PROPERTIES));
        request.getOptions().getAssistedQueryAlgorithmProperties().putAll(workflowPlanningArguments.getAlgorithmPropertyMapArgument(
                WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_PROPERTIES, EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY));
        request.getOptions().getAssistedQueryAlgorithmSecretReferences().putAll(workflowPlanningArguments.getSecretReferenceMapArgument(WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_PROPERTIES));
        request.getOptions().getLikeQueryAlgorithmProperties().putAll(workflowPlanningArguments.getAlgorithmPropertyMapArgument(
                WorkflowFieldNames.LIKE_QUERY_ALGORITHM_PROPERTIES, EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY));
        request.getOptions().getLikeQueryAlgorithmSecretReferences().putAll(workflowPlanningArguments.getSecretReferenceMapArgument(WorkflowFieldNames.LIKE_QUERY_ALGORITHM_PROPERTIES));
    }
    
    private void applyStructuredIntentEvidence(final EncryptWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        request.getOptions().setRequiresDecrypt(getNullableBoolean(structuredIntentEvidence, WorkflowFieldNames.REQUIRES_DECRYPT));
        request.getOptions().setRequiresEqualityFilter(getNullableBoolean(structuredIntentEvidence, WorkflowFieldNames.REQUIRES_EQUALITY_FILTER));
        request.getOptions().setRequiresLikeQuery(getNullableBoolean(structuredIntentEvidence, WorkflowFieldNames.REQUIRES_LIKE_QUERY));
        Object fieldSemantics = structuredIntentEvidence.get(WorkflowFieldNames.FIELD_SEMANTICS);
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
    
    private Boolean getNullableBoolean(final Map<String, Object> source, final String fieldName) {
        if (!source.containsKey(fieldName) || null == source.get(fieldName)) {
            return null;
        }
        Object rawValue = source.get(fieldName);
        return rawValue instanceof Boolean ? (Boolean) rawValue : Boolean.parseBoolean(String.valueOf(rawValue).trim());
    }
}
