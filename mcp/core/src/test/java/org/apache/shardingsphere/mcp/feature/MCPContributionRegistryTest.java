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
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPDirectToolContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPFeatureProvider;
import org.apache.shardingsphere.mcp.feature.spi.MCPResourceContribution;
import org.apache.shardingsphere.mcp.feature.spi.MCPToolContribution;
import org.apache.shardingsphere.mcp.workflow.spi.MCPWorkflowToolContribution;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPContributionRegistryTest {
    
    @Test
    void assertCreateToolContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        MCPToolDescriptor toolDescriptor = createToolDescriptor("search_metadata");
        MCPToolDescriptor planningToolDescriptor = createToolDescriptor("plan_encrypt_rule");
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectToolContribution(toolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of())),
                new MCPDirectResourceContribution("shardingsphere://foo", (requestContext, uriVariables) -> new MCPMapResponse(Map.of())),
                new MCPWorkflowToolContribution(planningToolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of()),
                        "apply_encrypt_rule", "validate_encrypt_rule", (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                        },
                        (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of())));
        Collection<MCPToolContribution> actual = MCPContributionRegistry.createToolContributions(featureProvider);
        assertThat(actual.size(), is(2));
        assertTrue(actual.stream().anyMatch(MCPDirectToolContribution.class::isInstance));
        assertTrue(actual.stream().anyMatch(MCPWorkflowToolContribution.class::isInstance));
    }
    
    @Test
    void assertCreateResourceContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        MCPToolDescriptor toolDescriptor = createToolDescriptor("search_metadata");
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectToolContribution(toolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of())),
                new MCPDirectResourceContribution("shardingsphere://foo", (requestContext, uriVariables) -> new MCPMapResponse(Map.of()))));
        Collection<MCPResourceContribution> actual = MCPContributionRegistry.createResourceContributions(featureProvider);
        assertThat(actual.stream().map(MCPDirectResourceContribution.class::cast).map(MCPDirectResourceContribution::getUriPattern).toList(),
                is(List.of("shardingsphere://foo")));
    }
    
    @Test
    void assertLoadToolContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        MCPToolDescriptor toolDescriptor = createToolDescriptor("search_metadata");
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectToolContribution(toolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of())),
                new MCPDirectResourceContribution("shardingsphere://foo", (requestContext, uriVariables) -> new MCPMapResponse(Map.of()))));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            Collection<MCPToolContribution> actual = MCPContributionRegistry.loadToolContributions();
            assertThat(actual.size(), is(1));
            assertThrows(UnsupportedOperationException.class, actual::clear);
        }
    }
    
    @Test
    void assertLoadResourceContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        MCPToolDescriptor toolDescriptor = createToolDescriptor("search_metadata");
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectToolContribution(toolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of())),
                new MCPDirectResourceContribution("shardingsphere://foo", (requestContext, uriVariables) -> new MCPMapResponse(Map.of()))));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            Collection<MCPResourceContribution> actual = MCPContributionRegistry.loadResourceContributions();
            assertThat(actual.stream().map(MCPDirectResourceContribution.class::cast).map(MCPDirectResourceContribution::getUriPattern).toList(),
                    is(List.of("shardingsphere://foo")));
            assertThrows(UnsupportedOperationException.class, actual::clear);
        }
    }
    
    @Test
    void assertCreateContributionsWithNullContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        when(featureProvider.getContributions()).thenReturn(null);
        NullPointerException actual = assertThrows(NullPointerException.class, () -> MCPContributionRegistry.createContributions(featureProvider));
        assertThat(actual.getMessage(), is(String.format("Contributions are required for `%s`.", featureProvider.getClass().getName())));
    }
    
    private static MCPToolDescriptor createToolDescriptor(final String toolName) {
        MCPToolDescriptor result = mock(MCPToolDescriptor.class);
        when(result.getName()).thenReturn(toolName);
        return result;
    }
}
