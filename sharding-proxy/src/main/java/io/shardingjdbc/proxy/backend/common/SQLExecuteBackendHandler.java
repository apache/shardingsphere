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
import io.shardingjdbc.core.routing.SQLExecutionUnit;
import io.shardingjdbc.core.routing.SQLRouteResult;
import io.shardingjdbc.core.routing.StatementRoutingEngine;
import io.shardingjdbc.proxy.backend.mysql.MySQLPacketQueryResult;
import io.shardingjdbc.proxy.config.ShardingRuleRegistry;
import io.shardingjdbc.proxy.transport.mysql.constant.ColumnType;
import io.shardingjdbc.proxy.transport.mysql.constant.StatusFlag;
import io.shardingjdbc.proxy.transport.common.packet.DatabaseProtocolPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.FieldCountPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.TextResultSetRowPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.command.text.query.ColumnDefinition41Packet;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.EofPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.ErrPacket;
import io.shardingjdbc.proxy.transport.mysql.packet.generic.OKPacket;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL execute backend handler.
 *
 * @author zhangliang
 */
public final class SQLExecuteBackendHandler implements BackendHandler {
    
    private final String sql;
    
    private final StatementRoutingEngine routingEngine;
    
    public SQLExecuteBackendHandler(final String sql, final DatabaseType databaseType, final boolean showSQL) {
        this.sql = sql;
        routingEngine = new StatementRoutingEngine(ShardingRuleRegistry.getInstance().getShardingRule(), databaseType, showSQL);
    }
    
