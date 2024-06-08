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
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
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
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.implicit.ImplicitTransactionCallback;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Driver execute update executor.
 */
@RequiredArgsConstructor
public final class DriverExecuteUpdateExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    /**
     * Execute update.
     *
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param updateCallback statement execute update callback
     * @param replayCallback statement replay callback
     * @param addCallback statement add callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int executeUpdate(final ShardingSphereDatabase database, final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                             final StatementExecuteUpdateCallback updateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, updateCallback::executeUpdate);
        }
        ExecutionContext executionContext = createExecutionContext(database, queryContext);
        return hasRawExecutionRule(database)
                ? accumulate(rawExecutor.execute(createRawExecutionGroupContext(database, executionContext), queryContext, new RawSQLExecutorCallback()))
                : executeUpdate(database, updateCallback, queryContext.getSqlStatementContext(), executionContext, prepareEngine, isNeedImplicitCommitTransaction(
                        connection, queryContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1), addCallback, replayCallback);
    }
    
    @SuppressWarnings("rawtypes")
    private int executeUpdate(final ShardingSphereDatabase database, final StatementExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext,
                              final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final boolean isNeedImplicitCommitTransaction,
                              final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return isNeedImplicitCommitTransaction
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecuteUpdate(
                        database, updateCallback, sqlStatementContext, executionContext, prepareEngine, addCallback, replayCallback), connection, database.getProtocolType())
                : useDriverToExecuteUpdate(database, updateCallback, sqlStatementContext, executionContext, prepareEngine, addCallback, replayCallback);
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
        SQLAuditEngine.audit(queryContext, metaData.getGlobalRuleMetaData(), database);
        return new KernelProcessor().generateExecutionContext(
                queryContext, database, metaData.getGlobalRuleMetaData(), metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
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
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private int useDriverToExecuteUpdate(final ShardingSphereDatabase database, final StatementExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext,
                                         final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                         final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(database, executionContext, prepareEngine);
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            addCallback.add(getStatements(each), JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType()) ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        JDBCExecutorCallback<Integer> callback = createExecuteUpdateCallback(database, updateCallback, sqlStatementContext, prepareEngine.getType());
        return useDriverToExecuteUpdate(database, executionGroupContext, executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), callback);
    }
    
    private int useDriverToExecuteUpdate(final ShardingSphereDatabase database, final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                         final QueryContext queryContext, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Integer> callback) throws SQLException {
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            List<Integer> results = doExecute(database, executionGroupContext, queryContext, routeUnits, callback);
            return isNeedAccumulate(database.getRuleMetaData().getRules(), queryContext.getSqlStatementContext()) ? accumulate(results) : results.get(0);
        } finally {
            processEngine.completeSQLExecution(executionGroupContext.getReportContext().getProcessId());
        }
    }
    
    private boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof TableAvailable)) {
            return false;
        }
        for (ShardingSphereRule each : rules) {
            Optional<DataNodeRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataNodeRuleAttribute.class);
            if (ruleAttribute.isPresent() && ruleAttribute.get().isNeedAccumulate(((TableAvailable) sqlStatementContext).getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    private int accumulate(final List<Integer> updateResults) {
        int result = 0;
        for (Integer each : updateResults) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    private <T> List<T> doExecute(final ShardingSphereDatabase database, final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final QueryContext queryContext,
                                  final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<T> callback) throws SQLException {
        List<T> results = jdbcExecutor.execute(executionGroupContext, callback);
        new MetaDataRefreshEngine(
                connection.getContextManager().getPersistServiceFacade().getMetaDataManagerPersistService(), database, metaData.getProps()).refresh(queryContext.getSqlStatementContext(), routeUnits);
        return results;
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext,
                                                                                 final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) throws SQLException {
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
    }
    
    private JDBCExecutorCallback<Integer> createExecuteUpdateCallback(final ShardingSphereDatabase database,
                                                                      final StatementExecuteUpdateCallback updateCallback, final SQLStatementContext sqlStatementContext, final String jdbcDriverType) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Integer>(database.getProtocolType(), database.getResourceMetaData(), sqlStatementContext.getSqlStatement(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return JDBCDriverType.STATEMENT.equals(jdbcDriverType) ? updateCallback.executeUpdate(sql, statement) : ((PreparedStatement) statement).executeUpdate();
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
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
}
