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

import java.sql.Timestamp;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class PostgreSQLTimestampValueParserTest {
    
    @ParameterizedTest(name = "{0}")
    @ArgumentsSource(TestCaseArgumentsProvider.class)
    void assertGetLocalDateTimeNoExceptionOccurs(final String input, final Timestamp expected) {
        assertThat(new PostgreSQLTimestampValueParser().parse(input), is(expected));
    }
    
    private static final class TestCaseArgumentsProvider implements ArgumentsProvider {
        
        @Override
        public Stream<? extends Arguments> provideArguments(final ParameterDeclarations parameters, final ExtensionContext context) {
            return Stream.of(Arguments.of("20211012 2323", Timestamp.valueOf("2021-10-12 23:23:00")),
                    Arguments.of("20211012 23:23", Timestamp.valueOf("2021-10-12 23:23:00")),
                    Arguments.of("20211012 232323", Timestamp.valueOf("2021-10-12 23:23:23")),
                    Arguments.of("2021-10-12 23:23:23", Timestamp.valueOf("2021-10-12 23:23:23")),
                    Arguments.of("2021-10-12 23:23:23.1", Timestamp.valueOf("2021-10-12 23:23:23.1")),
                    Arguments.of("2021-10-12 23:23:23.12", Timestamp.valueOf("2021-10-12 23:23:23.12")),
                    Arguments.of("2021-10-12 23:23:23.123", Timestamp.valueOf("2021-10-12 23:23:23.123")),
                    Arguments.of("2021-10-12 23:23:23.123+08", Timestamp.valueOf("2021-10-12 23:23:23.123")),
                    Arguments.of("2021-10-12T23:23:23.123+08", Timestamp.valueOf("2021-10-12 23:23:23.123")),
                    Arguments.of("2021-10-12 23:23:23.12345", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("2021-10-12 23:23:23.12345+0800", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("20211012 23:23:23.12345+0800", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("2021-10-12 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("211012 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("10/12/21 23:23:23.12345+08:00:00", Timestamp.valueOf("2021-10-12 23:23:23.12345")),
                    Arguments.of("2021-10-12 23:23:23.123456", Timestamp.valueOf("2021-10-12 23:23:23.123456")),
                    Arguments.of("2021-10-12 23:23:23.1234567", Timestamp.valueOf("2021-10-12 23:23:23.1234567")),
                    Arguments.of("2021-10-12 23:23:23.12345678", Timestamp.valueOf("2021-10-12 23:23:23.12345678")),
                    Arguments.of("2021-10-12 23:23:23.123456789", Timestamp.valueOf("2021-10-12 23:23:23.123456789")),
                    Arguments.of("2021-10-12 23:23:23.123456 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")),
                    Arguments.of("2021-10-12 23:23:23.1234567 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.1234567")),
                    Arguments.of("2021-10-12 23:23:23.12345678 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.12345678")),
                    Arguments.of("2021-10-12 23:23:23.123456789+08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456789")),
                    Arguments.of("2021-10-12 23:23:23.123456789 +08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456789")),
                    Arguments.of("2021-10-12 23:23:23.123456 -08:00", Timestamp.valueOf("2021-10-12 23:23:23.123456")),
                    Arguments.of("2021-3-3 23:23:23.123456", Timestamp.valueOf("2021-03-03 23:23:23.123456")),
                    Arguments.of("infinity", new Timestamp(9223372036825200000L)),
                    Arguments.of("-infinity", new Timestamp(-9223372036832400000L)));
        }
    }
}
