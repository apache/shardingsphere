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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple;

import lombok.Getter;
import org.apache.shardingsphere.database.protocol.packet.sql.SQLReceivedPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.identifier.PostgreSQLIdentifierTag;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;

/**
 * Command query packet for PostgreSQL.
 */
public final class PostgreSQLComQueryPacket extends PostgreSQLCommandPacket implements SQLReceivedPacket {
    
    private final String sql;
    
    @Getter
    private final HintValueContext hintValueContext;
    
    public PostgreSQLComQueryPacket(final PostgreSQLPacketPayload payload) {
        payload.readInt4();
        String originSQL = payload.readStringNul();
        hintValueContext = SQLHintUtils.extractHint(originSQL);
        sql = SQLHintUtils.removeHint(originSQL);
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
        return PostgreSQLCommandPacketType.SIMPLE_QUERY;
    }
}
