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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.binary.BinaryCell;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLMessagePacketType;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.nio.charset.StandardCharsets;
import java.sql.SQLException;
import java.sql.SQLXML;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.temporal.ChronoField;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.regex.Pattern;

/**
 * Data row packet for PostgreSQL.
 */
@Getter
public final class PostgreSQLDataRowPacket extends PostgreSQLIdentifierPacket {
    
    private static final Pattern OFFSET_PATTERN = Pattern.compile("^[+-](?:0[0-9]|1[0-4]):[0-5][0-9](?::[0-5][0-9])?$");
    
    private static final byte[] HEX_DIGITS = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    
    private static final DateTimeFormatter DATE_TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("yyyy-MM-dd HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
            .appendOptional(
                    new DateTimeFormatterBuilder()
                            .appendOffset("+HH:MM:ss", "+00:00")
                            .toFormatter())
            .toFormatter();
    
    private static final DateTimeFormatter TIME_FORMATTER = new DateTimeFormatterBuilder()
            .appendPattern("HH:mm:ss")
            .appendFraction(ChronoField.MICRO_OF_SECOND, 0, 6, true)
            .appendOptional(
                    new DateTimeFormatterBuilder()
                            .appendOffset("+HH:MM:ss", "+00:00")
                            .toFormatter())
            .toFormatter();
    
    private final Collection<Object> data;
    
    private final Collection<Integer> columnTypes;
    
    private final ZoneId sessionTimeZone;
    
    public PostgreSQLDataRowPacket(final Collection<Object> data, final Collection<Integer> columnTypes, final String sessionTimeZone) {
        this.data = data;
        this.columnTypes = columnTypes;
        this.sessionTimeZone = getSessionZone(sessionTimeZone);
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt2(data.size());
        Iterator<Integer> columnTypeIterator;
        
        if (columnTypes != null) {
            columnTypeIterator = columnTypes.iterator();
        } else {
            columnTypeIterator = Collections.nCopies(data.size(), Types.VARCHAR).iterator();
        }
        for (Object each : data) {
            int columnType = columnTypeIterator.hasNext() ? columnTypeIterator.next() : Types.VARCHAR;
            if (each instanceof BinaryCell) {
                writeBinaryValue(payload, (BinaryCell) each);
            } else {
                writeTextValue(payload, each, columnType);
            }
        }
    }
    
    private void writeBinaryValue(final PostgreSQLPacketPayload payload, final BinaryCell each) {
        Object value = each.getData();
        if (null == value) {
            payload.writeInt4(0xFFFFFFFF);
            return;
        }
        PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(each.getColumnType());
        payload.writeInt4(binaryProtocolValue.getColumnLength(payload, value));
        binaryProtocolValue.write(payload, value);
    }
    
    private void writeTextValue(final PostgreSQLPacketPayload payload, final Object each, final int columnType) {
        if (null == each) {
            payload.writeInt4(0xFFFFFFFF);
        } else if (each instanceof byte[]) {
            byte[] columnData = encodeByteaText((byte[]) each);
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (each instanceof SQLXML) {
            writeSQLXMLData(payload, each);
        } else if (each instanceof Boolean) {
            byte[] columnData = ((Boolean) each ? "t" : "f").getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (Types.TIME_WITH_TIMEZONE == columnType) {
            String formatted = "";
            if (each instanceof LocalTime) {
                formatted = TIME_FORMATTER.format(((LocalTime) each).atDate(LocalDate.now()).atZone(sessionTimeZone));
            } else if (each instanceof OffsetTime) {
                formatted = TIME_FORMATTER.format((OffsetTime) each);
            } else {
                formatted = formatToPostgresTimestamp(each.toString());
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (Types.TIMESTAMP_WITH_TIMEZONE == columnType) {
            String formatted = "";
            if (each instanceof LocalDateTime) {
                formatted = DATE_TIME_FORMATTER.format(((LocalDateTime) each).atZone(sessionTimeZone));
            } else if (each instanceof OffsetDateTime) {
                formatted = DATE_TIME_FORMATTER.format(((OffsetDateTime) each).toInstant().atZone(sessionTimeZone));
            } else if (each instanceof Timestamp) {
                formatted = DATE_TIME_FORMATTER.format(((Timestamp) each).toLocalDateTime().atZone(sessionTimeZone));
            } else {
                formatted = formatToPostgresTimestamp(each.toString());
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (Types.TIME == columnType) {
            String formatted = "";
            if (each instanceof LocalTime) {
                formatted = TIME_FORMATTER.format((LocalTime) each);
            } else if (each instanceof Time) {
                formatted = TIME_FORMATTER.format(((Time) each).toLocalTime());
                
            } else {
                formatted = formatToPostgresTimestamp(each.toString());
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (Types.TIMESTAMP == columnType) {
            String formatted = "";
            if (each instanceof LocalDateTime) {
                formatted = DATE_TIME_FORMATTER.format((LocalDateTime) each);
            } else if (each instanceof Timestamp) {
                formatted = DATE_TIME_FORMATTER.format(((Timestamp) each).toLocalDateTime());
            } else {
                formatted = formatToPostgresTimestamp(each.toString());
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else {
            byte[] columnData = each.toString().getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        }
    }
    
    private String formatToPostgresTimestamp(final String s) {
        if (s.contains(".")) {
            return s.replaceAll("(\\.\\d+?)0+(?=[Z+\\-\\s]|$)", "$1")
                    .replaceAll("\\.0*(?=[Z+\\-\\s]|$)", "");
        }
        return s;
    }
    
    private byte[] encodeByteaText(final byte[] value) {
        byte[] result = new byte[value.length * 2 + 2];
        result[0] = '\\';
        result[1] = 'x';
        for (int i = 0; i < value.length; i++) {
            int unsignedByte = value[i] & 0xFF;
            result[2 + i * 2] = HEX_DIGITS[unsignedByte >>> 4];
            result[3 + i * 2] = HEX_DIGITS[unsignedByte & 0x0F];
        }
        return result;
    }
    
    private void writeSQLXMLData(final PostgreSQLPacketPayload payload, final Object data) {
        try {
            byte[] dataBytes = ((SQLXML) data).getString().getBytes(payload.getCharset());
            payload.writeInt4(dataBytes.length);
            payload.writeBytes(dataBytes);
        } catch (final SQLException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private ZoneId getSessionZone(final String sessionTimeZone) {
        if (null == sessionTimeZone || sessionTimeZone.trim().isEmpty()) {
            return ZoneId.of("UTC");
        }
        String timeZone = sessionTimeZone.replaceAll("^['\"]|['\"]$", "").trim();
        return ZoneId.of(timeZone);
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.DATA_ROW;
    }
}
