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

import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.core.context.MCPServiceHandlerContext;
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
import java.util.Comparator;
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
        Map<String, Object> actual = new ServerCapabilitiesHandler().handle(mock(MCPServiceHandlerContext.class), new MCPUriVariables(Map.of())).toPayload();
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "capabilities.yaml", createCapabilitiesContract(actual));
    }
    
    @Test
    void assertGuidanceBaselineContractProjection() {
        Map<String, Object> actual = new ServerGuidanceHandler().handle(mock(MCPServiceHandlerContext.class), new MCPUriVariables(Map.of())).toPayload();
        MCPBaselineContractAssertions.assertMatchesNormalizedBaselineContract(BASELINE_RESOURCE_PATH + "guidance.yaml", createGuidanceContract(actual));
    }
    
    private Map<String, Object> createCapabilitiesContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(8, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("guidanceResource", payload.get("guidanceResource"));
        result.put("protocolAvailability", payload.get("protocolAvailability"));
        result.put("resources", summarizeCapabilityResourceIdentities(MCPInteractionPayloads.getOptionalObjectList(payload, "resources")));
        result.put("resourceTemplates", summarizeCapabilityResourceTemplateIdentities(MCPInteractionPayloads.getOptionalObjectList(payload, "resourceTemplates")));
        result.put("tools", summarizeCapabilityTools(MCPInteractionPayloads.getOptionalObjectList(payload, "tools")));
        result.put("prompts", summarizePrompts(MCPInteractionPayloads.getOptionalObjectList(payload, "prompts")));
        result.put("completionTargets", payload.get("completionTargets"));
        return result;
    }
    
    private Map<String, Object> createGuidanceContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(9, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("guidance_resource", payload.get("guidance_resource"));
        result.put("model_first_summary", payload.get("model_first_summary"));
        result.put("model_contract", payload.get("model_contract"));
        result.put("surface_summary", payload.get("surface_summary"));
        result.put("field_naming_contract", payload.get("field_naming_contract"));
        result.put("next_action_contract", payload.get("next_action_contract"));
        result.put("common_flows", payload.get("common_flows"));
        result.put("security_hints", payload.get("security_hints"));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceIdentities(final List<Map<String, Object>> resources) {
        return resources.stream().map(this::summarizeCapabilityResourceIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceIdentity(final Map<String, Object> resource) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uri", resource.get("uri"));
        result.put("name", resource.get("name"));
        result.put("_meta", summarizeResourceMeta(resource));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceTemplateIdentities(final List<Map<String, Object>> resourceTemplates) {
        return resourceTemplates.stream().map(this::summarizeCapabilityResourceTemplateIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceTemplateIdentity(final Map<String, Object> resourceTemplate) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uriTemplate", resourceTemplate.get("uriTemplate"));
        result.put("name", resourceTemplate.get("name"));
        result.put("_meta", summarizeResourceMeta(resourceTemplate));
        return result;
    }
    
    private Map<String, Object> summarizeResourceMeta(final Map<String, Object> resource) {
        Map<String, Object> meta = MCPInteractionPayloads.getOptionalObject(resource, "_meta");
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put(MCPShardingSphereMetadataKeys.RESOURCE_KIND, meta.get(MCPShardingSphereMetadataKeys.RESOURCE_KIND));
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_TOOLS, (List<?>) meta.get(MCPShardingSphereMetadataKeys.RELATED_TOOLS));
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.URI_VARIABLES,
                summarizeParameters(MCPInteractionPayloads.getOptionalObjectList(meta, MCPShardingSphereMetadataKeys.URI_VARIABLES)));
        return removeNullValues(result);
    }
    
    private void putIfNotEmpty(final Map<String, Object> target, final String key, final List<?> value) {
        if (null != value && !value.isEmpty()) {
            target.put(key, value);
        }
    }
    
    private List<Map<String, Object>> summarizeCapabilityTools(final List<Map<String, Object>> tools) {
        return tools.stream().map(this::summarizeCapabilityTool).toList();
    }
    
    private Map<String, Object> summarizeCapabilityTool(final Map<String, Object> tool) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("name", tool.get("name"));
        result.put("annotations", tool.getOrDefault("annotations", Map.of()));
        return result;
    }
    
    private List<Map<String, Object>> summarizePrompts(final List<Map<String, Object>> prompts) {
        return prompts.stream().sorted(Comparator.comparing(each -> String.valueOf(each.get("name")))).map(this::summarizePrompt).toList();
    }
    
    private Map<String, Object> summarizePrompt(final Map<String, Object> prompt) {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("name", prompt.get("name"));
        result.put("title", prompt.get("title"));
        result.put("arguments", summarizeParameters(MCPInteractionPayloads.getOptionalObjectList(prompt, "arguments")));
        return result;
    }
    
    private Map<String, Object> removeNullValues(final Map<String, Object> value) {
        return value.entrySet().stream().filter(entry -> null != entry.getValue()).collect(LinkedHashMap::new, (map, entry) -> map.put(entry.getKey(), entry.getValue()), Map::putAll);
    }
    
    private List<Map<String, Object>> summarizeParameters(final List<Map<String, Object>> params) {
        return params.stream().map(this::summarizeParameter).toList();
    }
    
    private Map<String, Object> summarizeParameter(final Map<String, Object> parameter) {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("name", parameter.get("name"));
        result.put("required", parameter.get("required"));
        return result;
    }
    
}
