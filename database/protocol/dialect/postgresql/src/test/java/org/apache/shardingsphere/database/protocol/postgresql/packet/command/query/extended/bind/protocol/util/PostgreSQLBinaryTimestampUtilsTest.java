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

import java.sql.Timestamp;
import java.util.stream.Stream;
import java.util.TimeZone;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class PostgreSQLBinaryTimestampUtilsTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertToPostgreSQLTimeArguments")
    void assertToPostgreSQLTime(final String name, final long epochSeconds, final int nanos, final long expectedPostgreSQLTime) {
        Timestamp timestamp = mock(Timestamp.class);
        when(timestamp.getNanos()).thenReturn(nanos);
        when(timestamp.getTime()).thenReturn(epochSeconds * 1000L + nanos / 1000000L - TimeZone.getDefault().getRawOffset());
        assertThat(PostgreSQLBinaryTimestampUtils.toPostgreSQLTime(timestamp), is(expectedPostgreSQLTime));
    }
    
    private static Stream<Arguments> assertToPostgreSQLTimeArguments() {
        return Stream.of(
                Arguments.of("modern-epoch", 0L, 0, -946684800000000L),
                Arguments.of("julian-without-year-adjustment", -12219292801L, 0, -13166841601000000L),
                Arguments.of("julian-with-year-adjustment", -14825808001L, 0, -15773270401000000L),
                Arguments.of("nanos-overflow-adjusts-second", 0L, 1000000000, -946684799000000L));
    }
}
