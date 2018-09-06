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

import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.execute.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.SQLExecuteUnit;
import io.shardingsphere.core.executor.sql.execute.result.StreamQueryResult;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.execute.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.RouteUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.BackendExecutorContext;
import io.shardingsphere.proxy.backend.jdbc.connection.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.execute.StatementExecuteUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.wrapper.JDBCExecutorWrapper;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.QueryResponsePackets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
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
        sqlExecuteTemplate = new SQLExecuteTemplate(BackendExecutorContext.getInstance().getExecuteEngine());
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getSqlStatement() instanceof InsertStatement;
        SQLType sqlType = routeResult.getSqlStatement().getType();
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.execute(getSQLExecuteUnits(routeResult, isReturnGeneratedKeys), 
                new FirstMemoryStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys), 
                new MemoryStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys));
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse(((ExecuteQueryResponseUnit) firstExecuteResponseUnit).getQueryResponsePackets(), executeResponseUnits) : new ExecuteUpdateResponse(executeResponseUnits);
    }
    
    private Collection<SQLExecuteUnit> getSQLExecuteUnits(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Collection<SQLExecuteUnit> result = new LinkedList<>();
        List<Connection> connections = getConnections(routeResult);
        int count = 0;
        for (RouteUnit each : routeResult.getRouteUnits()) {
            Statement statement = getJdbcExecutorWrapper().createStatement(connections.get(count), each.getSqlUnit().getSql(), isReturnGeneratedKeys);
            result.add(new StatementExecuteUnit(each, statement));
            count++;
        }
        return result;
    }
    
    private List<Connection> getConnections(final SQLRouteResult routeResult) throws SQLException {
        List<Connection> result = new ArrayList<>(routeResult.getRouteUnits().size());
        synchronized (MemoryStrictlyExecuteEngine.class) {
            for (RouteUnit each : routeResult.getRouteUnits()) {
                result.add(getBackendConnection().getConnection(each.getDataSourceName()));
            }
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
        protected ExecuteResponseUnit executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
            sqlExecuteUnit.getStatement().setFetchSize(FETCH_ONE_ROW_A_TIME);
            return executeWithMetadata(sqlExecuteUnit.getStatement(), sqlExecuteUnit.getRouteUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
    
    private final class MemoryStrictlySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
        
        private MemoryStrictlySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final boolean isReturnGeneratedKeys) {
            super(sqlType, isExceptionThrown, dataMap);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        protected ExecuteResponseUnit executeSQL(final SQLExecuteUnit sqlExecuteUnit) throws SQLException {
            sqlExecuteUnit.getStatement().setFetchSize(FETCH_ONE_ROW_A_TIME);
            return executeWithoutMetadata(sqlExecuteUnit.getStatement(), sqlExecuteUnit.getRouteUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
}
