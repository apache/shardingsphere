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
                    new MCPToolFieldDefinition("intent_type", new MCPToolValueDefinition(Type.STRING, "Workflow intent type: encrypt or mask.", null), false),
                    new MCPToolFieldDefinition("operation_type", new MCPToolValueDefinition(Type.STRING, "Lifecycle operation: create, alter or drop.", null), false),
                    new MCPToolFieldDefinition("natural_language_intent", new MCPToolValueDefinition(Type.STRING, "Natural-language request used for intent clarification.", null), false),
                    new MCPToolFieldDefinition("delivery_mode", new MCPToolValueDefinition(Type.STRING, "Delivery mode: all-at-once or step-by-step.", null), false),
                    new MCPToolFieldDefinition("execution_mode", new MCPToolValueDefinition(Type.STRING, "Execution mode: review-then-execute or manual-only.", null), false),
                    new MCPToolFieldDefinition("allow_sample_data", new MCPToolValueDefinition(Type.BOOLEAN, "Whether sample-data inspection is allowed.", null), false),
                    new MCPToolFieldDefinition("allow_index_ddl", new MCPToolValueDefinition(Type.BOOLEAN, "Whether index DDL may be auto-generated.", null), false),
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
        request.setIntentType(toolArguments.getStringArgument("intent_type"));
        request.setOperationType(toolArguments.getStringArgument("operation_type"));
        request.setNaturalLanguageIntent(toolArguments.getStringArgument("natural_language_intent"));
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
        request.getPrimaryAlgorithmProperties().putAll(toolArguments.getMapArgument("primary_algorithm_properties"));
        request.getAssistedQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("assisted_query_algorithm_properties"));
        request.getLikeQueryAlgorithmProperties().putAll(toolArguments.getMapArgument("like_query_algorithm_properties"));
        WorkflowContextSnapshot snapshot = planningService.plan(runtimeContext, sessionId, request);
        return new MCPMapResponse(new WorkflowToolResponseBuilder(propertyTemplateService).buildPlanResponse(snapshot));
    }
}
