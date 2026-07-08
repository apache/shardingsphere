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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPProtocolDescriptorCatalogTest {
    
    @Test
    void assertGetResourceDescriptors() {
        Collection<MCPResourceDescriptor> resourceDescriptors = List.of(createResourceDescriptor("shardingsphere://test"));
        MCPProtocolDescriptorCatalog actual = new MCPProtocolDescriptorCatalog(resourceDescriptors, List.of(), List.of(), List.of());
        assertThat(actual.getResourceDescriptors(), is(resourceDescriptors));
    }
    
    @Test
    void assertGetResourceTemplateDescriptors() {
        Collection<MCPResourceDescriptor> resourceTemplateDescriptors = List.of(createResourceDescriptor("shardingsphere://test/{id}"));
        MCPProtocolDescriptorCatalog actual = new MCPProtocolDescriptorCatalog(List.of(), resourceTemplateDescriptors, List.of(), List.of());
        assertThat(actual.getResourceTemplateDescriptors(), is(resourceTemplateDescriptors));
    }
    
    @Test
    void assertGetToolDescriptors() {
        Collection<MCPToolDescriptor> toolDescriptors = List.of(createToolDescriptor());
        MCPProtocolDescriptorCatalog actual = new MCPProtocolDescriptorCatalog(List.of(), List.of(), toolDescriptors, List.of());
        assertThat(actual.getToolDescriptors(), is(toolDescriptors));
    }
    
    @Test
    void assertGetPromptDescriptors() {
        Collection<MCPPromptDescriptor> promptDescriptors = List.of(createPromptDescriptor());
        MCPProtocolDescriptorCatalog actual = new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(), promptDescriptors);
        assertThat(actual.getPromptDescriptors(), is(promptDescriptors));
    }
    
    @Test
    void assertGetAllResourceDescriptors() {
        MCPResourceDescriptor resourceDescriptor = createResourceDescriptor("shardingsphere://test");
        MCPResourceDescriptor resourceTemplateDescriptor = createResourceDescriptor("shardingsphere://test/{id}");
        MCPProtocolDescriptorCatalog actual = new MCPProtocolDescriptorCatalog(List.of(resourceDescriptor), List.of(resourceTemplateDescriptor), List.of(), List.of());
        assertThat(actual.getAllResourceDescriptors(), is(List.of(resourceDescriptor, resourceTemplateDescriptor)));
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final String uriTemplate) {
        return new MCPResourceDescriptor(uriTemplate, "test-resource", "Test Resource", "Read a test resource.", "application/json", MCPResourceAnnotations.EMPTY, Map.of());
    }
    
    private MCPToolDescriptor createToolDescriptor() {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", Map.of(), Map.of(),
                new MCPToolAnnotations("Test Tool", true, false, true, true), Map.of());
    }
    
    private MCPPromptDescriptor createPromptDescriptor() {
        return new MCPPromptDescriptor("test_prompt", "Test Prompt", "Render a test prompt.", List.of(), Map.of());
    }
}
