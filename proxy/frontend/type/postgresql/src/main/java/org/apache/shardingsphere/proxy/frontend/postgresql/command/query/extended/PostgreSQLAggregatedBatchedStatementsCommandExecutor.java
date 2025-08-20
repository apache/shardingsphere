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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.PostgreSQLCommandPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.PostgreSQLNoDataPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.PostgreSQLColumnType;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLComBindPacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.describe.PostgreSQLComDescribePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Aggregated batched statements command executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLAggregatedBatchedStatementsCommandExecutor implements CommandExecutor {
    
    private final ConnectionSession connectionSession;
    
    private final List<PostgreSQLCommandPacket> packets;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        PostgreSQLServerPreparedStatement preparedStatement = getPreparedStatement();
        PostgreSQLBatchedStatementsExecutor executor = new PostgreSQLBatchedStatementsExecutor(connectionSession, preparedStatement, readParameterSets(preparedStatement.getParameterTypes()));
        Collection<DatabasePacket> result = new ArrayList<>(packets.size());
        int totalInserted = executor.executeBatch();
        int executePacketCount = executePacketCount();
        for (PostgreSQLCommandPacket each : packets) {
            if (each instanceof PostgreSQLComBindPacket) {
                result.add(PostgreSQLBindCompletePacket.getInstance());
            }
            if (each instanceof PostgreSQLComDescribePacket) {
                result.add(preparedStatement.describeRows().orElseGet(PostgreSQLNoDataPacket::getInstance));
            }
            if (each instanceof PostgreSQLComExecutePacket) {
                String tag = PostgreSQLCommand.valueOf(preparedStatement.getSqlStatementContext().getSqlStatement().getClass()).orElse(PostgreSQLCommand.INSERT).getTag();
                result.add(new PostgreSQLCommandCompletePacket(tag, 0 == executePacketCount ? 1 : totalInserted / executePacketCount));
            }
        }
        return result;
    }
    
    private PostgreSQLServerPreparedStatement getPreparedStatement() {
        PostgreSQLComBindPacket bindPacket = (PostgreSQLComBindPacket) packets.get(0);
        return connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(bindPacket.getStatementId());
    }
    
    private List<List<Object>> readParameterSets(final List<PostgreSQLColumnType> parameterTypes) {
        List<List<Object>> result = new LinkedList<>();
        for (PostgreSQLCommandPacket each : packets) {
            if (each instanceof PostgreSQLComBindPacket) {
                result.add(((PostgreSQLComBindPacket) each).readParameters(parameterTypes));
            }
        }
        return result;
    }
    
    private int executePacketCount() {
        int result = 0;
        for (PostgreSQLCommandPacket each : packets) {
            if (each instanceof PostgreSQLComExecutePacket) {
                result++;
            }
        }
        return result;
    }
}
