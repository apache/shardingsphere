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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog;

import info.avalon566.shardingscaling.sync.mysql.binlog.BinlogContext;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.Serializable;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class RowsEventTest {
    
    private BinlogEventHeader binlogEventHeader;
    
    private BinlogContext binlogContext;
    
    @Before
    public void setUp() {
        binlogContext = new BinlogContext();
        binlogContext.setTableMap(new HashMap<Long, TableMapEvent>());
        binlogContext.getTableMap().put(0L, new TableMapEvent());
        binlogEventHeader = new BinlogEventHeader();
    }
    
    @Test
    public void assertParsePostHeader() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        ByteBuf postHeader = mock(ByteBuf.class);
        when(postHeader.readByte()).thenReturn((byte) 0x01);
        when(postHeader.readUnsignedShortLE()).thenReturn(2);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePostHeader(postHeader);
        assertThat(actual.getTableId(), is(0x010101010101L));
        assertThat(actual.getFlags(), is(2));
        verify(postHeader).readBytes(ArgumentMatchers.any(byte[].class), ArgumentMatchers.eq(0), ArgumentMatchers.eq(0));
    }
    
    @Test
    public void assertParsePayloadWithWriteNullRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(1);
        byteBuf.writeByte(1);
        byteBuf.writeByte(1);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        BitSet expectedBitSet = new BitSet(1);
        expectedBitSet.set(0);
        assertThat(actual.getColumnsPresentBitmap(), is(expectedBitSet));
        assertColumnValue(actual.getColumnValues1(), null);
    }
    
    @Test
    public void assertParsePayloadWithUpdateNullRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[] {1, 1, 1, 1, 1});
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        BitSet expectedBitSet = new BitSet(1);
        expectedBitSet.set(0);
        assertThat(actual.getColumnsPresentBitmap(), is(expectedBitSet));
        assertThat(actual.getColumnsPresentBitmap2(), is(expectedBitSet));
        assertColumnValue(actual.getColumnValues1(), null);
        assertColumnValue(actual.getColumnValues2(), null);
    }
    
    @Test
    public void assertParsePayloadWithWriteLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[] {1, 0, 0});
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getColumnValues1(), 2147483648L);
    }
    
    @Test
    public void assertParsePayloadWithUpdateLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[] {1, 0, 0, 0});
        byteBuf.writeIntLE(Integer.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getColumnValues1(), 2147483647L);
        assertColumnValue(actual.getColumnValues2(), 2147483648L);
    }
    
    @Test
    public void assertParsePayloadWithWriteTinyRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TINY);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[] {1, 0, 0});
        byteBuf.writeByte(0x80);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getColumnValues1(), (short) 0x80);
    }
    
    @Test
    public void assertParsePayloadWithUpdateTinyRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TINY);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[] {1, 0, 0, 0});
        byteBuf.writeByte(0x79);
        byteBuf.writeByte(0);
        byteBuf.writeByte(0x80);
        RowsEvent actual = new RowsEvent(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getColumnValues1(), (short) 0x79);
        assertColumnValue(actual.getColumnValues2(), (short) 0x80);
    }
    
    private void assertColumnValue(final List<Serializable[]> actual, final Serializable value) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).length, is(1));
        assertThat(actual.get(0)[0], is(value));
    }
    
    private void mockTableMapEvent(final int meta, final int columnType) {
        TableMapEvent result = mock(TableMapEvent.class);
        ColumnDef longColumnDef = new ColumnDef();
        longColumnDef.setMeta(meta);
        longColumnDef.setType(columnType);
        when(result.getColumnDefs()).thenReturn(new ColumnDef[] {longColumnDef});
        binlogContext.getTableMap().put(0L, result);
    }
}

