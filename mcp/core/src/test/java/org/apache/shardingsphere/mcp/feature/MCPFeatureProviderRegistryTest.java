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
import org.apache.shardingsphere.mcp.feature.spi.MCPWorkflowToolContribution;
import org.apache.shardingsphere.mcp.resource.handler.ResourceHandler;
import org.apache.shardingsphere.mcp.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.tool.handler.ToolHandler;
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
    void assertLoadToolHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        ToolHandler toolHandler = createToolHandler("plan_encrypt_rule");
        when(featureProvider.getToolHandlers()).thenReturn(List.of(toolHandler));
        when(featureProvider.getWorkflowToolContributions()).thenReturn(
                List.of(new MCPWorkflowToolContribution("apply_encrypt_rule", "validate_encrypt_rule",
                        (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of("status", "validated"))));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<String> actual = MCPFeatureProviderRegistry.loadToolHandlers().stream().map(each -> each.getToolDescriptor().getName()).toList();
            assertThat(actual, is(List.of("plan_encrypt_rule", "apply_encrypt_rule", "validate_encrypt_rule")));
        }
    }
    
    @Test
    void assertCreateToolHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        ToolHandler toolHandler = createToolHandler("plan_mask_rule");
        when(featureProvider.getToolHandlers()).thenReturn(List.of(toolHandler));
        when(featureProvider.getWorkflowToolContributions()).thenReturn(
                List.of(new MCPWorkflowToolContribution("apply_mask_rule", "validate_mask_rule",
                        (contextStore, metadataQueryFacade, queryFacade, executionFacade, sessionId, planId) -> Map.of())));
        List<String> actual = MCPFeatureProviderRegistry.createToolHandlers(featureProvider).stream().map(each -> each.getToolDescriptor().getName()).toList();
        assertThat(actual, is(List.of("plan_mask_rule", "apply_mask_rule", "validate_mask_rule")));
    }
    
    @Test
    void assertLoadResourceHandlers() {
        MCPFeatureProvider featureProvider = mock(MCPFeatureProvider.class);
        ResourceHandler resourceHandler = mock(ResourceHandler.class);
        when(featureProvider.getResourceHandlers()).thenReturn(List.of(resourceHandler));
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(MCPFeatureProvider.class)).thenReturn(List.of(featureProvider));
            List<ResourceHandler> actual = List.copyOf(MCPFeatureProviderRegistry.loadResourceHandlers());
            assertThat(actual, is(List.of(resourceHandler)));
            assertThrows(UnsupportedOperationException.class, () -> MCPFeatureProviderRegistry.loadResourceHandlers().clear());
        }
    }
    
    private static ToolHandler createToolHandler(final String toolName) {
        MCPToolDescriptor descriptor = mock(MCPToolDescriptor.class);
        when(descriptor.getName()).thenReturn(toolName);
        ToolHandler result = mock(ToolHandler.class);
        when(result.getToolDescriptor()).thenReturn(descriptor);
        return result;
    }
}
