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

import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.prompt.descriptor.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.resource.descriptor.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.tool.descriptor.MCPToolDescriptor;
import org.junit.jupiter.api.Test;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPDescriptorComponentValidatorsTest {
    
    @Test
    void assertResourceDescriptorValidator() {
        MCPDescriptorCatalog catalog = createCatalog(List.of(createResourceDescriptor()), List.of(createResourceDescriptor()), List.of());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPResourceDescriptorValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Resource template `shardingsphere://capabilities` must contain template variables."));
    }
    
    @Test
    void assertToolDescriptorCatalogValidator() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("oneOf", List.of(Map.of("required", List.of("query"))));
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, createOutputSchema(), createAnnotations(), Map.of())));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `oneOf`."));
    }
    
    @Test
    void assertToolOutputSchemaValidator() {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.",
                createInputSchema(), createOutputSchema(Map.of("recommended_next_tool", Map.of("type", "string", "description", "Removed alias."))), createAnnotations(), Map.of());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolOutputSchemaValidator.validate(descriptor));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `recommended_next_tool`."));
    }
    
    @Test
    void assertPromptDescriptorValidator() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.", List.of(), Map.of());
        MCPPromptTemplateBinding binding = new MCPPromptTemplateBinding("test_prompt", "META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md");
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPPromptDescriptorValidator.validate(List.of(prompt), List.of(binding)));
        assertThat(actual.getMessage(), is("Prompt template `META-INF/shardingsphere-mcp/prompts/fixture-single-brace-placeholder.md` contains unsupported model-facing placeholder `{database}`."));
    }
    
    @Test
    void assertCompletionTargetDescriptorValidator() {
        MCPPromptDescriptor prompt = new MCPPromptDescriptor("test_prompt", "Test Prompt", "Guide the model through a test prompt.",
                List.of(new MCPPromptArgumentDescriptor("database", "Database", "Logical database.", false)), Map.of());
        MCPCompletionTargetDescriptor completion = new MCPCompletionTargetDescriptor("prompt", "test_prompt", List.of("database"), 50,
                Map.of(MCPShardingSphereMetadataKeys.REQUIRED_CONTEXT_ARGUMENTS, Map.of("database", List.of("tenant"))));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPCompletionTargetDescriptorValidator.validate(List.of(completion), List.of(prompt), List.of()));
        assertThat(actual.getMessage(), is("Completion target `prompt:test_prompt` context argument `tenant` for `database` is not declared by the target."));
    }
    
    @Test
    void assertResourceNavigationDescriptorValidator() {
        MCPResourceNavigationDescriptor navigation = new MCPResourceNavigationDescriptor("missing", "database_gateway_test_tool", List.of(), List.of(), "Read test tool.");
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(createToolDescriptor()));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPResourceNavigationDescriptorValidator.validate(List.of(navigation), catalog));
        assertThat(actual.getMessage(), is("Resource navigation references unknown source `missing`."));
    }
    
    private MCPDescriptorCatalog createCatalog(final List<MCPResourceDescriptor> resourceDescriptors, final List<MCPResourceDescriptor> resourceTemplateDescriptors,
                                               final List<MCPToolDescriptor> toolDescriptors) {
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(resourceDescriptors, resourceTemplateDescriptors, toolDescriptors, List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
    
    private MCPResourceDescriptor createResourceDescriptor() {
        return new MCPResourceDescriptor("shardingsphere://capabilities", "server-capability-catalog", "Server Capability Catalog",
                "Read the model-facing capability catalog.", "application/json", MCPResourceAnnotations.EMPTY, Map.of());
    }
    
    private MCPToolDescriptor createToolDescriptor() {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(), createOutputSchema(), createAnnotations(), Map.of());
    }
    
    private MCPToolAnnotations createAnnotations() {
        return MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build();
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
