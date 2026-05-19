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
    
    @ParameterizedTest(name = "locate({0}, {1}) -> {2}")
    @MethodSource("locateTestCases")
    void assertLocate(final String substring, final String value, final Integer expected) {
        assertThat(MySQLLocateFunction.locate(substring, value), is(expected));
    }
    
    private static Stream<Arguments> locateTestCases() {
        return Stream.of(
                Arguments.of(null, "abc", null),
                Arguments.of("a", null, null),
                Arguments.of("a", "abc", 1),
                Arguments.of("c", "abc", 3),
                Arguments.of("x", "abc", 0),
                Arguments.of("", "abc", 1));
    }
    
    @ParameterizedTest(name = "locateAt({0}, {1}, {2}) -> {3}")
    @MethodSource("locateAtTestCases")
    void assertLocateAt(final String substring, final String value, final Integer position, final Integer expected) {
        assertThat(MySQLLocateFunction.locateAt(substring, value, position), is(expected));
    }
    
    private static Stream<Arguments> locateAtTestCases() {
        return Stream.of(
                Arguments.of(null, "abc", 1, null),
                Arguments.of("a", null, 1, null),
                Arguments.of("a", "abc", null, null),
                Arguments.of("a", "abca", 2, 4),
                Arguments.of("a", "abc", 0, 0),
                Arguments.of("a", "abc", -1, 0),
                Arguments.of("a", "abc", 5, 0),
                Arguments.of("c", "abc", 3, 3));
    }
}
