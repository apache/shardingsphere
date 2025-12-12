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

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.math.BigInteger;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MySQLBinFunctionTest {
    
    @ParameterizedTest(name = "bin({0}) -> {1}")
    @MethodSource("binTestCases")
    void assertBin(final Object input, final String expected) {
        assertThat(MySQLBinFunction.bin(input), is(expected));
    }
    
    private static Stream<Arguments> binTestCases() {
        String expectedNegative = "1111111111111111111111111111111111111111111111111111111111110100";
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of("", null),
                Arguments.of(5, "101"),
                Arguments.of(BigInteger.valueOf(2), "10"),
                Arguments.of("abc", "0"),
                Arguments.of("34xyz", "100010"),
                Arguments.of("-abc", "0"),
                Arguments.of("-", "0"),
                Arguments.of("-12abc", expectedNegative));
    }
}
