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

package io.shardingsphere.proxy.backend.common.jdbc.execute;

import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.proxy.backend.common.SQLExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.BackendConnection;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingsphere.proxy.util.ExecutorContext;
import lombok.Getter;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;

/**
 * SQL Execute engine for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 */
@Getter
@Setter
public abstract class JDBCExecuteEngine implements SQLExecuteEngine {
    
    private final List<QueryResult> queryResults = new LinkedList<>();
    
    private final BackendConnection backendConnection = new BackendConnection();
    
    private final ExecutorService executorService = ExecutorContext.getInstance().getExecutorService();
    
    private int columnCount;
    
    private List<ColumnType> columnTypes;
    
    protected abstract Statement createStatement(Connection connection, String sql, boolean isReturnGeneratedKeys) throws SQLException;
    
    protected JDBCExecuteResponse executeWithMetadata(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) {
        try {
            setFetchSize(statement);
            if (!executeSQL(statement, sql, isReturnGeneratedKeys)) {
                return new JDBCExecuteResponse(new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0)));
            }
            ResultSet resultSet = statement.getResultSet();
            return new JDBCExecuteResponse(getHeaderPackets(resultSet.getMetaData()), createQueryResult(resultSet));
        } catch (final SQLException ex) {
            return new JDBCExecuteResponse(new CommandResponsePackets(new ErrPacket(1, ex)));
        }
    }
    
    protected JDBCExecuteResponse executeWithoutMetadata(final Statement statement, final String sql, final boolean isReturnGeneratedKeys) {
        try {
            setFetchSize(statement);
            if (!executeSQL(statement, sql, isReturnGeneratedKeys)) {
                return new JDBCExecuteResponse(new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0)));
            }
            return new JDBCExecuteResponse(createQueryResult(statement.getResultSet()));
        } catch (final SQLException ex) {
            return new JDBCExecuteResponse(new CommandResponsePackets(new ErrPacket(1, ex)));
        }
    }
    
    protected abstract void setFetchSize(Statement statement) throws SQLException;
    
    protected abstract boolean executeSQL(Statement statement, String sql, boolean isReturnGeneratedKeys) throws SQLException;
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
    
    private CommandResponsePackets getHeaderPackets(final ResultSetMetaData resultSetMetaData) throws SQLException {
        int currentSequenceId = 0;
        int columnCount = resultSetMetaData.getColumnCount();
        if (0 == columnCount) {
            return new CommandResponsePackets(new OKPacket(++currentSequenceId));
        }
        CommandResponsePackets result = new CommandResponsePackets(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            result.getPackets().add(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData, columnIndex));
        }
        result.getPackets().add(new EofPacket(++currentSequenceId));
        return result;
    }
    
    protected abstract QueryResult createQueryResult(ResultSet resultSet) throws SQLException;
}
