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

package org.apache.shardingsphere.shardingscaling.mysql.binlog.packet.binlog;

import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DateAndTimeValueDecoder;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.BinlogContext;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.BlobValueDecoder;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DataTypesCodec;
import org.apache.shardingsphere.shardingscaling.mysql.binlog.codec.DecimalValueDecoder;
import org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.column.value.string.MySQLJsonValueDecoder;
import io.netty.buffer.ByteBuf;
import lombok.Getter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Rows event.
 *
 * <p>
 *     https://dev.mysql.com/doc/internals/en/rows-event.html
 * </p>
 *
 * @deprecated Replaced with {@link org.apache.shardingsphere.database.protocol.mysql.packet.binlog.row.MySQLBinlogRowsEventPacket}
 */
@Getter
@Deprecated
public final class RowsEventPacket {
    
    private final BinlogEventHeader binlogEventHeader;
    
    private long tableId;
    
    private int flags;
    
    private BitSet columnsPresentBitmap;
    
    private BitSet columnsPresentBitmap2;
    
    private List<Serializable[]> rows1 = new ArrayList<>();
    
    private List<Serializable[]> rows2 = new ArrayList<>();
    
    public RowsEventPacket(final BinlogEventHeader binlogEventHeader) {
        this.binlogEventHeader = binlogEventHeader;
    }
    
    /**
     * Parse post header.
     *
     * @param in buffer
     */
    public void parsePostHeader(final ByteBuf in) {
        tableId = DataTypesCodec.readUnsignedInt6LE(in);
        flags = DataTypesCodec.readUnsignedInt2LE(in);
        if (EventTypes.WRITE_ROWS_EVENT_V2 <= binlogEventHeader.getTypeCode()
                && EventTypes.DELETE_ROWS_EVENT_V2 >= binlogEventHeader.getTypeCode()) {
            // added the extra-data fields in v2
            int extraDataLength = DataTypesCodec.readUnsignedInt2LE(in) - 2;
            // skip data
            DataTypesCodec.readBytes(extraDataLength, in);
        }
    }
    
    /**
     * Parse payload.
     *
     * @param binlogContext binlog context
     * @param in            buffer
     */
    public void parsePayload(final BinlogContext binlogContext, final ByteBuf in) {
        int columnsLength = (int) DataTypesCodec.readLengthCodedIntLE(in);
        columnsPresentBitmap = DataTypesCodec.readBitmap(columnsLength, in);
        if (EventTypes.UPDATE_ROWS_EVENT_V1 == binlogEventHeader.getTypeCode()
                || EventTypes.UPDATE_ROWS_EVENT_V2 == binlogEventHeader.getTypeCode()) {
            columnsPresentBitmap2 = DataTypesCodec.readBitmap(columnsLength, in);
        }
        ColumnDef[] columnDefs = binlogContext.getColumnDefs(tableId);
        while (in.isReadable()) {
            //TODO support minimal binlog row image
            BitSet nullBitmap = DataTypesCodec.readBitmap(columnsLength, in);
            Serializable[] row = new Serializable[columnsLength];
            for (int i = 0; i < columnsLength; i++) {
                if (!nullBitmap.get(i)) {
                    row[i] = decodeColumnValue(columnDefs[i], in);
                } else {
                    row[i] = null;
                }
            }
            rows1.add(row);
            if (EventTypes.UPDATE_ROWS_EVENT_V1 == binlogEventHeader.getTypeCode()
                    || EventTypes.UPDATE_ROWS_EVENT_V2 == binlogEventHeader.getTypeCode()) {
                nullBitmap = DataTypesCodec.readBitmap(columnsLength, in);
                row = new Serializable[columnsLength];
                for (int i = 0; i < columnsLength; i++) {
                    if (!nullBitmap.get(i)) {
                        row[i] = decodeColumnValue(columnDefs[i], in);
                    } else {
                        row[i] = null;
                    }
                }
                rows2.add(row);
            }
        }
    }
    
