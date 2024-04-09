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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.data.pipeline.core.constant.PipelineSQLOperationType;
import org.apache.shardingsphere.data.pipeline.core.exception.IngestException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseLogSequenceNumber;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.BaseTimestampUtils;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingException;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.decode.DecodingPlugin;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.AbstractWALEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.BeginTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.CommitTXEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.DeleteRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.PlaceholderEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.UpdateRowEvent;
import org.apache.shardingsphere.data.pipeline.postgresql.ingest.wal.event.WriteRowEvent;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.util.json.JsonUtils;
import org.opengauss.util.PGInterval;
import org.opengauss.util.PGobject;

import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.sql.Date;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Mppdb decoding plugin in openGauss.
 */
@HighFrequencyInvocation
@RequiredArgsConstructor
public final class MppdbDecodingPlugin implements DecodingPlugin {
    
    private final BaseTimestampUtils timestampUtils;
    
    private final boolean decodeWithTX;
    
    private final boolean decodeParallelly;
    
    @Override
    public AbstractWALEvent decode(final ByteBuffer data, final BaseLogSequenceNumber logSequenceNumber) {
        AbstractWALEvent result;
        byte[] bytes = new byte[data.remaining()];
        data.get(bytes);
        String dataText = new String(bytes, StandardCharsets.UTF_8);
        if (decodeWithTX) {
            result = decodeDataWithTX(dataText);
        } else {
            result = decodeDataIgnoreTX(dataText);
        }
        result.setLogSequenceNumber(logSequenceNumber);
        return result;
    }
    
    private AbstractWALEvent decodeDataWithTX(final String dataText) {
        if (decodeParallelly) {
            return decodeParallelly(dataText);
        } else {
            return decodeSerially(dataText);
        }
    }
    
    private AbstractWALEvent decodeSerially(final String dataText) {
        AbstractWALEvent result = new PlaceholderEvent();
        if (dataText.startsWith("BEGIN")) {
            int beginIndex = dataText.indexOf("BEGIN") + "BEGIN".length() + 1;
            result = new BeginTXEvent(Long.parseLong(dataText.substring(beginIndex)), null);
        } else if (dataText.startsWith("COMMIT")) {
            int commitBeginIndex = dataText.indexOf("COMMIT") + "COMMIT".length() + 1;
            int csnBeginIndex = dataText.indexOf("CSN") + "CSN".length() + 1;
            result = new CommitTXEvent(Long.parseLong(dataText.substring(commitBeginIndex, dataText.indexOf(' ', commitBeginIndex))), Long.parseLong(dataText.substring(csnBeginIndex)));
        } else if (dataText.startsWith("{")) {
            result = readTableEvent(dataText);
        }
        return result;
    }
    
    private AbstractWALEvent decodeParallelly(final String dataText) {
        AbstractWALEvent result = new PlaceholderEvent();
        if (dataText.startsWith("BEGIN")) {
            int beginIndex = dataText.indexOf("CSN:") + "CSN:".length() + 1;
            int firstLsnIndex = dataText.indexOf("first_lsn");
            long csn = firstLsnIndex > 0 ? Long.parseLong(dataText.substring(beginIndex, firstLsnIndex - 1)) : 0L;
            result = new BeginTXEvent(null, csn);
        } else if (dataText.startsWith("commit") || dataText.startsWith("COMMIT")) {
            int beginIndex = dataText.indexOf("xid:") + "xid:".length() + 1;
            result = new CommitTXEvent(Long.parseLong(dataText.substring(beginIndex)), null);
        } else if (dataText.startsWith("{")) {
            result = readTableEvent(dataText);
        }
        return result;
    }
    
    private AbstractWALEvent decodeDataIgnoreTX(final String dataText) {
        return dataText.startsWith("{") ? readTableEvent(dataText) : new PlaceholderEvent();
    }
    
    private AbstractRowEvent readTableEvent(final String mppData) {
        MppTableData mppTableData;
        mppTableData = JsonUtils.fromJsonString(mppData, MppTableData.class);
        String rowEventType = mppTableData.getOpType();
        PipelineSQLOperationType type;
        try {
            type = PipelineSQLOperationType.valueOf(rowEventType);
        } catch (final IllegalArgumentException ex) {
            throw new IngestException("Unknown rowEventType: " + rowEventType);
        }
        AbstractRowEvent result;
        switch (type) {
            case INSERT:
                result = readWriteRowEvent(mppTableData);
                break;
            case UPDATE:
                result = readUpdateRowEvent(mppTableData);
                break;
            case DELETE:
                result = readDeleteRowEvent(mppTableData);
                break;
            default:
                throw new IngestException("Unknown rowEventType: " + rowEventType);
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
            case "tinyint":
            case "smallint":
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
            case "blob":
                return decodeBytea(data);
            case "raw":
            case "reltime":
            case "int4range":
            case "int8range":
            case "numrange":
            case "tsrange":
            case "tstzrange":
            case "daterange":
                return decodePgObject(data, columnType);
            case "money":
                return decodeMoney(data);
            case "interval":
                return decodeInterval(data);
            case "character varying":
            case "text":
            case "character":
            case "nvarchar2":
            case "tsquery":
            default:
                return decodeString(data).replace("''", "'");
        }
    }
    
    private PGobject decodeInterval(final String data) {
        try {
            return new PGInterval(decodeString(data));
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    private PGobject decodePgObject(final String data, final String type) {
        try {
            PGobject result = new PGobject();
            result.setType(type);
            result.setValue(decodeString(data));
            return result;
        } catch (final SQLException ignored) {
            return null;
        }
    }
    
    private Object decodeBytea(final String data) {
        return decodeHex(decodeString(data).substring(2));
    }
    
    private String decodeMoney(final String data) {
        String result = decodeString(data);
        return '$' == result.charAt(0) ? result.substring(1) : result;
    }
    
    private String decodeString(final String data) {
        if (data.length() > 1) {
            int begin = '\'' == data.charAt(0) ? 1 : 0;
            int end = data.length() + (data.charAt(data.length() - 1) == '\'' ? -1 : 0);
            return data.substring(begin, end);
        }
        return data;
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
