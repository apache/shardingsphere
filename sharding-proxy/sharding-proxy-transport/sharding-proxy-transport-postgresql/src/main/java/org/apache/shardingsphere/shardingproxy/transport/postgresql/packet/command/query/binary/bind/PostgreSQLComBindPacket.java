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

package org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind;

import lombok.Getter;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.PostgreSQLQueryCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.BinaryStatementRegistry;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.PostgreSQLBinaryStatement;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValue;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.protocol.PostgreSQLBinaryProtocolValueFactory;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * PostgreSQL command bind packet.
 *
 * @author zhangyonglun
 */
@Getter
public final class PostgreSQLComBindPacket implements PostgreSQLQueryCommandPacket {
    
    private final String statementId;
    
    private final PostgreSQLBinaryStatement binaryStatement;
    
    private List<Object> parameters;
    
    private final boolean binaryRowData;
    
    public PostgreSQLComBindPacket(final PostgreSQLPacketPayload payload, final int connectionId) throws SQLException {
        payload.readInt4();
        payload.readStringNul();
        statementId = payload.readStringNul();
        int parameterFormatsLength = payload.readInt2();
        for (int i = 0; i < parameterFormatsLength; i++) {
            payload.readInt2();
        }
        binaryStatement = BinaryStatementRegistry.getInstance().get(connectionId).getBinaryStatement(statementId);
        if (null != binaryStatement && null != binaryStatement.getSql()) {
            parameters = getParameters(payload);
        }
        int resultFormatsLength = payload.readInt2();
        binaryRowData = resultFormatsLength > 0;
        for (int i = 0; i < resultFormatsLength; i++) {
            payload.readInt2();
        }
    }
    
    private List<Object> getParameters(final PostgreSQLPacketPayload payload) throws SQLException {
        int parametersCount = payload.readInt2();
        List<Object> result = new ArrayList<>(parametersCount);
        for (int parameterIndex = 0; parameterIndex < parametersCount; parameterIndex++) {
            payload.readInt4();
            PostgreSQLBinaryProtocolValue binaryProtocolValue = PostgreSQLBinaryProtocolValueFactory.getBinaryProtocolValue(binaryStatement.getParameterTypes().get(parameterIndex).getColumnType());
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
