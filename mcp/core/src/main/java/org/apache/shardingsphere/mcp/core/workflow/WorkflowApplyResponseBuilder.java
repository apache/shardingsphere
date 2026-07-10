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

import org.apache.shardingsphere.mcp.support.protocol.MCPNextActionUtils;
import org.apache.shardingsphere.mcp.support.protocol.MCPPayloadFieldNames;
import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowGuidancePayloadBuilder;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Builds workflow apply response payloads.
 */
public final class WorkflowApplyResponseBuilder {
    
    private static final String EXECUTION_MODE_PREVIEW = "preview";
    
    private static final String EXECUTION_MODE_MANUAL_ONLY = "manual-only";
    
    /**
     * Build workflow apply response payload.
     *
     * @param snapshot workflow snapshot
     * @param status workflow status
     * @param executionMode execution mode
     * @param issues workflow issues
     * @param stepResults step results
     * @param executedDdl executed DDL
     * @param executedDistSql executed DistSQL
     * @param skippedArtifacts skipped artifacts
     * @param manualArtifactPackage manual artifact package
     * @return workflow apply response payload
     */
    public Map<String, Object> build(final WorkflowContextSnapshot snapshot, final String status, final String executionMode,
                                     final Collection<Map<String, Object>> issues, final Collection<Map<String, Object>> stepResults,
                                     final Collection<String> executedDdl, final Collection<String> executedDistSql, final Collection<String> skippedArtifacts,
                                     final Map<String, Object> manualArtifactPackage) {
        String planId = snapshot.getPlanId();
        Map<String, Object> result = new LinkedHashMap<>(24, 1F);
        result.put("response_mode", resolveResponseMode(status, executionMode));
        result.put(MCPPayloadFieldNames.SUMMARY, createSummary(planId, status, executionMode, issues, executedDdl, executedDistSql));
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
    
    /**
     * Build workflow preview response payload.
     *
     * @param snapshot workflow snapshot
     * @param executableArtifacts executable workflow artifacts
     * @param applyExecutionMode execution mode to use after preview
     * @param manualArtifactPackage manual artifact package
     * @return workflow preview response payload
     */
    public Map<String, Object> buildPreviewResponse(final WorkflowContextSnapshot snapshot, final Collection<WorkflowArtifactBundle.ExecutableWorkflowArtifact> executableArtifacts,
                                                    final String applyExecutionMode, final Map<String, Object> manualArtifactPackage) {
        List<Map<String, Object>> previewArtifacts = createPreviewArtifacts(executableArtifacts);
        Map<String, Object> result = build(snapshot, EXECUTION_MODE_PREVIEW, EXECUTION_MODE_PREVIEW,
                List.of(), List.of(), List.of(), List.of(), List.of(), manualArtifactPackage);
        result.put("would_apply", false);
        result.put("preview_artifacts", previewArtifacts);
        result.put("review_focus", createPreviewReviewFocus(applyExecutionMode, previewArtifacts));
        String reviewSummary = createReviewSummary(previewArtifacts);
        result.put(MCPPayloadFieldNames.SUMMARY, reviewSummary);
        result.put("review_summary", reviewSummary);
        result.put("argument_provenance", createPreviewArgumentProvenance());
        result.put(MCPPayloadFieldNames.NEXT_ACTIONS, createPreviewNextActions(snapshot, applyExecutionMode, previewArtifacts));
        return result;
    }
    
    private String resolveResponseMode(final String status, final String executionMode) {
        if (EXECUTION_MODE_PREVIEW.equals(executionMode)) {
            return MCPResponseMode.PREVIEW;
        }
        if (EXECUTION_MODE_MANUAL_ONLY.equals(executionMode)) {
            return MCPResponseMode.MANUAL_ONLY;
        }
        return WorkflowLifecycle.STATUS_COMPLETED.equals(status) ? MCPResponseMode.EXECUTED : MCPResponseMode.TERMINAL;
    }
    
    private String createSummary(final String planId, final String status, final String executionMode, final Collection<Map<String, Object>> issues,
                                 final Collection<String> executedDdl, final Collection<String> executedDistSql) {
        if (EXECUTION_MODE_PREVIEW.equals(executionMode)) {
            return String.format("Workflow apply preview is ready for plan `%s`.", planId);
        }
        if (WorkflowLifecycle.STATUS_COMPLETED.equals(status)) {
            return String.format("Workflow apply completed for plan `%s` with %d applied artifact(s).", planId, executedDdl.size() + executedDistSql.size());
        }
        if (WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION.equals(status)) {
            return String.format("Workflow apply exported manual artifacts for plan `%s`; external execution is required before validation.", planId);
        }
        if (WorkflowLifecycle.STATUS_FAILED.equals(status)) {
            return String.format("Workflow apply failed for plan `%s` with %d issue(s).", planId, issues.size());
        }
        return String.format("Workflow apply returned status `%s` for plan `%s`.", status, planId);
    }
    
    private List<Map<String, Object>> createPreviewArtifacts(final Collection<WorkflowArtifactBundle.ExecutableWorkflowArtifact> executableArtifacts) {
        return executableArtifacts.stream().map(this::createPreviewArtifact).toList();
    }
    
    private Map<String, Object> createPreviewArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("approval_step", artifact.approvalStep());
        result.put("artifact_type", artifact.artifactType());
        result.put("sql", artifact.displaySql());
        result.put("side_effect_scope", artifact.ruleDistSql() ? "rule-metadata" : "physical-structure");
        return result;
    }
    
