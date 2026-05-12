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

package org.apache.shardingsphere.mcp.core.handler;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPHandlerLoaderTest {
    
    @Test
    void assertLoadToolHandlers() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        List<MCPToolHandler<?>> toolHandlers = List.of(createToolHandler("search_metadata"), createToolHandler("plan_encrypt_rule"));
        when(provider.getToolHandlers()).thenReturn(toolHandlers);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(provider));
            List<String> actual = MCPHandlerLoader.loadToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("search_metadata", "plan_encrypt_rule")));
        }
    }
    
    @Test
    void assertLoadResourceHandlers() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        List<MCPResourceHandler<?>> resourceHandlers = List.of(createResourceHandler("shardingsphere://foo"));
        when(provider.getResourceHandlers()).thenReturn(resourceHandlers);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(provider));
            Collection<MCPResourceHandler<?>> actual = MCPHandlerLoader.loadResourceHandlers();
            assertThat(actual.stream().map(each -> each.getResourceDescriptor().getUriTemplate()).toList(), is(List.of("shardingsphere://foo")));
        }
    }
    
    @Test
    void assertCreateToolHandlersWithNullHandlers() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        when(provider.getToolHandlers()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPHandlerLoader.createToolHandlers(provider));
        assertThat(actual.getMessage(), is(String.format("Tool handlers are required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateToolHandlersWithNullHandler() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        List<MCPToolHandler<?>> toolHandlers = new ArrayList<>();
        toolHandlers.add(createToolHandler("search_metadata"));
        toolHandlers.add(null);
        when(provider.getToolHandlers()).thenReturn(toolHandlers);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPHandlerLoader.createToolHandlers(provider));
        assertThat(actual.getMessage(), is(String.format("Tool handler is required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceHandlersWithNullHandlers() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        when(provider.getResourceHandlers()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPHandlerLoader.createResourceHandlers(provider));
        assertThat(actual.getMessage(), is(String.format("Resource handlers are required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceHandlersWithNullHandler() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        List<MCPResourceHandler<?>> resourceHandlers = new ArrayList<>();
        resourceHandlers.add(createResourceHandler("shardingsphere://foo"));
        resourceHandlers.add(null);
        when(provider.getResourceHandlers()).thenReturn(resourceHandlers);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPHandlerLoader.createResourceHandlers(provider));
        assertThat(actual.getMessage(), is(String.format("Resource handler is required for `%s`.", provider.getClass().getName())));
    }
    
    private static MCPToolHandler<?> createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = createToolDescriptor(toolName);
        MCPToolHandler<?> result = mock(MCPToolHandler.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
    
    private static MCPToolDescriptor createToolDescriptor(final String toolName) {
        MCPToolDescriptor result = mock(MCPToolDescriptor.class);
        when(result.getName()).thenReturn(toolName);
        return result;
    }
    
    private static MCPResourceHandler<?> createResourceHandler(final String uriTemplate) {
        MCPResourceHandler<?> result = mock(MCPResourceHandler.class);
        when(result.getResourceDescriptor()).thenReturn(new MCPResourceDescriptor(uriTemplate, "foo", "Foo", "Read the fixture foo resource.", "application/json", Collections.emptyList(),
                MCPResourceAnnotations.EMPTY, null, null, null, Collections.emptyList(), Collections.emptyList(), Collections.emptyList(), Collections.emptyMap()));
        return result;
    }
}
