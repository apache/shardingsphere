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

class MCPResourceNavigationDescriptorValidatorTest {
    
    @Test
    void assertValidate() {
        MCPResourceNavigationDescriptor navigation = new MCPResourceNavigationDescriptor("missing", "database_gateway_test_tool", List.of(), List.of(), "Read test tool.");
        MCPToolDescriptor toolDescriptor = new MCPToolDescriptor("database_gateway_test_tool", "Test Tool", "Run a test tool.",
                Map.of("type", "object", "properties", Map.of("query", Map.of("type", "string", "description", "Query.")),
                        "required", List.of("query"), "additionalProperties", false),
                Map.of("type", "object", "properties", Map.of("status", Map.of("type", "string", "description", "Status.")), "examples", List.of(Map.of("status", "ok"))),
                MCPToolAnnotations.builder().title("Test Tool").readOnlyHint(true).destructiveHint(false).idempotentHint(true).openWorldHint(true).build(), Map.of());
        MCPDescriptorCatalog catalog = new MCPDescriptorCatalog(new MCPProtocolDescriptorCatalog(List.of(), List.of(), List.of(toolDescriptor), List.of()),
                new MCPShardingSphereDescriptorCatalog(List.of(), List.of(), List.of(), List.of(), List.of()));
        IllegalStateException actual = assertThrows(IllegalStateException.class,
                () -> MCPResourceNavigationDescriptorValidator.validate(List.of(navigation), catalog));
        assertThat(actual.getMessage(), is("Resource navigation references unknown source `missing`."));
    }
}
