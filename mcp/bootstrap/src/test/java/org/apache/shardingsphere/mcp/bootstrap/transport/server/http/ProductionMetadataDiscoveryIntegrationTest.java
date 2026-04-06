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

import org.apache.shardingsphere.mcp.bootstrap.H2RuntimeTestSupport;
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
import static org.junit.jupiter.api.Assertions.assertTrue;

class ProductionMetadataDiscoveryIntegrationTest extends AbstractProductionRuntimeIntegrationTest {
    
    private String jdbcUrl;
    
    private boolean useDriverClassName = true;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "production-metadata");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", useDriverClassName ? "org.h2.Driver" : ""));
    }
    
    @Test
    void assertReadDatabasesResource() throws SQLException, IOException, InterruptedException {
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
    void assertSearchMetadata() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "search_metadata",
                Map.of("database", "logic_db", "query", "order", "object_types", List.of("table", "view")));
        
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> items = getPayloadItems(getStructuredContent(actual.body()));
        assertThat(items.size(), is(3));
        assertThat(String.valueOf(items.get(0).get("name")), is("order_items"));
        assertThat(String.valueOf(items.get(1).get("name")), is("orders"));
        assertThat(String.valueOf(items.get(2).get("name")), is("active_orders"));
    }
    
    @Test
    void assertReadDatabaseCapabilitiesResource() throws SQLException, IOException, InterruptedException {
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic_db/capabilities");
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("database")), is("logic_db"));
        assertThat(String.valueOf(payload.get("databaseType")), is("H2"));
        assertTrue(String.valueOf(payload.get("supportedObjectTypes")).contains("VIEW"));
        assertTrue(String.valueOf(payload.get("supportedObjectTypes")).contains("INDEX"));
    }
    
    @Test
    void assertReadDatabaseCapabilitiesResourceWithoutExplicitDriverClassName() throws SQLException, IOException, InterruptedException {
        useDriverClassName = false;
        launchProductionRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic_db/capabilities");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("database")), is("logic_db"));
        assertThat(String.valueOf(payload.get("databaseType")), is("H2"));
    }
}
