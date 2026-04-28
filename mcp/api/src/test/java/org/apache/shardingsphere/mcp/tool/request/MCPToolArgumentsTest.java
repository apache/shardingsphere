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

package org.apache.shardingsphere.mcp.tool.request;

import org.apache.shardingsphere.mcp.capability.SupportedMCPMetadataObjectType;
import org.apache.shardingsphere.mcp.protocol.exception.MCPInvalidRequestException;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPToolArgumentsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getObjectTypesCases")
    void assertGetObjectTypes(final String name, final Map<String, Object> rawArguments, final Set<SupportedMCPMetadataObjectType> supportedObjectTypes,
                              final List<SupportedMCPMetadataObjectType> expectedObjectTypes) {
        assertThat(List.copyOf(new MCPToolArguments(rawArguments).getObjectTypes(supportedObjectTypes)), is(expectedObjectTypes));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidObjectTypesCases")
    void assertGetObjectTypesWithInvalidArgument(final String name, final Map<String, Object> rawArguments, final Set<SupportedMCPMetadataObjectType> supportedObjectTypes,
                                                 final String expectedMessage) {
        assertThat(assertThrows(MCPInvalidRequestException.class, () -> new MCPToolArguments(rawArguments).getObjectTypes(supportedObjectTypes)).getMessage(), is(expectedMessage));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStringArgumentCases")
    void assertGetStringArgument(final String name, final Map<String, Object> rawArguments, final String expectedValue) {
        assertThat(new MCPToolArguments(rawArguments).getStringArgument("name"), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getIntegerArgumentCases")
    void assertGetIntegerArgument(final String name, final Map<String, Object> rawArguments, final int defaultValue, final int expectedValue) {
        assertThat(new MCPToolArguments(rawArguments).getIntegerArgument("limit", defaultValue), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getBooleanArgumentCases")
    void assertGetBooleanArgument(final String name, final Map<String, Object> rawArguments, final boolean defaultValue, final boolean expectedValue) {
        assertThat(new MCPToolArguments(rawArguments).getBooleanArgument("enabled", defaultValue), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStringCollectionArgumentCases")
    void assertGetStringCollectionArgument(final String name, final Map<String, Object> rawArguments, final List<String> expectedValues) {
        assertThat(new MCPToolArguments(rawArguments).getStringCollectionArgument("steps"), is(expectedValues));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMapArgumentCases")
    void assertGetMapArgument(final String name, final Map<String, Object> rawArguments, final Map<String, String> expectedValues) {
        assertThat(new MCPToolArguments(rawArguments).getMapArgument("props"), is(expectedValues));
    }
    
    private static Stream<Arguments> getObjectTypesCases() {
        return Stream.of(
                Arguments.of("missing object types", Map.of(), Set.of(SupportedMCPMetadataObjectType.TABLE), List.of()),
                Arguments.of("empty object types", Map.of("object_types", List.of()), Set.of(SupportedMCPMetadataObjectType.TABLE), List.of()),
                Arguments.of("normalized object types", Map.of("object_types", List.of("table", " VIEW ", "table")),
                        Set.of(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW),
                        List.of(SupportedMCPMetadataObjectType.TABLE, SupportedMCPMetadataObjectType.VIEW)));
    }
    
    private static Stream<Arguments> invalidObjectTypesCases() {
        return Stream.of(
                Arguments.of("non array object types", Map.of("object_types", "table"), Set.of(SupportedMCPMetadataObjectType.TABLE), "object_types must be an array."),
                Arguments.of("blank object type", Map.of("object_types", List.of("   ")), Set.of(SupportedMCPMetadataObjectType.TABLE), "object_types cannot contain blank values."),
                Arguments.of("unknown object type", Map.of("object_types", List.of("unknown")), Set.of(SupportedMCPMetadataObjectType.TABLE), "Unsupported object_types value `unknown`."),
                Arguments.of("unsupported object type", Map.of("object_types", List.of("sequence")), Set.of(SupportedMCPMetadataObjectType.TABLE),
                        "Unsupported object_types value `sequence`."));
    }
    
    private static Stream<Arguments> getStringArgumentCases() {
        return Stream.of(
                Arguments.of("missing string", Map.of(), ""),
                Arguments.of("trimmed string", Map.of("name", " foo_name "), "foo_name"),
                Arguments.of("number string", Map.of("name", 42), "42"));
    }
    
    private static Stream<Arguments> getIntegerArgumentCases() {
        return Stream.of(
                Arguments.of("missing integer", Map.of(), 10, 10),
                Arguments.of("number integer", Map.of("limit", 5), 10, 5),
                Arguments.of("parsed integer", Map.of("limit", " 20 "), 10, 20),
                Arguments.of("blank integer", Map.of("limit", "   "), 10, 10),
                Arguments.of("invalid integer", Map.of("limit", "foo"), 10, 10));
    }
    
    private static Stream<Arguments> getBooleanArgumentCases() {
        return Stream.of(
                Arguments.of("missing boolean", Map.of(), true, true),
                Arguments.of("boolean literal", Map.of("enabled", false), true, false),
                Arguments.of("parsed boolean", Map.of("enabled", " true "), false, true),
                Arguments.of("blank boolean", Map.of("enabled", "   "), true, true));
    }
    
    private static Stream<Arguments> getStringCollectionArgumentCases() {
        return Stream.of(
                Arguments.of("missing collection", Map.of(), List.of()),
                Arguments.of("normalized collection", Map.of("steps", List.of(" ddl ", "", "rule_distsql")), List.of("ddl", "rule_distsql")));
    }
    
    private static Stream<Arguments> getMapArgumentCases() {
        return Stream.of(
                Arguments.of("missing map", Map.of(), Map.of()),
                Arguments.of("map argument", Map.of("props", Map.of("aes-key-value", "123456", "digest-algorithm-name", "SHA-1")),
                        Map.of("aes-key-value", "123456", "digest-algorithm-name", "SHA-1")),
                Arguments.of("entry list argument", Map.of("props", List.of(" aes-key-value = 123456 ", "digest-algorithm-name=SHA-1")),
                        Map.of("aes-key-value", "123456", "digest-algorithm-name", "SHA-1")));
    }
}
