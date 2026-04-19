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

import org.apache.shardingsphere.mcp.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.tool.model.workflow.DDLArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.IndexPlan;
import org.apache.shardingsphere.mcp.tool.model.workflow.RuleArtifact;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssue;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
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
        WorkflowContextStore actualContextStore = resolveContextStore(requestContext);
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
                snapshot.getInteractionPlan().setCurrentStep("manual-execution");
            }
            snapshot.setStatus("awaiting-manual-execution");
            actualContextStore.save(snapshot);
            issues.add(new WorkflowIssue(WorkflowIssueCode.MANUAL_EXECUTION_PENDING, "warning", "review",
                    "Artifacts are generated in manual-only mode and will not be executed automatically.", "Execute artifacts manually and run validation afterwards.", true, Map.of()).toMap());
            result.put("status", "awaiting-manual-execution");
            result.put("issues", issues);
            result.put("step_results", stepResults);
            result.put("executed_ddl", executedDdl);
            result.put("executed_distsql", executedDistSql);
            result.put("skipped_artifacts", skippedArtifacts);
            result.put("manual_artifact_package", createManualArtifactPackage(snapshot));
            return result;
        }
        try {
            for (DDLArtifact each : snapshot.getDdlArtifacts()) {
                currentArtifactType = each.getArtifactType();
                currentArtifactSql = each.getSql();
                if (!isApproved(approvedSteps, "ddl")) {
                    skippedArtifacts.add(each.getSql());
                    stepResults.add(createStepResult(each.getArtifactType(), "skipped", each.getSql()));
                    continue;
                }
                requestContext.getExecutionFacade().execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), each.getSql(), 0, 0));
                executedDdl.add(each.getSql());
                stepResults.add(createStepResult(each.getArtifactType(), "passed", each.getSql()));
            }
            for (IndexPlan each : snapshot.getIndexPlans()) {
                currentArtifactType = "create-index";
                currentArtifactSql = each.getSql();
                if (!isApproved(approvedSteps, "index_ddl")) {
                    skippedArtifacts.add(each.getSql());
                    stepResults.add(createStepResult("create-index", "skipped", each.getSql()));
                    continue;
                }
                requestContext.getExecutionFacade().execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), each.getSql(), 0, 0));
                executedDdl.add(each.getSql());
                stepResults.add(createStepResult("create-index", "passed", each.getSql()));
            }
            for (RuleArtifact each : snapshot.getRuleArtifacts()) {
                currentArtifactType = "rule_distsql";
                currentArtifactSql = each.getSql();
                if (!isApproved(approvedSteps, "rule_distsql")) {
                    skippedArtifacts.add(each.getSql());
                    stepResults.add(createStepResult("rule_distsql", "skipped", each.getSql()));
                    continue;
                }
                requestContext.getExecutionFacade().execute(new SQLExecutionRequest(sessionId, snapshot.getRequest().getDatabase(), snapshot.getRequest().getSchema(), each.getSql(), 0, 0));
                executedDistSql.add(each.getSql());
                stepResults.add(createStepResult("rule_distsql", "passed", each.getSql()));
            }
            if (null != snapshot.getInteractionPlan()) {
                snapshot.getInteractionPlan().setCurrentStep("executed");
            }
            snapshot.setStatus("executed");
            actualContextStore.save(snapshot);
            result.put("status", "completed");
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            String issueCode = resolveIssueCode(currentArtifactType);
            stepResults.add(createStepResult(currentArtifactType, "failed", currentArtifactSql));
            issues.add(new WorkflowIssue(issueCode, "error", "executing", ex.getMessage(), "Fix the failed artifact and retry execution.", true,
                    Map.of("artifact_type", currentArtifactType, "sql", currentArtifactSql)).toMap());
            if (null != snapshot.getInteractionPlan()) {
                snapshot.getInteractionPlan().setCurrentStep("failed");
            }
            snapshot.setStatus("failed");
            actualContextStore.save(snapshot);
            result.put("status", "failed");
        }
        result.put("issues", issues);
        result.put("step_results", stepResults);
        result.put("executed_ddl", executedDdl);
        result.put("executed_distsql", executedDistSql);
        result.put("skipped_artifacts", skippedArtifacts);
        result.put("manual_artifact_package", Map.of());
        return result;
    }
    
    private WorkflowContextStore resolveContextStore(final MCPFeatureContext requestContext) {
        return null == contextStore ? requestContext.getWorkflowContextStore() : contextStore;
    }
    
    private Map<String, Object> checkApplyPreconditions(final String sessionId, final WorkflowContextSnapshot snapshot) {
        if (!isOwnedBySession(sessionId, snapshot)) {
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
    
    private boolean isOwnedBySession(final String sessionId, final WorkflowContextSnapshot snapshot) {
        return WorkflowSqlUtils.trimToEmpty(snapshot.getSessionId()).isEmpty() || snapshot.getSessionId().equals(sessionId);
    }
    
    private boolean isApplicableStatus(final WorkflowContextSnapshot snapshot) {
        String actualStatus = WorkflowSqlUtils.trimToEmpty(snapshot.getStatus());
        if ("planned".equalsIgnoreCase(actualStatus) || "awaiting-manual-execution".equalsIgnoreCase(actualStatus)) {
            return true;
        }
        if (!"failed".equalsIgnoreCase(actualStatus)) {
            return false;
        }
        String currentStep = resolveCurrentStep(snapshot);
        return "failed".equalsIgnoreCase(currentStep) || "validated".equalsIgnoreCase(currentStep);
    }
    
    private String resolveCurrentStep(final WorkflowContextSnapshot snapshot) {
        return null == snapshot.getInteractionPlan() ? "" : WorkflowSqlUtils.trimToEmpty(snapshot.getInteractionPlan().getCurrentStep());
    }
    
    private Map<String, Object> createRejectedResponse(final String issueCode, final String message, final String userAction) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("status", "failed");
        result.put("issues", List.of(new WorkflowIssue(issueCode, "error", "review", message, userAction, false, Map.of()).toMap()));
        result.put("step_results", List.of());
        result.put("executed_ddl", List.of());
        result.put("executed_distsql", List.of());
        result.put("skipped_artifacts", List.of());
        result.put("manual_artifact_package", Map.of());
        return result;
    }
    
    private String resolveIssueCode(final String artifactType) {
        return "rule_distsql".equalsIgnoreCase(artifactType) ? WorkflowIssueCode.RULE_EXECUTION_FAILED : WorkflowIssueCode.DDL_EXECUTION_FAILED;
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
    
    private Map<String, Object> createManualArtifactPackage(final WorkflowContextSnapshot snapshot) {
        WorkflowPropertySource propertySource = WorkflowPropertySources.compose(snapshot.getRequest(), snapshot.getFeatureData());
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("ddl_artifacts", snapshot.getDdlArtifacts().stream().map(DDLArtifact::toMap).toList());
        result.put("index_plan", snapshot.getIndexPlans().stream().map(IndexPlan::toMap).toList());
        result.put("distsql_artifacts", snapshot.getRuleArtifacts().stream()
                .map(each -> WorkflowArtifactMaskUtils.createMaskedRuleArtifactMap(each, propertySource, snapshot.getPropertyRequirements())).toList());
        return result;
    }
}
