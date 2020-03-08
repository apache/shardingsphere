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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import com.google.common.base.Strings;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class RowsEventPacketTest {
    
    private BinlogEventHeader binlogEventHeader;
    
    private BinlogContext binlogContext;
    
    @Before
    public void setUp() {
        binlogContext = new BinlogContext();
        binlogContext.setTableMap(new HashMap<>());
        binlogContext.getTableMap().put(0L, new TableMapEventPacket());
        binlogEventHeader = new BinlogEventHeader();
    }
    
    @Test
    public void assertParsePostHeader() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        ByteBuf postHeader = mock(ByteBuf.class);
        when(postHeader.readByte()).thenReturn((byte) 0x01);
        when(postHeader.readUnsignedShortLE()).thenReturn(2);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
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
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        BitSet expectedBitSet = new BitSet(1);
        expectedBitSet.set(0);
        assertThat(actual.getColumnsPresentBitmap(), is(expectedBitSet));
        assertColumnValue(actual.getRows1(), null);
    }
    
    @Test
    public void assertParsePayloadWithUpdateNullRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 1, 1, 1, 1});
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        BitSet expectedBitSet = new BitSet(1);
        expectedBitSet.set(0);
        assertThat(actual.getColumnsPresentBitmap(), is(expectedBitSet));
        assertThat(actual.getColumnsPresentBitmap2(), is(expectedBitSet));
        assertColumnValue(actual.getRows1(), null);
        assertColumnValue(actual.getRows2(), null);
    }
    
    @Test
    public void assertParsePayloadWithWriteLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Integer.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithUpdateLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeIntLE(Integer.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeIntLE(Integer.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Integer.MAX_VALUE);
        assertColumnValue(actual.getRows2(), Integer.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithWriteTinyRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TINY);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeByte(0x80);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (byte) 0x80);
    }
    
    @Test
    public void assertParsePayloadWithUpdateTinyRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TINY);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeByte(0x79);
        byteBuf.writeByte(0);
        byteBuf.writeByte(0x80);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (byte) 0x79);
        assertColumnValue(actual.getRows2(), (byte) 0x80);
    }
    
    @Test
    public void assertParsePayloadWithWriteShortRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_SHORT);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeShortLE(Short.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Short.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithUpdateShortRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_SHORT);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeShortLE(Short.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeShortLE(Short.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Short.MAX_VALUE);
        assertColumnValue(actual.getRows2(), Short.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithWriteInt24Row() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_INT24);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeMediumLE(0x800000);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), -0x800000);
    }
    
    @Test
    public void assertParsePayloadWithUpdateInt24Row() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_INT24);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeMediumLE(0x7f0000);
        byteBuf.writeByte(0);
        byteBuf.writeMediumLE(0x800000);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), 0x7f0000);
        assertColumnValue(actual.getRows2(), -0x800000);
    }
    
    @Test
    public void assertParsePayloadWithWriteLongLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONGLONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeLongLE(Long.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Long.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithUpdateLongLongRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_LONGLONG);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeLongLE(Long.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeLongLE(Long.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Long.MAX_VALUE);
        assertColumnValue(actual.getRows2(), Long.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithWriteFloatRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_FLOAT);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeFloatLE(Float.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Float.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithUpdateFloatRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_FLOAT);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeFloatLE(Float.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeFloatLE(Float.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Float.MAX_VALUE);
        assertColumnValue(actual.getRows2(), Float.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithWriteNewDecimalRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent((14 << 8) + 4, ColumnTypes.MYSQL_TYPE_NEWDECIMAL);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeBytes(ByteBufUtil.decodeHexDump("7EF204C72DFB2D"));
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), new BigDecimal("-1234567890.1234"));
    }
    
    @Test
    public void assertParsePayloadWithUpdateNewDecimalRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent((14 << 8) + 4, ColumnTypes.MYSQL_TYPE_NEWDECIMAL);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeBytes(ByteBufUtil.decodeHexDump("810DFB38D204D2"));
        byteBuf.writeByte(0);
        byteBuf.writeBytes(ByteBufUtil.decodeHexDump("7EF204C72DFB2D"));
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), new BigDecimal("1234567890.1234"));
        assertColumnValue(actual.getRows2(), new BigDecimal("-1234567890.1234"));
    }
    
    @Test
    public void assertParsePayloadWithWriteDoubleRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DOUBLE);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeDoubleLE(Double.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Double.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithUpdateDoubleRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DOUBLE);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeDoubleLE(Double.MAX_VALUE);
        byteBuf.writeByte(0);
        byteBuf.writeDoubleLE(Double.MIN_VALUE);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), Double.MAX_VALUE);
        assertColumnValue(actual.getRows2(), Double.MIN_VALUE);
    }
    
    @Test
    public void assertParsePayloadWithWriteTimestampRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIMESTAMP);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeIntLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateTimestampRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIMESTAMP);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeIntLE(1571214733);
        byteBuf.writeByte(0);
        byteBuf.writeIntLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2019-10-16 08:32:13");
        assertColumnValue(actual.getRows2(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteTimestamp2Row() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIMESTAMP2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeInt(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateTimestamp2Row() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIMESTAMP2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeInt(1571214733);
        byteBuf.writeByte(0);
        byteBuf.writeInt(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2019-10-16 08:32:13");
        assertColumnValue(actual.getRows2(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteDatetimeRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATETIME);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeLongLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateDatetimeRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATETIME);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeLongLE(20191017111500L);
        byteBuf.writeByte(0);
        byteBuf.writeLongLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2019-10-17 11:15:00");
        assertColumnValue(actual.getRows2(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteDatetime2Row() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATETIME2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        DataTypesCodec.writeInt5(0x8000000000L, byteBuf);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateDatetime2Row() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATETIME2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        DataTypesCodec.writeInt5(659887819813L, byteBuf);
        byteBuf.writeByte(0);
        DataTypesCodec.writeInt5(0x8000000000L, byteBuf);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2019-10-16 16:48:37");
        assertColumnValue(actual.getRows2(), "0000-00-00 00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteTimeRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIME);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeMediumLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateTimeRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIME);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeMediumLE(101531);
        byteBuf.writeByte(0);
        byteBuf.writeMediumLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "10:15:31");
        assertColumnValue(actual.getRows2(), "00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteTime2Row() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIME2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeMedium(0x800000);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateTime2Row() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_TIME2);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeMedium(8432343);
        byteBuf.writeByte(0);
        byteBuf.writeMedium(0x800000);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "10:43:23");
        assertColumnValue(actual.getRows2(), "00:00:00");
    }
    
    @Test
    public void assertParsePayloadWithWriteDateRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATE);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeMediumLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000-00-00");
    }
    
    @Test
    public void assertParsePayloadWithUpdateDateRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_DATE);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeMediumLE(1034065);
        byteBuf.writeByte(0);
        byteBuf.writeMediumLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2019-10-17");
        assertColumnValue(actual.getRows2(), "0000-00-00");
    }
    
    @Test
    public void assertParsePayloadWithWriteYearRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_YEAR);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "0000");
    }
    
    @Test
    public void assertParsePayloadWithUpdateYearRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(0, ColumnTypes.MYSQL_TYPE_YEAR);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeByte(255);
        byteBuf.writeByte(0);
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), "2155");
        assertColumnValue(actual.getRows2(), "0000");
    }
    
    @Test
    public void assertParsePayloadWithWriteBlobRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(1, ColumnTypes.MYSQL_TYPE_BLOB);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), new byte[0]);
    }
    
    @Test
    public void assertParsePayloadWithUpdateBlobRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(1, ColumnTypes.MYSQL_TYPE_BLOB);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byte[] value1 = new byte[255];
        byteBuf.writeByte(value1.length);
        byteBuf.writeBytes(value1);
        byteBuf.writeByte(0);
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value1);
        assertColumnValue(actual.getRows2(), new byte[0]);
    }
    
    @Test
    public void assertParsePayloadWithWriteVarCharRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(8, ColumnTypes.MYSQL_TYPE_VARCHAR);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        String value = Strings.repeat("1", 4);
        byteBuf.writeByte(value.length());
        byteBuf.writeBytes(value.getBytes());
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value);
    }
    
    @Test
    public void assertParsePayloadWithUpdateVarCharRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(300, ColumnTypes.MYSQL_TYPE_VARCHAR);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        String value1 = Strings.repeat("1", 299);
        byteBuf.writeShortLE(value1.length());
        byteBuf.writeBytes(value1.getBytes());
        byteBuf.writeByte(0);
        byteBuf.writeShortLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value1);
        assertColumnValue(actual.getRows2(), "");
    }
    
    @Test
    public void assertParsePayloadWithWriteVarStringRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(8, ColumnTypes.MYSQL_TYPE_VAR_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        String value = Strings.repeat("1", 4);
        byteBuf.writeByte(value.length());
        byteBuf.writeBytes(value.getBytes());
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value);
    }
    
    @Test
    public void assertParsePayloadWithUpdateVarStringRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(300, ColumnTypes.MYSQL_TYPE_VAR_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        String value1 = Strings.repeat("1", 299);
        byteBuf.writeShortLE(value1.length());
        byteBuf.writeBytes(value1.getBytes());
        byteBuf.writeByte(0);
        byteBuf.writeShortLE(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value1);
        assertColumnValue(actual.getRows2(), "");
    }
    
    @Test
    public void assertParsePayloadWithWriteEnumRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent((ColumnTypes.MYSQL_TYPE_ENUM << 8) | 1, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (short) 0);
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent((ColumnTypes.MYSQL_TYPE_ENUM << 8) | 2, ColumnTypes.MYSQL_TYPE_STRING);
        byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeShortLE(256);
        actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), 256);
    }
    
    @Test
    public void assertParsePayloadWithUpdateEnumRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent((ColumnTypes.MYSQL_TYPE_ENUM << 8) | 1, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeByte(255);
        byteBuf.writeByte(0);
        byteBuf.writeByte(0);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (short) 255);
        assertColumnValue(actual.getRows2(), (short) 0);
    }
    
    @Test
    public void assertParsePayloadWithWriteSetRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(ColumnTypes.MYSQL_TYPE_SET << 8, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        byteBuf.writeByte(-128);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (byte) -128);
    }
    
    @Test
    public void assertParsePayloadWithUpdateSetRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(ColumnTypes.MYSQL_TYPE_SET << 8, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        byteBuf.writeByte(127);
        byteBuf.writeByte(0);
        byteBuf.writeByte(-128);
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), (byte) 127);
        assertColumnValue(actual.getRows2(), (byte) -128);
    }
    
    @Test
    public void assertParsePayloadWithWriteStringRow() {
        binlogEventHeader.setTypeCode(EventTypes.WRITE_ROWS_EVENT_V2);
        mockTableMapEvent(ColumnTypes.MYSQL_TYPE_STRING << 8, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0});
        String value = "";
        byteBuf.writeByte(value.length());
        byteBuf.writeBytes(value.getBytes());
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value);
    }
    
    @Test
    public void assertParsePayloadWithUpdateStringRow() {
        binlogEventHeader.setTypeCode(EventTypes.UPDATE_ROWS_EVENT_V2);
        mockTableMapEvent(ColumnTypes.MYSQL_TYPE_STRING << 8, ColumnTypes.MYSQL_TYPE_STRING);
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(new byte[]{1, 0, 0, 0});
        String value1 = Strings.repeat("1", 127);
        byteBuf.writeByte(value1.length());
        byteBuf.writeBytes(value1.getBytes());
        byteBuf.writeByte(0);
        String value2 = "";
        byteBuf.writeByte(value2.length());
        byteBuf.writeBytes(value2.getBytes());
        RowsEventPacket actual = new RowsEventPacket(binlogEventHeader);
        actual.parsePayload(binlogContext, byteBuf);
        assertColumnValue(actual.getRows1(), value1);
        assertColumnValue(actual.getRows2(), value2);
    }
    
    private void assertColumnValue(final List<Serializable[]> actual, final Serializable value) {
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0).length, is(1));
        assertThat(actual.get(0)[0], is(value));
    }
    
    private void mockTableMapEvent(final int meta, final int columnType) {
        TableMapEventPacket result = mock(TableMapEventPacket.class);
        ColumnDef longColumnDef = new ColumnDef();
        longColumnDef.setMeta(meta);
        longColumnDef.setType(columnType);
        when(result.getColumnDefs()).thenReturn(new ColumnDef[]{longColumnDef});
        binlogContext.getTableMap().put(0L, result);
    }
}

