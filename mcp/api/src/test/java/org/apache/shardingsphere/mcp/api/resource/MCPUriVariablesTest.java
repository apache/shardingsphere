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

package org.apache.shardingsphere.mcp.api.resource;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Map;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class MCPUriVariablesTest {
    
    @Test
    void assertGetVariable() {
        String actual = new MCPUriVariables(Map.of("foo_variable", "bar_value")).getVariable("foo_variable");
        assertThat(actual, is("bar_value"));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetVariableFailureCases")
    void assertGetVariableFailure(final String name, final MCPUriVariables uriVariables) {
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> uriVariables.getVariable("foo_variable"));
        assertThat(actual.getMessage(), is("Missing URI variable `foo_variable`."));
    }
    
    private static Stream<Arguments> assertGetVariableFailureCases() {
        return Stream.of(
                Arguments.of("missing variable", new MCPUriVariables(Map.of())),
                Arguments.of("empty variable", new MCPUriVariables(Map.of("foo_variable", ""))));
    }
}
