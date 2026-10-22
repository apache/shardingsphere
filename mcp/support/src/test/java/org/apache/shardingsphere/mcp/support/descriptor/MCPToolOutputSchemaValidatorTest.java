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

import java.util.Collection;
import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolOutputSchemaValidatorTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidOutputSchemas")
    void assertValidateWithInvalidOutputSchema(final String name, final Map<String, Object> outputSchema, final String expectedMessage) {
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolOutputSchemaValidator.validate(createDescriptor(outputSchema)));
        assertThat(actual.getMessage(), is(expectedMessage));
    }
    
    private MCPToolDescriptor createDescriptor(final Map<String, Object> outputSchema) {
        return new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", Map.of(), outputSchema, MCPToolAnnotations.builder().build(), Map.of());
    }
    
    private static Collection<Arguments> invalidOutputSchemas() {
        return List.of(
                Arguments.of("wrong schema type", Map.of("type", "array"), "Tool `database_gateway_test_tool` outputSchema must be an object."),
                Arguments.of("empty properties", createOutputSchema(Map.of(), List.of(Map.of("status", "ok"))), "Tool `database_gateway_test_tool` outputSchema must declare properties."),
                Arguments.of("missing examples", Map.of("type", "object", "properties", Map.of("status", Map.of("description", "Status."))),
                        "Tool `database_gateway_test_tool` outputSchema must declare examples."),
                Arguments.of("unknown response mode", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("response_mode", "foo_unknown"))),
                        "Tool `database_gateway_test_tool` output example uses unknown response_mode `foo_unknown`."),
                Arguments.of("unknown continuation mode", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("continuation_mode", "foo_unknown"))),
                        "Tool `database_gateway_test_tool` output example uses unknown continuation_mode `foo_unknown`."),
                Arguments.of("unknown recovery category", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("recovery_category", "foo_unknown"))),
                        "Tool `database_gateway_test_tool` output example uses unknown recovery_category `foo_unknown`."),
                Arguments.of("next actions is not array", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("next_actions", "foo_actions"))),
                        "Tool `database_gateway_test_tool` next_actions example must be an array."),
                Arguments.of("action is not object", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("next_actions", List.of("foo_action")))),
                        "Tool `database_gateway_test_tool` next_actions example item must be an object."),
                Arguments.of("unknown action type", createOutputSchema(Map.of("status", Map.of("description", "Status.")), List.of(Map.of("next_actions", List.of(Map.of("type", "foo_action"))))),
                        "Tool `database_gateway_test_tool` next_actions example uses unknown type `foo_action`."),
                Arguments.of("critical field is not object", createOutputSchema(Map.of("summary", "foo_summary"), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` model-critical output field `summary` must be an object."),
                Arguments.of("missing description", createOutputSchema(Map.of("summary", Map.of("type", "string")), List.of(Map.of("status", "ok"))),
                        "Tool model-critical output field `database_gateway_test_tool.summary` description is required."),
                Arguments.of("resource placeholder", createOutputSchema(Map.of("summary", Map.of("description", "ShardingSphere MCP resource: foo")), List.of(Map.of("status", "ok"))),
                        "Tool model-critical output field `database_gateway_test_tool.summary` description must not be a placeholder description."),
                Arguments.of("template placeholder", createOutputSchema(Map.of("summary", Map.of("description", "ShardingSphere MCP resource template: foo")), List.of(Map.of("status", "ok"))),
                        "Tool model-critical output field `database_gateway_test_tool.summary` description must not be a placeholder description."),
                Arguments.of("action schema type", createOutputSchema(Map.of("next_actions", Map.of("type", "object", "description", "Next steps.")), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` next_actions must be an array."),
                Arguments.of("action schema items", createOutputSchema(Map.of("next_actions", Map.of("type", "array", "description", "Next steps.", "items", "foo")), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` next_actions items must be an object."),
                Arguments.of("action schema properties",
                        createOutputSchema(Map.of("next_actions", Map.of("type", "array", "description", "Next steps.", "items", Map.of())), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` next_actions items must declare properties."),
                Arguments.of("action schema required field", createOutputSchema(Map.of("next_actions", createNextActionsSchema(Map.of())), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` next_actions item must declare `order`."),
                Arguments.of("action schema field is not object", createOutputSchema(Map.of("next_actions", createNextActionsSchema(Map.of("order", 1))), List.of(Map.of("status", "ok"))),
                        "Tool `database_gateway_test_tool` next_actions item field `order` must be an object."),
                Arguments.of("action schema missing field description",
                        createOutputSchema(Map.of("next_actions", createNextActionsSchema(Map.of("order", Map.of("type", "integer")))), List.of(Map.of("status", "ok"))),
                        "Tool next_actions item field `database_gateway_test_tool.order` description is required."));
    }
    
    private static Map<String, Object> createOutputSchema(final Map<String, Object> props, final Object examples) {
        return Map.of("type", "object", "properties", props, "examples", examples);
    }
    
    private static Map<String, Object> createNextActionsSchema(final Map<String, Object> props) {
        return Map.of("type", "array", "description", "Next steps.", "items", Map.of("type", "object", "properties", props));
    }
    
    @Test
    void assertValidate() {
        Map<String, Object> inputSchema = Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string", "description", "Query.")),
                "required", List.of("query"), "additionalProperties", false);
        Map<String, Object> outputSchema = Map.of("type", "object", "properties",
                Map.of("recommended_next_tool", Map.of("type", "string", "description", "Removed alias.")), "examples", List.of(Map.of("status", "ok")));
        MCPToolAnnotations annotations = MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build();
        MCPToolDescriptor descriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.", inputSchema, outputSchema, annotations, Map.of());
        IllegalStateException actual = assertThrows(IllegalStateException.class, () -> MCPToolOutputSchemaValidator.validate(descriptor));
        assertThat(actual.getMessage(), is("Tool `database_gateway_test_tool` model-facing contract must use canonical fields instead of removed `recommended_next_tool`."));
    }
}