    private Map<String, Object> createPreviewReviewFocus(final String applyExecutionMode, final List<Map<String, Object>> previewArtifacts) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1F);
        result.put("artifact_categories", previewArtifacts.stream().map(each -> (String) each.get("artifact_type")).distinct().toList());
        result.put("side_effect_scope", previewArtifacts.stream().map(each -> (String) each.get("side_effect_scope")).distinct().toList());
        boolean manualOnly = EXECUTION_MODE_MANUAL_ONLY.equals(applyExecutionMode);
        result.put("manual_only", manualOnly);
        if (!manualOnly && !previewArtifacts.isEmpty()) {
            result.put("approval_field", WorkflowFieldNames.APPROVED_STEPS);
            result.put("approval_values", previewArtifacts.stream().map(each -> (String) each.get("approval_step")).distinct().toList());
        }
        return result;
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
    
    private Map<String, Object> createPreviewArgumentProvenance() {
        return Map.of(WorkflowFieldNames.PLAN_ID, "server_generated", WorkflowFieldNames.EXECUTION_MODE, "server_defaulted");
    }
    
    private List<Map<String, Object>> createPreviewNextActions(final WorkflowContextSnapshot snapshot, final String applyExecutionMode,
                                                               final List<Map<String, Object>> previewArtifacts) {
        if (previewArtifacts.isEmpty()) {
            return MCPNextActionUtils.ordered(MCPNextActionUtils.stop("Preview has no artifacts to approve."));
        }
        if (EXECUTION_MODE_MANUAL_ONLY.equals(applyExecutionMode)) {
            return MCPNextActionUtils.ordered(MCPNextActionUtils.callTool(WorkflowToolDescriptors.APPLY_TOOL_NAME,
                    createPreviewNextActionReason(applyExecutionMode), createExecutionArguments(snapshot, applyExecutionMode)));
        }
        return MCPNextActionUtils.ordered(
                MCPNextActionUtils.askUser(createPreviewNextActionReason(applyExecutionMode), List.of(WorkflowFieldNames.APPROVED_STEPS)),
                MCPNextActionUtils.dependsOn(MCPNextActionUtils.callTool(WorkflowToolDescriptors.APPLY_TOOL_NAME,
                        "Apply reviewed workflow artifacts after merging approved_steps from action 1 into the arguments.",
                        createExecutionArguments(snapshot, applyExecutionMode)), 1));
    }
    
    private String createPreviewNextActionReason(final String applyExecutionMode) {
        return EXECUTION_MODE_MANUAL_ONLY.equals(applyExecutionMode)
                ? "Export reviewed workflow artifacts without applying runtime side effects."
                : "Confirm the preview_artifacts.approval_step values to approve before execution.";
    }
    
    private Map<String, Object> createExecutionArguments(final WorkflowContextSnapshot snapshot, final String executionMode) {
        return Map.of(WorkflowFieldNames.PLAN_ID, snapshot.getPlanId(), WorkflowFieldNames.EXECUTION_MODE, executionMode);
    }
    
    private Map<String, Object> createManualFollowUp() {
        return Map.of(
                "confirmation_required", true,
                "confirmation_field", "manual_artifacts_executed",
                "validation_blocked_until", "manual_artifacts_executed",
                "validation_tool_after_manual_execution", WorkflowToolDescriptors.VALIDATE_TOOL_NAME,
                "safe_independent_read_only_checks", "database_gateway_execute_query may run before manual execution confirmation when the user asked for read-only verification.");
    }
    
    private Map<String, Object> createManualArtifactSummary(final String planId, final Map<String, Object> manualArtifactPackage, final boolean ruleDistSQLOnlyWorkflow) {
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
        result.put("validation_tool_after_manual_execution", WorkflowToolDescriptors.VALIDATE_TOOL_NAME);
        result.put("validation_arguments_after_manual_execution", Map.of(WorkflowFieldNames.PLAN_ID, planId));
        return result;
    }
    
    private int getCollectionSize(final Map<String, Object> payload, final String key) {
        Object value = payload.get(key);
        return value instanceof Collection ? ((Collection<?>) value).size() : 0;
    }
    
    private List<String> createAppliedArtifacts(final Collection<String> executedDdl, final Collection<String> executedDistSql) {
        List<String> result = new LinkedList<>();
        result.addAll(executedDdl);
        result.addAll(executedDistSql);
        return result;
    }
}
