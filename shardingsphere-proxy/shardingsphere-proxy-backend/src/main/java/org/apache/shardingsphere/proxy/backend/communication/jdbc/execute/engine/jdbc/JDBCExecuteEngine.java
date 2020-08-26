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

package org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.engine.jdbc;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.control.panel.spi.engine.SingletonFacadeEngine;
import org.apache.shardingsphere.infra.config.properties.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ExecutorConstant;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.group.ExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.raw.RawSQLExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.ExecuteQueryResult;
import org.apache.shardingsphere.infra.executor.sql.raw.execute.result.query.QueryHeader;
import org.apache.shardingsphere.infra.executor.sql.raw.group.RawExecuteGroupEngine;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.group.StatementOption;
import org.apache.shardingsphere.metrics.enums.MetricsLabelEnum;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.execute.SQLExecuteEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.wrapper.JDBCExecutorWrapper;
import org.apache.shardingsphere.proxy.backend.executor.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.response.BackendResponse;
import org.apache.shardingsphere.proxy.backend.response.query.QueryResponse;
import org.apache.shardingsphere.proxy.backend.response.update.UpdateResponse;
import org.apache.shardingsphere.proxy.backend.schema.ProxySchemaContexts;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.UpdateStatement;

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
    
    private final SQLExecutor sqlExecutor;
    
    private final RawProxyExecutor rawExecutor;
    
    public JDBCExecuteEngine(final BackendConnection backendConnection, final JDBCExecutorWrapper jdbcExecutorWrapper) {
        this.backendConnection = backendConnection;
        this.jdbcExecutorWrapper = jdbcExecutorWrapper;
        sqlExecutor = new SQLExecutor(BackendExecutorContext.getInstance().getExecutorKernel(), backendConnection.isSerialExecute());
        rawExecutor = new RawProxyExecutor(BackendExecutorContext.getInstance().getExecutorKernel(), backendConnection.isSerialExecute());
    }
    
    @Override
    public ExecutionContext generateExecutionContext(final String sql) throws SQLException {
        return jdbcExecutorWrapper.generateExecutionContext(sql);
    }
    
    @Override
    public BackendResponse execute(final ExecutionContext executionContext) throws SQLException {
        SQLStatementContext<?> sqlStatementContext = executionContext.getSqlStatementContext();
        Collection<ExecuteResult> executeResults = execute(executionContext, 
                sqlStatementContext, sqlStatementContext.getSqlStatement() instanceof InsertStatement, ExecutorExceptionHandler.isExceptionThrown());
        ExecuteResult executeResult = executeResults.iterator().next();
        if (executeResult instanceof ExecuteQueryResult) {
            SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.SQL_STATEMENT_COUNT.getName(), "SELECT"));
            return getExecuteQueryResponse(((ExecuteQueryResult) executeResult).getQueryHeaders(), executeResults);
        } else {
            UpdateResponse result = new UpdateResponse(executeResults);
            if (sqlStatementContext.getSqlStatement() instanceof InsertStatement) {
                result.setType("INSERT");
            } else if (sqlStatementContext.getSqlStatement() instanceof DeleteStatement) {
                result.setType("DELETE");
            } else if (sqlStatementContext.getSqlStatement() instanceof UpdateStatement) {
                result.setType("UPDATE");
            }
            if (!Strings.isNullOrEmpty(result.getType())) {
                SingletonFacadeEngine.buildMetrics().ifPresent(metricsHandlerFacade -> metricsHandlerFacade.counterIncrement(MetricsLabelEnum.SQL_STATEMENT_COUNT.getName(), result.getType()));
            }
            return result;
        }
    }
    
    private Collection<ExecuteResult> execute(final ExecutionContext executionContext, 
                                              final SQLStatementContext<?> sqlStatementContext, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        return ExecutorConstant.MANAGED_RESOURCE
                ? executeWithManagedResource(executionContext, sqlStatementContext, isReturnGeneratedKeys, isExceptionThrown) : executeWithUnmanagedResource(executionContext);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private Collection<ExecuteResult> executeWithManagedResource(final ExecutionContext executionContext, final SQLStatementContext<?> sqlStatementContext, 
                                                                 final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        int maxConnectionsSizePerQuery = ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ExecuteGroupEngine executeGroupEngine = jdbcExecutorWrapper.getExecuteGroupEngine(backendConnection, maxConnectionsSizePerQuery, new StatementOption(isReturnGeneratedKeys));
        Collection<InputGroup<StatementExecuteUnit>> inputGroups = executeGroupEngine.generate(executionContext.getExecutionUnits());
        return sqlExecutor.execute(inputGroups,
                new ProxySQLExecutorCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, true),
                new ProxySQLExecutorCallback(sqlStatementContext, backendConnection, jdbcExecutorWrapper, isExceptionThrown, isReturnGeneratedKeys, false));
    }
    
    private Collection<ExecuteResult> executeWithUnmanagedResource(final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = ProxySchemaContexts.getInstance().getSchemaContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        Collection<InputGroup<RawSQLExecuteUnit>> inputGroups = new RawExecuteGroupEngine(maxConnectionsSizePerQuery,
                ProxySchemaContexts.getInstance().getSchema(backendConnection.getSchema()).getSchema().getRules()).generate(executionContext.getExecutionUnits());
        // TODO handle query header
        return rawExecutor.execute(inputGroups, new RawSQLExecutorCallback());
    }
    
    private BackendResponse getExecuteQueryResponse(final List<QueryHeader> queryHeaders, final Collection<ExecuteResult> executeResults) {
        QueryResponse result = new QueryResponse(queryHeaders);
        for (ExecuteResult each : executeResults) {
            result.getQueryResults().add(((ExecuteQueryResult) each).getQueryResult());
        }
        return result;
    }
}
