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

import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowContextSnapshot;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssue;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * Mutable outcome collected while applying workflow artifacts.
 */
public final class WorkflowApplyOutcome {
    
    private final List<Map<String, Object>> stepResults = new LinkedList<>();
    
    private final List<String> executedDdl = new LinkedList<>();
    
    private final List<String> executedDistSql = new LinkedList<>();
    
    private final List<String> skippedArtifacts = new LinkedList<>();
    
    private final List<Map<String, Object>> issues = new LinkedList<>();
    
    void addSkippedArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        skippedArtifacts.add(artifact.displaySql());
        stepResults.add(createStepResult(artifact.artifactType(), WorkflowLifecycle.STATUS_SKIPPED, artifact.displaySql()));
    }
    
    void addExecutedArtifact(final WorkflowArtifactBundle.ExecutableWorkflowArtifact artifact) {
        if (artifact.ruleDistSql()) {
            executedDistSql.add(artifact.displaySql());
        } else {
            executedDdl.add(artifact.displaySql());
        }
        stepResults.add(createStepResult(artifact.artifactType(), WorkflowLifecycle.STATUS_PASSED, artifact.displaySql()));
    }
    
    void addFailedArtifact(final String issueCode, final String artifactType, final String artifactSql, final String errorMessage) {
        stepResults.add(createStepResult(artifactType, WorkflowLifecycle.STATUS_FAILED, artifactSql));
        issues.add(new WorkflowIssue(issueCode, "error", "executing", errorMessage, "Fix the failed artifact and retry execution.", true,
                Map.of("artifact_type", artifactType, "sql", artifactSql)).toMap());
    }
    
    void addIssue(final Map<String, Object> issue) {
        issues.add(issue);
    }
    
    boolean hasSkippedArtifacts() {
        return !skippedArtifacts.isEmpty();
    }
    
    void addSynchronizationFailure(final String issueCode, final String errorMessage, final List<Map<String, Object>> mismatches) {
        issues.add(new WorkflowIssue(issueCode, "error", WorkflowLifecycle.STEP_EXECUTED, errorMessage,
                "Inspect mismatches and re-run validation after the Proxy state converges.", true, Map.of("mismatches", mismatches)).toMap());
    }
    
    void addSecretReferenceManualExecutionRequired(final String category, final Map<String, Object> secretReferenceSummary) {
        issues.add(new WorkflowIssue(WorkflowIssueCode.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED, "error", WorkflowLifecycle.STEP_REVIEW,
                "Sensitive placeholders require manual execution outside MCP.",
                "Review manual artifacts, replace neutral placeholders outside MCP, and execute them through the normal operational channel.", true,
                Map.of("category", category, "secret_reference_summary", secretReferenceSummary)).toMap());
    }
    
    Map<String, Object> createResponse(final String status, final WorkflowContextSnapshot snapshot, final String executionMode, final Map<String, Object> manualArtifactPackage) {
        return new WorkflowApplyResponseBuilder().build(snapshot, status, executionMode,
                issues, stepResults, executedDdl, executedDistSql, skippedArtifacts, manualArtifactPackage);
    }
    
    private static Map<String, Object> createStepResult(final String artifactType, final String status, final String sql) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("artifact_type", artifactType);
        result.put("status", status);
        result.put("sql", sql);
        return result;
    }
}
