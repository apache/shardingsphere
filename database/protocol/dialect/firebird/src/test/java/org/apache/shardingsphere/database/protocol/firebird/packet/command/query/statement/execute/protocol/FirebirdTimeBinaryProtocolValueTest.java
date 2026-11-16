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

package org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol;

import org.apache.shardingsphere.database.protocol.firebird.packet.command.query.statement.execute.protocol.util.FirebirdDateTimeUtils;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.sql.Time;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdTimeBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        LocalDateTime dateTime = LocalDateTime.of(1970, 1, 1, 12, 0, 0);
        int encoded = new FirebirdDateTimeUtils(dateTime).getEncodedTime();
        when(payload.readInt4()).thenReturn(encoded);
        assertThat(new FirebirdTimeBinaryProtocolValue().read(payload), is(FirebirdDateTimeUtils.getTime(encoded)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final int expected) {
        new FirebirdTimeBinaryProtocolValue().write(payload, value);
        verify(payload).writeInt4(expected);
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("legacy-time", Time.valueOf(LocalTime.of(12, 0, 0)), 432_000_000),
                Arguments.of("midnight", LocalTime.MIDNIGHT, 0),
                Arguments.of("whole-second", LocalTime.of(12, 34, 56), 452_960_000),
                Arguments.of("fractional-time", LocalTime.of(12, 34, 56, 123400000), 452_961_234),
                Arguments.of("midnight-fraction", LocalTime.of(0, 0, 0, 100000), 1));
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdTimeBinaryProtocolValue().getLength(payload), is(4));
    }
}
