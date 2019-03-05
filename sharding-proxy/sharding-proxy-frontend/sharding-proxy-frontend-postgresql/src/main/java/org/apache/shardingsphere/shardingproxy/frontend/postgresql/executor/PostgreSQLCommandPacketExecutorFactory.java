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

package org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.frontend.command.CommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.generic.PostgreSQLComTerminationPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.generic.PostgreSQLUnsupportedCommandPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.binary.bind.PostgreSQLComBindPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.binary.describe.PostgreSQLComDescribePacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.binary.execute.PostgreSQLComExecutePacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.binary.parse.PostgreSQLComParsePacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.binary.sync.PostgreSQLComSyncPacketExecutor;
import org.apache.shardingsphere.shardingproxy.frontend.postgresql.executor.query.text.PostgreSQLComQueryPacketExecutor;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.PostgreSQLPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.shardingproxy.transport.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;

/**
 * Command packet executor factory for PostgreSQL.
 *
 * @author zhangliang
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class PostgreSQLCommandPacketExecutorFactory {
    
    /**
     * Create new instance of command packet executor.
     *
     * @param commandPacketType command packet type for PostgreSQL
     * @param commandPacket command packet for PostgreSQL
     * @param backendConnection backend connection
     * @return command packet executor
     */
    public static CommandPacketExecutor<PostgreSQLPacket> newInstance(
            final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLCommandPacket commandPacket, final BackendConnection backendConnection) {
        switch (commandPacketType) {
            case QUERY:
                return new PostgreSQLComQueryPacketExecutor((PostgreSQLComQueryPacket) commandPacket, backendConnection);
            case PARSE:
                return new PostgreSQLComParsePacketExecutor((PostgreSQLComParsePacket) commandPacket, backendConnection);
            case BIND:
                return new PostgreSQLComBindPacketExecutor((PostgreSQLComBindPacket) commandPacket, backendConnection);
            case DESCRIBE:
                return new PostgreSQLComDescribePacketExecutor();
            case EXECUTE:
                return new PostgreSQLComExecutePacketExecutor();
            case SYNC:
                return new PostgreSQLComSyncPacketExecutor();
            case TERMINATE:
                return new PostgreSQLComTerminationPacketExecutor();
            default:
                return new PostgreSQLUnsupportedCommandPacketExecutor();
        }
    }
}
