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

package org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.decode;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.incremental.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

/**
 * Test decoding plugin.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class TestDecodingPlugin implements DecodingPlugin {
    
    private final BaseTimestampUtils timestampUtils;
    
    @Override
    public AbstractWALEvent decode(final ByteBuffer data, final BaseLogSequenceNumber logSequenceNumber) {
        AbstractWALEvent result;
        String type = readEventType(data);
        if (type.startsWith("BEGIN")) {
            result = new BeginTXEvent(Long.parseLong(readNextSegment(data)), null);
        } else if (type.startsWith("COMMIT")) {
            result = new CommitTXEvent(Long.parseLong(readNextSegment(data)), null);
        } else {
            result = "table".equals(type) ? readTableEvent(data) : new PlaceholderEvent();
        }
        result.setLogSequenceNumber(logSequenceNumber);
        return result;
    }
    
    private String readEventType(final ByteBuffer data) {
        return readNextSegment(data);
    }
    
    private AbstractRowEvent readTableEvent(final ByteBuffer data) {
        String tableName = readTableName(data);
        String rowEventType = readRowEventType(data);
        PipelineSQLOperationType type;
        try {
            type = PipelineSQLOperationType.valueOf(rowEventType);
        } catch (final IllegalArgumentException ex) {
            throw new IngestException("Unknown rowEventType: " + rowEventType);
        }
        AbstractRowEvent result;
        switch (type) {
            case INSERT:
                result = readWriteRowEvent(data);
                break;
            case UPDATE:
                result = readUpdateRowEvent(data);
                break;
            case DELETE:
                result = readDeleteRowEvent(data);
                break;
            default:
                throw new IngestException("Unknown rowEventType: " + rowEventType);
        }
        String[] tableMetaData = tableName.split("\\.");
        result.setSchemaName(tableMetaData[0]);
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
        skipColumnName(data);
        String columnType = readColumnType(data);
        data.get();
        return readColumnData(data, columnType);
    }
    
    private void skipColumnName(final ByteBuffer data) {
        while (data.hasRemaining()) {
            char c = (char) data.get();
            if ('[' == c) {
                return;
            }
        }
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
        if ('n' == data.get() && data.remaining() >= 3 && 'u' == data.get() && 'l' == data.get()) {
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
            case "json":
            case "jsonb":
                return readNextJson(data);
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
    
    private String readNextJson(final ByteBuffer data) {
        data.get();
        int offset = 0;
        int startPosition = data.position();
        int level = 0;
        while (data.hasRemaining()) {
            offset++;
            char c = (char) data.get();
            if ('{' == c) {
                level++;
            } else if ('}' == c) {
                level--;
                if (0 != level) {
                    continue;
                }
                if ('\'' != data.get()) {
                    throw new IngestException("Read json data unexpected exception");
                }
                if (data.hasRemaining()) {
                    data.get();
                }
                return readStringSegment(data, startPosition, offset).replace("''", "'");
            }
        }
        return null;
    }
    
    private String readStringSegment(final ByteBuffer data, final int startPosition, final int offset) {
        byte[] result = new byte[offset];
        for (int i = 0; i < offset; i++) {
            result[i] = data.get(startPosition + i);
        }
        return new String(result, StandardCharsets.UTF_8);
    }
    
    private String readNextString(final ByteBuffer data) {
        int offset = 0;
        data.get();
        int startPosition = data.position();
        while (data.hasRemaining()) {
            char c = (char) data.get();
            offset++;
            if ('\'' == c) {
                if (!data.hasRemaining()) {
                    offset--;
                    return readStringSegment(data, startPosition, offset).replace("''", "'");
                }
                char c2 = (char) data.get();
                if ('\'' == c2) {
                    offset++;
                    continue;
                }
                if (' ' == c2) {
                    offset--;
                    return readStringSegment(data, startPosition, offset).replace("''", "'");
                }
            }
        }
        return readStringSegment(data, startPosition, offset);
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
