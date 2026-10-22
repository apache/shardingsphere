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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.apache.shardingsphere.database.protocol.firebird.payload.FirebirdPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.Mockito.inOrder;

@ExtendWith(MockitoExtension.class)
class FirebirdTimestampTZBinaryProtocolValueTest {
    
    @Mock
    private FirebirdPacketPayload payload;
    
    @ParameterizedTest(name = "{2}: {3}")
    @CsvSource({
            "60310, 801301234, 1559, 2024-01-02T00:15:30.123400+02:00",
            "60617, 234000000, 65361, 2024-11-03T01:30:00-05:00",
            "60617, 234000000, -175, 2024-11-03T01:30:00-05:00"
    })
    void assertRead(final int encodedDate, final int encodedTime, final int encodedTimeZone, final String expected) {
        ByteBuf buffer = Unpooled.buffer();
        try {
            buffer.writeInt(encodedDate).writeInt(encodedTime).writeInt(encodedTimeZone).writeInt(0x12345678);
            FirebirdPacketPayload actualPayload = new FirebirdPacketPayload(buffer, StandardCharsets.UTF_8);
            Object actual = new FirebirdTimestampTZBinaryProtocolValue().read(actualPayload);
            assertThat(actual, is(OffsetDateTime.parse(expected)));
            assertThat(buffer.readerIndex(), is(12));
            assertThat(actualPayload.readInt4(), is(0x12345678));
        } finally {
            buffer.release();
        }
    }
    
    @ParameterizedTest(name = "{0}")
    @CsvSource({
            "2024-01-01T12:00:00Z, 60310, 432000000, 1439",
            "2024-01-01T12:00:00+02:00, 60310, 360000000, 1559",
            "2024-01-01T12:00:00-05:00, 60310, 612000000, 1139",
            "2024-01-01T12:00:00+05:30, 60310, 234000000, 1769",
            "2024-01-01T01:00:00+08:00, 60309, 612000000, 1919",
            "2024-01-01T23:30:00-05:00, 60311, 162000000, 1139",
            "2024-01-02T00:15:30.123400+02:00, 60310, 801301234, 1559",
            "2024-03-10T02:30:00Z, 60379, 90000000, 1439",
            "2024-11-03T01:30:00-05:00, 60617, 234000000, 1139"
    })
    void assertWrite(final String value, final int encodedDate, final int encodedTime, final int encodedTimeZone) {
        new FirebirdTimestampTZBinaryProtocolValue().write(payload, OffsetDateTime.parse(value));
        InOrder ordered = inOrder(payload);
        ordered.verify(payload).writeInt4(encodedDate);
        ordered.verify(payload).writeInt4(encodedTime);
        ordered.verify(payload).writeInt4(encodedTimeZone);
    }
    
    @Test
    void assertGetLength() {
        assertThat(new FirebirdTimestampTZBinaryProtocolValue().getLength(payload), is(12));
    }
}
