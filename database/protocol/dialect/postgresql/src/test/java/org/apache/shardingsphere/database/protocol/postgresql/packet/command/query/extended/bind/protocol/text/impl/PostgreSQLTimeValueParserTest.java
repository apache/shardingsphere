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

import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;
import org.junit.jupiter.params.support.ParameterDeclarations;

import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLTimeValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertParse(final String input, final LocalTime expected) {
        assertThat(new PostgreSQLTimeValueParser().parse(input), is(expected));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(Arguments.of("2323", LocalTime.of(23, 23, 0)),
                    Arguments.of("23:23", LocalTime.of(23, 23, 0)),
                    Arguments.of("232323", LocalTime.of(23, 23, 23)),
                    Arguments.of("23:23:23", LocalTime.of(23, 23, 23)),
                    Arguments.of("23:23:23.1", LocalTime.of(23, 23, 23, 100_000_000)),
                    Arguments.of("23:23:23.12", LocalTime.of(23, 23, 23, 120_000_000)),
                    Arguments.of("23:23:23.123", LocalTime.of(23, 23, 23, 123_000_000)),
                    Arguments.of("23:23:23.123+08", LocalTime.of(23, 23, 23, 123_000_000)),
                    Arguments.of("23:23:23.123+08", LocalTime.of(23, 23, 23, 123_000_000)),
                    Arguments.of("23:23:23.12345", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.12345+0800", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.12345+0800", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.12345+08:00:00", LocalTime.of(23, 23, 23, 123_450_000)),
                    Arguments.of("23:23:23.123456", LocalTime.of(23, 23, 23, 123_456_000)),
                    Arguments.of("23:23:23.1234567", LocalTime.of(23, 23, 23, 123_456_700)),
                    Arguments.of("23:23:23.12345678", LocalTime.of(23, 23, 23, 123_456_780)),
                    Arguments.of("23:23:23.123456789", LocalTime.of(23, 23, 23, 123_456_789)),
                    Arguments.of("23:23:23.123456 +08:00", LocalTime.of(23, 23, 23, 123_456_000)),
                    Arguments.of("23:23:23.123456 -08:00", LocalTime.of(23, 23, 23, 123_456_000)),
                    Arguments.of("23:23:23.123456789 +08:00", LocalTime.of(23, 23, 23, 123_456_789)),
                    Arguments.of("23:23:23.123456", LocalTime.of(23, 23, 23, 123_456_000)));
        }
    }
}
