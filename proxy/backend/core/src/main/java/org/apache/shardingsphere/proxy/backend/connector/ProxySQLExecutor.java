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

package org.apache.shardingsphere.proxy.backend.connector;

import com.google.common.base.Strings;
import lombok.Getter;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.DialectDatabaseMetaData;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.database.exception.core.exception.transaction.TableModifyInTransactionException;
import org.apache.shardingsphere.infra.binder.context.segment.table.TablesContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.connection.transaction.TransactionConnectionContext;
import org.apache.shardingsphere.infra.spi.type.ordered.OrderedSPILoader;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.executor.ProxyJDBCExecutor;
import org.apache.shardingsphere.proxy.backend.connector.jdbc.statement.JDBCBackendStatement;
import org.apache.shardingsphere.proxy.backend.connector.sane.DialectSaneQueryResultEngine;
import org.apache.shardingsphere.proxy.backend.context.BackendExecutorContext;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.session.transaction.TransactionStatus;
import org.apache.shardingsphere.proxy.backend.util.TransactionUtils;
import org.apache.shardingsphere.sql.parser.statement.core.enums.TransactionIsolationLevel;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.TruncateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.spi.TransactionHook;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

/**
 * Proxy SQL Executor.
 */
public final class ProxySQLExecutor {
    
    private final JDBCDriverType type;
    
    private final ProxyDatabaseConnectionManager databaseConnectionManager;
    
    private final ProxyJDBCExecutor regularExecutor;
    
    private final RawExecutor rawExecutor;
    
    @Getter
    private final SQLFederationEngine sqlFederationEngine;
    
    @SuppressWarnings("rawtypes")
    private final Map<ShardingSphereRule, TransactionHook> transactionHooks = OrderedSPILoader.getServices(
            TransactionHook.class, ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules());
    
