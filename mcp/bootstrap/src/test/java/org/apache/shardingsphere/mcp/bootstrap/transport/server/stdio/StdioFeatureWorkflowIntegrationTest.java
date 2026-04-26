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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import org.apache.shardingsphere.mcp.bootstrap.fixture.FeatureDistSQLTestDriver;
import org.apache.shardingsphere.mcp.bootstrap.fixture.FeatureWorkflowRuntimeTestSupport;
import org.apache.shardingsphere.mcp.bootstrap.fixture.FeatureWorkflowRuntimeTestSupport.H2AccessMode;
import org.apache.shardingsphere.mcp.tool.model.workflow.WorkflowIssueCode;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StdioFeatureWorkflowIntegrationTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertEncryptWorkflowLifecycle() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> algorithmsPayload = client.readResourcePayload("shardingsphere://features/encrypt/algorithms");
            List<Map<String, Object>> algorithmItems = client.getItems(algorithmsPayload);
            assertThat(algorithmItems.size(), is(2));
            assertTrue(algorithmItems.stream().anyMatch(each -> "AES".equals(each.get("type"))));
            assertEncryptPlanApplyValidate(client, createEncryptCreatePlanningArguments());
            String cipherColumnName = assertEncryptRuleState(client, "AES");
            assertEncryptPlanApplyValidate(client, createEncryptAlterPlanningArguments());
            assertThat(assertEncryptRuleState(client, "MD5"), is(cipherColumnName));
            assertEncryptPlanApplyValidate(client, createEncryptDropPlanningArguments());
            assertEncryptRulesRemoved(client);
        }
    }
    
    @Test
    void assertEncryptWorkflowRejectsAlterWithoutExistingRule() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> planPayload = client.callTool("plan_encrypt_rule", createEncryptAlterPlanningArguments());
            assertThat(planPayload.get("status"), is("failed"));
            assertIssueCode(planPayload, WorkflowIssueCode.RULE_STATE_MISMATCH);
        }
    }
    
    @Test
    void assertEncryptWorkflowRejectsValidateBeforeApply() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> planPayload = assertPlannedToolCall(client, "plan_encrypt_rule", createEncryptCreatePlanningArguments());
            Map<String, Object> validatePayload = client.callTool("validate_encrypt_rule", Map.of("plan_id", getPlanId(planPayload)));
            assertThat(validatePayload.get("status"), is("failed"));
            assertIssueCode(validatePayload, WorkflowIssueCode.WORKFLOW_STATUS_INVALID);
            assertEncryptRulesRemoved(client);
        }
    }
    
    @Test
    void assertMaskWorkflowLifecycle() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> algorithmsPayload = client.readResourcePayload("shardingsphere://features/mask/algorithms");
            List<Map<String, Object>> algorithmItems = client.getItems(algorithmsPayload);
            assertTrue(algorithmItems.stream().anyMatch(each -> "KEEP_FIRST_N_LAST_M".equals(each.get("type"))));
            assertMaskPlanApplyValidate(client, createMaskCreatePlanningArguments());
            assertMaskRuleState(client, "KEEP_FIRST_N_LAST_M");
            assertMaskPlanApplyValidate(client, createMaskAlterPlanningArguments());
            assertMaskRuleState(client, "MD5");
            assertMaskPlanApplyValidate(client, createMaskDropPlanningArguments());
            assertMaskRulesRemoved(client);
        }
    }
    
    @Test
    void assertMaskWorkflowRejectsAlterWithoutExistingRule() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> planPayload = client.callTool("plan_mask_rule", createMaskAlterPlanningArguments());
            assertThat(planPayload.get("status"), is("failed"));
            assertIssueCode(planPayload, WorkflowIssueCode.RULE_STATE_MISMATCH);
        }
    }
    
    @Test
    void assertMaskWorkflowRejectsValidateBeforeApply() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            client.initialize();
            client.notifyInitialized();
            Map<String, Object> planPayload = assertPlannedToolCall(client, "plan_mask_rule", createMaskCreatePlanningArguments());
            Map<String, Object> validatePayload = client.callTool("validate_mask_rule", Map.of("plan_id", getPlanId(planPayload)));
            assertThat(validatePayload.get("status"), is("failed"));
            assertIssueCode(validatePayload, WorkflowIssueCode.WORKFLOW_STATUS_INVALID);
            assertMaskRulesRemoved(client);
        }
    }
    
    private Path createRuntimeDatabasesConfigFile() throws IOException {
        String jdbcUrl = FeatureWorkflowRuntimeTestSupport.createJdbcUrl(tempDir, "feature-stdio", H2AccessMode.MULTI_PROCESS);
        initializeDatabase(jdbcUrl);
        Path result = tempDir.resolve("mcp-feature-runtime-databases.yaml");
        Files.writeString(result, "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    allowRemoteAccess: false\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: '" + jdbcUrl + "'\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: " + FeatureDistSQLTestDriver.class.getName() + '\n');
        return result;
    }
    
    private void initializeDatabase(final String jdbcUrl) {
        try {
            FeatureWorkflowRuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private Map<String, Object> assertPlannedToolCall(final StdioTransportTestClient client, final String toolName,
                                                      final Map<String, Object> arguments) throws IOException {
        Map<String, Object> result = client.callTool(toolName, arguments);
        assertThat(result.get("status"), is("planned"));
        return result;
    }
    
    private void assertEncryptPlanApplyValidate(final StdioTransportTestClient client, final Map<String, Object> planArguments) throws IOException {
        Map<String, Object> planPayload = assertPlannedToolCall(client, "plan_encrypt_rule", planArguments);
        String planId = getPlanId(planPayload);
        Map<String, Object> applyPayload = client.callTool("apply_encrypt_rule",
                Map.of("plan_id", planId, "approved_steps", List.of("ddl", "rule_distsql")));
        assertThat("encrypt apply payload: " + applyPayload, applyPayload.get("status"), is("completed"));
        Map<String, Object> validatePayload = client.callTool("validate_encrypt_rule", Map.of("plan_id", planId));
        assertThat("encrypt validate payload: " + validatePayload, validatePayload.get("status"), is("validated"));
    }
    
    private void assertMaskPlanApplyValidate(final StdioTransportTestClient client, final Map<String, Object> planArguments) throws IOException {
        Map<String, Object> planPayload = assertPlannedToolCall(client, "plan_mask_rule", planArguments);
        String planId = getPlanId(planPayload);
        Map<String, Object> applyPayload = client.callTool("apply_mask_rule",
                Map.of("plan_id", planId, "approved_steps", List.of("rule_distsql")));
        assertThat("mask apply payload: " + applyPayload, applyPayload.get("status"), is("completed"));
        Map<String, Object> validatePayload = client.callTool("validate_mask_rule", Map.of("plan_id", planId));
        assertThat("mask validate payload: " + validatePayload, validatePayload.get("status"), is("validated"));
    }
    
    private String assertEncryptRuleState(final StdioTransportTestClient client, final String expectedAlgorithmType) throws IOException {
        Map<String, Object> databaseRulesPayload = client.readResourcePayload("shardingsphere://features/encrypt/databases/logic_db/rules");
        List<Map<String, Object>> databaseRuleItems = client.getItems(databaseRulesPayload);
        assertThat(databaseRuleItems.size(), is(1));
        assertThat(databaseRuleItems.get(0).get("logic_column"), is("phone"));
        String actualCipherColumnName = String.valueOf(databaseRuleItems.get(0).get("cipher_column"));
        assertTrue(actualCipherColumnName.startsWith("phone_cipher"));
        assertThat(databaseRuleItems.get(0).get("encryptor_type"), is(expectedAlgorithmType));
        Map<String, Object> tableRulesPayload = client.readResourcePayload(
                "shardingsphere://features/encrypt/databases/logic_db/tables/customer_profiles/rules");
        List<Map<String, Object>> tableRuleItems = client.getItems(tableRulesPayload);
        assertThat(tableRuleItems.size(), is(1));
        assertThat(tableRuleItems.get(0).get("encryptor_type"), is(expectedAlgorithmType));
        return actualCipherColumnName;
    }
    
    private void assertEncryptRulesRemoved(final StdioTransportTestClient client) throws IOException {
        Map<String, Object> databaseRulesPayload = client.readResourcePayload("shardingsphere://features/encrypt/databases/logic_db/rules");
        assertThat(client.getItems(databaseRulesPayload).size(), is(0));
        Map<String, Object> tableRulesPayload = client.readResourcePayload(
                "shardingsphere://features/encrypt/databases/logic_db/tables/customer_profiles/rules");
        assertThat(client.getItems(tableRulesPayload).size(), is(0));
    }
    
    private void assertMaskRuleState(final StdioTransportTestClient client, final String expectedAlgorithmType) throws IOException {
        Map<String, Object> databaseRulesPayload = client.readResourcePayload("shardingsphere://features/mask/databases/logic_db/rules");
        List<Map<String, Object>> databaseRuleItems = client.getItems(databaseRulesPayload);
        assertThat(databaseRuleItems.size(), is(1));
        assertThat(databaseRuleItems.get(0).get("column"), is("id_card"));
        assertThat(databaseRuleItems.get(0).get("algorithm_type"), is(expectedAlgorithmType));
        Map<String, Object> tableRulesPayload = client.readResourcePayload(
                "shardingsphere://features/mask/databases/logic_db/tables/customer_profiles/rules");
        List<Map<String, Object>> tableRuleItems = client.getItems(tableRulesPayload);
        assertThat(tableRuleItems.size(), is(1));
        assertThat(tableRuleItems.get(0).get("algorithm_type"), is(expectedAlgorithmType));
    }
    
    private void assertMaskRulesRemoved(final StdioTransportTestClient client) throws IOException {
        Map<String, Object> databaseRulesPayload = client.readResourcePayload("shardingsphere://features/mask/databases/logic_db/rules");
        assertThat(client.getItems(databaseRulesPayload).size(), is(0));
        Map<String, Object> tableRulesPayload = client.readResourcePayload(
                "shardingsphere://features/mask/databases/logic_db/tables/customer_profiles/rules");
        assertThat(client.getItems(tableRulesPayload).size(), is(0));
    }
    
    @SuppressWarnings("unchecked")
    private void assertIssueCode(final Map<String, Object> payload, final String expectedIssueCode) {
        assertThat(((Map<String, Object>) ((List<?>) payload.get("issues")).get(0)).get("code"), is(expectedIssueCode));
    }
    
    private String getPlanId(final Map<String, Object> payload) {
        return String.valueOf(payload.get("plan_id"));
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
