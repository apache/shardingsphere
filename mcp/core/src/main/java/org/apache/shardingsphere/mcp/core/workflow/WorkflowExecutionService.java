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

import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureExecutionFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPFeatureQueryFacade;
import org.apache.shardingsphere.mcp.support.database.spi.MCPMetadataQueryFacade;
import org.apache.shardingsphere.mcp.support.database.tool.request.SQLExecutionRequest;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowPropertySource;
import org.apache.shardingsphere.mcp.support.workflow.WorkflowSessionContext;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowGuidancePayloadBuilder;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowLifecycleUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowSynchronizationException;
import org.apache.shardingsphere.mcp.support.workflow.spi.MCPWorkflowApplySynchronizationHandler;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow execution service.
 */
public final class WorkflowExecutionService {
    
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
        Map<String, Object> rejectedResponse = checkApplyPreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        String actualExecutionMode = resolveExecutionMode(executionMode, snapshot);
        WorkflowApplyOutcome applyOutcome = new WorkflowApplyOutcome();
        if (isManualOnly(actualExecutionMode)) {
            return applyManualOnly(workflowSessionContext, snapshot, applyOutcome);
        }
        return applyAutomatically(workflowSessionContext, metadataQueryFacade, queryFacade, executionFacade, workflowApplySynchronizationHandler, sessionId, snapshot, approvedSteps,
                actualExecutionMode, applyOutcome);
    }
    
    private Map<String, Object> checkApplyPreconditions(final String sessionId, final WorkflowContextSnapshot snapshot) {
        if (!WorkflowLifecycleUtils.isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(snapshot, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isApplicableStatus(snapshot)) {
            return createRejectedResponse(snapshot, WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
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
    
    private Map<String, Object> createRejectedResponse(final WorkflowContextSnapshot snapshot, final String issueCode, final String message, final String userAction) {
        return createResponse(snapshot.getPlanId(), WorkflowLifecycle.STATUS_FAILED, resolveExecutionMode("", snapshot),
                List.of(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_REVIEW, message, userAction, false, Map.of()).toMap()), List.of(), List.of(), List.of(), List.of(), Map.of());
    }
    
    private String resolveExecutionMode(final String executionMode, final WorkflowContextSnapshot snapshot) {
        return executionMode.isEmpty() ? resolveSnapshotExecutionMode(snapshot) : executionMode;
    }
    
    private String resolveSnapshotExecutionMode(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getRequest() ? "" : snapshot.getRequest().getExecutionMode();
    }
    
    private boolean isManualOnly(final String executionMode) {
        return "manual-only".equalsIgnoreCase(executionMode);
    }
    
    private Map<String, Object> applyManualOnly(final WorkflowSessionContext workflowSessionContext, final WorkflowContextSnapshot snapshot, final WorkflowApplyOutcome applyOutcome) {
        persistSnapshot(workflowSessionContext, snapshot, WorkflowLifecycle.STEP_MANUAL_EXECUTION, WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION);
        applyOutcome.addIssue(new WorkflowIssue(WorkflowIssueCode.MANUAL_EXECUTION_PENDING, "warning", WorkflowLifecycle.STEP_REVIEW,
                "Artifacts are generated in manual-only mode and will not be executed automatically.", "Execute artifacts manually and run validation afterwards.", true, Map.of()).toMap());
        return applyOutcome.createResponse(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION, snapshot, "manual-only",
                WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, getPropertySource(snapshot)));
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
    
    private String resolveIssueCode(final String artifactType) {
        return WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL.equalsIgnoreCase(artifactType) ? WorkflowIssueCode.RULE_EXECUTION_FAILED : WorkflowIssueCode.DDL_EXECUTION_FAILED;
    }
    
    private boolean isApproved(final List<String> approvedSteps, final String step) {
        return approvedSteps.isEmpty() || approvedSteps.contains(step);
    }
    
    private List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> createExecutableArtifacts(final WorkflowContextSnapshot snapshot) {
        return WorkflowArtifactBundle.from(snapshot).toExecutableArtifacts();
    }
    
    private void executeArtifact(final MCPFeatureExecutionFacade executionFacade, final String sessionId, final WorkflowContextSnapshot snapshot,
                                 final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        executionFacade.execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), artifact.sql(), 0, 0));
    }
    
    private static Map<String, Object> createResponse(final String planId, final String status, final String executionMode, final List<Map<String, Object>> issues,
                                                      final List<Map<String, Object>> stepResults, final List<String> executedDdl, final List<String> executedDistSql,
                                                      final List<String> skippedArtifacts, final Map<String, Object> manualArtifactPackage) {
        Map<String, Object> result = new LinkedHashMap<>(16, 1F);
        result.put("plan_id", planId);
        result.put("status", status);
        result.put("execution_mode", executionMode);
        result.put("issues", issues);
        result.put("step_results", stepResults);
        result.put("executed_ddl", executedDdl);
        result.put("executed_distsql", executedDistSql);
        result.put("applied_artifacts", createAppliedArtifacts(executedDdl, executedDistSql));
        result.put("skipped_artifacts", skippedArtifacts);
        result.put("manual_artifacts", manualArtifactPackage.isEmpty() ? List.of() : List.of(manualArtifactPackage));
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE, manualArtifactPackage);
        WorkflowGuidancePayloadBuilder.appendApplyGuidance(result, status);
        return result;
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
            return WorkflowExecutionService.createResponse(snapshot.getPlanId(), status, executionMode, issues, stepResults, executedDdl, executedDistSql, skippedArtifacts,
                    manualArtifactPackage);
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
