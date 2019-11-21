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

package io.shardingsphere.shardingproxy.backend.jdbc.execute;

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.DatabaseType;
import io.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import io.shardingsphere.core.executor.ShardingExecuteEngine;
import io.shardingsphere.core.executor.ShardingExecuteGroup;
import io.shardingsphere.core.executor.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.execute.result.MemoryQueryResult;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareCallback;
import io.shardingsphere.core.executor.sql.prepare.SQLExecutePrepareTemplate;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.shardingproxy.backend.BackendExecutorContext;
import io.shardingsphere.shardingproxy.backend.SQLExecuteEngine;
import io.shardingsphere.shardingproxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.shardingproxy.backend.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import io.shardingsphere.shardingproxy.backend.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.shardingproxy.runtime.GlobalRegistry;
import io.shardingsphere.shardingproxy.transport.mysql.constant.ColumnType;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.ColumnDefinition41Packet;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.FieldCountPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.command.query.QueryResponsePackets;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.EofPacket;
import io.shardingsphere.shardingproxy.transport.mysql.packet.generic.OKPacket;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
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
    
    private final List<QueryResult> queryResults = new LinkedList<>();
    
    private final BackendConnection backendConnection;
    
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private int columnCount;
    
    private List<ColumnType> columnTypes;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = GlobalRegistry.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ShardingExecuteEngine executeEngine = BackendExecutorContext.getInstance().getExecuteEngine();
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(executeEngine);
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
            return new ExecuteUpdateResponseUnit(new OKPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0));
        }
        ResultSet resultSet = statement.getResultSet();
        backendConnection.add(resultSet);
        ResultSetMetaData resultSetMetaData = resultSet.getMetaData();
        if (0 == resultSetMetaData.getColumnCount()) {
            return new ExecuteUpdateResponseUnit(new OKPacket(1));
        }
        return new ExecuteQueryResponseUnit(getHeaderPackets(resultSetMetaData), createQueryResult(resultSet, connectionMode));
    }
    
    private ExecuteResponseUnit executeWithoutMetadata(final Statement statement, final String sql, final ConnectionMode connectionMode, final boolean isReturnGeneratedKeys) throws SQLException {
        backendConnection.add(statement);
        if (!jdbcExecutorWrapper.executeSQL(statement, sql, isReturnGeneratedKeys)) {
            return new ExecuteUpdateResponseUnit(new OKPacket(1, statement.getUpdateCount(), isReturnGeneratedKeys ? getGeneratedKey(statement) : 0));
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
        int currentSequenceId = 0;
        int columnCount = resultSetMetaData.getColumnCount();
        FieldCountPacket fieldCountPacket = new FieldCountPacket(++currentSequenceId, columnCount);
        Collection<ColumnDefinition41Packet> columnDefinition41Packets = new LinkedList<>();
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            columnDefinition41Packets.add(new ColumnDefinition41Packet(++currentSequenceId, resultSetMetaData, columnIndex));
        }
        return new QueryResponsePackets(fieldCountPacket, columnDefinition41Packets, new EofPacket(++currentSequenceId));
    }
    
    private QueryResult createQueryResult(final ResultSet resultSet, final ConnectionMode connectionMode) throws SQLException {
        return connectionMode == ConnectionMode.MEMORY_STRICTLY ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
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
            super(DatabaseType.MySQL, isExceptionThrown);
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
            super(DatabaseType.MySQL, isExceptionThrown);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit statementExecuteUnit) throws SQLException {
            return executeWithoutMetadata(
                    statementExecuteUnit.getStatement(), statementExecuteUnit.getRouteUnit().getSqlUnit().getSql(), statementExecuteUnit.getConnectionMode(), isReturnGeneratedKeys);
        }
    }
}
