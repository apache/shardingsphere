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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WorkflowApplyOutcomeTest {
    
    @Test
    void assertCreateResponseWithExecutedAndSkippedArtifacts() {
        WorkflowApplyOutcome outcome = new WorkflowApplyOutcome();
        outcome.addExecutedArtifact(new WorkflowArtifactBundle.ExecutableWorkflowArtifact(
                WorkflowArtifactPayloadUtils.STEP_DDL, "add-column", "ALTER TABLE orders ADD COLUMN phone VARCHAR(32)", false));
        outcome.addExecutedArtifact(new WorkflowArtifactBundle.ExecutableWorkflowArtifact(
                WorkflowArtifactPayloadUtils.STEP_RULE_DISTSQL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_RULE_DISTSQL,
                "CREATE MASK RULE orders", true));
        outcome.addSkippedArtifact(new WorkflowArtifactBundle.ExecutableWorkflowArtifact(
                WorkflowArtifactPayloadUtils.STEP_INDEX_DDL, WorkflowArtifactPayloadUtils.ARTIFACT_TYPE_CREATE_INDEX,
                "CREATE INDEX idx_orders_phone ON orders(phone)", false));
        Map<String, Object> actual = outcome.createResponse(WorkflowLifecycle.STATUS_COMPLETED, createSnapshot(), "review-then-execute", Map.of());
        assertThat(((List<?>) actual.get("executed_ddl")).size(), is(1));
        assertThat(((List<?>) actual.get("executed_distsql")).size(), is(1));
        assertThat(((List<?>) actual.get("skipped_artifacts")).size(), is(1));
        assertTrue(outcome.hasSkippedArtifacts());
        assertThat(((Map<?, ?>) ((List<?>) actual.get("step_results")).get(2)).get("status"), is(WorkflowLifecycle.STATUS_SKIPPED));
    }
    
    @Test
    void assertCreateResponseWithFailedArtifact() {
        WorkflowApplyOutcome outcome = new WorkflowApplyOutcome();
        outcome.addFailedArtifact(WorkflowIssueCode.DDL_EXECUTION_FAILED, "add-column", "ALTER TABLE orders ADD COLUMN phone VARCHAR(32)", "ddl failed");
        Map<String, Object> actual = outcome.createResponse(WorkflowLifecycle.STATUS_FAILED, createSnapshot(), "review-then-execute", Map.of());
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actual.get("issues")).getFirst();
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.DDL_EXECUTION_FAILED));
        assertThat(actualIssue.get("message"), is("ddl failed"));
        assertThat(((Map<?, ?>) ((List<?>) actual.get("step_results")).getFirst()).get("status"), is(WorkflowLifecycle.STATUS_FAILED));
    }
    
    @Test
    void assertCreateResponseWithSecretReferenceIssue() {
        WorkflowApplyOutcome outcome = new WorkflowApplyOutcome();
        outcome.addSecretReferenceManualExecutionRequired("secret_reference_manual_execution_required", Map.of("total", 1));
        Map<String, Object> actual = outcome.createResponse(WorkflowLifecycle.STATUS_FAILED, createSnapshot(), "review-then-execute", Map.of());
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) actual.get("issues")).getFirst();
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.SECRET_REFERENCE_MANUAL_EXECUTION_REQUIRED));
        assertThat(((Map<?, ?>) actualIssue.get("details")).get("category"), is("secret_reference_manual_execution_required"));
    }
    
    private WorkflowContextSnapshot createSnapshot() {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf("encrypt.table"));
        return result;
    }
}
