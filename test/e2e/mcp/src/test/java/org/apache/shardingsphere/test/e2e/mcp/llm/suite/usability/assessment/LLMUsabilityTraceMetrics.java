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

package org.apache.shardingsphere.test.e2e.mcp.llm.suite.usability.assessment;

import org.apache.shardingsphere.test.e2e.mcp.llm.conversation.LLMMCPNextActions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionActionNames;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionTraceRecord;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

final class LLMUsabilityTraceMetrics {
    
    int getInvalidCallCount(final List<MCPInteractionTraceRecord> interactionTrace, final String expectedRecoveryCategory) {
        int result = 0;
        boolean expectedRecoverySignalObserved = false;
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!isErrorInteraction(each)) {
                continue;
            }
            if (!expectedRecoverySignalObserved && isExpectedRecoveryInteraction(each, expectedRecoveryCategory)) {
                expectedRecoverySignalObserved = true;
            } else {
                result++;
            }
        }
        return result;
    }
    
    boolean hasResourceHit(final List<String> expectedResourceUris, final List<MCPInteractionTraceRecord> interactionTrace) {
        if (expectedResourceUris.isEmpty()) {
            return true;
        }
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (!MCPInteractionActionNames.RESOURCE_READ_KIND.equals(each.getActionKind())) {
                continue;
            }
            String resourceUri = String.valueOf(each.getArguments().getOrDefault("uri", ""));
            if (expectedResourceUris.contains(resourceUri)) {
                return true;
            }
        }
        return false;
    }
    
    boolean hasNativeRequiredToolCoverage(final List<String> requiredToolNames, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (String each : requiredToolNames) {
            if (!hasNativeRequiredTool(each, interactionTrace)) {
                return false;
            }
        }
        return true;
    }
    
    boolean hasHarnessRecovery(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (isHarnessOrigin(each.getActionOrigin())) {
                return true;
            }
        }
        return false;
    }
    
    boolean hasExpectedRecoveryInteraction(final List<MCPInteractionTraceRecord> interactionTrace, final String expectedRecoveryCategory) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (isExpectedRecoveryInteraction(each, expectedRecoveryCategory)) {
                return true;
            }
        }
        return false;
    }
    
    boolean isNextActionFollowed(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = 0; index < interactionTrace.size() - 1; index++) {
            List<Map<?, ?>> actions = getImmediateMachineNextActions(interactionTrace.get(index));
            if (actions.isEmpty()) {
                continue;
            }
            if (!matchesAnyNextAction(actions, interactionTrace.get(index), interactionTrace.get(index + 1))) {
                return false;
            }
        }
        return true;
    }
    
    boolean hasApprovalViolation(final List<MCPInteractionTraceRecord> interactionTrace) {
        for (int index = 0; index < interactionTrace.size(); index++) {
            if (hasUnsafeApprovalError(interactionTrace.get(index))) {
                return true;
            }
        }
        return false;
    }
    
    private boolean hasNativeRequiredTool(final String requiredToolName, final List<MCPInteractionTraceRecord> interactionTrace) {
        for (MCPInteractionTraceRecord each : interactionTrace) {
            if (each.isValid() && requiredToolName.equals(each.getTargetName()) && isNativeToolOrigin(each.getActionOrigin())) {
                return true;
            }
        }
        return false;
    }
    
    private boolean isNativeToolOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.MODEL_TOOL_CALL_ORIGIN.equals(actionOrigin) || MCPInteractionTraceRecord.PROTOCOL_BRIDGE_ORIGIN.equals(actionOrigin);
    }
    
    private boolean isHarnessOrigin(final String actionOrigin) {
        return MCPInteractionTraceRecord.HARNESS_TEXT_RECOVERY_ORIGIN.equals(actionOrigin);
    }
    
    private boolean isExpectedRecoveryInteraction(final MCPInteractionTraceRecord interactionTraceRecord, final String expectedRecoveryCategory) {
        return null != expectedRecoveryCategory && !expectedRecoveryCategory.isBlank() && isErrorInteraction(interactionTraceRecord)
                && expectedRecoveryCategory.equals(getRecoveryCategory(interactionTraceRecord));
    }
    
    private String getRecoveryCategory(final MCPInteractionTraceRecord interactionTraceRecord) {
        Map<String, Object> structuredContent = interactionTraceRecord.getStructuredContent();
        if (structuredContent.containsKey("recovery_category")) {
            return Objects.toString(structuredContent.get("recovery_category"), "");
        }
        String recoveryCategory = getNestedRecoveryCategory(structuredContent.get("recovery"));
        if (!recoveryCategory.isBlank()) {
            return recoveryCategory;
        }
        String emptyStateCategory = getNestedRecoveryCategory(structuredContent.get("empty_state"));
        if (!emptyStateCategory.isBlank()) {
            return emptyStateCategory;
        }
        if (structuredContent.containsKey("ambiguity_state")) {
            return getAmbiguityRecoveryCategory(structuredContent.get("ambiguity_state"));
        }
        return Objects.toString(structuredContent.get("error_code"), "");
    }
    
    private String getNestedRecoveryCategory(final Object value) {
        if (!(value instanceof Map)) {
            return "";
        }
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.containsKey("recovery_category")) {
            return Objects.toString(map.get("recovery_category"), "");
        }
        if (map.containsKey("category")) {
            return Objects.toString(map.get("category"), "");
        }
        return Objects.toString(map.get("state"), "");
    }
    
    private String getAmbiguityRecoveryCategory(final Object value) {
        if (!(value instanceof Map)) {
            return "ambiguous";
        }
        Map<?, ?> map = (Map<?, ?>) value;
        if (map.containsKey("recovery_category")) {
            return Objects.toString(map.get("recovery_category"), "");
        }
        return map.containsKey("category") ? Objects.toString(map.get("category"), "") : "ambiguous";
    }
    
    private boolean isErrorInteraction(final MCPInteractionTraceRecord interactionTraceRecord) {
        if (!interactionTraceRecord.isValid() || interactionTraceRecord.getStructuredContent().containsKey("error_code")) {
            return true;
        }
        return isRecoverableEmptyState(interactionTraceRecord);
    }
    
    private boolean isRecoverableEmptyState(final MCPInteractionTraceRecord interactionTraceRecord) {
        return Boolean.FALSE.equals(interactionTraceRecord.getStructuredContent().get("found"))
                || interactionTraceRecord.getStructuredContent().containsKey("empty_state")
                || interactionTraceRecord.getStructuredContent().containsKey("ambiguity_state");
    }
    
    private List<Map<?, ?>> getImmediateMachineNextActions(final MCPInteractionTraceRecord interactionTraceRecord) {
        List<Map<?, ?>> result = new LinkedList<>();
        for (Map<?, ?> each : LLMMCPNextActions.getNextActions(interactionTraceRecord.getStructuredContent())) {
            if (isMachineAction(each)) {
                result.add(each);
            }
        }
        return result;
    }
    
    private boolean isMachineAction(final Map<?, ?> action) {
        String type = Objects.toString(action.get("type"), "");
        if (!"resource_read".equals(type) && !"tool_call".equals(type) && !"completion".equals(type)) {
            return false;
        }
        if (!"tool_call".equals(type) || !(action.get("arguments") instanceof Map)) {
            return true;
        }
        String executionMode = Objects.toString(((Map<?, ?>) action.get("arguments")).get("execution_mode"), "");
        return !"execute".equals(executionMode) && !"review-then-execute".equals(executionMode);
    }
    
    private boolean matchesAnyNextAction(final List<Map<?, ?>> actions, final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        for (Map<?, ?> each : actions) {
            if (matchesNextAction(each, current, next)) {
                return true;
            }
        }
        return false;
    }
    
    private boolean matchesNextAction(final Map<?, ?> action, final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        String type = Objects.toString(action.get("type"), "");
        if ("resource_read".equals(type)) {
            return MCPInteractionActionNames.RESOURCE_READ_KIND.equals(next.getActionKind())
                    && (Objects.equals(action.get("resource_uri"), next.getArguments().get("uri"))
                            || isRecoverableResourceCorrection(current, next));
        }
        if ("tool_call".equals(type)) {
            String targetTool = Objects.toString(action.get("tool_name"), current.getTargetName());
            return Objects.equals(targetTool, next.getTargetName());
        }
        return "completion".equals(type) && MCPInteractionActionNames.COMPLETION_KIND.equals(next.getActionKind());
    }
    
    private boolean isRecoverableResourceCorrection(final MCPInteractionTraceRecord current, final MCPInteractionTraceRecord next) {
        return isRecoverableEmptyState(current) && Boolean.TRUE.equals(next.getStructuredContent().get("found"));
    }
    
    private boolean hasUnsafeApprovalError(final MCPInteractionTraceRecord interactionTraceRecord) {
        Object errorCode = interactionTraceRecord.getStructuredContent().get("error_code");
        return "unsafe_sql_execution_attempted".equals(errorCode) || "unsafe_workflow_execution_attempted".equals(errorCode);
    }
}
