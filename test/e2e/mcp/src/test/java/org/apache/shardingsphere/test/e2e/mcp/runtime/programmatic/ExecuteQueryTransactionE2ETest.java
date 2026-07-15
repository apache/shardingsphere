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

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

@EnabledIf("isEnabled")
class ExecuteQueryTransactionE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertRejectCrossDatabaseTransactionSwitch() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", createExecuteSQLArguments("logic_db", "logic_db", "BEGIN"));
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                createExecuteSQLArguments("analytics_db", "analytics_db", "BEGIN"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getToolCallPayload(actual.body());
        assertThat(String.valueOf(payload.get("response_mode")), is("recovery"));
        assertThat(String.valueOf(payload.get("message")), is("Cross-database transaction switching is not supported."));
    }
    
    @Test
    void assertTransactionsAreSessionScoped() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String firstSessionId = initializeSession(httpClient);
        String secondSessionId = initializeSession(httpClient);
        assertTransactionMessage(httpClient, firstSessionId, "logic_db", "BEGIN", "Transaction started.");
        assertTransactionMessage(httpClient, secondSessionId, "analytics_db", "BEGIN", "Transaction started.");
        assertTransactionMessage(httpClient, firstSessionId, "logic_db", "ROLLBACK", "Transaction rolled back.");
        assertTransactionMessage(httpClient, secondSessionId, "analytics_db", "ROLLBACK", "Transaction rolled back.");
    }
    
    @Test
    void assertDeleteRollsBackTransaction() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        assertTransactionMessage(httpClient, sessionId, "logic_db", "BEGIN", "Transaction started.");
        HttpResponse<String> update = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update",
                createExecuteSQLArguments("logic_db", "logic_db", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        assertThat(update.statusCode(), is(200));
        assertThat(String.valueOf(getToolCallPayload(update.body()).get("affected_rows")), is("1"));
        assertThat(sendDeleteRequest(httpClient, createSessionHeaders(sessionId)).statusCode(), is(200));
        String newSessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, newSessionId, "database_gateway_execute_query",
                Map.of("database", "logic_db", "schema", "logic_db", "sql", "SELECT status FROM orders WHERE order_id = 1"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getToolCallPayload(actual.body());
        assertThat(String.valueOf(((List<?>) ((List<?>) payload.get("rows")).get(0)).get(0)), is("NEW"));
    }
    
    private Map<String, Object> createExecuteSQLArguments(final String databaseName, final String schemaName, final String sql) {
        return Map.of("database", databaseName, "schema", schemaName, "sql", sql, "execution_mode", "execute");
    }
    
    private void assertTransactionMessage(final HttpClient httpClient, final String sessionId, final String databaseName, final String sql,
                                          final String expectedMessage) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_execute_update", createExecuteSQLArguments(databaseName, databaseName, sql));
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getToolCallPayload(actual.body()).get("message")), is(expectedMessage));
    }
    
}
