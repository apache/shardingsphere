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

package org.apache.shardingsphere.database.protocol.postgresql.packet.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.admin.PostgreSQLUnsupportedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.flush.PostgreSQLComFlushPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.sync.PostgreSQLComSyncPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLComTerminationPacket;
import org.apache.shardingsphere.database.protocol.postgresql.payload.PostgreSQLPacketPayload;

import java.util.ArrayList;
import java.util.List;

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
     * @return created instance
     */
    public static PostgreSQLCommandPacket newInstance(final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLPacketPayload payload) {
        if (!PostgreSQLCommandPacketType.isExtendedProtocolPacketType(commandPacketType)) {
            payload.getByteBuf().skipBytes(1);
            return getPostgreSQLCommandPacket(commandPacketType, payload);
        }
        List<PostgreSQLCommandPacket> result = new ArrayList<>();
        while (payload.hasCompletePacket()) {
            PostgreSQLCommandPacketType type = PostgreSQLCommandPacketType.valueOf(payload.readInt1());
            int length = payload.getByteBuf().getInt(payload.getByteBuf().readerIndex());
            PostgreSQLPacketPayload slicedPayload = new PostgreSQLPacketPayload(payload.getByteBuf().readSlice(length), payload.getCharset());
            result.add(getPostgreSQLCommandPacket(type, slicedPayload));
        }
        return new PostgreSQLAggregatedCommandPacket(result);
    }
    
    private static PostgreSQLCommandPacket getPostgreSQLCommandPacket(final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLPacketPayload payload) {
        switch (commandPacketType) {
            case SIMPLE_QUERY:
                return new PostgreSQLComQueryPacket(payload);
            case PARSE_COMMAND:
                return new PostgreSQLComParsePacket(payload);
            case BIND_COMMAND:
                return new PostgreSQLComBindPacket(payload);
            case DESCRIBE_COMMAND:
                return new PostgreSQLComDescribePacket(payload);
            case EXECUTE_COMMAND:
                return new PostgreSQLComExecutePacket(payload);
            case SYNC_COMMAND:
                return new PostgreSQLComSyncPacket(payload);
            case CLOSE_COMMAND:
                return new PostgreSQLComClosePacket(payload);
            case FLUSH_COMMAND:
                return new PostgreSQLComFlushPacket(payload);
            case TERMINATE:
                return new PostgreSQLComTerminationPacket(payload);
            default:
                return new PostgreSQLUnsupportedCommandPacket(commandPacketType);
        }
    }
}
