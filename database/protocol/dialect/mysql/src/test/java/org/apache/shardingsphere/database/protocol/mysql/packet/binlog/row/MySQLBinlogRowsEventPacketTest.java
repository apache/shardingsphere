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

package org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.stream.Stream;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogRowsEventPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("assertNewArguments")
    void assertNew(final String name, final int eventType, final int expectedSkipReservedInvocationCount, final boolean expectedHasColumnsPresentBitmap2) {
        when(binlogEventHeader.getEventType()).thenReturn(eventType);
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2, 4);
        when(payload.readIntLenenc()).thenReturn(1L);
        when(payload.readInt1()).thenReturn(0, 0);
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        assertThat(actual.getTableId(), is(1L));
        assertThat(actual.getFlags(), is(2));
        verify(payload, times(expectedSkipReservedInvocationCount)).skipReserved(2);
        assertThat(actual.getColumnNumber(), is(1));
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertThat(null != actual.getColumnsPresentBitmap2(), is(expectedHasColumnsPresentBitmap2));
    }
    
    @Test
    void assertReadRowsWithWriteRowsEvent() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.WRITE_ROWS_EVENT_V1.getValue());
        when(binlogEventHeader.getEventSize()).thenReturn(2);
        when(binlogEventHeader.getChecksumLength()).thenReturn(0);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readerIndex()).thenReturn(0, 0, 3, 3);
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2, 4);
        when(payload.readIntLenenc()).thenReturn(1L);
        when(payload.readInt1()).thenReturn(0, 0);
        when(payload.readInt8()).thenReturn(10L);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(Collections.singletonList(new MySQLBinlogColumnDef(MySQLBinaryColumnType.LONGLONG)));
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows2().size(), is(0));
        assertThat(actual.getRows().get(0)[0], is((Object) 10L));
    }
    
    @Test
    void assertReadRowsWithUpdateRowsEvent() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.UPDATE_ROWS_EVENT_V1.getValue());
        when(binlogEventHeader.getEventSize()).thenReturn(2);
        when(binlogEventHeader.getChecksumLength()).thenReturn(0);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.readerIndex()).thenReturn(0, 0, 3, 3);
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2, 4);
        when(payload.readIntLenenc()).thenReturn(1L);
        when(payload.readInt1()).thenReturn(0, 0, 1, 0);
        when(payload.readInt8()).thenReturn(11L);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(Collections.singletonList(new MySQLBinlogColumnDef(MySQLBinaryColumnType.LONGLONG)));
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows2().size(), is(1));
        assertNull(actual.getRows().get(0)[0]);
        assertThat(actual.getRows2().get(0)[0], is((Object) 11L));
    }
    
    @Test
    void assertWrite() {
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2, 4);
        when(payload.readIntLenenc()).thenReturn(1L);
        when(payload.readInt1()).thenReturn(0, 0);
        MySQLBinlogEventHeader eventHeader = new MySQLBinlogEventHeader(1, MySQLBinlogEventType.WRITE_ROWS_EVENT_V1.getValue(), 1, 1, 1, 1, 0);
        MySQLPacketPayload writePayload = mock(MySQLPacketPayload.class);
        new MySQLBinlogRowsEventPacket(eventHeader, payload).write(writePayload);
        verify(writePayload).writeInt1(MySQLBinlogEventType.WRITE_ROWS_EVENT_V1.getValue());
    }
    
    private static Stream<Arguments> assertNewArguments() {
        return Stream.of(
                Arguments.of("write rows event v2", MySQLBinlogEventType.WRITE_ROWS_EVENT_V2.getValue(), 1, false),
                Arguments.of("update rows event v2", MySQLBinlogEventType.UPDATE_ROWS_EVENT_V2.getValue(), 1, true),
                Arguments.of("delete rows event v2", MySQLBinlogEventType.DELETE_ROWS_EVENT_V2.getValue(), 1, false),
                Arguments.of("update rows event v1", MySQLBinlogEventType.UPDATE_ROWS_EVENT_V1.getValue(), 0, true),
                Arguments.of("write rows event v1", MySQLBinlogEventType.WRITE_ROWS_EVENT_V1.getValue(), 0, false));
    }
}
