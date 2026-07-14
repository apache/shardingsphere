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

import org.apache.shardingsphere.mcp.api.MCPRequestContext;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.core.context.MCPRequestScope;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedToolException;
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.session.MCPSessionExecutionCoordinator;
import org.apache.shardingsphere.mcp.core.tool.handler.MCPToolDefinition;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolDefinitionRegistry;
import org.apache.shardingsphere.mcp.support.security.MCPClientSafetyPolicy;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPToolControllerTest {
    
    @Test
    void assertHandle() {
        MCPToolDefinition toolDefinition = ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("items", 1);
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenReturn(toolDefinition);
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq(Map.of("query", "order"))))
                    .thenReturn(response);
            MCPResponse actual = new MCPToolController(createRuntimeContext()).handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            assertThat(actual.toPayload(), is(payload));
        }
    }
    
    @Test
    void assertHandleWithToolDefinition() {
        MCPToolDefinition toolDefinition = ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("items", 1);
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq(Map.of("query", "order"))))
                    .thenReturn(response);
            assertThat(new MCPToolController(createRuntimeContext()).handle("session-1", toolDefinition, Map.of("query", "order")).toPayload(), is(payload));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedTool() {
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.getToolDefinition("unsupported_tool")).thenThrow(new UnsupportedToolException("unsupported_tool"));
            UnsupportedToolException actual =
                    assertThrows(UnsupportedToolException.class, () -> new MCPToolController(createRuntimeContext()).handle("session-1", "unsupported_tool", Map.of()));
            assertThat(actual.getToolName(), is("unsupported_tool"));
            assertThat(actual.getMessage(), is("Unsupported tool `unsupported_tool`."));
        }
    }
    
    @Test
    void assertHandleWithHandlerException() {
        MCPToolDefinition toolDefinition = ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata");
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenReturn(toolDefinition);
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq(Map.of("query", "order"))))
                    .thenThrow(new MCPUnsupportedException("Search is not supported."));
            MCPResponse actual = new MCPToolController(createRuntimeContext()).handle("session-1", "database_gateway_search_metadata", Map.of("query", "order"));
            assertThat(toolDefinition.getDescriptor().getName(), is("database_gateway_search_metadata"));
            Map<String, Object> actualPayload = actual.toPayload();
            assertThat(actualPayload.get("message"), is("Search is not supported."));
        }
    }
    
    @Test
    void assertHandleWithToolCallLimitExceeded() {
        MCPToolDefinition toolDefinition = ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata");
        MCPResponse response = mock(MCPResponse.class);
        when(response.toPayload()).thenReturn(Map.of("items", 1));
        try (MockedStatic<ToolDefinitionRegistry> mocked = mockStatic(ToolDefinitionRegistry.class)) {
            mocked.when(() -> ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata")).thenReturn(toolDefinition);
            mocked.when(() -> ToolDefinitionRegistry.dispatch(any(MCPRequestScope.class), eq(toolDefinition), eq(Map.of("query", "order"))))
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
    
    @Test
    void assertCloseWaitsForToolHandler() throws InterruptedException, ExecutionException {
        MCPRuntimeContext runtimeContext = createRuntimeContext();
        CountDownLatch handlerStarted = new CountDownLatch(1);
        CountDownLatch releaseHandler = new CountDownLatch(1);
        CountDownLatch closeAttempted = new CountDownLatch(1);
        CountDownLatch closeCompleted = new CountDownLatch(1);
        MCPToolHandler<MCPRequestContext> handler = mock(MCPToolHandler.class);
        when(handler.getContextType()).thenReturn(MCPRequestContext.class);
        when(handler.handle(any(), eq(Map.of()))).thenAnswer(invocation -> {
            handlerStarted.countDown();
            assertTrue(releaseHandler.await(1L, TimeUnit.SECONDS));
            return mock(MCPResponse.class);
        });
        MCPToolDefinition toolDefinition = new MCPToolDefinition(
                ToolDefinitionRegistry.getToolDefinition("database_gateway_search_metadata").getDescriptor(), handler);
        ExecutorService executorService = Executors.newFixedThreadPool(2);
        try {
            Future<MCPResponse> toolFuture = executorService.submit(() -> new MCPToolController(runtimeContext).handle("session-1", toolDefinition, Map.of()));
            assertTrue(handlerStarted.await(1L, TimeUnit.SECONDS));
            assertFalse(toolFuture.isDone());
            Future<?> closeFuture = executorService.submit(() -> {
                closeAttempted.countDown();
                new MCPSessionExecutionCoordinator(runtimeContext.getSessionManager()).closeSession("session-1");
                closeCompleted.countDown();
            });
            assertFalse(closeFuture.isDone());
            assertTrue(closeAttempted.await(1L, TimeUnit.SECONDS));
            assertFalse(closeCompleted.await(200L, TimeUnit.MILLISECONDS));
            releaseHandler.countDown();
            toolFuture.get();
            closeFuture.get();
            assertFalse(runtimeContext.getSessionManager().hasSession("session-1"));
        } finally {
            releaseHandler.countDown();
            executorService.shutdownNow();
        }
    }
    
    private MCPToolController createController(final int maxToolCallsPerSession) {
        String previous = System.getProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY);
        try {
            System.setProperty(MCPClientSafetyPolicy.MAX_TOOL_CALLS_PER_SESSION_PROPERTY, String.valueOf(maxToolCallsPerSession));
            return new MCPToolController(createRuntimeContext());
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
    
    private MCPRuntimeContext createRuntimeContext() {
        MCPRuntimeContext result = ResourceTestDataFactory.createRuntimeContext();
        result.getSessionManager().createSession("session-1");
        return result;
    }
}
