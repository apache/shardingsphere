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
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;

import java.util.Locale;

/**
 * Shared workflow intent resolver support.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WorkflowIntentResolverSupport {
    
    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
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
        String actualOperationType = request.getOperationType().toLowerCase(Locale.ENGLISH);
        if (!actualOperationType.isEmpty()) {
            return actualOperationType;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        if (containsAny(naturalLanguageIntent, "drop", "delete", "remove", "删除", "移除", "去掉")) {
            return recordInferredValue(clarifiedIntent, "operation_type", WorkflowLifecycle.OPERATION_DROP);
        }
        if (containsAny(naturalLanguageIntent, "alter", "modify", "update", "修改", "更新", "调整", "变更")) {
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
        String actualFieldSemantics = request.getFieldSemantics().toLowerCase(Locale.ENGLISH);
        if (!actualFieldSemantics.isEmpty()) {
            return actualFieldSemantics;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        String columnName = request.getColumn().toLowerCase(Locale.ENGLISH);
        if (containsAny(naturalLanguageIntent, "phone number", "phone", "mobile", "tel", "手机号", "手机", "电话号码", "电话")
                || containsAny(columnName, "phone", "mobile", "tel")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "phone");
        }
        if (containsAny(naturalLanguageIntent, "identity card", "id card", "身份证", "证件") || columnName.contains("id_card")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "id_card");
        }
        if (containsAny(naturalLanguageIntent, "email", "邮箱", "邮件") || columnName.contains("email")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "email");
        }
        if (containsAny(naturalLanguageIntent, "bank card", "银行卡", "银行卡号", "卡号")
                || containsAny(columnName, "bank_card", "card_no", "card_number")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "bank_card");
        }
        if (containsAny(naturalLanguageIntent, "passport", "护照") || columnName.contains("passport")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "passport");
        }
        if (containsAny(naturalLanguageIntent, "license plate", "车牌", "车牌号")
                || containsAny(columnName, "license_plate", "plate_no", "plate_number")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "license_plate");
        }
        if (containsAny(naturalLanguageIntent, "birth date", "birthday", "出生日期", "生日")
                || containsAny(columnName, "birth_date", "birthday", "date_of_birth")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "birth_date");
        }
        if (containsAny(naturalLanguageIntent, "address", "地址", "住址") || containsAny(columnName, "address", "addr")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "address");
        }
        if (containsAny(naturalLanguageIntent, "real name", "full name", "姓名", "名字")
                || containsAny(columnName, "real_name", "full_name", "name")) {
            return recordInferredValue(clarifiedIntent, "field_semantics", "name");
        }
        return recordInferredValue(clarifiedIntent, "field_semantics", columnName);
    }
    
    static String resolveExecutionMode(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        String actualExecutionMode = request.getExecutionMode().toLowerCase(Locale.ENGLISH);
        if (!actualExecutionMode.isEmpty()) {
            return actualExecutionMode;
        }
        String naturalLanguageIntent = getNaturalLanguageIntent(request);
        return containsAny(naturalLanguageIntent, "manual", "manual-only", "manual only", "manual execution", "manual artifacts",
                "export", "outside mcp", "without applying", "do not apply", "no runtime side effects", "side effects out of mcp",
                "手动", "手工", "人工", "导出", "不要执行", "不执行", "不要应用", "不应用", "不产生副作用")
                        ? recordInferredValue(clarifiedIntent, "execution_mode", EXECUTION_MODE_MANUAL_ONLY)
                        : "";
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
        return request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
    }
    
    private static boolean containsAny(final String value, final String... candidates) {
        for (String each : candidates) {
            if (value.contains(each)) {
                return true;
            }
        }
        return false;
    }
    
    private static String recordInferredValue(final ClarifiedIntent clarifiedIntent, final String fieldName, final String value) {
        if (null != clarifiedIntent) {
            clarifiedIntent.getInferredValues().put(fieldName, value);
        }
        return value;
    }
}
