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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.util;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

class PostgreSQLTextBitUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertGetTextValueArguments")
    void assertGetTextValue(final String name, final Object jdbcBitValue, final String expectedTextValue) {
        assertThat(PostgreSQLTextBitUtils.getTextValue(jdbcBitValue), is(expectedTextValue));
    }
    
    private static Stream<Arguments> assertGetTextValueArguments() {
        return Stream.of(
                Arguments.of("null-input", null, null),
                Arguments.of("true-input", true, "1"),
                Arguments.of("false-input", false, "0"));
    }
}
