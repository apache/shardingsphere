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

import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceAnnotations;
import org.apache.shardingsphere.mcp.api.capability.resource.MCPResourceDescriptor;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolDescriptorCatalogValidatorTest {
    
    @Test
    void assertValidateWithUnsupportedCompositionField() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("oneOf", List.of(Map.of("required", List.of("query"))));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(createCatalog(inputSchema)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `oneOf`."));
    }
    
    @ParameterizedTest(name = "{0}")
    @ValueSource(strings = {"$defs", "definitions"})
    void assertValidateWithUnsupportedDefinitionField(final String fieldName) {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put(fieldName, Map.of("query", Map.of("type", "string")));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(createCatalog(inputSchema)));
        assertThat(actual.getMessage(), is(String.format("Tool `database_gateway_test_tool` inputSchema contains unsupported top-level field `%s`.", fieldName)));
    }
    
    @Test
    void assertValidateWithUnsupportedReferenceField() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("properties", Map.of("query", Map.of("$ref", "#/$defs/query", "description", "Query.")));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(createCatalog(inputSchema)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` contains unsupported field `$ref`."));
    }
    
    @Test
    void assertValidateWithObjectAdditionalProperties() {
        Map<String, Object> inputSchema = new LinkedHashMap<>(createInputSchema());
        inputSchema.put("additionalProperties", Map.of("type", "string"));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(createCatalog(inputSchema)));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` inputSchema additionalProperties must be a boolean."));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidCoreTools")
    void assertValidateWithInvalidCoreTool(final String name, final String toolName, final Map<String, Object> inputSchema, final Map<String, Object> outputSchema, final String expectedMessage) {
        MCPToolDescriptor descriptor = new MCPToolDescriptor(toolName, "Test Tool", "Run a test tool.", inputSchema, outputSchema, MCPToolAnnotations.builder().build(), Map.of());
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(descriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private static Collection<Arguments> invalidCoreTools() {
        Map<String, Object> inputSchema = createInputSchema();
        Map<String, Object> searchOutput = createOutputSchema(List.of("response_mode", "items", "count", "search_context", "total_match_count"));
        Map<String, Object> queryOutput = createOutputSchema(List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "truncated", "next_actions"));
        Map<String, Object> updateOutput = createOutputSchema(List.of("response_mode", "result_kind", "statement_class", "statement_type", "status", "columns", "rows", "returned_row_count",
                "applied_max_rows", "applied_timeout_ms", "suggested_arguments", "next_actions"));
        Map<String, Object> runtimeOutput = createOutputSchema(List.of("response_mode", "status", "database", "checks", "category", "recovery"));
        Map<String, Object> validRuntimeOutput = replaceProperty(runtimeOutput, "response_mode", Map.of("type", "string", "description", "Mode.", "enum", List.of("validation")));
        Map<String, Object> databaseInput = Map.of("type", "object", "properties", Map.of("database", Map.of("type", "string", "description", "Database.")),
                "required", List.of("database"), "additionalProperties", false);
        return List.of(
                Arguments.of("search items schema is not object", "database_gateway_search_metadata", inputSchema,
                        replaceProperty(searchOutput, "items", Map.of("type", "array", "description", "Items.", "items", true)),
                        "Tool `database_gateway_search_metadata` outputSchema property `items.items` must be an object."),
                Arguments.of("search item properties missing", "database_gateway_search_metadata", inputSchema,
                        replaceProperty(searchOutput, "items", Map.of("type", "array", "description", "Items.", "items", Map.of("type", "object"))),
                        "Tool `database_gateway_search_metadata` outputSchema property `items.items.properties` must declare properties."),
                Arguments.of("search item field missing", "database_gateway_search_metadata", inputSchema,
                        replaceProperty(searchOutput, "items",
                                Map.of("type", "array", "description", "Items.", "items", Map.of("type", "object", "properties", Map.of("foo", Map.of("description", "Field."))))),
                        "Tool `database_gateway_search_metadata` outputSchema item must declare `database`."),
                Arguments.of("search item field is not object", "database_gateway_search_metadata", inputSchema,
                        replaceProperty(searchOutput, "items", Map.of("type", "array", "description", "Items.", "items", Map.of("type", "object", "properties", Map.of("database", true)))),
                        "Tool `database_gateway_search_metadata` outputSchema item property `database` must be an object."),
                Arguments.of("explain database missing", "database_gateway_execute_explain_query", inputSchema, queryOutput, "Tool `database_gateway_execute_explain_query` must declare `database`."),
                Arguments.of("explain database optional", "database_gateway_execute_explain_query",
                        Map.of("type", "object", "properties", Map.of("database", Map.of("type", "string", "description", "Database.")), "required", List.of(), "additionalProperties", false),
                        queryOutput, "Tool `database_gateway_execute_explain_query` `database` must be required."),
                Arguments.of("update mode missing", "database_gateway_execute_update", inputSchema, updateOutput, "Tool `database_gateway_execute_update` must declare execution_mode."),
                Arguments.of("update mode optional", "database_gateway_execute_update", createModeInputSchema(List.of()), updateOutput,
                        "Tool `database_gateway_execute_update` execution_mode must be required."),
                Arguments.of("update mode enum missing", "database_gateway_execute_update",
                        Map.of("type", "object", "properties", Map.of("execution_mode", Map.of("type", "string", "description", "Mode.")), "required", List.of("execution_mode"),
                                "additionalProperties", false),
                        updateOutput, "Tool `database_gateway_execute_update` execution_mode must allow execute and preview."),
                Arguments.of("runtime response mode enum missing", "database_gateway_validate_runtime_database", databaseInput, runtimeOutput,
                        "Tool `database_gateway_validate_runtime_database` response_mode must allow validation."),
                Arguments.of("runtime database missing", "database_gateway_validate_runtime_database", inputSchema, validRuntimeOutput,
                        "Tool `database_gateway_validate_runtime_database` must declare `database`."),
                Arguments.of("runtime database optional", "database_gateway_validate_runtime_database",
                        Map.of("type", "object", "properties", Map.of("database", Map.of("type", "string", "description", "Database.")), "required", List.of(), "additionalProperties", false),
                        validRuntimeOutput, "Tool `database_gateway_validate_runtime_database` database must be required."),
                Arguments.of("runtime credential exposed", "database_gateway_validate_runtime_database",
                        Map.of("type", "object", "properties",
                                Map.of("database", Map.of("type", "string", "description", "Database."), "password", Map.of("type", "string", "description", "Password.")), "required",
                                List.of("database"), "additionalProperties", false),
                        validRuntimeOutput, "Tool `database_gateway_validate_runtime_database` must not expose `password`."),
                Arguments.of("runtime check items missing", "database_gateway_validate_runtime_database", databaseInput,
                        replaceProperty(validRuntimeOutput, "checks", Map.of("type", "array", "description", "Checks.")),
                        "Tool `database_gateway_validate_runtime_database` outputSchema property `checks.items` must be an object."),
                Arguments.of("runtime check properties missing", "database_gateway_validate_runtime_database", databaseInput,
                        replaceProperty(validRuntimeOutput, "checks", Map.of("type", "array", "description", "Checks.", "items", Map.of("type", "object"))),
                        "Tool `database_gateway_validate_runtime_database` outputSchema property `checks.items.properties` must declare properties."),
                Arguments.of("runtime check field missing", "database_gateway_validate_runtime_database", databaseInput,
                        replaceProperty(validRuntimeOutput, "checks",
                                Map.of("type", "array", "description", "Checks.", "items", Map.of("type", "object", "properties", Map.of("foo", Map.of("description", "Field."))))),
                        "Tool `database_gateway_validate_runtime_database` outputSchema check item must declare `name`."));
    }
    
    private static Map<String, Object> createOutputSchema(final Collection<String> fields) {
        Map<String, Object> props = new LinkedHashMap<>(fields.size(), 1F);
        for (String each : fields) {
            props.put(each, Map.of("type", "string", "description", "Output field."));
        }
        if (props.containsKey("next_actions")) {
            props.put("next_actions", Map.of("type", "array", "description", "Next steps.", "items", Map.of("type", "object", "properties",
                    Map.of("order", Map.of("description", "Order."), "type", Map.of("description", "Type."), "title", Map.of("description", "Title.")))));
        }
        return Map.of("type", "object", "properties", props, "examples", List.of(Map.of("status", "ok")));
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, Object> replaceProperty(final Map<String, Object> schema, final String name, final Object value) {
        Map<String, Object> props = new LinkedHashMap<>((Map<String, Object>) schema.get("properties"));
        props.put(name, value);
        return Map.of("type", "object", "properties", props, "examples", schema.get("examples"));
    }
    
    private static Map<String, Object> createModeInputSchema(final Collection<String> required) {
        return Map.of("type", "object", "properties", Map.of("execution_mode", Map.of("type", "string", "description", "Mode.", "enum", List.of("execute", "preview"))),
                "required", required, "additionalProperties", false);
    }
    
    @Test
    void assertValidateWithRelatedResourceMetadataType() {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(), createOutputSchema(List.of("status")),
                MCPToolAnnotations.builder().build(), Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, "shardingsphere://foo_resource"));
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(descriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` metadata `org.apache.shardingsphere/related-resource-uris` must be a list."));
    }
    
    @Test
    void assertValidateWithRelatedResourceWithoutMetadata() {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", createInputSchema(), createOutputSchema(List.of("status")),
                MCPToolAnnotations.builder().build(), Map.of(MCPShardingSphereMetadataKeys.RELATED_RESOURCE_URIS, List.of("shardingsphere://foo_resource")));
        MCPResourceDescriptor resource =
                new MCPResourceDescriptor("shardingsphere://foo_resource", "foo_resource", "Resource", "Read resource.", "application/json", MCPResourceAnnotations.EMPTY, Map.of());
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(resource), List.of(), List.of(descriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is(
                "Tool `database_gateway_test_tool` metadata `org.apache.shardingsphere/related-resource-uris` references resource `shardingsphere://foo_resource` without ShardingSphere metadata."));
    }
    
    @Test
    void assertValidateWithDuplicateTools() {
        MCPDescriptorCatalog catalog = createCatalog(createInputSchema());
        MCPToolDescriptor descriptor = catalog.getProtocolDescriptors().getToolDescriptors().iterator().next();
        MCPDescriptorCatalog duplicateCatalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(descriptor, descriptor), List.of()),
                catalog.getShardingSphereDescriptors());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(duplicateCatalog));
        assertThat(actual.getMessage(), is("Duplicate MCP tool descriptor `database_gateway_test_tool`."));
    }
    
    @Test
    void assertValidateWithMissingPlanningTool() {
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of(new MCPToolRuntimeDescriptor("foo_missing", "plan"))));
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolDescriptorCatalogValidator.validate(catalog));
        assertThat(actual.getMessage(), is("Planning runtime tool `foo_missing` must reference a registered tool descriptor."));
    }
    
    private MCPDescriptorCatalog createCatalog(final Map<String, Object> inputSchema) {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema,
                Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string", "description", "Status.")), "examples", List.of(Map.of("status", "ok"))),
                MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(), Map.of());
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(descriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
    
    private static Map<String, Object> createInputSchema() {
        return Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string", "description", "Query.")),
                "required", List.of("query"), "additionalProperties", false);
    }
}
