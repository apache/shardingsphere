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

package org.apache.shardingsphere.test.e2e.mcp;

import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class ProductionRuntimeSmokeE2ETest extends AbstractProductionRuntimeE2ETest {
    
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
