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

import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationReport;
import org.apache.shardingsphere.mcp.support.workflow.model.ValidationSection;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * Workflow validation support.
 */
public final class WorkflowValidationSupport {
    
    /**
     * Check whether workflow validation can proceed.
     *
     * @param sessionId session identifier
     * @param snapshot workflow snapshot
     * @return rejection response or empty map
     */
    public Map<String, Object> checkValidatePreconditions(final String sessionId, final WorkflowContextSnapshot snapshot) {
        if (!WorkflowLifecycleUtils.isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(snapshot, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isValidatableStatus(snapshot)) {
            return createRejectedResponse(snapshot, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    String.format("Workflow status `%s` cannot enter validate in the current lifecycle.", snapshot.getStatus()),
                    "Execute the workflow first or continue from a validatable status.");
        }
        return Map.of();
    }
    
    /**
     * Validate workflow snapshot and persist validation response.
     *
     * @param workflowSessionContext workflow session context
     * @param sessionId session identifier
     * @param snapshot workflow snapshot
     * @param validationReportSupplier validation report supplier
     * @return validation response
     */
    public Map<String, Object> validateAndFinalize(final WorkflowSessionContext workflowSessionContext, final String sessionId, final WorkflowContextSnapshot snapshot,
                                                   final Supplier<ValidationReport> validationReportSupplier) {
        Map<String, Object> rejectedResponse = checkValidatePreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        ValidationReport validationReport = validationReportSupplier.get();
        snapshot.setValidationReport(validationReport);
        return finalizeValidation(workflowSessionContext, snapshot, validationReport);
    }
    
    /**
     * Resolve overall validation status from validation sections.
     *
     * @param validationSections validation sections
     * @return overall validation status
     */
    public String resolveOverallStatus(final ValidationSection... validationSections) {
        for (ValidationSection each : validationSections) {
            if (null != each && WorkflowLifecycle.STATUS_FAILED.equals(each.getStatus())) {
                return WorkflowLifecycle.STATUS_FAILED;
            }
        }
        return WorkflowLifecycle.STATUS_PASSED;
    }
    
    /**
     * Persist validation result and create response payload.
     *
     * @param workflowSessionContext workflow session context
     * @param snapshot workflow snapshot
     * @param validationReport validation report
     * @return validation response
     */
    public Map<String, Object> finalizeValidation(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot,
                                                  final ValidationReport validationReport) {
        String validationStatus = resolveValidationStatus(validationReport);
        workflowSessionContext.persist(snapshot, WorkflowLifecycle.STEP_VALIDATED, validationStatus);
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("response_mode", "validation");
        result.put(MCPPayloadFieldNames.SUMMARY, createValidationSummary(snapshot, validationReport));
        result.put(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId());
        result.put("status", validationStatus);
        result.put("issues", createValidationIssues(validationReport));
        result.putAll(validationReport.toMap());
        result.put("sections", createValidationSections(validationReport));
        WorkflowGuidancePayloadBuilder.appendValidationGuidance(result, snapshot, validationReport);
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
     * @param remediation remediation guidance
     * @return mismatch payload
     */
    public Map<String, Object> createMismatch(final String code, final String layer, final String expected,
                                              final String actual, final String impact, final String remediation) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("code", code);
        result.put("layer", layer);
        result.put("expected", expected);
        result.put("actual", actual);
        result.put("impact", impact);
        result.put("remediation", remediation);
        return result;
    }
    
    private List<Map<String, Object>> createValidationIssues(final ValidationReport validationReport) {
        if (!WorkflowLifecycle.STATUS_FAILED.equals(validationReport.getOverallStatus())) {
            return List.of();
        }
        return List.of(new WorkflowIssue(resolveValidationIssueCode(validationReport), "error", WorkflowLifecycle.STEP_VALIDATING,
                "Validation detected mismatches between the plan and the current state.", "Inspect mismatches and re-run the workflow after fixes.", true, Map.of()).toMap());
    }
    
    private List<Map<String, Object>> createValidationSections(final ValidationReport validationReport) {
        List<Map<String, Object>> result = new LinkedList<>();
        addValidationSection(result, "rule", validationReport.getRuleValidation());
        addValidationSection(result, "logical_metadata", validationReport.getLogicalMetadataValidation());
        addValidationSection(result, "sql_executability", validationReport.getSqlExecutabilityValidation());
        return result;
    }
    
    private void addValidationSection(final List<Map<String, Object>> sections, final String layer, final ValidationSection section) {
        if (null == section) {
            return;
        }
        Map<String, Object> sectionPayload = new LinkedHashMap<>(4, 1F);
        sectionPayload.put("layer", layer);
        sectionPayload.putAll(section.toMap());
        sections.add(sectionPayload);
    }
    
    private String resolveValidationStatus(final ValidationReport validationReport) {
        return WorkflowLifecycle.STATUS_FAILED.equals(validationReport.getOverallStatus()) ? WorkflowLifecycle.STATUS_FAILED : WorkflowLifecycle.STATUS_VALIDATED;
    }
    
    private String createValidationSummary(final WorkflowContextSnapshot snapshot, final ValidationReport validationReport) {
        if (WorkflowLifecycle.STATUS_FAILED.equals(validationReport.getOverallStatus())) {
            return String.format("Workflow validation failed for plan `%s` with %d mismatch(es).", snapshot.getPlanId(), validationReport.getMismatches().size());
        }
        return String.format("Workflow validation passed for plan `%s`.", snapshot.getPlanId());
    }
    
    private boolean isValidatableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = null == snapshot.getStatus() ? "" : snapshot.getStatus();
        if (WorkflowLifecycle.STATUS_VALIDATED.equalsIgnoreCase(actualStatus)
                || WorkflowLifecycle.STATUS_EXECUTED.equalsIgnoreCase(actualStatus)
                || WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equalsIgnoreCase(actualStatus)) {
            return true;
        }
        if (!WorkflowLifecycle.STATUS_FAILED.equalsIgnoreCase(actualStatus)) {
            return false;
        }
        String currentStep = WorkflowLifecycleUtils.resolveCurrentStep(snapshot);
        return WorkflowLifecycle.STEP_VALIDATED.equalsIgnoreCase(currentStep)
                || WorkflowLifecycle.STEP_FAILED.equalsIgnoreCase(currentStep)
                || WorkflowLifecycle.STEP_EXECUTED.equalsIgnoreCase(currentStep);
    }
    
