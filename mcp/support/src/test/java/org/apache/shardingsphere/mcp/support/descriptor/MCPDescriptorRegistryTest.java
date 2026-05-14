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
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorRegistryTest {
    
    @Test
    void assertGetPromptDescriptors() {
        Collection<MCPPromptDescriptor> actualDescriptors = MCPDescriptorRegistry.getPromptDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "inspect_metadata".equals(each.getName())));
    }
    
    @Test
    void assertGetCompletionTargetDescriptors() {
        Collection<MCPCompletionTargetDescriptor> actualDescriptors = MCPDescriptorRegistry.getCompletionTargetDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "prompt".equals(each.getReferenceType()) && "inspect_metadata".equals(each.getReference())));
    }
    
    @Test
    void assertGetResourceNavigationDescriptors() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorRegistry.getResourceNavigationDescriptors();
        assertTrue(actualDescriptors.stream().anyMatch(each -> "database_gateway_apply_workflow".equals(each.getFrom()) && "database_gateway_validate_workflow".equals(each.getTo())));
    }
    
    @Test
    void assertGetResourceNavigationDescriptorsByFrom() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorRegistry.getResourceNavigationDescriptors("database_gateway_apply_workflow");
        assertFalse(actualDescriptors.isEmpty());
        assertTrue(actualDescriptors.stream().allMatch(each -> "database_gateway_apply_workflow".equals(each.getFrom())));
        assertTrue(actualDescriptors.stream().anyMatch(each -> "database_gateway_validate_workflow".equals(each.getTo())));
    }
    
    @Test
    void assertGetResourceNavigationDescriptorsByUnknownFrom() {
        Collection<MCPResourceNavigationDescriptor> actualDescriptors = MCPDescriptorRegistry.getResourceNavigationDescriptors("shardingsphere://unknown");
        assertTrue(actualDescriptors.isEmpty());
    }
}
