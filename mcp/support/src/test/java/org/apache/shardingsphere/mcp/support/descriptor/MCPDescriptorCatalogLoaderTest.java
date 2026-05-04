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

import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorCatalogLoaderTest {
    
    @Test
    void assertLoadValidatesDescriptorQuality() {
        MCPDescriptorCatalog actual = MCPDescriptorCatalogLoader.load();
        Set<String> actualToolNames = actual.getToolDescriptors().stream().map(MCPToolDescriptor::getName).collect(Collectors.toSet());
        assertTrue(actualToolNames.contains("apply_workflow"));
        assertTrue(actualToolNames.contains("validate_workflow"));
        assertOutputProperties(actual, "apply_workflow", Set.of("plan_id", "execution_mode", "next_actions", "requires_user_approval"));
        assertOutputProperties(actual, "validate_workflow", Set.of("plan_id", "status", "next_actions"));
    }
    
    @Test
    void assertLoadValidatesEnumFields() {
        MCPToolDescriptor actual = findTool(MCPDescriptorCatalogLoader.load(), "apply_workflow");
        MCPToolFieldDefinition actualExecutionMode = actual.getFields().stream().filter(each -> "execution_mode".equals(each.getName())).findFirst().orElseThrow();
        assertThat(actualExecutionMode.getValueDefinition().getEnumValues(), is(List.of("preview", "review-then-execute", "manual-only")));
    }
    
    private void assertOutputProperties(final MCPDescriptorCatalog catalog, final String toolName, final Set<String> expectedProperties) {
        Map<?, ?> actualProperties = (Map<?, ?>) findTool(catalog, toolName).getOutputSchema().get("properties");
        for (String each : expectedProperties) {
            assertTrue(actualProperties.containsKey(each));
        }
    }
    
    private MCPToolDescriptor findTool(final MCPDescriptorCatalog catalog, final String toolName) {
        return catalog.getToolDescriptors().stream().filter(each -> toolName.equals(each.getName())).findFirst().orElseThrow();
    }
}
