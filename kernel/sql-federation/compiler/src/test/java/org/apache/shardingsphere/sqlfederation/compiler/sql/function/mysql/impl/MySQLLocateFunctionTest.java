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

package org.apache.shardingsphere.sqlfederation.compiler.sql.function.mysql.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MySQLLocateFunctionTest {
    
    @Test
    void assertConstructor() {
        assertDoesNotThrow(() -> new MySQLLocateFunction(false));
        assertDoesNotThrow(() -> new MySQLLocateFunction(true));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("locateTestCases")
    void assertLocate(final String name, final String substring, final String value, final Integer expected) {
        assertThat(MySQLLocateFunction.locate(substring, value), is(expected));
    }
    
    private static Stream<Arguments> locateTestCases() {
        return Stream.of(
                Arguments.of("null substring returns null", null, "abc", null),
                Arguments.of("null value returns null", "a", null, null),
                Arguments.of("substring at start", "a", "abc", 1),
                Arguments.of("substring at end", "c", "abc", 3),
                Arguments.of("substring not found", "x", "abc", 0),
                Arguments.of("empty substring returns 1", "", "abc", 1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("locateAtTestCases")
    void assertLocateAt(final String name, final String substring, final String value, final Integer position, final Integer expected) {
        assertThat(MySQLLocateFunction.locateAt(substring, value, position), is(expected));
    }
    
    private static Stream<Arguments> locateAtTestCases() {
        return Stream.of(
                Arguments.of("null substring returns null", null, "abc", 1, null),
                Arguments.of("null value returns null", "a", null, 1, null),
                Arguments.of("null position returns null", "a", "abc", null, null),
                Arguments.of("substring found after start position", "a", "abca", 2, 4),
                Arguments.of("zero start position returns 0", "a", "abc", 0, 0),
                Arguments.of("negative start position returns 0", "a", "abc", -1, 0),
                Arguments.of("start position past end returns 0", "a", "abc", 5, 0),
                Arguments.of("substring at exact start position", "c", "abc", 3, 3));
    }
}
