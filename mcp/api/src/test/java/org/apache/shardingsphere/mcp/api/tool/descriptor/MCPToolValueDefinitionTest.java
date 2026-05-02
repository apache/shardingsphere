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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPToolValueDefinitionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToSchemaFragmentCases")
    void assertToSchemaFragment(final String name, final MCPToolValueDefinition valueDefinition, final Map<String, Object> expectedSchemaFragment) {
        assertThat(valueDefinition.toSchemaFragment(), is(expectedSchemaFragment));
    }
    
    private static Stream<Arguments> assertToSchemaFragmentCases() {
        return Stream.of(
                Arguments.of("string", new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "foo description", null),
                        Map.of("type", "string", "description", "foo description")),
                Arguments.of("string enum", new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "foo description", null, List.of("foo", "bar")),
                        Map.of("type", "string", "description", "foo description", "enum", List.of("foo", "bar"))),
                Arguments.of("integer", new MCPToolValueDefinition(MCPToolValueDefinition.Type.INTEGER, "bar description", null),
                        Map.of("type", "integer", "description", "bar description")),
                Arguments.of("array", new MCPToolValueDefinition(MCPToolValueDefinition.Type.ARRAY, "baz description",
                        new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "item description", null, List.of("foo"))),
                        Map.of("type", "array", "description", "baz description", "items", Map.of("type", "string", "description", "item description", "enum", List.of("foo")))),
                Arguments.of("boolean", new MCPToolValueDefinition(MCPToolValueDefinition.Type.BOOLEAN, "qux description", null),
                        Map.of("type", "boolean", "description", "qux description")),
                Arguments.of("object", new MCPToolValueDefinition(MCPToolValueDefinition.Type.OBJECT, "quux description", null),
                        Map.of("type", "object", "description", "quux description", "additionalProperties", true)));
    }
}
