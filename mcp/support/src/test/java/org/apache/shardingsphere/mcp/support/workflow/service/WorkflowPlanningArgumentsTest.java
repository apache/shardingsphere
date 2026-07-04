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

package org.apache.shardingsphere.mcp.support.workflow.service;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class WorkflowPlanningArgumentsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getStringArgumentCases")
    void assertGetStringArgument(final String name, final Map<String, Object> rawArguments, final String expectedValue) {
        assertThat(new WorkflowPlanningArguments(rawArguments).getStringArgument("name"), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getMapArgumentCases")
    void assertGetMapArgument(final String name, final Map<String, Object> rawArguments, final Map<String, String> expectedValue) {
        assertThat(new WorkflowPlanningArguments(rawArguments).getMapArgument("props"), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getAlgorithmPropertyMapArgumentCases")
    void assertGetAlgorithmPropertyMapArgument(final String name, final Map<String, Object> rawArguments, final Map<String, String> expectedValue) {
        assertThat(new WorkflowPlanningArguments(rawArguments).getAlgorithmPropertyMapArgument("props", "primary"), is(expectedValue));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("getSecretReferenceMapArgumentCases")
    void assertGetSecretReferenceMapArgument(final String name, final Map<String, Object> rawArguments, final boolean expectedMalformed) {
        assertThat(new WorkflowPlanningArguments(rawArguments).getSecretReferenceMapArgument("props").get("aes-key-value").isMalformed(), is(expectedMalformed));
    }
    
    private static Stream<Arguments> getStringArgumentCases() {
        return Stream.of(
                Arguments.of("missing string", Map.of(), ""),
                Arguments.of("trimmed string", Map.of("name", " foo_name "), "foo_name"),
                Arguments.of("number string", Map.of("name", 42), "42"));
    }
    
    private static Stream<Arguments> getMapArgumentCases() {
        return Stream.of(
                Arguments.of("missing map", Map.of(), Map.of()),
                Arguments.of("map argument", Map.of("props", Map.of("aes-key-value", "123456", "digest-algorithm-name", "SHA-1")),
                        Map.of("aes-key-value", "123456", "digest-algorithm-name", "SHA-1")));
    }
    
    private static Stream<Arguments> getAlgorithmPropertyMapArgumentCases() {
        return Stream.of(
                Arguments.of("secret reference object", Map.of("props", Map.of("aes-key-value", Map.of("secret_ref", "placeholder://secret-value-1"))),
                        Map.of("aes-key-value", "secret_reference:primary.aes-key-value")),
                Arguments.of("literal map argument", Map.of("props", Map.of("digest-algorithm-name", "SHA-256")), Map.of("digest-algorithm-name", "SHA-256")));
    }
    
    private static Stream<Arguments> getSecretReferenceMapArgumentCases() {
        return Stream.of(
                Arguments.of("valid secret reference", Map.of("props", Map.of("aes-key-value", Map.of("secret_ref", "placeholder://secret-value-1"))), false),
                Arguments.of("malformed secret reference", Map.of("props", Map.of("aes-key-value", Map.of("label", "placeholder"))), true));
    }
}
