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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpTransportRecoveryE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String RECOVERY_SECRET = "recovery-secret-value";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertRecoverMissingDatabase() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_query", Map.of("schema", "logic_db", "sql", "SELECT * FROM orders"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "invalid_request", "missing_context");
        Map<String, Object> nextAction = getFirstNextAction(recovery);
        assertThat(String.valueOf(nextAction.get("type")), is("resource_read"));
        assertThat(String.valueOf(nextAction.get("resource_uri")), is("shardingsphere://databases"));
        HttpResponse<String> followUp = sendResourceReadRequest(httpClient, sessionId, String.valueOf(nextAction.get("resource_uri")));
        assertThat(followUp.statusCode(), is(200));
        Map<String, Object> resourcePayload = getFirstResourcePayload(followUp.body());
        assertThat(String.valueOf(resourcePayload.get("response_mode")), is("list"));
        assertDatabaseListContains(resourcePayload, "logic_db");
        assertModelFacingPayloadContract(payload);
        assertModelFacingPayloadContract(resourcePayload);
    }
    
    @Test
    void assertRecoverUnsupportedTarget() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "logic_db", "sql", "SELECT * FROM orders", "execution_mode", "preview"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "unsupported", "unsupported_target");
        Map<String, Object> nextAction = getFirstNextAction(recovery);
        assertThat(String.valueOf(nextAction.get("tool_name")), is("database_gateway_execute_query"));
        HttpResponse<String> followUp = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_query", castToMap(nextAction.get("arguments")));
        assertThat(followUp.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(followUp.body()).get("result_kind")), is("result_set"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertRecoverInvalidExecutionMode() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                Map.of("database", "logic_db", "schema", "logic_db", "sql", "UPDATE orders SET status = status WHERE order_id = -1", "execution_mode", "run"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "invalid_request", "invalid_enum");
        Map<String, Object> nextAction = getFirstNextAction(recovery);
        assertThat(String.valueOf(nextAction.get("tool_name")), is("database_gateway_execute_update"));
        Map<String, Object> retryArguments = castToMap(nextAction.get("arguments"));
        assertThat(String.valueOf(retryArguments.get("execution_mode")), is("preview"));
        assertThat(String.valueOf(retryArguments.get("database")), is("logic_db"));
        assertThat(String.valueOf(retryArguments.get("schema")), is("logic_db"));
        HttpResponse<String> retry = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", retryArguments);
        assertThat(retry.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(retry.body()).get("result_kind")), is("preview"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertRecoverWrongSqlTool() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_query",
                Map.of("database", "logic_db", "schema", "logic_db", "sql", "UPDATE orders SET status = status WHERE order_id = -1"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "unsupported", "unsafe_sql");
        Map<String, Object> nextAction = getFirstNextAction(recovery);
        assertThat(String.valueOf(nextAction.get("tool_name")), is("database_gateway_execute_update"));
        Map<String, Object> retryArguments = castToMap(nextAction.get("arguments"));
        assertThat(String.valueOf(retryArguments.get("execution_mode")), is("preview"));
        HttpResponse<String> retry = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", retryArguments);
        assertThat(retry.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(retry.body()).get("result_kind")), is("preview"));
        assertModelFacingPayloadContract(payload);
    }
    
    @Test
    void assertRecoverStaleWorkflowPlan() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME,
                Map.of("plan_id", "plan-missing", "execution_mode", "preview"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "invalid_request", "stale_workflow");
        assertThat(String.valueOf(recovery.get("plan_id")), is("plan-missing"));
        Map<String, Object> nextAction = getFirstNextAction(recovery);
        assertThat(String.valueOf(nextAction.get("type")), is("completion"));
        assertThat(String.valueOf(nextAction.get("argument_name")), is("plan_id"));
        HttpResponse<String> completion = sendCompletionRequest(httpClient, sessionId, createCompletionReference(nextAction), String.valueOf(nextAction.get("argument_name")));
        assertThat(completion.statusCode(), is(200));
        Map<String, Object> completionPayload = getResultPayload(completion);
        assertThat(castToMap(completionPayload.get("completion")).get("total"), is(0));
        assertModelFacingPayloadContract(payload);
        assertModelFacingPayloadContract(completionPayload);
    }
    
    @Test
    void assertWorkflowRecoveryRedactsEncryptSecret() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> planResponse = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_encrypt_rule", createEncryptRulePlanArguments());
        assertThat(planResponse.statusCode(), is(200));
        assertFalse(planResponse.body().contains(RECOVERY_SECRET));
        String planId = String.valueOf(getStructuredContent(planResponse.body()).get("plan_id"));
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, WorkflowToolDescriptors.APPLY_TOOL_NAME, Map.of("plan_id", planId));
        assertThat(actual.statusCode(), is(200));
        assertFalse(actual.body().contains(RECOVERY_SECRET));
        Map<String, Object> payload = getStructuredContent(actual.body());
        Map<String, Object> recovery = assertRecoveryCategory(payload, "invalid_request", "missing_context");
        assertThat(String.valueOf(recovery.get("category")), is("missing_execution_mode"));
        assertModelFacingPayloadContract(payload);
    }
    
    private Map<String, Object> assertRecoveryCategory(final Map<String, Object> payload, final String expectedErrorCode, final String expectedRecoveryCategory) {
        assertThat(String.valueOf(payload.get("error_code")), is(expectedErrorCode));
        Map<String, Object> result = castToMap(payload.get("recovery"));
        assertThat(String.valueOf(result.get("recovery_category")), is(expectedRecoveryCategory));
        return result;
    }
    
    private Map<String, Object> getFirstNextAction(final Map<String, Object> recovery) {
        return castToMapList(recovery.get("next_actions")).iterator().next();
    }
    
    private void assertDatabaseListContains(final Map<String, Object> payload, final String expectedDatabase) {
        assertTrue(castToMapList(payload.get("items")).stream().anyMatch(each -> expectedDatabase.equals(each.get("database"))));
    }
    
    private HttpResponse<String> sendCompletionRequest(final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                                       final String argumentName) throws IOException, InterruptedException {
        Map<String, Object> params = new LinkedHashMap<>(2, 1F);
        params.put("ref", reference);
        params.put("argument", Map.of("name", argumentName, "value", ""));
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "completion-1", "completion/complete", params));
    }
    
    private Map<String, Object> createCompletionReference(final Map<String, Object> action) {
        String referenceType = Objects.toString(action.get("reference_type"), "");
        String reference = Objects.toString(action.get("reference"), "");
        return "ref/resource".equals(referenceType) ? Map.of("type", referenceType, "uri", reference) : Map.of("type", referenceType, "name", reference);
    }
    
    private Map<String, Object> createEncryptRulePlanArguments() {
        return Map.of(
                "database", "logic_db",
                "schema", "logic_db",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "AES",
                "primary_algorithm_properties", Map.of("aes-key-value", RECOVERY_SECRET),
                "structured_intent_evidence", Map.of("requires_equality_filter", false, "requires_like_query", false));
    }
    
    private Map<String, Object> getResultPayload(final HttpResponse<String> response) {
        return castToMap(parseJsonBody(response.body()).get("result"));
    }
    
    private void assertModelFacingPayloadContract(final Map<String, Object> payload) {
        MCPModelContractAssertions.assertNoBannedPublicFields(payload);
        MCPModelContractAssertions.assertCanonicalNextActionLists(payload);
    }
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
}
