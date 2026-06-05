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
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowGuidancePayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow execution service.
 */
public final class WorkflowExecutionService {
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final String EXECUTION_MODE_REVIEW_THEN_EXECUTE = "review-then-execute";
    
    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
    private static final List<String> EXECUTION_MODES = List.of(EXECUTION_MODE_PREVIEW, EXECUTION_MODE_REVIEW_THEN_EXECUTE, EXECUTION_MODE_MANUAL_ONLY);
    
    private static final List<String> APPROVED_STEPS = List.of(WorkflowArtifactPayloadUtils.STEP_DDL, WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL);
    
    /**
     * Apply workflow artifacts.
     *
     * @param workflowSessionContext workflow session context
     * @param metadataQueryFacade metadata query facade
     * @param queryFacade query facade
     * @param executionFacade execution facade
     * @param workflowApplySynchronizationHandler workflow apply synchronization handler
     * @param sessionId session id
     * @param snapshot workflow snapshot
     * @param approvedSteps approved steps
     * @param executionMode execution mode override
     * @return apply payload
     */
    public Map<String, Object> apply(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                     final MCPFeatureExecutionFacade executionFacade, final MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler,
                                     final String sessionId, final WorkflowContextSnapshot snapshot, final List<String> approvedSteps, final String executionMode) {
        String actualExecutionMode = requireExecutionMode(snapshot, executionMode);
        requireApprovedSteps(snapshot, approvedSteps);
        Map<String, Object> rejectedResponse = checkApplyPreconditions(sessionId, snapshot, actualExecutionMode);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        WorkflowApplyOutcome applyOutcome = new WorkflowApplyOutcome();
        if (EXECUTION_MODE_PREVIEW.equalsIgnoreCase(actualExecutionMode)) {
            return previewApply(snapshot);
        }
        if (isManualOnly(actualExecutionMode)) {
            return applyManualOnly(workflowSessionContext, snapshot, applyOutcome);
        }
        return applyAutomatically(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, workflowApplySynchronizationHandler, sessionId, snapshot,
                createActualApprovedSteps(approvedSteps), actualExecutionMode, applyOutcome);
    }
    
    private String requireExecutionMode(final WorkflowContextSnapshot snapshot, final String executionMode) {
        if (executionMode.isEmpty()) {
            throw new MCPExecutionModeRequiredException("database_gateway_apply_workflow", EXECUTION_MODES, createPreviewSuggestedArguments(snapshot));
        }
        String result = executionMode.toLowerCase();
        if (!EXECUTION_MODES.contains(result)) {
            throw new MCPInvalidExecutionModeException("database_gateway_apply_workflow", EXECUTION_MODES, createPreviewSuggestedArguments(snapshot));
        }
        return result;
    }
    
    private void requireApprovedSteps(final WorkflowContextSnapshot snapshot, final List<String> approvedSteps) {
        if (null == approvedSteps || approvedSteps.isEmpty()) {
            return;
        }
        for (String each : approvedSteps) {
            if (!APPROVED_STEPS.contains(each)) {
                throw new MCPInvalidApprovedStepsException(APPROVED_STEPS, createPreviewSuggestedArguments(snapshot));
            }
        }
    }
    
    private List<String> createActualApprovedSteps(final List<String> approvedSteps) {
        return null == approvedSteps || approvedSteps.isEmpty() ? List.of() : approvedSteps;
    }
    
    private Map<String, Object> checkApplyPreconditions(final String sessionId, final WorkflowContextSnapshot snapshot, final String executionMode) {
        if (!WorkflowLifecycleUtils.isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isApplicableStatus(snapshot)) {
            return createRejectedResponse(snapshot, executionMode, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    String.format("Workflow status `%s` cannot enter apply in the current lifecycle.", snapshot.getStatus()),
                    "Plan the workflow again or continue from a reviewable status.");
        }
        return Map.of();
    }
    
