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

import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MetadataDiscoveryE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    @Test
    void assertSearchMetadataTablesAndViews() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order", "object_types", List.of("TABLE", "VIEW")));
        assertThat(actual.statusCode(), is(200));
        assertThat(getItemNames(getStructuredContent(actual.body())), is(List.of("order_items", "orders", "active_orders")));
    }
    
    @Test
    void assertRejectUnsupportedObjectType() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order",
                        "object_types", List.of("TABLE", "VIEW", "INDEX", "MATERIALIZED_VIEW", "SEQUENCE")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> actualPayload = getStructuredContent(actual.body());
        assertThat(String.valueOf(actualPayload.get("error_code")), is("invalid_request"));
        assertThat(String.valueOf(actualPayload.get("message")), is("Unsupported object_types value `MATERIALIZED_VIEW`."));
    }
    
    @Test
    void assertRejectUnsupportedIndexesResource() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId,
                "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes");
        assertThat(actual.statusCode(), is(200));
        assertThat(String.valueOf(getFirstResourcePayload(actual.body()).get("error_code")), is("unsupported"));
    }
    
    private List<String> getItemNames(final Map<String, Object> payload) {
        return MCPInteractionPayloads.castToList(payload.get("items")).stream().map(each -> String.valueOf(each.get("name"))).toList();
    }
}
