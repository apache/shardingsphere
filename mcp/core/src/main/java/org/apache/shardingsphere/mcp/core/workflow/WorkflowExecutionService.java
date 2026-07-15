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

package org.apache.shardingsphere.mcp.core.workflow;

import org.apache.shardingsphere.mcp.core.protocol.exception.MCPExecutionModeRequiredException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidApprovedStepsException;
import org.apache.shardingsphere.mcp.core.protocol.exception.MCPInvalidExecutionModeException;
import org.apache.shardingsphere.mcp.support.diagnostic.MCPDiagnosticCategory;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactMaskUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowGuidancePayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSecretReferenceUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplyArtifactValidator;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;

import java.util.List;
import java.util.Map;

/**
 * Workflow execution service.
 */
public final class WorkflowExecutionService {
    
    private static final List<String> EXECUTION_MODES = List.of(WorkflowLifecycle.EXECUTION_MODE_PREVIEW,
            WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE, WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY);
    
    private static final List<String> ALLOWED_APPROVAL_STEPS = List.of(
            WorkflowArtifactPayloadUtils.STEP_DDL, WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL);
    
    /**
     * Apply workflow artifacts.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param executionFacade execution facade
     * @param workflowApplySynchronizationHandler workflow apply synchronization handler
     * @param workflowApplyArtifactValidator workflow apply artifact validator
     * @param sessionId session id
     * @param snapshot workflow snapshot
     * @param approvedSteps approved steps
     * @param executionMode execution mode override
     * @return apply payload
     */
    public Map<String, Object> apply(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                     final MCPFeatureExecutionFacade executionFacade, final MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler,
                                     final MCPWorkflowApplyArtifactValidator workflowApplyArtifactValidator, final String sessionId, final WorkflowContextSnapshot snapshot,
                                     final List<String> approvedSteps, final String executionMode) {
        String actualExecutionMode = requireExecutionMode(snapshot, executionMode);
        requireApprovedSteps(snapshot, approvedSteps);
        Map<String, Object> rejectedResponse = checkApplyPreconditions(sessionId, snapshot, actualExecutionMode, approvedSteps);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        Map<String, Object> invalidArtifactResponse = validateApplyArtifacts(workflowSessionContext, snapshot, actualExecutionMode, workflowApplyArtifactValidator);
        if (!invalidArtifactResponse.isEmpty()) {
            return invalidArtifactResponse;
        }
        WorkflowApplyOutcome applyOutcome = new WorkflowApplyOutcome();
        if (WorkflowLifecycle.EXECUTION_MODE_PREVIEW.equalsIgnoreCase(actualExecutionMode)) {
            return previewApply(workflowSessionContext, snapshot);
        }
        if (isManualOnly(actualExecutionMode)) {
            return applyManualOnly(workflowSessionContext, snapshot, applyOutcome);
        }
        return applyAutomatically(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, workflowApplySynchronizationHandler, sessionId, snapshot,
                createActualApprovedSteps(approvedSteps), actualExecutionMode, applyOutcome);
    }
    
    private String requireExecutionMode(final WorkflowContextSnapshot snapshot, final String executionMode) {
        if (executionMode.isEmpty()) {
            throw new MCPExecutionModeRequiredException(WorkflowToolDescriptors.APPLY_TOOL_NAME, EXECUTION_MODES, createPreviewSuggestedArguments(snapshot));
        }
        String result = executionMode.toLowerCase();
        if (!EXECUTION_MODES.contains(result)) {
            throw new MCPInvalidExecutionModeException(WorkflowToolDescriptors.APPLY_TOOL_NAME, EXECUTION_MODES, createPreviewSuggestedArguments(snapshot));
        }
        return result;
    }
    
    private void requireApprovedSteps(final WorkflowContextSnapshot snapshot, final List<String> approvedSteps) {
        if (null == approvedSteps || approvedSteps.isEmpty()) {
            return;
        }
        for (String each : approvedSteps) {
            if (!ALLOWED_APPROVAL_STEPS.contains(each)) {
                throw new MCPInvalidApprovedStepsException(ALLOWED_APPROVAL_STEPS, createPreviewSuggestedArguments(snapshot));
            }
        }
    }
    
