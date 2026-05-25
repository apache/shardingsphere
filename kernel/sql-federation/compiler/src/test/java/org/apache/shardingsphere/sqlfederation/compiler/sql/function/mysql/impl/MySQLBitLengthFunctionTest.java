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

class MySQLBitLengthFunctionTest {
    
    @Test
    void assertConstructor() {
        assertDoesNotThrow(MySQLBitLengthFunction::new);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bitLengthTestCases")
    void assertBitLength(final String name, final Object input, final Object expected) {
        assertThat(MySQLBitLengthFunction.bitLength(input), is(expected));
    }
    
    private static Stream<Arguments> bitLengthTestCases() {
        return Stream.of(
                Arguments.of("null input returns null", null, null),
                Arguments.of("empty string returns zero", "", 0L),
                Arguments.of("ASCII string", "abc", 24L),
                Arguments.of("multi-byte UTF-8 string", "中文", 48L),
                Arguments.of("non-empty byte array", new byte[]{1, 2, 3}, 24L),
                Arguments.of("empty byte array", new byte[0], 0L));
    }
}
