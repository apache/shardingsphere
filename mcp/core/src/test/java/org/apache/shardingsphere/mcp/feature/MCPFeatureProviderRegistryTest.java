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
import org.apache.shardingsphere.mcp.workflow.spi.MCPWorkflowToolContribution;
import org.apache.shardingsphere.mcp.protocol.response.MCPMapResponse;
import org.apache.shardingsphere.mcp.resource.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class MCPFeatureProviderRegistryTest {
    
    @Test
    void assertLoadToolHandlersWithContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        MCPToolDescriptor toolDescriptor = createToolDescriptor("search_metadata");
        MCPToolDescriptor planningToolDescriptor = createToolDescriptor("plan_encrypt_rule");
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectToolContribution(toolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of())),
                new MCPWorkflowToolContribution(planningToolDescriptor, (requestContext, sessionId, arguments) -> new MCPMapResponse(Map.of()),
                        "apply_encrypt_rule", "validate_encrypt_rule", (snapshot, metadataQueryFacade, queryFacade, executionFacade, sessionId) -> {
                        },
                        (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of("status", "validated"))));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<String> actual = MCPFeatureProviderRegistry.loadToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("search_metadata", "plan_encrypt_rule", "apply_encrypt_rule", "validate_encrypt_rule")));
        }
    }
    
    @Test
    void assertLoadResourceHandlersWithContributions() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        when(featureProvider.getContributions()).thenReturn(List.of(
                new MCPDirectResourceContribution("shardingsphere://foo", (requestContext, uriVariables) -> new MCPMapResponse(Map.of()))));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<ResourceHandler> actual = List.copyOf(MCPFeatureProviderRegistry.loadResourceHandlers());
            assertThat(actual.stream().map(ResourceHandler::getUriPattern).toList(), is(List.of("shardingsphere://foo")));
            assertThrows(UnsupportedOperationException.class, () -> MCPFeatureProviderRegistry.loadResourceHandlers().clear());
        }
    }
    
    private static MCPToolDescriptor createToolDescriptor(final String toolName) {
        MCPToolDescriptor result = mock(MCPToolDescriptor.class);
        when(result.getName()).thenReturn(toolName);
        return result;
    }
}
