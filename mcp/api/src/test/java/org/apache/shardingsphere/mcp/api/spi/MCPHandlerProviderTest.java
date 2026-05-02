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

package org.apache.shardingsphere.mcp.api.spi;

import org.apache.shardingsphere.infra.spi.ShardingSphereSPI;
import org.apache.shardingsphere.mcp.api.context.MCPFeatureContext;
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.ResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.handler.ToolHandler;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class MCPHandlerProviderTest {
    
    @Test
    void assertGetToolHandlers() {
        ToolHandler toolHandler = new FixtureToolHandler();
        MCPHandlerProvider provider = new FixtureMCPHandlerProvider(List.of(toolHandler), List.of());
        assertThat(provider, isA(ShardingSphereSPI.class));
        assertThat(provider.getToolHandlers(), is(List.of(toolHandler)));
    }
    
    @Test
    void assertGetResourceHandlers() {
        ResourceHandler resourceHandler = new FixtureResourceHandler();
        MCPHandlerProvider provider = new FixtureMCPHandlerProvider(List.of(), List.of(resourceHandler));
        assertThat(provider, isA(ShardingSphereSPI.class));
        assertThat(provider.getResourceHandlers(), is(List.of(resourceHandler)));
    }
    
    private static final class FixtureMCPHandlerProvider implements MCPHandlerProvider {
        
        private final Collection<ToolHandler> toolHandlers;
        
        private final Collection<ResourceHandler> resourceHandlers;
        
        private FixtureMCPHandlerProvider(final Collection<ToolHandler> toolHandlers, final Collection<ResourceHandler> resourceHandlers) {
            this.toolHandlers = toolHandlers;
            this.resourceHandlers = resourceHandlers;
        }
        
        @Override
        public Collection<ToolHandler> getToolHandlers() {
            return toolHandlers;
        }
        
        @Override
        public Collection<ResourceHandler> getResourceHandlers() {
            return resourceHandlers;
        }
    }
    
    private static final class FixtureToolHandler implements ToolHandler {
        
        @Override
        public MCPToolDescriptor getToolDescriptor() {
            return new MCPToolDescriptor("foo_tool", List.of());
        }
        
        @Override
        public MCPResponse handle(final MCPFeatureContext requestContext, final String sessionId, final Map<String, Object> arguments) {
            return () -> Map.of("foo_key", "foo_value");
        }
    }
    
    private static final class FixtureResourceHandler implements ResourceHandler {
        
        @Override
        public String getUriPattern() {
            return "shardingsphere://foo";
        }
        
        @Override
        public MCPResponse handle(final MCPFeatureContext requestContext, final MCPUriVariables uriVariables) {
            return () -> Map.of("foo_key", "foo_value");
        }
    }
}
