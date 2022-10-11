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
import org.apache.shardingsphere.driver.executor.batch.BatchExecutionUnit;
import org.apache.shardingsphere.driver.executor.batch.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.driver.executor.callback.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractPreparedStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetUtil;
import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.driver.jdbc.exception.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.decider.context.SQLFederationDeciderContext;
import org.apache.shardingsphere.infra.binder.decider.engine.SQLFederationDeciderEngine;
import org.apache.shardingsphere.infra.binder.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.context.kernel.KernelProcessor;
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
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
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
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.parser.ShardingSphereSQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.rule.identifier.type.RawExecutionRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StorageConnectorReusableRule;
import org.apache.shardingsphere.infra.util.eventbus.EventBusContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sqlfederation.spi.SQLFederationExecutorContext;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.exception.metadata.EmptyTrafficExecutionUnitException;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere prepared statement.
 */
public final class ShardingSpherePreparedStatement extends AbstractPreparedStatementAdapter {
    
    @Getter
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final String sql;
    
    private final List<PreparedStatement> statements;
    
    private final List<List<Object>> parameterSets;
    
    private final SQLStatement sqlStatement;
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final StatementOption statementOption;
    
    @Getter
    private final ParameterMetaData parameterMetaData;
    
    @Getter(AccessLevel.PROTECTED)
    private final DriverExecutor executor;
    
    private final BatchPreparedStatementExecutor batchPreparedStatementExecutor;
    
    private final Collection<Comparable<?>> generatedValues = new LinkedList<>();
    
    private final KernelProcessor kernelProcessor;
    
    private final boolean statementsCacheable;
    
    private final TrafficRule trafficRule;
    
    @Getter(AccessLevel.PROTECTED)
    private final StatementManager statementManager;
    
    private final EventBusContext eventBusContext;
    
    private ExecutionContext executionContext;
    
    private Map<String, Integer> columnLabelAndIndexMap;
    
    private ResultSet currentResultSet;
    
    private String trafficInstanceId;
    
