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
import io.modelcontextprotocol.spec.McpSchema.CallToolRequest;
import io.modelcontextprotocol.spec.McpSchema.CallToolResult;
import io.modelcontextprotocol.spec.McpSchema.TextContent;
import io.modelcontextprotocol.spec.McpSchema;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("search_metadata")));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0).tool().name(), is("search_metadata"));
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
            assertThat(actual.get(0).tool().meta(), is(Map.of("relatedResources", List.of("shardingsphere://databases"))));
            assertNotNull(actual.get(0).callHandler());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = Map.of("status", "ok");
            MCPResponse response = () -> expectedPayload;
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("search_metadata"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("search_metadata", null));
            assertThat(actual.structuredContent(), is(expectedPayload));
            assertThat(((TextContent) actual.content().get(0)).text(), is("{\"status\":\"ok\"}"));
            assertFalse(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleErrorResponse() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("search_metadata")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("search_metadata"), eq(Map.of("query", "foo_query"))))
                    .thenReturn(Optional.of(new MCPErrorResponse("invalid_request", "")));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("search_metadata", Map.of("query", "foo_query")));
            @SuppressWarnings("unchecked")
            Map<String, Object> actualPayload = (Map<String, Object>) actual.structuredContent();
            assertThat(actualPayload.get("response_mode"), is("recovery"));
            assertThat(actualPayload.get("error_code"), is("invalid_request"));
            assertThat(actualPayload.get("message"), is(""));
            assertTrue(actual.isError());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleInteractiveElicitation() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> clarifyingPayload = createClarifyingPayload();
            Map<String, Object> expectedPayload = Map.of("status", "planned");
            MCPResponse clarifyingResponse = () -> clarifyingPayload;
            MCPResponse plannedResponse = () -> expectedPayload;
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("plan_encrypt_rule")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("plan_encrypt_rule"), any()))
                    .thenReturn(Optional.of(clarifyingResponse), Optional.of(plannedResponse));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(McpSchema.ElicitResult.Action.ACCEPT,
                    Map.of("primary_algorithm_properties.aes-key-value", "foo_secret", "requires_like_query", true)));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("plan_encrypt_rule", Map.of()));
            assertThat(actual.structuredContent(), is(expectedPayload));
            verify(exchange).createElicitation(any());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsFallbackWithoutElicitation() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = createClarifyingPayload();
            MCPResponse response = () -> expectedPayload;
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("plan_encrypt_rule")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("plan_encrypt_rule"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("plan_encrypt_rule", Map.of()));
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
            MCPResponse response = () -> expectedPayload;
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor("plan_encrypt_rule")));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("plan_encrypt_rule"), eq(Map.of())))
                    .thenReturn(Optional.of(response));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = createElicitationExchange(new McpSchema.ElicitResult(action, Map.of()));
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("plan_encrypt_rule", Map.of()));
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
    
    private Map<String, Object> createClarifyingPayload() {
        return Map.of(
                "plan_id", "plan-1",
                "status", "clarifying",
                "clarification_questions", List.of(
                        Map.of("field", "primary_algorithm_properties.aes-key-value", "input_type", "secret", "secret", true, "display_message", "Provide AES key."),
                        Map.of("field", "requires_like_query", "input_type", "boolean", "secret", false, "display_message", "Enable LIKE query?")));
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName) {
        return new MCPToolDescriptor(toolName, "Search Metadata", "Search database metadata.", List.of(
                new MCPToolFieldDefinition("query", new MCPToolValueDefinition(Type.STRING, "Search query.", null), true),
                new MCPToolFieldDefinition("object_types", new MCPToolValueDefinition(Type.ARRAY, "Optional object-type filter.",
                        new MCPToolValueDefinition(Type.STRING, "Object type.", null, List.of("TABLE", "VIEW"))), false)),
                Map.of("type", "object"), new MCPToolAnnotations("Search Metadata", true, false, true, true, false),
                Map.of("relatedResources", List.of("shardingsphere://databases")));
    }
}
