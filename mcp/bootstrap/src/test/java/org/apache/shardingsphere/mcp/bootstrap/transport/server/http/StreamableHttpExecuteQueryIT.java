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

import org.apache.shardingsphere.mcp.bootstrap.fixture.MCPBootstrapTestDataFactory;
import org.apache.shardingsphere.mcp.test.fixture.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpExecuteQueryIT extends AbstractStreamableHttpIT {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "jdbc-execute");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return MCPBootstrapTestDataFactory.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertExecuteSelect() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> payload = callToolAndGetStructuredContent(session, "execute_query",
                createExecuteQueryArguments("SELECT status FROM orders ORDER BY order_id"));
        assertThat(payload.get("result_kind"), is("result_set"));
        assertThat(payload.get("statement_class"), is("query"));
        assertThat(payload.get("statement_type"), is("SELECT"));
        assertTrue(((Iterable<?>) payload.get("columns")).iterator().hasNext());
        assertTrue(((Iterable<?>) payload.get("rows")).iterator().hasNext());
        assertFalse((Boolean) payload.get("truncated"));
    }
    
    @Test
    void assertExecuteSelectWithAccessToken() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        Map<String, Object> payload = callToolAndGetStructuredContent(session, "execute_query",
                createExecuteQueryArguments("SELECT status FROM orders ORDER BY order_id"));
        assertThat(payload.get("result_kind"), is("result_set"));
        assertThat(payload.get("statement_class"), is("query"));
        assertThat(payload.get("statement_type"), is("SELECT"));
    }
    
    @Test
    void assertExecuteTransactionCommit() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("BEGIN"));
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("UPDATE orders SET status = 'PROCESSING' WHERE order_id = 1"));
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("COMMIT"));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("PROCESSING"));
    }
    
    @Test
    void assertDeleteRollsBackPendingTransaction() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("BEGIN"));
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        assertThat(sendDeleteRequest(session.httpClient(), session.sessionId(), session.accessToken()).statusCode(), is(200));
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
    }
    
    @Test
    void assertStopRollsBackPendingTransaction() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("BEGIN"));
        callToolAndGetStructuredContent(session, "execute_query", createExecuteQueryArguments("UPDATE orders SET status = 'PENDING' WHERE order_id = 1"));
        stopRuntime();
        assertThat(H2RuntimeTestSupport.querySingleString(jdbcUrl, "SELECT status FROM public.orders WHERE order_id = 1"), is("NEW"));
    }
    
    @Test
    void assertRejectExecuteQueryWithoutAccessToken() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        HttpResponse<String> actualResponse = sendToolCallRequest(session.httpClient(), session.sessionId(), "execute_query",
                createExecuteQueryArguments("SELECT status FROM orders ORDER BY order_id"));
        assertThat(actualResponse.statusCode(), is(401));
        assertThat(parseJsonBody(actualResponse.body()).get("message"), is("Unauthorized."));
    }
    
    private Map<String, Object> createExecuteQueryArguments(final String sql) {
        return Map.of("database", "logic_db", "schema", "public", "sql", sql);
    }
}
