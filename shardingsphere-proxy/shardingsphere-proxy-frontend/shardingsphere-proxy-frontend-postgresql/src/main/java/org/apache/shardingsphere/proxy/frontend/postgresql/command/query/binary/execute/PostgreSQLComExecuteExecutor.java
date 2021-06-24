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
import org.apache.shardingsphere.proxy.frontend.postgresql.command.PostgreSQLConnectionContext;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.PostgreSQLCommand;
import org.apache.shardingsphere.proxy.frontend.postgresql.command.query.binary.PostgreSQLPortal;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.EmptyStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.CommitStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.tcl.RollbackStatement;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Command execute executor for PostgreSQL.
 */
@RequiredArgsConstructor
public final class PostgreSQLComExecuteExecutor implements CommandExecutor {
    
    private final PostgreSQLConnectionContext connectionContext;
    
    private final PostgreSQLComExecutePacket packet;
    
    private long dataRows;
    
    @Override
    public Collection<DatabasePacket<?>> execute() throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        for (CommandExecutor each : connectionContext.getPendingExecutors()) {
            result.addAll(each.execute());
        }
        connectionContext.getPendingExecutors().clear();
        result.addAll(doExecute());
        result.add(createExecutionCompletedPacket());
        return result;
    }
    
    private Collection<? extends DatabasePacket<?>> doExecute() throws SQLException {
        Collection<DatabasePacket<?>> result = new LinkedList<>();
        while (!isPortalSuspended()) {
            Optional<DatabasePacket<?>> packet = getPacketFromPortal();
            if (!packet.isPresent()) {
                break;
            }
            dataRows++;
            result.add(packet.get());
        }
        return result;
    }
    
    private Optional<DatabasePacket<?>> getPacketFromPortal() throws SQLException {
        PostgreSQLPortal portal = connectionContext.getPortal(packet.getPortal());
        return portal.next() ? Optional.of(portal.nextPacket()) : Optional.empty();
    }
    
    private PostgreSQLIdentifierPacket createExecutionCompletedPacket() {
        if (isPortalSuspended()) {
            return new PostgreSQLPortalSuspendedPacket();
        }
        if (connectionContext.getSqlStatement().map(EmptyStatement.class::isInstance).orElse(false)) {
            return new PostgreSQLEmptyQueryResponsePacket();
        }
        String sqlCommand = connectionContext.getSqlStatement().map(SQLStatement::getClass).map(PostgreSQLCommand::valueOf).map(command -> command.map(Enum::name).orElse("")).orElse("");
        PostgreSQLCommandCompletePacket result = new PostgreSQLCommandCompletePacket(sqlCommand, Math.max(dataRows, connectionContext.getUpdateCount()));
        connectionContext.clearContext();
        return result;
    }
    
    @Override
    public void close() throws SQLException {
        if (!isPortalSuspended()) {
            connectionContext.getPortal(packet.getPortal()).close();
        }
        if (connectionContext.getSqlStatement().isPresent()
                && (connectionContext.getSqlStatement().get() instanceof CommitStatement || connectionContext.getSqlStatement().get() instanceof RollbackStatement)) {
            connectionContext.closeAllPortals();
        }
    }
    
    private boolean isPortalSuspended() {
        return packet.getMaxRows() > 0 && dataRows == packet.getMaxRows();
    }
}
