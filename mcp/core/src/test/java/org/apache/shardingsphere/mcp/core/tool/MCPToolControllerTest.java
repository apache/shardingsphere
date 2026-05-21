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

package org.apache.shardingsphere.mcp.core.tool;

import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPToolControllerTest {
    
    @Test
    void assertHandle() {
        MCPToolDefinition toolDefinition = getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("items", 1);
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.findToolDefinition("database_gateway_search_metadata")).thenReturn(Optional.of(toolDefinition));
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-1"), eq(Map.of("query", "order"))))
                    .thenReturn(response);
            MCPResponse actual = createController().handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            assertThat(actual.toPayload(), is(payload));
        }
    }
    
    @Test
    void assertHandleWithToolDefinition() {
        MCPToolDefinition toolDefinition = getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("items", 1);
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-1"), eq(Map.of("query", "order"))))
                    .thenReturn(response);
            assertThat(createController().handle("session-1", toolDefinition, Map.of("query", "order")).toPayload(), is(payload));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedTool() {
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.findToolDefinition("unsupported_tool")).thenReturn(Optional.empty());
            UnsupportedToolException actual = assertThrows(UnsupportedToolException.class, () -> createController().handle("session-1", "unsupported_tool", Map.of()));
            assertThat(actual.getToolName(), is("unsupported_tool"));
            assertThat(actual.getMessage(), is("Unsupported tool `unsupported_tool`."));
        }
    }
    
    @Test
    void assertHandleWithHandlerException() {
        MCPToolDefinition toolDefinition = getToolDefinition("database_gateway_search_metadata");
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.findToolDefinition("database_gateway_search_metadata")).thenReturn(Optional.of(toolDefinition));
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-1"), eq(Map.of("query", "order"))))
                    .thenThrow(new MCPUnsupportedException("Search is not supported."));
            MCPResponse actual = createController().handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("message"), is("Search is not supported."));
        }
    }
    
    @Test
    void assertHandleWithToolCallLimitExceeded() {
        MCPToolDefinition toolDefinition = getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        when(response.toPayload()).thenReturn(Map.of("items", 1));
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.findToolDefinition("database_gateway_search_metadata")).thenReturn(Optional.of(toolDefinition));
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq("session-1"), eq(Map.of("query", "order"))))
                    .thenReturn(response);
            MCPToolController controller = createController(1);
            controller.handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            MCPResponse actual = controller.handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            Map<String, Object> actualPayload = actual.toPayload();
            Map<?, ?> actualRecovery = (Map<?, ?>) actualPayload.get("recovery");
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            assertThat(actualPayload.get("message"), is("MCP session exceeded the maximum tool call quota of 1."));
            assertThat(actualRecovery.get("category"), is("tool_call_limit_exceeded"));
        }
    }
    
    private MCPToolDefinition getToolDefinition(final String toolName) {
        return ToolDefinitionRegistry.findToolDefinition(toolName).orElseThrow();
    }
    
    private MCPToolController createController() {
        return new MCPToolController(ResourceTestDataFactory.createRuntimeContext());
    }
    
    private MCPToolController createController(final int maxToolCallsPerSession) {
        String previous = System.getProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        try {
            System.setProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, String.valueOf(maxToolCallsPerSession));
            return createController();
        } finally {
            resetMaxToolCallsPerSessionProperty(previous);
        }
    }
    
    private void resetMaxToolCallsPerSessionProperty(final String previous) {
        if (null == previous) {
            System.clearProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        } else {
            System.setProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, previous);
        }
    }
}
