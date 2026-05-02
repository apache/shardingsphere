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

package org.apache.shardingsphere.mcp.resource;

import org.apache.shardingsphere.mcp.api.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.api.protocol.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandlerRegistry;
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

class MCPResourceControllerTest {
    
    @Test
    void assertHandle() {
        MCPResponse response = mock(MCPResponse.class);
        Map<String, Object> payload = Map.of("resource", "capabilities");
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ResourceHandlerRegistry> mocked = mockStatic(ResourceHandlerRegistry.class)) {
            mocked.when(() -> ResourceHandlerRegistry.dispatch(any(MCPFeatureContext.class), eq("shardingsphere://capabilities"))).thenReturn(Optional.of(response));
            Map<String, Object> actual = createController().handle("shardingsphere://capabilities").toPayload();
            assertThat(actual, is(payload));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedResourceUri() {
        try (MockedStatic<ResourceHandlerRegistry> mocked = mockStatic(ResourceHandlerRegistry.class)) {
            mocked.when(() -> ResourceHandlerRegistry.dispatch(any(MCPFeatureContext.class), eq("unsupported://resource"))).thenReturn(Optional.empty());
            Map<String, Object> actual = createController().handle("unsupported://resource").toPayload();
            assertThat(actual.get("error_code"), is("invalid_request"));
            assertThat(actual.get("message"), is("Unsupported resource URI."));
        }
    }
    
    @Test
    void assertHandleWithHandlerException() {
        try (MockedStatic<ResourceHandlerRegistry> mocked = mockStatic(ResourceHandlerRegistry.class)) {
            mocked.when(() -> ResourceHandlerRegistry.dispatch(any(MCPFeatureContext.class), eq("shardingsphere://indexes")))
                    .thenThrow(new MCPUnsupportedException("Index resources are not supported."));
            Map<String, Object> actual = createController().handle("shardingsphere://indexes").toPayload();
            assertThat(actual.get("error_code"), is("unsupported"));
            assertThat(actual.get("message"), is("Index resources are not supported."));
        }
    }
    
    private MCPResourceController createController() {
        return new MCPResourceController(ResourceTestDataFactory.createRuntimeContext());
    }
}
