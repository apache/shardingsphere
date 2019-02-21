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

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.executor.ShardingExecuteEngine;
import org.apache.shardingsphere.core.executor.ShardingExecuteGroup;
import org.apache.shardingsphere.core.executor.StatementExecuteUnit;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import org.apache.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import org.apache.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import org.apache.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import org.apache.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.core.merger.QueryResult;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.routing.RouteUnit;
import org.apache.shardingsphere.core.routing.SQLRouteResult;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteUpdateResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.runtime.ExecutorContext;
import org.apache.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.generic.DatabaseSuccessPacket;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * SQL Execute engine for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 */
@RequiredArgsConstructor
@Getter
@Setter
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    private static final Integer MEMORY_FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private int columnCount;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ShardingExecuteEngine executeEngine = ExecutorContext.getInstance().getExecuteEngine();
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine, backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getSqlStatement() instanceof InsertStatement;
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> sqlExecuteGroups =
                sqlExecutePrepareTemplate.getExecuteUnitGroups(routeResult.getRouteUnits(), new ProxyJDBCExecutePrepareCallback(isReturnGeneratedKeys));
        SQLExecuteCallback<ExecuteResponseUnit> firstProxySQLExecuteCallback = new FirstProxyJDBCExecuteCallback(isExceptionThrown, isReturnGeneratedKeys);
        SQLExecuteCallback<ExecuteResponseUnit> proxySQLExecuteCallback = new ProxyJDBCExecuteCallback(isExceptionThrown, isReturnGeneratedKeys);
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.executeGroup((Collection) sqlExecuteGroups,
                firstProxySQLExecuteCallback, proxySQLExecuteCallback);
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse(((ExecuteQueryResponseUnit) firstExecuteResponseUnit).getQueryResponsePackets(), executeResponseUnits) : new ExecuteUpdateResponse(executeResponseUnits);
    }
    
    private ExecuteResponse getExecuteQueryResponse(final QueryResponsePackets queryResponsePackets, final Collection<ExecuteResponseUnit> executeResponseUnits) {
        ExecuteQueryResponse result = new ExecuteQueryResponse(queryResponsePackets);
        for (ExecuteResponseUnit each : executeResponseUnits) {
            result.getQueryResults().add(((ExecuteQueryResponseUnit) each).getQueryResult());
        }
        return result;
    }
    
    private ExecuteResponseUnit executeWithMetadata(final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean isReturnGeneratedKeys) throws SQLException {
        backendConnection.add(statement);
        if (!jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            return new ExecuteUpdateResponseUnit(new DatabaseSuccessPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L));
        }
        ResultSet resultSet = statement.getResultSet();
        backendConnection.add(resultSet);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        if (0 == resultSetMetaData.getColumnCount()) {
            return new ExecuteUpdateResponseUnit(new DatabaseSuccessPacket(1, 0L, 0L));
        }
        return new ExecuteQueryResponseUnit(getHeaderPackets(resultSetMetaData), createQueryResult(resultSet, connectionMode));
    }
    
    private ExecuteResponseUnit executeWithoutMetadata(final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean isReturnGeneratedKeys) throws SQLException {
        backendConnection.add(statement);
        if (!jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            return new ExecuteUpdateResponseUnit(new DatabaseSuccessPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0));
        }
        ResultSet resultSet = statement.getResultSet();
        backendConnection.add(resultSet);
        return new ExecuteQueryResponseUnit(null, createQueryResult(resultSet, connectionMode));
    }
    
    private long getGeneratedKey(final Statement statement) throws SQLException {
        ResultSet resultSet = statement.getGeneratedKeys();
        return resultSet.next() ? resultSet.getLong(1) : 0L;
    }
    
    private QueryResponsePackets getHeaderPackets(final ResultSetMetaData resultSetMetaData) throws SQLException {
        int currentSequenceId = 1;
        int columnCount = resultSetMetaData.getColumnCount();
        Collection<DataHeaderPacket> dataHeaderPackets = new LinkedList<>();
        List<Integer> columnTypes = new ArrayList<>(128);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            dataHeaderPackets.add(new DataHeaderPacket(++currentSequenceId, resultSetMetaData, backendConnection.getLogicSchema(), columnIndex));
            columnTypes.add(resultSetMetaData.getColumnType(columnIndex));
        }
        return new QueryResponsePackets(columnTypes, columnCount, dataHeaderPackets, ++currentSequenceId);
    }
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) {
        ShardingRule shardingRule = ((ShardingSchema) backendConnection.getLogicSchema()).getShardingRule();
        return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet, shardingRule) : new MemoryQueryResult(resultSet, shardingRule);
    }
    
    @RequiredArgsConstructor
    private final class ProxyJDBCExecutePrepareCallback implements SQLExecutePrepareCallback {
        
        private final boolean isReturnGeneratedKeys;
        
        @Override
        public List<Connection> getConnections(final ConnectionMode connectionMode, final String dataSourceName, final int connectionSize) throws SQLException {
            return getBackendConnection().getConnections(connectionMode, dataSourceName, connectionSize);
        }
        
        @Override
        public StatementExecuteUnit createStatementExecuteUnit(final Connection connection, final RouteUnit routeUnit, final ConnectionMode connectionMode) throws SQLException {
            Statement statement = getJdbcExecutorWrapper().createStatement(connection, routeUnit.getSqlUnit(), isReturnGeneratedKeys);
            if (connectionMode.equals(ConnectionMode.MEMORY_STRICTLY)) {
                statement.setFetchSize(MEMORY_FETCH_ONE_ROW_A_TIME);
            }
            return new StatementExecuteUnit(routeUnit, statement, connectionMode);
        }
    }
    
    private final class FirstProxyJDBCExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private boolean hasMetaData;
        
        private FirstProxyJDBCExecuteCallback(final boolean isExceptionThrown, final boolean isReturnGeneratedKeys) {
            super(GlobalRegistry.getInstance().getDatabaseType(), isExceptionThrown);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
            if (hasMetaData) {
                return executeWithoutMetadata(
                        statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys);
            } else {
                hasMetaData = true;
                return executeWithMetadata(
                        statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys);
            }
        }
    }
    
    private final class ProxyJDBCExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private ProxyJDBCExecuteCallback(final boolean isExceptionThrown, final boolean isReturnGeneratedKeys) {
            super(GlobalRegistry.getInstance().getDatabaseType(), isExceptionThrown);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
            return executeWithoutMetadata(
                    statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys);
        }
    }
}
