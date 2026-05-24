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

import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPDescriptorCatalogValidatorTest {
    
    @Test
    void assertValidateResourceAnnotations() {
        MCPDescriptorCatalog actual = createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of("assistant"), 0D, "2026-05-13T00:00:00Z"))), List.of());
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(actual));
    }
    
    @Test
    void assertValidateScopedProtocolNonGoalFieldsAreNotRequired() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createCatalog(
                List.of(createResourceDescriptor(MCPResourceAnnotations.EMPTY)),
                List.of(createToolDescriptor(new MCPToolAnnotations("Test Tool", true, false, true, true))))));
    }
    
    @Test
    void assertValidateRejectsPublicAliasOutputField() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_test_tool", new MCPToolAnnotations("Test Tool", true, false, true, true),
                createOutputSchema(Map.of("recommended_next_tool", Map.of("type", "string", "description", "Legacy alias.")))))),
                "Tool `database_gateway_test_tool` outputSchema must use canonical fields instead of banned `recommended_next_tool`.");
    }
    
    @Test
    void assertValidateRejectsFeatureOwnedToolDescriptor() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_extension_test_tool", new MCPToolAnnotations("Extension Tool", true, false, true, true), createOutputSchema()))),
                "Tool `database_gateway_extension_test_tool` outputSchema must declare `extension_marker`.");
    }
    
    @Test
    void assertValidateDoesNotHardcodeCoreToolDescriptor() {
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_search_metadata", new MCPToolAnnotations("Search Metadata", true, false, true, true), createOutputSchema())))));
    }
    
    private void assertValidationError(final MCPDescriptorCatalog catalog, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors) {
        return createCatalog(resourceDescriptors, List.of(), toolDescriptors);
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPResourceDescriptor> resourceTemplateDescriptors,
                                               final List<MCPToolDescriptor> toolDescriptors) {
        return new MCPDescriptorCatalog(resourceDescriptors, resourceTemplateDescriptors, List.of(), toolDescriptors, List.of(), List.of(), List.of(), List.of(), List.of());
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final MCPResourceAnnotations annotations) {
        return new MCPResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", "application/json", annotations, Map.of());
    }
    
    private MCPToolDescriptor createToolDescriptor(final MCPToolAnnotations annotations) {
        return createToolDescriptor("database_gateway_test_tool", annotations, createOutputSchema());
    }
    
    private MCPToolDescriptor createToolDescriptor(final String toolName, final MCPToolAnnotations annotations, final Map<String, Object> outputSchema) {
        return new MCPToolDescriptor(toolName, "Test Tool", "Run a test tool.", createInputSchema(), outputSchema, annotations, Map.of());
    }
    
    private Map<String, Object> createInputSchema() {
        return Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string", "description", "Query.")), "required", List.of("query"), "additionalProperties", false);
    }
    
    private Map<String, Object> createOutputSchema() {
        return createOutputSchema(Map.of("status", Map.of("type", "string", "description", "Status.")));
    }
    
    private Map<String, Object> createOutputSchema(final Map<String, Object> properties) {
        return Map.of("type", "object", "properties", properties, "examples", List.of(Map.of("status", "ok")));
    }
}
