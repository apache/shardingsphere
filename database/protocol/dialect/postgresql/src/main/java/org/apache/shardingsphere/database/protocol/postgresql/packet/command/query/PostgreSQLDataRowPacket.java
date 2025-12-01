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

import java.sql.SQLException;
import java.sql.SQLXML;
import java.util.Collection;

/**
 * Data row packet for PostgreSQL.
 */
@RequiredArgsConstructor
@Getter
public final class PostgreSQLDataRowPacket extends PostgreSQLIdentifierPacket {
    
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
            payload.writeInt4(((byte[]) each).length);
            payload.writeBytes((byte[]) each);
        } else if (each instanceof SQLXML) {
            writeSQLXMLData(payload, each);
        } else if (each instanceof Boolean) {
            byte[] columnData = ((Boolean) each ? "t" : "f").getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData) ;
        } else {
            byte[] columnData = each.toString().getBytes(payload.getCharset());
            payload.writeInt4(columnData.length);
            payload.writeBytes(columnData);
        }
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
