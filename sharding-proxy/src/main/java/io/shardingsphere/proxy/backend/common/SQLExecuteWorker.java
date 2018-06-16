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
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingsphere.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingsphere.proxy.transport.mysql.packet.generic.OKPacket;
import lombok.AllArgsConstructor;

import javax.sql.DataSource;
import javax.sql.rowset.CachedRowSet;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * SQL execute worker.
 *
 * @author zhangyonglun
 */
@AllArgsConstructor
public final class SQLExecuteWorker implements Callable<CommandResponsePackets> {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final SQLExecuteBackendHandler sqlExecuteBackendHandler;
    
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
    
    private CommandResponsePackets executeQueryWithStreamResultSet(final DataSource dataSource, final String sql) {
        try {
            Connection connection = dataSource.getConnection();
            sqlExecuteBackendHandler.getConnections().add(connection);
            Statement statement = connection.createStatement();
            statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
            sqlExecuteBackendHandler.getResultSets().add(statement.executeQuery(sql));
            return getQueryDatabaseProtocolPackets();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeQueryWithNonStreamResultSet(final DataSource dataSource, final String sql) {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql)
        ) {
            CachedRowSet cachedRowSet = new CachedRowSetImpl();
            cachedRowSet.populate(resultSet);
            sqlExecuteBackendHandler.getResultSets().add(cachedRowSet);
            return getQueryDatabaseProtocolPackets();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets executeUpdate(final DataSource dataSource, final String sql, final SQLStatement sqlStatement) {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            int affectedRows;
            long lastInsertId = 0;
            if (sqlStatement instanceof InsertStatement) {
                affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                lastInsertId = getGeneratedKey(statement);
            } else {
                affectedRows = statement.executeUpdate(sql);
            }
            return new CommandResponsePackets(new OKPacket(1, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        } finally {
            MasterVisitedManager.clear();
        }
    }
    
    private CommandResponsePackets executeCommon(final DataSource dataSource, final String sql) {
        try (
            Connection connection = dataSource.getConnection();
            Statement statement = connection.createStatement()) {
            boolean hasResultSet = statement.execute(sql);
            if (hasResultSet) {
                return getCommonDatabaseProtocolPackets(statement.getResultSet());
            } else {
                return new CommandResponsePackets(new OKPacket(1, statement.getUpdateCount(), 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
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
        ResultSetMetaData resultSetMetaData = sqlExecuteBackendHandler.getResultSets().get(sqlExecuteBackendHandler.getResultSets().size() - 1).getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        sqlExecuteBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            result.addPacket(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.addPacket(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int i = 1; i <= columnCount; i++) {
            result.addPacket(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i),
                resultSetMetaData.getColumnDisplaySize(i), ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i)), 0));
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
    
    private CommandResponsePackets getCommonDatabaseProtocolPackets(final ResultSet resultSet) throws SQLException {
        CommandResponsePackets result = new CommandResponsePackets();
        int currentSequenceId = 0;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        sqlExecuteBackendHandler.setColumnCount(columnCount);
        if (0 == columnCount) {
            result.addPacket(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.addPacket(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int i = 1; i <= columnCount; i++) {
            result.addPacket(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i),
                resultSetMetaData.getColumnDisplaySize(i), ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i)), 0));
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        while (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(resultSet.getObject(i));
            }
            result.addPacket(new TextResultSetRowPacket(++currentSequenceId, data));
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        long result = 0;
        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            result = resultSet.getLong(1);
        }
        return result;
    }
}
