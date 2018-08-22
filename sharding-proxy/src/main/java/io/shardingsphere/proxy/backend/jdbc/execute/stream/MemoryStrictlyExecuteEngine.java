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

import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.BackendExecutorContext;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.execute.ProxyStatementExecuteUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.QueryResponsePackets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Map;

/**
 * Memory strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class MemoryStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public MemoryStrictlyExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        super(backendConnection, jdbcExecutorWrapper);
        sqlExecuteTemplate = new SQLExecuteTemplate(BackendExecutorContext.getInstance().getExecuteEngine(), ConnectionMode.MEMORY_STRICTLY);
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getSqlStatement() instanceof InsertStatement;
        SQLType sqlType = routeResult.getSqlStatement().getType();
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.execute(getStatementExecuteUnits(routeResult, isReturnGeneratedKeys), 
                new FirstMemoryStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys), 
                new MemoryStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys));
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse(((ExecuteQueryResponseUnit) firstExecuteResponseUnit).getQueryResponsePackets(), executeResponseUnits) : new ExecuteUpdateResponse(executeResponseUnits);
    }
    
    private Collection<StatementExecuteUnit> getStatementExecuteUnits(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Collection<StatementExecuteUnit> result = new LinkedList<>();
        for (SQLExecutionUnit each : routeResult.getExecutionUnits()) {
            Statement statement = getJdbcExecutorWrapper().createStatement(getBackendConnection().getConnection(each.getDataSource()), each.getSqlUnit().getSql(), isReturnGeneratedKeys);
            result.add(new ProxyStatementExecuteUnit(each, statement));
        }
        return result;
    }
    
    private ExecuteResponse getExecuteQueryResponse(final QueryResponsePackets queryResponsePackets, final Collection<ExecuteResponseUnit> executeResponseUnits) {
        ExecuteQueryResponse result = new ExecuteQueryResponse(queryResponsePackets);
        for (ExecuteResponseUnit each : executeResponseUnits) {
            result.getQueryResults().add(((ExecuteQueryResponseUnit) each).getQueryResult());
        }
        return result;
    }
    
    @Override
    protected QueryResult createQueryResult(final ResultSet resultSet) {
        return new StreamQueryResult(resultSet);
    }
    
    private final class FirstMemoryStrictlySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private FirstMemoryStrictlySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final boolean isReturnGeneratedKeys) {
            super(sqlType, isExceptionThrown, dataMap);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        protected ExecuteResponseUnit executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
            executeUnit.getStatement().setFetchSize(FETCH_ONE_ROW_A_TIME);
            return executeWithMetadata(executeUnit.getStatement(), executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
    
    private final class MemoryStrictlySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private MemoryStrictlySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final boolean isReturnGeneratedKeys) {
            super(sqlType, isExceptionThrown, dataMap);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        protected ExecuteResponseUnit executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
            executeUnit.getStatement().setFetchSize(FETCH_ONE_ROW_A_TIME);
            return executeWithoutMetadata(executeUnit.getStatement(), executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
}
