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

package info.avalon566.shardingscaling.sync.mysql.binlog.codec;

import info.avalon566.shardingscaling.sync.mysql.binlog.event.DeleteRowsEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.UpdateRowsEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.event.WriteRowsEvent;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.BinlogEventHeader;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.EventTypes;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.FormatDescriptionEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.RotateEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.RowsEvent;
import info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog.TableMapEvent;
import lombok.extern.slf4j.Slf4j;
import lombok.var;
import java.util.List;

/**
 * MySQL binlog event packet decoder.
 *
 * @author avalon566
 * @author yangyi
 */
@Slf4j
public final class MySQLBinlogEventPacketDecoder extends ByteToMessageDecoder {

    private final BinlogContext binlogContext = new BinlogContext();

    @Override
    protected void decode(final ChannelHandlerContext ctx, final ByteBuf in, final List<Object> out) {
        in.readByte();
        var binlogEventHeader = new BinlogEventHeader();
        binlogEventHeader.fromBytes(in);
        handleChecksum(in);
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
            case EventTypes.WRITE_ROWS_EVENTv2:
                WriteRowsEvent writeRowsEvent = decodeWriteRowsEventV2(binlogEventHeader, in);
                out.add(writeRowsEvent);
                break;
            case EventTypes.UPDATE_ROWS_EVENTv2:
                UpdateRowsEvent updateRowsEvent = decodeUpdateRowsEventV2(binlogEventHeader, in);
                out.add(updateRowsEvent);
                break;
            case EventTypes.DELETE_ROWS_EVENTv2:
                DeleteRowsEvent deleteRowsEvent = decodeDeleteRowsEventV2(binlogEventHeader, in);
                out.add(deleteRowsEvent);
                break;
            default:
                DataTypesCodec.skipBytes(in.readableBytes(), in);
        }
        if (in.isReadable()) {
            throw new UnsupportedOperationException();
        }
    }

    private void handleChecksum(final ByteBuf in) {
        //TODO: detect checksum length
        var checksumLength = 4;
        in.writerIndex(in.writerIndex() - 4);
    }

    private void decodeRotateEvent(final ByteBuf in) {
        var rotateEvent = new RotateEvent();
        rotateEvent.parse(in);
        binlogContext.setFileName(rotateEvent.getNextFileName());
    }

    private DeleteRowsEvent decodeDeleteRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        var rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePaylod(binlogContext, in);
        var deleteRowsEvent = new DeleteRowsEvent();
        deleteRowsEvent.setTableName(binlogContext.getFullTableName(rowsEvent.getTableId()));
        deleteRowsEvent.setBeforeColumns(rowsEvent.getColumnValues1());
        return deleteRowsEvent;
    }

    private UpdateRowsEvent decodeUpdateRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        var rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePaylod(binlogContext, in);
        var updateRowsEvent = new UpdateRowsEvent();
        updateRowsEvent.setTableName(binlogContext.getFullTableName(rowsEvent.getTableId()));
        updateRowsEvent.setBeforeColumns(rowsEvent.getColumnValues1());
        updateRowsEvent.setAfterColumns(rowsEvent.getColumnValues2());
        return updateRowsEvent;
    }

    private WriteRowsEvent decodeWriteRowsEventV2(final BinlogEventHeader binlogEventHeader, final ByteBuf in) {
        var rowsEvent = new RowsEvent(binlogEventHeader);
        rowsEvent.parsePostHeader(in);
        rowsEvent.parsePaylod(binlogContext, in);
        var writeRowsEvent = new WriteRowsEvent();
        writeRowsEvent.setTableName(binlogContext.getFullTableName(rowsEvent.getTableId()));
        writeRowsEvent.setAfterColumns(rowsEvent.getColumnValues1());
        return writeRowsEvent;
    }

    private void decodeTableMapEvent(final ByteBuf in) {
        var tableMapLogEvent = new TableMapEvent();
        tableMapLogEvent.parsePostHeader(in);
        tableMapLogEvent.parsePayload(in);
        binlogContext.putTableMapEvent(tableMapLogEvent.getTableId(), tableMapLogEvent);
    }

    private void decodeFormatDescriptionEvent(final ByteBuf in) {
        var formatDescriptionEvent = new FormatDescriptionEvent();
        formatDescriptionEvent.parse(in);
    }
}
