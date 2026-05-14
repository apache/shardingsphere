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

import org.apache.shardingsphere.mcp.api.common.descriptor.MCPAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceTemplateDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MCPDescriptorCatalogLoaderTest {
    
    @Test
    void assertLoadValidatesDescriptorQuality() {
        MCPDescriptorCatalog actual = MCPDescriptorCatalogLoader.load();
        Set<String> actualToolNames = actual.getToolDescriptors().stream().map(MCPToolDescriptor::getName).collect(Collectors.toSet());
        assertToolNames(actualToolNames);
        assertOutputProperties(actual, "database_gateway_apply_workflow", Set.of(
                "response_mode", "plan_id", "execution_mode", "next_actions", "requires_user_approval", "manual_artifact_package", "manual_artifact_summary", "manual_follow_up", "argument_provenance",
                "approval_summary",
                "approval_question", "review_focus"));
        assertOutputProperties(actual, "database_gateway_validate_workflow", Set.of("response_mode", "plan_id", "status", "recovery_guidance", "next_actions", "sections", "mismatches"));
        assertPlanningToolAnnotations(actual);
        assertResourceDescriptor(actual);
        assertOfficialOptionalFields(actual);
        assertNoBannedPublicAliasFields(actual);
        assertNoResponseFormatOptions(actual);
        assertNoToolExecutionPayload(actual);
    }
    
    @Test
    void assertLoadValidatesEnumFields() {
        MCPDescriptorCatalog catalog = MCPDescriptorCatalogLoader.load();
        MCPToolDescriptor actual = findTool(catalog, "database_gateway_apply_workflow");
        assertThat(findInputProperty(actual, "execution_mode").get("enum"), is(List.of("preview", "review-then-execute", "manual-only")));
        assertFalse(isRequiredInput(actual, "approved_by_user"));
    }
    
    @Test
    void assertValidateRejectsLargeCompletionMaxValues() {
        MCPDescriptorCatalog actual = new MCPDescriptorCatalog(List.of(), List.of(createResourceTemplateDescriptor()), List.of(createResourceExtensionDescriptor()), List.of(), List.of(), List.of(),
                List.of(new MCPCompletionTargetDescriptor("resource", "shardingsphere://databases/{database}", List.of("database"), 101, Map.of())), List.of(), List.of());
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogValidator.validate(actual));
        assertThat(exception.getMessage(), is("Completion target `resource:shardingsphere://databases/{database}` maxValues must not exceed 100."));
    }
    
    private void assertOutputProperties(final MCPDescriptorCatalog catalog, final String toolName, final Set<String> expectedProperties) {
        Map<?, ?> actualProperties = (Map<?, ?>) findTool(catalog, toolName).getOutputSchema().get("properties");
        for (String each : expectedProperties) {
            assertTrue(actualProperties.containsKey(each));
        }
    }
    
    private void assertToolNames(final Set<String> actualToolNames) {
        assertTrue(actualToolNames.contains("database_gateway_apply_workflow"));
        assertTrue(actualToolNames.contains("database_gateway_validate_workflow"));
        assertTrue(actualToolNames.stream().allMatch(each -> each.startsWith("database_gateway_")));
    }
    
    private void assertPlanningToolAnnotations(final MCPDescriptorCatalog catalog) {
        for (String each : List.of("database_gateway_plan_encrypt_rule")) {
            MCPToolDescriptor actual = findTool(catalog, each);
            assertFalse(actual.getAnnotations().getReadOnlyHint());
            assertFalse(actual.getAnnotations().getDestructiveHint());
            assertFalse(actual.getAnnotations().getIdempotentHint());
        }
    }
    
    private MCPToolDescriptor findTool(final MCPDescriptorCatalog catalog, final String toolName) {
        return catalog.getToolDescriptors().stream().filter(each -> toolName.equals(each.getName())).findFirst().orElseThrow();
    }
    
    private MCPResourceDescriptor findResource(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getAllResourceDescriptors().stream().filter(each -> uriTemplate.equals(MCPResourceDescriptorUtils.getUriOrTemplate(each))).findFirst().orElseThrow();
    }
    
    private MCPResourceTemplateDescriptor createResourceTemplateDescriptor() {
        return new MCPResourceTemplateDescriptor("shardingsphere://databases/{database}", "logical-database-detail", "Logical Database Detail",
                "Read one logical database detail.", Collections.emptyList(), "application/json", MCPAnnotations.EMPTY, Collections.emptyMap());
    }
    
    private MCPResourceExtensionDescriptor createResourceExtensionDescriptor() {
        return new MCPResourceExtensionDescriptor("shardingsphere://databases/{database}",
                List.of(new MCPUriVariableDescriptor("database", "Database", "Logical database name.", true, "database")),
                "detail", "database", "", List.of(), List.of(), List.of());
    }
    
    private MCPResourceExtensionDescriptor findResourceExtension(final MCPDescriptorCatalog catalog, final String uriTemplate) {
        return catalog.getResourceExtensionDescriptors().stream().filter(each -> uriTemplate.equals(each.getUriOrTemplate())).findFirst().orElseThrow();
    }
    
    private Map<?, ?> findInputProperty(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        Object properties = toolDescriptor.getInputSchema().get("properties");
        return (Map<?, ?>) ((Map<?, ?>) properties).get(fieldName);
    }
    
    private boolean isRequiredInput(final MCPToolDescriptor toolDescriptor, final String fieldName) {
        return ((List<?>) toolDescriptor.getInputSchema().get("required")).contains(fieldName);
    }
    
    private void assertResourceDescriptor(final MCPDescriptorCatalog catalog) {
        MCPResourceExtensionDescriptor extension = findResourceExtension(catalog, "shardingsphere://workflows/{plan_id}");
        assertThat(extension.getResourceKind(), is("detail"));
        assertThat(extension.getObjectScope(), is("workflow-plan"));
        assertThat(extension.getRelatedTools(), is(List.of("database_gateway_validate_workflow", "database_gateway_apply_workflow")));
        MCPResourceDescriptor actual = findResource(catalog, "shardingsphere://workflows/{plan_id}");
        assertTrue(actual.getMeta().isEmpty());
        assertThat(findResourceExtension(catalog, "shardingsphere://workflow/test-resource").getResourceKind(), is("detail"));
    }
    
    private void assertOfficialOptionalFields(final MCPDescriptorCatalog catalog) {
        MCPResourceDescriptor resource = findResource(catalog, "shardingsphere://workflow/test-resource");
        assertThat(resource.getIcons().get(0).getSizes(), is(List.of("64x64")));
    }
    
    private void assertNoBannedPublicAliasFields(final MCPDescriptorCatalog catalog) {
        for (MCPToolDescriptor each : catalog.getToolDescriptors()) {
            assertFalse(containsBannedPublicAliasField(each.getOutputSchema()));
        }
    }
    
    private void assertNoResponseFormatOptions(final MCPDescriptorCatalog catalog) {
        for (MCPToolDescriptor each : catalog.getToolDescriptors()) {
            assertFalse(containsResponseFormatOption(each.getInputSchema()));
            assertFalse(containsResponseFormatOption(each.getOutputSchema()));
        }
    }
    
    private void assertNoToolExecutionPayload(final MCPDescriptorCatalog catalog) {
        Map<String, Object> payload = MCPDescriptorCatalogPayloadBuilder.build(catalog, List.of(), List.of(), List.of());
        for (Object each : (List<?>) payload.get("tools")) {
            assertFalse(((Map<?, ?>) each).containsKey("execution"));
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
    
    private boolean containsResponseFormatOption(final Object value) {
        if (value instanceof Map) {
            return containsResponseFormatOptionMap((Map<?, ?>) value);
        }
        if (value instanceof Iterable) {
            for (Object each : (Iterable<?>) value) {
                if (containsResponseFormatOption(each)) {
                    return true;
                }
            }
        }
        return value instanceof String && String.valueOf(value).toLowerCase().contains("response_format");
    }
    
    private boolean containsResponseFormatOptionMap(final Map<?, ?> value) {
        if (value.containsKey("response_format") || value.containsKey("responseFormat")) {
            return true;
        }
        for (Object each : value.values()) {
            if (containsResponseFormatOption(each)) {
                return true;
            }
        }
        return false;
    }
}
