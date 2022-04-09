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

package org.apache.shardingsphere.proxy.frontend.opengauss.command;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.OpenGaussCommandPacketType;
import org.apache.shardingsphere.db.protocol.opengauss.packet.command.query.extended.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.db.protocol.packet.CommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.PostgreSQLCommandPacketType;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.PostgreSQLAggregatedCommandPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.close.PostgreSQLComClosePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.extended.parse.PostgreSQLComParsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.simple.PostgreSQLComQueryPacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind.OpenGaussComBatchBindExecutor;
import org.apache.shardingsphere.proxy.frontend.opengauss.command.query.simple.OpenGaussComQueryExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLComTerminationExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.generic.PostgreSQLUnsupportedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLAggregatedBatchedStatementsCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLAggregatedCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.bind.PostgreSQLComBindExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.close.PostgreSQLComCloseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.describe.PostgreSQLComDescribeExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.execute.PostgreSQLComExecuteExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.parse.PostgreSQLComParseExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.sync.PostgreSQLComSyncExecutor;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Command executor factory for openGauss.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@Slf4j
public final class OpenGaussCommandExecutorFactory {
    
    /**
     * Create new instance of command executor.
     *
     * @param commandPacketType command packet type for PostgreSQL/openGauss
     * @param commandPacket command packet for PostgreSQL/openGauss
     * @param connectionSession connection session
     * @param connectionContext PostgreSQL connection context
     * @return command executor
     * @throws SQLException SQL exception
     */
    public static CommandExecutor newInstance(final CommandPacketType commandPacketType, final PostgreSQLCommandPacket commandPacket,
                                              final ConnectionSession connectionSession, final PostgreSQLConnectionContext connectionContext) throws SQLException {
        log.debug("Execute packet type: {}, value: {}", commandPacketType, commandPacket);
        if (!(commandPacket instanceof PostgreSQLAggregatedCommandPacket)) {
            return getCommandExecutor(commandPacketType, commandPacket, connectionSession, connectionContext);
        }
        PostgreSQLAggregatedCommandPacket aggregatedCommandPacket = (PostgreSQLAggregatedCommandPacket) commandPacket;
        if (aggregatedCommandPacket.isContainsBatchedStatements() && aggregatedCommandPacket.getPackets().stream().noneMatch(each -> each instanceof OpenGaussComBatchBindPacket)) {
            return new PostgreSQLAggregatedCommandExecutor(getExecutorsOfAggregatedBatchedStatements(aggregatedCommandPacket, connectionSession, connectionContext));
        }
        List<CommandExecutor> result = new ArrayList<>(aggregatedCommandPacket.getPackets().size());
        for (PostgreSQLCommandPacket each : aggregatedCommandPacket.getPackets()) {
            result.add(getCommandExecutor((CommandPacketType) each.getIdentifier(), each, connectionSession, connectionContext));
        }
        return new PostgreSQLAggregatedCommandExecutor(result);
    }
    
    private static List<CommandExecutor> getExecutorsOfAggregatedBatchedStatements(final PostgreSQLAggregatedCommandPacket aggregatedCommandPacket,
                                                                                   final ConnectionSession connectionSession, final PostgreSQLConnectionContext connectionContext) throws SQLException {
        List<PostgreSQLCommandPacket> packets = aggregatedCommandPacket.getPackets();
        int firstBindIndex = aggregatedCommandPacket.getFirstBindIndex();
        int lastExecuteIndex = aggregatedCommandPacket.getLastExecuteIndex();
        List<CommandExecutor> result = new ArrayList<>(firstBindIndex + packets.size() - lastExecuteIndex);
        for (int i = 0; i < firstBindIndex; i++) {
            PostgreSQLCommandPacket each = packets.get(i);
            result.add(getCommandExecutor((CommandPacketType) each.getIdentifier(), each, connectionSession, connectionContext));
        }
        result.add(new PostgreSQLAggregatedBatchedStatementsCommandExecutor(connectionSession, packets.subList(firstBindIndex, lastExecuteIndex + 1)));
        for (int i = lastExecuteIndex + 1; i < packets.size(); i++) {
            PostgreSQLCommandPacket each = packets.get(i);
            result.add(getCommandExecutor((CommandPacketType) each.getIdentifier(), each, connectionSession, connectionContext));
        }
        return result;
    }
    
    private static CommandExecutor getCommandExecutor(final CommandPacketType commandPacketType, final PostgreSQLCommandPacket commandPacket, final ConnectionSession connectionSession,
                                                      final PostgreSQLConnectionContext connectionContext) throws SQLException {
        if (OpenGaussCommandPacketType.BATCH_BIND_COMMAND == commandPacketType) {
            return new OpenGaussComBatchBindExecutor((OpenGaussComBatchBindPacket) commandPacket, connectionSession);
        }
        switch ((PostgreSQLCommandPacketType) commandPacketType) {
            case SIMPLE_QUERY:
                return new OpenGaussComQueryExecutor(connectionContext, (PostgreSQLComQueryPacket) commandPacket, connectionSession);
            case PARSE_COMMAND:
                return new PostgreSQLComParseExecutor((PostgreSQLComParsePacket) commandPacket, connectionSession);
            case BIND_COMMAND:
                return new PostgreSQLComBindExecutor(connectionContext, (PostgreSQLComBindPacket) commandPacket, connectionSession);
            case DESCRIBE_COMMAND:
                return new PostgreSQLComDescribeExecutor(connectionContext, (PostgreSQLComDescribePacket) commandPacket, connectionSession);
            case EXECUTE_COMMAND:
                return new PostgreSQLComExecuteExecutor(connectionContext, (PostgreSQLComExecutePacket) commandPacket);
            case SYNC_COMMAND:
                return new PostgreSQLComSyncExecutor(connectionSession);
            case CLOSE_COMMAND:
                return new PostgreSQLComCloseExecutor(connectionContext, (PostgreSQLComClosePacket) commandPacket, connectionSession);
            case TERMINATE:
                return new PostgreSQLComTerminationExecutor();
            default:
                return new PostgreSQLUnsupportedCommandExecutor();
        }
    }
}
