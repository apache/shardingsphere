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

package io.shardingsphere.proxy.backend.common.jdbc.execute.stream;

import com.google.common.collect.Lists;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteResponseUnit;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.SQLExecuteResponse;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
public abstract class MemoryStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    private static final Integer FETCH_ONE_ROW_A_TIME = Integer.MIN_VALUE;
    
    @Override
    public final SQLExecuteResponse execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Iterator<SQLExecutionUnit> sqlExecutionUnits = routeResult.getExecutionUnits().iterator();
        SQLExecutionUnit firstSQLExecutionUnit = sqlExecutionUnits.next();
        List<Future<ExecuteResponseUnit>> futureList = asyncExecute(isReturnGeneratedKeys, Lists.newArrayList(sqlExecutionUnits));
        ExecuteResponseUnit firstJDBCExecuteResponse = syncExecute(isReturnGeneratedKeys, firstSQLExecutionUnit);
        return buildCommandResponsePackets(firstJDBCExecuteResponse, futureList);
    }
    
    private List<Future<ExecuteResponseUnit>> asyncExecute(final boolean isReturnGeneratedKeys, final Collection<SQLExecutionUnit> sqlExecutionUnits) {
        List<Future<ExecuteResponseUnit>> result = new LinkedList<>();
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            final String dataSourceName = each.getDataSource();
            final String actualSQL = each.getSqlUnit().getSql();
            result.add(getExecutorService().submit(new Callable<ExecuteResponseUnit>() {
                
                @Override
                public ExecuteResponseUnit call() throws SQLException {
                    Statement statement = createStatement(getBackendConnection().getConnection(dataSourceName), actualSQL, isReturnGeneratedKeys);
                    return executeWithoutMetadata(statement, actualSQL, isReturnGeneratedKeys);
                }
            }));
        }
        return result;
    }
    
    private ExecuteResponseUnit syncExecute(final boolean isReturnGeneratedKeys, final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
        Statement statement = createStatement(getBackendConnection().getConnection(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
        return executeWithMetadata(statement, sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
    }
    
    private SQLExecuteResponse buildCommandResponsePackets(final ExecuteResponseUnit firstJDBCExecuteResponse, final List<Future<ExecuteResponseUnit>> futureList) {
        List<CommandResponsePackets> commandResponsePackets = new ArrayList<>(futureList.size() + 1);
        List<QueryResult> queryResults = new ArrayList<>(futureList.size() + 1);
        commandResponsePackets.add(firstJDBCExecuteResponse.getCommandResponsePackets());
        if (firstJDBCExecuteResponse instanceof ExecuteQueryResponseUnit) {
            queryResults.add(((ExecuteQueryResponseUnit) firstJDBCExecuteResponse).getQueryResult());
        }
        for (Future<ExecuteResponseUnit> each : futureList) {
            try {
                ExecuteResponseUnit executeResponse = each.get();
                if (executeResponse instanceof ExecuteQueryResponseUnit) {
                    queryResults.add(((ExecuteQueryResponseUnit) executeResponse).getQueryResult());
                } else {
                    commandResponsePackets.add(executeResponse.getCommandResponsePackets());
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return new SQLExecuteResponse(commandResponsePackets, queryResults);
    }
    
    @Override
    protected final void setFetchSize(final Statement statement) throws SQLException {
        statement.setFetchSize(FETCH_ONE_ROW_A_TIME);
    }
    
    @Override
    protected final QueryResult createQueryResult(final ResultSet resultSet) {
        return new StreamQueryResult(resultSet);
    }
}
