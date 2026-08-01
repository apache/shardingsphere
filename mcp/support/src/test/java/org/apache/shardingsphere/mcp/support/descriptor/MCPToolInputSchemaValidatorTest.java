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
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolInputSchemaValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidInputSchemaCases")
    void assertValidateWithInvalidInputSchema(final String name, final Map<String, Object> inputSchema, final String expectedMessage) {
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
                Arguments.of("items is not an object", createInputSchemaWithQuery(Map.of("type", "array", "items", "string", "description", "Queries."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.items` must be an object."),
                Arguments.of("additional properties has unsupported type",
                        createInputSchemaWithQuery(Map.of("type", "object", "additionalProperties", "string", "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.additionalProperties` must be a boolean or object."),
                Arguments.of("enum is not an array", createInputSchemaWithQuery(Map.of("type", "string", "enum", "query", "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must be a non-empty array."),
                Arguments.of("enum is empty", createInputSchemaWithQuery(Map.of("type", "string", "enum", List.of(), "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must be a non-empty array."),
                Arguments.of("enum contains duplicate", createInputSchemaWithQuery(Map.of("type", "string", "enum", List.of("query", "query"), "description", "Query."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.enum` must contain unique values."),
                Arguments.of("description is not a string", createInputSchemaWithQuery(Map.of("type", "string", "description", 1), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.description` must be a string."),
                Arguments.of("examples is not an array", createInputSchemaWithQuery(Map.of("type", "string", "description", "Query.", "examples", "query"), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query.examples` must be an array."),
                Arguments.of("range is incomplete", createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 0, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` must declare minimum and maximum together."),
                Arguments.of("range is not integer",
                        createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 0.5D, "maximum", 10, "description", "Limit."), List.of("query")),
                        "Tool `database_gateway_test_tool` inputSchema at `inputSchema.properties.query` minimum and maximum must be integers in the Java int range."),
                Arguments.of("range is reversed", createInputSchemaWithQuery(Map.of("type", "integer", "minimum", 10, "maximum", 0, "description", "Limit."), List.of("query")),
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
    void assertValidateWithStringDefault() {
        Map<String, Object> inputSchema = createInputSchemaWithQuery(Map.of("type", "string", "default", "query", "description", "Query."), List.of("query"));
        assertDoesNotThrow(() -> MCPToolInputSchemaValidator.validate(createToolDescriptor(inputSchema)));
    }
    
    private MCPToolDescriptor createToolDescriptor(final Map<String, Object> inputSchema) {
        Map<String, Object> outputSchema = Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string", "description", "Status.")),
                "examples", List.of(Map.of("status", "ok")));
        MCPToolAnnotations annotations = MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build();
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, outputSchema, annotations, Map.of());
    }
}
