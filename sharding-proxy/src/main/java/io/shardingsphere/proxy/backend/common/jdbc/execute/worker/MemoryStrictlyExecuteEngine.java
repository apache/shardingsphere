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

package io.shardingsphere.proxy.backend.common.jdbc.execute.worker;

import com.google.common.collect.Lists;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLExecutionUnit;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.result.StreamQueryResult;
import io.shardingsphere.proxy.transport.mysql.packet.command.CommandResponsePackets;

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
    public final List<CommandResponsePackets> execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Iterator<SQLExecutionUnit> sqlExecutionUnits = routeResult.getExecutionUnits().iterator();
        SQLExecutionUnit firstSQLExecutionUnit = sqlExecutionUnits.next();
        List<Future<JDBCExecuteResponse>> futureList = asyncExecute(isReturnGeneratedKeys, Lists.newArrayList(sqlExecutionUnits));
        JDBCExecuteResponse firstJDBCExecuteResponse = syncExecute(isReturnGeneratedKeys, firstSQLExecutionUnit);
        return buildCommandResponsePackets(firstJDBCExecuteResponse, futureList);
    }
    
    private List<Future<JDBCExecuteResponse>> asyncExecute(final boolean isReturnGeneratedKeys, final Collection<SQLExecutionUnit> sqlExecutionUnits) {
        List<Future<JDBCExecuteResponse>> result = new LinkedList<>();
        for (SQLExecutionUnit each : sqlExecutionUnits) {
            final String dataSourceName = each.getDataSource();
            final String actualSQL = each.getSqlUnit().getSql();
            result.add(getUserGroup().submit(new Callable<JDBCExecuteResponse>() {
                
                @Override
                public JDBCExecuteResponse call() throws SQLException {
                    return execute(createStatement(getConnectionManager().getConnection(dataSourceName), actualSQL, isReturnGeneratedKeys), actualSQL, isReturnGeneratedKeys);
                }
            }));
        }
        return result;
    }
    
    private JDBCExecuteResponse syncExecute(final boolean isReturnGeneratedKeys, final SQLExecutionUnit sqlExecutionUnit) throws SQLException {
        return execute(createStatement(getConnectionManager().getConnection(sqlExecutionUnit.getDataSource()), sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys),
                sqlExecutionUnit.getSqlUnit().getSql(), isReturnGeneratedKeys);
    }
    
    private List<CommandResponsePackets> buildCommandResponsePackets(final JDBCExecuteResponse firstJDBCExecuteResponse, final List<Future<JDBCExecuteResponse>> futureList) {
        List<CommandResponsePackets> result = new ArrayList<>(futureList.size() + 1);
        result.add(firstJDBCExecuteResponse.getCommandResponsePackets());
        setColumnCount(firstJDBCExecuteResponse.getColumnCount());
        setColumnTypes(firstJDBCExecuteResponse.getColumnTypes());
        getQueryResults().add(firstJDBCExecuteResponse.getQueryResult());
        for (Future<JDBCExecuteResponse> each : futureList) {
            try {
                JDBCExecuteResponse executeResponse = each.get();
                result.add(executeResponse.getCommandResponsePackets());
                getQueryResults().add(executeResponse.getQueryResult());
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
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
