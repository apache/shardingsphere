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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.db.protocol.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogFormatDescriptionEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogRotateEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogRowsEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL binlog event packet decoder.
 */
@Slf4j
public final class MySQLBinlogEventPacketDecoder extends ByteToMessageDecoder {
    
    private final BinlogContext binlogContext;
    
    public MySQLBinlogEventPacketDecoder(final int checksumLength, final Map<Long, MySQLBinlogTableMapEventPacket> tableMap) {
        binlogContext = new BinlogContext(checksumLength, tableMap);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        // readable bytes must greater + seqId(1b) + statusCode(1b) + header-length(19b) +
        while (in.readableBytes() >= 2 + MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH) {
            in.markReaderIndex();
            MySQLPacketPayload payload = new MySQLPacketPayload(in, ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
            skipSequenceId(payload);
            checkError(payload);
            MySQLBinlogEventHeader binlogEventHeader = new MySQLBinlogEventHeader(payload, binlogContext.getChecksumLength());
            // make sure event has complete body
            if (in.readableBytes() < binlogEventHeader.getEventSize() - MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH) {
                log.debug("the event body is not complete, event size={}, readable bytes={}", binlogEventHeader.getEventSize(), in.readableBytes());
                in.resetReaderIndex();
                break;
            }
            Optional.ofNullable(decodeEvent(payload, binlogEventHeader)).ifPresent(out::add);
            skipChecksum(binlogEventHeader.getEventType(), in);
        }
    }
    
    private AbstractBinlogEvent decodeEvent(final MySQLPacketPayload payload, final MySQLBinlogEventHeader binlogEventHeader) {
        switch (MySQLBinlogEventType.valueOf(binlogEventHeader.getEventType())) {
            case ROTATE_EVENT:
                decodeRotateEvent(binlogEventHeader, payload);
                return null;
            case FORMAT_DESCRIPTION_EVENT:
                MySQLBinlogFormatDescriptionEventPacket formatDescriptionEventPacket = new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload);
                // MySQL mgr checksum length is 0, but the event ends up with 4 extra bytes, need to skip them.
                int readableBytes = payload.getByteBuf().readableBytes();
                if (binlogEventHeader.getChecksumLength() <= 0 && readableBytes > 0) {
                    if (readableBytes != 4) {
                        log.warn("the format description event has extra bytes, readable bytes length={}, binlogEventHeader={}, formatDescriptionEvent={}", readableBytes, binlogEventHeader,
                                formatDescriptionEventPacket);
                    }
                    payload.getByteBuf().skipBytes(readableBytes);
                }
                return null;
            case TABLE_MAP_EVENT:
                decodeTableMapEvent(binlogEventHeader, payload);
                return null;
            case WRITE_ROWS_EVENTv1:
            case WRITE_ROWS_EVENTv2:
                return decodeWriteRowsEventV2(binlogEventHeader, payload);
            case UPDATE_ROWS_EVENTv1:
            case UPDATE_ROWS_EVENTv2:
                return decodeUpdateRowsEventV2(binlogEventHeader, payload);
            case DELETE_ROWS_EVENTv1:
            case DELETE_ROWS_EVENTv2:
                return decodeDeleteRowsEventV2(binlogEventHeader, payload);
            default:
                PlaceholderEvent result = createPlaceholderEvent(binlogEventHeader);
                int remainDataLength = binlogEventHeader.getEventSize() + 2 - binlogEventHeader.getChecksumLength() - payload.getByteBuf().readerIndex();
                if (remainDataLength > 0) {
                    payload.skipReserved(remainDataLength);
                }
                return result;
        }
    }
    
    private void skipSequenceId(final MySQLPacketPayload payload) {
        payload.readInt1();
    }
    
    private void checkError(final MySQLPacketPayload payload) {
        int statusCode = payload.readInt1();
        if (255 == statusCode) {
            int errorNo = payload.readInt2();
            payload.skipReserved(1);
            String sqlState = payload.readStringFix(5);
            throw new RuntimeException(String.format("Decode binlog event failed, errorCode: %d, sqlState: %s, errorMessage: %s", errorNo, sqlState, payload.readStringEOF()));
        } else if (0 != statusCode) {
            if (log.isDebugEnabled()) {
                log.debug("Illegal binlog status code {}, remaining packet \n{}", statusCode, readRemainPacket(payload));
            }
        }
    }
    
    private String readRemainPacket(final MySQLPacketPayload payload) {
        return ByteBufUtil.hexDump(payload.readStringFixByBytes(payload.getByteBuf().readableBytes()));
    }
    
    private void skipChecksum(final int eventType, final ByteBuf in) {
        if (0 < binlogContext.getChecksumLength() && MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() != eventType) {
            in.skipBytes(binlogContext.getChecksumLength());
        }
    }
    
    private void decodeRotateEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRotateEventPacket rotateEventPacket = new MySQLBinlogRotateEventPacket(binlogEventHeader, payload);
        binlogContext.setFileName(rotateEventPacket.getNextBinlogName());
    }
    
    private void decodeTableMapEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogTableMapEventPacket tableMapEventPacket = new MySQLBinlogTableMapEventPacket(binlogEventHeader, payload);
        binlogContext.putTableMapEvent(tableMapEventPacket.getTableId(), tableMapEventPacket);
    }
    
    private DeleteRowsEvent decodeDeleteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket rowsEventPacket = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        rowsEventPacket.readRows(binlogContext.getTableMapEvent(rowsEventPacket.getTableId()), payload);
        DeleteRowsEvent result = new DeleteRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEventPacket.getTableId());
        result.setBeforeRows(rowsEventPacket.getRows());
        return result;
    }
    
    private UpdateRowsEvent decodeUpdateRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket rowsEventPacket = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        rowsEventPacket.readRows(binlogContext.getTableMapEvent(rowsEventPacket.getTableId()), payload);
        UpdateRowsEvent result = new UpdateRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEventPacket.getTableId());
        result.setBeforeRows(rowsEventPacket.getRows());
        result.setAfterRows(rowsEventPacket.getRows2());
        return result;
    }
    
    private WriteRowsEvent decodeWriteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket rowsEventPacket = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        rowsEventPacket.readRows(binlogContext.getTableMapEvent(rowsEventPacket.getTableId()), payload);
        WriteRowsEvent result = new WriteRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEventPacket.getTableId());
        result.setAfterRows(rowsEventPacket.getRows());
        return result;
    }
    
    private void initRowsEvent(final AbstractRowsEvent rowsEvent, final MySQLBinlogEventHeader binlogEventHeader, final long tableId) {
        rowsEvent.setDatabaseName(binlogContext.getDatabaseName(tableId));
        rowsEvent.setTableName(binlogContext.getTableName(tableId));
        rowsEvent.setFileName(binlogContext.getFileName());
        rowsEvent.setPosition(binlogEventHeader.getLogPos());
        rowsEvent.setTimestamp(binlogEventHeader.getTimestamp());
        rowsEvent.setServerId(binlogEventHeader.getServerId());
    }
    
    private PlaceholderEvent createPlaceholderEvent(final MySQLBinlogEventHeader binlogEventHeader) {
        PlaceholderEvent result = new PlaceholderEvent();
        result.setFileName(binlogContext.getFileName());
        result.setPosition(binlogEventHeader.getLogPos());
        result.setTimestamp(binlogEventHeader.getTimestamp());
        return result;
    }
}
