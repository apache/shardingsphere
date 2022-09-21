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

package org.apache.shardingsphere.data.pipeline.opengauss.ingest.wal.decode;

import com.google.common.base.Preconditions;
import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.opengauss.util.PGInterval;
import org.opengauss.util.PGobject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mppdb decoding plugin in openGauss.
 */
@AllArgsConstructor
public final class MppdbDecodingPlugin implements DecodingPlugin {
    
    private final BaseTimestampUtils timestampUtils;
    
    @Override
    public AbstractWalEvent decode(final ByteBuffer data, final BaseLogSequenceNumber logSequenceNumber) {
        AbstractWalEvent result;
        char eventType = readOneChar(data);
        if ('{' == eventType) {
            result = readTableEvent(readMppData(data));
        } else {
            result = new PlaceholderEvent();
        }
        result.setLogSequenceNumber(logSequenceNumber);
        return result;
    }
    
    private char readOneChar(final ByteBuffer data) {
        return (char) data.get();
    }
    
    private String readMppData(final ByteBuffer data) {
        StringBuilder mppData = new StringBuilder();
        mppData.append('{');
        int depth = 1;
        while (0 != depth && data.hasRemaining()) {
            char next = (char) data.get();
            mppData.append(next);
            int optDepth = '{' == next ? 1 : ('}' == next ? -1 : 0);
            depth += optDepth;
        }
        return mppData.toString();
    }
    
    private AbstractRowEvent readTableEvent(final String mppData) {
        Gson mppDataGson = new Gson();
        MppTableData mppTableData = mppDataGson.fromJson(mppData, MppTableData.class);
        AbstractRowEvent result;
        String rowEventType = mppTableData.getOpType();
        switch (rowEventType) {
            case IngestDataChangeType.INSERT:
                result = readWriteRowEvent(mppTableData);
                break;
            case IngestDataChangeType.UPDATE:
                result = readUpdateRowEvent(mppTableData);
                break;
            case IngestDataChangeType.DELETE:
                result = readDeleteRowEvent(mppTableData);
                break;
            default:
                throw new IngestException("Unknown rowEventType: " + rowEventType);
        }
        String[] tableMetaData = mppTableData.getTableName().split("\\.");
        result.setDatabaseName(tableMetaData[0]);
        result.setTableName(tableMetaData[1]);
        return result;
    }
    
    private AbstractRowEvent readWriteRowEvent(final MppTableData data) {
        WriteRowEvent result = new WriteRowEvent();
        result.setAfterRow(getColumnDataFromMppDataEvent(data));
        return result;
    }
    
    private AbstractRowEvent readUpdateRowEvent(final MppTableData data) {
        UpdateRowEvent result = new UpdateRowEvent();
        result.setAfterRow(getColumnDataFromMppDataEvent(data));
        return result;
    }
    
    private AbstractRowEvent readDeleteRowEvent(final MppTableData data) {
        DeleteRowEvent result = new DeleteRowEvent();
        result.setPrimaryKeys(getDeleteColumnDataFromMppDataEvent(data));
        return result;
    }
    
    private List<Object> getColumnDataFromMppDataEvent(final MppTableData data) {
        List<Object> result = new ArrayList<>(data.getColumnsType().length);
        for (int i = 0; i < data.getColumnsType().length; i++) {
            result.add(readColumnData(data.getColumnsVal()[i], data.getColumnsType()[i]));
        }
        return result;
    }
    
    private List<Object> getDeleteColumnDataFromMppDataEvent(final MppTableData data) {
        List<Object> result = new ArrayList<>(data.getOldKeysType().length);
        for (int i = 0; i < data.getOldKeysType().length; i++) {
            result.add(readColumnData(data.getOldKeysVal()[i], data.getOldKeysType()[i]));
        }
        return result;
    }
    
    private Object readColumnData(final String data, final String columnType) {
        if ("null".equals(data)) {
            return null;
        }
        if (columnType.startsWith("numeric")) {
            return new BigDecimal(data);
        }
        if (columnType.startsWith("bit")) {
            return decodeString(data.substring(1));
        }
        switch (columnType) {
            case "smallint":
                return Short.parseShort(data);
            case "integer":
                return Integer.parseInt(data);
            case "bigint":
                return Long.parseLong(data);
            case "real":
                return Float.parseFloat(data);
            case "double precision":
                return Double.parseDouble(data);
            case "boolean":
                return Boolean.parseBoolean(data);
            case "time without time zone":
            case "time with time zone":
                try {
                    return timestampUtils.toTime(null, decodeString(data));
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "date":
                return Date.valueOf(decodeString(data));
            case "timestamp without time zone":
            case "timestamp with time zone":
            case "smalldatetime":
                try {
                    return timestampUtils.toTimestamp(null, decodeString(data));
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "bytea":
                return decodeBytea(data);
            case "raw":
            case "reltime":
                return decodePgObject(data, columnType);
            case "money":
                return decodeMoney(data);
            case "interval":
                return decodeInterval(data);
            case "character varying":
            case "text":
            case "character":
            case "nvarchar2":
            default:
                return decodeString(data);
        }
    }
    
    private static PGobject decodeInterval(final String data) {
        try {
            return new PGInterval(decodeString(data));
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    private static PGobject decodePgObject(final String data, final String type) {
        try {
            PGobject result = new PGobject();
            result.setType(type);
            result.setValue(decodeString(data));
            return result;
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    private static PGobject decodeBytea(final String data) {
        try {
            PGobject result = new PGobject();
            result.setType("bytea");
            byte[] decodeByte = decodeHex(decodeString(data).substring(2));
            result.setValue(new String(decodeByte));
            return result;
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    private static String decodeMoney(final String data) {
        String result = decodeString(data);
        return '$' == result.charAt(0) ? result.substring(1) : result;
    }
    
    private static String decodeString(final String data) {
        if (data.length() > 1) {
            int begin = '\'' == data.charAt(0) ? 1 : 0;
            int end = data.length() + (data.charAt(data.length() - 1) == '\'' ? -1 : 0);
            return data.substring(begin, end);
        }
        return data;
    }
    
    private static byte[] decodeHex(final String hexString) {
        int dataLength = hexString.length();
        Preconditions.checkState(0 == (dataLength & 1), "Illegal hex data `%s`", hexString);
        if (0 == dataLength) {
            return new byte[0];
        }
        byte[] result = new byte[dataLength >>> 1];
        for (int i = 0; i < dataLength; i += 2) {
            result[i >>> 1] = decodeHexByte(hexString, i);
        }
        return result;
    }
    
    private static byte decodeHexByte(final String hexString, final int index) {
        int firstHexChar = Character.digit(hexString.charAt(index), 16);
        int secondHexChar = Character.digit(hexString.charAt(index + 1), 16);
        Preconditions.checkArgument(-1 != firstHexChar && -1 != secondHexChar, "Illegal hex byte `%s` in index `%d`", hexString, index);
        return (byte) ((firstHexChar << 4) + secondHexChar);
    }
}
