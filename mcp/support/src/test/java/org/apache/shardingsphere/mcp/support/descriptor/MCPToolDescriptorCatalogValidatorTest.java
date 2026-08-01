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

import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolAnnotations;
import org.apache.shardingsphere.mcp.api.capability.tool.MCPToolDescriptor;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

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
    
    private MCPDescriptorCatalog createCatalog(final Map<String, Object> inputSchema) {
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema,
                Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string", "description", "Status.")), "examples", List.of(Map.of("status", "ok"))),
                MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(), Map.of());
        return new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(descriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
    }
    
    private Map<String, Object> createInputSchema() {
        return Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string", "description", "Query.")),
                "required", List.of("query"), "additionalProperties", false);
    }
}
