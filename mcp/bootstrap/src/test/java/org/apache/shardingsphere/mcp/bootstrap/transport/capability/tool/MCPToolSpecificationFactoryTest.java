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

import io.modelcontextprotocol.json.schema.JsonSchemaValidator;
import io.modelcontextprotocol.json.schema.JsonSchemaValidator.ValidationResponse;
import io.modelcontextprotocol.json.schema.jackson2.DefaultJsonSchemaValidator;
import io.modelcontextprotocol.server.McpServerFeatures.SyncToolSpecification;
import io.modelcontextprotocol.server.McpSyncServerExchange;
import io.modelcontextprotocol.spec.McpError;
import io.modelcontextprotocol.spec.McpSchema;
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.ResourceLink;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
            assertThat(actual.get(0).tool().name(), is("database_gateway_search_metadata"));
            assertThat(actual.get(0).tool().title(), is("Search Metadata"));
            assertThat(actual.get(0).tool().description(), is("Search database metadata."));
            assertThat(actual.get(0).tool().inputSchema().type(), is("object"));
            assertThat(actual.get(0).tool().inputSchema().required(), is(List.of("query")));
            assertFalse(actual.get(0).tool().inputSchema().additionalProperties());
            assertThat(actual.get(0).tool().inputSchema().properties().get("query"), is(Map.of("type", "string", "description", "Search query.")));
            assertThat(actual.get(0).tool().inputSchema().properties().get("object_types"), is(Map.of(
                    "type", "array",
                    "description", "Optional object-type filter.",
                    "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW")))));
            assertThat(actual.get(0).tool().outputSchema(), is(Map.of("type", "object")));
            assertTrue(actual.get(0).tool().annotations().readOnlyHint());
            assertNull(actual.get(0).tool().annotations().returnDirect());
            assertThat(actual.get(0).tool().meta(), is(Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://databases"))));
            assertNotNull(actual.get(0).callHandler());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsMapAnnotationPresence() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor(
                    "fixture_declared_defaults", new MCPToolAnnotations(null, false, true, false, true))));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertNotNull(actual.get(0).tool().annotations());
            assertFalse(actual.get(0).tool().annotations().readOnlyHint());
            assertTrue(actual.get(0).tool().annotations().destructiveHint());
            assertFalse(actual.get(0).tool().annotations().idempotentHint());
            assertTrue(actual.get(0).tool().annotations().openWorldHint());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPResponse response = new MCPMapResponse(Map.of("status", "ok"));
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), response);
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", null));
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().get(0)).text(), is("{\"status\":\"ok\"}"));
            assertFalse(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleErrorResponse() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of("query", "foo_query"), new MCPErrorResponse(""));
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", Map.of("query", "foo_query")));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
            assertThat(actualPayload.get("response_mode"), is("recovery"));
            assertThat(actualPayload.get("message"), is(""));
            assertTrue(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandlePlainPayload() {
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(Map.of("message", "invalid_request")));
        assertFalse(actual.isError());
    }
    
    @Test
    void assertCreateToolSpecificationsHandleResourceLinks() {
        Map<String, Object> payload = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "read_first", "Read logical database.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        ResourceLink actualLink = (ResourceLink) actual.content().get(1);
        assertThat(actualLink.uri(), is("shardingsphere://databases/logic_db"));
        assertThat(actualLink.title(), is("logical-database"));
        assertThat(actualLink.mimeType(), is("application/json"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleItemResourceLinks() {
        Map<String, Object> payload = Map.of("items", List.of(Map.of(
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db/tables/t_order", "table", "inspect_detail", "Read table.", "resource"),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_parent", "Read database.", "parent_resource"),
                "next_resources", List.of(MCPResourceHintUtils.create(
                        "shardingsphere://databases/logic_db/tables/t_order/columns", "column-list", "inspect_children", "Read columns.", "next_resources")))));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.content().size(), is(4));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://databases/logic_db/tables/t_order"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases/logic_db/tables/t_order/columns"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resource"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleRecoveryResourceLinks() {
        Map<String, Object> recovery = Map.of("resources_to_read", List.of(
                MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPErrorResponse("", recovery));
        assertTrue(actual.isError());
        assertThat(actual.content().get(1), isA(ResourceLink.class));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleBoundedResourceLinks() {
        Map<String, Object> payload = Map.of(
                "next_resources", createResourceHints("shardingsphere://databases/next_", "next_resources", 30),
                "parent_resource", MCPResourceHintUtils.create("shardingsphere://databases", "logical-database", "inspect_parent", "Read parent.", "parent_resource"),
                "resource", MCPResourceHintUtils.create("shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read detail.", "resource"),
                "resources_to_read", List.of(MCPResourceHintUtils.create("shardingsphere://capabilities", "capability", "read_first", "Read capabilities.", "resources_to_read")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.structuredContent(), is(payload));
        assertThat(actual.content().size(), is(25));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_EMITTED), is(24));
        assertThat(actual.meta().get(MCPShardingSphereMetadataKeys.RESOURCE_LINKS_OMITTED), is(9));
        assertThat(((ResourceLink) actual.content().get(1)).uri(), is("shardingsphere://capabilities"));
        assertThat(((ResourceLink) actual.content().get(2)).uri(), is("shardingsphere://databases/logic_db"));
        assertThat(((ResourceLink) actual.content().get(3)).uri(), is("shardingsphere://databases"));
        assertThat(((Map<?, ?>) actual.content().get(1).meta()).get(MCPShardingSphereMetadataKeys.SOURCE_FIELD), is("resources_to_read"));
    }
    
    @Test
    void assertCreateToolSpecificationsIgnoreRawUriLink() {
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(Map.of("resource_uri", "shardingsphere://databases/logic_db")));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsIgnoreArbitraryNestedResourceHint() {
        Map<String, Object> payload = Map.of("debug", Map.of("resource", MCPResourceHintUtils.create(
                "shardingsphere://databases/logic_db", "logical-database", "inspect_detail", "Read logical database.", "resource")));
        CallToolResult actual = createCallToolResult("fixture_ping", new MCPMapResponse(payload));
        assertThat(actual.content().size(), is(1));
    }
    
    @Test
    void assertCreateToolSpecificationsHandleToolDefinitionDescriptor() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createToolDescriptor("fixture_ping"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapResponse(Map.of("status", "ok")));
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(createRuntimeContext("stdio")).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("fixture_ping", Map.of()));
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().get(0)).text(), is("{\"status\":\"ok\"}"));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleUnsupportedToolAsProtocolError() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            mockedToolDefinitionRegistry.when(ToolDefinitionRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolDefinitionRegistry.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenThrow(UnsupportedToolException.class);
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            McpError actual = assertThrows(McpError.class, () -> actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", Map.of())));
            assertThat(actual.getJsonRpcError().code(), is(McpSchema.ErrorCodes.INVALID_PARAMS));
            assertThat(actual.getJsonRpcError().message(), is("Unsupported tool `database_gateway_search_metadata`."));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualData = (Map<String, Object>) actual.getJsonRpcError().data();
            assertThat(actualData.get("message"), is("Unsupported tool `database_gateway_search_metadata`."));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsRejectInvalidInputSchema() {
        MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
        when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
        SyncToolSpecification actualSpecification = findToolSpecification(new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications(), "database_gateway_search_metadata");
        McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
        when(exchange.sessionId()).thenReturn("session-id");
        CallToolResult actual = actualSpecification.callHandler().apply(exchange,
                new CallToolRequest("database_gateway_search_metadata", Map.of("query", "order", "object_types", List.of("TABLE"))));
        @SuppressWarnings("unchecked")
        Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
        Map<?, ?> actualRecovery = (Map<?, ?>) actualPayload.get("recovery");
        assertThat(actualPayload.get("message"), is("object_types[0] must be one of [database, schema, table, view, column, index, sequence]."));
        assertThat(actualRecovery.get("category"), is("invalid_enum_value"));
        assertThat(actualRecovery.get("field"), is("object_types[0]"));
        assertThat(actualRecovery.get("allowed_values"), is(List.of("database", "schema", "table", "view", "column", "index", "sequence")));
        assertThat(actualRecovery.get("suggested_arguments"), is(Map.of("query", "order")));
        assertTrue(actual.isError());
    }
    
    @Test
    void assertCreateToolSpecificationsValidateStructuredOutput() {
        try (MockedStatic<ToolDefinitionRegistry> mockedToolDefinitionRegistry = mockStatic(ToolDefinitionRegistry.class)) {
            MCPToolDefinition toolDefinition = mockSupportedTool(mockedToolDefinitionRegistry, createStrictToolDescriptor("database_gateway_search_metadata"));
            mockToolDispatch(mockedToolDefinitionRegistry, toolDefinition, Map.of(), new MCPMapResponse(Map.of("count", 1)));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", Map.of()));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
            assertTrue(String.valueOf(actualPayload.get("message")).contains("database_gateway_search_metadata"));
            assertTrue(actual.isError());
        }
    }
    
    @Test
    void assertToolOutputSchemaExamplesMatchSchemas() {
        JsonSchemaValidator validator = new DefaultJsonSchemaValidator();
        for (MCPToolDescriptor each : ToolDefinitionRegistry.getSupportedToolDescriptors()) {
            for (Map<String, Object> example : getOutputSchemaExamples(each)) {
                ValidationResponse actual = validator.validate(each.getOutputSchema(), example);
                assertTrue(actual.valid(), () -> String.format("Invalid outputSchema example for `%s`: %s", each.getName(), actual.errorMessage()));
            }
        }
    }
    
    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getOutputSchemaExamples(final MCPToolDescriptor toolDescriptor) {
        return toolDescriptor.getOutputSchema().containsKey("examples") ? (List<Map<String, Object>>) toolDescriptor.getOutputSchema().get("examples") : List.of();
    }
    
}
