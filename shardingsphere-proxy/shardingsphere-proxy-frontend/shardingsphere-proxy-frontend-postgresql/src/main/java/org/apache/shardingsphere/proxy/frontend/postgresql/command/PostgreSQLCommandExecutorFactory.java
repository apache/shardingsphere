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

package org.apache.shardingsphere.proxy.frontend.postgresql.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.text.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLComTerminationExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.bind.PostgreSQLComBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.close.PostgreSQLComCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.execute.PostgreSQLComExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.text.PostgreSQLComQueryExecutor;

import java.sql.SQLException;
import java.util.Collections;

/**
 * Command executor factory for PostgreSQL.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class PostgreSQLCommandExecutorFactory {
    
    /**
     * Create new instance of command executor.
     *
     * @param commandPacketType command packet type for PostgreSQL
     * @param commandPacket command packet for PostgreSQL
     * @param backendConnection backend connection
     * @param connectionContext PostgreSQL connection context
     * @return command executor
     * @throws SQLException SQL exception
     */
    public static CommandExecutor newInstance(final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLCommandPacket commandPacket,
                                              final BackendConnection backendConnection, final PostgreSQLConnectionContext connectionContext) throws SQLException {
        log.debug("Execute packet type: {}, value: {}", commandPacketType, commandPacket);
        switch (commandPacketType) {
            case SIMPLE_QUERY:
                return new PostgreSQLComQueryExecutor((PostgreSQLComQueryPacket) commandPacket, backendConnection);
            case PARSE_COMMAND:
                return new PostgreSQLComParseExecutor(connectionContext, (PostgreSQLComParsePacket) commandPacket, backendConnection);
            case BIND_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComBindExecutor(connectionContext, (PostgreSQLComBindPacket) commandPacket, backendConnection));
                break;
            case DESCRIBE_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComDescribeExecutor(connectionContext));
                break;
            case EXECUTE_COMMAND:
                return new PostgreSQLComExecuteExecutor(connectionContext);
            case SYNC_COMMAND:
                return new PostgreSQLComSyncExecutor(connectionContext, backendConnection);
            case CLOSE_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComCloseExecutor((PostgreSQLComClosePacket) commandPacket, backendConnection));
                break;
            case TERMINATE:
                return new PostgreSQLComTerminationExecutor();
            default:
                return new PostgreSQLUnsupportedCommandExecutor(connectionContext);
        }
        return Collections::emptyList;
    }
}
