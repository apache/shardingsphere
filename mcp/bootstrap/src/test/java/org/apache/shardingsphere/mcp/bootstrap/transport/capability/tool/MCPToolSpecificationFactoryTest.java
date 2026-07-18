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

package org.apache.shardingsphere.mcp.bootstrap.transport.capability.tool;

import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema.ErrorCodes;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import org.apache.shardingsphere.mcp.api.exception.MCPInvalidRequestException;
import org.apache.shardingsphere.mcp.api.transport.MCPTransportType;
import org.apache.shardingsphere.mcp.api.tool.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.database.exception.DatabaseCapabilityNotFoundException;
import org.apache.shardingsphere.mcp.support.protocol.payload.MCPMapPayload;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest extends AbstractMCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecifications() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.getFirst().tool().name(), is("database_gateway_search_metadata"));
            assertThat(actual.getFirst().tool().title(), is("Search Metadata"));
            assertThat(actual.getFirst().tool().description(), is("Search database metadata."));
            assertThat(actual.getFirst().tool().inputSchema().type(), is("object"));
            assertThat(actual.getFirst().tool().inputSchema().required(), is(List.of("query")));
            assertFalse(actual.getFirst().tool().inputSchema().additionalProperties());
            assertThat(actual.getFirst().tool().inputSchema().properties().get("query"), is(Map.of("type", "string", "description", "Search query.")));
            assertThat(actual.getFirst().tool().inputSchema().properties().get("object_types"), is(Map.of(
                    "type", "array",
                    "description", "Optional object-type filter.",
                    "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW")))));
            assertThat(actual.getFirst().tool().outputSchema(), is(Map.of("type", "object")));
            assertTrue(actual.getFirst().tool().annotations().readOnlyHint());
            assertNull(actual.getFirst().tool().annotations().returnDirect());
            assertThat(actual.getFirst().tool().meta(), is(Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://databases"))));
            assertNotNull(actual.getFirst().callHandler());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsMapAnnotationPresence() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor(
                    "fixture_declared_defaults", MCPToolAnnotations.builder()
                            .title(null).readOnlyHint(false).destructiveHint(true).idempotentHint(false).openWorldHint(true).build())));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertNotNull(actual.getFirst().tool().annotations());
            assertFalse(actual.getFirst().tool().annotations().readOnlyHint());
            assertTrue(actual.getFirst().tool().annotations().destructiveHint());
            assertFalse(actual.getFirst().tool().annotations().idempotentHint());
            assertTrue(actual.getFirst().tool().annotations().openWorldHint());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleToolDefinitionDescriptor() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("fixture_ping"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapPayload(Map.of("status", "ok")));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), createExchange(), "fixture_ping", Map.of());
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(actual.content().getFirst().type(), is("text"));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleUnsupportedToolAsProtocolError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenThrow(UnsupportedToolException.class);
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = createToolSpecification(runtimeContext);
            McpSyncServerExchange exchange = createExchange();
            McpError actual = assertThrows(McpError.class, () -> callTool(actualSpecification, exchange, "database_gateway_search_metadata", Map.of()));
            assertThat(actual.getJsonRpcError().code(), is(ErrorCodes.INVALID_PARAMS));
            assertThat(actual.getJsonRpcError().message(), is("Unsupported tool `database_gateway_search_metadata`."));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
            assertThat(actualData.get("summary"), is("Unsupported tool `database_gateway_search_metadata`."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleExecutionError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptorWithoutOutputSchema("fixture_ping"));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(Map.of())))
                    .thenThrow(new DatabaseCapabilityNotFoundException());
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), createExchange(), "fixture_ping", Map.of());
            assertTrue(actual.isError());
            assertNull(actual.structuredContent());
            assertThat(getTextContentPayload(actual).get("summary"), is("Database capability does not exist."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleBlankExecutionError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptorWithoutOutputSchema("fixture_ping"));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(Map.of())))
                    .thenThrow(new MCPInvalidRequestException(" "));
            CallToolResult actual = callTool(createToolSpecification(MCPTransportType.STDIO), createExchange(), "fixture_ping", Map.of());
            assertTrue(actual.isError());
            assertThat(getTextContentPayload(actual).get("summary"), is("Invalid request."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSanitizeUnexpectedError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptorWithoutOutputSchema("fixture_ping"));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq(toolDefinition), eq(Map.of())))
                    .thenThrow(new IllegalStateException("sensitive detail"));
            McpError actual = assertThrows(McpError.class, () -> callTool(createToolSpecification(MCPTransportType.STDIO), createExchange(), "fixture_ping", Map.of()));
            assertThat(actual.getJsonRpcError().code(), is(ErrorCodes.INTERNAL_ERROR));
            assertThat(actual.getJsonRpcError().message(), is("Service is temporarily unavailable."));
            assertFalse(String.valueOf(actual.getJsonRpcError().data()).contains("sensitive detail"));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsRejectInvalidInputSchema() {
        SyncToolSpecification actualSpecification = findToolSpecification(
                new MCPToolSpecificationFactory(createRuntimeContext(MCPTransportType.HTTP)).createToolSpecifications(), "database_gateway_search_metadata");
        CallToolResult actual = callTool(actualSpecification, createExchange(), "database_gateway_search_metadata", Map.of("query", "order", "object_types", List.of("TABLE")));
        Map<String, Object> actualPayload = getTextContentPayload(actual);
        Map<?, ?> actualRecovery = (Map<?, ?>) actualPayload.get("recovery");
        assertThat(actualPayload.get("summary"), is("object_types[0] must be one of [database, schema, table, view, column, index, storage_unit, sequence]."));
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("field"), is("object_types[0]"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("database", "schema", "table", "view", "column", "index", "storage_unit", "sequence")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("query", "order")));
        assertNull(actual.structuredContent());
        assertTrue(actual.isError());
    }
    
    @Test
    void assertCreateToolSpecificationsValidateStructuredOutput() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createStrictToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapPayload(Map.of("count", 1)));
            CallToolResult actual = callTool(createToolSpecification(createRuntimeContext(MCPTransportType.HTTP)), createExchange(), "database_gateway_search_metadata", Map.of());
            Map<String, Object> actualPayload = getTextContentPayload(actual);
            assertTrue(String.valueOf(actualPayload.get("summary")).contains("database_gateway_search_metadata"));
            assertNull(actual.structuredContent());
            assertTrue(actual.isError());
        }
    }
}
