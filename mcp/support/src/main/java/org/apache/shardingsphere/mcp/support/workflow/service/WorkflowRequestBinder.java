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

package org.apache.shardingsphere.mcp.support.workflow.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Workflow request binder.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowRequestBinder {
    
    /**
     * Bind workflow planning request from MCP arguments.
     *
     * @param requestSupplier request supplier
     * @param arguments raw MCP arguments
     * @param featureArgumentBinder feature-specific argument binder
     * @param structuredIntentBinder structured intent binder
     * @param <T> request type
     * @return bound workflow request
     */
    public static <T extends WorkflowRequest> T bindPlanningRequest(final Supplier<T> requestSupplier, final Map<String, Object> arguments,
                                                                    final BiConsumer<T, WorkflowPlanningArguments> featureArgumentBinder,
                                                                    final BiConsumer<T, Map<String, Object>> structuredIntentBinder) {
        WorkflowPlanningArguments workflowPlanningArguments = new WorkflowPlanningArguments(arguments);
        T result = requestSupplier.get();
        bindCommonPlanningFields(result, workflowPlanningArguments);
        applyObjectMap(arguments.get(WorkflowFieldNames.STRUCTURED_INTENT_EVIDENCE), WorkflowFieldNames.STRUCTURED_INTENT_EVIDENCE, actualValue -> structuredIntentBinder.accept(result, actualValue));
        featureArgumentBinder.accept(result, workflowPlanningArguments);
        return result;
    }
    
    /**
     * Bind workflow planning request from MCP arguments.
     *
     * @param arguments raw MCP arguments
     * @param featureArgumentBinder feature-specific argument binder
     * @param structuredIntentBinder structured intent binder
     * @return bound workflow request
     */
    public static WorkflowRequest bindPlanningRequest(final Map<String, Object> arguments,
                                                      final BiConsumer<WorkflowRequest, WorkflowPlanningArguments> featureArgumentBinder,
                                                      final BiConsumer<WorkflowRequest, Map<String, Object>> structuredIntentBinder) {
        return bindPlanningRequest(WorkflowRequest::new, arguments, featureArgumentBinder, structuredIntentBinder);
    }
    
    private static void bindCommonPlanningFields(final WorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setPlanId(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.PLAN_ID));
        request.setDatabase(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.DATABASE));
        request.setSchema(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.SCHEMA));
        request.setTable(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.TABLE));
        request.setColumn(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.COLUMN));
        request.setOperationType(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.OPERATION_TYPE));
        request.setNaturalLanguageIntent(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.NATURAL_LANGUAGE_INTENT));
        request.setDeliveryMode(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.DELIVERY_MODE));
        request.setExecutionMode(workflowPlanningArguments.getStringArgument(WorkflowFieldNames.EXECUTION_MODE));
    }
    
    private static void applyObjectMap(final Object rawValue, final String name, final Consumer<Map<String, Object>> consumer) {
        Map<String, Object> actualValue = getObjectMap(rawValue, name);
        if (!actualValue.isEmpty()) {
            consumer.accept(actualValue);
        }
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> getObjectMap(final Object rawValue, final String name) {
        if (null == rawValue) {
            return Map.of();
        }
        if (rawValue instanceof Map) {
            return (Map<String, Object>) rawValue;
        }
        throw new MCPInvalidRequestException(String.format("%s must be an object.", name));
    }
    
}
