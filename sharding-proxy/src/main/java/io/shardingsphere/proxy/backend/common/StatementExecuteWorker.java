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

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    private final String dataSourceName;
    
    private final String realSQL;
    
    @Override
    public CommandResponsePackets call() {
        return execute(sqlStatement, dataSourceName, realSQL);
    }
    
    private CommandResponsePackets execute(final SQLStatement sqlStatement, final String dataSourceName, final String sql) {
        switch (sqlStatement.getType()) {
            case DQL:
            case DAL:
                return executeQuery(RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName), sql);
            case DML:
            case DDL:
                return executeUpdate(RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName), sql, sqlStatement);
            default:
                return executeCommon(RuleRegistry.getInstance().getDataSourceMap().get(dataSourceName), sql);
        }
    }
    
    private CommandResponsePackets executeQuery(final DataSource dataSource, final String sql) {
        if (ProxyMode.MEMORY_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            return executeQueryWithStreamResultSet(dataSource, sql);
        } else if (ProxyMode.CONNECTION_STRICTLY == ProxyMode.valueOf(RuleRegistry.getInstance().getProxyMode())) {
            return executeQueryWithNonStreamResultSet(dataSource, sql);
        } else {
            return new CommandResponsePackets(new ErrPacket(1, 0, "", "", "Invalid proxy.mode"));
        }
    }
    
    private void setJDBCPreparedStatementParameters(final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < statementExecuteBackendHandler.getComStmtExecuteParameters().size(); i++) {
            preparedStatement.setObject(i + 1, statementExecuteBackendHandler.getComStmtExecuteParameters().get(i));
        }
    }
    
    private CommandResponsePackets executeQueryWithStreamResultSet(final DataSource dataSource, final String sql) {
        try {
            Connection connection = dataSource.getConnection();
            statementExecuteBackendHandler.getConnections().add(connection);
            PreparedStatement preparedStatement = connection.prepareStatement(sql);
            preparedStatement.setFetchSize(FETCH_ONE_ROW_A_TIME);
            setJDBCPreparedStatementParameters(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            statementExecuteBackendHandler.getResultSets().add(resultSet);
            return getQueryDatabaseProtocolPackets(resultSet);
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeQueryWithNonStreamResultSet(final DataSource dataSource, final String sql) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)
        ) {
            setJDBCPreparedStatementParameters(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            ResultList resultList = new ResultList();
            while (resultSet.next()) {
                for (int i = 1; i <= resultSet.getMetaData().getColumnCount(); i++) {
                    resultList.add(resultSet.getObject(i));
                }
            }
            resultList.setIterator(resultList.getResultList().iterator());
            statementExecuteBackendHandler.getResultLists().add(resultList);
            return getQueryDatabaseProtocolPackets(resultSet);
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeUpdate(final DataSource dataSource, final String sql, final SQLStatement sqlStatement) {
        PreparedStatement preparedStatement = null;
        try (
            Connection connection = dataSource.getConnection()) {
            int affectedRows;
            long lastInsertId = 0;
            if (sqlStatement instanceof InsertStatement) {
                preparedStatement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
                setJDBCPreparedStatementParameters(preparedStatement);
                affectedRows = preparedStatement.executeUpdate();
                lastInsertId = getGeneratedKey(preparedStatement);
            } else {
                preparedStatement = connection.prepareStatement(sql);
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
    
    private CommandResponsePackets executeCommon(final DataSource dataSource, final String sql) {
        try (
            Connection connection = dataSource.getConnection();
            PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
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
    
    private CommandResponsePackets getQueryDatabaseProtocolPackets(final ResultSet resultSet) throws SQLException {
        CommandResponsePackets result = new CommandResponsePackets();
        int currentSequenceId = 0;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        statementExecuteBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            result.addPacket(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.addPacket(new FieldCountPacket(++currentSequenceId, columnCount));
        List<ColumnType> columnTypes = new ArrayList<>(128);
        for (int i = 1; i <= columnCount; i++) {
            ColumnType columnType = ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i));
            ColumnDefinition41Packet columnDefinition41Packet = new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnDisplaySize(i), columnType, 0);
            result.addPacket(columnDefinition41Packet);
            columnTypes.add(columnType);
        }
        statementExecuteBackendHandler.setColumnTypes(columnTypes);
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
        List<ColumnType> columnTypes = new ArrayList<>(128);
        for (int i = 1; i <= columnCount; i++) {
            ColumnType columnType = ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i));
            ColumnDefinition41Packet columnDefinition41Packet = new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i), resultSetMetaData.getColumnDisplaySize(i), columnType, 0);
            result.addPacket(columnDefinition41Packet);
            columnTypes.add(columnType);
        }
        statementExecuteBackendHandler.setColumnTypes(columnTypes);
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        while (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(resultSet.getObject(i));
            }
            result.addPacket(new BinaryResultSetRowPacket(++currentSequenceId, columnCount, data, columnTypes));
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
