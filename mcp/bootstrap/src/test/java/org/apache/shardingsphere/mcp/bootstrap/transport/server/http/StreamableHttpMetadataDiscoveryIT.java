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

import org.apache.shardingsphere.mcp.jdbc.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpResponse;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StreamableHttpMetadataDiscoveryIT extends AbstractStreamableHttpIT {
    
    private String jdbcUrl;
    
    @Override
    protected void prepareRuntimeFixture() throws SQLException {
        jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(getTempDir(), "jdbc-metadata");
        H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
    }
    
    @Override
    protected Map<String, RuntimeDatabaseConfiguration> createRuntimeDatabases() {
        return Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver"));
    }
    
    @Test
    void assertReadDatabasesResource() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        List<Map<String, Object>> items = getPayloadItems(readResourceAndGetPayload(session, "shardingsphere://databases"));
        assertThat(items.size(), is(1));
        assertThat(items.get(0).get("database"), is("logic_db"));
    }
    
    @Test
    void assertReadDatabasesResourceWithAccessToken() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        List<Map<String, Object>> items = getPayloadItems(readResourceAndGetPayload(session, "shardingsphere://databases"));
        assertThat(items.size(), is(1));
        assertThat(items.get(0).get("database"), is("logic_db"));
    }
    
    @Test
    void assertSearchMetadata() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        List<Map<String, Object>> items = getPayloadItems(callToolAndGetStructuredContent(session, "search_metadata",
                Map.of("database", "logic_db", "query", "order", "object_types", List.of("table", "view"))));
        assertThat(items.size(), is(3));
        assertThat(items.get(0).get("name"), is("order_items"));
        assertThat(items.get(1).get("name"), is("orders"));
        assertThat(items.get(2).get("name"), is("active_orders"));
    }
    
    @Test
    void assertSequenceDiscovery() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        List<Map<String, Object>> searchItems = getPayloadItems(callToolAndGetStructuredContent(session, "search_metadata",
                Map.of("database", "logic_db", "query", "order", "object_types", List.of("sequence"))));
        assertThat(searchItems.size(), is(1));
        assertThat(searchItems.get(0).get("name"), is("order_seq"));
        List<Map<String, Object>> resourceItems = getPayloadItems(readResourceAndGetPayload(session, "shardingsphere://databases/logic_db/schemas/public/sequences"));
        assertThat(resourceItems.size(), is(1));
        assertThat(resourceItems.get(0).get("sequence"), is("order_seq"));
    }
    
    @Test
    void assertReadDatabaseCapabilitiesResource() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> payload = readResourceAndGetPayload(session, "shardingsphere://databases/logic_db/capabilities");
        assertThat(payload.get("database"), is("logic_db"));
        assertThat(payload.get("databaseType"), is("H2"));
        List<String> supportedObjectTypes = getStringList(payload, "supportedObjectTypes");
        assertTrue(supportedObjectTypes.contains("VIEW"));
        assertTrue(supportedObjectTypes.contains("INDEX"));
        assertTrue(supportedObjectTypes.contains("SEQUENCE"));
    }
    
    @Test
    void assertReadGlobalCapabilitiesResourceWithFeatureEntries() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntime();
        Map<String, Object> payload = readResourceAndGetPayload(session, "shardingsphere://capabilities");
        List<String> supportedTools = getStringList(payload, "supportedTools");
        assertTrue(supportedTools.contains("plan_encrypt_rule"));
        assertTrue(supportedTools.contains("validate_encrypt_rule"));
        assertTrue(supportedTools.contains("apply_encrypt_rule"));
        assertTrue(supportedTools.contains("plan_mask_rule"));
        assertTrue(supportedTools.contains("validate_mask_rule"));
        assertTrue(supportedTools.contains("apply_mask_rule"));
        List<String> supportedResources = getStringList(payload, "supportedResources");
        assertTrue(supportedResources.contains("shardingsphere://features/encrypt/algorithms"));
        assertTrue(supportedResources.contains("shardingsphere://features/encrypt/databases/{database}/rules"));
        assertTrue(supportedResources.contains("shardingsphere://features/mask/algorithms"));
        assertTrue(supportedResources.contains("shardingsphere://features/mask/databases/{database}/tables/{table}/rules"));
    }
    
    @Test
    void assertRejectMetadataReadWithoutAccessToken() throws SQLException, IOException, InterruptedException {
        RuntimeHttpSession session = launchRuntimeWithAccessToken();
        HttpResponse<String> actualResponse = sendResourceReadRequest(session.httpClient(), session.sessionId(), "shardingsphere://databases");
        assertThat(actualResponse.statusCode(), is(401));
        assertThat(parseJsonBody(actualResponse.body()).get("message"), is("Unauthorized."));
    }
    
}
