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
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementParameterType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command bind packet for PostgreSQL.
 */
@Getter
@ToString
public final class PostgreSQLComBindPacket extends PostgreSQLCommandPacket {
    
    private final String statementId;
    
    private final String sql;
    
    private final List<Object> parameters;
    
    private final boolean binaryRowData;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final int connectionId) throws SQLException {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        int parameterFormatsLength = payload.readInt2();
        for (int i = 0; i < parameterFormatsLength; i++) {
            payload.readInt2();
        }
        PostgreSQLBinaryStatement binaryStatement = BinaryStatementRegistry.getInstance().get(connectionId).getBinaryStatement(statementId);
        sql = null == binaryStatement ? null : binaryStatement.getSql();
        parameters = null == sql ? Collections.emptyList() : getParameters(payload, binaryStatement.getParameterTypes());
        int resultFormatsLength = payload.readInt2();
        binaryRowData = resultFormatsLength > 0;
        for (int i = 0; i < resultFormatsLength; i++) {
            payload.readInt2();
        }
    }
    
    private List<Object> getParameters(final PostgreSQLPacketPayload payload, final List<PostgreSQLBinaryStatementParameterType> parameterTypes) throws SQLException {
        int parameterCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parameterCount);
        for (int parameterIndex = 0; parameterIndex < parameterCount; parameterIndex++) {
            int paramValueLen = payload.readInt4();
            if (-1 == paramValueLen) {
                result.add(null);
                continue;
            }
            PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(parameterTypes.get(parameterIndex).getColumnType());
            result.add(binaryProtocolValue.read(payload));
        }
        return result;
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public char getMessageType() {
        return PostgreSQLCommandPacketType.BIND.getValue();
    }
}
