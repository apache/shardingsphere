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
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.apache.shardingsphere.mcp.support.descriptor.MCPShardingSphereMetadataKeys;
import org.apache.shardingsphere.mcp.support.protocol.MCPResourceHintUtils;
import org.apache.shardingsphere.mcp.support.protocol.response.MCPMapResponse;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecifications() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
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
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor(
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
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            MCPResponse response = new MCPMapResponse(Map.of("status", "ok"));
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_search_metadata"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
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
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_search_metadata"), eq(Map.of("query", "foo_query"))))
                    .thenReturn(Optional.of(new MCPErrorResponse("")));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
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
    void assertCreateToolSpecificationsHandleResponseWithoutDescriptor() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("fixture_ping"), eq(Map.of())))
                    .thenReturn(Optional.of(new MCPMapResponse(Map.of("status", "ok"))));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("fixture_ping", Map.of()));
            assertThat(actual.structuredContent(), is(Map.of("status", "ok")));
            assertThat(((TextContent) actual.content().get(0)).text(), is("{\"status\":\"ok\"}"));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleUnsupportedToolAsProtocolError() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_search_metadata"), eq(Map.of())))
                    .thenReturn(Optional.empty());
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
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createStrictToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_search_metadata"), eq(Map.of())))
                    .thenReturn(Optional.of(new MCPMapResponse(Map.of("count", 1))));
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
        for (MCPToolDescriptor each : ToolHandlerRegistry.getSupportedToolDescriptors()) {
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
    
    @Test
    void assertCreateToolSpecificationsHandleInteractiveElicitation() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            MCPResponse clarifyingResponse = new MCPMapResponse(createClarifyingPayload());
            MCPResponse plannedResponse = new MCPMapResponse(Map.of("status", "planned"));
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createPlanningToolDescriptor(toolName)));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq(toolName), any()))
                    .thenReturn(Optional.of(clarifyingResponse), Optional.of(plannedResponse));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("custom_properties.display-name", "foo_display", "requires_review", true)));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
            assertThat(actual.structuredContent(), is(Map.of("status", "planned")));
            ArgumentCaptor<McpSchema.ElicitRequest> requestCaptor = ArgumentCaptor.forClass(McpSchema.ElicitRequest.class);
            verify(exchange).createElicitation(requestCaptor.capture());
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.TOOL), is(toolName));
            assertThat(requestCaptor.getValue().meta().get(MCPShardingSphereMetadataKeys.PLAN_ID), is("plan-1"));
            assertThat(requestCaptor.getValue().requestedSchema(), is(createExpectedElicitRequestedSchema()));
            mockedToolHandlerRegistry.verify(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq(toolName), eq(createElicitedArguments())));
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSecretQuestion() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("custom_properties.display-name", "string", true, "Provide display name."));
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSecretInputType() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("custom_properties.display-name", "secret", false, "Provide display name."));
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithSensitiveFieldName() {
        assertCreateToolSpecificationsSkipUnsafeElicitation(createClarifyingQuestion("primary_algorithm_properties.access-token", "string", false, "Provide access token."));
    }
    
    private void assertCreateToolSpecificationsSkipUnsafeElicitation(final Map<String, Object> question) {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            String toolName = "database_gateway_plan_encrypt_rule";
            Map<String, Object> expectedPayload = createClarifyingPayload(question);
            MCPResponse response = new MCPMapResponse(expectedPayload);
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createPlanningToolDescriptor(toolName)));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq(toolName), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of("custom_properties.display-name", "foo_display")));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationForNonPlanningTool() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("database_gateway_search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_search_metadata"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_search_metadata", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsSkipElicitationWithoutRuntimeDescriptor() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("fixture_ping")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("fixture_ping"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("fixture_ping", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitation() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_plan_encrypt_rule"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange, never()).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationDeclined() {
        assertCreateToolSpecificationsFallbackWhenElicitationAction(McpSchema.ElicitResult.Action.DECLINE);
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWhenElicitationCancelled() {
        assertCreateToolSpecificationsFallbackWhenElicitationAction(McpSchema.ElicitResult.Action.CANCEL);
    }
    
    private void assertCreateToolSpecificationsFallbackWhenElicitationAction(final McpSchema.ElicitResult.Action action) {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = new MCPMapResponse(expectedPayload);
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createPlanningToolDescriptor("database_gateway_plan_encrypt_rule")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("database_gateway_plan_encrypt_rule"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(action, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("database_gateway_plan_encrypt_rule", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange).createElicitation(any());
        }
    }
    
    private McpSyncServerExchange createElicitationExchange(final McpSchema.ElicitResult elicitationResult) {
        McpSyncServerExchange result = mock(McpSyncServerExchange.class);
        when(result.sessionId()).thenReturn("session-id");
        when(result.getClientCapabilities()).thenReturn(McpSchema.ClientCapabilities.builder().elicitation().build());
        when(result.createElicitation(any())).thenReturn(elicitationResult);
        return result;
    }
    
    private SyncToolSpecification findToolSpecification(final List<SyncToolSpecification> specifications, final String toolName) {
        return specifications.stream().filter(each -> toolName.equals(each.tool().name())).findFirst().orElseThrow();
    }
    
    private CallToolResult createCallToolResult(final String toolName, final MCPResponse response) {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptorWithoutOutputSchema(toolName)));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq(toolName), eq(Map.of()))).thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            return actualSpecification.callHandler().apply(exchange, new CallToolRequest(toolName, Map.of()));
        }
    }
    
    private Map<String, Object> createClarifyingPayload() {
        return Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(
                        createClarifyingQuestion("custom_properties.display-name", "string", false, "Provide display name."),
                        createClarifyingQuestion("requires_review", "boolean", false, "Require review?")));
    }
    
    private Map<String, Object> createClarifyingPayload(final Map<String, Object> question) {
        return Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(question));
    }
    
    private Map<String, Object> createClarifyingQuestion(final String field, final String inputType, final boolean secret, final String displayMessage) {
        return Map.of("field", field, "input_type", inputType, "secret", secret, "display_message", displayMessage);
    }
    
    private Map<String, Object> createElicitedArguments() {
        return Map.of(
                "plan_id", "plan-1",
                "custom_properties", Map.of("display-name", "foo_display"),
                "intent", Map.of("requires_review", true));
    }
    
    private Map<String, Object> createExpectedElicitRequestedSchema() {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("custom_properties.display-name", Map.of("type", "string", "description", "Provide display name."));
        properties.put("requires_review", Map.of("type", "boolean", "description", "Require review?"));
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", List.of("custom_properties.display-name", "requires_review"));
        result.put("additionalProperties", false);
        return result;
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName) {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("query", Map.of("type", "string", "description", "Search query."));
        properties.put("object_types", Map.of("type", "array", "description", "Optional object-type filter.",
                "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW"))));
        return new MCPToolDescriptor(toolName, "Search Metadata", "Search database metadata.", createInputSchema(properties, List.of("query")),
                Map.of("type", "object"), new MCPToolAnnotations("Search Metadata", true, false, true, true),
                Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://databases")));
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName, final MCPToolAnnotations annotations) {
        return new MCPToolDescriptor(toolName, "Fixture Tool", "Run a fixture tool.", createInputSchema(Map.of(), List.of()), Map.of("type", "object"), annotations, Collections.emptyMap());
    }
    
    private MCPToolDescriptor createToolDescriptorWithoutOutputSchema(final String toolName) {
        return new MCPToolDescriptor(toolName, "Fixture Tool", "Run a fixture tool.", createInputSchema(Map.of(), List.of()), Collections.emptyMap(),
                new MCPToolAnnotations("Fixture Tool", true, false, true, true), Collections.emptyMap());
    }
    
    private MCPToolDescriptor createStrictToolDescriptor(final String toolName) {
        return new MCPToolDescriptor(toolName, "Search Metadata", "Search database metadata.", createInputSchema(Map.of(), List.of()),
                Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string")), "required", List.of("status")),
                new MCPToolAnnotations("Search Metadata", true, false, true, true), Collections.emptyMap());
    }
    
    private MCPToolDescriptor createPlanningToolDescriptor(final String toolName) {
        Map<String, Object> properties = new LinkedHashMap<>(2, 1F);
        properties.put("custom_properties", Map.of("type", "object", "description", "Custom properties.", "additionalProperties", true));
        properties.put("intent", Map.of("type", "object", "description", "Intent.", "properties",
                Map.of("requires_review", Map.of("type", "boolean", "description", "Requires review.")), "required", List.of(), "additionalProperties", false));
        return new MCPToolDescriptor(toolName, "Plan Custom Rule", "Plan a custom rule.", createInputSchema(properties, List.of()),
                Map.of("type", "object"), new MCPToolAnnotations("Plan Custom Rule", false, false, true, true), Collections.emptyMap());
    }
    
    private Map<String, Object> createInputSchema(final Map<String, Object> properties, final List<String> required) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("type", "object");
        result.put("properties", properties);
        result.put("required", required);
        result.put("additionalProperties", false);
        return result;
    }
    
    private List<Map<String, Object>> createResourceHints(final String uriPrefix, final String sourceField, final int count) {
        List<Map<String, Object>> result = new LinkedList<>();
        for (int i = 0; i < count; i++) {
            result.add(MCPResourceHintUtils.create(uriPrefix + i, "logical-database", "inspect_detail", "Read resource.", sourceField));
        }
        return result;
    }
}
