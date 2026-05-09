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

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Workflow request binder.
 */
public final class WorkflowRequestBinder {

    private WorkflowRequestBinder() {
    }

    /**
     * Bind workflow planning request from MCP arguments.
     *
     * @param requestSupplier request supplier
     * @param arguments raw MCP arguments
     * @param featureArgumentBinder feature-specific argument binder
     * @param structuredIntentBinder structured intent binder
     * @param userOverrideBinder user override binder
     * @param <T> request type
     * @return bound workflow request
     */
    public static <T extends WorkflowRequest> T bindPlanningRequest(final Supplier<T> requestSupplier, final Map<String, Object> arguments,
                                                                    final BiConsumer<T, WorkflowPlanningArguments> featureArgumentBinder,
                                                                    final BiConsumer<T, Map<String, Object>> structuredIntentBinder,
                                                                    final BiConsumer<T, Map<String, Object>> userOverrideBinder) {
        WorkflowPlanningArguments workflowPlanningArguments = new WorkflowPlanningArguments(arguments);
        T result = requestSupplier.get();
        bindCommonPlanningFields(result, workflowPlanningArguments);
        applyObjectMap(arguments.get("structured_intent_evidence"), actualValue -> structuredIntentBinder.accept(result, actualValue));
        featureArgumentBinder.accept(result, workflowPlanningArguments);
        Map<String, Object> userOverrides = getObjectMap(arguments.get("user_overrides"));
        validateUserOverrideConflicts(arguments, userOverrides);
        if (!userOverrides.isEmpty()) {
            userOverrideBinder.accept(result, userOverrides);
        }
        return result;
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
                                                      final BiConsumer<WorkflowRequest, WorkflowPlanningArguments> featureArgumentBinder,
                                                      final BiConsumer<WorkflowRequest, Map<String, Object>> structuredIntentBinder,
                                                      final BiConsumer<WorkflowRequest, Map<String, Object>> userOverrideBinder) {
        return bindPlanningRequest(WorkflowRequest::new, arguments, featureArgumentBinder, structuredIntentBinder, userOverrideBinder);
    }

    private static void bindCommonPlanningFields(final WorkflowRequest request, final WorkflowPlanningArguments workflowPlanningArguments) {
        request.setPlanId(normalizePlanId(workflowPlanningArguments.getStringArgument("plan_id")));
        request.setDatabase(workflowPlanningArguments.getStringArgument("database"));
        request.setSchema(workflowPlanningArguments.getStringArgument("schema"));
        request.setTable(workflowPlanningArguments.getStringArgument("table"));
        request.setColumn(workflowPlanningArguments.getStringArgument("column"));
        request.setOperationType(workflowPlanningArguments.getStringArgument("operation_type"));
        request.setNaturalLanguageIntent(workflowPlanningArguments.getStringArgument("natural_language_intent"));
        request.setDeliveryMode(workflowPlanningArguments.getStringArgument("delivery_mode"));
        request.setExecutionMode(workflowPlanningArguments.getStringArgument("execution_mode"));
    }

    private static String normalizePlanId(final String planId) {
        String result = planId.trim();
        return "plan_id".equals(result) || "{plan_id}".equals(result) || "<plan_id>".equals(result) ? "" : result;
    }

    private static void applyObjectMap(final Object rawValue, final Consumer<Map<String, Object>> consumer) {
        Map<String, Object> actualValue = getObjectMap(rawValue);
        if (!actualValue.isEmpty()) {
            consumer.accept(actualValue);
        }
    }

    @SuppressWarnings("unchecked")
    private static Map<String, Object> getObjectMap(final Object rawValue) {
        return rawValue instanceof Map ? (Map<String, Object>) rawValue : Map.of();
    }

    private static void validateUserOverrideConflicts(final Map<String, Object> arguments, final Map<String, Object> userOverrides) {
        if (userOverrides.isEmpty()) {
            return;
        }
        List<String> conflicts = new LinkedList<>();
        for (Entry<String, Object> entry : userOverrides.entrySet()) {
            addUserOverrideConflict(conflicts, arguments, entry);
        }
        if (!conflicts.isEmpty()) {
            throw new WorkflowArgumentConflictException(conflicts);
        }
    }

    private static void addUserOverrideConflict(final Collection<String> conflicts, final Map<String, Object> arguments, final Entry<String, Object> entry) {
        if (!arguments.containsKey(entry.getKey())) {
            return;
        }
        String actualValue = normalizeComparableValue(arguments.get(entry.getKey()));
        String overrideValue = normalizeComparableValue(entry.getValue());
        if (!actualValue.isEmpty() && !overrideValue.isEmpty() && !actualValue.equals(overrideValue)) {
            conflicts.add(String.format("%s conflicts with user_overrides.%s", entry.getKey(), entry.getKey()));
        }
    }

    private static String normalizeComparableValue(final Object value) {
        return null == value ? "" : String.valueOf(value).trim();
    }
}
