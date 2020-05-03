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
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.ProxySQLExecutorCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.callback.RuleProxySQLExecutorCallback;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteQueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.execute.response.ExecuteResponse;
import org.apache.shardingsphere.shardingproxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.shardingproxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.shardingproxy.backend.response.BackendResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.shardingproxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.shardingproxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;
import org.apache.shardingsphere.underlying.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.group.StatementOption;
import org.apache.shardingsphere.underlying.executor.sql.group.ExecuteGroupEngine;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * SQL Execute engine for JDBC.
 */
public final class JDBCExecuteEngine implements SQLExecuteEngine {
    
    static {
        ShardingSphereServiceLoader.register(RuleProxySQLExecutorCallback.class);
    }
    
    @Getter
    private final BackendConnection backendConnection;
    
    @Getter
    private final JDBCExecutorWrapper jdbcExecutorWrapper;
    
    private final ExecuteGroupEngine executeGroupEngine;
    
    private final SQLExecutor sqlExecutor;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        executeGroupEngine = jdbcExecutorWrapper.getExecuteGroupEngine();
        sqlExecutor = new SQLExecutor(BackendExecutorContext.getInstance().getExecutorKernel(), backendConnection.isSerialExecute());
    }
    
    @SuppressWarnings("unchecked")
    @Override
    public BackendResponse execute(final ExecutionContext executionContext) throws SQLException {
        SQLStatementContext sqlStatementContext = executionContext.getSqlStatementContext();
        boolean isReturnGeneratedKeys = sqlStatementContext.getSqlStatement() instanceof InsertStatement;
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(executionContext.getExecutionUnits(), backendConnection, new StatementOption(isReturnGeneratedKeys));
        Collection<ExecuteResponse> executeResponses = sqlExecutor.execute(inputGroups,
                getSQLExecutorCallback(new ProxySQLExecutorCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, true)),
                getSQLExecutorCallback(new ProxySQLExecutorCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, false)));
        ExecuteResponse executeResponse = executeResponses.iterator().next();
        if (executeResponse instanceof ExecuteQueryResponse) {
            return getExecuteQueryResponse(((ExecuteQueryResponse) executeResponse).getQueryHeaders(), executeResponses);
        } else {
            UpdateResponse updateResponse = new UpdateResponse(executeResponses);
            if (sqlStatementContext.getSqlStatement() instanceof InsertStatement) {
                updateResponse.setType("INSERT");
            } else if (sqlStatementContext.getSqlStatement() instanceof DeleteStatement) {
                updateResponse.setType("DELETE");
            } else if (sqlStatementContext.getSqlStatement() instanceof UpdateStatement) {
                updateResponse.setType("UPDATE");
            }
            return updateResponse;
        }
    }
    
    private SQLExecutorCallback<ExecuteResponse> getSQLExecutorCallback(final ProxySQLExecutorCallback callback) {
        Map<ShardingSphereRule, RuleProxySQLExecutorCallback> callbackMap = OrderedSPIRegistry.getRegisteredServices(backendConnection.getLogicSchema().getRules(), RuleProxySQLExecutorCallback.class);
        return callbackMap.isEmpty() ? callback : callbackMap.values().iterator().next();
    }
    
    private BackendResponse getExecuteQueryResponse(final List<QueryHeader> queryHeaders, final Collection<ExecuteResponse> executeResponses) {
        QueryResponse result = new QueryResponse(queryHeaders);
        for (ExecuteResponse each : executeResponses) {
            result.getQueryResults().add(((ExecuteQueryResponse) each).getQueryResult());
        }
        return result;
    }
}
