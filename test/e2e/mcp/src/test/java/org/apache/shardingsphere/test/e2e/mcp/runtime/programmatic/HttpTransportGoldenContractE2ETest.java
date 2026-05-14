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
import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.assertion.MCPGoldenContractAssertions;
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

@EnabledIf("isEnabled")
class HttpTransportGoldenContractE2ETest extends AbstractHttpProgrammaticRuntimeE2ETest {
    
    private static final String GOLDEN_RESOURCE_PATH = "golden/model-contract/";
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isContractEnabled();
    }
    
    @Test
    void assertCapabilitiesGoldenContract() throws IOException, InterruptedException {
        launchHttpTransport();
        HttpClient httpClient = HttpClient.newHttpClient();
        String sessionId = initializeSession(httpClient);
        HttpResponse<String> actual = sendResourceReadRequest(httpClient, sessionId, "shardingsphere://capabilities");
        assertThat(actual.statusCode(), is(200));
        MCPGoldenContractAssertions.assertMatchesNormalizedGoldenContract(GOLDEN_RESOURCE_PATH + "capabilities.yaml", createCapabilitiesContract(getFirstResourcePayload(actual.body())));
    }
    
    private Map<String, Object> createCapabilitiesContract(final Map<String, Object> payload) {
        Map<String, Object> result = new LinkedHashMap<>(13, 1F);
        result.put("response_mode", payload.get("response_mode"));
        result.put("model_first_summary", payload.get("model_first_summary"));
        result.put("model_contract", payload.get("model_contract"));
        result.put("surface_summary", payload.get("surface_summary"));
        result.put("field_naming_contract", payload.get("field_naming_contract"));
        result.put("next_action_contract", payload.get("next_action_contract"));
        result.put("common_flows", payload.get("common_flows"));
        result.put("protocolAvailability", payload.get("protocolAvailability"));
        result.put("fingerprints", payload.get("fingerprints"));
        result.put("resources", summarizeCapabilityResourceIdentities(castToMapList(payload.get("resources"))));
        result.put("resourceTemplates", summarizeCapabilityResourceTemplateIdentities(castToMapList(payload.get("resourceTemplates"))));
        result.put("tools", summarizeCapabilityTools(castToMapList(payload.get("tools"))));
        result.put("prompts", summarizePrompts(castToMapList(payload.get("prompts"))));
        result.put("completionTargets", payload.get("completionTargets"));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceIdentities(final List<Map<String, Object>> resources) {
        return resources.stream().map(this::summarizeCapabilityResourceIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceIdentity(final Map<String, Object> resource) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uri", resource.get("uri"));
        result.put("name", resource.get("name"));
        result.put("meta", summarizeResourceMeta(resource));
        return result;
    }
    
    private List<Map<String, Object>> summarizeCapabilityResourceTemplateIdentities(final List<Map<String, Object>> resourceTemplates) {
        return resourceTemplates.stream().map(this::summarizeCapabilityResourceTemplateIdentity).toList();
    }
    
    private Map<String, Object> summarizeCapabilityResourceTemplateIdentity(final Map<String, Object> resourceTemplate) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("uriTemplate", resourceTemplate.get("uriTemplate"));
        result.put("name", resourceTemplate.get("name"));
        result.put("meta", summarizeResourceMeta(resourceTemplate));
        return result;
    }
    
    private Map<String, Object> summarizeResourceMeta(final Map<String, Object> resource) {
        Map<String, Object> meta = castToMap(resource.getOrDefault("meta", Map.of()));
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put(MCPShardingSphereMetadataKeys.RESOURCE_KIND, meta.get(MCPShardingSphereMetadataKeys.RESOURCE_KIND));
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.RELATED_TOOLS, (List<?>) meta.get(MCPShardingSphereMetadataKeys.RELATED_TOOLS));
        putIfNotEmpty(result, MCPShardingSphereMetadataKeys.URI_VARIABLES, summarizeParameters(castToMapList(meta.get(MCPShardingSphereMetadataKeys.URI_VARIABLES))));
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
        result.put("arguments", summarizeParameters(castToMapList(prompt.get("arguments"))));
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
    
    private List<Map<String, Object>> castToMapList(final Object value) {
        if (null == value) {
            return List.of();
        }
        return ((List<?>) value).stream().map(this::castToMap).toList();
    }
}
