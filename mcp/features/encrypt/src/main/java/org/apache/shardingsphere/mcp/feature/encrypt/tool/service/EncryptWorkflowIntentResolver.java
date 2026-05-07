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
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowIntentResolverSupport;

import java.util.Locale;

/**
 * Encrypt workflow intent resolver.
 */
final class EncryptWorkflowIntentResolver {

    ClarifiedIntent resolve(final EncryptWorkflowRequest request) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(WorkflowIntentResolverSupport.resolveOperationType(request, result));
        result.setFieldSemantics(WorkflowIntentResolverSupport.resolveFieldSemantics(request, result));
        request.getOptions().setRequiresDecrypt(resolveRequiresDecrypt(request, result));
        request.getOptions().setRequiresEqualityFilter(resolveRequiresEqualityFilter(request, result));
        request.getOptions().setRequiresLikeQuery(resolveRequiresLikeQuery(request, result));
        result.setReasoningNotes(WorkflowIntentResolverSupport.summarizeReasoning(result));
        return result;
    }

    private Boolean resolveRequiresDecrypt(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresDecrypt()) {
            return request.getOptions().getRequiresDecrypt();
        }
        String naturalLanguageIntent = request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
        if (containsAny(naturalLanguageIntent, "irreversible", "not reversible", "不可逆", "无法解密", "哈希", "散列")) {
            return inferOption(clarifiedIntent, "requires_decrypt", false);
        }
        if (containsAny(naturalLanguageIntent, "reversible", "decrypt", "可逆", "解密")) {
            return inferOption(clarifiedIntent, "requires_decrypt", true);
        }
        addClarificationMessage(clarifiedIntent, "requires_decrypt", "Do you need reversible decryption?");
        return null;
    }

    private Boolean resolveRequiresEqualityFilter(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresEqualityFilter()) {
            return request.getOptions().getRequiresEqualityFilter();
        }
        String naturalLanguageIntent = request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
        if (containsAny(naturalLanguageIntent, "no equality", "without equality", "不需要等值", "不支持等值", "无等值")) {
            return inferOption(clarifiedIntent, "requires_equality_filter", false);
        }
        if (containsAny(naturalLanguageIntent, "equality", "exact", "等值", "精确查询", "精确匹配")) {
            return inferOption(clarifiedIntent, "requires_equality_filter", true);
        }
        addClarificationMessage(clarifiedIntent, "requires_equality_filter", "Do you need equality query?");
        return null;
    }

    private Boolean resolveRequiresLikeQuery(final EncryptWorkflowRequest request, final ClarifiedIntent clarifiedIntent) {
        if (isDropWorkflow(clarifiedIntent)) {
            return null;
        }
        if (null != request.getOptions().getRequiresLikeQuery()) {
            return request.getOptions().getRequiresLikeQuery();
        }
        String naturalLanguageIntent = request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
        if (containsAny(naturalLanguageIntent, "no like", "without like", "no fuzzy", "without fuzzy", "不需要模糊", "不支持模糊", "无模糊", "不需要 like")) {
            return inferOption(clarifiedIntent, "requires_like_query", false);
        }
        if (containsAny(naturalLanguageIntent, "like", "fuzzy", "模糊", "模糊查询")) {
            return inferOption(clarifiedIntent, "requires_like_query", true);
        }
        addClarificationMessage(clarifiedIntent, "requires_like_query", "Do you need LIKE query?");
        return null;
    }

    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return "drop".equalsIgnoreCase(clarifiedIntent.getOperationType());
    }

    private Boolean inferOption(final ClarifiedIntent clarifiedIntent, final String fieldName, final boolean value) {
        clarifiedIntent.getInferredValues().put(fieldName, value);
        return value;
    }

    private boolean containsAny(final String value, final String... candidates) {
        for (String each : candidates) {
            if (value.contains(each)) {
                return true;
            }
        }
        return false;
    }

    private void addClarificationMessage(final ClarifiedIntent clarifiedIntent, final String unresolvedField, final String message) {
        if (!clarifiedIntent.getUnresolvedFields().contains(unresolvedField)) {
            clarifiedIntent.getUnresolvedFields().add(unresolvedField);
        }
        clarifiedIntent.getClarificationMessages().add(message);
    }
}
