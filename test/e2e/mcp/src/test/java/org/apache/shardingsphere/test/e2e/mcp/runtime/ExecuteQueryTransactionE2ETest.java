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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ExecuteQueryTransactionE2ETest extends AbstractMCPE2ETest implements CrossDatabaseTransactionContractTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteTransactionControlCases")
    void assertExecuteTransactionControl(final String name, final String sql, final boolean requiresActiveTransaction,
                                         final String expectedMessage) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        if (requiresActiveTransaction) {
            sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        }
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", sql));
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get("message")), is(expectedMessage));
    }
    
    static Stream<Arguments> assertExecuteTransactionControlCases() {
        return Stream.of(
                Arguments.of("begin", "BEGIN", false, "Transaction started."),
                Arguments.of("commit", "COMMIT", true, "Transaction committed."),
                Arguments.of("rollback", "ROLLBACK", true, "Transaction rolled back."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertExecuteSavepointCases")
    void assertExecuteSavepoint(final String name, final String databaseName, final String schemaName, final String sql,
                                final boolean requiresActiveTransaction, final boolean requiresExistingSavepoint,
                                final String expectedKey, final String expectedValue) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        if (requiresActiveTransaction) {
            sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                    Map.of("database", databaseName, "schema", schemaName, "sql", "BEGIN"));
        }
        if (requiresExistingSavepoint) {
            sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                    Map.of("database", databaseName, "schema", schemaName, "sql", "SAVEPOINT sp_1"));
        }
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", databaseName, "schema", schemaName, "sql", sql));
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get(expectedKey)), is(expectedValue));
    }
    
    static Stream<Arguments> assertExecuteSavepointCases() {
        return Stream.of(
                Arguments.of("create savepoint", "logic_db", "public", "SAVEPOINT sp_1", true, false, "message", "Savepoint created."),
                Arguments.of("rollback to savepoint", "logic_db", "public", "ROLLBACK TO SAVEPOINT sp_1", true, true, "message", "Savepoint rolled back."),
                Arguments.of("release savepoint", "logic_db", "public", "RELEASE SAVEPOINT sp_1", true, true, "message", "Savepoint released."),
                Arguments.of("unsupported savepoint", "warehouse", "warehouse", "SAVEPOINT sp_1", false, false, "error_code", "unsupported"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRejectInvalidTransactionStateCases")
    void assertRejectInvalidTransactionState(final String name, final String sql, final boolean createActiveTransaction,
                                             final boolean createExistingSavepoint, final String expectedMessage) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        if (createActiveTransaction) {
            sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        }
        if (createExistingSavepoint) {
            sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                    Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT sp_1"));
        }
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", sql));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("transaction_state_error"));
        assertThat(String.valueOf(payload.get("message")), is(expectedMessage));
    }
    
    static Stream<Arguments> assertRejectInvalidTransactionStateCases() {
        return Stream.of(
                Arguments.of("commit without transaction", "COMMIT", false, false, "No active transaction."),
                Arguments.of("rollback without transaction", "ROLLBACK", false, false, "No active transaction."),
                Arguments.of("savepoint without transaction", "SAVEPOINT sp_1", false, false, "No active transaction."),
                Arguments.of("rollback to missing savepoint", "ROLLBACK TO SAVEPOINT sp_1", true, false, "Savepoint does not exist."),
                Arguments.of("release missing savepoint", "RELEASE SAVEPOINT sp_1", true, false, "Savepoint does not exist."));
    }
    
    @Test
    void assertExecuteSelectOverHttpSession() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT * FROM orders", "max_rows", 10));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("result_set"));
    }
    
    @Test
    void assertExecuteSingleStatementValidation() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT 1; SELECT 2"));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("invalid_request"));
    }
    
    @Override
    public void launchContractRuntime() {
        launchRuntime();
    }
    
    @Override
    public HttpClient createContractHttpClient() {
        return createHttpClient();
    }
    
    @Override
    public String initializeContractSession(final HttpClient httpClient) throws IOException, InterruptedException {
        return initializeSession(httpClient);
    }
    
    @Override
    public HttpResponse<String> callContractTool(final HttpClient httpClient, final String sessionId,
                                                 final String toolName, final Map<String, Object> arguments) throws IOException, InterruptedException {
        return sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, toolName, arguments);
    }
    
    @Override
    public Map<String, Object> getContractStructuredContent(final String responseBody) {
        return getStructuredContent(responseBody);
    }
    
    @Override
    public String getPrimaryDatabaseName() {
        return "logic_db";
    }
    
    @Override
    public String getSecondaryDatabaseName() {
        return "analytics_db";
    }
    
    @Override
    public String getSecondaryDatabaseSwitchSql() {
        return "BEGIN";
    }
}
