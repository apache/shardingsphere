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

package org.apache.shardingsphere.db.protocol.postgresql.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.db.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.sql.SQLException;

/**
 * Command packet factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCommandPacketFactory {
    
    /**
     * Create new instance of command packet.
     *
     * @param commandPacketType command packet type for PostgreSQL
     * @param payload packet payload for PostgreSQL
     * @param connectionId connection id
     * @return command packet for PostgreSQL
     * @throws SQLException SQL exception
     */
    public static PostgreSQLCommandPacket newInstance(
            final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLPacketPayload payload, final int connectionId) throws SQLException {
        switch (commandPacketType) {
            case QUERY:
                return new PostgreSQLComQueryPacket(payload);
            case PARSE:
                return new PostgreSQLComParsePacket(payload);
            case BIND:
                return new PostgreSQLComBindPacket(payload, connectionId);
            case DESCRIBE:
                return new PostgreSQLComDescribePacket(payload);
            case EXECUTE:
                return new PostgreSQLComExecutePacket(payload);
            case SYNC:
                return new PostgreSQLComSyncPacket(payload);
            case TERMINATE:
                return new PostgreSQLComTerminationPacket(payload);
            default:
                return new PostgreSQLUnsupportedCommandPacket(commandPacketType.getValue());
        }
    }
}
