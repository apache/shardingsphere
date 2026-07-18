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

package org.apache.shardingsphere.mcp.core.resource;

import org.apache.shardingsphere.mcp.api.exception.MCPUnsupportedException;
import org.apache.shardingsphere.mcp.api.payload.MCPSuccessPayload;
import org.apache.shardingsphere.mcp.api.session.MCPSessionIdentity;
import org.apache.shardingsphere.mcp.core.context.MCPFeatureRuntimeRequestContext;
import org.apache.shardingsphere.mcp.core.context.MCPRuntimeContext;
import org.apache.shardingsphere.mcp.core.protocol.exception.UnsupportedResourceUriException;
import org.apache.shardingsphere.mcp.core.resource.handler.ResourceDefinitionRegistry;
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

class MCPResourceControllerTest {
    
    @Test
    void assertHandle() {
        MCPSuccessPayload response = mock(MCPSuccessPayload.class);
        Map<String, Object> payload = Map.of("resource", "capabilities");
        when(response.toPayload()).thenReturn(payload);
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("shardingsphere://capabilities"))).thenReturn(Optional.of(response));
            Map<String, Object> actual = createController().handle("session-1", "shardingsphere://capabilities").toPayload();
            assertThat(actual, is(payload));
        }
    }
    
    @Test
    void assertHandleWithUnsupportedResourceUri() {
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("unsupported://resource"))).thenReturn(Optional.empty());
            UnsupportedResourceUriException actual = assertThrows(UnsupportedResourceUriException.class, () -> createController().handle("session-1", "unsupported://resource"));
            assertThat(actual.getResourceUri(), is("unsupported://resource"));
        }
    }
    
    @Test
    void assertHandleWithHandlerException() {
        try (MockedStatic<ResourceDefinitionRegistry> mocked = mockStatic(ResourceDefinitionRegistry.class)) {
            mocked.when(() -> ResourceDefinitionRegistry.dispatch(any(MCPFeatureRuntimeRequestContext.class), eq("shardingsphere://indexes")))
                    .thenThrow(new MCPUnsupportedException("Index resources are not supported."));
            MCPUnsupportedException actual = assertThrows(MCPUnsupportedException.class, () -> createController().handle("session-1", "shardingsphere://indexes"));
            assertThat(actual.getMessage(), is("Index resources are not supported."));
        }
    }
    
    private MCPResourceController createController() {
        MCPRuntimeContext runtimeContext = ResourceTestDataFactory.createRuntimeContext();
        runtimeContext.getSessionManager().createSession(new MCPSessionIdentity("session-1", "", "", Map.of()));
        return new MCPResourceController(runtimeContext);
    }
}
