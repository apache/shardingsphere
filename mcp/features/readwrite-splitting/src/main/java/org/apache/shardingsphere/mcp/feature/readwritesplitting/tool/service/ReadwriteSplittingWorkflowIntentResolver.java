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

package org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.service;

import org.apache.shardingsphere.mcp.feature.readwritesplitting.tool.model.ReadwriteSplittingStatusWorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.model.ClarifiedIntent;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowRequest;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowIntentResolverSupport;

import java.util.Locale;

/**
 * Readwrite-splitting workflow intent resolver.
 */
final class ReadwriteSplittingWorkflowIntentResolver {
    
    ClarifiedIntent resolveRuleIntent(final WorkflowRequest request) {
        ClarifiedIntent result = new ClarifiedIntent();
        result.setOperationType(WorkflowIntentResolverSupport.resolveOperationType(request, result));
        result.setReasoningNotes(WorkflowIntentResolverSupport.summarizeReasoning(result));
        return result;
    }
    
    ClarifiedIntent resolveStatusIntent(final ReadwriteSplittingStatusWorkflowRequest request) {
        ClarifiedIntent result = new ClarifiedIntent();
        String operationType = resolveStatusOperation(request);
        if (!operationType.isEmpty() && request.getOperationType().isEmpty()) {
            result.getInferredValues().put(WorkflowFieldNames.OPERATION_TYPE, operationType);
        }
        result.setOperationType(operationType);
        result.setReasoningNotes(WorkflowIntentResolverSupport.summarizeReasoning(result));
        return result;
    }
    
    boolean hasConflictingStatusInputs(final ReadwriteSplittingStatusWorkflowRequest request) {
        String operationType = normalizeStatusOperation(request.getOperationType());
        String targetStatus = normalizeStatusOperation(request.getTargetStatus());
        return !operationType.isEmpty() && !targetStatus.isEmpty() && !operationType.equals(targetStatus);
    }
    
    private String resolveStatusOperation(final ReadwriteSplittingStatusWorkflowRequest request) {
        if (hasConflictingStatusInputs(request)) {
            return "";
        }
        String targetStatus = normalizeStatusOperation(request.getTargetStatus());
        if (!targetStatus.isEmpty()) {
            return targetStatus;
        }
        String operationType = normalizeStatusOperation(request.getOperationType());
        if (!operationType.isEmpty()) {
            return operationType;
        }
        String naturalLanguageIntent = request.getNaturalLanguageIntent().toLowerCase(Locale.ENGLISH);
        if (naturalLanguageIntent.contains("enable") || naturalLanguageIntent.contains("启用")) {
            return "enable";
        }
        if (naturalLanguageIntent.contains("disable") || naturalLanguageIntent.contains("禁用")) {
            return "disable";
        }
        return "";
    }
    
    private String normalizeStatusOperation(final String value) {
        String actualValue = value.trim().toLowerCase(Locale.ENGLISH);
        if ("enable".equals(actualValue) || "enabled".equals(actualValue)) {
            return "enable";
        }
        if ("disable".equals(actualValue) || "disabled".equals(actualValue)) {
            return "disable";
        }
        return "";
    }
}
