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

package io.shardingsphere.proxy.backend.jdbc.execute.stream;

import io.shardingsphere.core.constant.transaction.TransactionType;
import io.shardingsphere.core.executor.ShardingExecuteCallback;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.BackendExecutorContext;
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

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Memory strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class MemoryStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    public MemoryStrictlyExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        super(backendConnection, jdbcExecutorWrapper);
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Collection<ExecuteResponseUnit> executeResponseUnits;
        if (TransactionType.XA == RuleRegistry.getInstance().getTransactionType()) {
            Map<SQLExecutionUnit, Statement> statements = new HashMap<>(routeResult.getExecutionUnits().size(), 1);
            for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
                statements.put(each, getJdbcExecutorWrapper().createStatement(getBackendConnection().getConnection(each.getDataSource()), each.getSqlUnit().getSql(), isReturnGeneratedKeys));
            }
            executeResponseUnits = BackendExecutorContext.getInstance().getExecuteEngine().execute(
                    routeResult.getExecutionUnits(), new FirstTransactionExecuteCallback(isReturnGeneratedKeys), new XATransactionExecuteCallback(isReturnGeneratedKeys, statements));
        } else {
            executeResponseUnits = BackendExecutorContext.getInstance().getExecuteEngine().execute(
                    routeResult.getExecutionUnits(), new FirstTransactionExecuteCallback(isReturnGeneratedKeys), new LocalTransactionExecuteCallback(isReturnGeneratedKeys));
        }
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
    protected void setFetchSize(final Statement statement) throws SQLException {
        statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
    }
    
    @Override
    protected QueryResult createQueryResult(final ResultSet resultSet) {
        return new StreamQueryResult(resultSet);
    }
    
    @RequiredArgsConstructor
    class FirstTransactionExecuteCallback implements ShardingExecuteCallback<SQLExecutionUnit, ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        @Override
        public ExecuteResponseUnit execute(final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
            Statement statement = getJdbcExecutorWrapper().createStatement(
                    getBackendConnection().getConnection(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
            return executeWithMetadata(statement, sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
    
    @RequiredArgsConstructor
    class XATransactionExecuteCallback implements ShardingExecuteCallback<SQLExecutionUnit, ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private final Map<SQLExecutionUnit, Statement> statements;
        
        @Override
        public ExecuteResponseUnit execute(final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
            String actualSQL = sqlExecutionUnit.getSqlUnit().getSql();
            return executeWithoutMetadata(statements.get(sqlExecutionUnit), actualSQL, isReturnGeneratedKeys);
        }
    }
    
    @RequiredArgsConstructor
    class LocalTransactionExecuteCallback implements ShardingExecuteCallback<SQLExecutionUnit, ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        @Override
        public ExecuteResponseUnit execute(final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
            String actualSQL = sqlExecutionUnit.getSqlUnit().getSql();
            Statement statement = getJdbcExecutorWrapper().createStatement(getBackendConnection().getConnection(sqlExecutionUnit.getDataSource()), actualSQL, isReturnGeneratedKeys);
            return executeWithoutMetadata(statement, actualSQL, isReturnGeneratedKeys);
        }
    }
}
