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
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.BinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.QueryEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.binlog.event.XidEvent;
import org.apache.shardingsphere.db.protocol.constant.CommonConstants;
import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogFormatDescriptionEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogRotateEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogRowsEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * MySQL binlog event packet decoder.
 */
@Slf4j
public final class MySQLBinlogEventPacketDecoder extends ByteToMessageDecoder {
    
    private static final String TX_BEGIN_SQL = "BEGIN";
    
    private final BinlogContext binlogContext;
    
    private final boolean decodeWithTX;
    
    private List<AbstractBinlogEvent> records = new LinkedList<>();
    
    public MySQLBinlogEventPacketDecoder(final int checksumLength, final Map<Long, MySQLBinlogTableMapEventPacket> tableMap, final boolean decodeWithTX) {
        this.decodeWithTX = decodeWithTX;
        binlogContext = new BinlogContext(checksumLength, tableMap);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        while (in.readableBytes() >= 1 + MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH) {
            in.markReaderIndex();
            MySQLPacketPayload payload = new MySQLPacketPayload(in, ctx.channel().attr(CommonConstants.CHARSET_ATTRIBUTE_KEY).get());
            checkPayload(payload);
            MySQLBinlogEventHeader binlogEventHeader = new MySQLBinlogEventHeader(payload, binlogContext.getChecksumLength());
            if (!checkEventIntegrity(in, binlogEventHeader)) {
                return;
            }
            Optional<AbstractBinlogEvent> binlogEvent = decodeEvent(binlogEventHeader, payload);
            if (!binlogEvent.isPresent()) {
                skipChecksum(binlogEventHeader.getEventType(), in);
                return;
            }
            if (binlogEvent.get() instanceof PlaceholderEvent) {
                out.add(binlogEvent.get());
                skipChecksum(binlogEventHeader.getEventType(), in);
                return;
            }
            if (decodeWithTX) {
                processEventWithTX(binlogEvent.get(), out);
            } else {
                processEventIgnoreTX(binlogEvent.get(), out);
            }
            skipChecksum(binlogEventHeader.getEventType(), in);
        }
    }
    
    private void checkPayload(final MySQLPacketPayload payload) {
        int statusCode = payload.readInt1();
        if (255 == statusCode) {
            int errorNo = payload.readInt2();
            payload.skipReserved(1);
            String sqlState = payload.readStringFix(5);
            throw new PipelineInternalException("Decode binlog event failed, errorCode: %d, sqlState: %s, errorMessage: %s", errorNo, sqlState, payload.readStringEOF());
        }
        if (0 != statusCode) {
            log.debug("Illegal binlog status code {}, remaining packet \n{}", statusCode, readRemainPacket(payload));
        }
    }
    
    private String readRemainPacket(final MySQLPacketPayload payload) {
        return ByteBufUtil.hexDump(payload.readStringFixByBytes(payload.getByteBuf().readableBytes()));
    }
    
    private boolean checkEventIntegrity(final ByteBuf in, final MySQLBinlogEventHeader binlogEventHeader) {
        if (in.readableBytes() < binlogEventHeader.getEventSize() - MySQLBinlogEventHeader.MYSQL_BINLOG_EVENT_HEADER_LENGTH) {
            log.debug("the event body is not complete, event size={}, readable bytes={}", binlogEventHeader.getEventSize(), in.readableBytes());
            in.resetReaderIndex();
            return false;
        }
        return true;
    }
    
    private void processEventWithTX(final AbstractBinlogEvent binlogEvent, final List<Object> out) {
        if (binlogEvent instanceof QueryEvent) {
            QueryEvent queryEvent = (QueryEvent) binlogEvent;
            if (TX_BEGIN_SQL.equals(queryEvent.getSql())) {
                records = new LinkedList<>();
            } else {
                out.add(binlogEvent);
            }
        } else if (binlogEvent instanceof XidEvent) {
            records.add(binlogEvent);
            out.add(records);
        } else {
            records.add(binlogEvent);
        }
    }
    
    private void processEventIgnoreTX(final AbstractBinlogEvent binlogEvent, final List<Object> out) {
        if (binlogEvent instanceof QueryEvent) {
            QueryEvent queryEvent = (QueryEvent) binlogEvent;
            if (TX_BEGIN_SQL.equals(queryEvent.getSql())) {
                return;
            }
        }
        out.add(binlogEvent);
    }
    
