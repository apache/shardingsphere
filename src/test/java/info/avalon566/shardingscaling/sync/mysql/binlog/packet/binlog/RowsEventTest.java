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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentMatchers;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.BitSet;
import java.util.HashMap;

import info.avalon566.shardingscaling.sync.mysql.binlog.BinlogContext;

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
        assertThat(actual.getColumnValues1().size(), is(1));
        assertThat(actual.getColumnValues1().get(0).length, is(1));
        assertNull(actual.getColumnValues1().get(0)[0]);
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
        assertThat(actual.getColumnValues1().size(), is(1));
        assertThat(actual.getColumnValues1().get(0).length, is(1));
        assertNull(actual.getColumnValues1().get(0)[0]);
        assertThat(actual.getColumnValues2().size(), is(1));
        assertThat(actual.getColumnValues2().get(0).length, is(1));
        assertNull(actual.getColumnValues2().get(0)[0]);
    }
}

