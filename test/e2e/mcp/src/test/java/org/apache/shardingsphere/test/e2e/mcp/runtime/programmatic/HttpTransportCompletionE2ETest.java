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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledIf("isEnabled")
class HttpTransportCompletionE2ETest extends AbstractSharedHttpProgrammaticRuntimeE2ETest {
    
    private static final String PLAN_MASK_PROMPT_NAME = "plan_mask_rule";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    void assertCompleteMetadataValues() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        Map<String, Object> promptReference = Map.of("type", "ref/prompt", "name", "inspect_metadata");
        assertCompletionValues("database completion", httpClient, sessionId, promptReference, "database", "logic", Map.of(), List.of("logic_db"));
        assertCompletionValues("schema completion", httpClient, sessionId, promptReference, "schema", "logic", Map.of("database", "logic_db"), List.of("logic_db"));
        assertCompletionValues("table completion", httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}"),
                "table", "ord", Map.of("database", "logic_db", "schema", "logic_db"), List.of("order_items", "orders"));
        assertCompletionValues("column completion", httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/columns/{column}"),
                "column", "sta", Map.of("database", "logic_db", "schema", "logic_db", "table", "orders"), List.of("status"));
        assertCompletionValues("index completion", httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/tables/{table}/indexes/{index}"),
                "index", "idx", Map.of("database", "logic_db", "schema", "logic_db", "table", "orders"), List.of("idx_orders_status"));
        assertCompletionValues("sequence completion", httpClient, sessionId, createResourceReference("shardingsphere://databases/{database}/schemas/{schema}/sequences/{sequence}"),
                "sequence", "ord", Map.of("database", "logic_db", "schema", "logic_db"), List.of());
    }
    
    @Test
    void assertCompleteMaskAlgorithmValues() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        List<String> actual = completeValues(httpClient, sessionId, Map.of("type", "ref/prompt", "name", PLAN_MASK_PROMPT_NAME),
                "algorithm_type", "KEEP", Map.of());
        assertTrue(actual.contains("KEEP_FIRST_N_LAST_M"));
    }
    
    @Test
    void assertCompleteFeaturePluginValues() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        assertCompletionValuesContain("encrypt algorithm completion", httpClient, sessionId,
                Map.of("type", "ref/prompt", "name", "plan_encrypt_rule"), "algorithm_type", "AES", "AES");
        assertCompletionValuesContain("readwrite-splitting load balancer completion", httpClient, sessionId,
                Map.of("type", "ref/prompt", "name", "plan_readwrite_splitting_rule"), "load_balancer_type", "ROUND", "ROUND_ROBIN");
        assertCompletionValuesContain("shadow algorithm completion", httpClient, sessionId,
                Map.of("type", "ref/prompt", "name", "plan_shadow_rule"), "algorithm_type", "VALUE", "VALUE_MATCH");
        assertCompletionValuesContain("sharding algorithm completion", httpClient, sessionId,
                Map.of("type", "ref/prompt", "name", "plan_sharding_table_rule"), "algorithm_type", "INLINE", "INLINE");
        assertCompletionValuesContain("sharding key generator completion", httpClient, sessionId,
                Map.of("type", "ref/prompt", "name", "plan_sharding_key_generator"), "key_generator_type", "SNOW", "SNOWFLAKE");
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
        return extractCompletionValues(complete(httpClient, sessionId, reference, argumentName, argumentValue, contextArguments));
    }
    
    private void assertCompletionValues(final String scenarioName, final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                        final String argumentName, final String argumentValue, final Map<String, String> contextArguments,
                                        final List<String> expectedValues) throws IOException, InterruptedException {
        assertThat(scenarioName, completeValues(httpClient, sessionId, reference, argumentName, argumentValue, contextArguments), is(expectedValues));
    }
    
    private void assertCompletionValuesContain(final String scenarioName, final HttpClient httpClient, final String sessionId, final Map<String, Object> reference,
                                               final String argumentName, final String argumentValue, final String expectedValue) throws IOException, InterruptedException {
        List<String> actualValues = completeValues(httpClient, sessionId, reference, argumentName, argumentValue, Map.of());
        assertTrue(actualValues.contains(expectedValue), () -> scenarioName + ": " + actualValues);
    }
    
    private List<String> extractCompletionValues(final Map<String, Object> result) {
        Map<String, Object> completion = castToMap(result.get("completion"));
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
        Map<String, Object> result = castToMap(parseJsonBody(actual.body()).get("result"));
        assertNotNull(result, actual.body());
        return result;
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
    
}