    private Optional<AbstractBinlogEvent> decodeEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        switch (MySQLBinlogEventType.valueOf(binlogEventHeader.getEventType()).orElse(MySQLBinlogEventType.UNKNOWN_EVENT)) {
            case ROTATE_EVENT:
                decodeRotateEvent(binlogEventHeader, payload);
                return Optional.empty();
            case FORMAT_DESCRIPTION_EVENT:
                decodeFormatDescriptionEvent(binlogEventHeader, payload);
                return Optional.empty();
            case TABLE_MAP_EVENT:
                decodeTableMapEvent(binlogEventHeader, payload);
                return Optional.empty();
            case WRITE_ROWS_EVENT_V1:
            case WRITE_ROWS_EVENT_V2:
                return Optional.of(decodeWriteRowsEventV2(binlogEventHeader, payload));
            case UPDATE_ROWS_EVENT_V1:
            case UPDATE_ROWS_EVENT_V2:
                return Optional.of(decodeUpdateRowsEventV2(binlogEventHeader, payload));
            case DELETE_ROWS_EVENT_V1:
            case DELETE_ROWS_EVENT_V2:
                return Optional.of(decodeDeleteRowsEventV2(binlogEventHeader, payload));
            case QUERY_EVENT:
                return Optional.of(decodeQueryEvent(binlogEventHeader, payload));
            case XID_EVENT:
                return Optional.of(decodeXidEvent(binlogEventHeader, payload));
            default:
                return Optional.of(decodePlaceholderEvent(binlogEventHeader, payload));
        }
    }
    
    private void decodeRotateEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRotateEventPacket packet = new MySQLBinlogRotateEventPacket(binlogEventHeader, payload);
        binlogContext.setFileName(packet.getNextBinlogName());
    }
    
    private void decodeFormatDescriptionEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogFormatDescriptionEventPacket packet = new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload);
        // MySQL MGR checksum length is 0, but the event ends up with 4 extra bytes, need to skip them.
        int readableBytes = payload.getByteBuf().readableBytes();
        if (binlogEventHeader.getChecksumLength() <= 0 && readableBytes > 0) {
            if (readableBytes != 4) {
                log.warn("the format description event has extra bytes, readable bytes length={}, binlogEventHeader={}, formatDescriptionEvent={}", readableBytes, binlogEventHeader, packet);
            }
            payload.getByteBuf().skipBytes(readableBytes);
        }
    }
    
    private void decodeTableMapEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogTableMapEventPacket packet = new MySQLBinlogTableMapEventPacket(binlogEventHeader, payload);
        binlogContext.putTableMapEvent(packet.getTableId(), packet);
    }
    
    private WriteRowsEvent decodeWriteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        packet.readRows(binlogContext.getTableMapEvent(packet.getTableId()), payload);
        WriteRowsEvent result = new WriteRowsEvent();
        initRowsEvent(result, binlogEventHeader, packet.getTableId());
        result.setAfterRows(packet.getRows());
        return result;
    }
    
    private UpdateRowsEvent decodeUpdateRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        packet.readRows(binlogContext.getTableMapEvent(packet.getTableId()), payload);
        UpdateRowsEvent result = new UpdateRowsEvent();
        initRowsEvent(result, binlogEventHeader, packet.getTableId());
        result.setBeforeRows(packet.getRows());
        result.setAfterRows(packet.getRows2());
        return result;
    }
    
    private DeleteRowsEvent decodeDeleteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        packet.readRows(binlogContext.getTableMapEvent(packet.getTableId()), payload);
        DeleteRowsEvent result = new DeleteRowsEvent();
        initRowsEvent(result, binlogEventHeader, packet.getTableId());
        result.setBeforeRows(packet.getRows());
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
    
    private PlaceholderEvent decodePlaceholderEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        PlaceholderEvent result = createPlaceholderEvent(binlogEventHeader);
        int remainDataLength = binlogEventHeader.getEventSize() + 1 - binlogEventHeader.getChecksumLength() - payload.getByteBuf().readerIndex();
        if (remainDataLength > 0) {
            payload.skipReserved(remainDataLength);
        }
        return result;
    }
    
    private QueryEvent decodeQueryEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        int threadId = payload.readInt4();
        int executionTime = payload.readInt4();
        payload.skipReserved(1);
        int errorCode = payload.readInt2();
        payload.skipReserved(payload.readInt2());
        String databaseName = payload.readStringNul();
        String sql = payload.readStringFix(payload.getByteBuf().readableBytes() - binlogEventHeader.getChecksumLength());
        QueryEvent result = new QueryEvent(threadId, executionTime, errorCode, databaseName, sql);
        result.setFileName(binlogContext.getFileName());
        result.setPosition(binlogEventHeader.getLogPos());
        result.setTimestamp(binlogEventHeader.getTimestamp());
        result.setServerId(binlogEventHeader.getServerId());
        return result;
    }
    
    private XidEvent decodeXidEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        XidEvent result = new XidEvent(payload.readInt8());
        result.setFileName(binlogContext.getFileName());
        result.setPosition(binlogEventHeader.getLogPos());
        result.setTimestamp(binlogEventHeader.getTimestamp());
        result.setServerId(binlogEventHeader.getServerId());
        return result;
    }
    
    // TODO May be used again later, keep this method first.
    private PlaceholderEvent createPlaceholderEvent(final MySQLBinlogEventHeader binlogEventHeader) {
        PlaceholderEvent result = new PlaceholderEvent();
        result.setFileName(binlogContext.getFileName());
        result.setPosition(binlogEventHeader.getLogPos());
        result.setTimestamp(binlogEventHeader.getTimestamp());
        return result;
    }
    
    private void skipChecksum(final int eventType, final ByteBuf in) {
        if (0 < binlogContext.getChecksumLength() && MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() != eventType) {
            in.skipBytes(binlogContext.getChecksumLength());
        }
    }
}
