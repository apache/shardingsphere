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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind;

import lombok.Getter;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Binary result set row packet for PostgreSQL.
 */
public final class PostgreSQLBinaryResultSetRowPacket implements PostgreSQLPacket {
    
    @Getter
    private final char messageType = PostgreSQLCommandPacketType.DATA_ROW.getValue();
    
    private final Collection<PostgreSQLBinaryResultSetRow> binaryRows;
    
    public PostgreSQLBinaryResultSetRowPacket(final List<Object> data, final List<Integer> columnTypes) {
        binaryRows = getBinaryResultSetRows(columnTypes, data);
    }
    
    private Collection<PostgreSQLBinaryResultSetRow> getBinaryResultSetRows(final List<Integer> columnTypes, final List<Object> data) {
        Collection<PostgreSQLBinaryResultSetRow> result = new LinkedList<>();
        for (int i = 0; i < columnTypes.size(); i++) {
            result.add(new PostgreSQLBinaryResultSetRow(PostgreSQLColumnType.valueOfJDBCType(columnTypes.get(i)), data.get(i)));
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
        payload.writeInt2(binaryRows.size());
        writeValues(payload);
    }
    
    private void writeValues(final PostgreSQLPacketPayload payload) {
        for (PostgreSQLBinaryResultSetRow each : binaryRows) {
            PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(each.getColumnType());
            Object value = each.getData();
            payload.writeInt4(binaryProtocolValue.getColumnLength(value));
            binaryProtocolValue.write(payload, value);
        }
    }
}
