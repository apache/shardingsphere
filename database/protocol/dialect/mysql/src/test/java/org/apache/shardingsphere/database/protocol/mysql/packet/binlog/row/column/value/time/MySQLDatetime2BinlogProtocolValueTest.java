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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.time;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.stubbing.OngoingStubbing;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLDatetime2BinlogProtocolValueTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    private MySQLBinlogColumnDef columnDef;
    
    private MySQLDatetime2BinlogProtocolValue protocolValue;
    
    @BeforeEach
    void setUp() {
        columnDef = new MySQLBinlogColumnDef(MySQLBinaryColumnType.DATETIME2);
        protocolValue = new MySQLDatetime2BinlogProtocolValue();
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("readCases")
    void assertReadWithDatetime(final String name, final int columnMeta, final int[] readInt1Values, final Integer unsignedShortValue, final Integer unsignedMediumValue,
                                final LocalDateTime expectedDateTime) {
        columnDef.setColumnMeta(columnMeta);
        stubReadInt1(readInt1Values);
        if (null != unsignedShortValue || null != unsignedMediumValue) {
            when(payload.getByteBuf()).thenReturn(byteBuf);
        }
        if (null != unsignedShortValue) {
            when(byteBuf.readUnsignedShort()).thenReturn(unsignedShortValue);
        }
        if (null != unsignedMediumValue) {
            when(byteBuf.readUnsignedMedium()).thenReturn(unsignedMediumValue);
        }
        assertThat(protocolValue.read(columnDef, payload), is(Timestamp.valueOf(expectedDateTime)));
    }
    
    private void stubReadInt1(final int[] readInt1Values) {
        OngoingStubbing<Integer> stubbing = when(payload.readInt1()).thenReturn(readInt1Values[0]);
        for (int i = 1; i < readInt1Values.length; i++) {
            stubbing = stubbing.thenReturn(readInt1Values[i]);
        }
    }
    
    @Test
    void assertReadWithZeroDatetime() {
        assertThat(protocolValue.read(columnDef, payload), is(MySQLTimeValueUtils.DATETIME_OF_ZERO));
    }
    
    @Test
    void assertReadWithSignedZeroDatetime() {
        when(payload.readInt1()).thenReturn(0x80, 0x00, 0x00, 0x00, 0x00);
        assertThat(protocolValue.read(columnDef, payload), is(MySQLTimeValueUtils.DATETIME_OF_ZERO));
    }
    
    private static Stream<Arguments> readCases() {
        return Stream.of(
                Arguments.of("no_fraction", 0, new int[]{0xfe, 0xf3, 0xff, 0x7e, 0xfb}, null, null, LocalDateTime.of(9999, 12, 31, 23, 59, 59)),
                Arguments.of("one_byte_zero", 1, new int[]{0xfe, 0xf3, 0xff, 0x7e, 0xfb, 0x00}, null, null, LocalDateTime.of(9999, 12, 31, 23, 59, 59)),
                Arguments.of("two_bytes_999ms", 3, new int[]{0xfe, 0xf3, 0xff, 0x7e, 0xfb}, 9990, null, LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_000_000)),
                Arguments.of("three_bytes_999990us", 5, new int[]{0xfe, 0xf3, 0xff, 0x7e, 0xfb}, null, 999990, LocalDateTime.of(9999, 12, 31, 23, 59, 59, 999_990_000)));
    }
}
