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

package org.apache.shardingsphere.distsql.parser.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class DistSQLStringUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("standardEscapesArguments")
    void assertReplaceStandardEscapes(final String input, final String expected) {
        String actual = DistSQLStringUtils.replaceStandardEscapes(input);
        assertThat(actual, is(expected));
    }
    
    private static Stream<Arguments> standardEscapesArguments() {
        return Stream.of(
                Arguments.of("foo", "foo"),
                Arguments.of("\\b", "\b"),
                Arguments.of("\\t", "\t"),
                Arguments.of("\\n", "\n"),
                Arguments.of("\\f", "\f"),
                Arguments.of("\\r", "\r"),
                Arguments.of("\\s", " "),
                Arguments.of("\\\"", "\""),
                Arguments.of("\\'", "'"),
                Arguments.of("\\x", "\\x"),
                Arguments.of("\\\\", "\\"),
                Arguments.of("\\", "\\"));
    }
}
