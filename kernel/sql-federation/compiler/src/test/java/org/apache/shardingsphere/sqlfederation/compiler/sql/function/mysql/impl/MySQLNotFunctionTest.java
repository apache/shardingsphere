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

import org.apache.calcite.sql.SqlSyntax;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class MySQLNotFunctionTest {
    
    @Test
    void assertPrefixSyntax() {
        assertThat(new MySQLNotFunction().getSyntax(), is(SqlSyntax.PREFIX));
    }
    
    @ParameterizedTest(name = "not({0}) -> {1}")
    @MethodSource("notTestCases")
    void assertNotCoversAllBranches(final Object input, final Long expected) {
        assertThat(MySQLNotFunction.not(input), is(expected));
    }
    
    private static Stream<Arguments> notTestCases() {
        return Stream.of(
                Arguments.of(null, null),
                Arguments.of(0, 1L),
                Arguments.of(5, 0L),
                Arguments.of(true, 0L),
                Arguments.of(false, 1L),
                Arguments.of("foo", null));
    }
}
