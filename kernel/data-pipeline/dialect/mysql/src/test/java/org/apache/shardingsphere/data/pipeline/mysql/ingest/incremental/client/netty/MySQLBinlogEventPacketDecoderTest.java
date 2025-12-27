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

package org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.client.netty;

import com.google.common.collect.Lists;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufAllocator;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.util.internal.StringUtil;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.MySQLBinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.PlaceholderBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.query.MySQLQueryBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLUpdateRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLWriteRowsBinlogEvent;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinaryColumnType;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.MySQLBinlogColumnDef;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLBinaryString;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MySQLBinlogEventPacketDecoderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ChannelHandlerContext channelHandlerContext;
    
    @Mock
    private MySQLBinlogTableMapEventPacket tableMapEventPacket;
    
    private MySQLBinlogContext binlogContext;
    
    private MySQLBinlogEventPacketDecoder binlogEventPacketDecoder;
    
    private List<MySQLBinlogColumnDef> columnDefs;
    
    @SuppressWarnings("CollectionWithoutInitialCapacity")
    @BeforeEach
    void setUp() throws ReflectiveOperationException {
        binlogEventPacketDecoder = new MySQLBinlogEventPacketDecoder(4, new ConcurrentHashMap<>(), true);
        binlogContext = (MySQLBinlogContext) Plugins.getMemberAccessor().get(MySQLBinlogEventPacketDecoder.class.getDeclaredField("binlogContext"), binlogEventPacketDecoder);
        lenient().when(channelHandlerContext.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get()).thenReturn(StandardCharsets.UTF_8);
        columnDefs = Lists.newArrayList(new MySQLBinlogColumnDef(MySQLBinaryColumnType.LONGLONG), new MySQLBinlogColumnDef(MySQLBinaryColumnType.LONG),
                new MySQLBinlogColumnDef(MySQLBinaryColumnType.VARCHAR), new MySQLBinlogColumnDef(MySQLBinaryColumnType.NEWDECIMAL));
    }
    
    @Test
    void assertDecodeWithStatusCode255() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(255);
        byteBuf.writeShortLE(123);
        byteBuf.writeByte(0);
        byteBuf.writeBytes("ABCDE".getBytes(StandardCharsets.UTF_8));
        byteBuf.writeBytes("errorMessage".getBytes(StandardCharsets.UTF_8));
        byteBuf.writeZero(MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH);
        assertThrows(PipelineInternalException.class, () -> binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, new LinkedList<>()));
    }
    
    @Test
    void assertDecodeWithIllegalStatusCode() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeByte(1);
        byteBuf.writeZero(MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH);
        assertThrows(IndexOutOfBoundsException.class, () -> binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, new LinkedList<>()));
    }
    
    @Test
    void assertDecodeRotateEvent() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(StringUtil.decodeHexDump("000000000004010000002c0000000000000020001a9100000000000062696e6c6f672e3030303032394af65c24"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertTrue(decodedEvents.isEmpty());
        assertThat(binlogContext.getFileName(), is("binlog.000029"));
    }
    
    @Test
    void assertDecodeFormatDescriptionEvent() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(StringUtil.decodeHexDump("00513aa8620f01000000790000000000000000000400382e302e323700000000000000000000000000000000000000000000000000000000000000000000000000000000000000"
                + "000000000013000d0008000000000400040000006100041a08000000080808020000000a0a0a2a2a001234000a280140081396"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertTrue(decodedEvents.isEmpty());
        assertThat(binlogContext.getChecksumLength(), is(4));
    }
    
    @Test
    void assertDecodeQueryEvent() {
        ByteBuf byteBuf = Unpooled.buffer();
        byteBuf.writeBytes(StringUtil.decodeHexDump("00f3e25665020100000087000000c2740f0a0400c9150000000000000400002d000000000000012000a045000000000603737464042d002d00e0000c0164735f3000116df40b00000"
                + "0000012ff0064735f300044524f50205441424c452060745f70726f76696e636560202f2a2067656e65726174656420627920736572766572202a2fcefe4ec6"));
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertFalse(decodedEvents.isEmpty());
        Object actual = decodedEvents.get(0);
        assertThat(actual, isA(MySQLQueryBinlogEvent.class));
        assertThat(((MySQLQueryBinlogEvent) actual).getTimestamp(), is(1700193011L));
        assertThat(((MySQLQueryBinlogEvent) actual).getPosition(), is(168785090L));
    }
    
    @Test
    void assertDecodeTableMapEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from binlog data, The first event used in Row Based Replication
        byteBuf.writeBytes(StringUtil.decodeHexDump("00cb38a962130100000041000000be7d000000007b000000000001000464735f310009745f6f726465725f31000408030ff604c8000a020c0101000201e0ff0a9b3a"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(binlogContext.getTableMap().size(), is(1));
        assertThat(binlogContext.getTableMap().get(123L), isA(MySQLBinlogTableMapEventPacket.class));
    }
    
    @Test
    void assertDecodeWriteRowEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from INSERT INTO t_order(order_id, user_id, status, t_numeric) VALUES (1, 1, 'SUCCESS',null);
        byteBuf.writeBytes(StringUtil.decodeHexDump("007a36a9621e0100000038000000bb7c000000007b00000000000100020004ff08010000000000000001000000075355434345535365eff9ff"));
        byteBuf.writeBytes(StringUtil.decodeHexDump("006acb656410010000001f000000fa29000000001643000000000000b13f8340"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        LinkedList<?> actualEventList = (LinkedList<?>) decodedEvents.get(0);
        assertThat(actualEventList.get(0), isA(MySQLWriteRowsBinlogEvent.class));
        MySQLWriteRowsBinlogEvent actual = (MySQLWriteRowsBinlogEvent) actualEventList.get(0);
        assertThat(actual.getAfterRows().get(0), is(new Serializable[]{1L, 1, new MySQLBinaryString("SUCCESS".getBytes()), null}));
    }
    
    @Test
    void assertDecodeUpdateRowEvent() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        // the hex data is from update t_order set status = 'updated' where order_id = 1;
        byteBuf.writeBytes(StringUtil.decodeHexDump("00cb38a9621f010000004e0000000c7e000000007b00000000000100020004ffff08010000000000000001000000075355434345535308010000000000000001000000077570"
                + "6461746564e78cee6c"));
        byteBuf.writeBytes(StringUtil.decodeHexDump("006acb656410010000001f000000fa29000000001643000000000000b13f8340"));
        binlogContext.getTableMap().put(123L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        LinkedList<?> actualEventList = (LinkedList<?>) decodedEvents.get(0);
        assertThat(actualEventList.get(0), isA(MySQLUpdateRowsBinlogEvent.class));
        MySQLUpdateRowsBinlogEvent actual = (MySQLUpdateRowsBinlogEvent) actualEventList.get(0);
        assertThat(actual.getBeforeRows().get(0), is(new Serializable[]{1L, 1, new MySQLBinaryString("SUCCESS".getBytes()), null}));
        assertThat(actual.getAfterRows().get(0), is(new Serializable[]{1L, 1, new MySQLBinaryString("updated".getBytes()), null}));
    }
    
    @Test
    void assertBinlogEventBodyIncomplete() {
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.buffer();
        byte[] completeData = StringUtil.decodeHexDump("002a80a862200100000038000000c569000000007400000000000100020004ff0801000000000000000100000007535543434553531c9580c5");
        byteBuf.writeBytes(completeData);
        byteBuf.writeBytes(StringUtil.decodeHexDump("006acb656410010000001f000000fa29000000001643000000000000b13f8340"));
        byte[] notCompleteData = StringUtil.decodeHexDump("00cb38a962130100000041000000be7d000000007b000000000001000464735f310009745f6f726465725f31000408030f");
        byteBuf.writeBytes(notCompleteData);
        List<Object> decodedEvents = new LinkedList<>();
        binlogContext.getTableMap().put(116L, tableMapEventPacket);
        when(tableMapEventPacket.getColumnDefs()).thenReturn(columnDefs);
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
    }
    
    @Test
    void assertDecodePlaceholderEventSkipRemainBytes() {
        binlogContext.setFileName("binlog.000001");
        ByteBuf byteBuf = createPlaceholderEventByteBuf(MySQLBinlogEventType.UNKNOWN_EVENT.getValue(), MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH + 11, 7);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), isA(PlaceholderBinlogEvent.class));
        assertThat(byteBuf.readableBytes(), is(0));
    }
    
    @Test
    void assertDecodePlaceholderEventWithoutRemainBytes() {
        binlogContext.setFileName("binlog.000002");
        ByteBuf byteBuf = createPlaceholderEventByteBuf(MySQLBinlogEventType.UNKNOWN_EVENT.getValue(), MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH, 0);
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), isA(PlaceholderBinlogEvent.class));
    }
    
    @Test
    void assertProcessBeginQueryWithTX() throws NoSuchFieldException, IllegalAccessException {
        List<MySQLBaseBinlogEvent> existingRecords = new LinkedList<>();
        existingRecords.add(new PlaceholderBinlogEvent("binlog.000003", 1L, 1L));
        Plugins.getMemberAccessor().set(MySQLBinlogEventPacketDecoder.class.getDeclaredField("records"), binlogEventPacketDecoder, existingRecords);
        ByteBuf byteBuf = createQueryEventByteBuf("BEGIN", binlogContext.getChecksumLength());
        List<Object> decodedEvents = new LinkedList<>();
        binlogEventPacketDecoder.decode(channelHandlerContext, byteBuf, decodedEvents);
        assertTrue(decodedEvents.isEmpty());
        @SuppressWarnings("unchecked")
        List<MySQLBaseBinlogEvent> records = (List<MySQLBaseBinlogEvent>) Plugins.getMemberAccessor().get(MySQLBinlogEventPacketDecoder.class.getDeclaredField("records"), binlogEventPacketDecoder);
        assertTrue(records.isEmpty());
        assertThat(records, not(existingRecords));
    }
    
    @Test
    void assertDecodeWithoutTXSkipBeginQuery() {
        MySQLBinlogEventPacketDecoder decoderWithoutTX = new MySQLBinlogEventPacketDecoder(4, new ConcurrentHashMap<>(), false);
        ByteBuf beginEvent = createQueryEventByteBuf("BEGIN", 4);
        List<Object> decodedEvents = new LinkedList<>();
        decoderWithoutTX.decode(channelHandlerContext, beginEvent, decodedEvents);
        assertTrue(decodedEvents.isEmpty());
        ByteBuf queryEvent = createQueryEventByteBuf("SELECT 1", 4);
        decoderWithoutTX.decode(channelHandlerContext, queryEvent, decodedEvents);
        assertThat(decodedEvents.size(), is(1));
        assertThat(decodedEvents.get(0), isA(MySQLQueryBinlogEvent.class));
    }
    
    @Test
    void assertDecodeFormatDescriptionEventWithZeroChecksumAndExtraBytes() {
        MySQLBinlogEventPacketDecoder decoderWithoutChecksum = new MySQLBinlogEventPacketDecoder(0, new ConcurrentHashMap<>(), true);
        ByteBuf byteBuf = createFormatDescriptionEventByteBuf(0, 4);
        decoderWithoutChecksum.decode(channelHandlerContext, byteBuf, new LinkedList<>());
        assertThat(byteBuf.readableBytes(), is(0));
        ByteBuf byteBufWithWarning = createFormatDescriptionEventByteBuf(0, 3);
        decoderWithoutChecksum.decode(channelHandlerContext, byteBufWithWarning, new LinkedList<>());
        assertThat(byteBufWithWarning.readableBytes(), is(0));
    }
    
    private ByteBuf createPlaceholderEventByteBuf(final int eventType, final int eventSize, final int bodyLength) {
        ByteBuf result = Unpooled.buffer();
        result.writeByte(0);
        result.writeIntLE(1);
        result.writeByte(eventType);
        result.writeIntLE(1);
        result.writeIntLE(eventSize);
        result.writeIntLE(0);
        result.writeShortLE(0);
        result.writeZero(bodyLength);
        result.writeZero(binlogContext.getChecksumLength());
        return result;
    }
    
    private ByteBuf createQueryEventByteBuf(final String sql, final int checksumLength) {
        ByteBuf result = Unpooled.buffer();
        byte[] databaseBytes = "db".getBytes(StandardCharsets.UTF_8);
        byte[] sqlBytes = sql.getBytes(StandardCharsets.UTF_8);
        int bodyLength = 4 + 4 + 1 + 2 + 2 + databaseBytes.length + 1 + sqlBytes.length;
        int eventSize = MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH + bodyLength + checksumLength;
        result.writeByte(0);
        result.writeIntLE(1);
        result.writeByte(MySQLBinlogEventType.QUERY_EVENT.getValue());
        result.writeIntLE(1);
        result.writeIntLE(eventSize);
        result.writeIntLE(0);
        result.writeShortLE(0);
        result.writeIntLE(1);
        result.writeIntLE(2);
        result.writeByte(databaseBytes.length);
        result.writeShortLE(0);
        result.writeShortLE(0);
        result.writeBytes(databaseBytes);
        result.writeByte(0);
        result.writeBytes(sqlBytes);
        result.writeZero(checksumLength);
        return result;
    }
    
    private ByteBuf createFormatDescriptionEventByteBuf(final int checksumLength, final int extraBytesLength) {
        ByteBuf result = Unpooled.buffer();
        int bodyLength = 2 + 50 + 4 + 1 + (MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() - 1) + 1 + 1 + extraBytesLength;
        int eventSize = MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH + bodyLength + checksumLength;
        result.writeByte(0);
        result.writeIntLE(1);
        result.writeByte(MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue());
        result.writeIntLE(1);
        result.writeIntLE(eventSize);
        result.writeIntLE(0);
        result.writeShortLE(0);
        result.writeShortLE(4);
        result.writeZero(50);
        result.writeIntLE(0);
        result.writeByte(MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH);
        result.writeZero(MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() - 1);
        result.writeByte(72);
        result.writeZero(0);
        result.writeByte(0);
        result.writeZero(extraBytesLength);
        result.writeZero(checksumLength);
        return result;
    }
}
