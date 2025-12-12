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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse;

import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Command parse packet for PostgreSQL.
 */
@Getter
public final class PostgreSQLComParsePacket extends PostgreSQLCommandPacket implements SQLReceivedPacket {
    
    private final PostgreSQLPacketPayload payload;
    
    private final String statementId;
    
    @Getter(AccessLevel.NONE)
    private final String sql;
    
    private final HintValueContext hintValueContext;
    
    public PostgreSQLComParsePacket(final PostgreSQLPacketPayload payload) {
        this.payload = payload;
        payload.readInt4();
        statementId = payload.readStringNul();
        String originSQL = payload.readStringNul();
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        sql = SQLHintUtils.removeHint(originSQL);
    }
    
    /**
     * Read parameter types from Parse message.
     *
     * @return types of parameters
     */
    public List<PostgreSQLColumnType> readParameterTypes() {
        int parameterCount = payload.readInt2();
        List<PostgreSQLColumnType> result = new ArrayList<>(parameterCount);
        for (int i = 0; i < parameterCount; i++) {
            result.add(PostgreSQLColumnType.valueOf(payload.readInt4()));
        }
        return result;
    }
    
    @Override
    protected void write(final PostgreSQLPacketPayload payload) {
    }
    
    @Override
    public String getSQL() {
        return sql;
    }
    
    @Override
    public PostgreSQLIdentifierTag getIdentifier() {
        return PostgreSQLCommandPacketType.PARSE_COMMAND;
    }
}
