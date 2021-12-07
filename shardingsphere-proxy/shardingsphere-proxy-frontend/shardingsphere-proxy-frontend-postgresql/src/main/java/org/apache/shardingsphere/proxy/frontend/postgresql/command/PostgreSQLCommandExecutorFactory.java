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
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLComTerminationExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind.PostgreSQLComBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.close.PostgreSQLComCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.execute.PostgreSQLComExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.sync.PostgreSQLComSyncExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.simple.PostgreSQLComQueryExecutor;

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
     * @param connectionSession connection session
     * @param connectionContext PostgreSQL connection context
     * @return command executor
     * @throws SQLException SQL exception
     */
    public static CommandExecutor newInstance(final PostgreSQLCommandPacketType commandPacketType, final PostgreSQLCommandPacket commandPacket,
                                              final ConnectionSession connectionSession, final PostgreSQLConnectionContext connectionContext) throws SQLException {
        log.debug("Execute packet type: {}, value: {}", commandPacketType, commandPacket);
        switch (commandPacketType) {
            case SIMPLE_QUERY:
                return new PostgreSQLComQueryExecutor(connectionContext, (PostgreSQLComQueryPacket) commandPacket, connectionSession);
            case PARSE_COMMAND:
                return new PostgreSQLComParseExecutor((PostgreSQLComParsePacket) commandPacket, connectionSession);
            case BIND_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComBindExecutor(connectionContext, (PostgreSQLComBindPacket) commandPacket, connectionSession));
                break;
            case DESCRIBE_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComDescribeExecutor(connectionContext, (PostgreSQLComDescribePacket) commandPacket));
                break;
            case EXECUTE_COMMAND:
                return new PostgreSQLComExecuteExecutor(connectionContext, (PostgreSQLComExecutePacket) commandPacket);
            case SYNC_COMMAND:
                return new PostgreSQLComSyncExecutor(connectionContext, connectionSession);
            case CLOSE_COMMAND:
                connectionContext.getPendingExecutors().add(new PostgreSQLComCloseExecutor(connectionContext, (PostgreSQLComClosePacket) commandPacket, connectionSession));
                break;
            case TERMINATE:
                return new PostgreSQLComTerminationExecutor();
            default:
                return new PostgreSQLUnsupportedCommandExecutor(connectionContext);
        }
        return Collections::emptyList;
    }
}
