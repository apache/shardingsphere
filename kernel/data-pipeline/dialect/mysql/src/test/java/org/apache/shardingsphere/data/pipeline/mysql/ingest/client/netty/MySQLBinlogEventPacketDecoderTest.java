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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.client.netty;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.apache.shardingsphere.data.pipeline.core.util.ReflectionUtil;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class MySQLBinlogEventPacketDecoderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private BinlogContext binlogContext;
    
    private MySQLBinlogEventPacketDecoder binlogEventPacketDecoder;
    
    private List<MySQLBinlogColumnDef> columnDefs;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        binlogEventPacketDecoder = new MySQLBinlogEventPacketDecoder(4, new ConcurrentHashMap<>());
        binlogContext = ReflectionUtil.getFieldValue(binlogEventPacketDecoder, "binlogContext", BinlogContext.class);
        when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        columnDefs = Lists.newArrayList(new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_LONGLONG), new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_LONG),
                new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_VARCHAR), new MySQLBinlogColumnDef(MySQLBinaryColumnType.MYSQL_TYPE_NEWDECIMAL));
    }
    
    @Test(expected = RuntimeException.class)
    public void assertDecodeWithPacketError() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(1);
        byteBuf.writeByte(255);
        byteBuf.writeBytes(new byte[20]);
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, null);
    }
    
    @Test
    public void assertDecodeRotateEvent() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(StringUtil.decodeHexDump("01000000000004010000002c0000000000000020001a9100000000000062696e6c6f672e3030303032394af65c24"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getFileName(), is("binlog.000029"));
    }
    
    @Test
    public void assertDecodeFormatDescriptionEvent() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(StringUtil.decodeHexDump("0200513aa8620f01000000790000000000000000000400382e302e323700000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "000000000013000d0008000000000400040000006100041a08000000080808020000000a0a0a2a2a001234000a280140081396"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(0));
        assertThat(binlogContext.getChecksumLength(), is(4));
    }
    
    @Test
    public void assertDecodeTableMapEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from binlog data, The first event used in Row Based Replication
        byteBuf.writeBytes(StringUtil.decodeHexDump("3400cb38a962130100000041000000be7d000000007b000000000001000464735f310009745f6f726465725f31000408030ff604c8000a020c0101000201e0ff0a9b3a"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(binlogContext.getTableMap().size(), is(1));
        assertThat(binlogContext.getTableMap().get(123L), instanceOf(MySQLBinlogTableMapEventPacket.class));
    }
    
    @Test
    public void assertDecodeWriteRowEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from INSERT INTO t_order(order_id, user_id, status, t_numeric) VALUES (1, 1, 'SUCCESS',null);
        byteBuf.writeBytes(StringUtil.decodeHexDump("30007a36a9621e0100000038000000bb7c000000007b00000000000100020004ff08010000000000000001000000075355434345535365eff9ff"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(WriteRowsEvent.class));
        WriteRowsEvent actual = (WriteRowsEvent) decodedEvents.get(0);
        assertThat(actual.getAfterRows().get(0), is(new Serializable[]{1L, 1, "SUCCESS", null}));
    }
    
    @Test
    public void assertDecodeUpdateRowEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from update t_order set status = 'updated' where order_id = 1;
        byteBuf.writeBytes(StringUtil.decodeHexDump("3500cb38a9621f010000004e0000000c7e000000007b00000000000100020004ffff08010000000000000001000000075355434345535308010000000000000001000000077570"
                + "6461746564e78cee6c"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(UpdateRowsEvent.class));
        UpdateRowsEvent actual = (UpdateRowsEvent) decodedEvents.get(0);
        assertThat(actual.getBeforeRows().get(0), is(new Serializable[]{1L, 1, "SUCCESS", null}));
        assertThat(actual.getAfterRows().get(0), is(new Serializable[]{1L, 1, "updated", null}));
    }
    
    @Test
    public void assertDecodeDeleteRowEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // delete from t_order where order_id = 1;
        byteBuf.writeBytes(StringUtil.decodeHexDump("51002a80a862200100000038000000c569000000007400000000000100020004ff0801000000000000000100000007535543434553531c9580c5"));
        binlogContext.getTableMap().put(116L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), instanceOf(DeleteRowsEvent.class));
        DeleteRowsEvent actual = (DeleteRowsEvent) decodedEvents.get(0);
        assertThat(actual.getBeforeRows().get(0), is(new Serializable[]{1L, 1, "SUCCESS", null}));
    }
    
    @Test
    public void assertBinlogEventHeaderIncomplete() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byte[] completeData = StringUtil.decodeHexDump("51002a80a862200100000038000000c569000000007400000000000100020004ff0801000000000000000100000007535543434553531c9580c5");
        byteBuf.writeBytes(completeData);
        // write incomplete event data
        byteBuf.writeBytes(StringUtil.decodeHexDump("3400"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogContext.getTableMap().put(116L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(byteBuf.readerIndex(), is(completeData.length));
    }
    
    @Test
    public void assertBinlogEventBodyIncomplete() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byte[] completeData = StringUtil.decodeHexDump("51002a80a862200100000038000000c569000000007400000000000100020004ff0801000000000000000100000007535543434553531c9580c5");
        byteBuf.writeBytes(completeData);
        byte[] notCompleteData = StringUtil.decodeHexDump("3400cb38a962130100000041000000be7d000000007b000000000001000464735f310009745f6f726465725f31000408030f");
        byteBuf.writeBytes(notCompleteData);
        List<Object> decodedEvents = new LinkedList<>();
        binlogContext.getTableMap().put(116L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(byteBuf.readerIndex(), is(completeData.length));
    }
}
