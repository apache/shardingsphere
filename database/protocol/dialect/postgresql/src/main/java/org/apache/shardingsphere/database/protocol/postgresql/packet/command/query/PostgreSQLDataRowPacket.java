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
import lombok.RequiredArgsConstructor;
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
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.OffsetTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Collection;

/**
 * Data row packet for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLDataRowPacket extends PostgreSQLIdentifierPacket {
    
    private static final byte[] HEX_DIGITS = "0123456789abcdef".getBytes(StandardCharsets.US_ASCII);
    
    private final Collection<Object> data;
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt2(data.size());
        for (Object each : data) {
            if (each instanceof BinaryCell) {
                writeBinaryValue(payload, (BinaryCell) each);
            } else {
                writeTextValue(payload, each);
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
    
    private void writeTextValue(final PostgreSQLPacketPayload payload, final Object each) {
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
        } else if (each instanceof OffsetTime) {
            OffsetTime ot = (OffsetTime) each;
            String formatted;
            if (0 == ot.getNano()) {
                formatted = ot.format(DateTimeFormatter.ofPattern("HH:mm:ssXXX"));
            } else {
                formatted = ot.format(DateTimeFormatter.ofPattern("HH:mm:ss.SSSSSSXXX"));
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (each instanceof OffsetDateTime) {
            OffsetDateTime ot = (OffsetDateTime) each;
            ot = ot.withOffsetSameInstant(ZoneOffset.UTC);
            String formatted;
            if (1000 > ot.getNano()) {
                formatted = ot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) + "+00";
            } else {
                int precision = 9;
                int micros = ot.getNano();
                while (micros % 10 == 0) {
                    precision -= 1;
                    micros /= 10;
                }
                precision = Math.min(precision, 6);
                formatted = ot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss." + "SSSSSS".substring(0, precision))) + "+00";
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (each instanceof LocalTime) {
            LocalTime ot = (LocalTime) each;
            String formatted;
            if (1000 > ot.getNano()) {
                formatted = ot.format(DateTimeFormatter.ofPattern("HH:mm:ss"));
            } else {
                int precision = 9;
                int micros = ot.getNano();
                while (micros % 10 == 0) {
                    precision -= 1;
                    micros /= 10;
                }
                precision = Math.min(precision, 6);
                formatted = ot.format(DateTimeFormatter.ofPattern("HH:mm:ss." + "SSSSSS".substring(0, precision)));
            }
            byte[] columnData = formatted.getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        } else if (each instanceof LocalDateTime) {
            LocalDateTime ot = (LocalDateTime) each;
            String formatted;
            if (1000 > ot.getNano()) {
                formatted = ot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            } else {
                int precision = 9;
                int micros = ot.getNano();
                while (micros % 10 == 0) {
                    precision -= 1;
                    micros /= 10;
                }
                precision = Math.min(precision, 6);
                formatted = ot.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss." + "SSSSSS".substring(0, precision)));
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
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLMessagePacketType.DATA_ROW;
    }
}
