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

package io.shardingsphere.proxy.backend.jdbc.execute.memory;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.ShardingGroupExecuteCallback;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.proxy.config.RuleRegistry;
import lombok.RequiredArgsConstructor;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Connection strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class ConnectionStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    public ConnectionStrictlyExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        super(backendConnection, jdbcExecutorWrapper);
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Map<String, Collection<SQLUnit>> sqlUnitGroups = routeResult.getSQLUnitGroups();
        Collection<ExecuteResponseUnit> executeResponseUnits;
        try {
            if (TransactionType.XA == RuleRegistry.getInstance().getTransactionType()) {
                final Map<String, Map<SQLUnit, Statement>> sqlUnitStatements = new HashMap<>(sqlUnitGroups.size(), 1);
                for (Entry<String, Collection<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
                    sqlUnitStatements.put(entry.getKey(), createSQLUnitStatement(entry.getKey(), entry.getValue(), isReturnGeneratedKeys));
                }
                executeResponseUnits = getShardingExecuteEngine().groupExecute(
                        sqlUnitGroups, new FirstTransactionGroupExecuteCallback(isReturnGeneratedKeys), new XATransactionGroupExecuteCallback(isReturnGeneratedKeys, sqlUnitStatements));
            } else {
                executeResponseUnits = getShardingExecuteEngine().groupExecute(
                        sqlUnitGroups, new FirstTransactionGroupExecuteCallback(isReturnGeneratedKeys), new LocalTransactionGroupExecuteCallback(isReturnGeneratedKeys));
            }
        } catch (final Exception ex) {
            throw new SQLException(ex);
        }
        return getExecuteQueryResponse(executeResponseUnits);
    }
    
    private Map<SQLUnit, Statement> createSQLUnitStatement(final String dataSourceName, final Collection<SQLUnit> sqlUnits, final boolean isReturnGeneratedKeys) throws SQLException {
        Map<SQLUnit, Statement> result = new HashMap<>(sqlUnits.size(), 1);
        Connection connection = getBackendConnection().getConnection(dataSourceName);
        for (SQLUnit each : sqlUnits) {
            result.put(each, getJdbcExecutorWrapper().createStatement(connection, each.getSql(), isReturnGeneratedKeys));
        }
        return result;
    }
    
    private ExecuteResponse getExecuteQueryResponse(final Collection<ExecuteResponseUnit> executeResponseUnits) {
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse((ExecuteQueryResponseUnit) firstExecuteResponseUnit, executeResponseUnits) : getExecuteUpdateResponse(executeResponseUnits);
    }
    
    private ExecuteResponse getExecuteQueryResponse(final ExecuteQueryResponseUnit firstExecuteResponseUnit, final Collection<ExecuteResponseUnit> executeResponseUnits) {
        ExecuteQueryResponse result = new ExecuteQueryResponse(firstExecuteResponseUnit.getQueryResponsePackets());
        for (ExecuteResponseUnit each : executeResponseUnits) {
            result.getQueryResults().add(((ExecuteQueryResponseUnit) each).getQueryResult());
        }
        return result;
    }
    
    private ExecuteResponse getExecuteUpdateResponse(final Collection<ExecuteResponseUnit> executeResponseUnits) {
        return new ExecuteUpdateResponse(executeResponseUnits);
    }
    
    @Override
    protected void setFetchSize(final Statement statement) {
    }
    
    @Override
    protected QueryResult createQueryResult(final ResultSet resultSet) throws SQLException {
        return new MemoryQueryResult(resultSet);
    }
    
    @RequiredArgsConstructor
    class FirstTransactionGroupExecuteCallback implements ShardingGroupExecuteCallback<SQLUnit, ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        @Override
        public Collection<ExecuteResponseUnit> execute(final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws Exception {
            Collection<ExecuteResponseUnit> result = new LinkedList<>();
            boolean hasMetaData = false;
            Connection connection = getBackendConnection().getConnection(dataSourceName);
            for (SQLUnit each : sqlUnits) {
                String actualSQL = each.getSql();
                Statement statement = getJdbcExecutorWrapper().createStatement(connection, actualSQL, isReturnGeneratedKeys);
                ExecuteResponseUnit response;
                if (hasMetaData) {
                    response = executeWithoutMetadata(statement, actualSQL, isReturnGeneratedKeys);
                } else {
                    response = executeWithMetadata(statement, actualSQL, isReturnGeneratedKeys);
                    hasMetaData = true;
                }
                result.add(response);
            }
            return result;
        }
    }
    
    @RequiredArgsConstructor
    class XATransactionGroupExecuteCallback implements ShardingGroupExecuteCallback<SQLUnit, ExecuteResponseUnit> {
    
        private final boolean isReturnGeneratedKeys;
    
        private final Map<String, Map<SQLUnit, Statement>> sqlUnitStatements;
        
        @Override
        public Collection<ExecuteResponseUnit> execute(final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws Exception {
            Collection<ExecuteResponseUnit> result = new LinkedList<>();
            for (Entry<SQLUnit, Statement> each : sqlUnitStatements.get(dataSourceName).entrySet()) {
                result.add(executeWithoutMetadata(each.getValue(), each.getKey().getSql(), isReturnGeneratedKeys));
            }
            return result;
        }
    }
    
    @RequiredArgsConstructor
    class LocalTransactionGroupExecuteCallback implements ShardingGroupExecuteCallback<SQLUnit, ExecuteResponseUnit> {
    
        private final boolean isReturnGeneratedKeys;
        
        @Override
        public Collection<ExecuteResponseUnit> execute(final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws Exception {
            Collection<ExecuteResponseUnit> result = new LinkedList<>();
            for (Entry<SQLUnit, Statement> each : createSQLUnitStatement(dataSourceName, sqlUnits, isReturnGeneratedKeys).entrySet()) {
                result.add(executeWithoutMetadata(each.getValue(), each.getKey().getSql(), isReturnGeneratedKeys));
            }
            return result;
        }
    }
}
