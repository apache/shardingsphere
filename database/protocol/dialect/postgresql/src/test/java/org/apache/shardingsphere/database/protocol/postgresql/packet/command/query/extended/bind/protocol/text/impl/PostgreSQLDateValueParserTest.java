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
import org.mockito.MockedConstruction;
import org.postgresql.jdbc.TimestampUtils;

import java.sql.Date;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class PostgreSQLDateValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseSuccessArguments")
    void assertParseWithDateTimeFormatter(final String name, final String input, final Date expected) {
        assertThat(new PostgreSQLDateValueParser().parse(input), is(expected));
    }
    
    @Test
    void assertParseWithTimestampUtilsFallback() throws SQLException {
        assertThat(new PostgreSQLDateValueParser().parse("infinity"), is(new TimestampUtils(false, null).toDate(null, "infinity")));
    }
    
    @Test
    void assertParseWithTimestampUtilsException() {
        try (MockedConstruction<TimestampUtils> ignored = mockConstruction(TimestampUtils.class, (mock, context) -> {
            try {
                when(mock.toDate(isNull(), anyString())).thenThrow(new SQLException("failed"));
            } catch (final SQLException ex) {
                throw new IllegalStateException(ex);
            }
        })) {
            assertThat(assertThrows(SQLWrapperException.class, () -> new PostgreSQLDateValueParser().parse("bad")).getCause(), isA(SQLException.class));
        }
    }
    
    private static Stream<Arguments> parseSuccessArguments() {
        return Stream.of(
                Arguments.of("date only", "2020-01-01", Date.valueOf(LocalDate.of(2020, 1, 1))),
                Arguments.of("date with short offset", "2020-01-01 +08", Date.valueOf(LocalDate.of(2020, 1, 1))),
                Arguments.of("date with offset", "2020-01-01 +08:00", Date.valueOf(LocalDate.of(2020, 1, 1))),
                Arguments.of("date with long offset", "2020-01-01 +08:00:00", Date.valueOf(LocalDate.of(2020, 1, 1))));
    }
}
