/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.proxy.backend.common.jdbc;

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.backend.common.ProxyMode;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Execute worker via JDBC to connect databases.
 * 
 * @author zhaojun
 */
@RequiredArgsConstructor
public abstract class JDBCExecuteWorker implements Callable<CommandResponsePackets> {
    
    private final SQLType sqlType;
    
    @Getter
    private final JDBCBackendHandler jdbcBackendHandler;
    
    @Override
    public CommandResponsePackets call() {
        try {
            return execute();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex));
        } finally {
            MasterVisitedManager.clear();
        }
    }
    
    private CommandResponsePackets execute() throws SQLException {
        switch (sqlType) {
            case DQL:
            case DAL:
                return executeQuery();
            case DML:
            case DDL:
                return executeUpdate();
            default:
                // TODO when go to here? can DCL and TCL use executeUpdate? 
                return executeCommon();
        }
    }
    
    private CommandResponsePackets executeQuery() throws SQLException {
        return ProxyMode.MEMORY_STRICTLY == RuleRegistry.getInstance().getProxyMode() ? executeQueryWithStreamResultSet() : executeQueryWithMemoryResultSet();
    }
    
    protected abstract CommandResponsePackets executeQueryWithStreamResultSet() throws SQLException;
    
    protected abstract CommandResponsePackets executeQueryWithMemoryResultSet() throws SQLException;
    
    protected abstract CommandResponsePackets executeUpdate() throws SQLException;
    
    protected abstract CommandResponsePackets executeCommon() throws SQLException;
    
    protected final CommandResponsePackets getHeaderPackets(final ResultSetMetaData resultSetMetaData) throws SQLException {
        int currentSequenceId = 0;
        int columnCount = resultSetMetaData.getColumnCount();
        jdbcBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            return new CommandResponsePackets(new OKPacket(++currentSequenceId));
        }
        CommandResponsePackets result = new CommandResponsePackets(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            setColumnType(ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(columnIndex)));
            result.addPacket(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData, columnIndex));
        }
        result.addPacket(new EofPacket(++currentSequenceId));
        return result;
    }
    
    protected void setColumnType(final ColumnType columnType) {
    }
    
    protected final CommandResponsePackets getCommonDatabaseProtocolPackets(final ResultSet resultSet) throws SQLException {
        int currentSequenceId = 0;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        jdbcBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            return new CommandResponsePackets(new OKPacket(++currentSequenceId));
        }
        CommandResponsePackets result = new CommandResponsePackets(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.addPacket(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData, columnIndex));
        }
        result.addPacket(new EofPacket(++currentSequenceId));
        while (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
                data.add(resultSet.getObject(columnIndex));
            }
            result.addPacket(new TextResultSetRowPacket(++currentSequenceId, data));
        }
        result.addPacket(new EofPacket(++currentSequenceId));
        return result;
    }
    
    protected final long getGeneratedKey(final Statement statement) throws SQLException {
        long result = 0;
        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            result = resultSet.getLong(1);
        }
        return result;
    }
}
