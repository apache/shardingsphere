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

package org.apache.shardingsphere.mcp.feature.encrypt.tool.service;

import org.apache.shardingsphere.mcp.feature.encrypt.tool.model.EncryptWorkflowRequest;
import org.apache.shardingsphere.mcp.tool.model.workflow.ClarifiedIntent;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowIntentResolverSupport;
import org.apache.shardingsphere.mcp.tool.service.workflow.WorkflowSqlUtils;

import java.util.Locale;

/**
 * Encrypt workflow intent resolver.
 */
final class EncryptWorkflowIntentResolver {
    
    ClarifiedIntent resolve(final EncryptWorkflowRequest request) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(WorkflowIntentResolverSupport.resolveOperationType(request));
        result.setFieldSemantics(WorkflowIntentResolverSupport.resolveFieldSemantics(request));
        request.getOptions().setRequiresDecrypt(resolveRequiresDecrypt(request, result));
        request.getOptions().setRequiresEqualityFilter(resolveRequiresEqualityFilter(request, result));
        request.getOptions().setRequiresLikeQuery(resolveRequiresLikeQuery(request, result));
        result.setReasoningNotes("Derived from explicit arguments and encrypt-specific intent heuristics.");
        return result;
    }
    
    private Boolean resolveRequiresDecrypt(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresDecrypt()) {
            return request.getOptions().getRequiresDecrypt();
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
    
    private Boolean resolveRequiresEqualityFilter(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresEqualityFilter()) {
            return request.getOptions().getRequiresEqualityFilter();
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
    
    private Boolean resolveRequiresLikeQuery(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresLikeQuery()) {
            return request.getOptions().getRequiresLikeQuery();
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
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return "drop".equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
}
