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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
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
class MetadataDiscoveryE2ETest extends AbstractSharedHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertSearchMetadataTablesAndViews() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata",
                Map.of("database", "logic_db", "schema", "logic_db", "query", "order", "object_types", List.of("table", "view")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getToolCallPayload(actual.body());
        assertThat(String.valueOf(actualPayload.get("response_mode")), is("search"));
        assertThat(actualPayload.get("count"), is(3));
        assertThat(actualPayload.get("total_match_count"), is(3));
        assertThat(getItemNames(actualPayload), is(List.of("order_items", "orders", "active_orders")));
        List<Map<String, Object>> actualItems = getItems(actualPayload);
        Map<String, Object> actualResource = MCPInteractionPayloads.getRequiredObject(actualItems.get(1), "resource");
        assertThat(String.valueOf(actualResource.get("uri")), is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders"));
        assertThat(MCPInteractionPayloads.getRequiredObjectList(actualItems.get(1), "next_resources").stream()
                .map(each -> String.valueOf(each.get("uri"))).toList(),
                is(List.of("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders/columns",
                        "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders/indexes")));
        HttpResponse<String> tableResource = sendResourceReadRequest(httpClient, sessionId, String.valueOf(actualResource.get("uri")));
        assertThat(tableResource.statusCode(), is(200));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObjectList(getFirstResourcePayload(tableResource.body()), "items").getFirst().get("table")), is("orders"));
    }
    
    @Test
    void assertSearchMetadataAcrossDatabasesByDefault() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", Map.of("query", "metric"));
        assertThat(actual.statusCode(), is(200));
        assertThat(getItemNames(getToolCallPayload(actual.body())), is(List.of("metrics", "metric_id", "metric_name", "PRIMARY")));
    }
    
    @Test
    void assertRejectSchemaSearchWithoutDatabase() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", Map.of("schema", "logic_db", "query", "order"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getToolCallPayload(actual.body());
        assertThat(String.valueOf(actualPayload.get("response_mode")), is("recovery"));
        assertThat(String.valueOf(actualPayload.get("summary")), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertReadWarehouseIndexesResource() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes");
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> actualItems = MCPInteractionPayloads.getRequiredObjectList(getFirstResourcePayload(actual.body()), "items");
        assertThat(actualItems.size(), is(1));
        assertThat(String.valueOf(actualItems.getFirst().get("table")), is("facts"));
        assertThat(String.valueOf(actualItems.getFirst().get("index")), is("PRIMARY"));
    }
    
    @Test
    void assertReadResourceUriWithEncodedSegments() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> databaseResource = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic%5Fdb");
        assertThat(databaseResource.statusCode(), is(200));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObjectList(getFirstResourcePayload(databaseResource.body()), "items").getFirst().get("database")), is("logic_db"));
        HttpResponse<String> tableResource = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/logic_db/schemas/logic_db/tables/orders%20archive%2F2026");
        assertThat(tableResource.statusCode(), is(200));
        Map<String, Object> tablePayload = getFirstResourcePayload(tableResource.body());
        assertThat(MCPInteractionPayloads.getRequiredObjectList(tablePayload, "items"), is(List.of()));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObject(tablePayload, "self_resource").get("uri")),
                is("shardingsphere://databases/logic_db/schemas/logic_db/tables/orders%20archive%2F2026"));
        assertThat(String.valueOf(MCPInteractionPayloads.getRequiredObject(tablePayload, "recovery").get("requested_token")), is("orders archive/2026"));
    }
    
    @Test
    void assertRejectMalformedResourceUriEncoding() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic%ZZdb");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("json_rpc_error"));
        assertThat(String.valueOf(payload.get("message")), is("Unsupported resource URI `shardingsphere://databases/logic%ZZdb`."));
    }
    
    @Test
    void assertRejectUnexpandedResourceUriTemplate() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/logic_db/schemas/logic_db/tables/{table}");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("json_rpc_error"));
        assertThat(String.valueOf(payload.get("message")), is("Unsupported resource URI `shardingsphere://databases/logic_db/schemas/logic_db/tables/{table}`."));
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return getItems(payload).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
    
    private List<Map<String, Object>> getItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.getRequiredObjectList(payload, "items");
    }
}
