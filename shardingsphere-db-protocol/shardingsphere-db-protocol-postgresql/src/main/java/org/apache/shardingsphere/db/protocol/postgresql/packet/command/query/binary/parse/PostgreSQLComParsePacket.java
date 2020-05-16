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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse;

import lombok.Getter;
import lombok.ToString;
import org.apache.shardingsphere.db.protocol.postgresql.constant.PostgreSQLColumnType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.PostgreSQLBinaryStatementParameterType;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Command parse packet for PostgreSQL.
 */
@Getter
@ToString
public final class PostgreSQLComParsePacket extends PostgreSQLCommandPacket {
    
    private final String statementId;
    
    private final String sql;
    
    private final List<PostgreSQLBinaryStatementParameterType> binaryStatementParameterTypes;
    
    public PostgreSQLComParsePacket(final PostgreSQLPacketPayload payload) {
        payload.readInt4();
        statementId = payload.readStringNul();
        sql = alterSQLToJDBCStyle(payload.readStringNul());
        binaryStatementParameterTypes = sql.isEmpty() ? Collections.emptyList() : getParameterTypes(payload);
    }
    
    private List<PostgreSQLBinaryStatementParameterType> getParameterTypes(final PostgreSQLPacketPayload payload) {
        int parametersCount = payload.readInt2();
        List<PostgreSQLBinaryStatementParameterType> result = new ArrayList<>(parametersCount); 
        for (int i = 0; i < parametersCount; i++) {
            result.add(new PostgreSQLBinaryStatementParameterType(PostgreSQLColumnType.valueOf(payload.readInt4())));
        }
        return result;
    }
    
    private String alterSQLToJDBCStyle(final String sql) {
        return sql.replaceAll("\\$[0-9]+", "?");
    }
    
    @Override
    public void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public char getMessageType() {
        return PostgreSQLCommandPacketType.PARSE.getValue();
    }
}
