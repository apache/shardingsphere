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
        if (naturalLanguageIntent.contains("irreversible") || naturalLanguageIntent.contains("not reversible")) {
            return inferOption(clarifiedIntent, "requires_decrypt", false);
        }
        if (naturalLanguageIntent.contains("reversible") || naturalLanguageIntent.contains("decrypt")) {
            return inferOption(clarifiedIntent, "requires_decrypt", true);
        }
        addPendingQuestion(clarifiedIntent, "requires_decrypt", "Do you need reversible decryption?");
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
        if (naturalLanguageIntent.contains("no equality") || naturalLanguageIntent.contains("without equality")) {
            return inferOption(clarifiedIntent, "requires_equality_filter", false);
        }
        if (naturalLanguageIntent.contains("equality") || naturalLanguageIntent.contains("exact")) {
            return inferOption(clarifiedIntent, "requires_equality_filter", true);
        }
        addPendingQuestion(clarifiedIntent, "requires_equality_filter", "Do you need equality query?");
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
        if (naturalLanguageIntent.contains("no like") || naturalLanguageIntent.contains("without like")
                || naturalLanguageIntent.contains("no fuzzy") || naturalLanguageIntent.contains("without fuzzy")) {
            return inferOption(clarifiedIntent, "requires_like_query", false);
        }
        if (naturalLanguageIntent.contains("like") || naturalLanguageIntent.contains("fuzzy")) {
            return inferOption(clarifiedIntent, "requires_like_query", true);
        }
        addPendingQuestion(clarifiedIntent, "requires_like_query", "Do you need LIKE query?");
        return null;
    }
    
    private boolean isDropWorkflow(final ClarifiedIntent clarifiedIntent) {
        return "drop".equalsIgnoreCase(clarifiedIntent.getOperationType());
    }
    
    private Boolean inferOption(final ClarifiedIntent clarifiedIntent, final String fieldName, final boolean value) {
        clarifiedIntent.getInferredValues().put(fieldName, value);
        return value;
    }
    
    private void addPendingQuestion(final ClarifiedIntent clarifiedIntent, final String unresolvedField, final String question) {
        if (!clarifiedIntent.getUnresolvedFields().contains(unresolvedField)) {
            clarifiedIntent.getUnresolvedFields().add(unresolvedField);
        }
        clarifiedIntent.getPendingQuestions().add(question);
    }
}
