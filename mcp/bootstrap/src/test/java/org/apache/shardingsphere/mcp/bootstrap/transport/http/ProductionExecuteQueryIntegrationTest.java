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

package org.apache.shardingsphere.mcp.bootstrap.transport.http;

import org.apache.shardingsphere.mcp.jdbc.runtime.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProductionExecuteQueryIntegrationTest extends AbstractProductionRuntimeIntegrationTest {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-execute");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertExecuteSelect() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "SELECT status FROM orders ORDER BY order_id", "max_rows", 10));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("result_kind")), is("result_set"));
        assertThat(String.valueOf(payload.get("truncated")), is("false"));
    }
    
    @Test
    void assertExecuteTransactionCommit() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "COMMIT"));
        assertThat(actual.statusCode(), is(200));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PROCESSING"));
    }
    
    @Test
    void assertDeleteRollsBackPendingTransaction() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        sendToolCallRequest(httpClient, sessionId, "execute_query", Map.of("database", "logic_db", "schema", "public", "sql", "BEGIN"));
        sendToolCallRequest(httpClient, sessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        HttpResponse<String> actual = sendDeleteRequest(httpClient, sessionId);
        assertThat(actual.statusCode(), is(200));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
    }
}