    private Serializable decodeColumnValue(final ColumnDef columnDef, final ByteBuf in) {
        switch (columnDef.getType()) {
            case ColumnTypes.MYSQL_TYPE_LONG:
                return decodeLong(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TINY:
                return decodeTiny(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_SHORT:
                return decodeShort(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_INT24:
                return decodeInt24(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_LONGLONG:
                return decodeLonglong(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_FLOAT:
                return decodeFloat(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_NEWDECIMAL:
                return decodeNewDecimal(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DOUBLE:
                return DataTypesCodec.readDoubleLE(in);
            case ColumnTypes.MYSQL_TYPE_TIMESTAMP2:
                return DateAndTimeValueDecoder.decodeTimestamp2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATETIME2:
                return DateAndTimeValueDecoder.decodeDatetime2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIME:
                return DateAndTimeValueDecoder.decodeTime(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIME2:
                return DateAndTimeValueDecoder.decodeTime2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATE:
                return DateAndTimeValueDecoder.decodeDate(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATETIME:
                return DateAndTimeValueDecoder.decodeDateTime(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIMESTAMP:
                return DateAndTimeValueDecoder.decodeTimestamp(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_YEAR:
                return DateAndTimeValueDecoder.decodeYear(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_BLOB:
                return BlobValueDecoder.decodeBlob(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_VARCHAR:
            case ColumnTypes.MYSQL_TYPE_VAR_STRING:
                return decodeVarString(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_STRING:
                return decodeString(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_JSON:
                return decodeJson(columnDef.getMeta(), in);
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private Serializable decodeLong(final int meta, final ByteBuf in) {
        return DataTypesCodec.readInt4LE(in);
    }
    
    private Serializable decodeTiny(final int meta, final ByteBuf in) {
        return DataTypesCodec.readInt1(in);
    }
    
    private Serializable decodeShort(final int meta, final ByteBuf in) {
        return DataTypesCodec.readInt2LE(in);
    }
    
    private Serializable decodeInt24(final int meta, final ByteBuf in) {
        return DataTypesCodec.readInt3LE(in);
    }
    
    private Serializable decodeLonglong(final int meta, final ByteBuf in) {
        return DataTypesCodec.readInt8LE(in);
    }
    
    private Serializable decodeFloat(final int meta, final ByteBuf in) {
        return DataTypesCodec.readFloatLE(in);
    }
    
    private Serializable decodeNewDecimal(final int meta, final ByteBuf in) {
        return DecimalValueDecoder.decodeNewDecimal(meta, in);
    }
    
    private Serializable decodeEnumValue(final int meta, final ByteBuf in) {
        switch (meta) {
            case 1:
                return DataTypesCodec.readUnsignedInt1(in);
            case 2:
                return DataTypesCodec.readUnsignedInt2LE(in);
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private Serializable decodeVarString(final int meta, final ByteBuf in) {
        int length = 0;
        if (256 > meta) {
            length = DataTypesCodec.readUnsignedInt1(in);
        } else {
            length = DataTypesCodec.readUnsignedInt2LE(in);
        }
        return new String(DataTypesCodec.readBytes(length, in));
    }
    
    private Serializable decodeString(final int meta, final ByteBuf in) {
        switch (meta >> 8) {
            case ColumnTypes.MYSQL_TYPE_ENUM:
                return decodeEnumValue(meta & 0xff, in);
            case ColumnTypes.MYSQL_TYPE_SET:
                // hardcode
                return in.readByte();
            case ColumnTypes.MYSQL_TYPE_STRING:
                int length = DataTypesCodec.readUnsignedInt1(in);
                return new String(DataTypesCodec.readBytes(length, in));
            default:
                throw new UnsupportedOperationException();
        }
    }
    
    private Serializable decodeJson(final int meta, final ByteBuf in) {
        int length = 0;
        switch (meta) {
            case 1:
                length = DataTypesCodec.readUnsignedInt1(in);
                break;
            case 2:
                length = DataTypesCodec.readUnsignedInt2LE(in);
                break;
            case 3:
                length = DataTypesCodec.readUnsignedInt3LE(in);
                break;
            case 4:
                length = (int) DataTypesCodec.readUnsignedInt4LE(in);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        if (0 == length) {
            return "";
        } else {
            return MySQLJsonValueDecoder.decode(in.readBytes(length));
        }
    }
}
