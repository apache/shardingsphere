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
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.JDBCExecuteQueryResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.JDBCExecuteResponse;
import io.shardingsphere.proxy.backend.common.jdbc.execute.response.SQLExecuteResponses;
import io.shardingsphere.proxy.transport.mysql.packet.command.reponse.CommandResponsePackets;

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
    public final SQLExecuteResponses execute(final SQLRouteResult routeResult, final boolean isReturnGeneratedKeys) throws SQLException {
        Map<String, Collection<SQLUnit>> sqlExecutionUnits = routeResult.getSQLUnitGroups();
        Entry<String, Collection<SQLUnit>> firstEntry = sqlExecutionUnits.entrySet().iterator().next();
        sqlExecutionUnits.remove(firstEntry.getKey());
        List<Future<Collection<JDBCExecuteResponse>>> futureList = asyncExecute(isReturnGeneratedKeys, sqlExecutionUnits);
        Collection<JDBCExecuteResponse> firstJDBCExecuteResponses = syncExecute(isReturnGeneratedKeys, firstEntry.getKey(), firstEntry.getValue());
        return buildCommandResponsePackets(firstJDBCExecuteResponses, futureList);
    }
    
    private List<Future<Collection<JDBCExecuteResponse>>> asyncExecute(final boolean isReturnGeneratedKeys, final Map<String, Collection<SQLUnit>> sqlUnitGroups) throws SQLException {
        List<Future<Collection<JDBCExecuteResponse>>> result = new LinkedList<>();
        for (Entry<String, Collection<SQLUnit>> entry : sqlUnitGroups.entrySet()) {
            final Connection connection = getBackendConnection().getConnection(entry.getKey());
            final Collection<SQLUnit> sqlUnits = entry.getValue();
            result.add(getExecutorService().submit(new Callable<Collection<JDBCExecuteResponse>>() {
                
                @Override
                public Collection<JDBCExecuteResponse> call() throws SQLException {
                    Collection<JDBCExecuteResponse> result = new LinkedList<>();
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
    
    private Collection<JDBCExecuteResponse> syncExecute(final boolean isReturnGeneratedKeys, final String dataSourceName, final Collection<SQLUnit> sqlUnits) throws SQLException {
        Collection<JDBCExecuteResponse> result = new LinkedList<>();
        boolean hasMetaData = false;
        Connection connection = getBackendConnection().getConnection(dataSourceName);
        for (SQLUnit each : sqlUnits) {
            String actualSQL = each.getSql();
            Statement statement = createStatement(connection, actualSQL, isReturnGeneratedKeys);
            JDBCExecuteResponse response;
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
    
    private SQLExecuteResponses buildCommandResponsePackets(final Collection<JDBCExecuteResponse> firstJDBCExecuteResponses, final List<Future<Collection<JDBCExecuteResponse>>> futureList) {
        List<CommandResponsePackets> commandResponsePackets = new LinkedList<>();
        List<QueryResult> queryResults = new LinkedList<>();
        for (JDBCExecuteResponse each : firstJDBCExecuteResponses) {
            if (null != each.getCommandResponsePackets()) {
                commandResponsePackets.add(each.getCommandResponsePackets());
            }
            if (each instanceof JDBCExecuteQueryResponse) {
                queryResults.add(((JDBCExecuteQueryResponse) each).getQueryResult());
            }
        }
        for (Future<Collection<JDBCExecuteResponse>> each : futureList) {
            try {
                Collection<JDBCExecuteResponse> executeResponses = each.get();
                for (JDBCExecuteResponse executeResponse : executeResponses) {
                    if (executeResponse instanceof JDBCExecuteQueryResponse) {
                        queryResults.add(((JDBCExecuteQueryResponse) executeResponse).getQueryResult());
                    } else {
                        commandResponsePackets.add(executeResponse.getCommandResponsePackets());
                    }
                    commandResponsePackets.add(executeResponse.getCommandResponsePackets());
                }
            } catch (final InterruptedException | ExecutionException ex) {
                throw new ShardingException(ex.getMessage(), ex);
            }
        }
        return new SQLExecuteResponses(commandResponsePackets, queryResults);
    }
    
    @Override
    protected final void setFetchSize(final Statement statement) {
    }
    
    @Override
    protected final QueryResult createQueryResult(final ResultSet resultSet) throws SQLException {
        return new MemoryQueryResult(resultSet);
    }
}