    private SQLFederationDeciderContext deciderContext;
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int autoGeneratedKeys) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys, null);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final String[] columns) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, true, columns);
    }
    
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency,
                                           final int resultSetHoldability) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, false, null);
    }
    
    private ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql,
                                            final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys,
                                            final String[] columns) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new EmptySQLException().toSQLException();
        }
        this.connection = connection;
        metaDataContexts = connection.getContextManager().getMetaDataContexts();
        eventBusContext = connection.getContextManager().getInstanceContext().getEventBusContext();
        this.sql = sql;
        statements = new ArrayList<>();
        parameterSets = new ArrayList<>();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        ShardingSphereSQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(
                DatabaseTypeEngine.getTrunkDatabaseTypeName(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType()));
        sqlStatement = sqlParserEngine.parse(sql, true);
        sqlStatementContext = SQLStatementContextFactory.newInstance(metaDataContexts.getMetaData().getDatabases(), sqlStatement, connection.getDatabaseName());
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        statementOption = returnGeneratedKeys ? new StatementOption(true, columns) : new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        executor = new DriverExecutor(connection);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.isHoldTransaction());
        batchPreparedStatementExecutor = new BatchPreparedStatementExecutor(metaDataContexts, jdbcExecutor, connection.getDatabaseName(), eventBusContext);
        kernelProcessor = new KernelProcessor();
        statementsCacheable = isStatementsCacheable(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData());
        trafficRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        statementManager = new StatementManager();
    }
    
    private boolean isStatementsCacheable(final ShardingSphereRuleMetaData databaseRuleMetaData) {
        return databaseRuleMetaData.findRules(StorageConnectorReusableRule.class).size() == databaseRuleMetaData.getRules().size() && !HintManager.isInstantiated();
    }
    
    @Override
    public ResultSet executeQuery() throws SQLException {
        ResultSet result;
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().executeQuery();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).executeQuery());
            }
            deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()));
            if (deciderContext.isUseSQLFederation()) {
                return executeFederationQuery(queryContext);
            }
            executionContext = createExecutionContext(queryContext);
            List<QueryResult> queryResults = executeQuery0();
            MergedResult mergedResult = mergeQuery(queryResults);
            List<ResultSet> resultSets = getResultSets();
            Map<String, Integer> columnLabelAndIndexMap = null != this.columnLabelAndIndexMap ? this.columnLabelAndIndexMap
                    : (this.columnLabelAndIndexMap = ShardingSphereResultSetUtil.createColumnLabelAndIndexMap(sqlStatementContext, resultSets.get(0).getMetaData()));
            result = new ShardingSphereResultSet(resultSets, mergedResult, this, executionContext, columnLabelAndIndexMap);
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType().getType());
        } finally {
            clearBatch();
        }
        currentResultSet = result;
        return result;
    }
    
    private static SQLFederationDeciderContext decide(final QueryContext queryContext, final ConfigurationProperties props, final ShardingSphereDatabase database) {
        SQLFederationDeciderEngine deciderEngine = new SQLFederationDeciderEngine(database.getRuleMetaData().getRules(), props);
        return deciderEngine.decide(queryContext, database);
    }
    
    private JDBCExecutionUnit createTrafficExecutionUnit(final String trafficInstanceId, final QueryContext queryContext) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        ExecutionGroupContext<JDBCExecutionUnit> context = prepareEngine.prepare(new RouteContext(), Collections.singletonList(executionUnit));
        if (context.getInputGroups().isEmpty() || context.getInputGroups().iterator().next().getInputs().isEmpty()) {
            throw new EmptyTrafficExecutionUnitException();
        }
        return context.getInputGroups().iterator().next().getInputs().iterator().next();
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
    
    private void resetParameters() throws SQLException {
        parameterSets.clear();
        parameterSets.add(getParameters());
        replaySetParameter();
    }
    
    private List<QueryResult> executeQuery0() throws SQLException {
        if (hasRawExecutionRule()) {
            return executor.getRawExecutor().execute(createRawExecutionGroupContext(), executionContext.getQueryContext(),
                    new RawSQLExecutorCallback(eventBusContext)).stream().map(each -> (QueryResult) each).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
        cacheStatements(executionGroupContext.getInputGroups());
        return executor.getRegularExecutor().executeQuery(executionGroupContext, executionContext.getQueryContext(),
                new PreparedStatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType(),
                        sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown(), eventBusContext));
    }
    
    private ResultSet executeFederationQuery(final QueryContext queryContext) throws SQLException {
        PreparedStatementExecuteQueryCallback callback = new PreparedStatementExecuteQueryCallback(
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown(),
                eventBusContext);
        SQLFederationExecutorContext context = new SQLFederationExecutorContext(false, queryContext, metaDataContexts.getMetaData().getDatabases());
        return executor.getFederationExecutor().executeQuery(createDriverExecutionPrepareEngine(), callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine() {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery, connection.getConnectionManager(), statementManager,
                statementOption, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType());
    }
    
    @Override
    public int executeUpdate() throws SQLException {
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().executeUpdate();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).executeUpdate());
            }
            executionContext = createExecutionContext(queryContext);
            if (hasRawExecutionRule()) {
                Collection<ExecuteResult> executeResults = executor.getRawExecutor().execute(createRawExecutionGroupContext(), executionContext.getQueryContext(),
                        new RawSQLExecutorCallback(eventBusContext));
                return accumulate(executeResults);
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return executor.getRegularExecutor().executeUpdate(executionGroupContext,
                    executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), createExecuteUpdateCallback());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType().getType());
        } finally {
            clearBatch();
        }
    }
    
    private int accumulate(final Collection<ExecuteResult> results) {
        int result = 0;
        for (ExecuteResult each : results) {
            result += ((UpdateResult) each).getUpdateCount();
        }
        return result;
    }
    
    private JDBCExecutorCallback<Integer> createExecuteUpdateCallback() {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Integer>(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType(), sqlStatement, isExceptionThrown,
                eventBusContext) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).executeUpdate();
            }
            
            @Override
            protected Optional<Integer> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    @Override
    public boolean execute() throws SQLException {
        try {
            if (statementsCacheable && !statements.isEmpty()) {
                resetParameters();
                return statements.iterator().next().execute();
            }
            clearPrevious();
            QueryContext queryContext = createQueryContext();
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).execute());
            }
            deciderContext = decide(queryContext, metaDataContexts.getMetaData().getProps(), metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()));
            if (deciderContext.isUseSQLFederation()) {
                ResultSet resultSet = executeFederationQuery(queryContext);
                return null != resultSet;
            }
            executionContext = createExecutionContext(queryContext);
            if (hasRawExecutionRule()) {
                // TODO process getStatement
                Collection<ExecuteResult> executeResults = executor.getRawExecutor().execute(createRawExecutionGroupContext(), executionContext.getQueryContext(),
                        new RawSQLExecutorCallback(eventBusContext));
                return executeResults.iterator().next() instanceof QueryResult;
            }
            ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext();
            cacheStatements(executionGroupContext.getInputGroups());
            return executor.getRegularExecutor().execute(executionGroupContext,
                    executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), createExecuteCallback());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType().getType());
        } finally {
            clearBatch();
        }
    }
    
    private boolean hasRawExecutionRule() {
        for (ShardingSphereRule each : metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules()) {
            if (each instanceof RawExecutionRule) {
                return true;
            }
        }
        return false;
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext() throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext() throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits());
    }
    
    private JDBCExecutorCallback<Boolean> createExecuteCallback() {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType(), sqlStatement, isExceptionThrown,
                eventBusContext) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).execute();
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
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
            Map<String, Integer> columnLabelAndIndexMap = null != this.columnLabelAndIndexMap ? this.columnLabelAndIndexMap
                    : (this.columnLabelAndIndexMap = ShardingSphereResultSetUtil.createColumnLabelAndIndexMap(sqlStatementContext, resultSets.get(0).getMetaData()));
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, executionContext, columnLabelAndIndexMap);
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
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext) {
        SQLCheckEngine.check(queryContext.getSqlStatementContext(), queryContext.getParameters(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules(),
                connection.getDatabaseName(), metaDataContexts.getMetaData().getDatabases(), null);
        ExecutionContext result = kernelProcessor.generateExecutionContext(queryContext, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()),
                metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getProps(), connection.getConnectionContext());
        findGeneratedKey(result).ifPresent(generatedKey -> generatedValues.addAll(generatedKey.getGeneratedValues()));
        return result;
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext, final String trafficInstanceId) {
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        return new ExecutionContext(queryContext, Collections.singletonList(executionUnit), new RouteContext());
    }
    
    private QueryContext createQueryContext() {
        List<Object> parameters = new ArrayList<>(getParameters());
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(parameters);
        }
        return new QueryContext(sqlStatementContext, sql, parameters);
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()),
                metaDataContexts.getMetaData().getProps(), connection.getConnectionContext());
        return mergeEngine.merge(queryResults, executionContext.getSqlStatementContext());
    }
    
    private void cacheStatements(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups) throws SQLException {
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroups) {
            each.getInputs().forEach(eachInput -> {
                statements.add((PreparedStatement) eachInput.getStorageResource());
                parameterSets.add(eachInput.getExecutionUnit().getSqlUnit().getParameters());
            });
        }
        replay();
    }
    
    private void replay() throws SQLException {
        replaySetParameter();
        for (Statement each : statements) {
            getMethodInvocationRecorder().replay(each);
        }
    }
    
    private void replaySetParameter() throws SQLException {
        for (int i = 0; i < statements.size(); i++) {
            replaySetParameter(statements.get(i), parameterSets.get(i));
        }
    }
    
    private void clearPrevious() {
        statements.clear();
        parameterSets.clear();
        generatedValues.clear();
    }
    
    private Optional<GeneratedKeyContext> findGeneratedKey(final ExecutionContext executionContext) {
        return executionContext.getSqlStatementContext() instanceof InsertStatementContext
                ? ((InsertStatementContext) executionContext.getSqlStatementContext()).getGeneratedKeyContext()
                : Optional.empty();
    }
    
    @Override
    public ResultSet getGeneratedKeys() throws SQLException {
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey(executionContext);
        if (generatedKey.isPresent() && statementOption.isReturnGeneratedKeys() && !generatedValues.isEmpty()) {
            return new GeneratedKeysResultSet(generatedKey.get().getColumnName(), generatedValues.iterator(), this);
        }
        for (PreparedStatement statement : statements) {
            ResultSet resultSet = statement.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        String columnName = generatedKey.map(GeneratedKeyContext::getColumnName).orElse(null);
        return new GeneratedKeysResultSet(columnName, generatedValues.iterator(), this);
    }
    
    @Override
    public void addBatch() {
        try {
            QueryContext queryContext = createQueryContext();
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            executionContext = null != trafficInstanceId ? createExecutionContext(queryContext, trafficInstanceId) : createExecutionContext(queryContext);
            batchPreparedStatementExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
        } finally {
            currentResultSet = null;
            clearParameters();
        }
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        if (null == executionContext) {
            return new int[0];
        }
        try {
            // TODO add raw SQL executor
            initBatchPreparedStatementExecutor();
            return batchPreparedStatementExecutor.executeBatch(executionContext.getSqlStatementContext());
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType().getType());
        } finally {
            clearBatch();
        }
    }
    
    private void initBatchPreparedStatementExecutor() throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, metaDataContexts.getMetaData().getProps()
                .<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), connection.getConnectionManager(), statementManager, statementOption,
                metaDataContexts.getMetaData()
                        .getDatabase(connection.getDatabaseName()).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getResourceMetaData().getDatabaseType());
        List<ExecutionUnit> executionUnits = new ArrayList<>(batchPreparedStatementExecutor.getBatchExecutionUnits().size());
        for (BatchExecutionUnit each : batchPreparedStatementExecutor.getBatchExecutionUnits()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            executionUnits.add(executionUnit);
        }
        batchPreparedStatementExecutor.init(prepareEngine.prepare(executionContext.getRouteContext(), executionUnits));
        setBatchParametersForStatements();
    }
    
    private void setBatchParametersForStatements() throws SQLException {
        for (Statement each : batchPreparedStatementExecutor.getStatements()) {
            List<List<Object>> parameterSet = batchPreparedStatementExecutor.getParameterSet(each);
            for (List<Object> eachParameters : parameterSet) {
                replaySetParameter((PreparedStatement) each, eachParameters);
                ((PreparedStatement) each).addBatch();
            }
        }
    }
    
    @Override
    public void clearBatch() {
        currentResultSet = null;
        batchPreparedStatementExecutor.clear();
        clearParameters();
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
    public Collection<PreparedStatement> getRoutedStatements() {
        return statements;
    }
}
