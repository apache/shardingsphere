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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.codec;

import io.netty.buffer.ByteBuf;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.EventTypes;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.TableMapEventPacket;
import org.apache.shardingsphere.shardingscaling.utils.ReflectionUtil;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLBinlogEventPacketDecoderTest {
    
    @Mock
    private ByteBuf byteBuf;
    
    private BinlogContext binlogContext;
    
    private MySQLBinlogEventPacketDecoder binlogEventPacketDecoder;
    
    @Before
    public void setUp() throws Exception {
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
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.ROTATE_EVENT);
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getFileName(), is(""));
    }
    
    @Test
    public void assertDecodeFormatDescriptionEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.FORMAT_DESCRIPTION_EVENT);
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getChecksumLength(), is(0));
    }
    
    @Test
    public void assertDecodeTableMapEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.TABLE_MAP_EVENT);
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getTableMap().size(), is(1));
        assertThat(binlogContext.getTableMap().get(0L), instanceOf(TableMapEventPacket.class));
    }
    
    @Test
    public void assertDecodeWriteRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.WRITE_ROWS_EVENT_V2);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, new TableMapEventPacket());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(WriteRowsEvent.class));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.UPDATE_ROWS_EVENT_V2);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, new TableMapEventPacket());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(UpdateRowsEvent.class));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        when(byteBuf.readUnsignedByte()).thenReturn((short) 0, EventTypes.DELETE_ROWS_EVENT_V2);
        when(byteBuf.readUnsignedShortLE()).thenReturn(2);
        binlogContext.getTableMap().put(0L, new TableMapEventPacket());
        List<Object> decodedEvents = new ArrayList<>();
        binlogEventPacketDecoder.decode(null, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(DeleteRowsEvent.class));
    }
}
