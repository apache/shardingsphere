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

import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.sane.SaneQueryResultEngineFactory;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RawExecutionRule;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.JDBCDatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.JDBCBackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.dialect.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.OpenGaussStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.PostgreSQLStatement;
import org.apache.shardingsphere.transaction.core.TransactionType;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Proxy SQL Executor.
 */
public final class ProxySQLExecutor {
    
    private final String type;
    
    private final JDBCBackendConnection backendConnection;
    
    private final ProxyJDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    public ProxySQLExecutor(final String type, final JDBCBackendConnection backendConnection, final JDBCDatabaseCommunicationEngine databaseCommunicationEngine) {
        this.type = type;
        this.backendConnection = backendConnection;
        ExecutorEngine executorEngine = BackendExecutorContext.getInstance().getExecutorEngine();
        boolean isSerialExecute = backendConnection.isSerialExecute();
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        jdbcExecutor = new ProxyJDBCExecutor(type, backendConnection.getConnectionSession(), databaseCommunicationEngine, new JDBCExecutor(executorEngine, isSerialExecute));
        rawExecutor = new RawExecutor(executorEngine, isSerialExecute, metaDataContexts.getMetaData().getProps(), ProxyContext.getInstance().getContextManager().getInstanceContext()
                .getEventBusContext());
    }
    
    /**
     * Check execute prerequisites.
     *
     * @param executionContext execution context
     */
    public void checkExecutePrerequisites(final ExecutionContext executionContext) {
        if (isExecuteDDLInXATransaction(executionContext.getSqlStatementContext().getSqlStatement())
                || isExecuteDDLInPostgreSQLOpenGaussTransaction(executionContext.getSqlStatementContext().getSqlStatement())) {
            String tableName = executionContext.getSqlStatementContext() instanceof TableAvailable && !((TableAvailable) executionContext.getSqlStatementContext()).getAllTables().isEmpty()
                    ? ((TableAvailable) executionContext.getSqlStatementContext()).getAllTables().iterator().next().getTableName().getIdentifier().getValue()
                    : "unknown_table";
            throw new TableModifyInTransactionException(tableName);
        }
    }
    
    private boolean isExecuteDDLInXATransaction(final SQLStatement sqlStatement) {
        TransactionStatus transactionStatus = backendConnection.getConnectionSession().getTransactionStatus();
        return TransactionType.XA == transactionStatus.getTransactionType() && transactionStatus.isInTransaction() && isUnsupportedDDLStatement(sqlStatement);
    }
    
    private boolean isExecuteDDLInPostgreSQLOpenGaussTransaction(final SQLStatement sqlStatement) {
        // TODO implement DDL statement commit/rollback in PostgreSQL/openGauss transaction
        boolean isPostgreSQLOpenGaussStatement = isPostgreSQLOrOpenGaussStatement(sqlStatement);
        boolean isSupportedStatement = isSupportedSQLStatement(sqlStatement);
        return sqlStatement instanceof DDLStatement && !isSupportedStatement && isPostgreSQLOpenGaussStatement && backendConnection.getConnectionSession().getTransactionStatus().isInTransaction();
    }
    
    private boolean isSupportedSQLStatement(final SQLStatement sqlStatement) {
        return isCursorStatement(sqlStatement) || sqlStatement instanceof TruncateStatement;
    }
    
    private boolean isCursorStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof OpenGaussCursorStatement
                || sqlStatement instanceof CloseStatement || sqlStatement instanceof MoveStatement || sqlStatement instanceof FetchStatement;
    }
    
    private boolean isUnsupportedDDLStatement(final SQLStatement sqlStatement) {
        if (isPostgreSQLOrOpenGaussStatement(sqlStatement) && isSupportedSQLStatement(sqlStatement)) {
            return false;
        }
        return sqlStatement instanceof DDLStatement;
    }
    
    private boolean isPostgreSQLOrOpenGaussStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof PostgreSQLStatement || sqlStatement instanceof OpenGaussStatement;
    }
    
    /**
     * Execute SQL.
     *
     * @param executionContext execution context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public List<ExecuteResult> execute(final ExecutionContext executionContext) throws SQLException {
        String databaseName = backendConnection.getConnectionSession().getDatabaseName();
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = backendConnection.getConnectionSession().getTransactionStatus().isInConnectionHeldTransaction()
                ? 1
                : ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        boolean isReturnGeneratedKeys = executionContext.getSqlStatementContext().getSqlStatement() instanceof MySQLInsertStatement;
        return execute(executionContext, rules, maxConnectionsSizePerQuery, isReturnGeneratedKeys);
    }
    
    private List<ExecuteResult> execute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules,
                                        final int maxConnectionsSizePerQuery, final boolean isReturnGeneratedKeys) throws SQLException {
        return hasRawExecutionRule(rules) ? rawExecute(executionContext, rules, maxConnectionsSizePerQuery)
                : useDriverToExecute(executionContext, rules, maxConnectionsSizePerQuery, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    private boolean hasRawExecutionRule(final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            if (each instanceof RawExecutionRule) {
                return true;
            }
        }
        return false;
    }
    
    private List<ExecuteResult> rawExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, final int maxConnectionsSizePerQuery) throws SQLException {
        RawExecutionPrepareEngine prepareEngine = new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, rules);
        ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext;
        try {
            executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        } catch (final SQLException ex) {
            return getSaneExecuteResults(executionContext, ex);
        }
        executionGroupContext.setDatabaseName(backendConnection.getConnectionSession().getDatabaseName());
        executionGroupContext.setGrantee(backendConnection.getConnectionSession().getGrantee());
        executionGroupContext.setExecutionID(backendConnection.getConnectionSession().getExecutionId());
        // TODO handle query header
        return rawExecutor.execute(executionGroupContext, executionContext.getQueryContext(), new RawSQLExecutorCallback(ProxyContext.getInstance().getContextManager().getInstanceContext()
                .getEventBusContext()));
    }
    
    private List<ExecuteResult> useDriverToExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules,
                                                   final int maxConnectionsSizePerQuery, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        JDBCBackendStatement statementManager = (JDBCBackendStatement) backendConnection.getConnectionSession().getStatementManager();
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(type, maxConnectionsSizePerQuery, backendConnection, statementManager,
                new StatementOption(isReturnGeneratedKeys), rules, backendConnection.getConnectionSession().getDatabaseType());
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
        try {
            executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
        } catch (final SQLException ex) {
            return getSaneExecuteResults(executionContext, ex);
        }
        executionGroupContext.setDatabaseName(backendConnection.getConnectionSession().getDatabaseName());
        executionGroupContext.setGrantee(backendConnection.getConnectionSession().getGrantee());
        executionGroupContext.setExecutionID(backendConnection.getConnectionSession().getExecutionId());
        return jdbcExecutor.execute(executionContext.getQueryContext(), executionGroupContext, isReturnGeneratedKeys, isExceptionThrown);
    }
    
    private List<ExecuteResult> getSaneExecuteResults(final ExecutionContext executionContext, final SQLException originalException) throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getDatabase(backendConnection.getConnectionSession().getDatabaseName()).getResourceMetaData().getDatabaseType();
        Optional<ExecuteResult> executeResult = SaneQueryResultEngineFactory.getInstance(databaseType)
                .getSaneQueryResult(executionContext.getSqlStatementContext().getSqlStatement(), originalException);
        if (executeResult.isPresent()) {
            return Collections.singletonList(executeResult.get());
        }
        throw originalException;
    }
}
