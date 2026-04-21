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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.feature.encrypt.EncryptFeatureDefinition;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptAlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.feature.encrypt.tool.service.EncryptWorkflowPlanningService;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowRequestBinder;

import java.util.List;
import java.util.Map;

/**
 * Tool handler for encrypt workflow planning.
 */
public final class PlanEncryptRuleToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = WorkflowToolDescriptors.createPlanning(EncryptFeatureDefinition.PLAN_TOOL_NAME,
            List.of(
                    new MCPToolFieldDefinition("allow_index_ddl", new MCPToolValueDefinition(Type.BOOLEAN, "Whether index DDL may be auto-generated.", null), false),
                    new MCPToolFieldDefinition("user_overrides", new MCPToolValueDefinition(Type.OBJECT, "Optional user overrides for algorithm and naming fields.", null), false),
                    new MCPToolFieldDefinition("algorithm_type", new MCPToolValueDefinition(Type.STRING, "Primary algorithm type override.", null), false),
                    new MCPToolFieldDefinition("assisted_query_algorithm_type", new MCPToolValueDefinition(Type.STRING, "Assisted-query algorithm type override.", null), false),
                    new MCPToolFieldDefinition("like_query_algorithm_type", new MCPToolValueDefinition(Type.STRING, "LIKE-query algorithm type override.", null), false),
                    new MCPToolFieldDefinition("cipher_column_name", new MCPToolValueDefinition(Type.STRING, "Cipher column name override.", null), false),
                    new MCPToolFieldDefinition("assisted_query_column_name", new MCPToolValueDefinition(Type.STRING, "Assisted-query column name override.", null), false),
                    new MCPToolFieldDefinition("like_query_column_name", new MCPToolValueDefinition(Type.STRING, "LIKE-query column name override.", null), false),
                    new MCPToolFieldDefinition("primary_algorithm_properties", new MCPToolValueDefinition(Type.OBJECT, "Primary algorithm properties.", null), false),
                    new MCPToolFieldDefinition("assisted_query_algorithm_properties", new MCPToolValueDefinition(Type.OBJECT, "Assisted-query algorithm properties.", null), false),
                    new MCPToolFieldDefinition("like_query_algorithm_properties", new MCPToolValueDefinition(Type.OBJECT, "LIKE-query algorithm properties.", null), false)));
    
    private final EncryptWorkflowPlanningService planningService = new EncryptWorkflowPlanningService();
    
    private final EncryptAlgorithmPropertyTemplateService propertyTemplateService = new EncryptAlgorithmPropertyTemplateService();
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
        EncryptWorkflowRequest request = WorkflowRequestBinder.bindPlanningRequest(EncryptWorkflowRequest::new, arguments,
                this::bindFeatureArguments, this::applyStructuredIntentEvidence, this::applyUserOverrides);
        WorkflowContextSnapshot snapshot = planningService.plan(requestContext, sessionId, request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private void bindFeatureArguments(final EncryptWorkflowRequest request, final MCPToolArguments toolArguments) {
        String allowIndexDDL = toolArguments.getStringArgument("allow_index_ddl");
        if (!allowIndexDDL.isEmpty()) {
            request.getOptions().setAllowIndexDDL(toolArguments.getBooleanArgument("allow_index_ddl", true));
        }
        request.setAlgorithmType(toolArguments.getStringArgument("algorithm_type"));
        request.getOptions().setAssistedQueryAlgorithmType(toolArguments.getStringArgument("assisted_query_algorithm_type"));
        request.getOptions().setLikeQueryAlgorithmType(toolArguments.getStringArgument("like_query_algorithm_type"));
        request.getOptions().setCipherColumnName(toolArguments.getStringArgument("cipher_column_name"));
        request.getOptions().setAssistedQueryColumnName(toolArguments.getStringArgument("assisted_query_column_name"));
        request.getOptions().setLikeQueryColumnName(toolArguments.getStringArgument("like_query_column_name"));
        request.getPrimaryAlgorithmProperties().putAll(toolArguments.getMapArgument("primary_algorithm_properties"));
        request.getOptions().getAssistedQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("assisted_query_algorithm_properties"));
        request.getOptions().getLikeQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("like_query_algorithm_properties"));
    }
    
    private void applyStructuredIntentEvidence(final EncryptWorkflowRequest request, final Map<String, Object> structuredIntentEvidence) {
        request.getOptions().setRequiresDecrypt(getNullableBoolean(structuredIntentEvidence, "requires_decrypt"));
        request.getOptions().setRequiresEqualityFilter(getNullableBoolean(structuredIntentEvidence, "requires_equality_filter"));
        request.getOptions().setRequiresLikeQuery(getNullableBoolean(structuredIntentEvidence, "requires_like_query"));
        Object fieldSemantics = structuredIntentEvidence.get("field_semantics");
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
    
    private void applyUserOverrides(final EncryptWorkflowRequest request, final Map<String, Object> userOverrides) {
        request.setAlgorithmType(resolveOverrideValue(request.getAlgorithmType(), userOverrides.get("algorithm_type")));
        request.getOptions().setAssistedQueryAlgorithmType(resolveOverrideValue(request.getOptions().getAssistedQueryAlgorithmType(), userOverrides.get("assisted_query_algorithm_type")));
        request.getOptions().setLikeQueryAlgorithmType(resolveOverrideValue(request.getOptions().getLikeQueryAlgorithmType(), userOverrides.get("like_query_algorithm_type")));
        request.getOptions().setCipherColumnName(resolveOverrideValue(request.getOptions().getCipherColumnName(), userOverrides.get("cipher_column_name")));
        request.getOptions().setAssistedQueryColumnName(resolveOverrideValue(request.getOptions().getAssistedQueryColumnName(), userOverrides.get("assisted_query_column_name")));
        request.getOptions().setLikeQueryColumnName(resolveOverrideValue(request.getOptions().getLikeQueryColumnName(), userOverrides.get("like_query_column_name")));
    }
    
    private Boolean getNullableBoolean(final Map<String, Object> source, final String fieldName) {
        if (!source.containsKey(fieldName) || null == source.get(fieldName)) {
            return null;
        }
        Object rawValue = source.get(fieldName);
        return rawValue instanceof Boolean ? (Boolean) rawValue : Boolean.parseBoolean(String.valueOf(rawValue).trim());
    }
    
    private String resolveOverrideValue(final String currentValue, final Object rawValue) {
        if (null == rawValue) {
            return currentValue;
        }
        String actualValue = String.valueOf(rawValue).trim();
        return actualValue.isEmpty() ? currentValue : actualValue;
    }
}
