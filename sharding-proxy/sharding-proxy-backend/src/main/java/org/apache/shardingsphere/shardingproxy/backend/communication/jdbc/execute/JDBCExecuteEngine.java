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
import org.apache.shardingsphere.underlying.executor.StatementExecuteUnit;
import org.apache.shardingsphere.sharding.execute.sql.execute.SQLExecuteTemplate;
import org.apache.shardingsphere.sharding.execute.sql.execute.threadlocal.ExecutorExceptionHandler;
import org.apache.shardingsphere.underlying.executor.group.PreparedStatementExecuteGroupEngine;
import org.apache.shardingsphere.underlying.executor.group.ExecuteGroupEngine;
import org.apache.shardingsphere.underlying.executor.group.StatementExecuteGroupEngine;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxySQLExecuteCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.PreparedStatementExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.shardingproxy.context.ShardingProxyContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.underlying.common.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.underlying.executor.connection.StatementOption;
import org.apache.shardingsphere.underlying.executor.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * SQL Execute engine for JDBC.
 */
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    @Getter
    private final BackendConnection backendConnection;
    
    @Getter
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final ExecuteGroupEngine executeGroupEngine;
    
    private final SQLExecuteTemplate sqlExecuteTemplate;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        int maxConnectionsSizePerQuery = ShardingProxyContext.getInstance().getProperties().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        executeGroupEngine = jdbcExecutorWrapper instanceof PreparedStatementExecutorWrapper
                ? new PreparedStatementExecuteGroupEngine(maxConnectionsSizePerQuery) : new StatementExecuteGroupEngine(maxConnectionsSizePerQuery);
        sqlExecuteTemplate = new SQLExecuteTemplate(BackendExecutorContext.getInstance().getExecutorKernel(), backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute(final ExecutionContext executionContext) throws SQLException {
        SQLStatementContext sqlStatementContext = executionContext.getSqlStatementContext();
        boolean isReturnGeneratedKeys = sqlStatementContext.getSqlStatement() instanceof InsertStatement;
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(
                executionContext.getExecutionUnits(), backendConnection, new StatementOption(isReturnGeneratedKeys));
        Collection<ExecuteResponse> executeResponses = sqlExecuteTemplate.execute((Collection) inputGroups, 
                new ProxySQLExecuteCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, true),
                new ProxySQLExecuteCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, false));
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
