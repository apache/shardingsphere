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
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.RawExecutionRule;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.communication.raw.ProxyRawExecutor;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.exception.TableModifyInTransactionException;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;

/**
 * Proxy SQL Executor.
 */
public final class ProxySQLExecutor {
    
    private final String type;
    
    private final BackendConnection backendConnection;
    
    private final ProxyJDBCExecutor jdbcExecutor;
    
    private final ProxyRawExecutor rawExecutor;
    
    public ProxySQLExecutor(final String type, final BackendConnection backendConnection) {
        this.type = type;
        this.backendConnection = backendConnection;
        ExecutorEngine executorEngine = BackendExecutorContext.getInstance().getExecutorEngine();
        boolean isSerialExecute = backendConnection.isSerialExecute();
        jdbcExecutor = new ProxyJDBCExecutor(type, backendConnection, new JDBCExecutor(executorEngine, isSerialExecute));
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
     * @return execute results
     * @throws SQLException SQL exception
     */
    public Collection<ExecuteResult> execute(final ExecutionContext executionContext) throws SQLException {
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getMetaDataContexts().getMetaDataMap().get(backendConnection.getSchemaName()).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = ProxyContext.getInstance().getMetaDataContexts().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        boolean isReturnGeneratedKeys = executionContext.getSqlStatementContext().getSqlStatement() instanceof InsertStatement;
        return rules.stream().anyMatch(each -> each instanceof RawExecutionRule) 
                ? rawExecute(executionContext, rules, maxConnectionsSizePerQuery)
                : useDriverToExecute(executionContext, rules, maxConnectionsSizePerQuery, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    private Collection<ExecuteResult> rawExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, final int maxConnectionsSizePerQuery) throws SQLException {
        RawExecutionPrepareEngine prepareEngine = new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, rules);
        Collection<ExecutionGroup<RawSQLExecutionUnit>> executionGroups = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        // TODO handle query header
        return rawExecutor.execute(executionGroups, new RawSQLExecutorCallback());
    }
    
    private Collection<ExecuteResult> useDriverToExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, 
                                                         final int maxConnectionsSizePerQuery, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                type, maxConnectionsSizePerQuery, backendConnection, new StatementOption(isReturnGeneratedKeys), rules);
        Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        return jdbcExecutor.execute(executionGroups, isExceptionThrown, isReturnGeneratedKeys);
    }
}
