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

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.Locale;

/**
 * Shared workflow intent resolver support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowIntentResolverSupport {
    
    /**
     * Resolve workflow operation type from explicit fields and heuristics.
     *
     * @param request workflow request
     * @return resolved operation type
     */
    public static String resolveOperationType(final WorkflowRequest request) {
        return resolveOperationType(request, null);
    }
    
    /**
     * Resolve workflow operation type from explicit fields and heuristics.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @return resolved operation type
     */
    public static String resolveOperationType(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        String actualOperationType = WorkflowSqlUtils.trimToEmpty(request.getOperationType()).toLowerCase(Locale.ENGLISH);
        if (!actualOperationType.isEmpty()) {
            return actualOperationType;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        if (naturalLanguageIntent.contains("drop") || naturalLanguageIntent.contains("删除")) {
            return recordInferredValue(clarifiedIntent, "operation_type", WorkflowLifecycle.OPERATION_DROP);
        }
        if (naturalLanguageIntent.contains("alter") || naturalLanguageIntent.contains("修改") || naturalLanguageIntent.contains("补")) {
            return recordInferredValue(clarifiedIntent, "operation_type", "alter");
        }
        return recordInferredValue(clarifiedIntent, "operation_type", "create");
    }
    
    /**
     * Resolve field semantics from explicit fields and heuristics.
     *
     * @param request workflow request
     * @return resolved field semantics
     */
    public static String resolveFieldSemantics(final WorkflowRequest request) {
        return resolveFieldSemantics(request, null);
    }
    
    /**
     * Resolve field semantics from explicit fields and heuristics.
     *
     * @param request workflow request
     * @param clarifiedIntent clarified intent
     * @return resolved field semantics
     */
    public static String resolveFieldSemantics(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        String actualFieldSemantics = WorkflowSqlUtils.trimToEmpty(request.getFieldSemantics()).toLowerCase(Locale.ENGLISH);
        if (!actualFieldSemantics.isEmpty()) {
            return actualFieldSemantics;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        String columnName = WorkflowSqlUtils.trimToEmpty(request.getColumn()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("手机号") || columnName.contains("phone") || columnName.contains("mobile") || columnName.contains("tel")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "phone");
        }
        if (naturalLanguageIntent.contains("身份证") || columnName.contains("id_card")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "id_card");
        }
        return recordInferredValue(clarifiedIntent, "field_semantics", columnName);
    }
    
    /**
     * Create one reasoning summary for workflow intent resolution.
     *
     * @param clarifiedIntent clarified intent
     * @return reasoning summary
     */
    public static String summarizeReasoning(final ClarifiedIntent clarifiedIntent) {
        if (clarifiedIntent.getInferredValues().isEmpty() && clarifiedIntent.getUnresolvedFields().isEmpty()) {
            return "Resolved from explicit arguments.";
        }
        StringBuilder result = new StringBuilder("Resolved from explicit arguments");
        if (!clarifiedIntent.getInferredValues().isEmpty()) {
            result.append(", heuristic inference for ").append(String.join(", ", clarifiedIntent.getInferredValues().keySet()));
        }
        if (!clarifiedIntent.getUnresolvedFields().isEmpty()) {
            result.append(", unresolved fields: ").append(String.join(", ", clarifiedIntent.getUnresolvedFields()));
        }
        result.append('.');
        return result.toString();
    }
    
    private static String getNaturalLanguageIntent(final WorkflowRequest request) {
        return WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
    }
    
    private static String recordInferredValue(final ClarifiedIntent clarifiedIntent, final String fieldName, final String value) {
        if (null != clarifiedIntent) {
            clarifiedIntent.getInferredValues().put(fieldName, value);
        }
        return value;
    }
}
