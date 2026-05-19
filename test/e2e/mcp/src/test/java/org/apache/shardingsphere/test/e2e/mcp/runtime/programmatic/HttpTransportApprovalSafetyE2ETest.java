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
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class HttpTransportApprovalSafetyE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertExecuteUpdateWithoutApprovalArgument() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "execute"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("response_mode")), is("executed"));
        assertThat(String.valueOf(payload.get("result_kind")), is("update_count"));
        assertThat(String.valueOf(payload.get("affected_rows")), is("0"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertExecuteUpdateReturnsExecutionMode() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = status WHERE order_id = -1",
                        "execution_mode", "execute"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("execution_mode")), is("execute"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertPreviewExecuteUpdateWithoutExecution() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "preview"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("preview"));
        assertFalse((Boolean) payload.get("would_execute"));
        List<Map<String, Object>> nextActions = castToMapList(payload.get("next_actions"));
        assertThat(nextActions.size(), is(1));
        assertThat(String.valueOf(nextActions.get(0).get("type")), is("tool_call"));
        assertThat(String.valueOf(nextActions.get(0).get("tool_name")), is("database_gateway_execute_update"));
        assertThat(String.valueOf(castToMap(nextActions.get(0).get("arguments")).get("execution_mode")), is("execute"));
        assertFalse(payload.containsKey("requires_user_approval"));
        assertFalse(nextActions.get(0).containsKey("requires_user_approval"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertApplyWorkflowPreviewManualAndExecuteModes() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> previewPayload = callApplyWorkflow(httpClient, sessionId, createMaskRulePlan(httpClient, sessionId), Map.of("execution_mode", "preview"));
        assertThat(String.valueOf(previewPayload.get("status")), is("preview"));
        assertFalse((Boolean) previewPayload.get("would_apply"));
        assertFalse(previewPayload.containsKey("requires_user_approval"));
        Map<String, Object> manualPayload = callApplyWorkflow(httpClient, sessionId, createMaskRulePlan(httpClient, sessionId), Map.of("execution_mode", "manual-only"));
        assertThat(String.valueOf(manualPayload.get("status")), is("awaiting-manual-execution"));
        assertThat(String.valueOf(manualPayload.get("execution_mode")), is("manual-only"));
        Map<String, Object> executionPayload = callApplyWorkflow(httpClient, sessionId, createMaskRulePlan(httpClient, sessionId),
                Map.of("execution_mode", "review-then-execute", "approved_steps", List.of("ddl")));
        assertThat(String.valueOf(executionPayload.get("status")), is("completed"));
        assertThat(String.valueOf(executionPayload.get("execution_mode")), is("review-then-execute"));
        assertThat(((List<?>) executionPayload.get("skipped_artifacts")).size(), is(1));
        assertModelFacingPayloadContract(previewPayload);
        assertModelFacingPayloadContract(manualPayload);
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
        assertThat(String.valueOf(actual.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(castToMap(actual.get("recovery")).get("recovery_category")), is("stale_workflow"));
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
                "schema", "public",
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
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
}
