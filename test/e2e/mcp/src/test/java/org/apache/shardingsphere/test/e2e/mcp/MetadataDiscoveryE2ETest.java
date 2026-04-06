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

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MetadataDiscoveryE2ETest extends AbstractMCPE2ETest {
    
    @Test
    void assertMetadataResourcesExposeBaselineObjectsOnly() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, createRequestHeaders(), sessionId,
                "shardingsphere://databases/logic_db/schemas/public/tables");
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        List<Map<String, Object>> items = getPayloadItems(payload);
        assertThat(items.size(), is(2));
        assertThat(String.valueOf(items.get(0).get("table")), is("order_items"));
        assertThat(String.valueOf(items.get(1).get("table")), is("orders"));
    }
    
    @Test
    void assertMetadataToolsExcludeNonBaselineObjects() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        Set<String> actualNames = new LinkedHashSet<>();
        
        HttpResponse<String> actual = sendToolCallRequest(httpClient, createRequestHeaders(), sessionId, "search_metadata",
                Map.of("database", "logic_db", "schema", "public", "query", "order",
                        "object_types", List.of("TABLE", "VIEW", "INDEX", "MATERIALIZED_VIEW", "SEQUENCE")));
        
        assertThat(actual.statusCode(), is(200));
        for (Map<String, Object> each : getPayloadItems(getStructuredContent(actual.body()))) {
            actualNames.add(String.valueOf(each.get("name")));
        }
        assertTrue(actualNames.contains("orders"));
        assertTrue(actualNames.contains("order_items"));
        assertTrue(actualNames.contains("active_orders"));
        assertTrue(actualNames.contains("idx_orders_status"));
        assertFalse(actualNames.contains("mv_orders"));
        assertFalse(actualNames.contains("order_seq"));
    }
    
    @Test
    void assertMetadataResourcesRejectUnsupportedIndex() throws IOException, InterruptedException {
        launchRuntime();
        HttpClient httpClient = createHttpClient();
        String sessionId = initializeSession(httpClient);
        
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, createRequestHeaders(), sessionId,
                "shardingsphere://databases/warehouse/schemas/warehouse/tables/facts/indexes");
        
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getFirstResourcePayload(actual.body());
        assertThat(String.valueOf(payload.get("error_code")), is("unsupported"));
    }
}
