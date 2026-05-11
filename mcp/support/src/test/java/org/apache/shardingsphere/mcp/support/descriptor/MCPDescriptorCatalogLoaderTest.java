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

import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolFieldDefinition;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorCatalogLoaderTest {
    
    @Test
    void assertLoadValidatesDescriptorQuality() {
        MCPDescriptorCatalog actual = MCPDescriptorCatalogLoader.load();
        Set<String> actualToolNames = actual.getToolDescriptors().stream().map(MCPToolDescriptor::getName).collect(Collectors.toSet());
        assertTrue(actualToolNames.contains("apply_workflow"));
        assertTrue(actualToolNames.contains("validate_workflow"));
        assertOutputProperties(actual, "apply_workflow", Set.of(
                "response_mode", "plan_id", "execution_mode", "next_actions", "requires_user_approval", "manual_artifact_package", "manual_artifact_summary", "manual_follow_up", "argument_provenance",
                "approval_summary",
                "approval_question", "review_focus"));
        assertOutputProperties(actual, "validate_workflow", Set.of("response_mode", "plan_id", "status", "recovery_guidance", "next_actions", "sections", "mismatches"));
        assertResourceDescriptor(actual);
        assertNoBannedPublicAliasFields(actual);
    }
    
    @Test
    void assertLoadValidatesEnumFields() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        MCPToolDescriptor actual = findTool(catalog, "apply_workflow");
        MCPToolFieldDefinition actualExecutionMode = findField(actual, "execution_mode");
        assertThat(actualExecutionMode.getValueDefinition().getEnumValues(), is(List.of("preview", "review-then-execute", "manual-only")));
        assertFalse(findField(actual, "approved_by_user").isRequired());
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
    
    private MCPResourceDescriptor findResource(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getResourceDescriptors().stream().filter(each -> uriTemplate.equals(each.getUriTemplate())).findFirst().orElseThrow();
    }
    
    private MCPToolFieldDefinition findField(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        return toolDescriptor.getFields().stream().filter(each -> fieldName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private void assertResourceDescriptor(final MCPDescriptorCatalog catalog) {
        MCPResourceDescriptor actual = findResource(catalog, "shardingsphere://workflows/{plan_id}");
        assertThat(actual.getResourceKind(), is("detail"));
        assertThat(actual.getObjectScope(), is("workflow-plan"));
        assertThat(actual.getRelatedTools(), is(List.of("validate_workflow", "apply_workflow")));
        assertTrue(actual.getMeta().isEmpty());
        assertThat(findResource(catalog, "shardingsphere://workflow/test-resource").getResourceKind(), is("detail"));
    }
    
    private void assertNoBannedPublicAliasFields(final MCPDescriptorCatalog catalog) {
        for (MCPToolDescriptor each : catalog.getToolDescriptors()) {
            assertFalse(containsBannedPublicAliasField(each.getOutputSchema()));
        }
    }
    
    private boolean containsBannedPublicAliasField(final Object value) {
        if (value instanceof Map) {
            return containsBannedPublicAliasFieldMap((Map<?, ?>) value);
        }
        if (value instanceof Iterable) {
            for (Object each : (Iterable<?>) value) {
                if (containsBannedPublicAliasField(each)) {
                    return true;
                }
            }
        }
        return false;
    }
    
    private boolean containsBannedPublicAliasFieldMap(final Map<?, ?> value) {
        if (value.containsKey("recommended_next_tool") || value.containsKey("suggested_next_tool") || value.containsKey("suggested_next_tools")
                || value.containsKey("recommended_recovery") || value.containsKey("suggested_next_action")) {
            return true;
        }
        for (Object each : value.values()) {
            if (containsBannedPublicAliasField(each)) {
                return true;
            }
        }
        return false;
    }
}
