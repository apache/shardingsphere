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

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

class MySQLBitCountFunctionTest {
    
    @Test
    void assertConstructor() {
        assertDoesNotThrow(MySQLBitCountFunction::new);
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("bitCountTestCases")
    void assertBitCountCoversAllBranches(final String name, final Object input, final Object expected) {
        assertThat(MySQLBitCountFunction.bitCount(input), is(expected));
    }
    
    private static Stream<Arguments> bitCountTestCases() {
        return Stream.of(
                Arguments.of("null input", null, null),
                Arguments.of("byte array", new byte[]{(byte) 0b00001111}, 4L),
                Arguments.of("numeric text", "7", 3),
                Arguments.of("empty text", "", 0),
                Arguments.of("nonnumeric text", "abc", 0),
                Arguments.of("big integer", new BigInteger("15"), 4),
                Arguments.of("integer", 3, 2),
                Arguments.of("long", 1L, 1),
                Arguments.of("unsupported object", new Object(), 0));
    }
}
