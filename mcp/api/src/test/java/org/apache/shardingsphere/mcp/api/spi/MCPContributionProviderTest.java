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
import org.apache.shardingsphere.mcp.api.protocol.response.MCPResponse;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceContribution;
import org.apache.shardingsphere.mcp.api.resource.MCPUriVariables;
import org.apache.shardingsphere.mcp.api.resource.handler.ServerResourceHandler;
import org.apache.shardingsphere.mcp.api.tool.MCPToolCall;
import org.apache.shardingsphere.mcp.api.tool.MCPToolContribution;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.handler.ServerToolHandler;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;

class MCPContributionProviderTest {
    
    @Test
    void assertGetToolContributions() {
        MCPToolContribution toolContribution = new FixtureToolHandler();
        MCPContributionProvider provider = new FixtureMCPContributionProvider(List.of(toolContribution), List.of());
        assertThat(provider, isA(ShardingSphereSPI.class));
        assertThat(provider.getToolContributions(), is(List.of(toolContribution)));
    }
    
    @Test
    void assertGetResourceContributions() {
        MCPResourceContribution resourceContribution = new FixtureResourceHandler();
        MCPContributionProvider provider = new FixtureMCPContributionProvider(List.of(), List.of(resourceContribution));
        assertThat(provider, isA(ShardingSphereSPI.class));
        assertThat(provider.getResourceContributions(), is(List.of(resourceContribution)));
    }
    
    private static final class FixtureMCPContributionProvider implements MCPContributionProvider {
        
        private final Collection<MCPToolContribution> toolContributions;
        
        private final Collection<MCPResourceContribution> resourceContributions;
        
        private FixtureMCPContributionProvider(final Collection<MCPToolContribution> toolContributions, final Collection<MCPResourceContribution> resourceContributions) {
            this.toolContributions = toolContributions;
            this.resourceContributions = resourceContributions;
        }
        
        @Override
        public Collection<MCPToolContribution> getToolContributions() {
            return toolContributions;
        }
        
        @Override
        public Collection<MCPResourceContribution> getResourceContributions() {
            return resourceContributions;
        }
    }
    
    private static final class FixtureToolHandler implements ServerToolHandler {
        
        @Override
        public MCPToolDescriptor getToolDescriptor() {
            return new MCPToolDescriptor("foo_tool", "Foo Tool", "Foo tool.", List.of());
        }
        
        @Override
        public MCPResponse handle(final MCPToolCall toolCall) {
            return () -> Map.of("foo_key", "foo_value");
        }
    }
    
    private static final class FixtureResourceHandler implements ServerResourceHandler {
        
        @Override
        public String getUriPattern() {
            return "shardingsphere://foo";
        }
        
        @Override
        public MCPResponse handle(final MCPUriVariables uriVariables) {
            return () -> Map.of("foo_key", "foo_value");
        }
    }
}
