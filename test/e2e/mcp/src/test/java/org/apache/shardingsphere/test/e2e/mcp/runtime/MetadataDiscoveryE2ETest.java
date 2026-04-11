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

package org.apache.shardingsphere.test.e2e.mcp.runtime;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MetadataDiscoveryE2ETest extends AbstractMCPE2ETest implements MetadataResourceContractTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertMetadataResourcesCases")
    void assertMetadataResources(final String name, final String resourceUri, final String itemKey,
                                 final List<String> expectedNames) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, createRequestHeaders(), sessionId, resourceUri);
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        List<Map<String, Object>> items = getPayloadItems(payload);
        assertThat(items.stream().map(each -> String.valueOf(each.get(itemKey))).toList(), is(expectedNames));
    }
    
    static Stream<Arguments> assertMetadataResourcesCases() {
        return Stream.of(
                Arguments.of("tables resource", "shardingsphere://databases/logic_db/schemas/public/tables", "table", List.of("order_items", "orders")),
                Arguments.of("views resource", "shardingsphere://databases/logic_db/schemas/public/views", "view", List.of("active_orders")),
                Arguments.of("indexes resource", "shardingsphere://databases/logic_db/schemas/public/tables/orders/indexes", "index", List.of("idx_orders_status")),
                Arguments.of("sequences resource", "shardingsphere://databases/analytics_db/schemas/public/sequences", "sequence", List.of("metric_seq")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertSearchMetadataCases")
    void assertSearchMetadata(final String name, final String databaseName, final String query, final List<String> objectTypes,
                              final List<String> expectedNames) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "search_metadata",
                Map.of("database", databaseName, "schema", "public", "query", query, "object_types", objectTypes));
        assertThat(actual.statusCode(), is(200));
        assertThat(getPayloadItems(getStructuredContent(actual.body())).stream().map(each -> String.valueOf(each.get("name"))).toList(), is(expectedNames));
    }
    
    static Stream<Arguments> assertSearchMetadataCases() {
        return Stream.of(
                Arguments.of("search tables", "logic_db", "order", List.of("TABLE"), List.of("order_items", "orders")),
                Arguments.of("search views", "logic_db", "order", List.of("VIEW"), List.of("active_orders")),
                Arguments.of("search columns", "logic_db", "status", List.of("COLUMN"), List.of("status")),
                Arguments.of("search indexes", "logic_db", "status", List.of("INDEX"), List.of("idx_orders_status")),
                Arguments.of("search sequences", "analytics_db", "metric", List.of("SEQUENCE"), List.of("metric_seq")));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertRejectUnsupportedMetadataRequestCases")
    void assertRejectUnsupportedMetadataRequest(final String name, final Map<String, Object> toolArguments, final String resourceUri,
                                                final Map<String, String> expectedPayload) throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual;
        Map<String, Object> actualPayload;
        if (resourceUri.isEmpty()) {
            actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "search_metadata", toolArguments);
            actualPayload = getStructuredContent(actual.body());
        } else {
            actual = sendResourceReadRequest(httpClient, createRequestHeaders(), sessionId, resourceUri);
            actualPayload = getFirstResourcePayload(actual.body());
        }
        assertThat(actual.statusCode(), is(200));
        for (Entry<String, String> entry : expectedPayload.entrySet()) {
            assertThat(String.valueOf(actualPayload.get(entry.getKey())), is(entry.getValue()));
        }
    }
    
    static Stream<Arguments> assertRejectUnsupportedMetadataRequestCases() {
        return Stream.of(
                Arguments.of("unsupported object type",
                        Map.of("database", "logic_db", "schema", "public", "query", "order",
                                "object_types", List.of("TABLE", "VIEW", "INDEX", "MATERIALIZED_VIEW", "SEQUENCE")),
                        "",
                        Map.of("error_code", "invalid_request", "message", "Unsupported object_types value `MATERIALIZED_VIEW`.")),
                Arguments.of("unknown object type",
                        Map.of("database", "logic_db", "schema", "public", "query", "order",
                                "object_types", List.of("invalid_type")),
                        "",
                        Map.of("error_code", "invalid_request", "message", "Unsupported object_types value `invalid_type`.")),
                Arguments.of("unsupported indexes resource", Map.of(),
                        "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes",
                        Map.of("error_code", "unsupported")));
    }
    
    @Override
    public void launchContractRuntime() {
        launchRuntime();
    }
    
    @Override
    public HttpClient createContractHttpClient() {
        return createHttpClient();
    }
    
    @Override
    public String initializeContractSession(final HttpClient httpClient) throws IOException, InterruptedException {
        return initializeSession(httpClient);
    }
    
    @Override
    public HttpResponse<String> readContractResource(final HttpClient httpClient, final String sessionId, final String resourceUri) throws IOException, InterruptedException {
        return sendResourceReadRequest(httpClient, createRequestHeaders(), sessionId, resourceUri);
    }
    
    @Override
    public Map<String, Object> getContractResourcePayload(final String responseBody) {
        return getFirstResourcePayload(responseBody);
    }
    
    @Override
    public List<String> getExpectedTableColumns() {
        return List.of("order_id", "status");
    }
    
    @Override
    public List<String> getExpectedTableIndexes() {
        return List.of("idx_orders_status");
    }
}
