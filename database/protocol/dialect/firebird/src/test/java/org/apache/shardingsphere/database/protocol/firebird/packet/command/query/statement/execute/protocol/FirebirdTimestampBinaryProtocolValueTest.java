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

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FirebirdTimestampBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @Test
    void assertRead() {
        LocalDateTime dateTime = LocalDateTime.parse("2024-01-01T12:00:00");
        int encodedDate = FirebirdDateTimeUtils.getEncodedDate(dateTime);
        int encodedTime = new FirebirdDateTimeUtils(dateTime).getEncodedTime();
        when(payload.readInt4()).thenReturn(encodedDate, encodedTime);
        assertThat(new FirebirdTimestampBinaryProtocolValue().read(payload), is(FirebirdDateTimeUtils.getDateTime(encodedDate, encodedTime)));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("writeArguments")
    void assertWrite(final String name, final Object value, final LocalDateTime expectedDateTime) {
        new FirebirdTimestampBinaryProtocolValue().write(payload, value);
        verify(payload).writeInt4(FirebirdDateTimeUtils.getEncodedDate(expectedDateTime));
        verify(payload).writeInt4(new FirebirdDateTimeUtils(expectedDateTime).getEncodedTime());
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdTimestampBinaryProtocolValue().getLength(payload), is(8));
    }
    
    private static Stream<Arguments> writeArguments() {
        return Stream.of(
                Arguments.of("timestamp", Timestamp.valueOf(LocalDateTime.of(2024, 1, 1, 12, 0)), LocalDateTime.of(2024, 1, 1, 12, 0)),
                Arguments.of("sub_millisecond_timestamp", Timestamp.valueOf(LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_900_000)), LocalDateTime.of(2024, 2, 29, 23, 59, 59, 999_900_000)),
                Arguments.of("java_util_date", new Date(Timestamp.valueOf(LocalDateTime.of(2024, 1, 3, 14, 0, 0, 123_000_000)).getTime()), LocalDateTime.of(2024, 1, 3, 14, 0, 0, 123_000_000)),
                Arguments.of("local_date_time", LocalDateTime.parse("2024-01-02T13:00:00"), LocalDateTime.parse("2024-01-02T13:00:00")));
    }
}
