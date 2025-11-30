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

package org.apache.shardingsphere.proxy.frontend.opengauss.command.query.extended.bind;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.protocol.opengauss.packet.command.bind.OpenGaussComBatchBindPacket;
import org.apache.shardingsphere.database.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.command.query.extended.bind.PostgreSQLBindCompletePacket;
import org.apache.shardingsphere.database.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLBatchedStatementsExecutor;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.extended.PostgreSQLServerPreparedStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.Collection;

/**
 * Command batch bind executor for openGauss.
 */
@RequiredArgsConstructor
public final class OpenGaussComBatchBindExecutor implements CommandExecutor {
    
    private final OpenGaussComBatchBindPacket packet;
    
    private final ConnectionSession connectionSession;
    
    @Override
    public Collection<DatabasePacket> execute() throws SQLException {
        connectionSession.getDatabaseConnectionManager().handleAutoCommit();
        PostgreSQLServerPreparedStatement preparedStatement = connectionSession.getServerPreparedStatementRegistry().getPreparedStatement(packet.getStatementId());
        int updateCount = new PostgreSQLBatchedStatementsExecutor(connectionSession, preparedStatement, packet.readParameterSets(preparedStatement.getParameterTypes())).executeBatch();
        return Arrays.asList(PostgreSQLBindCompletePacket.getInstance(), createCommandComplete(preparedStatement.getSqlStatementContext().getSqlStatement(), updateCount));
    }
    
    private PostgreSQLCommandCompletePacket createCommandComplete(final SQLStatement sqlStatement, final int updateCount) {
        String sqlCommand = PostgreSQLCommand.valueOf(sqlStatement.getClass()).map(PostgreSQLCommand::getTag).orElse("");
        return new PostgreSQLCommandCompletePacket(sqlCommand, updateCount);
    }
}
