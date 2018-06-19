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

package io.shardingsphere.proxy.backend.common;

import com.sun.rowset.CachedRowSetImpl;
import io.shardingsphere.core.parsing.parser.sql.SQLStatement;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.router.masterslave.MasterVisitedManager;
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.proxy.transport.mysql.constant.StatusFlag;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingsphere.proxy.transport.mysql.packet.command.statement.execute.BinaryResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.AllArgsConstructor;

import javax.sql.rowset.CachedRowSet;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Statement execute worker.
 *
 * @author zhangyonglun
 */
@AllArgsConstructor
public final class StatementExecuteWorker implements Callable<CommandResponsePackets> {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final StatementExecuteBackendHandler statementExecuteBackendHandler;
    
    private final SQLStatement sqlStatement;
    
    private final PreparedStatement preparedStatement;
    
    @Override
    public CommandResponsePackets call() {
        return execute(sqlStatement);
    }
    
    private CommandResponsePackets execute(final SQLStatement sqlStatement) {
        switch (sqlStatement.getType()) {
            case DQL:
            case DAL:
                return executeQuery();
            case DML:
            case DDL:
                return executeUpdate();
            default:
                return executeCommon();
        }
    }
    
    private CommandResponsePackets executeQuery() {
        if (ProxyMode.MEMORY_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            return executeQueryWithStreamResultSet();
        } else if (ProxyMode.CONNECTION_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            return executeQueryWithNonStreamResultSet();
        } else {
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "Invalid proxy.mode"));
        }
    }
    
    private void setJDBCPreparedStatementParameters(final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < statementExecuteBackendHandler.getComStmtExecuteParameters().size(); i++) {
            preparedStatement.setObject(i + 1, statementExecuteBackendHandler.getComStmtExecuteParameters().get(i));
        }
    }
    
    private CommandResponsePackets executeQueryWithStreamResultSet() {
        try {
            preparedStatement.setFetchSize(FETCH_ONE_ROW_A_TIME);
            setJDBCPreparedStatementParameters(preparedStatement);
            statementExecuteBackendHandler.getJdbcResource().getResultSets().add(preparedStatement.executeQuery());
            return getQueryDatabaseProtocolPackets();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeQueryWithNonStreamResultSet() {
        try {
            setJDBCPreparedStatementParameters(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            CachedRowSet cachedRowSet = new CachedRowSetImpl();
            cachedRowSet.populate(resultSet);
            statementExecuteBackendHandler.getJdbcResource().getResultSets().add(cachedRowSet);
            return getQueryDatabaseProtocolPackets();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeUpdate() {
        PreparedStatement preparedStatement = null;
        try {
            int affectedRows;
            long lastInsertId = 0;
            if (sqlStatement instanceof InsertStatement) {
                setJDBCPreparedStatementParameters(preparedStatement);
                affectedRows = preparedStatement.executeUpdate();
                lastInsertId = getGeneratedKey(preparedStatement);
            } else {
                setJDBCPreparedStatementParameters(preparedStatement);
                affectedRows = preparedStatement.executeUpdate();
            }
            return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        } finally {
            MasterVisitedManager.clear();
            if (null != preparedStatement) {
                try {
                    preparedStatement.close();
                } catch (final SQLException ignore) {
                }
            }
        }
    }
    
    private CommandResponsePackets executeCommon() {
        try {
            setJDBCPreparedStatementParameters(preparedStatement);
            boolean hasResultSet = preparedStatement.execute();
            if (hasResultSet) {
                return getCommonDatabaseProtocolPackets(preparedStatement.getResultSet());
            } else {
                return new CommandResponsePackets(new OKPacket(1, preparedStatement.getUpdateCount(), 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            }
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        } finally {
            MasterVisitedManager.clear();
        }
    }
    
    private CommandResponsePackets getQueryDatabaseProtocolPackets() throws SQLException {
        CommandResponsePackets result = new CommandResponsePackets();
        int currentSequenceId = 0;
        int lastIndex = statementExecuteBackendHandler.getJdbcResource().getResultSets().size() - 1;
        ResultSetMetaData resultSetMetaData = statementExecuteBackendHandler.getJdbcResource().getResultSets().get(lastIndex).getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        statementExecuteBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            result.addPacket(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.addPacket(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int i = 1; i <= columnCount; i++) {
            ColumnType columnType = ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i));
            ColumnDefinition41Packet columnDefinition41Packet = new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                    resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnDisplaySize(i), columnType, 0);
            result.addPacket(columnDefinition41Packet);
            statementExecuteBackendHandler.getColumnTypes().add(columnType);
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
    
    private CommandResponsePackets getCommonDatabaseProtocolPackets(final ResultSet resultSet) throws SQLException {
        CommandResponsePackets result = new CommandResponsePackets();
        int currentSequenceId = 0;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        if (0 == columnCount) {
            result.addPacket(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.addPacket(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int i = 1; i <= columnCount; i++) {
            ColumnType columnType = ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i));
            ColumnDefinition41Packet columnDefinition41Packet = new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                    resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnDisplaySize(i), columnType, 0);
            result.addPacket(columnDefinition41Packet);
            statementExecuteBackendHandler.getColumnTypes().add(columnType);
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        while (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(resultSet.getObject(i));
            }
            result.addPacket(new BinaryResultSetRowPacket(++currentSequenceId, columnCount, data, statementExecuteBackendHandler.getColumnTypes()));
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
    
    private long getGeneratedKey(final PreparedStatement preparedStatement) throws SQLException {
        long result = 0;
        ResultSet resultSet = preparedStatement.getGeneratedKeys();
        if (resultSet.next()) {
            result = resultSet.getLong(1);
        }
        return result;
    }
}
