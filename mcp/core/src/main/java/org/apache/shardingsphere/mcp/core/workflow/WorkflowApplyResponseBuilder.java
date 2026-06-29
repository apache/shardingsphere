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

import org.apache.shardingsphere.mcp.support.protocol.MCPResponseMode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowFieldNames;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
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
    
    private String resolveResponseMode(final String status, final String executionMode) {
        if ("preview".equals(executionMode)) {
            return MCPResponseMode.PREVIEW;
        }
        if ("manual-only".equals(executionMode)) {
            return MCPResponseMode.MANUAL_ONLY;
        }
        return WorkflowLifecycle.STATUS_COMPLETED.equals(status) ? MCPResponseMode.EXECUTED : MCPResponseMode.TERMINAL;
    }
    
    private Map<String, Object> createManualFollowUp() {
        return Map.of(
                "confirmation_required", true,
                "confirmation_field", "manual_artifacts_executed",
                "validation_blocked_until", "manual_artifacts_executed",
                "validation_tool_after_manual_execution", "database_gateway_validate_workflow",
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
        result.put("validation_tool_after_manual_execution", "database_gateway_validate_workflow");
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
