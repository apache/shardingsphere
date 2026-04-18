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

package org.apache.shardingsphere.mcp.tool.handler.workflow;

import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;
import org.apache.shardingsphere.mcp.tool.service.workflow.AlgorithmPropertyTemplateService;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowPlanningService;

import java.util.Arrays;
import java.util.Map;

/**
 * Tool handler for encrypt and mask workflow planning.
 */
public final class PlanEncryptMaskRuleToolHandler implements ToolHandler {
    
    private static final MCPToolDescriptor TOOL_DESCRIPTOR = new MCPToolDescriptor("plan_encrypt_mask_rule",
            Arrays.asList(
                    new MCPToolFieldDefinition("plan_id", new MCPToolValueDefinition(Type.STRING, "Optional existing workflow plan identifier.", null), false),
                    new MCPToolFieldDefinition("database", new MCPToolValueDefinition(Type.STRING, "Logical database name.", null), false),
                    new MCPToolFieldDefinition("schema", new MCPToolValueDefinition(Type.STRING, "Optional logical schema name.", null), false),
                    new MCPToolFieldDefinition("table", new MCPToolValueDefinition(Type.STRING, "Target logical table name.", null), false),
                    new MCPToolFieldDefinition("column", new MCPToolValueDefinition(Type.STRING, "Target logical column name.", null), false),
                    new MCPToolFieldDefinition("feature_type", new MCPToolValueDefinition(Type.STRING, "Workflow feature type: encrypt or mask.", null), false),
                    new MCPToolFieldDefinition("operation_type", new MCPToolValueDefinition(Type.STRING, "Lifecycle operation: create, alter or drop.", null), false),
                    new MCPToolFieldDefinition("raw_user_request", new MCPToolValueDefinition(Type.STRING, "Original user request used as supplemental context.", null), false),
                    new MCPToolFieldDefinition("natural_language_intent", new MCPToolValueDefinition(Type.STRING, "Natural-language request used for intent clarification.", null), false),
                    new MCPToolFieldDefinition("structured_intent_evidence", new MCPToolValueDefinition(Type.OBJECT, "Structured intent evidence extracted by the caller.", null), false),
                    new MCPToolFieldDefinition("delivery_mode", new MCPToolValueDefinition(Type.STRING, "Delivery mode: all-at-once or step-by-step.", null), false),
                    new MCPToolFieldDefinition("execution_mode", new MCPToolValueDefinition(Type.STRING, "Execution mode: auto-execute, review-then-execute or manual-only.", null), false),
                    new MCPToolFieldDefinition("allow_sample_data", new MCPToolValueDefinition(Type.BOOLEAN, "Whether sample-data inspection is allowed.", null), false),
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
    
    private final WorkflowPlanningService planningService;
    
    private final AlgorithmPropertyTemplateService propertyTemplateService;
    
    public PlanEncryptMaskRuleToolHandler() {
        this(new WorkflowPlanningService(), new AlgorithmPropertyTemplateService());
    }
    
    PlanEncryptMaskRuleToolHandler(final WorkflowPlanningService planningService, final AlgorithmPropertyTemplateService propertyTemplateService) {
        this.planningService = planningService;
        this.propertyTemplateService = propertyTemplateService;
    }
    
    @Override
    public MCPToolDescriptor getToolDescriptor() {
        return TOOL_DESCRIPTOR;
    }
    
    @Override
    public MCPResponse handle(final MCPRuntimeContext runtimeContext, final String sessionId, final Map<String, Object> arguments) {
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        WorkflowRequest request = new WorkflowRequest();
        request.setPlanId(toolArguments.getStringArgument("plan_id"));
        request.setDatabase(toolArguments.getStringArgument("database"));
        request.setSchema(toolArguments.getStringArgument("schema"));
        request.setTable(toolArguments.getStringArgument("table"));
        request.setColumn(toolArguments.getStringArgument("column"));
        request.setFeatureType(resolveFeatureType(toolArguments));
        request.setOperationType(toolArguments.getStringArgument("operation_type"));
        request.setRawUserRequest(resolveRawUserRequest(toolArguments));
        applyStructuredIntentEvidence(request, arguments.get("structured_intent_evidence"));
        request.setDeliveryMode(toolArguments.getStringArgument("delivery_mode"));
        request.setExecutionMode(toolArguments.getStringArgument("execution_mode"));
        if (arguments.containsKey("allow_sample_data")) {
            request.setAllowSampleData(toolArguments.getBooleanArgument("allow_sample_data", false));
        }
        if (arguments.containsKey("allow_index_ddl")) {
            request.setAllowIndexDDL(toolArguments.getBooleanArgument("allow_index_ddl", true));
        }
        request.setAlgorithmType(toolArguments.getStringArgument("algorithm_type"));
        request.setAssistedQueryAlgorithmType(toolArguments.getStringArgument("assisted_query_algorithm_type"));
        request.setLikeQueryAlgorithmType(toolArguments.getStringArgument("like_query_algorithm_type"));
        request.setCipherColumnName(toolArguments.getStringArgument("cipher_column_name"));
        request.setAssistedQueryColumnName(toolArguments.getStringArgument("assisted_query_column_name"));
        request.setLikeQueryColumnName(toolArguments.getStringArgument("like_query_column_name"));
        applyUserOverrides(request, arguments.get("user_overrides"));
        request.getPrimaryAlgorithmProperties().putAll(toolArguments.getMapArgument("primary_algorithm_properties"));
        request.getAssistedQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("assisted_query_algorithm_properties"));
        request.getLikeQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("like_query_algorithm_properties"));
        WorkflowContextSnapshot snapshot = planningService.plan(runtimeContext, sessionId, request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
    
    private String resolveFeatureType(final MCPToolArguments toolArguments) {
        String actualFeatureType = toolArguments.getStringArgument("feature_type");
        return actualFeatureType.isEmpty() ? toolArguments.getStringArgument("intent_type") : actualFeatureType;
    }
    
    private String resolveRawUserRequest(final MCPToolArguments toolArguments) {
        String actualRawUserRequest = toolArguments.getStringArgument("raw_user_request");
        return actualRawUserRequest.isEmpty() ? toolArguments.getStringArgument("natural_language_intent") : actualRawUserRequest;
    }
    
    @SuppressWarnings("unchecked")
    private void applyStructuredIntentEvidence(final WorkflowRequest request, final Object rawEvidence) {
        if (!(rawEvidence instanceof Map)) {
            return;
        }
        Map<String, Object> structuredIntentEvidence = (Map<String, Object>) rawEvidence;
        request.setRequiresDecrypt(getNullableBoolean(structuredIntentEvidence, "requires_decrypt"));
        request.setRequiresEqualityFilter(getNullableBoolean(structuredIntentEvidence, "requires_equality_filter"));
        request.setRequiresLikeQuery(getNullableBoolean(structuredIntentEvidence, "requires_like_query"));
        Object fieldSemantics = structuredIntentEvidence.get("field_semantics");
        if (null != fieldSemantics) {
            request.setFieldSemantics(String.valueOf(fieldSemantics).trim());
        }
    }
    
    @SuppressWarnings("unchecked")
    private void applyUserOverrides(final WorkflowRequest request, final Object rawOverrides) {
        if (!(rawOverrides instanceof Map)) {
            return;
        }
        Map<String, Object> userOverrides = (Map<String, Object>) rawOverrides;
        request.setAlgorithmType(resolveOverrideValue(request.getAlgorithmType(), userOverrides.get("algorithm_type")));
        request.setAssistedQueryAlgorithmType(resolveOverrideValue(request.getAssistedQueryAlgorithmType(), userOverrides.get("assisted_query_algorithm_type")));
        request.setLikeQueryAlgorithmType(resolveOverrideValue(request.getLikeQueryAlgorithmType(), userOverrides.get("like_query_algorithm_type")));
        request.setCipherColumnName(resolveOverrideValue(request.getCipherColumnName(), userOverrides.get("cipher_column_name")));
        request.setAssistedQueryColumnName(resolveOverrideValue(request.getAssistedQueryColumnName(), userOverrides.get("assisted_query_column_name")));
        request.setLikeQueryColumnName(resolveOverrideValue(request.getLikeQueryColumnName(), userOverrides.get("like_query_column_name")));
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
