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

package org.apache.shardingsphere.mcp.bootstrap.transport.server.stdio;

import org.apache.shardingsphere.mcp.bootstrap.fixture.BootstrapMockRuntimeDriver;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StdioTransportIntegrationTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertBootstrapWithClasspathDrivenPluginDiscovery() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile, createClasspathWithoutOfficialFeatures())) {
            Map<String, Object> actualInitializeResult = client.initialize();
            client.notifyInitialized();
            List<Map<String, Object>> actualTools = client.listTools();
            assertThat(actualInitializeResult.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
            assertTrue(actualTools.stream().anyMatch(each -> "search_metadata".equals(each.get("name"))));
            assertTrue(actualTools.stream().anyMatch(each -> "execute_query".equals(each.get("name"))));
            assertTrue(actualTools.stream().anyMatch(each -> "fixture_ping".equals(each.get("name"))));
            assertFalse(actualTools.stream().anyMatch(each -> "plan_encrypt_rule".equals(each.get("name"))));
            assertFalse(actualTools.stream().anyMatch(each -> "plan_mask_rule".equals(each.get("name"))));
            Map<String, Object> actualStructuredContent = client.callTool("search_metadata",
                    Map.of("database", "logic_db", "query", "order", "object_types", List.of("table")));
            List<Map<String, Object>> actualItems = client.getItems(actualStructuredContent);
            assertThat(actualItems.size(), is(2));
            assertThat(actualItems.get(0).get("name"), is("order_items"));
            assertThat(actualItems.get(1).get("name"), is("orders"));
            Map<String, Object> fixturePayload = client.callTool("fixture_ping", Map.of("message", "hello"));
            assertThat(fixturePayload.get("status"), is("ready"));
            assertThat(fixturePayload.get("echo"), is("hello"));
            Map<String, Object> actualFixtureResource = client.readResourcePayload("shardingsphere://features/test-fixture/status");
            List<Map<String, Object>> actualFixtureItems = client.getItems(actualFixtureResource);
            assertThat(actualFixtureItems.size(), is(1));
            assertThat(actualFixtureItems.get(0).get("feature"), is("test-fixture"));
            assertThat(actualFixtureItems.get(0).get("status"), is("ready"));
            Map<String, Object> actualCapabilities = client.readResourcePayload("shardingsphere://capabilities");
            List<String> actualSupportedTools = client.getStringList(actualCapabilities, "supportedTools");
            assertTrue(actualSupportedTools.contains("search_metadata"));
            assertTrue(actualSupportedTools.contains("execute_query"));
            assertTrue(actualSupportedTools.contains("fixture_ping"));
            assertFalse(actualSupportedTools.contains("plan_encrypt_rule"));
            assertFalse(actualSupportedTools.contains("plan_mask_rule"));
            List<String> actualSupportedResources = client.getStringList(actualCapabilities, "supportedResources");
            assertTrue(actualSupportedResources.contains("shardingsphere://features/test-fixture/status"));
            assertFalse(actualSupportedResources.contains("shardingsphere://features/encrypt/algorithms"));
            assertFalse(actualSupportedResources.contains("shardingsphere://features/mask/algorithms"));
        }
    }
    
    private String createClasspathWithoutOfficialFeatures() {
        return Arrays.stream(System.getProperty("java.class.path").split(Pattern.quote(File.pathSeparator)))
                .filter(each -> !isOfficialFeatureClasspathEntry(each))
                .collect(Collectors.joining(File.pathSeparator));
    }
    
    private boolean isOfficialFeatureClasspathEntry(final String classpathEntry) {
        String actualPath = classpathEntry.replace('\\', '/');
        return actualPath.contains("shardingsphere-mcp-feature-encrypt")
                || actualPath.contains("shardingsphere-mcp-feature-mask")
                || actualPath.contains("/mcp/features/encrypt/target/")
                || actualPath.contains("/mcp/features/mask/target/");
    }
    
    private Path createRuntimeDatabasesConfigFile() throws IOException {
        String jdbcUrl = BootstrapMockRuntimeDriver.createJdbcUrl("stdio-transport");
        Path result = tempDir.resolve("mcp-runtime-databases.yaml");
        Files.writeString(result, "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
                + "    allowRemoteAccess: false\n"
                + "    port: 18088\n"
                + "    endpointPath: /mcp\n"
                + "  stdio:\n"
                + "    enabled: true\n"
                + "runtimeDatabases:\n"
                + "  logic_db:\n"
                + "    databaseType: H2\n"
                + "    jdbcUrl: '" + jdbcUrl + "'\n"
                + "    username: ''\n"
                + "    password: ''\n"
                + "    driverClassName: " + BootstrapMockRuntimeDriver.class.getName() + '\n');
        return result;
    }
}
