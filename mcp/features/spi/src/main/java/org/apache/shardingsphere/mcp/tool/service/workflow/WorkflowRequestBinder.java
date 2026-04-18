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

package org.apache.shardingsphere.mcp.tool.service.workflow;

import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;
import org.apache.shardingsphere.mcp.tool.request.MCPToolArguments;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * Workflow request binder.
 */
public final class WorkflowRequestBinder {
    
    private WorkflowRequestBinder() {
    }
    
    /**
     * Bind workflow planning request from MCP arguments.
     *
     * @param arguments raw MCP arguments
     * @param featureArgumentBinder feature-specific argument binder
     * @param structuredIntentBinder structured intent binder
     * @param userOverrideBinder user override binder
     * @return bound workflow request
     */
    public static WorkflowRequest bindPlanningRequest(final Map<String, Object> arguments,
                                                      final BiConsumer<WorkflowRequest, MCPToolArguments> featureArgumentBinder,
                                                      final BiConsumer<WorkflowRequest, Map<String, Object>> structuredIntentBinder,
                                                      final BiConsumer<WorkflowRequest, Map<String, Object>> userOverrideBinder) {
        Objects.requireNonNull(arguments);
        Objects.requireNonNull(featureArgumentBinder);
        Objects.requireNonNull(structuredIntentBinder);
        Objects.requireNonNull(userOverrideBinder);
        MCPToolArguments toolArguments = new MCPToolArguments(arguments);
        WorkflowRequest result = new WorkflowRequest();
        bindCommonPlanningFields(result, toolArguments);
        applyObjectMap(arguments.get("structured_intent_evidence"), actualValue -> structuredIntentBinder.accept(result, actualValue));
        featureArgumentBinder.accept(result, toolArguments);
        applyObjectMap(arguments.get("user_overrides"), actualValue -> userOverrideBinder.accept(result, actualValue));
        return result;
    }
    
    private static void bindCommonPlanningFields(final WorkflowRequest request, final MCPToolArguments toolArguments) {
        request.setPlanId(toolArguments.getStringArgument("plan_id"));
        request.setDatabase(toolArguments.getStringArgument("database"));
        request.setSchema(toolArguments.getStringArgument("schema"));
        request.setTable(toolArguments.getStringArgument("table"));
        request.setColumn(toolArguments.getStringArgument("column"));
        request.setOperationType(toolArguments.getStringArgument("operation_type"));
        request.setNaturalLanguageIntent(toolArguments.getStringArgument("natural_language_intent"));
        request.setDeliveryMode(toolArguments.getStringArgument("delivery_mode"));
        request.setExecutionMode(toolArguments.getStringArgument("execution_mode"));
    }
    
    @SuppressWarnings("unchecked")
    private static void applyObjectMap(final Object rawValue, final Consumer<Map<String, Object>> consumer) {
        if (rawValue instanceof Map) {
            consumer.accept((Map<String, Object>) rawValue);
        }
    }
}
