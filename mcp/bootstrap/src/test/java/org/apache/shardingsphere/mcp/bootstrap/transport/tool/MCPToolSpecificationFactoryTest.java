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

package org.apache.shardingsphere.mcp.bootstrap.transport.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.metadata.jdbc.RuntimeDatabaseConfiguration;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadata;
import org.apache.shardingsphere.mcp.metadata.model.MCPDatabaseMetadataCatalog;
import org.apache.shardingsphere.mcp.metadata.model.MCPSchemaMetadata;
import org.apache.shardingsphere.mcp.session.MCPSessionManager;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.nio.file.Path;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest {
    
    @TempDir
    private Path tempDir;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCreateToolSpecificationsArguments")
    void assertCreateToolSpecificationsWithSchema(final String name, final String toolName,
                                                  final String expectedTitle, final String fieldName, final boolean expectedRequired, final Map<String, Object> expectedProperty) {
        MCPToolSpecificationFactory factory = createFactory();
        List<SyncToolSpecification> actual = factory.createToolSpecifications();
        assertThat(actual.size(), is(5));
        SyncToolSpecification actualSpecification = findToolSpecification(actual, toolName);
        assertThat(actualSpecification.tool().title(), is(expectedTitle));
        assertThat(actualSpecification.tool().description(), is("ShardingSphere MCP tool: " + toolName));
        assertThat(actualSpecification.tool().inputSchema().type(), is("object"));
        assertTrue(actualSpecification.tool().inputSchema().additionalProperties());
        assertThat(actualSpecification.tool().inputSchema().required().contains(fieldName), is(expectedRequired));
        assertThat(actualSpecification.tool().inputSchema().properties().get(fieldName), is(expectedProperty));
        assertNotNull(actualSpecification.callHandler());
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertCreateToolSpecificationsWithCallHandlerArguments")
    @SuppressWarnings("unchecked")
    void assertCreateToolSpecificationsWithCallHandler(final String name, final String toolName, final Map<String, Object> arguments, final boolean expectedIsError,
                                                       final String expectedPayloadKey, final Object expectedPayloadValue, final boolean expectedHasMessage, final String expectedMessage) {
        MCPToolSpecificationFactory factory = createFactory();
        List<SyncToolSpecification> actual = factory.createToolSpecifications();
        SyncToolSpecification actualSpecification = findToolSpecification(actual, "execute_query");
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.sessionId()).thenReturn("session-1");
        McpSchema.CallToolResult actualResult = actualSpecification.callHandler().apply(exchange, new McpSchema.CallToolRequest(toolName, arguments));
        assertThat(actualResult.isError(), is(expectedIsError));
        assertThat(actualResult.structuredContent(), isA(Map.class));
        Map<String, Object> actualPayload = (Map<String, Object>) actualResult.structuredContent();
        Object actualMessage = actualPayload.getOrDefault("message", "");
        assertThat(actualPayload.get(expectedPayloadKey), is(expectedPayloadValue));
        assertThat(actualPayload.containsKey("message"), is(expectedHasMessage));
        assertThat(actualMessage, is(expectedMessage));
        assertThat(actualResult.content().size(), is(1));
        assertThat(actualResult.content().get(0), isA(TextContent.class));
        TextContent actualContent = (TextContent) actualResult.content().get(0);
        assertThat(actualContent.text(), is(JsonUtils.toJsonString(actualPayload)));
    }
    
    private SyncToolSpecification findToolSpecification(final List<SyncToolSpecification> specifications, final String toolName) {
        return specifications.stream().filter(each -> toolName.equals(each.tool().name())).findFirst().orElseThrow(IllegalStateException::new);
    }
    
    private MCPToolSpecificationFactory createFactory() {
        String jdbcUrl = String.format("jdbc:h2:file:%s;MODE=MySQL;DB_CLOSE_DELAY=-1;DATABASE_TO_UPPER=false", tempDir.resolve("tool-specification").toAbsolutePath());
        MCPSessionManager sessionManager = new MCPSessionManager(Map.of("logic_db", new RuntimeDatabaseConfiguration("H2", jdbcUrl, "", "", "org.h2.Driver")));
        sessionManager.createSession("session-1");
        return new MCPToolSpecificationFactory(new MCPRuntimeContext(sessionManager, createMetadataCatalog()));
    }
    
    private MCPDatabaseMetadataCatalog createMetadataCatalog() {
        Map<String, MCPDatabaseMetadata> databaseMetadataMap = new LinkedHashMap<>(1, 1F);
        databaseMetadataMap.put("logic_db", new MCPDatabaseMetadata("logic_db", "H2", "", List.of(new MCPSchemaMetadata("logic_db", "public", List.of(), List.of(), List.of()))));
        return new MCPDatabaseMetadataCatalog(databaseMetadataMap);
    }
    
    private static Stream<Arguments> assertCreateToolSpecificationsArguments() {
        return Stream.of(
                Arguments.of("search metadata query field", "search_metadata", "Search Metadata", "query", true,
                        Map.of("type", "string", "description", "Search query.")),
                Arguments.of("search metadata object types field", "search_metadata", "Search Metadata", "object_types", false,
                        Map.of("type", "array", "description", "Optional object-type filter. Allowed values: database, schema, table, view, column, index, sequence.",
                                "items", Map.of("type", "string", "description", "Allowed values: database, schema, table, view, column, index, sequence."))),
                Arguments.of("execute query timeout field", "execute_query", "Execute Query", "timeout_ms", false,
                        Map.of("type", "integer", "description", "Optional timeout in milliseconds.")));
    }
    
    private static Stream<Arguments> assertCreateToolSpecificationsWithCallHandlerArguments() {
        return Stream.of(
                Arguments.of("execute query call", "execute_query", Map.of("database", "logic_db", "sql", "SELECT 1"), false, "result_kind", "result_set", false, ""),
                Arguments.of("search metadata with null arguments", "search_metadata", null, true, "error_code", "invalid_request", true, "query is required."),
                Arguments.of("unsupported tool call", "unsupported_tool", Collections.emptyMap(), true, "error_code", "invalid_request", true, "Unsupported tool."));
    }
}
