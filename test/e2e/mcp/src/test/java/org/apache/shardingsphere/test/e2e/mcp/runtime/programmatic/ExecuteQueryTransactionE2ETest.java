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

import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;

@EnabledIf("isEnabled")
class ExecuteQueryTransactionE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertExecuteSelectOverHttpSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "SELECT * FROM orders", 10));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("result_set"));
    }
    
    @Test
    void assertExecuteReadOnlyCommonTableExpression() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "WITH foo_orders AS (SELECT * FROM orders) SELECT * FROM foo_orders"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(payload.get("statement_class")), is("query"));
    }
    
    @Test
    void assertRecoverDataModifyingCommonTableExpressionQuery() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "WITH updated_orders AS (UPDATE orders SET status = status WHERE order_id = -1 RETURNING *) SELECT * FROM updated_orders"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("unsupported"));
        Map<String, Object> recovery = castToMap(payload.get("recovery"));
        assertThat(String.valueOf(recovery.get("recovery_category")), is("unsafe_sql"));
        Map<String, Object> nextAction = castToMapList(recovery.get("next_actions")).get(0);
        assertThat(String.valueOf(nextAction.get("tool_name")), is("execute_update"));
        assertThat(String.valueOf(castToMap(nextAction.get("arguments")).get("execution_mode")), is("preview"));
    }
    
    @Test
    void assertRejectCrossDatabaseTransactionSwitch() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "execute_update", createExecuteSQLArguments("logic_db", "public", "BEGIN"));
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_update",
                createExecuteSQLArguments("analytics_db", "public", "BEGIN"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("transaction_state_error"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertPreviewSideEffectStatementCases")
    void assertPreviewSideEffectStatement(final String name, final String sql, final String expectedStatementClass,
                                          final String expectedStatementType) throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_update",
                Map.of("database", "logic_db", "schema", "public", "sql", sql, "execution_mode", "preview"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("preview"));
        assertThat(String.valueOf(payload.get("statement_class")), is(expectedStatementClass));
        assertThat(String.valueOf(payload.get("statement_type")), is(expectedStatementType));
        assertThat(payload.get("side_effect_scope"), is(List.of("transaction-state")));
        assertFalse((Boolean) payload.get("would_execute"));
    }
    
    @Test
    void assertExecuteSingleStatementValidation() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "SELECT 1; SELECT 2"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("invalid_request"));
    }
    
    @Test
    void assertRejectMetadataIntrospectionSql() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "SHOW TABLES"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("invalid_request"));
        Map<String, Object> recovery = castToMap(payload.get("recovery"));
        assertThat(String.valueOf(recovery.get("category")), is("metadata_introspection_sql"));
    }
    
    @Test
    void assertRejectBannedSql() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                createExecuteQueryArguments("logic_db", "public", "SET search_path public"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("unsupported"));
        Map<String, Object> recovery = castToMap(payload.get("recovery"));
        assertThat(String.valueOf(recovery.get("category")), is("banned_sql_statement"));
        assertThat(String.valueOf(recovery.get("recovery_category")), is("terminal_operator_action"));
    }
    
    @Test
    void assertRejectMissingRequiredSqlArgument() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(payload.get("message")), is("sql is required."));
    }
    
    @Test
    void assertRejectUnsupportedTool() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "unsupported_tool", Map.of());
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("json_rpc_error"));
        assertFalse(String.valueOf(payload.get("message")).isEmpty());
    }
    
    private Map<String, Object> createExecuteSQLArguments(final String databaseName, final String schemaName, final String sql) {
        return Map.of("database", databaseName, "schema", schemaName, "sql", sql, "execution_mode", "execute", "approved_by_user", true);
    }
    
    private Map<String, Object> createExecuteQueryArguments(final String databaseName, final String schemaName, final String sql) {
        return createExecuteQueryArguments(databaseName, schemaName, sql, -1);
    }
    
    private Map<String, Object> createExecuteQueryArguments(final String databaseName, final String schemaName, final String sql, final int maxRows) {
        return -1 == maxRows
                ? Map.of("database", databaseName, "schema", schemaName, "sql", sql)
                : Map.of("database", databaseName, "schema", schemaName, "sql", sql, "max_rows", maxRows);
    }
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
    
    private static Stream<Arguments> assertPreviewSideEffectStatementCases() {
        return Stream.of(
                Arguments.of("transaction control", "BEGIN", "transaction_control", "BEGIN"),
                Arguments.of("savepoint", "SAVEPOINT foo_sp_1", "savepoint", "SAVEPOINT"));
    }
}
