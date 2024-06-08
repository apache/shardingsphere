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

package org.apache.shardingsphere.driver.executor.engine;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Driver execute executor.
 */
@RequiredArgsConstructor
public final class DriverExecuteExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private ExecuteType executeType = ExecuteType.REGULAR;
    
    /**
     * Execute.
     *
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            executeType = ExecuteType.TRAFFIC;
            return trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, executeCallback::execute);
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            executeType = ExecuteType.FEDERATION;
            ResultSet resultSet = sqlFederationEngine.executeQuery(
                    prepareEngine, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
            return null != resultSet;
        }
        ExecutionContext executionContext = createExecutionContext(database, queryContext);
        if (hasRawExecutionRule(database)) {
            Collection<ExecuteResult> results = rawExecutor.execute(createRawExecutionGroupContext(database, executionContext), queryContext, new RawSQLExecutorCallback());
            return results.iterator().next() instanceof QueryResult;
        }
        boolean isNeedImplicitCommitTransaction = isNeedImplicitCommitTransaction(
                connection, queryContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1);
        return executeWithExecutionContext(database, executeCallback, executionContext, prepareEngine, isNeedImplicitCommitTransaction, addCallback, replayCallback);
    }
    
    private ExecuteQueryCallback getExecuteQueryCallback(final ShardingSphereDatabase database, final QueryContext queryContext, final String jdbcDriverType) {
        return JDBCDriverType.STATEMENT.equals(jdbcDriverType)
                ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                : new PreparedStatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    private boolean hasRawExecutionRule(final ShardingSphereDatabase database) {
        return !database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty();
    }
    
    private Collection<Statement> getStatements(final ExecutionGroup<JDBCExecutionUnit> executionGroup) {
        Collection<Statement> result = new LinkedList<>();
        for (JDBCExecutionUnit each : executionGroup.getInputs()) {
            result.add(each.getStorageResource());
        }
        return result;
    }
    
    private Collection<List<Object>> getParameterSets(final ExecutionGroup<JDBCExecutionUnit> executionGroup) {
        Collection<List<Object>> result = new LinkedList<>();
        for (JDBCExecutionUnit each : executionGroup.getInputs()) {
            result.add(each.getExecutionUnit().getSqlUnit().getParameters());
        }
        return result;
    }
    
    private ExecutionContext createExecutionContext(final ShardingSphereDatabase database, final QueryContext queryContext) {
        RuleMetaData globalRuleMetaData = metaData.getGlobalRuleMetaData();
        SQLAuditEngine.audit(queryContext, globalRuleMetaData, database);
        return new KernelProcessor().generateExecutionContext(queryContext, database, globalRuleMetaData, metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules()).prepare(
                executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
    }
    
    private <T> T executeWithImplicitCommitTransaction(final ImplicitTransactionCallback<T> callback, final Connection connection, final DatabaseType databaseType) throws SQLException {
        T result;
        try {
            connection.setAutoCommit(false);
            result = callback.execute();
            connection.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, databaseType);
        } finally {
            connection.setAutoCommit(true);
        }
        return result;
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext,
                                                                                 final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
    }
    
    @SuppressWarnings("rawtypes")
    private boolean executeWithExecutionContext(final ShardingSphereDatabase database, final StatementExecuteCallback statementExecuteCallback, final ExecutionContext executionContext,
                                                final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final boolean isNeedImplicitCommitTransaction,
                                                final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return isNeedImplicitCommitTransaction
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecute(database, statementExecuteCallback, executionContext, prepareEngine, addCallback, replayCallback), connection,
                        database.getProtocolType())
                : useDriverToExecute(database, statementExecuteCallback, executionContext, prepareEngine, addCallback, replayCallback);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean useDriverToExecute(final ShardingSphereDatabase database, final StatementExecuteCallback callback, final ExecutionContext executionContext,
                                       final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                       final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(database, executionContext, prepareEngine);
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            addCallback.add(getStatements(each), JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType()) ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = createExecuteCallback(database, callback, executionContext.getSqlStatementContext().getSqlStatement(), prepareEngine.getType());
        return useDriverToExecute(database, executionGroupContext, executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), jdbcExecutorCallback);
    }
    
    private boolean useDriverToExecute(final ShardingSphereDatabase database, final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final QueryContext queryContext,
                                       final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Boolean> callback) throws SQLException {
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            List<Boolean> results = doExecute(database, executionGroupContext, queryContext, routeUnits, callback);
            return null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
        } finally {
            processEngine.completeSQLExecution(executionGroupContext.getReportContext().getProcessId());
        }
    }
    
    private <T> List<T> doExecute(final ShardingSphereDatabase database, final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                  final QueryContext queryContext, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<T> callback) throws SQLException {
        List<T> results = jdbcExecutor.execute(executionGroupContext, callback);
        new MetaDataRefreshEngine(
                connection.getContextManager().getPersistServiceFacade().getMetaDataManagerPersistService(), database, metaData.getProps()).refresh(queryContext.getSqlStatementContext(), routeUnits);
        return results;
    }
    
    private JDBCExecutorCallback<Boolean> createExecuteCallback(final ShardingSphereDatabase database,
                                                                final StatementExecuteCallback executeCallback, final SQLStatement sqlStatement, final String jdbcDriverType) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(database.getProtocolType(), database.getResourceMetaData(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return JDBCDriverType.STATEMENT.equals(jdbcDriverType) ? executeCallback.execute(sql, statement) : ((PreparedStatement) statement).execute();
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement1, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private boolean isNeedImplicitCommitTransaction(final ShardingSphereConnection connection, final SQLStatement sqlStatement, final boolean multiExecutionUnits) {
        if (!connection.getAutoCommit()) {
            return false;
        }
        TransactionType transactionType = connection.getContextManager().getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getSingleRule(TransactionRule.class).getDefaultType();
        boolean isInTransaction = connection.getDatabaseConnectionManager().getConnectionTransaction().isInTransaction();
        if (!TransactionType.isDistributedTransaction(transactionType) || isInTransaction) {
            return false;
        }
        return isWriteDMLStatement(sqlStatement) && multiExecutionUnits;
    }
    
    private boolean isWriteDMLStatement(final SQLStatement sqlStatement) {
        return sqlStatement instanceof DMLStatement && !(sqlStatement instanceof SelectStatement);
    }
    
    /**
     * Get result set.
     *
     * @return result set
     */
    public Optional<ResultSet> getResultSet() {
        switch (executeType) {
            case TRAFFIC:
                return Optional.of(trafficExecutor.getResultSet());
            case FEDERATION:
                return Optional.of(sqlFederationEngine.getResultSet());
            default:
                return Optional.empty();
        }
    }
    
    public enum ExecuteType {
        
        TRAFFIC,
        
        FEDERATION,
        
        REGULAR
    }
}
