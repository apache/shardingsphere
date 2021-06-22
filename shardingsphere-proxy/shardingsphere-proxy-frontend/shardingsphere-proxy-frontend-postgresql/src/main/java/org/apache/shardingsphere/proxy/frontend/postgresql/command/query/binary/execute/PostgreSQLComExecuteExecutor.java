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

package org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.execute;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.db.protocol.packet.DatabasePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.PostgreSQLEmptyQueryResponsePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLComExecutePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.command.query.binary.execute.PostgreSQLPortalSuspendedPacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.generic.PostgreSQLCommandCompletePacket;
import org.apache.shardingsphere.db.protocol.postgresql.packet.identifier.PostgreSQLIdentifierPacket;
import org.apache.shardingsphere.proxy.frontend.command.executor.CommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.QueryCommandExecutor;
import org.apache.shardingsphere.proxy.frontend.command.executor.ResponseType;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Command execute executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComExecuteExecutor implements QueryCommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final PostgreSQLComExecutePacket packet;
    
    private long dataRows;
    
    private boolean executionComplete;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (CommandExecutor each : connectionContext.getPendingExecutors()) {
            result.addAll(each.execute());
        }
        connectionContext.getPendingExecutors().clear();
        return result;
    }
    
    @Override
    public ResponseType getResponseType() {
        return ResponseType.QUERY;
    }
    
    @Override
    public boolean next() throws SQLException {
        return !executionComplete;
    }
    
    @Override
    public DatabasePacket<?> getQueryRowPacket() throws SQLException {
        if (reachedMaxRows()) {
            executionComplete = true;
            return new PostgreSQLPortalSuspendedPacket();
        }
        Optional<DatabasePacket<?>> result = getPacketFromPortal();
        if (result.isPresent()) {
            dataRows++;
            return result.get();
        }
        executionComplete = true;
        return createCommandCompletePacket();
    }
    
    private boolean reachedMaxRows() {
        return packet.getMaxRows() > 0 && dataRows == packet.getMaxRows();
    }
    
    private Optional<DatabasePacket<?>> getPacketFromPortal() throws SQLException {
        PostgreSQLPortal portal = connectionContext.getPortal(packet.getPortal());
        return portal.next() ? Optional.of(portal.nextPacket()) : Optional.empty();
    }
    
    private PostgreSQLIdentifierPacket createCommandCompletePacket() {
        if (connectionContext.getSqlStatement().map(EmptyStatement.class::isInstance).orElse(false)) {
            return new PostgreSQLEmptyQueryResponsePacket();
        }
        String sqlCommand = connectionContext.getSqlStatement().map(SQLStatement::getClass).map(PostgreSQLCommand::valueOf).map(command -> command.map(Enum::name).orElse("")).orElse("");
        PostgreSQLCommandCompletePacket result = new PostgreSQLCommandCompletePacket(sqlCommand, Math.max(dataRows, connectionContext.getUpdateCount()));
        connectionContext.clearContext();
        return result;
    }
}
