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

package io.shardingsphere.proxy.backend.common.jdbc.execute.memory;

import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.merger.QueryResult;
import io.shardingsphere.core.routing.SQLRouteResult;
import io.shardingsphere.core.routing.SQLUnit;
import io.shardingsphere.proxy.backend.common.jdbc.execute.JDBCExecuteEngine;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteQueryResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.ExecuteUpdateResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.unit.ExecuteQueryResponseUnit;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.unit.ExecuteResponseUnit;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

/**
 * Connection strictly execute engine.
 *
 * @author zhaojun
 * @author zhangliang
 */
public abstract class ConnectionStrictlyExecuteEngine extends JDBCExecuteEngine {
    
    @Override
    public final ExecuteResponse execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Map<String, Collection<SQLUnit>> sqlExecutionUnits = routeResult.getSQLUnitGroups();
        Entry<String, Collection<SQLUnit>> firstEntry = sqlExecutionUnits.entrySet().iterator().next();
        sqlExecutionUnits.remove(firstEntry.getKey());
        List<Future<Collection<ExecuteResponseUnit>>> futureList = asyncExecute(isReturnGeneratedKeys, sqlExecutionUnits);
        Collection<ExecuteResponseUnit> firstExecuteResponseUnits = syncExecute(isReturnGeneratedKeys, firstEntry.getKey(), firstEntry.getValue());
        return buildCommandResponsePackets(firstExecuteResponseUnits, futureList);
    }
    
    private List<Future<Collection<ExecuteResponseUnit>>> asyncExecute(final boolean isReturnGeneratedKeys, final Map<String, Collection<SQLUnit>> sqlUnitGroups) throws SQLException {
        List<Future<Collection<ExecuteResponseUnit>>> result = new LinkedList<>();
        for (Entry<String, Collection<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            final Connection connection = getBackendConnection().getConnection(entry.getKey());
            final Collection<SQLUnit> sqlUnits = entry.getValue();
            result.add(getExecutorService().submit(new Callable<Collection<ExecuteResponseUnit>>() {
                
                @Override
                public Collection<ExecuteResponseUnit> call() throws SQLException {
                    Collection<ExecuteResponseUnit> result = new LinkedList<>();
                    for (SQLUnit each : sqlUnits) {
                        Statement statement = createStatement(connection, each.getSql(), isReturnGeneratedKeys);
                        result.add(executeWithoutMetadata(statement, each.getSql(), isReturnGeneratedKeys));
                    }
                    return result;
                }
            }));
        }
        return result;
    }
    
    private Collection<ExecuteResponseUnit> syncExecute(final boolean isReturnGeneratedKeys, final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws SQLException {
        Collection<ExecuteResponseUnit> result = new LinkedList<>();
        boolean hasMetaData = false;
        Connection connection = getBackendConnection().getConnection(dataSourceName);
        for (SQLUnit each : sqlUnits) {
            String actualSQL = each.getSql();
            Statement statement = createStatement(connection, actualSQL, isReturnGeneratedKeys);
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
    
    private ExecuteResponse buildCommandResponsePackets(final Collection<ExecuteResponseUnit> firstExecuteResponseUnits, final List<Future<Collection<ExecuteResponseUnit>>> futureList) {
        ExecuteResponseUnit firstExecuteResponseUnit = firstExecuteResponseUnits.iterator().next();
        return firstExecuteResponseUnit instanceof ExecuteQueryResponseUnit
                ? getExecuteQueryResponse((ExecuteQueryResponseUnit) firstExecuteResponseUnit, firstExecuteResponseUnits, futureList) : getExecuteUpdateResponse(firstExecuteResponseUnits, futureList);
    }
    
    private ExecuteResponse getExecuteQueryResponse(
            final ExecuteQueryResponseUnit firstExecuteResponseUnit, final Collection<ExecuteResponseUnit> firstExecuteResponseUnits, final List<Future<Collection<ExecuteResponseUnit>>> futureList) {
        ExecuteQueryResponse result = new ExecuteQueryResponse(firstExecuteResponseUnit.getCommandResponsePackets());
        for (ExecuteResponseUnit each : firstExecuteResponseUnits) {
            result.getQueryResults().add(((ExecuteQueryResponseUnit) each).getQueryResult());
        }
        for (Future<Collection<ExecuteResponseUnit>> each : futureList) {
            try {
                Collection<ExecuteResponseUnit> executeResponses = each.get();
                for (ExecuteResponseUnit executeResponse : executeResponses) {
                    if (executeResponse instanceof ExecuteQueryResponseUnit) {
                        result.getQueryResults().add(((ExecuteQueryResponseUnit) executeResponse).getQueryResult());
                    }
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    private ExecuteResponse getExecuteUpdateResponse(final Collection<ExecuteResponseUnit> firstExecuteResponseUnits, final List<Future<Collection<ExecuteResponseUnit>>> futureList) {
        ExecuteUpdateResponse result = new ExecuteUpdateResponse(firstExecuteResponseUnits);
        for (Future<Collection<ExecuteResponseUnit>> each : futureList) {
            try {
                for (ExecuteResponseUnit executeResponse : each.get()) {
                    result.getPackets().add(executeResponse.getCommandResponsePackets().getHeadPacket());
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return result;
    }
    
    @Override
    protected final void setFetchSize(final Statement statement) {
    }
    
    @Override
    protected final QueryResult createQueryResult(final ResultSet resultSet) throws SQLException {
        return new MemoryQueryResult(resultSet);
    }
}
