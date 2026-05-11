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

package org.apache.shardingsphere.mcp.api.tool.descriptor;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPToolDescriptorTest {
    
    @Test
    void assertToInputSchema() {
        MCPToolDescriptor actual = new MCPToolDescriptor("foo_tool", "Foo Tool", "Foo tool.", List.of(
                new MCPToolFieldDefinition("query", MCPToolValueDefinition.string("Search query."), true),
                new MCPToolFieldDefinition("object_types", MCPToolValueDefinition.array("Object types.", MCPToolValueDefinition.string("Object type.")), false)));
        Map<String, Object> expectedProperties = Map.of(
                "query", Map.of("type", "string", "description", "Search query."),
                "object_types", Map.of("type", "array", "description", "Object types.", "items",
                        Map.of("type", "string", "description", "Object type.")));
        assertThat(actual.toInputSchema(), is(Map.of(
                "type", "object",
                "properties", expectedProperties,
                "required", List.of("query"),
                "additionalProperties", false)));
    }
}
