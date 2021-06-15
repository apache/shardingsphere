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

import org.apache.shardingsphere.db.protocol.mysql.constant.MySQLBinlogEventType;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.MySQLBinlogEventHeader;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogFormatDescriptionEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.management.MySQLBinlogRotateEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogRowsEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.packet.binlog.row.MySQLBinlogTableMapEventPacket;
import org.apache.shardingsphere.db.protocol.mysql.payload.MySQLPacketPayload;
import org.apache.shardingsphere.scaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.scaling.mysql.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.scaling.mysql.binlog.event.WriteRowsEvent;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import lombok.extern.slf4j.Slf4j;
import java.util.List;

/**
 * MySQL binlog event packet decoder.
 */
@Slf4j
public final class MySQLBinlogEventPacketDecoder extends ByteToMessageDecoder {
    
    private final BinlogContext binlogContext;
    
    public MySQLBinlogEventPacketDecoder(final int checksumLength) {
        binlogContext = new BinlogContext();
        binlogContext.setChecksumLength(checksumLength);
    }
    
    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        MySQLPacketPayload payload = new MySQLPacketPayload(in);
        skipSequenceId(payload);
        checkError(payload);
        MySQLBinlogEventHeader binlogEventHeader = new MySQLBinlogEventHeader(payload);
        removeChecksum(binlogEventHeader.getEventType(), in);
        switch (MySQLBinlogEventType.valueOf(binlogEventHeader.getEventType())) {
            case ROTATE_EVENT:
                decodeRotateEvent(binlogEventHeader, payload);
                break;
            case FORMAT_DESCRIPTION_EVENT:
                new MySQLBinlogFormatDescriptionEventPacket(binlogEventHeader, payload);
                break;
            case TABLE_MAP_EVENT:
                decodeTableMapEvent(binlogEventHeader, payload);
                break;
            case WRITE_ROWS_EVENTv1:
            case WRITE_ROWS_EVENTv2:
                out.add(decodeWriteRowsEventV2(binlogEventHeader, payload));
                break;
            case UPDATE_ROWS_EVENTv1:
            case UPDATE_ROWS_EVENTv2:
                out.add(decodeUpdateRowsEventV2(binlogEventHeader, payload));
                break;
            case DELETE_ROWS_EVENTv1:
            case DELETE_ROWS_EVENTv2:
                out.add(decodeDeleteRowsEventV2(binlogEventHeader, payload));
                break;
            default:
                out.add(createPlaceholderEvent(binlogEventHeader));
                payload.skipReserved(payload.getByteBuf().readableBytes());
        }
        if (in.isReadable()) {
            throw new UnsupportedOperationException(String.format("Do not parse binlog event fully, eventHeader: %s, remaining packet %s", binlogEventHeader, readRemainPacket(payload)));
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
    
    private void removeChecksum(final int eventType, final ByteBuf in) {
        if (0 < binlogContext.getChecksumLength() && MySQLBinlogEventType.FORMAT_DESCRIPTION_EVENT.getValue() != eventType) {
            in.writerIndex(in.writerIndex() - binlogContext.getChecksumLength());
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
        rowsEvent.setSchemaName(binlogContext.getSchemaName(tableId));
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
