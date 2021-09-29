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

package org.apache.shardingsphere.scaling.opengauss.wal.decode;

import com.google.gson.Gson;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.scaling.core.common.constant.ScalingConstant;
import org.apache.shardingsphere.scaling.core.common.exception.ScalingTaskExecuteException;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.BaseTimestampUtils;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingException;
import org.apache.shardingsphere.scaling.postgresql.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.scaling.postgresql.wal.event.WriteRowEvent;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.LinkedList;
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
        while (depth != 0 && data.hasRemaining()) {
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
            case ScalingConstant.INSERT:
                result = readWriteRowEvent(mppTableData);
                break;
            case ScalingConstant.UPDATE:
                result = readUpdateRowEvent(mppTableData);
                break;
            case ScalingConstant.DELETE:
                result = readDeleteRowEvent(mppTableData);
                break;
            default:
                throw new ScalingTaskExecuteException("");
        }
        String[] tableMetaData = mppTableData.getTableName().split("\\.");
        result.setSchemaName(tableMetaData[0]);
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
        List<Object> columns = new LinkedList<>();
    
        for (int i = 0; i < data.getColumnsType().length; i++) {
            columns.add(readColumnData(data.getColumnsVal()[i], data.getColumnsType()[i]));
        }
        return columns;
    }
    
    private List<Object> getDeleteColumnDataFromMppDataEvent(final MppTableData data) {
        List<Object> columns = new LinkedList<>();
        
        for (int i = 0; i < data.getOldKeysType().length; i++) {
            columns.add(readColumnData(data.getOldKeysVal()[i], data.getOldKeysType()[i]));
        }
        return columns;
    }
    
    private Object readColumnData(final String data, final String columnType) {
        if (columnType.startsWith("numeric")) {
            return new BigDecimal(data);
        }
        if (columnType.startsWith("bit") || columnType.startsWith("bit varying")) {
            return data;
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
                try {
                    return timestampUtils.toTime(null, data);
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "date":
                return Date.valueOf(data);
            case "timestamp without time zone":
                try {
                    return timestampUtils.toTimestamp(null, data);
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "bytea":
                return decodeHex(data.substring(2));
            case "character varying":
                return decodeString(data);
            default:
                return data;
        }
    }
    
    private static String decodeString(final String data) {
        if (data.length() > 1) {
            int begin = data.charAt(0) == '\'' ? 1 : 0;
            int end = data.length() + (data.charAt(data.length() - 1) == '\'' ? -1 : 0);
            return data.substring(begin, end);
        }
        return data;
    }
    
    private byte[] decodeHex(final String hexString) {
        int dataLength = hexString.length();
        if (0 != (dataLength & 1)) {
            throw new IllegalArgumentException(String.format("Illegal hex data %s", hexString));
        }
        if (0 == dataLength) {
            return new byte[0];
        }
        byte[] result = new byte[dataLength >>> 1];
        for (int i = 0; i < dataLength; i += 2) {
            result[i >>> 1] = decodeHexByte(hexString, i);
        }
        return result;
    }
    
    private byte decodeHexByte(final String hexString, final int index) {
        int firstHexChar = Character.digit(hexString.charAt(index), 16);
        int secondHexChar = Character.digit(hexString.charAt(index + 1), 16);
        if (-1 == firstHexChar || -1 == secondHexChar) {
            throw new IllegalArgumentException(String.format("Illegal hex byte '%s' in index %d", hexString, index));
        }
        return (byte) ((firstHexChar << 4) + secondHexChar);
    }
}
