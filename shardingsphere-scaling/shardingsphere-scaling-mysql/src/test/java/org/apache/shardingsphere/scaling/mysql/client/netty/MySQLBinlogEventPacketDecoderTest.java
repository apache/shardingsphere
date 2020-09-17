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

package org.apache.shardingsphere.scaling.mysql.client.netty;

import io.netty.buffer.ByteBuf;

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.scaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.scaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLBinlogEventPacketDecoderTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private BinlogContext binlogContext;
    
    private MySQLBinlogEventPacketDecoder binlogEventPacketDecoder;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        binlogEventPacketDecoder = new MySQLBinlogEventPacketDecoder(4);
        binlogContext = ReflectionUtil.getFieldValueFromClass(binlogEventPacketDecoder, "binlogContext", BinlogContext.class);
    }
    
    @Test(expected = RuntimeException.class)
    public void assertDecodeWithPacketError() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 255);
        binlogEventPacketDecoder.decode(null, byteBuf, null);
    }
    
    @Test(expected = UnsupportedOperationException.class)
    public void assertDecodeWithReadError() {
        when(byteBuf.isReadable()).thenReturn(true);
        binlogEventPacketDecoder.decode(null, byteBuf, new ArrayList<>());
    }
    
    @Test
    public void assertDecodeRotateEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.ROTATE_EVENT.getValue());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getFileName(), is(""));
    }
    
    @Test
    public void assertDecodeFormatDescriptionEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue(), (short) 19);
        when(byteBuf.readUnsignedShortLE()).thenReturn(4);
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getChecksumLength(), is(4));
    }
    
    @Test
    public void assertDecodeTableMapEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.TABLE_MAP_EVENT.getValue(), (short) 0);
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getTableMap().size(), is(1));
        assertThat(binlogContext.getTableMap().get(0L), instanceOf(MySQLBinlogTableMapEventPacket.class));
    }
    
    @Test
    public void assertDecodeWriteRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.WRITE_ROWS_EVENTv2.getValue(), (short) 0);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(Collections.emptyList());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(WriteRowsEvent.class));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.UPDATE_ROWS_EVENTv2.getValue(), (short) 0);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(Collections.emptyList());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(UpdateRowsEvent.class));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, (short) 0, (short) MySQLBinlogEventType.DELETE_ROWS_EVENTv2.getValue(), (short) 0);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(Collections.emptyList());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(DeleteRowsEvent.class));
    }
}
