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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode;

import com.google.common.base.Preconditions;
import lombok.AllArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.ingest.IngestDataChangeType;
import org.apache.shardingsphere.data.pipeline.core.ingest.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWalEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.sql.Date;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Test decoding plugin.
 */
@AllArgsConstructor
public final class TestDecodingPlugin implements DecodingPlugin {
    
    private final BaseTimestampUtils timestampUtils;
    
    @Override
    public AbstractWalEvent decode(final ByteBuffer data, final BaseLogSequenceNumber logSequenceNumber) {
        AbstractWalEvent result;
        String eventType = readEventType(data);
        if ("table".equals(eventType)) {
            result = readTableEvent(data);
        } else {
            result = new PlaceholderEvent();
        }
        result.setLogSequenceNumber(logSequenceNumber);
        return result;
    }
    
    private String readEventType(final ByteBuffer data) {
        return readNextSegment(data);
    }
    
    private AbstractRowEvent readTableEvent(final ByteBuffer data) {
        AbstractRowEvent result;
        String tableName = readTableName(data);
        String rowEventType = readRowEventType(data);
        switch (rowEventType) {
            case IngestDataChangeType.INSERT:
                result = readWriteRowEvent(data);
                break;
            case IngestDataChangeType.UPDATE:
                result = readUpdateRowEvent(data);
                break;
            case IngestDataChangeType.DELETE:
                result = readDeleteRowEvent(data);
                break;
            default:
                throw new IngestException("Unknown rowEventType: " + rowEventType);
        }
        String[] tableMetaData = tableName.split("\\.");
        result.setDatabaseName(tableMetaData[0]);
        result.setTableName(tableMetaData[1].substring(0, tableMetaData[1].length() - 1));
        return result;
    }
    
    private AbstractRowEvent readWriteRowEvent(final ByteBuffer data) {
        WriteRowEvent result = new WriteRowEvent();
        List<Object> afterColumns = new LinkedList<>();
        while (data.hasRemaining()) {
            afterColumns.add(readColumn(data));
        }
        result.setAfterRow(afterColumns);
        return result;
    }
    
    private AbstractRowEvent readUpdateRowEvent(final ByteBuffer data) {
        UpdateRowEvent result = new UpdateRowEvent();
        List<Object> afterColumns = new LinkedList<>();
        while (data.hasRemaining()) {
            afterColumns.add(readColumn(data));
        }
        result.setAfterRow(afterColumns);
        return result;
    }
    
    private AbstractRowEvent readDeleteRowEvent(final ByteBuffer data) {
        DeleteRowEvent result = new DeleteRowEvent();
        List<Object> afterColumns = new LinkedList<>();
        while (data.hasRemaining()) {
            afterColumns.add(readColumn(data));
        }
        result.setPrimaryKeys(afterColumns);
        return result;
    }
    
    private String readTableName(final ByteBuffer data) {
        return readNextSegment(data);
    }
    
    private String readRowEventType(final ByteBuffer data) {
        String result = readNextSegment(data);
        return result.substring(0, result.length() - 1);
    }
    
    private Object readColumn(final ByteBuffer data) {
        String columnName = readColumnName(data);
        String columnType = readColumnType(data);
        data.get();
        return readColumnData(data, columnType);
    }
    
    private String readColumnName(final ByteBuffer data) {
        StringBuilder eventType = new StringBuilder();
        while (data.hasRemaining()) {
            char c = (char) data.get();
            if ('[' == c) {
                return eventType.toString();
            }
            eventType.append(c);
        }
        return eventType.toString();
    }
    
    private String readColumnType(final ByteBuffer data) {
        StringBuilder eventType = new StringBuilder();
        while (data.hasRemaining()) {
            char c = (char) data.get();
            if (']' == c) {
                return eventType.toString();
            }
            eventType.append(c);
        }
        return eventType.toString();
    }
    
    private Object readColumnData(final ByteBuffer data, final String columnType) {
        data.mark();
        if ('n' == data.get() && data.remaining() >= 3 && 'u' == data.get() && 'l' == data.get() && 'l' == data.get()) {
            if (data.hasRemaining()) {
                data.get();
            }
            return null;
        }
        data.reset();
        if (columnType.startsWith("numeric")) {
            return new BigDecimal(readNextSegment(data));
        }
        if (columnType.startsWith("bit") || columnType.startsWith("bit varying")) {
            return readNextSegment(data);
        }
        switch (columnType) {
            case "smallint":
                return Short.parseShort(readNextSegment(data));
            case "integer":
                return Integer.parseInt(readNextSegment(data));
            case "bigint":
                return Long.parseLong(readNextSegment(data));
            case "real":
                return Float.parseFloat(readNextSegment(data));
            case "double precision":
                return Double.parseDouble(readNextSegment(data));
            case "boolean":
                return Boolean.parseBoolean(readNextSegment(data));
            case "time without time zone":
                try {
                    return timestampUtils.toTime(null, readNextString(data));
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "date":
                return Date.valueOf(readNextString(data));
            case "timestamp without time zone":
                try {
                    return timestampUtils.toTimestamp(null, readNextString(data));
                } catch (final SQLException ex) {
                    throw new DecodingException(ex);
                }
            case "bytea":
                return decodeHex(readNextString(data).substring(2));
            default:
                return readNextString(data);
        }
    }
    
    private String readNextSegment(final ByteBuffer data) {
        StringBuilder eventType = new StringBuilder();
        while (data.hasRemaining()) {
            char c = (char) data.get();
            if (' ' == c) {
                return eventType.toString();
            }
            eventType.append(c);
        }
        return eventType.toString();
    }
    
    private String readNextString(final ByteBuffer data) {
        StringBuilder result = new StringBuilder();
        data.get();
        while (data.hasRemaining()) {
            char c = (char) data.get();
            if ('\'' == c) {
                if (!data.hasRemaining()) {
                    return result.toString();
                }
                char c2 = (char) data.get();
                if (' ' == c2) {
                    return result.toString();
                } else if ('\'' != c2) {
                    throw new IngestException("Read character varying data unexpected exception");
                }
            }
            result.append(c);
        }
        return result.toString();
    }
    
    private byte[] decodeHex(final String hexString) {
        int dataLength = hexString.length();
        Preconditions.checkArgument(0 == (dataLength & 1), "Illegal hex data `%s`", hexString);
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
        Preconditions.checkArgument(-1 != firstHexChar && -1 != secondHexChar, "Illegal hex byte `%s` in index `%d`", hexString, index);
        return (byte) ((firstHexChar << 4) + secondHexChar);
    }
}
