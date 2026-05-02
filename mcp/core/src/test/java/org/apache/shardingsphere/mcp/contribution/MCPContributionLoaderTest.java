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

package org.apache.shardingsphere.mcp.contribution;

import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.mcp.api.resource.MCPResourceContribution;
import org.apache.shardingsphere.mcp.api.spi.MCPContributionProvider;
import org.apache.shardingsphere.mcp.api.tool.MCPToolContribution;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
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

class MCPContributionLoaderTest {
    
    @Test
    void assertLoadToolContributions() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        List<MCPToolContribution> toolContributions = List.of(createToolContribution("search_metadata"), createToolContribution("plan_encrypt_rule"));
        when(provider.getToolContributions()).thenReturn(toolContributions);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPContributionProvider.class)).thenReturn(List.of(provider));
            List<String> actual = MCPContributionLoader.loadToolContributions().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("search_metadata", "plan_encrypt_rule")));
        }
    }
    
    @Test
    void assertLoadResourceContributions() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        List<MCPResourceContribution> resourceContributions = List.of(createResourceContribution("shardingsphere://foo"));
        when(provider.getResourceContributions()).thenReturn(resourceContributions);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPContributionProvider.class)).thenReturn(List.of(provider));
            List<MCPResourceContribution> actual = List.copyOf(MCPContributionLoader.loadResourceContributions());
            assertThat(actual.stream().map(MCPResourceContribution::getUriPattern).toList(), is(List.of("shardingsphere://foo")));
            assertThrows(UnsupportedOperationException.class, () -> MCPContributionLoader.loadResourceContributions().clear());
        }
    }
    
    @Test
    void assertCreateToolContributionsWithNullContributions() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        when(provider.getToolContributions()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPContributionLoader.createToolContributions(provider));
        assertThat(actual.getMessage(), is(String.format("Tool contributions are required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateToolContributionsWithNullContribution() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        List<MCPToolContribution> toolContributions = new ArrayList<>();
        toolContributions.add(createToolContribution("search_metadata"));
        toolContributions.add(null);
        when(provider.getToolContributions()).thenReturn(toolContributions);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPContributionLoader.createToolContributions(provider));
        assertThat(actual.getMessage(), is(String.format("Tool contribution is required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceContributionsWithNullContributions() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        when(provider.getResourceContributions()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPContributionLoader.createResourceContributions(provider));
        assertThat(actual.getMessage(), is(String.format("Resource contributions are required for `%s`.", provider.getClass().getName())));
    }
    
    @Test
    void assertCreateResourceContributionsWithNullContribution() {
        MCPContributionProvider provider = mock(MCPContributionProvider.class);
        List<MCPResourceContribution> resourceContributions = new ArrayList<>();
        resourceContributions.add(createResourceContribution("shardingsphere://foo"));
        resourceContributions.add(null);
        when(provider.getResourceContributions()).thenReturn(resourceContributions);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPContributionLoader.createResourceContributions(provider));
        assertThat(actual.getMessage(), is(String.format("Resource contribution is required for `%s`.", provider.getClass().getName())));
    }
    
    private static MCPToolContribution createToolContribution(final String toolName) {
        MCPToolDescriptor descriptor = createToolDescriptor(toolName);
        MCPToolContribution result = mock(MCPToolContribution.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
    
    private static MCPToolDescriptor createToolDescriptor(final String toolName) {
        MCPToolDescriptor result = mock(MCPToolDescriptor.class);
        when(result.getName()).thenReturn(toolName);
        return result;
    }
    
    private static MCPResourceContribution createResourceContribution(final String uriPattern) {
        MCPResourceContribution result = mock(MCPResourceContribution.class);
        when(result.getUriPattern()).thenReturn(uriPattern);
        return result;
    }
}