    private Map<String, Object> createRejectedResponse(final WorkflowContextSnapshot snapshot, final String issueCode, final String message, final String userAction) {
        Map<String, Object> result = new LinkedHashMap<>(10, 1F);
        result.put("response_mode", "terminal");
        result.put(MCPPayloadFieldNames.SUMMARY, String.format("Workflow validation cannot run for plan `%s`.", snapshot.getPlanId()));
        result.put(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId());
        result.put("status", WorkflowLifecycle.STATUS_FAILED);
        result.put("issues", List.of(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_VALIDATING, message, userAction, false, Map.of()).toMap()));
        result.put("overall_status", WorkflowLifecycle.STATUS_FAILED);
        result.put("mismatches", List.of());
        result.put("recovery_guidance", userAction);
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createRejectedNextActions(issueCode, userAction));
        return result;
    }
    
    private List<Map<String, Object>> createRejectedNextActions(final String issueCode, final String userAction) {
        return MCPNextActionUtils.ordered(MCPNextActionUtils.askUser(userAction, List.of(resolveRejectedRequiredInput(issueCode))));
    }
    
    private String resolveRejectedRequiredInput(final String issueCode) {
        return WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH.equals(issueCode) ? "same_mcp_session" : "validatable_workflow_state";
    }
    
    /**
     * Resolve validation issue code from mismatches.
     *
     * @param validationReport validation report
     * @return issue code
     */
    public String resolveValidationIssueCode(final ValidationReport validationReport) {
        for (Map<String, Object> each : validationReport.getMismatches()) {
            String actualCode = Objects.toString(each.get("code"), "").trim();
            if (!actualCode.isEmpty()) {
                return actualCode;
            }
        }
        return WorkflowIssueCode.RULE_STATE_MISMATCH;
    }
}
