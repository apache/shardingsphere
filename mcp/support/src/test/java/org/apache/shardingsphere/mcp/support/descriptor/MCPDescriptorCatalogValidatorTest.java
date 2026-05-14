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
        MCPDescriptorCatalog actual = createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of("assistant"), 0D, true, "2026-05-13T00:00:00Z"))), List.of());
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(actual));
    }
    
    @Test
    void assertValidateResourceSize() {
        MCPDescriptorCatalog actual = createCatalog(List.of(createSizedResourceDescriptor(128L, true)), List.of());
        assertDoesNotThrow(() -> MCPDescriptorCatalogValidator.validate(actual));
    }
    
    @Test
    void assertValidateRejectsNegativeResourceSize() {
        assertValidationError(createCatalog(List.of(createSizedResourceDescriptor(-1L, true)), List.of()), "Resource `shardingsphere://capabilities` size must be non-negative.");
    }
    
    @Test
    void assertValidateRejectsResourceTemplateSize() {
        MCPResourceDescriptor resourceTemplate = new MCPResourceDescriptor("shardingsphere://databases/{database}", "logical-database-detail", "Logical Database Detail",
                "Read one logical database detail.", "application/json", 128L, true, MCPResourceAnnotations.EMPTY, Map.of());
        assertValidationError(new MCPDescriptorCatalog(List.of(), List.of(resourceTemplate), List.of(createResourceExtensionDescriptor()), List.of(), List.of(), List.of(), List.of(), List.of(),
                List.of()), "Resource template `shardingsphere://databases/{database}` must not declare size.");
    }
    
    @Test
    void assertValidateRejectsInvalidResourceAudience() {
        assertValidationError(createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of("model"), 0D, false, null))), List.of()),
                "Resource `shardingsphere://capabilities` annotations audience `model` is not an MCP role.");
    }
    
    @Test
    void assertValidateRejectsInvalidResourcePriorityRange() {
        assertValidationError(createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of(), 1.1D, true, null))), List.of()),
                "Resource `shardingsphere://capabilities` annotations priority must be between 0.0 and 1.0.");
    }
    
    @Test
    void assertValidateRejectsInvalidResourcePriorityValue() {
        assertValidationError(createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of(), Double.NaN, true, null))), List.of()),
                "Resource `shardingsphere://capabilities` annotations priority must be finite.");
    }
    
    @Test
    void assertValidateRejectsInvalidResourceLastModified() {
        assertValidationError(createCatalog(List.of(createResourceDescriptor(new MCPResourceAnnotations(List.of(), 0D, false, "2026-05-13T00:00:00"))), List.of()),
                "Resource `shardingsphere://capabilities` annotations lastModified must be an ISO 8601 timestamp with an explicit offset or UTC marker.");
    }
    
    @Test
    void assertValidateRejectsEmptyToolAnnotations() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(MCPToolAnnotations.EMPTY))), "Tool `database_gateway_test_tool` must declare MCP annotations.");
    }
    
    @Test
    void assertValidateRejectsContradictoryToolAnnotations() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(new MCPToolAnnotations("Test Tool", true, true, true, true)))),
                "Tool `database_gateway_test_tool` annotations cannot be both read-only and destructive.");
    }
    
    @Test
    void assertValidateRejectsFeatureOwnedToolDescriptor() {
        assertValidationError(createCatalog(List.of(), List.of(createToolDescriptor(
                "database_gateway_extension_test_tool", new MCPToolAnnotations("Extension Tool", true, false, true, true), createOutputSchema()))),
                "Tool `database_gateway_extension_test_tool` outputSchema must declare `extension_marker`.");
    }
    
    private void assertValidationError(final MCPDescriptorCatalog catalog, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPToolDescriptor> toolDescriptors) {
        return new MCPDescriptorCatalog(resourceDescriptors, List.of(), List.of(), toolDescriptors, List.of(), List.of(), List.of(), List.of(), List.of());
    }
    
    private MCPResourceDescriptor createResourceDescriptor(final MCPResourceAnnotations annotations) {
        return new MCPResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", "application/json", annotations, Map.of());
    }
    
    private MCPResourceDescriptor createSizedResourceDescriptor(final long size, final boolean sizePresent) {
        return new MCPResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", "application/json", size, sizePresent, MCPResourceAnnotations.EMPTY, Map.of());
    }
    
    private MCPResourceExtensionDescriptor createResourceExtensionDescriptor() {
        return new MCPResourceExtensionDescriptor("shardingsphere://databases/{database}", List.of(
                new MCPUriVariableDescriptor("database", "Database", "Logical database name.", true, "database")), "detail", "database", "", List.of(), List.of(), List.of());
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
        return Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string", "description", "Status.")), "examples", List.of(Map.of("status", "ok")));
    }
}
