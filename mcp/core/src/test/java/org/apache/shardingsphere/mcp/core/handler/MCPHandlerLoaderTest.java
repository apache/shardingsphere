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
import org.apache.shardingsphere.mcp.api.MCPHandlerProvider;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceHandler;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.MCPToolHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPHandlerLoaderTest {
    
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
    
    private MCPResourceHandler<?> createResourceHandler(final String uriTemplate) {
        MCPResourceHandler<?> result = mock(MCPResourceHandler.class);
        when(result.getResourceDescriptor()).thenReturn(new MCPResourceDescriptor(uriTemplate, "foo", "Foo", "Read the fixture foo resource.", "application/json",
                MCPResourceAnnotations.EMPTY, Collections.emptyMap()));
        return result;
    }
    
    @Test
    void assertLoadToolHandlers() {
        MCPHandlerProvider provider = mock(MCPHandlerProvider.class);
        List<MCPToolHandler<?>> toolHandlers = List.of(createToolHandler("database_gateway_search_metadata"), createToolHandler("database_gateway_plan_encrypt_rule"));
        when(provider.getToolHandlers()).thenReturn(toolHandlers);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPHandlerProvider.class)).thenReturn(List.of(provider));
            List<String> actual = MCPHandlerLoader.loadToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("database_gateway_search_metadata", "database_gateway_plan_encrypt_rule")));
        }
    }
    
    private MCPToolHandler<?> createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = mock(MCPToolDescriptor.class);
        when(descriptor.getName()).thenReturn(toolName);
        MCPToolHandler<?> result = mock(MCPToolHandler.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
}
