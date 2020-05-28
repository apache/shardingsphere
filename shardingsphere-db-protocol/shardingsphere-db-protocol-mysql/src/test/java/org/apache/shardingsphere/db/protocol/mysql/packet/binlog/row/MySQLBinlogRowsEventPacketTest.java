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

package org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLBinlogRowsEventPacketTest {
    
    @Mock
    private MySQLPacketPayload payload;
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogEventHeader binlogEventHeader;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private List<MySQLBinlogColumnDef> columnDefs;
    
    @Before
    public void setUp() {
        mockColumnDefs();
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        when(payload.readInt6()).thenReturn(1L);
        when(payload.readInt2()).thenReturn(2);
        when(payload.readIntLenenc()).thenReturn(1L);
        when(payload.getByteBuf()).thenReturn(byteBuf);
        when(byteBuf.isReadable()).thenReturn(true, false);
    }
    
    private void mockColumnDefs() {
        columnDefs = new ArrayList<>();
        columnDefs.add(new MySQLBinlogColumnDef(MySQLColumnType.MYSQL_TYPE_LONGLONG));
    }
    
    @Test
    public void assertReadWriteRowV1WithoutNullValue() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.WRITE_ROWS_EVENTv1.getValue());
        when(payload.readInt8()).thenReturn(Long.MAX_VALUE);
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV1BeforeRows(actual);
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertNull(actual.getColumnsPresentBitmap2());
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().get(0)[0], is(Long.MAX_VALUE));
        assertTrue(actual.getRows2().isEmpty());
    }
    
    @Test
    public void assertReadWriteRowV1WithNullValue() {
        when(payload.readInt1()).thenReturn(0x01);
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.WRITE_ROWS_EVENTv1.getValue());
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV1BeforeRows(actual);
        assertTrue(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertNull(actual.getColumnsPresentBitmap2());
        assertThat(actual.getRows().size(), is(1));
        assertNull(actual.getRows().get(0)[0]);
        assertTrue(actual.getRows2().isEmpty());
    }
    
    @Test
    public void assertReadUpdateRowV1WithoutNullValue() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.UPDATE_ROWS_EVENTv1.getValue());
        when(payload.readInt8()).thenReturn(Long.MAX_VALUE, Long.MIN_VALUE);
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV1BeforeRows(actual);
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertFalse(actual.getColumnsPresentBitmap2().isNullParameter(0));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().get(0)[0], is(Long.MAX_VALUE));
        assertThat(actual.getRows2().size(), is(1));
        assertThat(actual.getRows2().get(0)[0], is(Long.MIN_VALUE));
    }
    
    private void assertBinlogRowsEventV1BeforeRows(final MySQLBinlogRowsEventPacket actual) {
        assertThat(actual.getTableId(), is(1L));
        assertThat(actual.getFlags(), is(2));
        verify(payload, never()).skipReserved(2);
        assertThat(actual.getColumnNumber(), is(1));
    }
    
    @Test
    public void assertReadWriteRowV2WithoutNullValue() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.WRITE_ROWS_EVENTv2.getValue());
        when(payload.readInt8()).thenReturn(Long.MAX_VALUE);
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV2BeforeRows(actual);
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertNull(actual.getColumnsPresentBitmap2());
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().get(0)[0], is(Long.MAX_VALUE));
        assertTrue(actual.getRows2().isEmpty());
    }
    
    @Test
    public void assertReadWriteRowV2WithNullValue() {
        when(payload.readInt1()).thenReturn(0x01);
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.WRITE_ROWS_EVENTv2.getValue());
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV2BeforeRows(actual);
        assertTrue(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertNull(actual.getColumnsPresentBitmap2());
        assertThat(actual.getRows().size(), is(1));
        assertNull(actual.getRows().get(0)[0]);
        assertTrue(actual.getRows2().isEmpty());
    }
    
    @Test
    public void assertReadUpdateRowV2WithoutNullValue() {
        when(binlogEventHeader.getEventType()).thenReturn(MySQLBinlogEventType.UPDATE_ROWS_EVENTv2.getValue());
        when(payload.readInt8()).thenReturn(Long.MAX_VALUE, Long.MIN_VALUE);
        MySQLBinlogRowsEventPacket actual = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        actual.readRows(tableMapEventPacket, payload);
        assertBinlogRowsEventV2BeforeRows(actual);
        assertFalse(actual.getColumnsPresentBitmap().isNullParameter(0));
        assertFalse(actual.getColumnsPresentBitmap2().isNullParameter(0));
        assertThat(actual.getRows().size(), is(1));
        assertThat(actual.getRows().get(0)[0], is(Long.MAX_VALUE));
        assertThat(actual.getRows2().size(), is(1));
        assertThat(actual.getRows2().get(0)[0], is(Long.MIN_VALUE));
    }
    
    private void assertBinlogRowsEventV2BeforeRows(final MySQLBinlogRowsEventPacket actual) {
        assertThat(actual.getTableId(), is(1L));
        assertThat(actual.getFlags(), is(2));
        verify(payload).skipReserved(0);
        assertThat(actual.getColumnNumber(), is(1));
    }
}
