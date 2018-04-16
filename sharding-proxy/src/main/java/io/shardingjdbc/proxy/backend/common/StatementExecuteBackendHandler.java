/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.proxy.backend.common;

import io.shardingjdbc.core.constant.DatabaseType;
import io.shardingjdbc.core.constant.SQLType;
import io.shardingjdbc.core.merger.MergeEngineFactory;
import io.shardingjdbc.core.merger.MergedResult;
import io.shardingjdbc.core.merger.QueryResult;
import io.shardingjdbc.core.parsing.parser.sql.SQLStatement;
import io.shardingjdbc.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingjdbc.core.routing.PreparedStatementRoutingEngine;
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.proxy.backend.mysql.MySQLPacketStatementExecuteQueryResult;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.mysql.packet.command.CommandResponsePackets;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute.BinaryResultSetRowPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.execute.PreparedStatementParameter;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.statement.PreparedStatementRegistry;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Statement execute backend handler.
 *
 * @author zhangyonglun
 */
public final class StatementExecuteBackendHandler implements BackendHandler {
    
    private final List<PreparedStatementParameter> preparedStatementParameters;
    
    private final PreparedStatementRoutingEngine routingEngine;
    
    public StatementExecuteBackendHandler(final List<PreparedStatementParameter> preparedStatementParameters, final int statementId, final DatabaseType databaseType, final boolean showSQL) {
        this.preparedStatementParameters = preparedStatementParameters;
        routingEngine = new PreparedStatementRoutingEngine(PreparedStatementRegistry.getInstance().getSql(statementId), ShardingRuleRegistry.getInstance().getShardingRule(), databaseType, showSQL);
    }
    
    @Override
    public CommandResponsePackets execute() {
        SQLRouteResult routeResult = routingEngine.route(getComStmtExecuteParameters());
        if (routeResult.getExecutionUnits().isEmpty()) {
            return new CommandResponsePackets(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        List<ColumnType> columnTypes = new ArrayList<>();
        List<CommandResponsePackets> result = new LinkedList<>();
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            // TODO multiple threads
            result.add(execute(routeResult.getSqlStatement(), each, columnTypes));
        }
        return merge(routeResult.getSqlStatement(), result, columnTypes);
    }
    
    private CommandResponsePackets execute(final SQLStatement sqlStatement, final SQLExecutionUnit sqlExecutionUnit, final List<ColumnType> columnTypes) {
        switch (sqlStatement.getType()) {
            case DQL:
                return executeQuery(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql(), columnTypes);
            case DML:
            case DDL:
                return executeUpdate(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql(), sqlStatement);
            default:
                return executeCommon(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql(), columnTypes);
        }
    }
    
    private List<Object> getComStmtExecuteParameters() {
        List<Object> result = new ArrayList<>();
        for (PreparedStatementParameter each : preparedStatementParameters) {
            result.add(each.getValue());
        }
        return result;
    }
    
    private void setJDBCPreparedStatementParameters(final PreparedStatement preparedStatement) throws SQLException {
        for (int i = 0; i < getComStmtExecuteParameters().size(); i++) {
            preparedStatement.setObject(i + 1, getComStmtExecuteParameters().get(i));
        }
    }
    
    private CommandResponsePackets executeQuery(final DataSource dataSource, final String sql, final List<ColumnType> columnTypes) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setJDBCPreparedStatementParameters(preparedStatement);
            ResultSet resultSet = preparedStatement.executeQuery();
            return getDatabaseProtocolPackets(resultSet, columnTypes);
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
            if (preparedStatement != null) {
                try {
                    preparedStatement.close();
                } catch (final SQLException ignore) {
                }
            }
        }
        
    }
    
    private CommandResponsePackets executeCommon(final DataSource dataSource, final String sql, final List<ColumnType> columnTypes) {
        try (
                Connection connection = dataSource.getConnection();
                PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
            setJDBCPreparedStatementParameters(preparedStatement);
            boolean hasResultSet = preparedStatement.execute();
            if (hasResultSet) {
                return getDatabaseProtocolPackets(preparedStatement.getResultSet(), columnTypes);
            } else {
                return new CommandResponsePackets(new OKPacket(1, preparedStatement.getUpdateCount(), 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            }
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private CommandResponsePackets getDatabaseProtocolPackets(final ResultSet resultSet, final List<ColumnType> columnTypes) throws SQLException {
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
            columnTypes.add(columnType);
        }
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
    
    private CommandResponsePackets merge(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<ColumnType> columnTypes) {
        if (1 == packets.size()) {
            return packets.iterator().next();
        }
        CommandResponsePackets headPackets = new CommandResponsePackets();
        for (CommandResponsePackets each : packets) {
            headPackets.addPacket(each.getHeadPacket());
        }
        for (DatabaseProtocolPacket each : headPackets.getDatabaseProtocolPackets()) {
            if (each instanceof ErrPacket) {
                return new CommandResponsePackets(each);
            }
        }
        if (SQLType.DML == sqlStatement.getType()) {
            return mergeDML(headPackets);
        }
        if (SQLType.DQL == sqlStatement.getType() || SQLType.DAL == sqlStatement.getType()) {
            return mergeDQLorDAL(sqlStatement, packets, columnTypes);
        }
        return packets.get(0);
    }
    
    private CommandResponsePackets mergeDML(final CommandResponsePackets firstPackets) {
        int affectedRows = 0;
        for (DatabaseProtocolPacket each : firstPackets.getDatabaseProtocolPackets()) {
            if (each instanceof OKPacket) {
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
            }
        }
        return new CommandResponsePackets(new OKPacket(1, affectedRows, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    private CommandResponsePackets mergeDQLorDAL(final SQLStatement sqlStatement, final List<CommandResponsePackets> packets, final List<ColumnType> columnTypes) {
        List<QueryResult> queryResults = new ArrayList<>(packets.size());
        for (CommandResponsePackets each : packets) {
            // TODO replace to a common PacketQueryResult
            queryResults.add(new MySQLPacketStatementExecuteQueryResult(each));
        }
        MergedResult mergedResult;
        try {
            mergedResult = MergeEngineFactory.newInstance(ShardingRuleRegistry.getInstance().getShardingRule(), queryResults, sqlStatement).merge();
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        return buildPackets(packets, mergedResult, columnTypes);
    }
    
    private CommandResponsePackets buildPackets(final List<CommandResponsePackets> packets, final MergedResult mergedResult, final List<ColumnType> columnTypes) {
        CommandResponsePackets result = new CommandResponsePackets();
        Iterator<DatabaseProtocolPacket> databaseProtocolPacketsSampling = packets.iterator().next().getDatabaseProtocolPackets().iterator();
        FieldCountPacket fieldCountPacketSampling = (FieldCountPacket) databaseProtocolPacketsSampling.next();
        result.addPacket(fieldCountPacketSampling);
        int columnCount = fieldCountPacketSampling.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            result.addPacket(databaseProtocolPacketsSampling.next());
        }
        result.addPacket(databaseProtocolPacketsSampling.next());
        int currentSequenceId = result.size();
        try {
            while (mergedResult.next()) {
                List<Object> data = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    data.add(mergedResult.getValue(i, Object.class));
                }
                result.addPacket(new BinaryResultSetRowPacket(++currentSequenceId, columnCount, data, columnTypes));
            }
        } catch (final SQLException ex) {
            return new CommandResponsePackets(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        result.addPacket(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
}