    public ProxySQLExecutor(final JDBCDriverType type,
                            final ProxyDatabaseConnectionManager databaseConnectionManager, final DatabaseProxyConnector databaseProxyConnector, final SQLStatementContext sqlStatementContext) {
        this.type = type;
        this.databaseConnectionManager = databaseConnectionManager;
        ExecutorEngine executorEngine = BackendExecutorContext.getInstance().getExecutorEngine();
        ConnectionContext connectionContext = databaseConnectionManager.getConnectionSession().getConnectionContext();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connectionContext);
        regularExecutor = new ProxyJDBCExecutor(type, databaseConnectionManager.getConnectionSession(), databaseProxyConnector, jdbcExecutor);
        rawExecutor = new RawExecutor(executorEngine, connectionContext);
        MetaDataContexts metaDataContexts = ProxyContext.getInstance().getContextManager().getMetaDataContexts();
        String currentDatabaseName = Strings.isNullOrEmpty(databaseConnectionManager.getConnectionSession().getCurrentDatabaseName())
                ? databaseConnectionManager.getConnectionSession().getUsedDatabaseName()
                : databaseConnectionManager.getConnectionSession().getCurrentDatabaseName();
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(currentDatabaseName);
        String currentSchemaName = getSchemaName(sqlStatementContext, currentDatabase);
        sqlFederationEngine = new SQLFederationEngine(currentDatabaseName, currentSchemaName, metaDataContexts.getMetaData(), metaDataContexts.getStatistics(), jdbcExecutor);
    }
    
    private String getSchemaName(final SQLStatementContext sqlStatementContext, final ShardingSphereDatabase database) {
        String defaultSchemaName = new DatabaseTypeRegistry(sqlStatementContext.getSqlStatement().getDatabaseType()).getDefaultSchemaName(database.getName());
        return sqlStatementContext.getTablesContext().getSchemaName().orElse(defaultSchemaName);
    }
    
    /**
     * Check execute prerequisites.
     *
     * @param sqlStatementContext execution context
     */
    public void checkExecutePrerequisites(final SQLStatementContext sqlStatementContext) {
        ShardingSpherePreconditions.checkState(
                isValidExecutePrerequisites(sqlStatementContext.getSqlStatement()), () -> new TableModifyInTransactionException(getTableName(sqlStatementContext.getTablesContext())));
    }
    
    private boolean isValidExecutePrerequisites(final SQLStatement sqlStatement) {
        return !(sqlStatement instanceof DDLStatement) || isSupportDDLInTransaction(sqlStatement.getDatabaseType(), (DDLStatement) sqlStatement);
    }
    
    private boolean isSupportDDLInTransaction(final DatabaseType databaseType, final DDLStatement sqlStatement) {
        DialectTransactionOption transactionOption = new DatabaseTypeRegistry(databaseType).getDialectDatabaseMetaData().getTransactionOption();
        boolean isDDLWithoutMetaDataChanged = isDDLWithoutMetaDataChanged(sqlStatement);
        if (isInXATransaction()) {
            return transactionOption.isSupportDDLInXATransaction() && (isDDLWithoutMetaDataChanged || transactionOption.isSupportMetaDataRefreshInTransaction());
        }
        if (isInLocalTransaction()) {
            return transactionOption.isSupportMetaDataRefreshInTransaction() || isDDLWithoutMetaDataChanged;
        }
        return true;
    }
    
    private boolean isInXATransaction() {
        TransactionType transactionType = TransactionUtils.getTransactionType(databaseConnectionManager.getConnectionSession().getConnectionContext().getTransactionContext());
        TransactionStatus transactionStatus = databaseConnectionManager.getConnectionSession().getTransactionStatus();
        return TransactionType.XA == transactionType && transactionStatus.isInTransaction();
    }
    
    private boolean isInLocalTransaction() {
        return databaseConnectionManager.getConnectionSession().getTransactionStatus().isInTransaction();
    }
    
    // TODO should be removed after metadata refresh supported for all database.
    private boolean isDDLWithoutMetaDataChanged(final DDLStatement sqlStatement) {
        return isCursorStatement(sqlStatement) || sqlStatement instanceof TruncateStatement;
    }
    
    private boolean isCursorStatement(final DDLStatement sqlStatement) {
        return sqlStatement instanceof CursorStatement || sqlStatement instanceof CloseStatement || sqlStatement instanceof MoveStatement || sqlStatement instanceof FetchStatement;
    }
    
    private String getTableName(final TablesContext tablesContext) {
        return tablesContext.getSimpleTables().isEmpty() ? "unknown_table" : tablesContext.getSimpleTables().iterator().next().getTableName().getIdentifier().getValue();
    }
    
    /**
     * Execute SQL.
     *
     * @param executionContext execution context
     * @return execute results
     * @throws SQLException SQL exception
     */
    public List<ExecuteResult> execute(final ExecutionContext executionContext) throws SQLException {
        String databaseName = databaseConnectionManager.getConnectionSession().getUsedDatabaseName();
        Collection<ShardingSphereRule> rules = ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData().getDatabase(databaseName).getRuleMetaData().getRules();
        int maxConnectionsSizePerQuery = ProxyContext.getInstance()
                .getContextManager().getMetaDataContexts().getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        DialectDatabaseMetaData dialectDatabaseMetaData = new DatabaseTypeRegistry(executionContext.getSqlStatementContext().getSqlStatement().getDatabaseType()).getDialectDatabaseMetaData();
        boolean isReturnGeneratedKeys = executionContext.getSqlStatementContext().getSqlStatement() instanceof InsertStatement && dialectDatabaseMetaData.getGeneratedKeyOption().isPresent();
        return hasRawExecutionRule(rules)
                ? rawExecute(executionContext, rules, maxConnectionsSizePerQuery)
                : useDriverToExecute(executionContext, rules, maxConnectionsSizePerQuery, isReturnGeneratedKeys, SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    private boolean hasRawExecutionRule(final Collection<ShardingSphereRule> rules) {
        for (ShardingSphereRule each : rules) {
            if (each.getAttributes().findAttribute(RawExecutionRuleAttribute.class).isPresent()) {
                return true;
            }
        }
        return false;
    }
    
    private List<ExecuteResult> rawExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules, final int maxConnectionsSizePerQuery) throws SQLException {
        RawExecutionPrepareEngine prepareEngine = new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, rules);
        ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext;
        try {
            String databaseName = databaseConnectionManager.getConnectionSession().getUsedDatabaseName();
            executionGroupContext = prepareEngine.prepare(databaseName, executionContext, executionContext.getExecutionUnits(),
                    new ExecutionGroupReportContext(databaseConnectionManager.getConnectionSession().getProcessId(),
                            databaseName, databaseConnectionManager.getConnectionSession().getConnectionContext().getGrantee()));
        } catch (final SQLException ex) {
            return getSaneExecuteResults(executionContext, ex);
        }
        // TODO handle query header
        return rawExecutor.execute(executionGroupContext, executionContext.getQueryContext(), new RawSQLExecutorCallback());
    }
    
    private List<ExecuteResult> useDriverToExecute(final ExecutionContext executionContext, final Collection<ShardingSphereRule> rules,
                                                   final int maxConnectionsSizePerQuery, final boolean isReturnGeneratedKeys, final boolean isExceptionThrown) throws SQLException {
        JDBCBackendStatement statementManager = (JDBCBackendStatement) databaseConnectionManager.getConnectionSession().getStatementManager();
        String databaseName = databaseConnectionManager.getConnectionSession().getUsedDatabaseName();
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(
                type, maxConnectionsSizePerQuery, databaseConnectionManager, statementManager, new StatementOption(isReturnGeneratedKeys), rules,
                ProxyContext.getInstance().getContextManager().getMetaDataContexts().getMetaData());
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext;
        try {
            executionGroupContext = prepareEngine.prepare(databaseName, executionContext, executionContext.getExecutionUnits(),
                    new ExecutionGroupReportContext(databaseConnectionManager.getConnectionSession().getProcessId(),
                            databaseName, databaseConnectionManager.getConnectionSession().getConnectionContext().getGrantee()));
        } catch (final SQLException ex) {
            return getSaneExecuteResults(executionContext, ex);
        }
        executeTransactionHooksBeforeExecuteSQL(databaseConnectionManager.getConnectionSession());
        return regularExecutor.execute(executionContext.getQueryContext(), executionGroupContext, isReturnGeneratedKeys, isExceptionThrown);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void executeTransactionHooksBeforeExecuteSQL(final ConnectionSession connectionSession) throws SQLException {
        if (!getTransactionContext(connectionSession).isInTransaction()) {
            return;
        }
        for (Entry<ShardingSphereRule, TransactionHook> entry : transactionHooks.entrySet()) {
            entry.getValue().beforeExecuteSQL(entry.getKey(), ProxyContext.getInstance().getContextManager().getDatabaseType(),
                    connectionSession.getDatabaseConnectionManager().getCachedConnections().values(), getTransactionContext(connectionSession),
                    connectionSession.getIsolationLevel().orElse(TransactionIsolationLevel.READ_COMMITTED));
        }
    }
    
    private TransactionConnectionContext getTransactionContext(final ConnectionSession connectionSession) {
        return connectionSession.getDatabaseConnectionManager().getConnectionSession().getConnectionContext().getTransactionContext();
    }
    
    private List<ExecuteResult> getSaneExecuteResults(final ExecutionContext executionContext, final SQLException originalException) throws SQLException {
        DatabaseType databaseType = ProxyContext.getInstance().getContextManager().getDatabase(databaseConnectionManager.getConnectionSession().getUsedDatabaseName()).getProtocolType();
        Optional<ExecuteResult> executeResult = DatabaseTypedSPILoader.findService(DialectSaneQueryResultEngine.class, databaseType)
                .flatMap(optional -> optional.getSaneQueryResult(executionContext.getSqlStatementContext().getSqlStatement(), originalException));
        return executeResult.map(Collections::singletonList).orElseThrow(() -> originalException);
    }
}
