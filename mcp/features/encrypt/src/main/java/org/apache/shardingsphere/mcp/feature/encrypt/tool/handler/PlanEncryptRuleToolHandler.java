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

import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.apache.shardingsphere.mcp.support.MCPFeatureRequestContext;
import org.apache.shardingsphere.mcp.support.workflow.model.AlgorithmPropertyRequirement;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanningArguments;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowPlanPayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowRequestBinder;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Tool handler for encrypt workflow planning.
 */
public final class PlanEncryptRuleToolHandler implements MCPToolHandler<MCPFeatureRequestContext> {
    
    private final EncryptWorkflowPlanningService planningService = new EncryptWorkflowPlanningService();
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService = new EncryptAlgorithmPropertyTemplateService();
    
    @Override
    public Class<MCPFeatureRequestContext> getContextType() {
        return MCPFeatureRequestContext.class;
    }
    
    @Override
    public String getToolName() {
        return EncryptFeatureDefinition.PLAN_TOOL_NAME;
    }
    
    @Override
    public MCPSuccessPayload handle(final MCPFeatureRequestContext requestContext, final Map<String, Object> arguments) {
        EncryptWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(EncryptWorkflowRequest::new, arguments, this::bindFeatureArguments);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext.getWorkflowSessionContext(), requestContext.getMetadataQueryFacade(),
                requestContext.getQueryFacade(), request);
        return new MCPMapPayload(buildPlanResponse(snapshot));
    }
    
    private Map<String, Object> buildPlanResponse(final WorkflowContextSnapshot snapshot) {
        WorkflowRequest request = snapshot.getRequest();
        Map<String, Object> result = WorkflowPlanPayloadBuilder.buildWithArtifacts(snapshot, request);
        result.put("masked_property_preview", createMaskedPropertyPreview(snapshot, request));
        return result;
    }
    
    private Map<String, Object> createMaskedPropertyPreview(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        List<AlgorithmPropertyRequirement> requirements = createPropertyRequirements(snapshot, request);
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        putMaskedProperties(result, requirements, request, EncryptFeatureDefinition.ALGORITHM_ROLE_PRIMARY);
        putMaskedProperties(result, requirements, request, EncryptFeatureDefinition.ALGORITHM_ROLE_ASSISTED_QUERY);
        putMaskedProperties(result, requirements, request, EncryptFeatureDefinition.ALGORITHM_ROLE_LIKE_QUERY);
        return result;
    }
    
    private void putMaskedProperties(final Map<String, Object> target, final List<AlgorithmPropertyRequirement> requirements,
                                     final WorkflowRequest request, final String role) {
        target.put(role, propertyTemplateService.maskProperties(
                requirements.stream().filter(each -> role.equals(each.getAlgorithmRole())).toList(), request.getAlgorithmProperties(role)));
    }
    
    private List<AlgorithmPropertyRequirement> createPropertyRequirements(final WorkflowContextSnapshot snapshot, final WorkflowRequest request) {
        if (snapshot.getPropertyRequirements().isEmpty() && request instanceof EncryptWorkflowRequest encryptRequest) {
            return propertyTemplateService.findRequirements(encryptRequest.getAlgorithmType(), encryptRequest.getOptions().getAssistedQueryAlgorithmType(),
                    encryptRequest.getOptions().getLikeQueryAlgorithmType());
        }
        return snapshot.getPropertyRequirements();
    }
    
    private void bindFeatureArguments(final EncryptWorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ALGORITHM_TYPE));
        request.getOptions().setAssistedQueryAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ASSISTED_QUERY_ALGORITHM_TYPE));
        request.getOptions().setLikeQueryAlgorithmType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.LIKE_QUERY_ALGORITHM_TYPE));
        request.getOptions().setCipherColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.CIPHER_COLUMN_NAME));
        request.getOptions().setAssistedQueryColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.ASSISTED_QUERY_COLUMN_NAME));
        request.getOptions().setLikeQueryColumnName(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.LIKE_QUERY_COLUMN_NAME));
        request.getOptions().setRequiresDecrypt(workflowPlanningArguments.getBooleanArgument(WorkflowFieldNames.REQUIRES_DECRYPT));
        request.getOptions().setRequiresEqualityFilter(workflowPlanningArguments.getBooleanArgument(WorkflowFieldNames.REQUIRES_EQUALITY_FILTER));
        request.getOptions().setRequiresLikeQuery(workflowPlanningArguments.getBooleanArgument(WorkflowFieldNames.REQUIRES_LIKE_QUERY));
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
    
}
