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

import org.apache.shardingsphere.mcp.bootstrap.H2RuntimeTestSupport;
import org.apache.shardingsphere.mcp.bootstrap.transport.MCPTransportConstants;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class StdioTransportIntegrationTest {
    
    @TempDir
    private Path tempDir;
    
    @Test
    void assertBootstrapWithStdioTransport() throws Exception {
        Path configFile = createRuntimeDatabasesConfigFile();
        try (StdioTransportTestClient client = new StdioTransportTestClient(configFile)) {
            Map<String, Object> actualInitializeResult = client.request("init-1", "initialize",
                    Map.of("protocolVersion", MCPTransportConstants.PROTOCOL_VERSION, "capabilities", Map.of(), "clientInfo", Map.of("name", "stdio-test", "version", "1.0.0")));
            client.notifyServer("notifications/initialized", Map.of());
            Map<String, Object> actualToolsResult = client.request("tool-list-1", "tools/list", Map.of());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actualTools = (List<Map<String, Object>>) actualToolsResult.get("tools");
            Map<String, Object> actualListDatabasesTool = actualTools.stream()
                    .filter(each -> "list_databases".equals(each.get("name")))
                    .findFirst()
                    .orElseThrow();
            Map<String, Object> actualListDatabasesInputSchema = castToMap(actualListDatabasesTool.get("inputSchema"));
            assertThat(actualInitializeResult.get("protocolVersion"), is(MCPTransportConstants.PROTOCOL_VERSION));
            assertTrue(actualTools.stream().anyMatch(each -> "list_databases".equals(each.get("name"))));
            assertTrue(castToMap(actualListDatabasesInputSchema.get("properties")).isEmpty());
            Map<String, Object> actualGetCapabilitiesTool = actualTools.stream()
                    .filter(each -> "get_capabilities".equals(each.get("name")))
                    .findFirst()
                    .orElseThrow();
            Map<String, Object> actualGetCapabilitiesInputSchema = castToMap(actualGetCapabilitiesTool.get("inputSchema"));
            Map<String, Object> actualGetCapabilitiesProperties = castToMap(actualGetCapabilitiesInputSchema.get("properties"));
            assertTrue(actualGetCapabilitiesProperties.containsKey("database"));
            assertThat(String.valueOf(castToMap(actualGetCapabilitiesProperties.get("database")).get("type")), is("string"));
            Map<String, Object> actualCallToolResult = client.request("tool-call-1", "tools/call", Map.of("name", "list_databases", "arguments", Map.of()));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualStructuredContent = (Map<String, Object>) actualCallToolResult.get("structuredContent");
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> actualItems = (List<Map<String, Object>>) actualStructuredContent.get("items");
            assertThat(actualItems.size(), is(1));
        }
    }
    
    private Path createRuntimeDatabasesConfigFile() throws IOException {
        String jdbcUrl = H2RuntimeTestSupport.createJdbcUrl(tempDir, "stdio-transport").replace(";DB_CLOSE_DELAY=-1", "");
        initializeDatabase(jdbcUrl);
        Path result = tempDir.resolve("mcp-runtime-databases.yaml");
        Files.writeString(result, "transport:\n"
                + "  http:\n"
                + "    enabled: false\n"
                + "    bindHost: 127.0.0.1\n"
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
                + "    driverClassName: org.h2.Driver\n");
        return result;
    }
    
    private void initializeDatabase(final String jdbcUrl) {
        try {
            H2RuntimeTestSupport.initializeDatabase(jdbcUrl);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Object> castToMap(final Object value) {
        return (Map<String, Object>) value;
    }
}
