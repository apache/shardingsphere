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

import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationReport;
import org.apache.shardingsphere.mcp.tool.model.workflow.ValidationSection;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Workflow validation utility methods.
 */
public final class WorkflowValidationUtils {
    
    private static final String STATUS_FAILED = "failed";
    
    private static final String STATUS_VALIDATED = "validated";
    
    private WorkflowValidationUtils() {
    }
    
    /**
     * Check whether workflow validation can proceed.
     *
     * @param sessionId session identifier
     * @param snapshot workflow snapshot
     * @return rejection response or empty map
     */
    public static Map<String, Object> checkValidatePreconditions(final String sessionId, final WorkflowContextSnapshot snapshot) {
        if (!isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isValidatableStatus(snapshot)) {
            return createRejectedResponse(WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    String.format("Workflow status `%s` cannot enter validate in the current lifecycle.", snapshot.getStatus()),
                    "Execute the workflow first or continue from a validatable status.");
        }
        return Map.of();
    }
    
    /**
     * Create workflow validation issues from a validation report.
     *
     * @param validationReport validation report
     * @return validation issues
     */
    public static List<Map<String, Object>> createValidationIssues(final ValidationReport validationReport) {
        if (!STATUS_FAILED.equals(validationReport.getOverallStatus())) {
            return List.of();
        }
        return List.of(new WorkflowIssue(resolveValidationIssueCode(validationReport), "error", "validating",
                "Validation detected mismatches between the plan and the current state.", "Inspect mismatches and re-run the workflow after fixes.", true, Map.of()).toMap());
    }
    
    /**
     * Resolve overall validation status from validation sections.
     *
     * @param validationSections validation sections
     * @return overall validation status
     */
    public static String resolveOverallStatus(final ValidationSection... validationSections) {
        for (ValidationSection each : validationSections) {
            if (null != each && STATUS_FAILED.equals(each.getStatus())) {
                return STATUS_FAILED;
            }
        }
        return "passed";
    }
    
    /**
     * Resolve workflow lifecycle status from validation report.
     *
     * @param validationReport validation report
     * @return workflow lifecycle status
     */
    public static String resolveValidationStatus(final ValidationReport validationReport) {
        return STATUS_FAILED.equals(validationReport.getOverallStatus()) ? STATUS_FAILED : STATUS_VALIDATED;
    }
    
    /**
     * Persist validation result and create response payload.
     *
     * @param contextStore workflow context store
     * @param snapshot workflow snapshot
     * @param validationReport validation report
     * @return validation response
     */
    public static Map<String, Object> finalizeValidation(final WorkflowContextStore contextStore, final WorkflowContextSnapshot snapshot,
                                                         final ValidationReport validationReport) {
        String validationStatus = resolveValidationStatus(validationReport);
        snapshot.setValidationReport(validationReport);
        if (null != snapshot.getInteractionPlan()) {
            snapshot.getInteractionPlan().setCurrentStep("validated");
        }
        snapshot.setStatus(validationStatus);
        contextStore.save(snapshot);
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", validationStatus);
        result.put("issues", createValidationIssues(validationReport));
        result.putAll(validationReport.toMap());
        return result;
    }
    
    /**
     * Create one validation mismatch entry.
     *
     * @param code mismatch code
     * @param layer mismatch layer
     * @param expected expected value
     * @param actual actual value
     * @param impact mismatch impact
     * @param suggestedNextAction suggested next action
     * @return mismatch payload
     */
    public static Map<String, Object> createMismatch(final String code, final String layer, final String expected,
                                                     final String actual, final String impact, final String suggestedNextAction) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("code", code);
        result.put("layer", layer);
        result.put("expected", expected);
        result.put("actual", actual);
        result.put("impact", impact);
        result.put("suggested_next_action", suggestedNextAction);
        return result;
    }
    
    private static boolean isOwnedBySession(final String sessionId, final WorkflowContextSnapshot snapshot) {
        return WorkflowSqlUtils.trimToEmpty(snapshot.getSessionId()).isEmpty() || snapshot.getSessionId().equals(sessionId);
    }
    
    private static boolean isValidatableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = WorkflowSqlUtils.trimToEmpty(snapshot.getStatus());
        if (STATUS_VALIDATED.equalsIgnoreCase(actualStatus) || "executed".equalsIgnoreCase(actualStatus) || "awaiting-manual-execution".equalsIgnoreCase(actualStatus)) {
            return true;
        }
        if (!STATUS_FAILED.equalsIgnoreCase(actualStatus)) {
            return false;
        }
        String currentStep = null == snapshot.getInteractionPlan() ? "" : WorkflowSqlUtils.trimToEmpty(snapshot.getInteractionPlan().getCurrentStep());
        return "validated".equalsIgnoreCase(currentStep) || "failed".equalsIgnoreCase(currentStep) || "executed".equalsIgnoreCase(currentStep);
    }
    
    private static Map<String, Object> createRejectedResponse(final String issueCode, final String message, final String userAction) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", STATUS_FAILED);
        result.put("issues", List.of(new WorkflowIssue(issueCode, "error", "validating", message, userAction, false, Map.of()).toMap()));
        result.put("overall_status", STATUS_FAILED);
        result.put("mismatches", List.of());
        return result;
    }
    
    private static String resolveValidationIssueCode(final ValidationReport validationReport) {
        for (Map<String, Object> each : validationReport.getMismatches()) {
            String actualCode = WorkflowSqlUtils.trimToEmpty(String.valueOf(each.get("code")));
            if (!actualCode.isEmpty()) {
                return actualCode;
            }
        }
        return WorkflowIssueCode.RULE_STATE_MISMATCH;
    }
}
