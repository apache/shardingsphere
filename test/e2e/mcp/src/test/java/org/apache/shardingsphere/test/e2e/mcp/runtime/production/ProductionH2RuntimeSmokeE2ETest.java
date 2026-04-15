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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.test.e2e.mcp.runtime.support.H2RuntimeTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.client.MCPHttpInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.runtime.transport.MCPInteractionResponse;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.is;

class ProductionH2RuntimeSmokeE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-runtime-smoke");
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IOException(ex);
        }
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> getRuntimeDatabases() {
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertReadDatabasesResource() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases");
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getResourcePayload(actual.body()));
        assertThat(items.size(), is(1));
        assertThat(String.valueOf(items.get(0).get("database")), is("logic_db"));
    }
    
    @Test
    void assertServiceCapabilitiesResource() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        assertThat(getResourcePayload(actual.body()).get("supportedTools"), is(List.of("search_metadata", "execute_query")));
    }
    
    @Test
    void assertTableDetailResource() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/logic_db/schemas/public/tables/orders");
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getResourcePayload(actual.body()));
        assertThat(items.size(), is(1));
        Map<String, Object> actualItem = items.get(0);
        assertThat(String.valueOf(actualItem.get("table")), is("orders"));
        assertThat(getNestedNames(actualItem, "columns", "column"), is(List.of("amount", "order_id", "status")));
        assertThat(getNestedNames(actualItem, "indexes", "index"), is(List.of("PRIMARY_KEY_C", "idx_orders_status")));
    }
    
    @Test
    void assertListResources() throws IOException, InterruptedException {
        launchProductionRuntime();
        MCPHttpInteractionClient actual = new MCPHttpInteractionClient(getEndpointUri(), createHttpClient());
        actual.open();
        MCPInteractionResponse response = actual.listResources();
        actual.close();
        assertThat(String.valueOf(response.getStructuredContent().get("resources")), containsString("shardingsphere://capabilities"));
    }
    
    @Test
    void assertExecuteSelect() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(actual.body()).get("result_kind")), is("result_set"));
    }
    
    @Test
    void assertExecuteUpdate() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("update_count"));
        assertThat(String.valueOf(payload.get("affected_rows")), is("1"));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
    }
    
    @Test
    void assertReadSequencesResource() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic_db/schemas/public/sequences");
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getResourcePayload(actual.body()));
        assertThat(items.size(), is(1));
        assertThat(String.valueOf(items.get(0).get("sequence")), is("order_seq"));
    }
    
    @Test
    void assertSearchSequence() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("SEQUENCE")));
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getStructuredContent(actual.body()));
        assertThat(items.size(), is(1));
        assertThat(String.valueOf(items.get(0).get("name")), is("order_seq"));
    }
    
    @Test
    void assertExecuteSavepointFlow() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        HttpResponse<String> savepointResponse = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SAVEPOINT sp_1"));
        sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'DONE' WHERE order_id = 1"));
        HttpResponse<String> rollbackResponse = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "ROLLBACK TO SAVEPOINT sp_1"));
        final HttpResponse<String> commitResponse = sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
        assertThat(savepointResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(savepointResponse.body()).get("message")), is("Savepoint created."));
        assertThat(rollbackResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(rollbackResponse.body()).get("message")), is("Savepoint rolled back."));
        assertThat(commitResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(commitResponse.body()).get("message")), is("Transaction committed."));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PENDING"));
    }
    
    @Test
    void assertDeleteRollsBackPendingTransaction() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        HttpResponse<String> actual = sendDeleteRequest(httpClient, sessionId);
        assertThat(actual.statusCode(), is(200));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
    }
    
    private static List<String> getNestedNames(final Map<String, Object> item, final String nestedKey, final String nameKey) {
        return ((List<?>) item.get(nestedKey)).stream().map(each -> String.valueOf(((Map<?, ?>) each).get(nameKey))).toList();
    }
}
