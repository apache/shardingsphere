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

package org.apache.shardingsphere.mcp.feature;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPFeatureProviderRegistryTest {
    
    @Test
    void assertLoadToolHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        List<ToolHandler> toolHandlers = List.of(createToolHandler("search_metadata"), createToolHandler("plan_encrypt_rule"));
        when(featureProvider.getToolHandlers()).thenReturn(toolHandlers);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<String> actual = MCPFeatureProviderRegistry.loadToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("search_metadata", "plan_encrypt_rule")));
        }
    }
    
    @Test
    void assertLoadResourceHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        List<ResourceHandler> resourceHandlers = List.of(createResourceHandler("shardingsphere://foo"));
        when(featureProvider.getResourceHandlers()).thenReturn(resourceHandlers);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<ResourceHandler> actual = List.copyOf(MCPFeatureProviderRegistry.loadResourceHandlers());
            assertThat(actual.stream().map(ResourceHandler::getUriPattern).toList(), is(List.of("shardingsphere://foo")));
            assertThrows(UnsupportedOperationException.class, () -> MCPFeatureProviderRegistry.loadResourceHandlers().clear());
        }
    }
    
    @Test
    void assertCreateToolHandlersWithNullHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        when(featureProvider.getToolHandlers()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPFeatureProviderRegistry.createToolHandlers(featureProvider));
        assertThat(actual.getMessage(), is(String.format("Tool handlers are required for `%s`.", featureProvider.getClass().getName())));
    }
    
    @Test
    void assertCreateToolHandlersWithNullHandler() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        List<ToolHandler> toolHandlers = new ArrayList<>();
        toolHandlers.add(createToolHandler("search_metadata"));
        toolHandlers.add(null);
        when(featureProvider.getToolHandlers()).thenReturn(toolHandlers);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPFeatureProviderRegistry.createToolHandlers(featureProvider));
        assertThat(actual.getMessage(), is(String.format("Tool handler is required for `%s`.", featureProvider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceHandlersWithNullHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        when(featureProvider.getResourceHandlers()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPFeatureProviderRegistry.createResourceHandlers(featureProvider));
        assertThat(actual.getMessage(), is(String.format("Resource handlers are required for `%s`.", featureProvider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceHandlersWithNullHandler() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        List<ResourceHandler> resourceHandlers = new ArrayList<>();
        resourceHandlers.add(createResourceHandler("shardingsphere://foo"));
        resourceHandlers.add(null);
        when(featureProvider.getResourceHandlers()).thenReturn(resourceHandlers);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPFeatureProviderRegistry.createResourceHandlers(featureProvider));
        assertThat(actual.getMessage(), is(String.format("Resource handler is required for `%s`.", featureProvider.getClass().getName())));
    }
    
    private static ToolHandler createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = createToolDescriptor(toolName);
        ToolHandler result = mock(ToolHandler.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
    
    private static MCPToolDescriptor createToolDescriptor(final String toolName) {
        MCPToolDescriptor result = mock(MCPToolDescriptor.class);
        when(result.getName()).thenReturn(toolName);
        return result;
    }
    
    private static ResourceHandler createResourceHandler(final String uriPattern) {
        ResourceHandler result = mock(ResourceHandler.class);
        when(result.getUriPattern()).thenReturn(uriPattern);
        return result;
    }
}
