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

import org.apache.shardingsphere.shardingscaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.AbstractRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.DeleteRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.PlaceholderEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.UpdateRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.event.WriteRowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.EventTypes;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.FormatDescriptionEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.RotateEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.RowsEvent;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.TableMapEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog.BinlogEventHeader;
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
        checkError(in);
        BinlogEventHeader binlogEventHeader = new BinlogEventHeader();
        binlogEventHeader.fromBytes(in);
        removeChecksum(binlogEventHeader.getTypeCode(), in);
        switch (binlogEventHeader.getTypeCode()) {
            case EventTypes.ROTATE_EVENT:
                decodeRotateEvent(in);
                break;
            case EventTypes.FORMAT_DESCRIPTION_EVENT:
                decodeFormatDescriptionEvent(in);
                break;
            case EventTypes.TABLE_MAP_EVENT:
                decodeTableMapEvent(in);
                break;
            case EventTypes.WRITE_ROWS_EVENT_V1:
            case EventTypes.WRITE_ROWS_EVENT_V2:
                out.add(decodeWriteRowsEventV2(binlogEventHeader, in));
                break;
            case EventTypes.UPDATE_ROWS_EVENT_V1:
            case EventTypes.UPDATE_ROWS_EVENT_V2:
                out.add(decodeUpdateRowsEventV2(binlogEventHeader, in));
                break;
            case EventTypes.DELETE_ROWS_EVENT_V1:
            case EventTypes.DELETE_ROWS_EVENT_V2:
                out.add(decodeDeleteRowsEventV2(binlogEventHeader, in));
                break;
            default:
                out.add(createPlaceholderEvent(binlogEventHeader));
                DataTypesCodec.skipBytes(in.readableBytes(), in);
        }
        if (in.isReadable()) {
            throw new UnsupportedOperationException();
        }
    }

    private void checkError(final ByteBuf in) {
        short errorCode = DataTypesCodec.readUnsignedInt1(in);
        if (0 == errorCode) {
            return;
        }
        if (255 == errorCode) {
            int errorNo = DataTypesCodec.readUnsignedInt2LE(in);
            DataTypesCodec.skipBytes(1, in);
            String sqlState = DataTypesCodec.readFixedLengthString(5, in);
            throw new RuntimeException(
                String.format("Decode binlog event failed, errorCode: %d, sqlState: %s, errorMessage: %s", errorNo, sqlState, DataTypesCodec.readFixedLengthString(in.readableBytes(), in)));
        }
    }

    private void removeChecksum(final short eventType, final ByteBuf in) {
        if (0 < binlogContext.getChecksumLength()
                && EventTypes.FORMAT_DESCRIPTION_EVENT != eventType) {
            in.writerIndex(in.writerIndex() - binlogContext.getChecksumLength());
        }
    }

    private void decodeRotateEvent(final ByteBuf in) {
        RotateEvent rotateEvent = new RotateEvent();
        rotateEvent.parse(in);
        binlogContext.setFileName(rotateEvent.getNextFileName());
    }

    private DeleteRowsEvent decodeDeleteRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        RowsEvent rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePayload(binlogContext, in);
        DeleteRowsEvent result = new DeleteRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEvent.getTableId());
        result.setBeforeRows(rowsEvent.getRows1());
        return result;
    }

    private UpdateRowsEvent decodeUpdateRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        RowsEvent rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePayload(binlogContext, in);
        UpdateRowsEvent result = new UpdateRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEvent.getTableId());
        result.setBeforeRows(rowsEvent.getRows1());
        result.setAfterRows(rowsEvent.getRows2());
        return result;
    }

    private WriteRowsEvent decodeWriteRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        RowsEvent rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePayload(binlogContext, in);
        WriteRowsEvent result = new WriteRowsEvent();
        initRowsEvent(result, binlogEventHeader, rowsEvent.getTableId());
        result.setAfterRows(rowsEvent.getRows1());
        return result;
    }

    private void initRowsEvent(final AbstractRowsEvent rowsEvent, final BinlogEventHeader binlogEventHeader, final long tableId) {
        rowsEvent.setSchemaName(binlogContext.getSchemaName(tableId));
        rowsEvent.setTableName(binlogContext.getTableName(tableId));
        rowsEvent.setFileName(binlogContext.getFileName());
        rowsEvent.setPosition(binlogEventHeader.getEndLogPos());
        rowsEvent.setTimestamp(binlogEventHeader.getTimeStamp());
        rowsEvent.setServerId(binlogEventHeader.getServerId());
    }
    
    private PlaceholderEvent createPlaceholderEvent(final BinlogEventHeader binlogEventHeader) {
        PlaceholderEvent result = new PlaceholderEvent();
        result.setFileName(binlogContext.getFileName());
        result.setPosition(binlogEventHeader.getEndLogPos());
        result.setTimestamp(binlogEventHeader.getTimeStamp());
        return result;
    }

    private void decodeTableMapEvent(final ByteBuf in) {
        TableMapEvent tableMapLogEvent = new TableMapEvent();
        tableMapLogEvent.parsePostHeader(in);
        tableMapLogEvent.parsePayload(in);
        binlogContext.putTableMapEvent(tableMapLogEvent.getTableId(), tableMapLogEvent);
    }

    private void decodeFormatDescriptionEvent(final ByteBuf in) {
        FormatDescriptionEvent formatDescriptionEvent = new FormatDescriptionEvent();
        formatDescriptionEvent.parse(in);
        binlogContext.setChecksumLength(formatDescriptionEvent.getChecksumLength());
    }
}
