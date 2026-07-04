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

package org.apache.shardingsphere.mcp.support.descriptor;

import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPShardingSphereDescriptorCatalogTest {
    
    @Test
    void assertGetResourceMetadata() {
        Collection<ShardingSphereMCPResourceMetadata> resourceMetadata = List.of(createResourceMetadata());
        MCPShardingSphereDescriptorCatalog actual = new MCPShardingSphereDescriptorCatalog(resourceMetadata, List.of(), List.of(), List.of(), List.of());
        assertThat(actual.getResourceMetadata(), is(resourceMetadata));
    }
    
    @Test
    void assertGetPromptTemplateBindings() {
        Collection<MCPPromptTemplateBinding> promptTemplateBindings = List.of(new MCPPromptTemplateBinding("test_prompt", "META-INF/test.md"));
        MCPShardingSphereDescriptorCatalog actual = new MCPShardingSphereDescriptorCatalog(List.of(), promptTemplateBindings, List.of(), List.of(), List.of());
        assertThat(actual.getPromptTemplateBindings(), is(promptTemplateBindings));
    }
    
    @Test
    void assertGetCompletionTargetDescriptors() {
        Collection<MCPCompletionTargetDescriptor> completionTargetDescriptors = List.of(new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50, Map.of()));
        MCPShardingSphereDescriptorCatalog actual = new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), completionTargetDescriptors, List.of(), List.of());
        assertThat(actual.getCompletionTargetDescriptors(), is(completionTargetDescriptors));
    }
    
    @Test
    void assertGetResourceNavigationDescriptors() {
        Collection<MCPResourceNavigationDescriptor> resourceNavigationDescriptors = List.of(new MCPResourceNavigationDescriptor(
                "shardingsphere://source", "shardingsphere://target", List.of(), List.of(), "Read the target resource."));
        MCPShardingSphereDescriptorCatalog actual = new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), resourceNavigationDescriptors, List.of());
        assertThat(actual.getResourceNavigationDescriptors(), is(resourceNavigationDescriptors));
    }
    
    @Test
    void assertGetToolRuntimeDescriptors() {
        Collection<MCPToolRuntimeDescriptor> toolRuntimeDescriptors = List.of(new MCPToolRuntimeDescriptor("database_gateway_test_tool", "plan", List.of("metadata")));
        MCPShardingSphereDescriptorCatalog actual = new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), toolRuntimeDescriptors);
        assertThat(actual.getToolRuntimeDescriptors(), is(toolRuntimeDescriptors));
    }
    
    private ShardingSphereMCPResourceMetadata createResourceMetadata() {
        return new ShardingSphereMCPResourceMetadata("shardingsphere://test", List.of(), "detail", "database", "", List.of(), List.of(), List.of());
    }
}
