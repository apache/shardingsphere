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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.http;

import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.bootstrap.fixture.FeatureWorkflowRuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport.H2AccessMode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpFeatureWorkflowIT extends AbstractStreamableHttpIT {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = FeatureWorkflowRuntimeTestSupport.createJdbcUrl(getTempDir(), "feature-http", H2AccessMode.SINGLE_PROCESS);
        FeatureWorkflowRuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return FeatureWorkflowRuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertEncryptWorkflowLifecycle() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> algorithmsPayload = readResourceAndGetPayload(session, "shardingsphere://features/encrypt/algorithms");
        List<Map<String, Object>> algorithmItems = getPayloadItems(algorithmsPayload);
        assertThat(algorithmItems.size(), is(2));
        assertTrue(algorithmItems.stream().anyMatch(each -> "AES".equals(each.get("type"))));
        assertEncryptPlanApplyValidate(session, createEncryptCreatePlanningArguments());
        String cipherColumnName = assertEncryptRuleState(session, "AES");
        assertEncryptPlanApplyValidate(session, createEncryptAlterPlanningArguments());
        assertThat(assertEncryptRuleState(session, "MD5"), is(cipherColumnName));
        assertEncryptPlanApplyValidate(session, createEncryptDropPlanningArguments());
        assertEncryptRulesRemoved(session);
    }
    
    @Test
    void assertEncryptWorkflowRejectsAlterWithoutExistingRule() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = callToolAndGetStructuredContent(session, "plan_encrypt_rule", createEncryptAlterPlanningArguments());
        assertThat(planPayload.get("status"), is("failed"));
        assertIssueCode(planPayload, WorkflowIssueCode.RULE_STATE_MISMATCH);
    }
    
    @Test
    void assertEncryptWorkflowAwaitsManualExecution() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_encrypt_rule", createEncryptCreatePlanningArguments());
        Map<String, Object> applyPayload = callToolAndGetStructuredContent(session, "apply_encrypt_rule",
                Map.of("plan_id", getPlanId(planPayload), "approved_steps", List.of("ddl", "rule_distsql"), "execution_mode", "manual-only"));
        assertThat(applyPayload.get("status"), is("awaiting-manual-execution"));
        assertIssueCode(applyPayload, WorkflowIssueCode.MANUAL_EXECUTION_PENDING);
        assertThat(getStringList(applyPayload, "executed_ddl").size(), is(0));
        assertThat(getStringList(applyPayload, "executed_distsql").size(), is(0));
        assertTrue(String.valueOf(getMapList(getMapField(applyPayload, "manual_artifact_package"), "distsql_artifacts").get(0).get("sql")).contains("******"));
        assertEncryptRulesRemoved(session);
    }
    
    @Test
    void assertEncryptWorkflowRejectsApplyFromAnotherSession() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_encrypt_rule", createEncryptCreatePlanningArguments());
        String anotherSessionId = createAdditionalSessionId(session.httpClient(), "encrypt-apply-session-2");
        Map<String, Object> applyPayload = callToolAndGetStructuredContent(session.httpClient(), anotherSessionId, "apply_encrypt_rule",
                Map.of("plan_id", getPlanId(planPayload), "approved_steps", List.of("ddl", "rule_distsql")));
        assertThat(applyPayload.get("status"), is("failed"));
        assertIssueCode(applyPayload, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH);
        assertEncryptRulesRemoved(session);
    }
    
    @Test
    void assertEncryptWorkflowRejectsValidateFromAnotherSession() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_encrypt_rule", createEncryptCreatePlanningArguments());
        String anotherSessionId = createAdditionalSessionId(session.httpClient(), "encrypt-validate-session-2");
        Map<String, Object> validatePayload = callToolAndGetStructuredContent(session.httpClient(), anotherSessionId, "validate_encrypt_rule",
                Map.of("plan_id", getPlanId(planPayload)));
        assertThat(validatePayload.get("status"), is("failed"));
        assertIssueCode(validatePayload, WorkflowIssueCode.SESSION_OWNERSHIP_MISMATCH);
    }
    
    @Test
    void assertEncryptWorkflowRejectsValidateBeforeApply() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_encrypt_rule", createEncryptCreatePlanningArguments());
        Map<String, Object> validatePayload = callToolAndGetStructuredContent(session, "validate_encrypt_rule", Map.of("plan_id", getPlanId(planPayload)));
        assertThat(validatePayload.get("status"), is("failed"));
        assertIssueCode(validatePayload, WorkflowIssueCode.WORKFLOW_STATUS_INVALID);
        assertEncryptRulesRemoved(session);
    }
    
    @Test
    void assertMaskWorkflowLifecycle() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> algorithmsPayload = readResourceAndGetPayload(session, "shardingsphere://features/mask/algorithms");
        List<Map<String, Object>> algorithmItems = getPayloadItems(algorithmsPayload);
        assertTrue(algorithmItems.stream().anyMatch(each -> "KEEP_FIRST_N_LAST_M".equals(each.get("type"))));
        assertMaskPlanApplyValidate(session, createMaskCreatePlanningArguments());
        assertMaskRuleState(session, "KEEP_FIRST_N_LAST_M");
        assertMaskPlanApplyValidate(session, createMaskAlterPlanningArguments());
        assertMaskRuleState(session, "MD5");
        assertMaskPlanApplyValidate(session, createMaskDropPlanningArguments());
        assertMaskRulesRemoved(session);
    }
    
    @Test
    void assertMaskWorkflowRejectsAlterWithoutExistingRule() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = callToolAndGetStructuredContent(session, "plan_mask_rule", createMaskAlterPlanningArguments());
        assertThat(planPayload.get("status"), is("failed"));
        assertIssueCode(planPayload, WorkflowIssueCode.RULE_STATE_MISMATCH);
    }
    
    @Test
    void assertMaskWorkflowRejectsValidateBeforeApply() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_mask_rule", createMaskCreatePlanningArguments());
        Map<String, Object> validatePayload = callToolAndGetStructuredContent(session, "validate_mask_rule", Map.of("plan_id", getPlanId(planPayload)));
        assertThat(validatePayload.get("status"), is("failed"));
        assertIssueCode(validatePayload, WorkflowIssueCode.WORKFLOW_STATUS_INVALID);
        assertMaskRulesRemoved(session);
    }
    
    private Map<String, Object> assertPlannedToolCall(final RuntimeHttpSession session, final String toolName,
                                                      final Map<String, Object> arguments) throws IOException, InterruptedException {
        Map<String, Object> result = callToolAndGetStructuredContent(session, toolName, arguments);
        assertThat(result.get("status"), is("planned"));
        return result;
    }
    
    private Map<String, Object> callToolAndGetStructuredContent(final HttpClient httpClient, final String sessionId,
                                                                final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, toolName, arguments);
        assertThat(actual.statusCode(), is(200));
        return getStructuredContent(actual.body());
    }
    
    private String createAdditionalSessionId(final HttpClient httpClient, final String clientName) throws IOException, InterruptedException {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder(createEndpointUri())
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtils.toJsonString(Map.of(
                        "jsonrpc", "2.0",
                        "id", clientName + "-init",
                        "method", "initialize",
                        "params", createInitializeRequestParams(clientName)))));
        for (Entry<String, String> entry : createJsonRequestHeaders().entrySet()) {
            requestBuilder.header(entry.getKey(), entry.getValue());
        }
        HttpResponse<String> actual = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        assertThat(actual.statusCode(), is(200));
        return actual.headers().firstValue("MCP-Session-Id").orElseThrow();
    }
    
    private void assertEncryptPlanApplyValidate(final RuntimeHttpSession session, final Map<String, Object> planArguments) throws IOException, InterruptedException {
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_encrypt_rule", planArguments);
        String planId = getPlanId(planPayload);
        Map<String, Object> applyPayload = callToolAndGetStructuredContent(session, "apply_encrypt_rule",
                Map.of("plan_id", planId, "approved_steps", List.of("ddl", "rule_distsql")));
        assertThat("encrypt apply payload: " + applyPayload, applyPayload.get("status"), is("completed"));
        Map<String, Object> validatePayload = callToolAndGetStructuredContent(session, "validate_encrypt_rule", Map.of("plan_id", planId));
        assertThat("encrypt validate payload: " + validatePayload, validatePayload.get("status"), is("validated"));
    }
    
    private void assertMaskPlanApplyValidate(final RuntimeHttpSession session, final Map<String, Object> planArguments) throws IOException, InterruptedException {
        Map<String, Object> planPayload = assertPlannedToolCall(session, "plan_mask_rule", planArguments);
        String planId = getPlanId(planPayload);
        Map<String, Object> applyPayload = callToolAndGetStructuredContent(session, "apply_mask_rule",
                Map.of("plan_id", planId, "approved_steps", List.of("rule_distsql")));
        assertThat("mask apply payload: " + applyPayload, applyPayload.get("status"), is("completed"));
        Map<String, Object> validatePayload = callToolAndGetStructuredContent(session, "validate_mask_rule", Map.of("plan_id", planId));
        assertThat("mask validate payload: " + validatePayload, validatePayload.get("status"), is("validated"));
    }
    
    private String assertEncryptRuleState(final RuntimeHttpSession session, final String expectedAlgorithmType) throws IOException, InterruptedException {
        Map<String, Object> databaseRulesPayload = readResourceAndGetPayload(session, "shardingsphere://features/encrypt/databases/logic_db/rules");
        List<Map<String, Object>> databaseRuleItems = getPayloadItems(databaseRulesPayload);
        assertThat(databaseRuleItems.size(), is(1));
        assertThat(databaseRuleItems.get(0).get("logic_column"), is("phone"));
        String actualCipherColumnName = String.valueOf(databaseRuleItems.get(0).get("cipher_column"));
        assertTrue(actualCipherColumnName.startsWith("phone_cipher"));
        assertThat(databaseRuleItems.get(0).get("encryptor_type"), is(expectedAlgorithmType));
        Map<String, Object> tableRulesPayload = readResourceAndGetPayload(session,
                "shardingsphere://features/encrypt/databases/logic_db/tables/customer_profiles/rules");
        List<Map<String, Object>> tableRuleItems = getPayloadItems(tableRulesPayload);
        assertThat(tableRuleItems.size(), is(1));
        assertThat(tableRuleItems.get(0).get("encryptor_type"), is(expectedAlgorithmType));
        return actualCipherColumnName;
    }
    
    private void assertEncryptRulesRemoved(final RuntimeHttpSession session) throws IOException, InterruptedException {
        Map<String, Object> databaseRulesPayload = readResourceAndGetPayload(session, "shardingsphere://features/encrypt/databases/logic_db/rules");
        assertThat(getPayloadItems(databaseRulesPayload).size(), is(0));
        Map<String, Object> tableRulesPayload = readResourceAndGetPayload(session,
                "shardingsphere://features/encrypt/databases/logic_db/tables/customer_profiles/rules");
        assertThat(getPayloadItems(tableRulesPayload).size(), is(0));
    }
    
    private void assertMaskRuleState(final RuntimeHttpSession session, final String expectedAlgorithmType) throws IOException, InterruptedException {
        Map<String, Object> databaseRulesPayload = readResourceAndGetPayload(session, "shardingsphere://features/mask/databases/logic_db/rules");
        List<Map<String, Object>> databaseRuleItems = getPayloadItems(databaseRulesPayload);
        assertThat(databaseRuleItems.size(), is(1));
        assertThat(databaseRuleItems.get(0).get("column"), is("id_card"));
        assertThat(databaseRuleItems.get(0).get("algorithm_type"), is(expectedAlgorithmType));
        Map<String, Object> tableRulesPayload = readResourceAndGetPayload(session,
                "shardingsphere://features/mask/databases/logic_db/tables/customer_profiles/rules");
        List<Map<String, Object>> tableRuleItems = getPayloadItems(tableRulesPayload);
        assertThat(tableRuleItems.size(), is(1));
        assertThat(tableRuleItems.get(0).get("algorithm_type"), is(expectedAlgorithmType));
    }
    
    private void assertMaskRulesRemoved(final RuntimeHttpSession session) throws IOException, InterruptedException {
        Map<String, Object> databaseRulesPayload = readResourceAndGetPayload(session, "shardingsphere://features/mask/databases/logic_db/rules");
        assertThat(getPayloadItems(databaseRulesPayload).size(), is(0));
        Map<String, Object> tableRulesPayload = readResourceAndGetPayload(session,
                "shardingsphere://features/mask/databases/logic_db/tables/customer_profiles/rules");
        assertThat(getPayloadItems(tableRulesPayload).size(), is(0));
    }
    
    @SuppressWarnings("unchecked")
    private void assertIssueCode(final Map<String, Object> payload, final String expectedIssueCode) {
        assertThat(((Map<String, Object>) ((List<?>) payload.get("issues")).get(0)).get("code"), is(expectedIssueCode));
    }
    
    private String getPlanId(final Map<String, Object> payload) {
        return String.valueOf(payload.get("plan_id"));
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> getMapField(final Map<String, Object> payload, final String fieldName) {
        return (Map<String, Object>) payload.get(fieldName);
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getMapList(final Map<String, Object> payload, final String fieldName) {
        return (List<Map<String, Object>>) payload.get(fieldName);
    }
    
    private Map<String, Object> createEncryptCreatePlanningArguments() {
        return createEncryptPlanningArguments("create", "AES", Map.of("aes-key-value", "123456"), true, "phone_cipher");
    }
    
    private Map<String, Object> createEncryptAlterPlanningArguments() {
        return createEncryptPlanningArguments("alter", "MD5", Map.of("salt", "pepper"), false, "");
    }
    
    private Map<String, Object> createEncryptDropPlanningArguments() {
        return Map.of(
                "database", "logic_db",
                "table", "customer_profiles",
                "column", "phone",
                "operation_type", "drop");
    }
    
    private Map<String, Object> createEncryptPlanningArguments(final String operationType, final String algorithmType,
                                                               final Map<String, String> primaryAlgorithmProperties, final boolean requiresDecrypt,
                                                               final String cipherColumnName) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("database", "logic_db");
        result.put("table", "customer_profiles");
        result.put("column", "phone");
        result.put("operation_type", operationType);
        result.put("allow_index_ddl", false);
        result.put("algorithm_type", algorithmType);
        result.put("primary_algorithm_properties", primaryAlgorithmProperties);
        result.put("structured_intent_evidence", Map.of(
                "field_semantics", "phone",
                "requires_decrypt", requiresDecrypt,
                "requires_equality_filter", false,
                "requires_like_query", false));
        if (!cipherColumnName.isEmpty()) {
            result.put("cipher_column_name", cipherColumnName);
        }
        return result;
    }
    
    private Map<String, Object> createMaskCreatePlanningArguments() {
        return createMaskPlanningArguments("create", "KEEP_FIRST_N_LAST_M", Map.of(
                "first-n", "3",
                "last-m", "2",
                "replace-char", "*"));
    }
    
    private Map<String, Object> createMaskAlterPlanningArguments() {
        return createMaskPlanningArguments("alter", "MD5", Map.of());
    }
    
    private Map<String, Object> createMaskDropPlanningArguments() {
        return Map.of(
                "database", "logic_db",
                "table", "customer_profiles",
                "column", "id_card",
                "operation_type", "drop");
    }
    
    private Map<String, Object> createMaskPlanningArguments(final String operationType, final String algorithmType,
                                                            final Map<String, String> primaryAlgorithmProperties) {
        return Map.of(
                "database", "logic_db",
                "table", "customer_profiles",
                "column", "id_card",
                "operation_type", operationType,
                "algorithm_type", algorithmType,
                "primary_algorithm_properties", primaryAlgorithmProperties,
                "structured_intent_evidence", Map.of("field_semantics", "id_card"));
    }
}
