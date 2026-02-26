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
import java.sql.Timestamp;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLTimestampValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseSuccessArguments")
    void assertParse(final String name, final String input, final Timestamp expected) {
        assertThat(new PostgreSQLTimestampValueParser().parse(input), is(expected));
    }
    
    @Test
    void assertParseWithTimestampUtilsException() {
        assertThat(assertThrows(SQLWrapperException.class, () -> new PostgreSQLTimestampValueParser().parse("bad")).getCause(), isA(SQLException.class));
    }
    
    private static Stream<Arguments> parseSuccessArguments() {
        return Stream.of(
                Arguments.of("date and short time", "20211012 2323", Timestamp.valueOf("2021-10-12 23:23:00")),
                Arguments.of("date and time", "2021-10-12 23:23:23", Timestamp.valueOf("2021-10-12 23:23:23")),
                Arguments.of("date and nanos", "2021-10-12 23:23:23.123456789", Timestamp.valueOf("2021-10-12 23:23:23.123456789")),
                Arguments.of("timestamp with T and offset", "2021-10-12T23:23:23.123+08", Timestamp.valueOf("2021-10-12 23:23:23.123")),
                Arguments.of("timestamp with extended offset", "2021-10-12 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                Arguments.of("timestamp with compact date", "20211012 23:23:23.12345+0800", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                Arguments.of("timestamp with short date", "211012 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                Arguments.of("timestamp with slash date", "10/12/21 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                Arguments.of("timestamp with positive zone spacing", "2021-10-12 23:23:23.123456 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")),
                Arguments.of("timestamp with negative zone spacing", "2021-10-12 23:23:23.123456 -08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")),
                Arguments.of("single-digit month and day", "2021-3-3 23:23:23.123456", Timestamp.valueOf("2021-03-03 23:23:23.123456")),
                Arguments.of("positive infinity fallback", "infinity", new Timestamp(9223372036825200000L)),
                Arguments.of("negative infinity fallback", "-infinity", new Timestamp(-9223372036832400000L)));
    }
}
