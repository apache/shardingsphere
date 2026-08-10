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

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolOutputSchemaValidatorTest {
    
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
