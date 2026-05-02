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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolValueDefinition.Type;
import org.apache.shardingsphere.mcp.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.protocol.response.MCPErrorResponse;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandlerRegistry;
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
import static org.mockito.Mockito.when;

class MCPToolSpecificationFactoryTest {
    
    @Test
    void assertCreateToolSpecifications() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor()));
            MCPToolSpecificationFactory actualFactory = new MCPToolSpecificationFactory(mock(MCPRuntimeContext.class));
            List<SyncToolSpecification> actual = actualFactory.createToolSpecifications();
            assertThat(actual.size(), is(1));
            assertThat(actual.get(0).tool().name(), is("search_metadata"));
            assertThat(actual.get(0).tool().title(), is("Search Metadata"));
            assertThat(actual.get(0).tool().description(), is("Search database metadata."));
            assertThat(actual.get(0).tool().inputSchema().type(), is("object"));
            assertThat(actual.get(0).tool().inputSchema().required(), is(List.of("query")));
            assertThat(actual.get(0).tool().inputSchema().properties().get("query"), is(Map.of("type", "string", "description", "Search query.")));
            assertThat(actual.get(0).tool().inputSchema().properties().get("object_types"), is(Map.of(
                    "type", "array",
                    "description", "Optional object-type filter.",
                    "items", Map.of("type", "string", "description", "Object type.", "enum", List.of("TABLE", "VIEW")))));
            assertNotNull(actual.get(0).callHandler());
        }
    }
    
    @Test
    void assertCreateToolSpecificationsHandleNullArguments() {
        try (MockedStatic<ToolHandlerRegistry> mockedToolHandlerRegistry = mockStatic(ToolHandlerRegistry.class)) {
            Map<String, Object> expectedPayload = Map.of("status", "ok");
            MCPResponse response = () -> expectedPayload;
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor()));
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
            mockedToolHandlerRegistry.when(ToolHandlerRegistry::getSupportedToolDescriptors).thenReturn(List.of(createToolDescriptor()));
            mockedToolHandlerRegistry.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-id"), eq("search_metadata"), eq(Map.of("query", "foo_query"))))
                    .thenReturn(Optional.of(new MCPErrorResponse("invalid_request", "")));
            MCPRuntimeContext runtimeContext = mock(MCPRuntimeContext.class, RETURNS_DEEP_STUBS);
            when(runtimeContext.getSessionManager().getTransactionResourceManager().getRuntimeDatabases()).thenReturn(Collections.emptyMap());
            SyncToolSpecification actualSpecification = new MCPToolSpecificationFactory(runtimeContext).createToolSpecifications().get(0);
            McpSyncServerExchange exchange = mock(McpSyncServerExchange.class);
            when(exchange.sessionId()).thenReturn("session-id");
            CallToolResult actual = actualSpecification.callHandler().apply(exchange, new CallToolRequest("search_metadata", Map.of("query", "foo_query")));
            assertThat(actual.structuredContent(), is(Map.of("error_code", "invalid_request", "message", "")));
            assertTrue(actual.isError());
        }
    }
    
    private MCPToolDescriptor createToolDescriptor() {
        return new MCPToolDescriptor("search_metadata", "Search Metadata", "Search database metadata.", List.of(
                new MCPToolFieldDefinition("query", new MCPToolValueDefinition(Type.STRING, "Search query.", null), true),
                new MCPToolFieldDefinition("object_types", new MCPToolValueDefinition(Type.ARRAY, "Optional object-type filter.",
                        new MCPToolValueDefinition(Type.STRING, "Object type.", null, List.of("TABLE", "VIEW"))), false)));
    }
}
