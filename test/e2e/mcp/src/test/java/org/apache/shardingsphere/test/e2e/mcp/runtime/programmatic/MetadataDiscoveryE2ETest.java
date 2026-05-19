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
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class MetadataDiscoveryE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertSearchMetadataTablesAndViews() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("table", "view")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getStructuredContent(actual.body());
        List<Map<String, Object>> actualItems = getItems(actualPayload);
        assertThat(getItemNames(actualPayload), is(List.of("order_items", "orders", "active_orders")));
        Map<String, Object> actualResource = MCPInteractionPayloads.castToMap(actualItems.get(1).get("resource"));
        assertThat(String.valueOf(actualResource.get("uri")), is("shardingsphere://databases/logic_db/schemas/public/tables/orders"));
        assertThat(MCPInteractionPayloads.castToList(actualItems.get(1).get("next_resources")).stream()
                .map(each -> String.valueOf(MCPInteractionPayloads.castToMap(each).get("uri"))).toList(),
                is(List.of("shardingsphere://databases/logic_db/schemas/public/tables/orders/columns",
                        "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes")));
        HttpResponse<String> tableResource = sendResourceReadRequest(httpClient, sessionId, String.valueOf(actualResource.get("uri")));
        assertThat(tableResource.statusCode(), is(200));
        assertThat(String.valueOf(MCPInteractionPayloads.castToList(getFirstResourcePayload(tableResource.body()).get("items")).get(0).get("table")), is("orders"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertSearchMetadataObjectTypeCases")
    void assertSearchMetadataByObjectType(final String name, final Map<String, Object> arguments, final List<String> expectedNames) throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", arguments);
        assertThat(actual.statusCode(), is(200));
        assertThat(getItemNames(getStructuredContent(actual.body())), is(expectedNames));
    }
    
    private static Stream<Arguments> assertSearchMetadataObjectTypeCases() {
        return Stream.of(
                Arguments.of("database objects", createSearchArguments("", "", "logic", List.of("database")), List.of("logic_db")),
                Arguments.of("schema objects", createSearchArguments("logic_db", "", "public", List.of("schema")), List.of("public")),
                Arguments.of("column objects", createSearchArguments("logic_db", "", "status", List.of("column")), List.of("status", "status")),
                Arguments.of("index objects", createSearchArguments("logic_db", "", "status", List.of("index")), List.of("idx_orders_status")));
    }
    
    @Test
    void assertSearchMetadataAcrossDatabasesByDefault() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", Map.of("query", "metric"));
        assertThat(actual.statusCode(), is(200));
        assertThat(getItemNames(getStructuredContent(actual.body())), is(List.of("metrics", "metric_id", "metric_name", "PRIMARY_KEY_3")));
    }
    
    @Test
    void assertSearchMetadataWithPagination() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> firstPageArguments = createSearchArguments("logic_db", "public", "order", List.of("table", "view"));
        firstPageArguments.put("page_size", 2);
        HttpResponse<String> firstPage = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", firstPageArguments);
        assertThat(firstPage.statusCode(), is(200));
        Map<String, Object> firstPagePayload = getStructuredContent(firstPage.body());
        assertThat(getItemNames(firstPagePayload), is(List.of("order_items", "orders")));
        assertTrue((boolean) firstPagePayload.get("has_more"));
        String nextPageToken = String.valueOf(firstPagePayload.get("next_page_token"));
        assertFalse(nextPageToken.isEmpty());
        assertFalse(nextPageToken.matches("\\d+"));
        Map<String, Object> secondPageArguments = createSearchArguments("logic_db", "public", "order", List.of("table", "view"));
        secondPageArguments.put("page_size", 2);
        secondPageArguments.put("page_token", nextPageToken);
        HttpResponse<String> secondPage = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", secondPageArguments);
        assertThat(secondPage.statusCode(), is(200));
        Map<String, Object> secondPagePayload = getStructuredContent(secondPage.body());
        assertThat(getItemNames(secondPagePayload), is(List.of("active_orders")));
        assertFalse((boolean) secondPagePayload.get("has_more"));
        assertFalse(secondPagePayload.containsKey("next_page_token"));
    }
    
    @Test
    void assertSearchMetadataWithOutOfRangePageToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> arguments = createSearchArguments("logic_db", "public", "order", List.of("table", "view"));
        arguments.put("page_token", "99");
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", arguments);
        assertThat(actual.statusCode(), is(200));
        assertThat(getItemNames(getStructuredContent(actual.body())), is(List.of()));
    }
    
    @Test
    void assertRejectSchemaSearchWithoutDatabase() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", Map.of("schema", "public", "query", "order"));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getStructuredContent(actual.body());
        assertThat(String.valueOf(actualPayload.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(actualPayload.get("message")), is("Schema cannot be provided without database."));
    }
    
    @Test
    void assertRejectInvalidPageToken() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> arguments = createSearchArguments("logic_db", "public", "order", List.of("table", "view"));
        arguments.put("page_token", "invalid");
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata", arguments);
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getStructuredContent(actual.body());
        assertThat(String.valueOf(actualPayload.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(actualPayload.get("message")), is("Invalid page token."));
    }
    
    @Test
    void assertRejectUnsupportedObjectType() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order",
                        "object_types", List.of("table", "view", "index", "materialized_view", "sequence")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getStructuredContent(actual.body());
        assertThat(String.valueOf(actualPayload.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(actualPayload.get("message")), is("object_types[3] must be one of [database, schema, table, view, column, index, sequence]."));
    }
    
    @Test
    void assertReadWarehouseIndexesResource() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes");
        assertThat(actual.statusCode(), is(200));
        List<Map<String, Object>> actualItems = MCPInteractionPayloads.castToList(getFirstResourcePayload(actual.body()).get("items"));
        assertThat(actualItems.size(), is(1));
        assertThat(String.valueOf(actualItems.get(0).get("table")), is("facts"));
        assertTrue(String.valueOf(actualItems.get(0).get("index")).startsWith("PRIMARY_KEY_"));
    }
    
    @Test
    void assertReadResourceUriWithEncodedSegments() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> databaseResource = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://databases/logic%5Fdb");
        assertThat(databaseResource.statusCode(), is(200));
        assertThat(String.valueOf(castToMap(getFirstResourcePayload(databaseResource.body()).get("item")).get("database")), is("logic_db"));
        HttpResponse<String> tableResource = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/logic_db/schemas/public/tables/orders%20archive%2F2026");
        assertThat(tableResource.statusCode(), is(200));
        Map<String, Object> tablePayload = getFirstResourcePayload(tableResource.body());
        assertFalse((Boolean) tablePayload.get("found"));
        assertThat(String.valueOf(tablePayload.get("self_uri")),
                is("shardingsphere://databases/logic_db/schemas/public/tables/orders%20archive%2F2026"));
        assertThat(String.valueOf(castToMap(tablePayload.get("recovery")).get("requested_token")), is("orders archive/2026"));
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
        assertThat(String.valueOf(payload.get("message")), is("Resource not found"));
    }
    
    @Test
    void assertRejectUnexpandedResourceUriTemplate() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/logic_db/schemas/public/tables/{table}");
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("json_rpc_error"));
        assertThat(String.valueOf(payload.get("message")), is("Resource not found"));
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return getItems(payload).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
    
    private List<Map<String, Object>> getItems(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items"));
    }
    
    private static Map<String, Object> createSearchArguments(final String databaseName, final String schemaName, final String query, final List<String> objectTypes) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        if (!databaseName.isEmpty()) {
            result.put("database", databaseName);
        }
        if (!schemaName.isEmpty()) {
            result.put("schema", schemaName);
        }
        result.put("query", query);
        result.put("object_types", objectTypes);
        return result;
    }
}
