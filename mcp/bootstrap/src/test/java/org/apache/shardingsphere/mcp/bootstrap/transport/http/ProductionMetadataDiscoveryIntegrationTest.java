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
import org.apache.shardingsphere.mcp.jdbc.config.RuntimeDatabaseConfiguration;
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

class ProductionMetadataDiscoveryIntegrationTest extends AbstractProductionRuntimeIntegrationTest {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-metadata");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return H2RuntimeTestSupport.createRuntimeDatabases("logic_db", jdbcUrl);
    }
    
    @Test
    void assertListDatabases() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "list_databases", Map.of());
        
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getStructuredContent(actual.body()));
        assertThat(items.size(), is(1));
        assertThat(String.valueOf(items.get(0).get("name")), is("logic_db"));
    }
    
    @Test
    void assertDescribeTable() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "describe_table",
                Map.of("database", "logic_db", "schema", "public", "table", "orders"));
        
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getStructuredContent(actual.body()));
        assertThat(items.size(), is(4));
        assertThat(String.valueOf(items.get(0).get("name")), is("orders"));
        assertTrue(items.toString().contains("order_id"));
        assertTrue(items.toString().contains("status"));
    }
    
    @Test
    void assertGetCapabilities() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "get_capabilities", Map.of("database", "logic_db"));
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> structuredContent = getStructuredContent(actual.body());
        assertThat(String.valueOf(structuredContent.get("database")), is("logic_db"));
        assertThat(String.valueOf(structuredContent.get("databaseType")), is("H2"));
        assertTrue(String.valueOf(structuredContent.get("supportedObjectTypes")).contains("VIEW"));
        assertTrue(String.valueOf(structuredContent.get("supportedObjectTypes")).contains("INDEX"));
    }
}
