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

import lombok.Getter;
import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.tool.request.SQLExecutionRequest;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Workflow execution service.
 */
public final class WorkflowExecutionService {
    
    private final WorkflowContextStore contextStore;
    
    public WorkflowExecutionService() {
        this(null);
    }
    
    WorkflowExecutionService(final WorkflowContextStore contextStore) {
        this.contextStore = contextStore;
    }
    
    /**
     * Apply workflow artifacts.
     *
     * @param requestContext request context
     * @param sessionId session id
     * @param planId plan identifier
     * @param approvedSteps approved steps
     * @param executionMode execution mode override
     * @return apply payload
     */
    public Map<String, Object> apply(final MCPFeatureContext requestContext, final String sessionId, final String planId,
                                     final List<String> approvedSteps, final String executionMode) {
        WorkflowContextStore actualContextStore = WorkflowLifecycleUtils.resolveContextStore(contextStore, requestContext);
        WorkflowContextSnapshot snapshot = actualContextStore.getRequired(planId);
        Map<String, Object> rejectedResponse = checkApplyPreconditions(sessionId, snapshot);
        if (!rejectedResponse.isEmpty()) {
            return rejectedResponse;
        }
        String actualExecutionMode = WorkflowSqlUtils.trimToEmpty(executionMode).isEmpty() ? snapshot.getRequest().getExecutionMode() : executionMode;
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        List<Map<String, Object>> stepResults = new LinkedList<>();
        List<String> executedDdl = new LinkedList<>();
        List<String> executedDistSql = new LinkedList<>();
        List<String> skippedArtifacts = new LinkedList<>();
        List<Map<String, Object>> issues = new LinkedList<>();
        String currentArtifactType = "";
        String currentArtifactSql = "";
        if ("manual-only".equalsIgnoreCase(actualExecutionMode)) {
            if (null != snapshot.getInteractionPlan()) {
                snapshot.getInteractionPlan().setCurrentStep(WorkflowLifecycle.STEP_MANUAL_EXECUTION);
            }
            snapshot.setStatus(WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION);
            actualContextStore.save(snapshot);
            issues.add(new WorkflowIssue(WorkflowIssueCode.MANUAL_EXECUTION_PENDING, "warning", WorkflowLifecycle.STEP_REVIEW,
                    "Artifacts are generated in manual-only mode and will not be executed automatically.", "Execute artifacts manually and run validation afterwards.", true, Map.of()).toMap());
            result.put("status", WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION);
            result.put("issues", issues);
            result.put("step_results", stepResults);
            result.put("executed_ddl", executedDdl);
            result.put("executed_distsql", executedDistSql);
            result.put("skipped_artifacts", skippedArtifacts);
            result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE,
                    WorkflowArtifactPayloadUtils.createArtifactPayload(snapshot, WorkflowPropertySources.compose(snapshot.getRequest(), snapshot.getFeatureData())));
            return result;
        }
        try {
            for (ExecutableArtifact each : createExecutableArtifacts(snapshot)) {
                currentArtifactType = each.getArtifactType();
                currentArtifactSql = each.getSql();
                if (!isApproved(approvedSteps, each.getApprovalStep())) {
                    skippedArtifacts.add(each.getSql());
                    stepResults.add(createStepResult(each.getArtifactType(), WorkflowLifecycle.STATUS_SKIPPED, each.getSql()));
                    continue;
                }
                executeArtifact(requestContext, sessionId, snapshot, each);
                addExecutedSql(each, executedDdl, executedDistSql);
                stepResults.add(createStepResult(each.getArtifactType(), WorkflowLifecycle.STATUS_PASSED, each.getSql()));
            }
            if (null != snapshot.getInteractionPlan()) {
                snapshot.getInteractionPlan().setCurrentStep(WorkflowLifecycle.STEP_EXECUTED);
            }
            snapshot.setStatus(WorkflowLifecycle.STATUS_EXECUTED);
            actualContextStore.save(snapshot);
            result.put("status", WorkflowLifecycle.STATUS_COMPLETED);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            String issueCode = resolveIssueCode(currentArtifactType);
            stepResults.add(createStepResult(currentArtifactType, WorkflowLifecycle.STATUS_FAILED, currentArtifactSql));
            issues.add(new WorkflowIssue(issueCode, "error", "executing", ex.getMessage(), "Fix the failed artifact and retry execution.", true,
                    Map.of("artifact_type", currentArtifactType, "sql", currentArtifactSql)).toMap());
            if (null != snapshot.getInteractionPlan()) {
                snapshot.getInteractionPlan().setCurrentStep(WorkflowLifecycle.STEP_FAILED);
            }
            snapshot.setStatus(WorkflowLifecycle.STATUS_FAILED);
            actualContextStore.save(snapshot);
            result.put("status", WorkflowLifecycle.STATUS_FAILED);
        }
        result.put("issues", issues);
        result.put("step_results", stepResults);
        result.put("executed_ddl", executedDdl);
        result.put("executed_distsql", executedDistSql);
        result.put("skipped_artifacts", skippedArtifacts);
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE, Map.of());
        return result;
    }
    
    private List<ExecutableArtifact> createExecutableArtifacts(final WorkflowContextSnapshot snapshot) {
        List<ExecutableArtifact> result = new LinkedList<>();
        for (DDLArtifact each : snapshot.getDdlArtifacts()) {
            result.add(new ExecutableArtifact(WorkflowArtifactPayloadUtils.STEP_DDL, each.getArtifactType(), each.getSql(), false));
        }
        for (IndexPlan each : snapshot.getIndexPlans()) {
            result.add(new ExecutableArtifact(WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_CREATE_INDEX, each.getSql(), false));
        }
        for (RuleArtifact each : snapshot.getRuleArtifacts()) {
            result.add(new ExecutableArtifact(WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL, each.getSql(), true));
        }
        return result;
    }
    
    private void executeArtifact(final MCPFeatureContext requestContext, final String sessionId, final WorkflowContextSnapshot snapshot, final ExecutableArtifact artifact) {
        requestContext.getExecutionFacade().execute(
                new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), artifact.getSql(), 0, 0));
    }
    
    private void addExecutedSql(final ExecutableArtifact artifact, final List<String> executedDdl, final List<String> executedDistSql) {
        if (artifact.isRuleDistSql()) {
            executedDistSql.add(artifact.getSql());
            return;
        }
        executedDdl.add(artifact.getSql());
    }
    
    private Map<String, Object> checkApplyPreconditions(final String sessionId, final WorkflowContextSnapshot snapshot) {
        if (!WorkflowLifecycleUtils.isOwnedBySession(sessionId, snapshot)) {
            return createRejectedResponse(WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH, "The workflow plan belongs to another MCP session.",
                    "Continue the workflow from the same session that created the plan.");
        }
        if (!isApplicableStatus(snapshot)) {
            return createRejectedResponse(WorkflowIssueCode.WORKFLOW_STATUS_INVALID,
                    String.format("Workflow status `%s` cannot enter apply in the current lifecycle.", snapshot.getStatus()),
                    "Plan the workflow again or continue from a reviewable status.");
        }
        return Map.of();
    }
    
    private boolean isApplicableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = WorkflowSqlUtils.trimToEmpty(snapshot.getStatus());
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
    
    private Map<String, Object> createRejectedResponse(final String issueCode, final String message, final String userAction) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", WorkflowLifecycle.STATUS_FAILED);
        result.put("issues", List.of(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_REVIEW, message, userAction, false, Map.of()).toMap()));
        result.put("step_results", List.of());
        result.put("executed_ddl", List.of());
        result.put("executed_distsql", List.of());
        result.put("skipped_artifacts", List.of());
        result.put(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE, Map.of());
        return result;
    }
    
    private String resolveIssueCode(final String artifactType) {
        return WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL.equalsIgnoreCase(artifactType) ? WorkflowIssueCode.RULE_EXECUTION_FAILED : WorkflowIssueCode.DDL_EXECUTION_FAILED;
    }
    
    private boolean isApproved(final List<String> approvedSteps, final String step) {
        return approvedSteps.isEmpty() || approvedSteps.contains(step);
    }
    
    private Map<String, Object> createStepResult(final String artifactType, final String status, final String sql) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("artifact_type", artifactType);
        result.put("status", status);
        result.put("sql", sql);
        return result;
    }
    
    @Getter
    private static final class ExecutableArtifact {
        
        private final String approvalStep;
        
        private final String artifactType;
        
        private final String sql;
        
        private final boolean ruleDistSql;
        
        ExecutableArtifact(final String approvalStep, final String artifactType, final String sql, final boolean ruleDistSql) {
            this.approvalStep = approvalStep;
            this.artifactType = artifactType;
            this.sql = sql;
            this.ruleDistSql = ruleDistSql;
        }
    }
}
