/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute;

import lombok.Getter;
import org.apache.shardingsphere.core.constant.properties.ShardingPropertiesConstant;
import org.apache.shardingsphere.core.execute.ShardingExecuteGroup;
import org.apache.shardingsphere.core.execute.StatementExecuteUnit;
import org.apache.shardingsphere.core.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.core.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.core.execute.sql.prepare.SQLExecutePrepareTemplate;
import org.apache.shardingsphere.core.parse.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.core.route.SQLRouteResult;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxyJDBCExecutePrepareCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxySQLExecuteCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * SQL Execute engine for JDBC.
 *
 * @author zhaojun
 * @author zhangliang
 * @author panjuan
 */
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    @Getter
    private final BackendConnection backendConnection;
    
    @Getter
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final SQLExecutePrepareTemplate sqlExecutePrepareTemplate;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = ShardingProxyContext.getInstance().getShardingProperties().<Integer>getValue(ShardingPropertiesConstant.MAX_CONNECTIONS_SIZE_PER_QUERY);
        sqlExecutePrepareTemplate = new SQLExecutePrepareTemplate(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(BackendExecutorContext.getInstance().getExecuteEngine(), backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute(final SQLRouteResult routeResult) throws SQLException {
        boolean isReturnGeneratedKeys = routeResult.getShardingStatement().getSQLStatement() instanceof InsertStatement;
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Collection<ShardingExecuteGroup<StatementExecuteUnit>> sqlExecuteGroups = sqlExecutePrepareTemplate.getExecuteUnitGroups(
                routeResult.getRouteUnits(), new ProxyJDBCExecutePrepareCallback(backendConnection, jdbcExecutorWrapper, isReturnGeneratedKeys));
        Collection<ExecuteResponse> executeResponses = sqlExecuteTemplate.executeGroup((Collection) sqlExecuteGroups, 
                new ProxySQLExecuteCallback(backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, true), 
                new ProxySQLExecuteCallback(backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, false));
        ExecuteResponse executeResponse = executeResponses.iterator().next();
        return executeResponse instanceof ExecuteQueryResponse
                ? getExecuteQueryResponse(((ExecuteQueryResponse) executeResponse).getQueryHeaders(), executeResponses) : new UpdateResponse(executeResponses);
    }
    
    private BackendResponse getExecuteQueryResponse(final List<QueryHeader> queryHeaders, final Collection<ExecuteResponse> executeResponses) {
        QueryResponse result = new QueryResponse(queryHeaders);
        for (ExecuteResponse each : executeResponses) {
            result.getQueryResults().add(((ExecuteQueryResponse) each).getQueryResult());
        }
        return result;
    }
}
