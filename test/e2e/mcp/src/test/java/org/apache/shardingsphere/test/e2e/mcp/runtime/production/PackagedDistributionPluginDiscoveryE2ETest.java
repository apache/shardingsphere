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

package org.apache.shardingsphere.test.e2e.mcp.runtime.production;

import org.apache.shardingsphere.test.e2e.mcp.env.MCPE2ECondition;
import org.apache.shardingsphere.test.e2e.mcp.support.OfficialMCPToolNames;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionPluginFixtureSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport;
import org.apache.shardingsphere.test.e2e.mcp.support.distribution.PackagedDistributionTestSupport.PreparedPackagedDistribution;
import org.apache.shardingsphere.test.e2e.mcp.support.runtime.RuntimeTransport;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.MCPInteractionPayloads;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.MCPInteractionClient;
import org.apache.shardingsphere.test.e2e.mcp.support.transport.client.PackagedDistributionStdioInteractionClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;
import org.junit.jupiter.api.condition.EnabledOnOs;
import org.junit.jupiter.api.condition.OS;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@EnabledOnOs({OS.LINUX, OS.MAC, OS.WINDOWS})
@EnabledIf("isEnabled")
class PackagedDistributionPluginDiscoveryE2ETest {
    
    private static final List<String> CORE_TOOL_NAMES = List.of("search_metadata", "execute_query");
    
    private static final List<String> REMOVED_OFFICIAL_TOOL_NAMES = OfficialMCPToolNames.getAll().stream().filter(each -> !CORE_TOOL_NAMES.contains(each)).toList();
    
    private static final List<String> REMOVED_OFFICIAL_RESOURCE_URIS = List.of("shardingsphere://features/encrypt/algorithms", "shardingsphere://features/mask/algorithms");
    
    private static final String FIXTURE_RESOURCE_URI = "shardingsphere://features/test-fixture/status";
    
    @TempDir
    private Path tempDir;
    
    private static boolean isEnabled() {
        return MCPE2ECondition.isDistributionEnabled();
    }
    
    @Test
    void assertDiscoverFixturePluginFromPluginsDirectory() throws IOException, InterruptedException {
        PreparedPackagedDistribution distribution = PackagedDistributionTestSupport.prepare(tempDir.resolve("plugin-discovery"), RuntimeTransport.STDIO);
        List<String> actualRemovedJarNames = PackagedDistributionPluginFixtureSupport.removeOfficialFeatureJars(distribution.home().resolve("lib"));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-encrypt")));
        assertTrue(actualRemovedJarNames.stream().anyMatch(each -> each.contains("shardingsphere-mcp-feature-mask")));
        Path actualFixturePluginJar = PackagedDistributionPluginFixtureSupport.createFixturePluginJar(distribution.home().resolve("plugins"));
        assertTrue(Files.isRegularFile(actualFixturePluginJar));
        try (MCPInteractionClient interactionClient = new PackagedDistributionStdioInteractionClient(distribution.home(), distribution.configFile())) {
            interactionClient.open();
            assertDiscoveredTools(interactionClient.listTools());
            assertSearchMetadataTool(interactionClient.call("search_metadata", Map.of("database", "orders", "query", "order", "object_types", List.of("TABLE"))));
            assertFixtureTool(interactionClient.call("fixture_ping", Map.of("message", "hello")));
            assertFixtureResource(interactionClient.readResource(FIXTURE_RESOURCE_URI));
            assertCapabilities(interactionClient.readResource("shardingsphere://capabilities"));
        }
    }
    
    private void assertDiscoveredTools(final List<Map<String, Object>> tools) {
        List<String> actualToolNames = tools.stream().map(each -> String.valueOf(each.get("name"))).toList();
        assertThat(actualToolNames, hasItems("search_metadata", "execute_query", "fixture_ping"));
        for (String each : REMOVED_OFFICIAL_TOOL_NAMES) {
            assertFalse(actualToolNames.contains(each));
        }
    }
    
    private void assertSearchMetadataTool(final Map<String, Object> payload) {
        List<String> actualItemNames = MCPInteractionPayloads.castToList(payload.get("items")).stream().map(each -> String.valueOf(each.get("name"))).toList();
        assertThat(actualItemNames, hasItems("order_items", "orders"));
    }
    
    private void assertFixtureTool(final Map<String, Object> payload) {
        assertThat(payload.get("status"), is("ready"));
        assertThat(payload.get("echo"), is("hello"));
    }
    
    private void assertFixtureResource(final Map<String, Object> payload) {
        List<Map<String, Object>> actualItems = MCPInteractionPayloads.castToList(payload.get("items"));
        assertThat(actualItems.size(), is(1));
        assertThat(actualItems.get(0).get("feature"), is("test-fixture"));
        assertThat(actualItems.get(0).get("status"), is("ready"));
    }
    
    private void assertCapabilities(final Map<String, Object> payload) {
        List<String> actualSupportedTools = ((List<?>) payload.get("supportedTools")).stream().map(String::valueOf).toList();
        assertThat(actualSupportedTools, hasItems("search_metadata", "execute_query", "fixture_ping"));
        for (String each : REMOVED_OFFICIAL_TOOL_NAMES) {
            assertFalse(actualSupportedTools.contains(each));
        }
        List<String> actualSupportedResources = ((List<?>) payload.get("supportedResources")).stream().map(String::valueOf).toList();
        assertTrue(actualSupportedResources.contains(FIXTURE_RESOURCE_URI));
        for (String each : REMOVED_OFFICIAL_RESOURCE_URIS) {
            assertFalse(actualSupportedResources.contains(each));
        }
    }
}
