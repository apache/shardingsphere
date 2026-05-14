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
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPHttpTransportTestSupport;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpTransportCompletionE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertCompleteMetadataValues() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> promptReference = Map.of("type", "ref/prompt", "name", "inspect_metadata");
        assertThat(completeValues(httpClient, sessionId, promptReference, "database", "logic", Map.of()), is(List.of("logic_db")));
        assertThat(completeValues(httpClient, sessionId, promptReference, "schema", "pub", Map.of("database", "logic_db")), is(List.of("public")));
        assertThat(completeValues(httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}"),
                "table", "ord", Map.of("database", "logic_db", "schema", "public")),
                is(List.of("order_items", "orders")));
        assertThat(completeValues(httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}"),
                "column", "sta", Map.of("database", "logic_db", "schema", "public", "table", "orders")),
                is(List.of("status")));
        assertThat(completeValues(httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"),
                "index", "idx", Map.of("database", "logic_db", "schema", "public", "table", "orders")),
                is(List.of("idx_orders_status")));
        assertThat(completeValues(httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}"),
                "sequence", "ord", Map.of("database", "logic_db", "schema", "public")),
                is(List.of("order_seq")));
    }
    
    @Test
    void assertCompleteMaskAlgorithmValues() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        List<String> actual = completeValues(httpClient, sessionId, Map.of("type", "ref/prompt", "name", "database_gateway_plan_mask_rule"),
                "algorithm_type", "KEEP", Map.of());
        assertTrue(actual.contains("KEEP_FIRST_N_LAST_M"));
    }
    
    @Test
    void assertCompleteWorkflowPlanIdsWithinCurrentSession() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String firstSessionId = initializeSession(httpClient);
        String secondSessionId = initializeSession(httpClient);
        String firstPlanId = createMaskRulePlan(httpClient, firstSessionId);
        String secondPlanId = createMaskRulePlan(httpClient, secondSessionId);
        Map<String, Object> reference = Map.of("type", "ref/resource", "uri", "shardingsphere://workflows/{plan_id}");
        List<String> firstSessionValues = completeValues(httpClient, firstSessionId, reference, "plan_id", "", Map.of());
        assertTrue(firstSessionValues.contains(firstPlanId));
        assertFalse(firstSessionValues.contains(secondPlanId));
        List<String> secondSessionValues = completeValues(httpClient, secondSessionId, reference, "plan_id", "", Map.of());
        assertTrue(secondSessionValues.contains(secondPlanId));
        assertFalse(secondSessionValues.contains(firstPlanId));
    }
    
    private List<String> completeValues(final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                        final String argumentName, final String argumentValue,
                                        final Map<String, String> contextArguments) throws IOException, InterruptedException {
        Map<String, Object> completion = castToMap(complete(httpClient, sessionId, reference, argumentName, argumentValue, contextArguments).get("completion"));
        return ((List<?>) completion.get("values")).stream().map(String::valueOf).toList();
    }
    
    private Map<String, Object> createResourceReference(final String uriTemplate) {
        return Map.of("type", "ref/resource", "uri", uriTemplate);
    }
    
    private Map<String, Object> complete(final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                         final String argumentName, final String argumentValue,
                                         final Map<String, String> contextArguments) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendCompletionRequest(httpClient, sessionId, reference, argumentName, argumentValue, contextArguments);
        assertThat(actual.statusCode(), is(200));
        return castToMap(parseJsonBody(actual.body()).get("result"));
    }
    
    private HttpResponse<String> sendCompletionRequest(final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                                       final String argumentName, final String argumentValue,
                                                       final Map<String, String> contextArguments) throws IOException, InterruptedException {
        Map<String, Object> params = new LinkedHashMap<>(3, 1F);
        params.put("ref", reference);
        params.put("argument", Map.of("name", argumentName, "value", argumentValue));
        if (!contextArguments.isEmpty()) {
            params.put("context", Map.of("arguments", contextArguments));
        }
        return sendRawPostRequest(httpClient, createSessionHeaders(sessionId), MCPHttpTransportTestSupport.createJsonRpcRequestBody(
                "completion-1", "completion/complete", params));
    }
    
    private String createMaskRulePlan(final HttpClient httpClient, final String sessionId) throws IOException, InterruptedException {
        HttpResponse<String> actual = sendToolCallRequest(httpClient, sessionId, "database_gateway_plan_mask_rule", Map.of(
                "database", "logic_db",
                "schema", "public",
                "table", "orders",
                "column", "status",
                "operation_type", "create",
                "algorithm_type", "KEEP_FIRST_N_LAST_M",
                "primary_algorithm_properties", Map.of("first-n", "1", "last-m", "1", "replace-char", "*")));
        assertThat(actual.statusCode(), is(200));
        Map<String, Object> payload = getStructuredContent(actual.body());
        assertThat(String.valueOf(payload.get("status")), is("planned"));
        return String.valueOf(payload.get("plan_id"));
    }
}
