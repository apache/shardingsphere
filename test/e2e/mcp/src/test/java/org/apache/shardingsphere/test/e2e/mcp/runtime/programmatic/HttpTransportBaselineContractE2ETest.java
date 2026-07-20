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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceURIVariables;
import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerCapabilitiesHandler;
import org.apache.shardingsphere.mcp.core.resource.handler.capability.ServerGuidanceHandler;
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPBaselineContractAssertions;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;

class HttpTransportBaselineContractE2ETest extends AbstractSharedHttpProgrammaticRuntimeE2ETest {
    
    private static final String BASELINE_RESOURCE_PATH = "baseline-contract/model-contract/";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDockerEnabled();
    }
    
    @Test
    @EnabledIf("isEnabled")
    void assertCapabilitiesBaselineContract() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "capabilities.yaml", createCapabilitiesContract(getFirstResourcePayload(actual.body())));
    }
    
    @Test
    @EnabledIf("isEnabled")
    void assertGuidanceBaselineContract() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://guidance");
        assertThat(actual.statusCode(), is(200));
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "guidance.yaml", createGuidanceContract(getFirstResourcePayload(actual.body())));
    }
    
    @Test
    void assertCapabilitiesBaselineContractProjection() {
        Map<String, Object> actual = new ServerCapabilitiesHandler().handle(mock(MCPRequestContext.class), new MCPResourceURIVariables(Map.of())).toPayload();
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "capabilities.yaml", createCapabilitiesContract(actual));
    }
    
    @Test
    void assertGuidanceBaselineContractProjection() {
        Map<String, Object> actual = new ServerGuidanceHandler().handle(mock(MCPRequestContext.class), new MCPResourceURIVariables(Map.of())).toPayload();
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "guidance.yaml", createGuidanceContract(actual));
    }
    
    private Map<String, Object> createCapabilitiesContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("supportedStatementClasses", ((Collection<?>) payload.get("supportedStatementClasses")).stream().map(String::valueOf).sorted().toList());
        result.put("completionTargets", summarizeCompletionTargets(MCPInteractionPayloads.getOptionalObjectList(payload, "completionTargets")));
        result.put("resourceNavigation", summarizeResourceNavigation(MCPInteractionPayloads.getOptionalObjectList(payload, "resourceNavigation")));
        return result;
    }
    
    private Map<String, Object> createGuidanceContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("discovery", payload.get("discovery"));
        result.put("model_contract", payload.get("model_contract"));
        result.put("next_action_contract", payload.get("next_action_contract"));
        result.put("common_flows", payload.get("common_flows"));
        result.put("security_hints", payload.get("security_hints"));
        return result;
    }
    
    private List<String> summarizeCompletionTargets(final List<Map<String, Object>> targets) {
        return targets.stream().map(each -> String.format("%s:%s:%s", each.get("referenceType"), each.get("reference"), each.get("arguments"))).sorted().toList();
    }
    
    private List<String> summarizeResourceNavigation(final List<Map<String, Object>> navigation) {
        return navigation.stream().map(each -> String.format("%s->%s", each.get("from"), each.get("to"))).sorted().toList();
    }
    
}
