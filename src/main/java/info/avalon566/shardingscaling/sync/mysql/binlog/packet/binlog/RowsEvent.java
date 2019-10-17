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

package info.avalon566.shardingscaling.sync.mysql.binlog.packet.binlog;

import com.google.common.base.Strings;
import info.avalon566.shardingscaling.sync.mysql.binlog.BinlogContext;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DataTypesCodec;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.DecimalValueDecoder;
import info.avalon566.shardingscaling.sync.mysql.binlog.codec.JsonValueDecoder;
import io.netty.buffer.ByteBuf;
import lombok.Data;

import java.io.Serializable;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

/**
 * Rows event.
 * https://dev.mysql.com/doc/internals/en/rows-event.html
 *
 * @author avalon566
 */
@Data
public class RowsEvent {

    private final BinlogEventHeader binlogEventHeader;

    private long tableId;

    private int flags;

    private BitSet columnsPresentBitmap;

    private BitSet columnsPresentBitmap2;

    private List<Serializable[]> columnValues1 = new ArrayList<>();

    private List<Serializable[]> columnValues2 = new ArrayList<>();

    public RowsEvent(final BinlogEventHeader binlogEventHeader) {
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
            Serializable[] columnValues = new Serializable[columnsLength];
            for (int i = 0; i < columnsLength; i++) {
                if (!nullBitmap.get(i)) {
                    columnValues[i] = decodeValue(columnDefs[i], in);
                } else {
                    columnValues[i] = null;
                }
            }
            columnValues1.add(columnValues);
            if (EventTypes.UPDATE_ROWS_EVENT_V1 == binlogEventHeader.getTypeCode()
                    || EventTypes.UPDATE_ROWS_EVENT_V2 == binlogEventHeader.getTypeCode()) {
                nullBitmap = DataTypesCodec.readBitmap(columnsLength, in);
                columnValues = new Serializable[columnsLength];
                for (int i = 0; i < columnsLength; i++) {
                    if (!nullBitmap.get(i)) {
                        columnValues[i] = decodeValue(columnDefs[i], in);
                    } else {
                        columnValues[i] = null;
                    }
                }
                columnValues2.add(columnValues);
            }
        }
    }

    private Serializable decodeValue(final ColumnDef columnDef, final ByteBuf in) {
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
                return decodeTimestamp2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATETIME2:
                return decodeDatetime2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIME:
                return decodeTime(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIME2:
                return decodeTime2(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATE:
                return decodeDate(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_DATETIME:
                return decodeDateTime(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_TIMESTAMP:
                return decodeTimestamp(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_YEAR:
                return decodeYear(columnDef.getMeta(), in);
            case ColumnTypes.MYSQL_TYPE_BLOB:
                return decodeBlob(columnDef.getMeta(), in);
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

    private Serializable decodeTime(final int meta, final ByteBuf in) {
        String datetime = Long.toString(DataTypesCodec.readUnsignedInt3LE(in));
        if ("0".equals(datetime)) {
            return "00:00:00";
        }
        datetime = Strings.padStart(datetime, 6, '0');
        final String hour = datetime.substring(0, 2);
        final String minute = datetime.substring(2, 4);
        final String second = datetime.substring(4, 6);
        return String.format("%s:%s:%s", hour, minute, second);
    }

    private Serializable decodeTimestamp(final int meta, final ByteBuf in) {
        long second = DataTypesCodec.readUnsignedInt4LE(in);
        if (0 == second) {
            return "0000-00-00 00:00:00";
        }
        String secondStr = new Timestamp(second * 1000).toString();
        // remove millsecond data
        return secondStr.substring(0, secondStr.length() - 2);
    }

    private Serializable decodeDateTime(final int meta, final ByteBuf in) {
        final String datetime = Long.toString(DataTypesCodec.readInt8LE(in));
        if ("0".equals(datetime)) {
            return "0000-00-00 00:00:00";
        }
        final String year = datetime.substring(0, 4);
        final String month = datetime.substring(4, 6);
        final String day = datetime.substring(6, 8);
        final String hour = datetime.substring(8, 10);
        final String minute = datetime.substring(10, 12);
        final String second = datetime.substring(12, 14);
        return String.format("%s-%s-%s %s:%s:%s", year, month, day, hour, minute, second);
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

    private Serializable decodeTimestamp2(final int meta, final ByteBuf in) {
        long second = DataTypesCodec.readUnsignedInt4BE(in);
        if (0 == second) {
            return "0000-00-00 00:00:00";
        }
        String secondStr = new Timestamp(second * 1000).toString();
        // remove millsecond data
        secondStr = secondStr.substring(0, secondStr.length() - 2);
        if (0 < meta) {
            secondStr += "." + readAndAlignMillisecond(meta, in);
        }
        return secondStr;
    }

    private Serializable decodeDatetime2(final int meta, final ByteBuf in) {
        long datetime = DataTypesCodec.readUnsignedInt5BE(in) - 0x8000000000L;
        if (0 == datetime) {
            return "0000-00-00 00:00:00";
        }
        long ymd = datetime >> 17;
        long ym = ymd >> 5;
        long hms = datetime % (1 << 17);
        return String.format("%d-%02d-%02d %02d:%02d:%02d%s",
                ym / 13,
                ym % 13,
                ymd % (1 << 5),
                hms >> 12,
                (hms >> 6) % (1 << 6),
                hms % (1 << 6),
                0 < meta ? "." + readAndAlignMillisecond(meta, in) : "");
    }

    private Serializable decodeTime2(final int meta, final ByteBuf in) {
        long time = DataTypesCodec.readUnsignedInt3BE(in) - 0x800000L;
        return String.format("%02d:%02d:%02d",
                (time >> 12) % (1 << 10),
                (time >> 6) % (1 << 6),
                time % (1 << 6));
    }

    private Serializable decodeDate(final int meta, final ByteBuf in) {
        int date = DataTypesCodec.readUnsignedInt3LE(in);
        if (0 == date) {
            return "0000-00-00";
        }
        return String.format("%d-%02d-%02d",
                date / 16 / 32,
                date / 32 % 16,
                date % 32);
    }

    private Serializable decodeYear(final int meta, final ByteBuf in) {
        return DataTypesCodec.readUnsignedInt1(in) + 1900;
    }

    private Serializable decodeNewDecimal(final int meta, final ByteBuf in) {
        return DecimalValueDecoder.decodeNewDecimal(meta, in);
    }

    private Serializable decodeEnumVale(final int meta, final ByteBuf in) {
        switch (meta) {
            case 1:
                return DataTypesCodec.readUnsignedInt1(in);
            case 2:
                return DataTypesCodec.readUnsignedInt2LE(in);
            default:
                throw new UnsupportedOperationException();
        }
    }

    private Serializable decodeBlob(final int meta, final ByteBuf in) {
        switch (meta) {
            case 1:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt1(in), in);
            case 2:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt2LE(in), in);
            case 3:
                return DataTypesCodec.readBytes(DataTypesCodec.readUnsignedInt3LE(in), in);
            case 4:
                return DataTypesCodec.readBytes((int) DataTypesCodec.readUnsignedInt4LE(in), in);
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
                return decodeEnumVale(meta & 0xff, in);
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
            return JsonValueDecoder.decode(in.readBytes(length));
        }
    }

    private String readAndAlignMillisecond(final int meta, final ByteBuf in) {
        int fraction = 0;
        switch (meta) {
            case 1:
            case 2:
                fraction = DataTypesCodec.readUnsignedInt1(in) * 10000;
                break;
            case 3:
            case 4:
                fraction = DataTypesCodec.readUnsignedInt2BE(in) * 100;
                break;
            case 5:
            case 6:
                fraction = DataTypesCodec.readUnsignedInt3BE(in);
                break;
            default:
                throw new UnsupportedOperationException();
        }
        return alignMillisecond(meta, fraction);
    }

    private String alignMillisecond(final int meta, final int fraction) {
        StringBuilder result = new StringBuilder(6);
        String str = Integer.toString(fraction);
        int append = 6 - str.length();
        for (int i = 0; i < append; i++) {
            result.append("0");
        }
        result.append(str);
        return result.substring(0, meta);
    }
}