    private boolean isApplicableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = null == snapshot.getStatus() ? "" : snapshot.getStatus();
        if (WorkflowLifecycle.STATUS_PLANNED.equalsIgnoreCase(actualStatus)
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
        return createResponse(snapshot, WorkflowLifecycle.STATUS_FAILED, executionMode,
                List.of(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_REVIEW, message, userAction, false, Map.of()).toMap()), List.of(), List.of(), List.of(), List.of(), Map.of());
    }
    
    private String resolveSnapshotExecutionMode(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? "" : snapshot.getRequest().getExecutionMode();
    }
    
    private boolean isManualOnly(final String executionMode) {
        return EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(executionMode);
    }
    
    private Map<String, Object> previewApply(final WorkflowContextSnapshot snapshot) {
        List<Map<String, Object>> previewArtifacts = createPreviewArtifacts(snapshot);
        Map<String, Object> result = createResponse(snapshot, "preview", EXECUTION_MODE_PREVIEW, List.of(), List.of(), List.of(), List.of(), List.of(), createArtifactPayload(snapshot));
        result.put("would_apply", false);
        result.put("preview_artifacts", previewArtifacts);
        result.put("review_focus", createPreviewReviewFocus(snapshot, previewArtifacts));
        result.put("review_summary", createReviewSummary(previewArtifacts));
        result.put("argument_provenance", createPreviewArgumentProvenance());
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createPreviewNextActions(snapshot));
        return result;
    }
    
    private List<Map<String, Object>> createPreviewArtifacts(final WorkflowContextSnapshot snapshot) {
        return createExecutableArtifacts(snapshot).stream().map(this::createPreviewArtifact).toList();
    }
    
    private String createReviewSummary(final List<Map<String, Object>> previewArtifacts) {
        if (previewArtifacts.isEmpty()) {
            return "Previewed 0 workflow artifacts. Nothing has been applied.";
        }
        String artifactLabel = 1 == previewArtifacts.size() ? "artifact" : "artifacts";
        String sideEffectScopes = String.join(", ", previewArtifacts.stream()
                .map(each -> (String) each.get("side_effect_scope"))
                .distinct()
                .toList());
        return String.format("Previewed %d workflow %s with side-effect scope %s. Nothing has been applied.", previewArtifacts.size(), artifactLabel, sideEffectScopes);
    }
    
    private Map<String, Object> createPreviewReviewFocus(final WorkflowContextSnapshot snapshot, final List<Map<String, Object>> previewArtifacts) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("artifact_categories", previewArtifacts.stream().map(each -> (String) each.get("artifact_type")).distinct().toList());
        result.put("side_effect_scope", previewArtifacts.stream().map(each -> (String) each.get("side_effect_scope")).distinct().toList());
        result.put("manual_only", EXECUTION_MODE_MANUAL_ONLY.equals(resolveApplyExecutionMode(snapshot)));
        return result;
    }
    
    private Map<String, Object> createPreviewArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("approval_step", artifact.approvalStep());
        result.put("artifact_type", artifact.artifactType());
        result.put("sql", artifact.sql());
        result.put("side_effect_scope", artifact.ruleDistSql() ? "rule-metadata" : "physical-structure");
        return result;
    }
    
    private Map<String, Object> createPreviewNextAction(final WorkflowContextSnapshot snapshot) {
        String executionMode = resolveApplyExecutionMode(snapshot);
        return MCPNextActionUtils.callTool("database_gateway_apply_workflow", createPreviewNextActionReason(executionMode),
                createExecutionArguments(snapshot, executionMode));
    }
    
    private List<Map<String, Object>> createPreviewNextActions(final WorkflowContextSnapshot snapshot) {
        return MCPNextActionUtils.ordered(createPreviewNextAction(snapshot));
    }
    
    private String createPreviewNextActionReason(final String executionMode) {
        return EXECUTION_MODE_MANUAL_ONLY.equals(executionMode)
                ? "Export reviewed workflow artifacts without applying runtime side effects."
                : "Apply workflow artifacts after reviewing the previewed side effects.";
    }
    
    private Map<String, Object> createPreviewArgumentProvenance() {
        return Map.of(WorkflowFieldNames.PLAN_ID, "server_generated", WorkflowFieldNames.EXECUTION_MODE, "server_defaulted");
    }
    
    private Map<String, Object> createExecutionArguments(final WorkflowContextSnapshot snapshot, final String executionMode) {
        return Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, executionMode);
    }
    
    private Map<String, Object> createPreviewSuggestedArguments(final WorkflowContextSnapshot snapshot) {
        return Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, EXECUTION_MODE_PREVIEW);
    }
    
    private String resolveApplyExecutionMode(final WorkflowContextSnapshot snapshot) {
        return EXECUTION_MODE_MANUAL_ONLY.equalsIgnoreCase(resolveSnapshotExecutionMode(snapshot)) ? EXECUTION_MODE_MANUAL_ONLY : EXECUTION_MODE_REVIEW_THEN_EXECUTE;
    }
    
    private Map<String, Object> applyManualOnly(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_MANUAL_EXECUTION, WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION);
        applyOutcome.addIssue(new WorkflowIssue(WorkflowIssueCode.MANUAL_EXECUTION_PENDING, "warning", WorkflowLifecycle.STEP_REVIEW,
                "Artifacts are generated in manual-only mode and will not be executed automatically.", "Execute artifacts manually and run validation afterwards.", true, Map.of()).toMap());
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION, snapshot, "manual-only", createArtifactPayload(snapshot));
    }
    
    private Map<String, Object> applyAutomatically(final WorkflowSessionContext workflowSessionContext, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                                   final MCPFeatureExecutionFacade executionFacade, final MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler,
                                                   final String sessionId, final WorkflowContextSnapshot snapshot, final List<String> approvedSteps, final String executionMode,
                                                   final WorkflowApplyOutcome applyOutcome) {
        String currentArtifactType = "";
        String currentArtifactSql = "";
        try {
            for (WorkflowArtifactBundle.ExecutableWorkflowArtifact each : createExecutableArtifacts(snapshot)) {
                currentArtifactType = each.artifactType();
                currentArtifactSql = each.sql();
                if (!isApproved(approvedSteps, each.approvalStep())) {
                    applyOutcome.addSkippedArtifact(each);
                    continue;
                }
                executeArtifact(executionFacade, sessionId, snapshot, each);
                applyOutcome.addExecutedArtifact(each);
            }
            if (!applyOutcome.hasSkippedArtifacts()) {
                synchronizeAppliedWorkflow(snapshot, metadataQueryFacade, queryFacade, executionFacade, workflowApplySynchronizationHandler, sessionId);
            }
            return completeApply(workflowSessionContext, snapshot, executionMode, applyOutcome);
        } catch (final WorkflowSynchronizationException ex) {
            return failApplySynchronization(workflowSessionContext, snapshot, executionMode, applyOutcome, ex);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            return failApply(workflowSessionContext, snapshot, executionMode, applyOutcome, currentArtifactType, currentArtifactSql, ex);
        }
    }
    
    private void synchronizeAppliedWorkflow(final WorkflowContextSnapshot snapshot, final MCPMetadataQueryFacade metadataQueryFacade, final MCPFeatureQueryFacade queryFacade,
                                            final MCPFeatureExecutionFacade executionFacade, final MCPWorkflowApplySynchronizationHandler workflowApplySynchronizationHandler,
                                            final String sessionId) {
        workflowApplySynchronizationHandler.synchronize(snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId);
    }
    
    private Map<String, Object> completeApply(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final String executionMode,
                                              final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_EXECUTED, WorkflowLifecycle.STATUS_EXECUTED);
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_COMPLETED, snapshot, executionMode, Map.of());
    }
    
    private Map<String, Object> failApply(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final String executionMode,
                                          final WorkflowApplyOutcome applyOutcome, final String artifactType, final String artifactSql, final RuntimeException ex) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_FAILED, WorkflowLifecycle.STATUS_FAILED);
        applyOutcome.addFailedArtifact(resolveIssueCode(artifactType), artifactType, artifactSql, ex.getMessage());
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_FAILED, snapshot, executionMode, Map.of());
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
        return WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL.equalsIgnoreCase(artifactType) ? WorkflowIssueCode.RULE_EXECUTION_FAILED : WorkflowIssueCode.DDL_EXECUTION_FAILED;
    }
    
    private boolean isApproved(final List<String> approvedSteps, final String step) {
        return approvedSteps.isEmpty() || approvedSteps.contains(step);
    }
    
    private List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> createExecutableArtifacts(final WorkflowContextSnapshot snapshot) {
        WorkflowArtifactBundle result = WorkflowArtifactBundle.from(snapshot);
        return WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot) ? result.toRuleExecutableArtifacts() : result.toExecutableArtifacts();
    }
    
    private void executeArtifact(final MCPFeatureExecutionFacade executionFacade, final String sessionId, final WorkflowContextSnapshot snapshot,
                                 final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        executionFacade.execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), artifact.sql(), 0, 0));
    }
    
    private static Map<String, Object> createResponse(final WorkflowContextSnapshot snapshot, final String status, final String executionMode, final List<Map<String, Object>> issues,
                                                      final List<Map<String, Object>> stepResults, final List<String> executedDdl, final List<String> executedDistSql,
                                                      final List<String> skippedArtifacts, final Map<String, Object> manualArtifactPackage) {
        String planId = snapshot.getPlanId();
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("response_mode", resolveResponseMode(status, executionMode));
        result.put(WorkflowFieldNames.PLAN_ID, planId);
        result.put("status", status);
        result.put(WorkflowFieldNames.EXECUTION_MODE, executionMode);
        result.put("issues", issues);
        result.put("step_results", stepResults);
        boolean ruleDistSQLOnlyWorkflow = WorkflowArtifactPayloadUtils.isRuleDistSQLOnlyWorkflow(snapshot);
        if (!ruleDistSQLOnlyWorkflow) {
            result.put("executed_ddl", executedDdl);
        }
        result.put("executed_distsql", executedDistSql);
        result.put("applied_artifacts", createAppliedArtifacts(executedDdl, executedDistSql));
        result.put("skipped_artifacts", skippedArtifacts);
        result.put("manual_artifacts", manualArtifactPackage.isEmpty() ? List.of() : List.of(manualArtifactPackage));
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE, manualArtifactPackage);
        if (WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status) && !manualArtifactPackage.isEmpty()) {
            result.put("manual_artifact_summary", createManualArtifactSummary(planId, manualArtifactPackage, ruleDistSQLOnlyWorkflow));
            result.put("manual_follow_up", createManualFollowUp());
        }
        WorkflowGuidancePayloadBuilder.appendApplyGuidance(result, status);
        return result;
    }
    
    private static String resolveResponseMode(final String status, final String executionMode) {
        if (EXECUTION_MODE_PREVIEW.equals(executionMode)) {
            return MCPResponseMode.PREVIEW;
        }
        if (EXECUTION_MODE_MANUAL_ONLY.equals(executionMode)) {
            return MCPResponseMode.MANUAL_ONLY;
        }
        return WorkflowLifecycle.STATUS_COMPLETED.equals(status) ? MCPResponseMode.EXECUTED : MCPResponseMode.TERMINAL;
    }
    
    private static Map<String, Object> createManualFollowUp() {
        return Map.of(
                "confirmation_required", true,
                "confirmation_field", "manual_artifacts_executed",
                "validation_blocked_until", "manual_artifacts_executed",
                "validation_tool_after_manual_execution", "database_gateway_validate_workflow",
                "safe_independent_read_only_checks", "database_gateway_execute_query may run before manual execution confirmation when the user asked for read-only verification.");
    }
    
    private static Map<String, Object> createManualArtifactSummary(final String planId, final Map<String, Object> manualArtifactPackage, final boolean ruleDistSQLOnlyWorkflow) {
        int distSqlArtifactCount = getCollectionSize(manualArtifactPackage, WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS);
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        if (!ruleDistSQLOnlyWorkflow) {
            int ddlArtifactCount = getCollectionSize(manualArtifactPackage, WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DDL_ARTIFACTS);
            int indexPlanCount = getCollectionSize(manualArtifactPackage, WorkflowArtifactPayloadUtils.PAYLOAD_KEY_INDEX_PLAN);
            result.put("ddl_artifact_count", ddlArtifactCount);
            result.put("index_plan_count", indexPlanCount);
            result.put("total_artifact_count", ddlArtifactCount + indexPlanCount + distSqlArtifactCount);
        } else {
            result.put("total_artifact_count", distSqlArtifactCount);
        }
        result.put("distsql_artifact_count", distSqlArtifactCount);
        result.put("external_execution_required", true);
        result.put("requires_user_confirmation", true);
        result.put("validation_blocked_until", "manual_artifacts_executed");
        result.put("validation_tool_after_manual_execution", "database_gateway_validate_workflow");
        result.put("validation_arguments_after_manual_execution", Map.of(WorkflowFieldNames.PLAN_ID, planId));
        return result;
    }
    
    private static int getCollectionSize(final Map<String, Object> payload, final String key) {
        Object value = payload.get(key);
        return value instanceof Collection ? ((Collection<?>) value).size() : 0;
    }
    
    private static List<String> createAppliedArtifacts(final List<String> executedDdl, final List<String> executedDistSql) {
        List<String> result = new LinkedList<>();
        result.addAll(executedDdl);
        result.addAll(executedDistSql);
        return result;
    }
    
    private static final class WorkflowApplyOutcome {
        
        private final List<Map<String, Object>> stepResults = new LinkedList<>();
        
        private final List<String> executedDdl = new LinkedList<>();
        
        private final List<String> executedDistSql = new LinkedList<>();
        
        private final List<String> skippedArtifacts = new LinkedList<>();
        
        private final List<Map<String, Object>> issues = new LinkedList<>();
        
        private void addSkippedArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
            skippedArtifacts.add(artifact.sql());
            stepResults.add(createStepResult(artifact.artifactType(), WorkflowLifecycle.STATUS_SKIPPED, artifact.sql()));
        }
        
        private void addExecutedArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
            if (artifact.ruleDistSql()) {
                executedDistSql.add(artifact.sql());
            } else {
                executedDdl.add(artifact.sql());
            }
            stepResults.add(createStepResult(artifact.artifactType(), WorkflowLifecycle.STATUS_PASSED, artifact.sql()));
        }
        
        private void addFailedArtifact(final String issueCode, final String artifactType, final String artifactSql, final String errorMessage) {
            stepResults.add(createStepResult(artifactType, WorkflowLifecycle.STATUS_FAILED, artifactSql));
            issues.add(new WorkflowIssue(issueCode, "error", "executing", errorMessage, "Fix the failed artifact and retry execution.", true,
                    Map.of("artifact_type", artifactType, "sql", artifactSql)).toMap());
        }
        
        private void addIssue(final Map<String, Object> issue) {
            issues.add(issue);
        }
        
        private boolean hasSkippedArtifacts() {
            return !skippedArtifacts.isEmpty();
        }
        
        private void addSynchronizationFailure(final String issueCode, final String errorMessage, final List<Map<String, Object>> mismatches) {
            issues.add(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_EXECUTED, errorMessage,
                    "Inspect mismatches and re-run validation after the Proxy state converges.", true, Map.of("mismatches", mismatches)).toMap());
        }
        
        private Map<String, Object> createResponse(final String status, final WorkflowContextSnapshot snapshot, final String executionMode, final Map<String, Object> manualArtifactPackage) {
            return WorkflowExecutionService.createResponse(snapshot, status, executionMode, issues, stepResults, executedDdl, executedDistSql, skippedArtifacts, manualArtifactPackage);
        }
        
        private static Map<String, Object> createStepResult(final String artifactType, final String status, final String sql) {
            Map<String, Object> result = new LinkedHashMap<>(4, 1F);
            result.put("artifact_type", artifactType);
            result.put("status", status);
            result.put("sql", sql);
            return result;
        }
    }
}
