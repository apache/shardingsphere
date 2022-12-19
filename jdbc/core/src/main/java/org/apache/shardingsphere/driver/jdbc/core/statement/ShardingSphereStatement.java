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

package org.apache.shardingsphere.driver.jdbc.core.statement;

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.driver.executor.DriverExecutor;
import org.apache.shardingsphere.driver.executor.callback.ExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.ExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.exception.syntax.EmptySQLException;
import org.apache.shardingsphere.driver.jdbc.exception.transaction.JDBCTransactionAcrossDatabasesException;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.engine.SQLFederationDeciderEngine;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.executor.check.SQLCheckEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.context.SQLUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.ExecuteResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.impl.driver.jdbc.type.stream.JDBCStreamQueryResult;
import org.apache.shardingsphere.infra.executor.sql.execute.result.update.UpdateResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.StatementOption;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RawExecutionRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.exception.metadata.EmptyTrafficExecutionUnitException;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.ConnectionTransaction;
import org.apache.shardingsphere.transaction.api.TransactionType;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere statement.
 */
public final class ShardingSphereStatement extends AbstractStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final List<Statement> statements;
    
    private final StatementOption statementOption;
    
    @Getter(AccessLevel.PROTECTED)
    private final DriverExecutor executor;
    
    private final KernelProcessor kernelProcessor;
    
    private final TrafficRule trafficRule;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    private final EventBusContext eventBusContext;
    
    private boolean returnGeneratedKeys;
    
    private ExecutionContext executionContext;
    
    private ResultSet currentResultSet;
    
    private String trafficInstanceId;
    
    private SQLFederationDeciderContext deciderContext;
    
    public ShardingSphereStatement(final ShardingSphereConnection connection) {
        this(connection, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency) {
        this(connection, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT);
    }
    
    public ShardingSphereStatement(final ShardingSphereConnection connection, final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability) {
        this.connection = connection;
        metaDataContexts = connection.getContextManager().getMetaDataContexts();
        eventBusContext = connection.getContextManager().getInstanceContext().getEventBusContext();
        statements = new LinkedList<>();
        statementOption = new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        executor = new DriverExecutor(connection);
        kernelProcessor = new KernelProcessor();
        trafficRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        statementManager = new StatementManager();
    }
    
    @Override
    public ResultSet executeQuery(final String sql) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new EmptySQLException().toSQLException();
        }
        ResultSet result;
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, Statement::executeQuery);
            }
            deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()));
            if (deciderContext.isUseSQLFederation()) {
                return executeFederationQuery(queryContext);
            }
            executionContext = createExecutionContext(queryContext);
            List<QueryResult> queryResults = executeQuery0();
            MergedResult mergedResult = mergeQuery(queryResults);
            result = new ShardingSphereResultSet(getResultSets(), mergedResult, this, executionContext);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        } finally {
            currentResultSet = null;
        }
        currentResultSet = result;
        return result;
    }
    
    private static SQLFederationDeciderContext decide(final QueryContext queryContext, final ConfigurationProperties props, final ShardingSphereDatabase database) {
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(database.getRuleMetaData().getRules(), props);
        return deciderEngine.decide(queryContext, database);
    }
    
    private Optional<String> getInstanceIdAndSet(final QueryContext queryContext) {
        Optional<String> result = connection.getConnectionContext().getTrafficInstanceId();
        if (!result.isPresent()) {
            result = getInstanceId(queryContext);
        }
        if (connection.isHoldTransaction() && result.isPresent()) {
            connection.getConnectionContext().setTrafficInstanceId(result.get());
        }
        return result;
    }
    
    private Optional<String> getInstanceId(final QueryContext queryContext) {
        InstanceContext instanceContext = connection.getContextManager().getInstanceContext();
        return null != trafficRule && !trafficRule.getStrategyRules().isEmpty()
                ? new TrafficEngine(trafficRule, instanceContext).dispatch(queryContext, connection.isHoldTransaction())
                : Optional.empty();
    }
    
    private List<QueryResult> executeQuery0() throws SQLException {
        if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
            return executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(),
                    new RawSQLExecutorCallback(eventBusContext)).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
        cacheStatements(executionGroupContext.getInputGroups());
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getStorageTypes(), executionContext.getSqlStatementContext().getSqlStatement(),
                SQLExecutorExceptionHandler.isExceptionThrown(), eventBusContext);
        return executor.getRegularExecutor().executeQuery(executionGroupContext, executionContext.getQueryContext(), callback);
    }
    
    private ResultSet executeFederationQuery(final QueryContext queryContext) throws SQLException {
        StatementExecuteQueryCallback callback = new StatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getStorageTypes(), queryContext.getSqlStatementContext().getSqlStatement(),
                SQLExecutorExceptionHandler.isExceptionThrown(), eventBusContext);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(false, queryContext, metaDataContexts.getMetaData());
        return executor.getFederationExecutor().executeQuery(createDriverExecutionPrepareEngine(), callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine() {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.STATEMENT, maxConnectionsSizePerQuery, connection.getConnectionManager(), statementManager, statementOption,
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getStorageTypes());
    }
    
    @Override
    public int executeUpdate(final String sql) throws SQLException {
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, Statement::executeUpdate);
            }
            executionContext = createExecutionContext(queryContext);
            if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(), new RawSQLExecutorCallback(eventBusContext)));
            }
            return executeUpdate((actualSQL, statement) -> statement.executeUpdate(actualSQL), executionContext.getSqlStatementContext());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int autoGeneratedKeys) throws SQLException {
        if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
            returnGeneratedKeys = true;
        }
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, actualSQL) -> statement.executeUpdate(actualSQL, autoGeneratedKeys));
            }
            executionContext = createExecutionContext(queryContext);
            if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(), new RawSQLExecutorCallback(eventBusContext)));
            }
            return executeUpdate((actualSQL, statement) -> statement.executeUpdate(actualSQL, autoGeneratedKeys), executionContext.getSqlStatementContext());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final int[] columnIndexes) throws SQLException {
        returnGeneratedKeys = true;
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, actualSQL) -> statement.executeUpdate(actualSQL, columnIndexes));
            }
            executionContext = createExecutionContext(queryContext);
            if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(), new RawSQLExecutorCallback(eventBusContext)));
            }
            return executeUpdate((actualSQL, statement) -> statement.executeUpdate(actualSQL, columnIndexes), executionContext.getSqlStatementContext());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        } finally {
            currentResultSet = null;
        }
    }
    
    @Override
    public int executeUpdate(final String sql, final String[] columnNames) throws SQLException {
        returnGeneratedKeys = true;
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, actualSQL) -> statement.executeUpdate(actualSQL, columnNames));
            }
            executionContext = createExecutionContext(queryContext);
            if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                return accumulate(executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(), new RawSQLExecutorCallback(eventBusContext)));
            }
            return executeUpdate((actualSQL, statement) -> statement.executeUpdate(actualSQL, columnNames), executionContext.getSqlStatementContext());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        } finally {
            currentResultSet = null;
        }
    }
    
    private int executeUpdate(final ExecuteUpdateCallback updater, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        return isNeedImplicitCommitTransaction(executionContext) ? executeUpdateWithImplicitCommitTransaction(updater, sqlStatementContext) : useDriverToExecuteUpdate(updater, sqlStatementContext);
    }
    
    private int executeUpdateWithImplicitCommitTransaction(final ExecuteUpdateCallback updater, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        int result;
        try {
            connection.setAutoCommit(false);
            result = useDriverToExecuteUpdate(updater, sqlStatementContext);
            connection.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
        return result;
    }
    
    private int useDriverToExecuteUpdate(final ExecuteUpdateCallback updater, final SQLStatementContext<?> sqlStatementContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
        cacheStatements(executionGroupContext.getInputGroups());
        JDBCExecutorCallback<Integer> callback = createExecuteUpdateCallback(updater, sqlStatementContext);
        return executor.getRegularExecutor().executeUpdate(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), callback);
    }
    
    private JDBCExecutorCallback<Integer> createExecuteUpdateCallback(final ExecuteUpdateCallback updater, final SQLStatementContext<?> sqlStatementContext) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Integer>(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getStorageTypes(), sqlStatementContext.getSqlStatement(), isExceptionThrown,
                eventBusContext) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return updater.executeUpdate(sql, statement);
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    @Override
    public boolean execute(final String sql) throws SQLException {
        try {
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int autoGeneratedKeys) throws SQLException {
        try {
            if (RETURN_GENERATED_KEYS == autoGeneratedKeys) {
                returnGeneratedKeys = true;
            }
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, autoGeneratedKeys));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final int[] columnIndexes) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnIndexes));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
    }
    
    @Override
    public boolean execute(final String sql, final String[] columnNames) throws SQLException {
        try {
            returnGeneratedKeys = true;
            return execute0(sql, (actualSQL, statement) -> statement.execute(actualSQL, columnNames));
            // CHECKSTYLE:OFF
        } catch (final SQLException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
    }
    
    private boolean execute0(final String sql, final ExecuteCallback callback) throws SQLException {
        try {
            QueryContext queryContext = createQueryContext(sql);
            checkSameDatabaseNameInTransaction(queryContext.getSqlStatementContext(), connection.getDatabaseName());
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, actualSQL) -> callback.execute(actualSQL, statement));
            }
            deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()));
            if (deciderContext.isUseSQLFederation()) {
                ResultSet resultSet = executeFederationQuery(queryContext);
                return null != resultSet;
            }
            executionContext = createExecutionContext(queryContext);
            if (metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream().anyMatch(each -> each instanceof RawExecutionRule)) {
                // TODO process getStatement
                Collection<ExecuteResult> results = executor.getRawExecutor().execute(createRawExecutionContext(), executionContext.getQueryContext(), new RawSQLExecutorCallback(eventBusContext));
                return results.iterator().next() instanceof QueryResult;
            }
            return isNeedImplicitCommitTransaction(executionContext) ? executeWithImplicitCommitTransaction(callback) : useDriverToExecute(callback);
        } finally {
            currentResultSet = null;
        }
    }
    
    private void checkSameDatabaseNameInTransaction(final SQLStatementContext<?> sqlStatementContext, final String connectionDatabaseName) {
        if (!connection.getConnectionContext().getTransactionConnectionContext().isInTransaction()) {
            return;
        }
        if (sqlStatementContext instanceof TableAvailable) {
            ((TableAvailable) sqlStatementContext).getTablesContext().getDatabaseName().ifPresent(databaseName -> {
                if (!databaseName.equals(connectionDatabaseName)) {
                    throw new JDBCTransactionAcrossDatabasesException();
                }
            });
        }
    }
    
    private JDBCExecutionUnit createTrafficExecutionUnit(final String trafficInstanceId, final QueryContext queryContext) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        ExecutionGroupContext<JDBCExecutionUnit> context = prepareEngine.prepare(new RouteContext(), Collections.singletonList(executionUnit));
        return context.getInputGroups().stream().flatMap(each -> each.getInputs().stream()).findFirst().orElseThrow(EmptyTrafficExecutionUnitException::new);
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    private QueryContext createQueryContext(final String originSQL) {
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        String sql = sqlParserRule.isSqlCommentParseEnabled() ? originSQL : SQLHintUtils.removeHint(originSQL);
        SQLStatement sqlStatement = sqlParserRule.getSQLParserEngine(
                DatabaseTypeEngine.getTrunkDatabaseTypeName(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType())).parse(sql, false);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData(), sqlStatement, connection.getDatabaseName());
        HintValueContext hintValueContext = sqlParserRule.isSqlCommentParseEnabled() ? new HintValueContext() : SQLHintUtils.extractHint(originSQL);
        return new QueryContext(sqlStatementContext, sql, Collections.emptyList(), hintValueContext);
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) throws SQLException {
        clearStatements();
        SQLCheckEngine.check(queryContext.getSqlStatementContext(), queryContext.getParameters(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules(),
                connection.getDatabaseName(), metaDataContexts.getMetaData().getDatabases(), null);
        return kernelProcessor.generateExecutionContext(queryContext, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()), metaDataContexts.getMetaData().getGlobalRuleMetaData(),
                metaDataContexts.getMetaData().getProps(), connection.getConnectionContext());
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext() throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionContext() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private boolean isNeedImplicitCommitTransaction(final ExecutionContext executionContext) {
        ConnectionTransaction connectionTransaction = connection.getConnectionManager().getConnectionTransaction();
        boolean isInTransaction = connection.getConnectionContext().getTransactionConnectionContext().isInTransaction();
        SQLStatement sqlStatement = executionContext.getSqlStatementContext().getSqlStatement();
        return TransactionType.isDistributedTransaction(connectionTransaction.getTransactionType()) && !isInTransaction && sqlStatement instanceof DMLStatement
                && !(sqlStatement instanceof SelectStatement) && executionContext.getExecutionUnits().size() > 1;
    }
    
    private boolean executeWithImplicitCommitTransaction(final ExecuteCallback callback) throws SQLException {
        boolean result;
        try {
            connection.setAutoCommit(false);
            result = useDriverToExecute(callback);
            connection.commit();
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            connection.rollback();
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType().getType());
        }
        return result;
    }
    
    private boolean useDriverToExecute(final ExecuteCallback callback) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
        cacheStatements(executionGroupContext.getInputGroups());
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = createExecuteCallback(callback, executionContext.getSqlStatementContext().getSqlStatement());
        return executor.getRegularExecutor().execute(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), jdbcExecutorCallback);
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) throws SQLException {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            statements.addAll(each.getInputs().stream().map(JDBCExecutionUnit::getStorageResource).collect(Collectors.toList()));
        }
        replay();
    }
    
    private JDBCExecutorCallback<Boolean> createExecuteCallback(final ExecuteCallback executeCallback, final SQLStatement sqlStatement) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getStorageTypes(), sqlStatement, isExceptionThrown, eventBusContext) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return executeCallback.execute(sql, statement);
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement1, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private void replay() throws SQLException {
        for (Statement each : statements) {
            getMethodInvocationRecorder().replay(each);
        }
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        if (null != trafficInstanceId) {
            return executor.getTrafficExecutor().getResultSet();
        }
        if (null != deciderContext && deciderContext.isUseSQLFederation()) {
            return executor.getFederationExecutor().getResultSet();
        }
        if (executionContext.getSqlStatementContext() instanceof SelectStatementContext || executionContext.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            List<ResultSet> resultSets = getResultSets();
            MergedResult mergedResult = mergeQuery(getQueryResults(resultSets));
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, executionContext);
        }
        return currentResultSet;
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        List<ResultSet> result = new ArrayList<>(statements.size());
        for (Statement each : statements) {
            result.add(each.getResultSet());
        }
        return result;
    }
    
    private List<QueryResult> getQueryResults(final List<ResultSet> resultSets) throws SQLException {
        List<QueryResult> result = new ArrayList<>(resultSets.size());
        for (ResultSet each : resultSets) {
            if (null != each) {
                result.add(new JDBCStreamQueryResult(each));
            }
        }
        return result;
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()),
                metaDataContexts.getMetaData().getProps(), connection.getConnectionContext());
        return mergeEngine.merge(queryResults, executionContext.getSqlStatementContext());
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetType() {
        return statementOption.getResultSetType();
    }
    
    @SuppressWarnings("MagicConstant")
    @Override
    public int getResultSetConcurrency() {
        return statementOption.getResultSetConcurrency();
    }
    
    @Override
    public int getResultSetHoldability() {
        return statementOption.getResultSetHoldability();
    }
    
    @Override
    public boolean isAccumulate() {
        return metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules().stream()
                .anyMatch(each -> each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each)
                        .isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames()));
    }
    
    @Override
    public Collection<Statement> getRoutedStatements() {
        return statements;
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey();
        if (returnGeneratedKeys && generatedKey.isPresent() && !generatedKey.get().getGeneratedValues().isEmpty()) {
            return new GeneratedKeysResultSet(generatedKey.get().getColumnName(), generatedKey.get().getGeneratedValues().iterator(), this);
        }
        Collection<Comparable<?>> generatedValues = new LinkedList<>();
        for (Statement each : statements) {
            ResultSet resultSet = each.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        String columnName = generatedKey.map(GeneratedKeyContext::getColumnName).orElse(null);
        return new GeneratedKeysResultSet(columnName, generatedValues.iterator(), this);
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey() {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext()
                : Optional.empty();
    }
}