    private List<String> createActualApprovedSteps(final List<String> approvedSteps) {
        return null == approvedSteps || approvedSteps.isEmpty() ? List.of() : approvedSteps;
    }
    
    private Map<String, Object> checkApplyPreconditions(final String sessionId, final WorkflowContextSnapshot snapshot, final String executionMode,
                                                        final List<String> approvedSteps) {
        if (!WorkflowLifecycleUtils.isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isApplicableStatus(snapshot)) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    String.format("Workflow status `%s` cannot enter apply in the current lifecycle.", snapshot.getStatus()),
                    "Plan the workflow again or continue from a reviewable status.");
        }
        if (WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE.equals(executionMode) && !WorkflowLifecycle.STATUS_PREVIEWED.equalsIgnoreCase(snapshot.getStatus())) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    "Automatic workflow execution requires an execution_mode=preview call first.",
                    "Call database_gateway_apply_workflow with execution_mode=preview, review preview_artifacts, then pass explicit approved_steps.");
        }
        if (WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE.equals(executionMode) && (null == approvedSteps || approvedSteps.isEmpty())) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    "Automatic workflow execution requires explicit approved_steps.",
                    "Pass the reviewed preview_artifacts.approval_step values in approved_steps.");
        }
        if (WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE.equals(executionMode) && !areApprovedStepsVisible(snapshot, approvedSteps)) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    "Automatic workflow execution received approved_steps that are not present in the previewed artifacts.",
                    "Pass only the preview_artifacts.approval_step values from the latest preview response.");
        }
        return Map.of();
    }
    
    private boolean isApplicableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = null == snapshot.getStatus() ? "" : snapshot.getStatus();
        if (WorkflowLifecycle.STATUS_PLANNED.equalsIgnoreCase(actualStatus)
                || WorkflowLifecycle.STATUS_PREVIEWED.equalsIgnoreCase(actualStatus)
                || WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equalsIgnoreCase(actualStatus)) {
            return true;
        }
        if (!WorkflowLifecycle.STATUS_FAILED.equalsIgnoreCase(actualStatus)) {
            return false;
        }
        String currentStep = WorkflowLifecycleUtils.resolveCurrentStep(snapshot);
        return WorkflowLifecycle.STEP_FAILED.equalsIgnoreCase(currentStep) || WorkflowLifecycle.STEP_VALIDATED.equalsIgnoreCase(currentStep);
    }
    
    private Map<String, Object> createRejectedResponse(final WorkflowContextSnapshot snapshot, final String executionMode, final String issueCode, final String message,
                                                       final String userAction) {
        return new WorkflowApplyResponseBuilder().buildFailureResponse(snapshot, executionMode,
                List.of(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_REVIEW, message, userAction, false, Map.of()).toMap()));
    }
    
    private String resolveSnapshotExecutionMode(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? "" : snapshot.getRequest().getExecutionMode();
    }
    
    private boolean isManualOnly(final String executionMode) {
        return WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(executionMode);
    }
    
    private boolean areApprovedStepsVisible(final WorkflowContextSnapshot snapshot, final List<String> approvedSteps) {
        List<String> visibleSteps = createExecutableArtifacts(snapshot).stream().map(WorkflowArtifactBundle.ExecutableWorkflowArtifact::approvalStep).distinct().toList();
        return visibleSteps.containsAll(approvedSteps);
    }
    
    private Map<String, Object> validateApplyArtifacts(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot,
                                                       final String executionMode, final MCPWorkflowApplyArtifactValidator workflowApplyArtifactValidator) {
        List<Map<String, Object>> validationIssues = workflowApplyArtifactValidator.validate(snapshot, createExecutableArtifacts(snapshot));
        if (!validationIssues.isEmpty()) {
            persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
            Map<String, Object> result = new WorkflowApplyResponseBuilder().buildFailureResponse(snapshot, executionMode, validationIssues);
            if (WorkflowLifecycle.EXECUTION_MODE_PREVIEW.equalsIgnoreCase(executionMode)) {
                result.put("would_apply", false);
                result.put("preview_artifacts", List.of());
            }
            result.put("review_summary", "Workflow apply blocked invalid generated artifacts before approval.");
            result.put(MCPPayloadFieldNames.NEXT_ACTIONS,
                    MCPNextActionUtils.ordered(MCPNextActionUtils.stop("Fix generated workflow artifacts, then preview the workflow again.")));
            return result;
        }
        return Map.of();
    }
    
    private Map<String, Object> previewApply(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_REVIEW, WorkflowLifecycle.STATUS_PREVIEWED);
        return new WorkflowApplyResponseBuilder().buildPreviewResponse(snapshot,
                createExecutableArtifacts(snapshot), resolveApplyExecutionMode(snapshot), createArtifactPayload(snapshot));
    }
    
    private Map<String, Object> createPreviewSuggestedArguments(final WorkflowContextSnapshot snapshot) {
        return Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, WorkflowLifecycle.EXECUTION_MODE_PREVIEW);
    }
    
    private String resolveApplyExecutionMode(final WorkflowContextSnapshot snapshot) {
        return WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(resolveSnapshotExecutionMode(snapshot))
                ? WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY
                : WorkflowLifecycle.EXECUTION_MODE_REVIEW_THEN_EXECUTE;
    }
    
    private Map<String, Object> applyManualOnly(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot,
                                                final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_MANUAL_EXECUTION, WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION);
        applyOutcome.addIssue(new WorkflowIssue(WorkflowIssueCode.MANUAL_EXECUTION_PENDING, "warning", WorkflowLifecycle.STEP_REVIEW,
                "Artifacts are generated in manual-only mode and will not be executed automatically.",
                "Execute artifacts manually and run validation afterwards.", true, Map.of()).toMap());
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION, snapshot,
                WorkflowLifecycle.EXECUTION_MODE_MANUAL_ONLY, createArtifactPayload(snapshot));
    }
    
    private Map<String, Object> applyAutomatically(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade,
                                                   final MCPFeatureQueryFacade queryFacade, final MCPFeatureExecutionFacade executionFacade,
                                                   final MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler,
                                                   final String sessionId, final WorkflowContextSnapshot snapshot, final List<String> approvedSteps, final String executionMode,
                                                   final WorkflowApplyOutcome applyOutcome) {
        if (WorkflowSecretReferenceUtils.hasSecretReferences(getPropertySource(snapshot))) {
            return failSecretReferenceManualExecutionRequired(workflowSessionContext, snapshot, executionMode, applyOutcome);
        }
        String currentArtifactType = "";
        String currentArtifactDisplaySql = "";
        try {
            for (WorkflowArtifactBundle.ExecutableWorkflowArtifact each : createExecutableArtifacts(snapshot)) {
                currentArtifactType = each.artifactType();
                currentArtifactDisplaySql = each.displaySql();
                if (!isApproved(approvedSteps, each.approvalStep())) {
                    applyOutcome.addSkippedArtifact(each);
                    continue;
                }
                executeArtifact(executionFacade, sessionId, snapshot, each);
                applyOutcome.addExecutedArtifact(each);
            }
            if (!applyOutcome.hasSkippedArtifacts()) {
                workflowApplySynchronizationHandler.synchronize(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId);
            }
            return completeApply(workflowSessionContext, snapshot, executionMode, applyOutcome);
        } catch (final WorkflowSynchronizationException ex) {
            return failApplySynchronization(workflowSessionContext, snapshot, executionMode, applyOutcome, ex);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            return failApply(workflowSessionContext, snapshot, executionMode, applyOutcome, currentArtifactType, currentArtifactDisplaySql, ex);
        }
    }
    
    private Map<String, Object> completeApply(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final String executionMode,
                                              final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_EXECUTED, WorkflowLifecycle.STATUS_EXECUTED);
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_COMPLETED, snapshot, executionMode, Map.of());
    }
    
    private Map<String, Object> failApply(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final String executionMode,
                                          final WorkflowApplyOutcome applyOutcome, final String artifactType, final String artifactSql, final RuntimeException ex) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        applyOutcome.addFailedArtifact(resolveIssueCode(artifactType), artifactType, artifactSql, WorkflowArtifactMaskUtils.maskSensitiveSql(
                null == ex.getMessage() ? "" : ex.getMessage(), getPropertySource(snapshot), snapshot.getPropertyRequirements()));
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_FAILED, snapshot, executionMode, Map.of());
    }
    
    private Map<String, Object> failSecretReferenceManualExecutionRequired(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot,
                                                                           final String executionMode, final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        WorkflowPropertySource propertySource = getPropertySource(snapshot);
        Map<String, Object> secretReferenceSummary = WorkflowArtifactMaskUtils.createSecretReferenceSummary(propertySource);
        String category = WorkflowSecretReferenceUtils.hasMalformedSecretReferences(propertySource)
                ? MCPDiagnosticCategory.SECRET_REFERENCE_MALFORMED
                : MCPDiagnosticCategory.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED;
        applyOutcome.addSecretReferenceManualExecutionRequired(category, secretReferenceSummary);
        Map<String, Object> result = applyOutcome.createResponse(WorkflowLifecycle.STATUS_FAILED, snapshot, executionMode, createArtifactPayload(snapshot));
        result.put("response_mode", MCPResponseMode.RECOVERY);
        result.put("category", category);
        result.put("message", "This workflow contains sensitive placeholders that must be filled outside MCP before execution.");
        result.put("secret_reference_summary", secretReferenceSummary);
        WorkflowGuidancePayloadBuilder.appendApplyGuidance(result, WorkflowLifecycle.STATUS_FAILED);
        return result;
    }
    
    private Map<String, Object> failApplySynchronization(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot,
                                                         final String executionMode, final WorkflowApplyOutcome applyOutcome, final WorkflowSynchronizationException ex) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        applyOutcome.addSynchronizationFailure(ex.getIssueCode(), ex.getMessage(), ex.getMismatches());
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_FAILED, snapshot, executionMode, Map.of());
    }
    
    private void persistSnapshot(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final String currentStep, final String status) {
        if (null != snapshot.getInteractionPlan()) {
            snapshot.getInteractionPlan().setCurrentStep(currentStep);
        }
        snapshot.setStatus(status);
        workflowSessionContext.save(snapshot);
    }
    
    private WorkflowPropertySource getPropertySource(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? algorithmRole -> Map.of() : snapshot.getRequest();
    }
    
    private Map<String, Object> createArtifactPayload(final WorkflowContextSnapshot snapshot) {
        return WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot)
                ? WorkflowArtifactPayloadUtils.createRuleArtifactPayload(snapshot, getPropertySource(snapshot))
                : WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, getPropertySource(snapshot));
    }
    
    private String resolveIssueCode(final String artifactType) {
        return WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL.equalsIgnoreCase(artifactType)
                ? WorkflowIssueCode.RULE_EXECUTION_FAILED
                : WorkflowIssueCode.DDL_EXECUTION_FAILED;
    }
    
    private boolean isApproved(final List<String> approvedSteps, final String step) {
        return approvedSteps.contains(step);
    }
    
    private List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> createExecutableArtifacts(final WorkflowContextSnapshot snapshot) {
        WorkflowArtifactBundle result = WorkflowArtifactBundle.from(snapshot);
        WorkflowPropertySource propertySource = getPropertySource(snapshot);
        return WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot)
                ? result.toRuleExecutableArtifacts(propertySource, snapshot.getPropertyRequirements())
                : result.toExecutableArtifacts(propertySource, snapshot.getPropertyRequirements());
    }
    
    private void executeArtifact(final MCPFeatureExecutionFacade executionFacade, final String sessionId, final WorkflowContextSnapshot snapshot,
                                 final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        executionFacade.execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), artifact.sql(), 0, 0));
    }
}
