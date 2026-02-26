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

import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PostgreSQLTimeValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("parseSuccessArguments")
    void assertParse(final String name, final String input, final LocalTime expected) {
        assertThat(new PostgreSQLTimeValueParser().parse(input), is(expected));
    }
    
    @Test
    void assertParseWithUnsupportedTimeFormat() {
        assertThat(assertThrows(UnsupportedSQLOperationException.class,
                () -> new PostgreSQLTimeValueParser().parse("bad")).getMessage(), is("Unsupported SQL operation: Unsupported time format: [bad]."));
    }
    
    private static Stream<Arguments> parseSuccessArguments() {
        return Stream.of(
                Arguments.of("compact hour minute", "2323", LocalTime.of(23, 23, 0)),
                Arguments.of("hour minute", "23:23", LocalTime.of(23, 23, 0)),
                Arguments.of("compact with second", "232323", LocalTime.of(23, 23, 23)),
                Arguments.of("with second", "23:23:23", LocalTime.of(23, 23, 23)),
                Arguments.of("single fraction", "23:23:23.1", LocalTime.of(23, 23, 23, 100_000_000)),
                Arguments.of("three fractions", "23:23:23.123", LocalTime.of(23, 23, 23, 123_000_000)),
                Arguments.of("with offset", "23:23:23.123+08", LocalTime.of(23, 23, 23, 123_000_000)),
                Arguments.of("with long fraction and compact zone", "23:23:23.12345+0800", LocalTime.of(23, 23, 23, 123_450_000)),
                Arguments.of("with long zone", "23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)),
                Arguments.of("with spacing and positive zone", "23:23:23.123456 +08:00", LocalTime.of(23, 23, 23, 123_456_000)),
                Arguments.of("with spacing and negative zone", "23:23:23.123456 -08:00", LocalTime.of(23, 23, 23, 123_456_000)),
                Arguments.of("with max nanos", "23:23:23.123456789", LocalTime.of(23, 23, 23, 123_456_789)));
    }
}
