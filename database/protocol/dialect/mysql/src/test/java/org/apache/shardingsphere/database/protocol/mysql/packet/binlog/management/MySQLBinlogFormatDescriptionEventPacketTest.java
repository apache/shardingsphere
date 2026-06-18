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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.management;

import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.charset.StandardCharsets;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogFormatDescriptionEventPacketTest {
    
    private static final int BINLOG_VERSION = 4;
    
    private static final int CREATE_TIMESTAMP = 1234567890;
    
    private static final int EVENT_HEADER_LENGTH = 19;
    
    private static final int FORMAT_DESCRIPTION_EVENT_LENGTH = 95;
    
    private static final int TYPE_HEADER_LENGTH = 14;
    
    private static final int TYPE_HEADER_REMAIN_LENGTH = 23;
    
    private static final byte[] MYSQL_SERVER_VERSION = "5.7.14-log0000000000000000000000000000000000000000".getBytes(StandardCharsets.UTF_8);
    
    private static final String BINLOG_VERSION_ERROR_MESSAGE = "Binlog version of FORMAT_DESCRIPTION_EVENT should always 4";
    
    private static final String EVENT_HEADER_LENGTH_ERROR_MESSAGE = "Length of the Binlog Event Header should always be 19.";
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNewParameters")
    void assertNew(final String name, final int checksumAlgorithmFlag, final int expectedChecksumSkipReservedCount) {
        mockPayloadForValidCase(checksumAlgorithmFlag);
        MySQLBinlogFormatDescriptionEventPacket actual = new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload);
        assertThat(actual.getBinlogVersion(), is(BINLOG_VERSION));
        assertThat(actual.getMysqlServerVersion(), is(MYSQL_SERVER_VERSION));
        assertThat(actual.getCreateTimestamp(), is(CREATE_TIMESTAMP));
        assertThat(actual.getEventHeaderLength(), is(EVENT_HEADER_LENGTH));
        assertThat(actual.getBinlogEventHeader(), is(binlogEventHeader));
        verify(payload).skipReserved(TYPE_HEADER_LENGTH);
        verify(payload).skipReserved(TYPE_HEADER_REMAIN_LENGTH);
        verify(payload, times(expectedChecksumSkipReservedCount)).skipReserved(4);
    }
    
    @Test
    void assertNewWithInvalidBinlogVersion() {
        when(payload.readInt2()).thenReturn(3);
        assertThat(assertThrows(IllegalArgumentException.class, () -> new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload)).getMessage(), is(BINLOG_VERSION_ERROR_MESSAGE));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNewWithInvalidEventHeaderLengthParameters")
    void assertNewWithInvalidEventHeaderLength(final String name, final int invalidEventHeaderLength) {
        when(payload.readInt2()).thenReturn(BINLOG_VERSION);
        when(payload.readStringFixByBytes(50)).thenReturn(MYSQL_SERVER_VERSION);
        when(payload.readInt4()).thenReturn(CREATE_TIMESTAMP);
        when(payload.readInt1()).thenReturn(invalidEventHeaderLength);
        IllegalArgumentException actual = assertThrows(IllegalArgumentException.class, () -> new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload));
        assertThat(actual.getMessage(), is(EVENT_HEADER_LENGTH_ERROR_MESSAGE));
    }
    
    @Test
    void assertWrite() {
        mockPayloadForValidCase(0);
        MySQLBinlogEventHeader eventHeader = new MySQLBinlogEventHeader(1, 2, 3, 4, 5, 6, 0);
        MySQLBinlogFormatDescriptionEventPacket packet = new MySQLBinlogFormatDescriptionEventPacket(eventHeader, payload);
        packet.write(payload);
        verify(payload).writeInt4(1);
        verify(payload).writeInt1(2);
        verify(payload).writeInt4(3);
        verify(payload).writeInt4(4);
        verify(payload).writeInt4(5);
        verify(payload).writeInt2(6);
    }
    
    private void mockPayloadForValidCase(final int checksumAlgorithmFlag) {
        when(payload.readInt2()).thenReturn(BINLOG_VERSION);
        when(payload.readStringFixByBytes(50)).thenReturn(MYSQL_SERVER_VERSION);
        when(payload.readInt4()).thenReturn(CREATE_TIMESTAMP);
        when(payload.readInt1()).thenReturn(EVENT_HEADER_LENGTH, FORMAT_DESCRIPTION_EVENT_LENGTH, checksumAlgorithmFlag);
    }
    
    private static Stream<Arguments> assertNewParameters() {
        return Stream.of(
                Arguments.of("checksum algorithm is crc32", 1, 1),
                Arguments.of("checksum algorithm is off", 0, 0),
                Arguments.of("checksum algorithm is unsupported", 2, 0));
    }
    
    private static Stream<Arguments> assertNewWithInvalidEventHeaderLengthParameters() {
        return Stream.of(
                Arguments.of("event header length is less than 19", 18),
                Arguments.of("event header length is greater than 19", 20),
                Arguments.of("event header length is zero", 0));
    }
}
