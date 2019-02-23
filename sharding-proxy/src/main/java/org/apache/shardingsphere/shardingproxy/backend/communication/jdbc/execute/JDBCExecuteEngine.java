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
import org.apache.shardingsphere.core.constant.ConnectionMode;
import org.apache.shardingsphere.core.constant.DatabaseType;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
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
import org.apache.shardingsphere.shardingproxy.runtime.schema.LogicSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.MasterSlaveSchema;
import org.apache.shardingsphere.shardingproxy.runtime.schema.ShardingSchema;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.DataHeaderPacket;
import org.apache.shardingsphere.shardingproxy.transport.common.packet.command.query.QueryResponsePackets;

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
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    private static final Integer MEMORY_FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    private final DatabaseType databaseType = GlobalRegistry.getInstance().getDatabaseType();
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(ExecutorContext.getInstance().getExecuteEngine(), backendConnection.isSerialExecute());
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
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.executeGroup((Collection) sqlExecuteGroups, firstProxySQLExecuteCallback, proxySQLExecuteCallback);
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
    
    private ExecuteResponseUnit executeSQL(
            final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean isReturnGeneratedKeys, final boolean withMetadata) throws SQLException {
        backendConnection.add(statement);
        if (!jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            return new ExecuteUpdateResponseUnit(statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0L);
        }
        ResultSet resultSet = statement.getResultSet();
        backendConnection.add(resultSet);
        return new ExecuteQueryResponseUnit(withMetadata ? getHeaderPackets(resultSet.getMetaData()) : null, createQueryResult(resultSet, connectionMode));
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
        LogicSchema logicSchema = backendConnection.getLogicSchema();
        if (logicSchema instanceof MasterSlaveSchema) {
            return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
        }
        ShardingRule shardingRule = ((ShardingSchema) logicSchema).getShardingRule();
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
            super(databaseType, isExceptionThrown);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
            if (hasMetaData) {
                return JDBCExecuteEngine.this.executeSQL(
                        statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys, false);
            } else {
                hasMetaData = true;
                return JDBCExecuteEngine.this.executeSQL(
                        statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys, true);
            }
        }
    }
    
    private final class ProxyJDBCExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private ProxyJDBCExecuteCallback(final boolean isExceptionThrown, final boolean isReturnGeneratedKeys) {
            super(databaseType, isExceptionThrown);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
            return JDBCExecuteEngine.this.executeSQL(
                    statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys, false);
        }
    }
}
