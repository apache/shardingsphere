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

import java.util.List;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MCPToolDescriptorTest {
    
    @Test
    void assertGetName() {
        MCPToolDescriptor actual = new MCPToolDescriptor("foo_tool", List.of());
        assertThat(actual.getName(), is("foo_tool"));
    }
    
    @Test
    void assertGetFields() {
        List<MCPToolFieldDefinition> expectedFields = List.of(new MCPToolFieldDefinition("foo_field", new MCPToolValueDefinition(MCPToolValueDefinition.Type.STRING, "foo description", null), true));
        MCPToolDescriptor actual = new MCPToolDescriptor("foo_tool", expectedFields);
        assertThat(actual.getFields(), is(expectedFields));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetTitleCases")
    void assertGetTitle(final String name, final String toolName, final String expectedTitle) {
        MCPToolDescriptor actual = new MCPToolDescriptor(toolName, List.of());
        assertThat(actual.getTitle(), is(expectedTitle));
    }
    
    static Stream<Arguments> assertGetTitleCases() {
        return Stream.of(
                Arguments.of("multiple words", "foo_bar", "Foo Bar"),
                Arguments.of("single word", "foo", "Foo"),
                Arguments.of("empty segments", "_foo__bar", "Foo Bar"));
    }
    
    @Test
    void assertGetDescription() {
        MCPToolDescriptor actual = new MCPToolDescriptor("foo_tool", List.of());
        assertThat(actual.getDescription(), is("ShardingSphere MCP tool: foo_tool"));
    }
}
