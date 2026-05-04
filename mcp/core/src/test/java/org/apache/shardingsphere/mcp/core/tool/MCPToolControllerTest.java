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
import org.apache.shardingsphere.mcp.core.resource.ResourceTestDataFactory;
import org.apache.shardingsphere.mcp.core.tool.handler.ToolHandlerRegistry;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Map;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPToolControllerTest {
    
    @Test
    void assertHandle() {
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("items", 1);
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ToolHandlerRegistry> mocked = mockStatic(ToolHandlerRegistry.class)) {
            mocked.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-1"), eq("search_metadata"), eq(Map.of("query", "order"))))
                    .thenReturn(Optional.of(response));
            Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "order")).toPayload();
            assertThat(actual, is(payload));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedTool() {
        try (MockedStatic<ToolHandlerRegistry> mocked = mockStatic(ToolHandlerRegistry.class)) {
            mocked.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-1"), eq("unsupported_tool"), eq(Map.of()))).thenReturn(Optional.empty());
            Map<String, Object> actual = createController().handle("session-1", "unsupported_tool", Map.of()).toPayload();
            Map<?, ?> actualRecovery = (Map<?, ?>) actual.get("recovery");
            assertThat(actual.get("error_code"), is("invalid_request"));
            assertThat(actual.get("message"), is("Unsupported tool `unsupported_tool`."));
            assertThat(actualRecovery.get("category"), is("unsupported_tool"));
        }
    }
    
    @Test
    void assertHandleWithHandlerException() {
        try (MockedStatic<ToolHandlerRegistry> mocked = mockStatic(ToolHandlerRegistry.class)) {
            mocked.when(() -> ToolHandlerRegistry.dispatch(any(MCPRequestScope.class), eq("session-1"), eq("search_metadata"), eq(Map.of("query", "order"))))
                    .thenThrow(new MCPUnsupportedException("Search is not supported."));
            Map<String, Object> actual = createController().handle("session-1", "search_metadata", Map.of("query", "order")).toPayload();
            assertThat(actual.get("error_code"), is("unsupported"));
            assertThat(actual.get("message"), is("Search is not supported."));
        }
    }
    
    private MCPToolController createController() {
        return new MCPToolController(ResourceTestDataFactory.createRuntimeContext());
    }
}
