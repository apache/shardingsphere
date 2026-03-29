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

import org.apache.shardingsphere.mcp.runtime.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionRefreshVisibilityE2ETest extends AbstractProductionRuntimeE2ETest {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws IOException {
        try {
            jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-e2e-refresh");
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
    void assertDdlRefreshVisibleAcrossSessions() throws IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String firstSessionId = initializeSession(httpClient);
        HttpResponse<String> executeResponse = sendToolCallRequest(httpClient, firstSessionId, "execute_query",
                Map.of("database", "logic_db", "schema", "public", "sql", "CREATE TABLE orders_archive (order_id INT PRIMARY KEY)"));
        HttpResponse<String> sameSessionMetadata = sendToolCallRequest(httpClient, firstSessionId, "list_tables",
                Map.of("database", "logic_db", "schema", "public"));
        String secondSessionId = initializeSession(httpClient);
        HttpResponse<String> secondSessionMetadata = sendToolCallRequest(httpClient, secondSessionId, "list_tables",
                Map.of("database", "logic_db", "schema", "public"));
        assertThat(executeResponse.statusCode(), is(200));
        assertThat(String.valueOf(getStructuredContent(executeResponse.body()).get("result_kind")), is("statement_ack"));
        List<Map<String, Object>> firstSessionItems = getPayloadItems(getStructuredContent(sameSessionMetadata.body()));
        List<Map<String, Object>> secondSessionItems = getPayloadItems(getStructuredContent(secondSessionMetadata.body()));
        assertTrue(firstSessionItems.toString().contains("orders_archive"));
        assertTrue(secondSessionItems.toString().contains("orders_archive"));
    }
}
