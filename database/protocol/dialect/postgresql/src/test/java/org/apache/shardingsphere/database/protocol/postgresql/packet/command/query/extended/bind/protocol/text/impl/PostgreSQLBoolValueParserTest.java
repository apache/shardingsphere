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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.text.impl;

import org.apache.shardingsphere.infra.exception.external.sql.type.wrapper.SQLWrapperException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.sql.SQLException;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLBoolValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseArguments")
    void assertParse(final String name, final String input, final boolean expected) {
        assertThat(new PostgreSQLBoolValueParser().parse(input), is(expected));
    }
    
    @Test
    void assertParseWithInvalidValue() {
        SQLException actual = (SQLException) assertThrows(SQLWrapperException.class, () -> new PostgreSQLBoolValueParser().parse("foo_value")).getCause();
        assertThat(actual.getSQLState(), is("22P02"));
    }
    
    private static Stream<Arguments> parseArguments() {
        return Stream.of(
                Arguments.of("true", "true", true),
                Arguments.of("short true", "t", true),
                Arguments.of("true prefix", "tru", true),
                Arguments.of("yes", "yes", true),
                Arguments.of("short yes", "y", true),
                Arguments.of("on", "on", true),
                Arguments.of("one", "1", true),
                Arguments.of("upper case true with spaces", " TRUE ", true),
                Arguments.of("false", "false", false),
                Arguments.of("short false", "f", false),
                Arguments.of("no", "no", false),
                Arguments.of("short no", "n", false),
                Arguments.of("off", "off", false),
                Arguments.of("short off", "of", false),
                Arguments.of("zero", "0", false));
    }
}
