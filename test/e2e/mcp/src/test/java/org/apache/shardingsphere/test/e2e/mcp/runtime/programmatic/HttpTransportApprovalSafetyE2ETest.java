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

package org.apache.shardingsphere.test.e2e.mcp.runtime.programmatic;

import org.apache.shardingsphere.mcp.support.workflow.descriptor.WorkflowToolDescriptors;
import org.apache.shardingsphere.mcp.support.workflow.model.WorkflowIssueCode;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPModelContractAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class HttpTransportApprovalSafetyE2ETest extends AbstractSharedHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertExecuteUpdateWithoutApprovalArgument() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "logic_db", "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("response_mode")), is("executed"));
        assertThat(String.valueOf(payload.get("result_kind")), is("update_count"));
        assertThat(String.valueOf(payload.get("affected_rows")), is("0"));
        assertThat(String.valueOf(payload.get("execution_mode")), is("execute"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertRejectWorkflowReviewThenExecuteWithoutPreview() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> executionPayload = callApplyWorkflow(httpClient, sessionId, createMaskRulePlan(httpClient, sessionId),
                Map.of("execution_mode", "review-then-execute", "approved_steps", List.of("ddl")));
        assertThat(String.valueOf(executionPayload.get("status")), is("failed"));
        assertThat(String.valueOf(executionPayload.get("execution_mode")), is("review-then-execute"));
        Map<?, ?> actualIssue = (Map<?, ?>) ((List<?>) executionPayload.get("issues")).getFirst();
        assertThat(actualIssue.get("code"), is(WorkflowIssueCode.WORKFLOW_STATUS_INVALID));
        assertModelFacingPayloadContract(executionPayload);
    }
    
    @Test
    void assertRejectWorkflowExecutionFromOtherSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String ownerSessionId = initializeSession(httpClient);
        String otherSessionId = initializeSession(httpClient);
        String planId = createMaskRulePlan(httpClient, ownerSessionId);
        Map<String, Object> actual = callApplyWorkflow(httpClient, otherSessionId, planId,
                Map.of("execution_mode", "review-then-execute", "approved_steps", List.of("ddl")));
        getRecoveryPayload(actual, "stale_workflow");
        assertModelFacingPayloadContract(actual);
    }
    
    private Map<String, Object> callApplyWorkflow(final HttpClient httpClient, final String sessionId, final String planId,
                                                  final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> actualArguments = new LinkedHashMap<>(arguments.size() + 1, 1F);
        actualArguments.put("plan_id", planId);
        actualArguments.putAll(arguments);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME, actualArguments);
        assertThat(actual.statusCode(), is(200));
        return getStructuredContent(actual.body());
    }
    
    private String createMaskRulePlan(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_mask_rule", Map.of(
                "database", "logic_db",
                "schema", "logic_db",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "KEEP_FIRST_N_LAST_M",
                "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("status")), is("planned"));
        return String.valueOf(payload.get("plan_id"));
    }
    
    private void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertNoBannedPublicFields(payload);
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
}
