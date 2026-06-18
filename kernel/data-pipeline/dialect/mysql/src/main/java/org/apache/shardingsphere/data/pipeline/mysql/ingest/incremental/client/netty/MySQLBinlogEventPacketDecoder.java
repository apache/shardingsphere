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

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.data.pipeline.core.exception.PipelineInternalException;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.MySQLBinlogContext;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.MySQLBaseBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.PlaceholderBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.query.MySQLQueryBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLDeleteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLUpdateRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.rows.MySQLWriteRowsBinlogEvent;
import org.apache.shardingsphere.data.pipeline.mysql.ingest.incremental.binlog.event.transaction.MySQLXidBinlogEvent;
import org.apache.shardingsphere.database.protocol.constant.CommonConstants;
import org.apache.shardingsphere.database.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.management.MySQLBinlogFormatDescriptionEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.management.MySQLBinlogRotateEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogRowsEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.database.protocol.mysql.payload.MySQLPacketPayload;

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
    
    private final MySQLBinlogContext binlogContext;
    
    private final boolean decodeWithTX;
    
    private List<MySQLBaseBinlogEvent> records = new LinkedList<>();
    
    public MySQLBinlogEventPacketDecoder(final int checksumLength, final Map<Long, MySQLBinlogTableMapEventPacket> tableMap, final boolean decodeWithTX) {
        this.decodeWithTX = decodeWithTX;
        binlogContext = new MySQLBinlogContext(checksumLength, tableMap);
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
            Optional<MySQLBaseBinlogEvent> binlogEvent = decodeEvent(binlogEventHeader, payload);
            if (!binlogEvent.isPresent()) {
                skipChecksum(binlogEventHeader.getEventType(), in);
                return;
            }
            if (binlogEvent.get() instanceof PlaceholderBinlogEvent) {
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
    
    private void processEventWithTX(final MySQLBaseBinlogEvent binlogEvent, final List<Object> out) {
        if (binlogEvent instanceof MySQLQueryBinlogEvent) {
            MySQLQueryBinlogEvent queryEvent = (MySQLQueryBinlogEvent) binlogEvent;
            if (TX_BEGIN_SQL.equals(queryEvent.getSql())) {
                records = new LinkedList<>();
            } else {
                out.add(binlogEvent);
            }
        } else if (binlogEvent instanceof MySQLXidBinlogEvent) {
            records.add(binlogEvent);
            out.add(records);
        } else {
            records.add(binlogEvent);
        }
    }
    
    private void processEventIgnoreTX(final MySQLBaseBinlogEvent binlogEvent, final List<Object> out) {
        if (binlogEvent instanceof MySQLQueryBinlogEvent) {
            MySQLQueryBinlogEvent queryEvent = (MySQLQueryBinlogEvent) binlogEvent;
            if (TX_BEGIN_SQL.equals(queryEvent.getSql())) {
                return;
            }
        }
        out.add(binlogEvent);
    }
    
    private Optional<MySQLBaseBinlogEvent> decodeEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
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
        binlogContext.putTableMapEvent(new MySQLBinlogTableMapEventPacket(binlogEventHeader, payload));
    }
    
    private MySQLWriteRowsBinlogEvent decodeWriteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        MySQLBinlogTableMapEventPacket tableMapEventPacket = binlogContext.getTableMapEvent(packet.getTableId());
        packet.readRows(tableMapEventPacket, payload);
        return new MySQLWriteRowsBinlogEvent(binlogContext.getFileName(),
                binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp(), tableMapEventPacket.getSchemaName(), tableMapEventPacket.getTableName(), packet.getRows());
    }
    
    private MySQLUpdateRowsBinlogEvent decodeUpdateRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        MySQLBinlogTableMapEventPacket tableMapEventPacket = binlogContext.getTableMapEvent(packet.getTableId());
        packet.readRows(tableMapEventPacket, payload);
        return new MySQLUpdateRowsBinlogEvent(binlogContext.getFileName(),
                binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp(), tableMapEventPacket.getSchemaName(), tableMapEventPacket.getTableName(), packet.getRows(), packet.getRows2());
    }
    
    private MySQLDeleteRowsBinlogEvent decodeDeleteRowsEventV2(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        MySQLBinlogRowsEventPacket packet = new MySQLBinlogRowsEventPacket(binlogEventHeader, payload);
        MySQLBinlogTableMapEventPacket tableMapEventPacket = binlogContext.getTableMapEvent(packet.getTableId());
        packet.readRows(tableMapEventPacket, payload);
        return new MySQLDeleteRowsBinlogEvent(binlogContext.getFileName(),
                binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp(), tableMapEventPacket.getSchemaName(), tableMapEventPacket.getTableName(), packet.getRows());
    }
    
    private PlaceholderBinlogEvent decodePlaceholderEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        PlaceholderBinlogEvent result = new PlaceholderBinlogEvent(binlogContext.getFileName(), binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp());
        int remainDataLength = binlogEventHeader.getEventSize() + 1 - binlogEventHeader.getChecksumLength() - payload.getByteBuf().readerIndex();
        if (remainDataLength > 0) {
            payload.skipReserved(remainDataLength);
        }
        return result;
    }
    
    private MySQLQueryBinlogEvent decodeQueryEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        int threadId = payload.readInt4();
        int executionTime = payload.readInt4();
        payload.skipReserved(1);
        int errorCode = payload.readInt2();
        payload.skipReserved(payload.readInt2());
        String databaseName = payload.readStringNul();
        String sql = payload.readStringFix(payload.getByteBuf().readableBytes() - binlogEventHeader.getChecksumLength());
        return new MySQLQueryBinlogEvent(binlogContext.getFileName(), binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp(), threadId, executionTime, errorCode, databaseName, sql);
    }
    
    private MySQLXidBinlogEvent decodeXidEvent(final MySQLBinlogEventHeader binlogEventHeader, final MySQLPacketPayload payload) {
        return new MySQLXidBinlogEvent(binlogContext.getFileName(), binlogEventHeader.getLogPos(), binlogEventHeader.getTimestamp(), payload.readInt8());
    }
    
    private void skipChecksum(final int eventType, final ByteBuf in) {
        if (0 < binlogContext.getChecksumLength() && MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() != eventType) {
            in.skipBytes(binlogContext.getChecksumLength());
        }
    }
}
