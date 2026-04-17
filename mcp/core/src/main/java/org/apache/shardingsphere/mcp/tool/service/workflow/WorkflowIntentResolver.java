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

import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowRequest;

import java.util.Locale;

/**
 * Workflow intent resolver.
 */
final class WorkflowIntentResolver {
    
    ClarifiedIntent resolve(final WorkflowRequest request) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setIntentType(resolveIntentType(request));
        result.setOperationType(resolveOperationType(request));
        result.setFieldSemantics(resolveFieldSemantics(request));
        result.setRequiresDecrypt(resolveRequiresDecrypt(request, result));
        result.setRequiresEqualityFilter(resolveRequiresEqualityFilter(request, result));
        result.setRequiresLikeQuery(resolveRequiresLikeQuery(request, result));
        result.setReasoningNotes("Derived from explicit arguments and natural language hints.");
        return result;
    }
    
    private String resolveIntentType(final WorkflowRequest request) {
        String actualIntentType = WorkflowSqlUtils.trimToEmpty(request.getIntentType()).toLowerCase(Locale.ENGLISH);
        if (!actualIntentType.isEmpty()) {
            return actualIntentType;
        }
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("mask") || naturalLanguageIntent.contains("脱敏")) {
            return "mask";
        }
        if (naturalLanguageIntent.contains("encrypt") || naturalLanguageIntent.contains("加密")) {
            return "encrypt";
        }
        return "";
    }
    
    private String resolveOperationType(final WorkflowRequest request) {
        String actualOperationType = WorkflowSqlUtils.trimToEmpty(request.getOperationType()).toLowerCase(Locale.ENGLISH);
        if (!actualOperationType.isEmpty()) {
            return actualOperationType;
        }
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("drop") || naturalLanguageIntent.contains("删除")) {
            return "drop";
        }
        if (naturalLanguageIntent.contains("alter") || naturalLanguageIntent.contains("修改") || naturalLanguageIntent.contains("补")) {
            return "alter";
        }
        return "create";
    }
    
    private String resolveFieldSemantics(final WorkflowRequest request) {
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        String columnName = WorkflowSqlUtils.trimToEmpty(request.getColumn()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("手机号") || columnName.contains("phone") || columnName.contains("mobile") || columnName.contains("tel")) {
            return "phone";
        }
        if (naturalLanguageIntent.contains("身份证") || columnName.contains("id_card")) {
            return "id_card";
        }
        return columnName;
    }
    
    private Boolean resolveRequiresDecrypt(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (!"encrypt".equalsIgnoreCase(clarifiedIntent.getIntentType())) {
            return null;
        }
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("不可逆")) {
            return false;
        }
        if (naturalLanguageIntent.contains("可逆") || naturalLanguageIntent.contains("解密") || naturalLanguageIntent.contains("decrypt")) {
            return true;
        }
        clarifiedIntent.getPendingQuestions().add("是否需要可逆解密？");
        return null;
    }
    
    private Boolean resolveRequiresEqualityFilter(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (!"encrypt".equalsIgnoreCase(clarifiedIntent.getIntentType())) {
            return null;
        }
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("不需要等值")) {
            return false;
        }
        if (naturalLanguageIntent.contains("等值") || naturalLanguageIntent.contains("精确") || naturalLanguageIntent.contains("equality")) {
            return true;
        }
        clarifiedIntent.getPendingQuestions().add("是否需要等值查询？");
        return null;
    }
    
    private Boolean resolveRequiresLikeQuery(final WorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (!"encrypt".equalsIgnoreCase(clarifiedIntent.getIntentType())) {
            return null;
        }
        String naturalLanguageIntent = WorkflowSqlUtils.trimToEmpty(request.getNaturalLanguageIntent()).toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("不需要like") || naturalLanguageIntent.contains("不需要模糊")) {
            return false;
        }
        if (naturalLanguageIntent.contains("like") || naturalLanguageIntent.contains("模糊")) {
            return true;
        }
        clarifiedIntent.getPendingQuestions().add("是否需要 LIKE 查询？");
        return null;
    }
}