    @Override
    public List<DatabaseProtocolPacket> execute() {
        SQLRouteResult routeResult = routingEngine.route(sql);
        if (routeResult.getExecutionUnits().isEmpty()) {
            return Collections.<DatabaseProtocolPacket>singletonList(new OKPacket(1, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        }
        List<List<DatabaseProtocolPacket>> result = new LinkedList<>();
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            // TODO multiple threads
            result.add(execute(routeResult.getSqlStatement(), each));
        }
        return merge(routeResult.getSqlStatement(), result);
    }
    
    private List<DatabaseProtocolPacket> execute(final SQLStatement sqlStatement, final SQLExecutionUnit sqlExecutionUnit) {
        switch (sqlStatement.getType()) {
            case DQL:
                return executeQuery(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql());
            case DML:
            case DDL:
                return executeUpdate(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql(), sqlStatement);
            default:
                return executeCommon(ShardingRuleRegistry.getInstance().getDataSourceMap().get(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSql());
        }
    }
    
    private List<DatabaseProtocolPacket> executeQuery(final DataSource dataSource, final String sql) {
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement();
                ResultSet resultSet = statement.executeQuery(sql)) {
            return getDatabaseProtocolPackets(resultSet);
        } catch (final SQLException ex) {
            return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private List<DatabaseProtocolPacket> executeCommon(final DataSource dataSource, final String sql) {
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()) {
            boolean hasResultSet = statement.execute(sql);
            if (hasResultSet) {
                return getDatabaseProtocolPackets(statement.getResultSet());
            } else {
                return Collections.<DatabaseProtocolPacket>singletonList(new OKPacket(1, statement.getUpdateCount(), 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, "")); 
            }
        } catch (final SQLException ex) {
            return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private List<DatabaseProtocolPacket> getDatabaseProtocolPackets(final ResultSet resultSet) throws SQLException {
        List<DatabaseProtocolPacket> result = new LinkedList<>();
        int currentSequenceId = 0;
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        int columnCount = resultSetMetaData.getColumnCount();
        if (0 == columnCount) {
            result.add(new OKPacket(++currentSequenceId, 0, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
            return result;
        }
        result.add(new FieldCountPacket(++currentSequenceId, columnCount));
        for (int i = 1; i <= columnCount; i++) {
            result.add(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData.getSchemaName(i), resultSetMetaData.getTableName(i),
                    resultSetMetaData.getTableName(i), resultSetMetaData.getColumnLabel(i), resultSetMetaData.getColumnName(i),
                    resultSetMetaData.getColumnDisplaySize(i), ColumnType.valueOfJDBCType(resultSetMetaData.getColumnType(i)), 0));
        }
        result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        while (resultSet.next()) {
            List<Object> data = new ArrayList<>(columnCount);
            for (int i = 1; i <= columnCount; i++) {
                data.add(resultSet.getObject(i));
            }
            result.add(new TextResultSetRowPacket(++currentSequenceId, data));
        }
        result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
    
    private List<DatabaseProtocolPacket> executeUpdate(final DataSource dataSource, final String sql, final SQLStatement sqlStatement) {
        try (
                Connection conn = dataSource.getConnection();
                Statement statement = conn.createStatement()) {
            int affectedRows;
            long lastInsertId = 0;
            if (sqlStatement instanceof InsertStatement) {
                affectedRows = statement.executeUpdate(sql, Statement.RETURN_GENERATED_KEYS);
                lastInsertId = getGeneratedKey(statement);
            } else {
                affectedRows = statement.executeUpdate(sql);
            }
            return Collections.<DatabaseProtocolPacket>singletonList(new OKPacket(1, affectedRows, lastInsertId, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
        } catch (final SQLException ex) {
            return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        long result = 0;
        ResultSet resultSet = statement.getGeneratedKeys();
        if (resultSet.next()) {
            result = resultSet.getLong(1);
        }
        return result;
    }
    
    private List<DatabaseProtocolPacket> merge(final SQLStatement sqlStatement, final List<List<DatabaseProtocolPacket>> packets) {
        if (1 == packets.size()) {
            return packets.iterator().next();
        }
        List<DatabaseProtocolPacket> firstPackets = new LinkedList<>();
        for (List<DatabaseProtocolPacket> each : packets) {
            firstPackets.add(each.get(0));
        }
        for (DatabaseProtocolPacket each : firstPackets) {
            if (each instanceof ErrPacket) {
                return Collections.singletonList(each);
            }
        }
        if (SQLType.DML == sqlStatement.getType()) {
            return mergeDML(firstPackets);
        }
        if (SQLType.DQL == sqlStatement.getType() || SQLType.DAL == sqlStatement.getType()) {
            return mergeDQLorDAL(sqlStatement, packets);
        }
        return packets.get(0);
    }
    
    private List<DatabaseProtocolPacket> mergeDML(final List<DatabaseProtocolPacket> firstPackets) {
        int affectedRows = 0;
        for (DatabaseProtocolPacket each : firstPackets) {
            if (each instanceof OKPacket) {
                OKPacket okPacket = (OKPacket) each;
                affectedRows += okPacket.getAffectedRows();
            }
        }
        return Collections.<DatabaseProtocolPacket>singletonList(new OKPacket(1, affectedRows, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue(), 0, ""));
    }
    
    private List<DatabaseProtocolPacket> mergeDQLorDAL(final SQLStatement sqlStatement, final List<List<DatabaseProtocolPacket>> packets) {
        List<QueryResult> queryResults = new ArrayList<>(packets.size());
        for (List<DatabaseProtocolPacket> each : packets) {
            // TODO replace to a common PacketQueryResult
            queryResults.add(new MySQLPacketQueryResult(each));
        }
        MergedResult mergedResult;
        try {
            mergedResult = MergeEngineFactory.newInstance(ShardingRuleRegistry.getInstance().getShardingRule(), queryResults, sqlStatement).merge();
        } catch (final SQLException ex) {
            return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        return buildPackets(packets, mergedResult);
    }
    
    private List<DatabaseProtocolPacket> buildPackets(final List<List<DatabaseProtocolPacket>> packets, final MergedResult mergedResult) {
        List<DatabaseProtocolPacket> result = new LinkedList<>();
        Iterator<DatabaseProtocolPacket> databaseProtocolPacketsSampling = packets.iterator().next().iterator();
        FieldCountPacket fieldCountPacketSampling = (FieldCountPacket) databaseProtocolPacketsSampling.next();
        result.add(fieldCountPacketSampling);
        int columnCount = fieldCountPacketSampling.getColumnCount();
        for (int i = 0; i < columnCount; i++) {
            result.add(databaseProtocolPacketsSampling.next());
        }
        result.add(databaseProtocolPacketsSampling.next());
        int currentSequenceId = result.size();
        try {
            while (mergedResult.next()) {
                List<Object> data = new ArrayList<>(columnCount);
                for (int i = 1; i <= columnCount; i++) {
                    data.add(mergedResult.getValue(i, Object.class));
                }
                result.add(new TextResultSetRowPacket(++currentSequenceId, data));
            }
        } catch (final SQLException ex) {
            return Collections.<DatabaseProtocolPacket>singletonList(new ErrPacket(1, ex.getErrorCode(), "", ex.getSQLState(), ex.getMessage()));
        }
        result.add(new EofPacket(++currentSequenceId, 0, StatusFlag.SERVER_STATUS_AUTOCOMMIT.getValue()));
        return result;
    }
}
