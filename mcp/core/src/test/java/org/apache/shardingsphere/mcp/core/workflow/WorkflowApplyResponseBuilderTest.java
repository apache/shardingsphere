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
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowKind;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowLifecycle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactBundle;
import org.apache.shardingsphere.mcp.support.workflow.service.WorkflowArtifactPayloadUtils;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowApplyResponseBuilderTest {
    
    @Test
    void assertBuildPreviewResponse() {
        List<WorkflowArtifactBundle.ExecutableWorkflowArtifact> executableArtifacts = List.of(
                new WorkflowArtifactBundle.ExecutableWorkflowArtifact("CREATE MASK RULE orders", "CREATE MASK RULE orders"),
                new WorkflowArtifactBundle.ExecutableWorkflowArtifact("ALTER MASK RULE orders", "ALTER MASK RULE orders"));
        Map<String, Object> actual = new WorkflowApplyResponseBuilder().buildPreviewResponse(createSnapshot("mask.rule"), executableArtifacts, "review-then-execute", Map.of());
        assertThat(actual.get("response_mode"), is("preview"));
        assertThat(actual.get("summary"), is("Previewed 2 workflow artifacts with side-effect scope rule-metadata. Nothing has been applied."));
        assertThat(actual.get("plan_id"), is("plan-1"));
        assertThat(actual.get("status"), is("preview"));
        assertThat(actual.get("execution_mode"), is("preview"));
        assertThat(actual.get("manual_artifacts"), is(List.of()));
        assertThat(actual.get(WorkflowArtifactPayloadUtils.PAYLOAD_KEY_MANUAL_ARTIFACT_PACKAGE), is(Map.of()));
        assertThat(((List<?>) actual.get("preview_artifacts")).size(), is(2));
        Map<?, ?> actualReviewFocus = (Map<?, ?>) actual.get("review_focus");
        assertThat(actualReviewFocus.get("artifact_categories"), is(List.of("rule_distsql")));
        assertThat(actualReviewFocus.get("side_effect_scope"), is(List.of("rule-metadata")));
        assertThat(actualReviewFocus.get("approval_values"), is(List.of("rule_distsql")));
        assertThat(((List<?>) actual.get("next_actions")).size(), is(2));
    }
    
    @Test
    void assertBuildCompletedResponse() {
        Map<String, Object> actual = new WorkflowApplyResponseBuilder().build(createSnapshot("encrypt.rule"), WorkflowLifecycle.STATUS_COMPLETED, "review-then-execute",
                List.of(), List.of(), List.of("CREATE ENCRYPT RULE orders"), Map.of());
        assertThat(actual.get("response_mode"), is("executed"));
        assertThat(actual.get("summary"), is("Workflow apply completed for plan `plan-1` with 1 applied artifact(s)."));
        assertThat(actual.get("executed_distsql"), is(List.of("CREATE ENCRYPT RULE orders")));
        assertThat(actual.get("applied_artifacts"), is(List.of("CREATE ENCRYPT RULE orders")));
        assertThat(((List<?>) actual.get("next_actions")).size(), is(1));
    }
    
    @Test
    void assertBuildManualResponse() {
        Map<String, Object> manualArtifactPackage = Map.of(
                WorkflowArtifactPayloadUtils.PAYLOAD_KEY_DISTSQL_ARTIFACTS, List.of(Map.of("sql", "CREATE ENCRYPT RULE orders")));
        Map<String, Object> actual = new WorkflowApplyResponseBuilder().build(createSnapshot("encrypt.rule"), WorkflowLifecycle.STATUS_AWAITING_MANUAL_EXECUTION, "manual-only",
                List.of(), List.of(), List.of(), manualArtifactPackage);
        assertThat(actual.get("response_mode"), is("manual_only"));
        assertThat(actual.get("summary"), is("Workflow apply exported manual artifacts for plan `plan-1`; external execution is required before validation."));
        assertThat(actual.get("manual_artifacts"), is(List.of(manualArtifactPackage)));
        Map<?, ?> actualSummary = (Map<?, ?>) actual.get("manual_artifact_summary");
        assertThat(actualSummary.get("distsql_artifact_count"), is(1));
        assertThat(actualSummary.get("total_artifact_count"), is(1));
        assertThat(((Map<?, ?>) actual.get("manual_follow_up")).get("confirmation_field"), is("manual_artifacts_executed"));
    }
    
    private WorkflowContextSnapshot createSnapshot(final String workflowKind) {
        WorkflowContextSnapshot result = new WorkflowContextSnapshot();
        result.setPlanId("plan-1");
        result.setWorkflowKind(WorkflowKind.valueOf(workflowKind));
        return result;
    }
}
