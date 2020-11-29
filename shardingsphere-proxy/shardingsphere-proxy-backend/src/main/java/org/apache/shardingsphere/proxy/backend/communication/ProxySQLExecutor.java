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

package org.apache.shardingsphere.proxy.backend.communication;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.RawExecutionRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.accessor.JDBCAccessor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.communication.raw.ProxyRawExecutor;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryHeader;
import org.apache.shardingsphere.proxy.backend.response.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.UpdateStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Proxy SQL Executor.
 */
public final class ProxySQLExecutor {
    
    private final BackendConnection backendConnection;
    
    private final JDBCAccessor accessor;
    
    private final ProxyJDBCExecutor jdbcExecutor;
    
    private final ProxyRawExecutor rawExecutor;
    
    public ProxySQLExecutor(final BackendConnection backendConnection, final JDBCAccessor accessor) {
        this.backendConnection = backendConnection;
        this.accessor = accessor;
        ExecutorEngine executorEngine = BackendExecutorContext.getInstance().getExecutorEngine();
        boolean isSerialExecute = backendConnection.isSerialExecute();
        jdbcExecutor = new ProxyJDBCExecutor(backendConnection, new JDBCExecutor(executorEngine, isSerialExecute), accessor);
        rawExecutor = new ProxyRawExecutor(executorEngine, isSerialExecute);
    }
    
    /**
     * Check execute prerequisites.
     *
     * @param executionContext execution context
     */
    public void checkExecutePrerequisites(final ExecutionContext executionContext) {
        if (isExecuteDDLInXATransaction(executionContext.getSqlStatementContext().getSqlStatement())) {
            throw new TableModifyInTransactionException(executionContext.getSqlStatementContext());
        }
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        TransactionStatus transactionStatus = backendConnection.getTransactionStatus();
        return TransactionType.XA == transactionStatus.getTransactionType() && sqlStatement instanceof DDLStatement && transactionStatus.isInTransaction();
    }
    
    /**
     * Execute SQL.
     *
     * @param executionContext execution context
     * @return execute response
     * @throws SQLException SQL exception
     */
    public BackendResponse execute(final ExecutionContext executionContext) throws SQLException {
        Collection<ExecuteResult> executeResults = execute(executionContext,
                executionContext.getSqlStatementContext().getSqlStatement() instanceof InsertStatement, SQLExecutorExceptionHandler.isExceptionThrown());
        ExecuteResult executeResultSample = executeResults.iterator().next();
        return executeResultSample instanceof QueryResult
                ? processExecuteQuery(executionContext, executeResults, (QueryResult) executeResultSample) : processExecuteUpdate(executionContext, executeResults);
    }
    
    private Collection<ExecuteResult> execute(final ExecutionContext executionContext, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        int maxConnectionsSizePerQuery = ProxyContext.getInstance().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getMetaDataContexts().getMetaDataMap().get(backendConnection.getSchemaName()).getRuleMetaData().getRules();
        return rules.stream().anyMatch(each -> each instanceof RawExecutionRule) 
                ? rawExecute(executionContext, rules, maxConnectionsSizePerQuery) : useDriverToExecute(executionContext, rules, maxConnectionsSizePerQuery, isReturnGeneratedKeys, isExceptionThrown);
    }
    
    private Collection<ExecuteResult> rawExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, final int maxConnectionsSizePerQuery) throws SQLException {
        RawExecutionPrepareEngine prepareEngine = new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, rules);
        Collection<ExecutionGroup<RawSQLExecutionUnit>> executionGroups = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        // TODO handle query header
        return rawExecutor.execute(executionGroups, new RawSQLExecutorCallback());
    }
    
    private Collection<ExecuteResult> useDriverToExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, 
                                                         final int maxConnectionsSizePerQuery, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = accessor.getExecutionPrepareEngine(
                backendConnection, maxConnectionsSizePerQuery, new StatementOption(isReturnGeneratedKeys), rules);
        Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        return jdbcExecutor.execute(executionGroups, isExceptionThrown, isReturnGeneratedKeys);
    }
    
    private BackendResponse processExecuteQuery(final ExecutionContext executionContext, final Collection<ExecuteResult> executeResults, final QueryResult executeResultSample) throws SQLException {
        ShardingSphereMetaData metaData = ProxyContext.getInstance().getMetaData(backendConnection.getSchemaName());
        int columnCount = executeResultSample.getMetaData().getColumnCount();
        List<QueryHeader> queryHeaders = new ArrayList<>(columnCount);
        for (int columnIndex = 1; columnIndex <= columnCount; columnIndex++) {
            if (hasSelectExpandProjections(executionContext.getSqlStatementContext())) {
                queryHeaders.add(QueryHeaderBuilder.build(
                        ((SelectStatementContext) executionContext.getSqlStatementContext()).getProjectionsContext(), executeResultSample, metaData, columnIndex));
            } else {
                queryHeaders.add(QueryHeaderBuilder.build(executeResultSample, metaData, columnIndex));
            }
        }
        return getQueryResponses(queryHeaders, executeResults);
    }
    
    private BackendResponse getQueryResponses(final List<QueryHeader> queryHeaders, final Collection<ExecuteResult> executeResults) {
        QueryResponse result = new QueryResponse(queryHeaders);
        for (ExecuteResult each : executeResults) {
            result.getQueryResults().add((QueryResult) each);
        }
        return result;
    }
    
    private boolean hasSelectExpandProjections(final SQLStatementContext<?> sqlStatementContext) {
        return sqlStatementContext instanceof SelectStatementContext && !((SelectStatementContext) sqlStatementContext).getProjectionsContext().getExpandProjections().isEmpty();
    }
    
    private UpdateResponse processExecuteUpdate(final ExecutionContext executionContext, final Collection<ExecuteResult> executeResults) {
        UpdateResponse result = new UpdateResponse(executeResults);
        if (executionContext.getSqlStatementContext().getSqlStatement() instanceof InsertStatement) {
            result.setType("INSERT");
        } else if (executionContext.getSqlStatementContext().getSqlStatement() instanceof DeleteStatement) {
            result.setType("DELETE");
        } else if (executionContext.getSqlStatementContext().getSqlStatement() instanceof UpdateStatement) {
            result.setType("UPDATE");
        }
        return result;
    }
}
