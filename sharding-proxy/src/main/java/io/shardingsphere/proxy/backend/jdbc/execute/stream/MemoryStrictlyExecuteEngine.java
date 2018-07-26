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

import com.google.common.collect.Lists;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.jdbc.BackendConnection;
import io.shardingsphere.proxy.backend.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.execute.response.unit.ExecuteUpdateResponseUnit;
import io.shardingsphere.proxy.backend.jdbc.wrapper.JDBCExecutorWrapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Memory strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public final class MemoryStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    private final BackendConnection backendConnection;
    
    public MemoryStrictlyExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        super(jdbcExecutorWrapper);
        this.backendConnection = backendConnection;
    }
    
    @Override
    public ExecuteResponse execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Iterator<SQLExecutionUnit> executionUnits = routeResult.getExecutionUnits().iterator();
        SQLExecutionUnit firstSQLExecutionUnit = executionUnits.next();
        List<Future<ExecuteResponseUnit>> futureList = asyncExecute(isReturnGeneratedKeys, Lists.newArrayList(executionUnits));
        ExecuteResponseUnit firstResponseUnit = syncExecute(isReturnGeneratedKeys, firstSQLExecutionUnit);
        return firstResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse((ExecuteQueryResponseUnit) firstResponseUnit, futureList) : getExecuteUpdateResponse((ExecuteUpdateResponseUnit) firstResponseUnit, futureList);
    }
    
    private List<Future<ExecuteResponseUnit>> asyncExecute(final boolean isReturnGeneratedKeys, final Collection<SQLExecutionUnit> sqlExecutionUnits) {
        List<Future<ExecuteResponseUnit>> result = new LinkedList<>();
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            final String dataSourceName = each.getDataSource();
            final String actualSQL = each.getSqlUnit().getSql();
            result.add(getExecutorService().submit(new Callable<ExecuteResponseUnit>() {
                
                @Override
                public ExecuteResponseUnit call() throws SQLException {
                    Statement statement = getJdbcExecutorWrapper().createStatement(backendConnection.getConnection(dataSourceName), actualSQL, isReturnGeneratedKeys);
                    return executeWithoutMetadata(statement, actualSQL, isReturnGeneratedKeys);
                }
            }));
        }
        return result;
    }
    
    private ExecuteResponseUnit syncExecute(final boolean isReturnGeneratedKeys, final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
        Statement statement = getJdbcExecutorWrapper().createStatement(
                backendConnection.getConnection(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
        return executeWithMetadata(statement, sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
    }
    
    private ExecuteResponse getExecuteQueryResponse(final ExecuteQueryResponseUnit firstResponseUnit, final List<Future<ExecuteResponseUnit>> futureList) {
        ExecuteQueryResponse result = new ExecuteQueryResponse(firstResponseUnit.getQueryResponsePackets());
        result.getQueryResults().add(firstResponseUnit.getQueryResult());
        for (Future<ExecuteResponseUnit> each : futureList) {
            try {
                result.getQueryResults().add(((ExecuteQueryResponseUnit) each.get()).getQueryResult());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    private ExecuteResponse getExecuteUpdateResponse(final ExecuteUpdateResponseUnit firstResponseUnit, final List<Future<ExecuteResponseUnit>> futureList) {
        ExecuteUpdateResponse result = new ExecuteUpdateResponse(firstResponseUnit.getOkPacket());
        for (Future<ExecuteResponseUnit> each : futureList) {
            try {
                result.getPackets().add(((ExecuteUpdateResponseUnit) each.get()).getOkPacket());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    @Override
    protected void setFetchSize(final Statement statement) throws SQLException {
        statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
    }
    
    @Override
    protected QueryResult createQueryResult(final ResultSet resultSet) {
        return new StreamQueryResult(resultSet);
    }
}
