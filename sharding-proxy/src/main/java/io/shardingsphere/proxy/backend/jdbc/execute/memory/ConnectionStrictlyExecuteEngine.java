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

import com.google.common.collect.Lists;
import io.shardingsphere.core.constant.ConnectionMode;
import io.shardingsphere.core.constant.SQLType;
import io.shardingsphere.core.executor.sql.SQLExecuteCallback;
import io.shardingsphere.core.executor.sql.SQLExecuteTemplate;
import io.shardingsphere.core.executor.sql.StatementExecuteUnit;
import io.shardingsphere.core.executor.sql.result.MemoryQueryResult;
import io.shardingsphere.core.executor.sql.threadlocal.ExecutorDataMap;
import io.shardingsphere.core.executor.sql.threadlocal.ExecutorExceptionHandler;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
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
import io.shardingsphere.proxy.config.RuleRegistry;
import io.shardingsphere.proxy.transport.mysql.packet.command.query.QueryResponsePackets;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Connection strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class ConnectionStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public ConnectionStrictlyExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        super(backendConnection, jdbcExecutorWrapper);
        sqlExecuteTemplate = new SQLExecuteTemplate(BackendExecutorContext.getInstance().getExecuteEngine(), ConnectionMode.CONNECTION_STRICTLY, 1);
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getSqlStatement() instanceof InsertStatement;
        SQLType sqlType = routeResult.getSqlStatement().getType();
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Map<String, Object> dataMap = ExecutorDataMap.getDataMap();
        Collection<ExecuteResponseUnit> executeResponseUnits = sqlExecuteTemplate.execute(getStatementExecuteUnits(routeResult, isReturnGeneratedKeys), 
                new FirstConnectionStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys), 
                new ConnectionStrictlySQLExecuteCallback(sqlType, isExceptionThrown, dataMap, isReturnGeneratedKeys));
        ExecuteResponseUnit firstExecuteResponseUnit = executeResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse(((ExecuteQueryResponseUnit) firstExecuteResponseUnit).getQueryResponsePackets(), executeResponseUnits) : new ExecuteUpdateResponse(executeResponseUnits);
    }
    
    private Collection<StatementExecuteUnit> getStatementExecuteUnits(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Collection<StatementExecuteUnit> result = new LinkedList<>();
        for (Entry<String, Collection<SQLUnit>> entry : routeResult.getSQLUnitGroups().entrySet()) {
            result.addAll(getStatementExecuteUnits(entry.getKey(), entry.getValue(), isReturnGeneratedKeys));
        }
        return result;
    }
    
    private Collection<StatementExecuteUnit> getStatementExecuteUnits(final String dataSourceName, final Collection<SQLUnit> sqlUnits, final boolean isReturnGeneratedKeys) throws SQLException {
        Collection<StatementExecuteUnit> result = new LinkedList<>();
        for (List<SQLUnit> sqlUnitList : Lists.partition(new ArrayList<>(sqlUnits), RuleRegistry.getInstance().getMaxConnectionsSizePerQuery())) {
            Connection connection = getBackendConnection().getConnection(dataSourceName);
            for (SQLUnit each : sqlUnitList) {
                result.add(new ProxyStatementExecuteUnit(new SQLExecutionUnit(dataSourceName, each), getJdbcExecutorWrapper().createStatement(connection, each.getSql(), isReturnGeneratedKeys)));
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
    protected QueryResult createQueryResult(final ResultSet resultSet) throws SQLException {
        return new MemoryQueryResult(resultSet);
    }
    
    private final class FirstConnectionStrictlySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
    
        private boolean hasMetaData;
    
        private FirstConnectionStrictlySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final boolean isReturnGeneratedKeys) {
            super(sqlType, isExceptionThrown, dataMap);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
            if (hasMetaData) {
                return executeWithoutMetadata(executeUnit.getStatement(), executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
            } else {
                hasMetaData = true;
                return executeWithMetadata(executeUnit.getStatement(), executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
            }
        }
    }
    
    private final class ConnectionStrictlySQLExecuteCallback extends SQLExecuteCallback<ExecuteResponseUnit> {
        
        private final boolean isReturnGeneratedKeys;
    
        private ConnectionStrictlySQLExecuteCallback(final SQLType sqlType, final boolean isExceptionThrown, final Map<String, Object> dataMap, final boolean isReturnGeneratedKeys) {
            super(sqlType, isExceptionThrown, dataMap);
            this.isReturnGeneratedKeys = isReturnGeneratedKeys;
        }
        
        @Override
        public ExecuteResponseUnit executeSQL(final StatementExecuteUnit executeUnit) throws SQLException {
            return executeWithoutMetadata(executeUnit.getStatement(), executeUnit.getSqlExecutionUnit().getSqlUnit().getSql(), isReturnGeneratedKeys);
        }
    }
}
