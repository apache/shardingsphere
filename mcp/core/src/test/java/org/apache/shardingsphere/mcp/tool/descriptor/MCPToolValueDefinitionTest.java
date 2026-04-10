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

package org.apache.shardingsphere.mcp.tool.descriptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolValueDefinitionTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToSchemaFragmentCases")
    void assertToSchemaFragment(final String name, final MCPToolValueDefinition valueDefinition, final Map<String, Object> expectedSchemaFragment) {
        Map<String, Object> actual = valueDefinition.toSchemaFragment();
        assertThat(actual, is(expectedSchemaFragment));
    }
    
    @Test
    void assertToSchemaFragmentWithUnsupportedType() throws ReflectiveOperationException {
        int actualOrdinal = MCPToolValueDefinition.Type.STRING.ordinal();
        setOrdinal(MCPToolValueDefinition.Type.values().length);
        try {
            MCPToolValueDefinition valueDefinition = new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "foo description", null);
            IllegalStateException actual = assertThrows(IllegalStateException.class, valueDefinition::toSchemaFragment);
            assertThat(actual.getMessage(), is("Unsupported MCP tool value type `STRING`."));
        } finally {
            setOrdinal(actualOrdinal);
        }
    }
    
    private void setOrdinal(final int ordinal) throws ReflectiveOperationException {
        Plugins.getMemberAccessor().set(Enum.class.getDeclaredField("ordinal"), MCPToolValueDefinition.Type.STRING, ordinal);
    }
    
    private static Stream<Arguments> assertToSchemaFragmentCases() {
        return Stream.of(
                Arguments.of("string", new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "foo description", null),
                        Map.of("type", "string", "description", "foo description")),
                Arguments.of("integer", new MCPToolValueDefinition(MCPToolValueDefinition.Type.INTEGER, "bar description", null),
                        Map.of("type", "integer", "description", "bar description")),
                Arguments.of("array", new MCPToolValueDefinition(MCPToolValueDefinition.Type.ARRAY, "baz description",
                                new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "item description", null)),
                        Map.of("type", "array", "description", "baz description", "items", Map.of("type", "string", "description", "item description"))));
    }
}
