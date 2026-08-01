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

import org.apache.shardingsphere.mcp.api.capability.completion.MCPCompletionTargetDescriptor;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptArgumentDescriptor;
import org.apache.shardingsphere.mcp.api.capability.prompt.MCPPromptDescriptor;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
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
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"$defs", "definitions"})
    void assertToolDescriptorCatalogValidatorWithUnsupportedDefinitionField(final String fieldName) {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put(fieldName, Map.of("query", Map.of("type", "string")));
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, createOutputSchema(), createAnnotations(), Map.of())));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(String.format("Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `%s`.", fieldName)));
    }
    
    @Test
    void assertToolDescriptorCatalogValidatorWithUnsupportedReferenceField() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("properties", Map.of("query", Map.of("$ref", "#/$defs/query", "description", "Query.")));
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, createOutputSchema(), createAnnotations(), Map.of())));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` contains unsupported field `$ref`."));
    }
    
    @Test
    void assertToolDescriptorCatalogValidatorWithObjectAdditionalProperties() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("additionalProperties", Map.of("type", "string"));
        MCPDescriptorCatalog catalog = createCatalog(List.of(), List.of(), List.of(new MCPToolDescriptor(
                "database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, createOutputSchema(), createAnnotations(), Map.of())));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema additionalProperties must be a boolean."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputSchemaCases")
    void assertToolInputSchemaValidatorWithInvalidInputSchema(final String name, final Map<String, Object> inputSchema, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolInputSchemaValidator.validate(createToolDescriptor(inputSchema)));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Stream<Arguments> invalidInputSchemaCases() {
        return Stream.of(
                Arguments.of("unsupported type", createInputSchemaWithQuery(Map.of("type", "unsupported", "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` uses unsupported type `unsupported`."),
                Arguments.of("property is not an object", createInputSchemaWithQuery("string", List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema property `inputSchema.properties.query` must be an object."),
                Arguments.of("required contains non-string", createInputSchemaWithQuery(Map.of("type", "string", "description", "Query."), List.of(1)),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.required` must contain only strings."),
                Arguments.of("required contains duplicate", createInputSchemaWithQuery(Map.of("type", "string", "description", "Query."), List.of("query", "query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.required` must contain unique property names."),
                Arguments.of("required references undeclared property", createInputSchemaWithQuery(Map.of("type", "string", "description", "Query."), List.of("missing")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.required` references an undeclared property."),
                Arguments.of("items is not an object",
                        createInputSchemaWithQuery(Map.of("type", "array", "items", "string", "description", "Queries."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.items` must be an object."),
                Arguments.of("additional properties has unsupported type",
                        createInputSchemaWithQuery(Map.of("type", "object", "additionalProperties", "string", "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.additionalProperties` must be a boolean or object."),
                Arguments.of("enum is not an array",
                        createInputSchemaWithQuery(Map.of("type", "string", "enum", "query", "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must be a non-empty array."),
                Arguments.of("enum is empty",
                        createInputSchemaWithQuery(Map.of("type", "string", "enum", List.of(), "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must be a non-empty array."),
                Arguments.of("enum contains duplicate",
                        createInputSchemaWithQuery(Map.of("type", "string", "enum", List.of("query", "query"), "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must contain unique values."),
                Arguments.of("description is not a string", createInputSchemaWithQuery(Map.of("type", "string", "description", 1), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.description` must be a string."),
                Arguments.of("examples is not an array", createInputSchemaWithQuery(Map.of("type", "string", "description", "Query.", "examples", "query"), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.examples` must be an array."),
                Arguments.of("range is incomplete",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 0, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` must declare minimum and maximum together."),
                Arguments.of("range is not integer",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 0.5D, "maximum", 10, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` minimum and maximum must be integers in the Java int range."),
                Arguments.of("range is reversed",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 10, "maximum", 0, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` minimum must not exceed maximum."),
                Arguments.of("range exceeds supported values",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", Long.MIN_VALUE, "maximum", 0, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` minimum and maximum must be integers in the Java int range."),
                Arguments.of("default is outside range",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 0, "maximum", 10, "default", 11, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.default` must be within the declared range."));
    }
    
    private static Map<String, Object> createInputSchemaWithQuery(final Object querySchema, final List<?> required) {
        return Map.of("type", "object", "properties", Map.of("query", querySchema), "required", required, "additionalProperties", false);
    }
    
    @Test
    void assertToolInputSchemaValidatorWithStringDefault() {
        Map<String, Object> inputSchema = createInputSchemaWithQuery(Map.of("type", "string", "default", "query", "description", "Query."), List.of("query"));
        assertDoesNotThrow(() -> MCPToolInputSchemaValidator.validate(createToolDescriptor(inputSchema)));
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
    
    private MCPToolDescriptor createToolDescriptor(final Map<String, Object> inputSchema) {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, createOutputSchema(), createAnnotations(), Map.of());
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
