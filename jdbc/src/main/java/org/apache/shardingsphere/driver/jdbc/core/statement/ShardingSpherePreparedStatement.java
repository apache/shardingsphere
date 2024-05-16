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
import com.sphereex.dbplusengine.SphereEx;
import com.sphereex.dbplusengine.SphereEx.Type;
import com.sphereex.dbplusengine.authority.model.subject.RoleSubject;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.decide.DDLConsistencyDeciderEngine;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.engine.DDLExecutionEngine;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.engine.DDLExecutionEngineFactory;
import com.sphereex.dbplusengine.data.pipeline.scenario.ddl.enums.DDLExecutionTypeEnum;
import com.sphereex.dbplusengine.global.index.execute.GlobalIndexExecutorContext;
import com.sphereex.dbplusengine.global.index.rewrite.context.GlobalIndexRewriterContext;
import com.sphereex.dbplusengine.infra.connection.execute.BatchConnectionContext;
import lombok.AccessLevel;
import lombok.Getter;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.driver.executor.DriverExecutor;
import org.apache.shardingsphere.driver.executor.batch.BatchExecutionUnit;
import org.apache.shardingsphere.driver.executor.batch.BatchPreparedStatementExecutor;
import org.apache.shardingsphere.driver.executor.callback.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.adapter.AbstractPreparedStatementAdapter;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.GeneratedKeysResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetUtils;
import org.apache.shardingsphere.driver.jdbc.core.statement.metadata.ShardingSphereParameterMetaData;
import org.apache.shardingsphere.infra.annotation.HighFrequencyInvocation;
import org.apache.shardingsphere.infra.binder.context.aware.ParameterAware;
import org.apache.shardingsphere.infra.binder.context.segment.insert.keygen.GeneratedKeyContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.config.props.temporary.TemporaryConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.type.MySQLDatabaseType;
import org.apache.shardingsphere.infra.exception.core.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.dialect.SQLExceptionTransformEngine;
import org.apache.shardingsphere.infra.exception.kernel.syntax.EmptySQLException;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.hint.SQLHintUtils;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.instance.metadata.InstanceType;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.parser.SQLParserEngine;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.rule.attribute.resoure.StorageConnectorReusableRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.parser.rule.SQLParserRule;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dal.DALStatement;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.traffic.engine.TrafficEngine;
import org.apache.shardingsphere.traffic.exception.EmptyTrafficExecutionUnitException;
import org.apache.shardingsphere.traffic.rule.TrafficRule;
import org.apache.shardingsphere.transaction.util.AutoCommitUtils;

import java.sql.Connection;
import java.sql.ParameterMetaData;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * ShardingSphere prepared statement.
 */
// SPEX CHANGED: BEGIN
@Getter
// SPEX CHANGED: END
@HighFrequencyInvocation
public final class ShardingSpherePreparedStatement extends AbstractPreparedStatementAdapter {
    
    private final ShardingSphereConnection connection;
    
    private final MetaDataContexts metaDataContexts;
    
    private final String sql;
    
    private final List<PreparedStatement> statements;
    
    private final List<List<Object>> parameterSets;
    
    private final SQLStatement sqlStatement;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final String databaseName;
    
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
    
    @Getter
    private final boolean selectContainsEnhancedTable;
    
    private ExecutionContext executionContext;
    
    private Map<String, Integer> columnLabelAndIndexMap;
    
    private ResultSet currentResultSet;
    
    private String trafficInstanceId;
    
    private boolean useFederation;
    
    private final HintValueContext hintValueContext;
    
    private ResultSet currentBatchGeneratedKeysResultSet;
    
    @SphereEx
    private final Grantee grantee;
    
    @SphereEx
    private final BatchConnectionContext batchContext;
    
    @SphereEx
    private final Map<GlobalIndexRewriterContext, BatchPreparedStatementExecutor> globalIndexBatchExecutors = new LinkedHashMap<>();
    
    @SphereEx
    private final Map<GlobalIndexRewriterContext, List<Integer>> globalIndexLogicalAddBatchCallTimes = new LinkedHashMap<>();
    
    @SphereEx
    private Integer globalIndexBatchCount = 0;
    
    @SphereEx(Type.MODIFY)
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, @SphereEx final Grantee grantee) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null, grantee);
    }
    
    @SphereEx(Type.MODIFY)
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql,
                                           final int resultSetType, final int resultSetConcurrency, @SphereEx final Grantee grantee) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, ResultSet.HOLD_CURSORS_OVER_COMMIT, false, null, grantee);
    }
    
    @SphereEx(Type.MODIFY)
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int autoGeneratedKeys, @SphereEx final Grantee grantee) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, Statement.RETURN_GENERATED_KEYS == autoGeneratedKeys, null, grantee);
    }
    
    @SphereEx(Type.MODIFY)
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final String[] columns, @SphereEx final Grantee grantee) throws SQLException {
        this(connection, sql, ResultSet.TYPE_FORWARD_ONLY, ResultSet.CONCUR_READ_ONLY, ResultSet.HOLD_CURSORS_OVER_COMMIT, true, columns, grantee);
    }
    
    @SphereEx(Type.MODIFY)
    public ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql, final int resultSetType, final int resultSetConcurrency,
                                           final int resultSetHoldability, @SphereEx final Grantee grantee) throws SQLException {
        this(connection, sql, resultSetType, resultSetConcurrency, resultSetHoldability, false, null, grantee);
    }
    
    @SphereEx(Type.MODIFY)
    private ShardingSpherePreparedStatement(final ShardingSphereConnection connection, final String sql,
                                            final int resultSetType, final int resultSetConcurrency, final int resultSetHoldability, final boolean returnGeneratedKeys,
                                            final String[] columns, @SphereEx final Grantee grantee) throws SQLException {
        if (Strings.isNullOrEmpty(sql)) {
            throw new EmptySQLException().toSQLException();
        }
        this.connection = connection;
        metaDataContexts = connection.getContextManager().getMetaDataContexts();
        hintValueContext = SQLHintUtils.extractHint(sql);
        this.sql = SQLHintUtils.removeHint(sql);
        // SPEX ADDED: BEGIN
        this.grantee = grantee;
        batchContext = new BatchConnectionContext();
        setMaskQueryParameterToConnectionContext();
        // SPEX ADDED: END
        statements = new ArrayList<>();
        parameterSets = new ArrayList<>();
        SQLParserRule sqlParserRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(SQLParserRule.class);
        SQLParserEngine sqlParserEngine = sqlParserRule.getSQLParserEngine(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType());
        sqlStatement = sqlParserEngine.parse(this.sql, true);
        sqlStatementContext = new SQLBindEngine(metaDataContexts.getMetaData(), connection.getDatabaseName(), hintValueContext).bind(sqlStatement, Collections.emptyList());
        databaseName = sqlStatementContext.getTablesContext().getDatabaseName().orElse(connection.getDatabaseName());
        connection.getDatabaseConnectionManager().getConnectionContext().setCurrentDatabase(databaseName);
        parameterMetaData = new ShardingSphereParameterMetaData(sqlStatement);
        statementOption = returnGeneratedKeys ? new StatementOption(true, columns) : new StatementOption(resultSetType, resultSetConcurrency, resultSetHoldability);
        executor = new DriverExecutor(connection);
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        // SPEX CHANGED: BEGIN
        batchPreparedStatementExecutor = new BatchPreparedStatementExecutor(metaDataContexts, jdbcExecutor, databaseName, connection.getProcessId(), batchContext);
        // SPEX CHANGED: END
        kernelProcessor = new KernelProcessor();
        statementsCacheable = isStatementsCacheable(metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData());
        trafficRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(TrafficRule.class);
        selectContainsEnhancedTable = sqlStatementContext instanceof SelectStatementContext && ((SelectStatementContext) sqlStatementContext).isContainsEnhancedTable();
        statementManager = new StatementManager();
    }
    
    @SphereEx
    private void setMaskQueryParameterToConnectionContext() {
        connection.getDatabaseConnectionManager().getConnectionContext().setGrantee(grantee);
        AuthorityRule authorityRule = metaDataContexts.getMetaData().getGlobalRuleMetaData().getSingleRule(AuthorityRule.class);
        connection.getDatabaseConnectionManager().getConnectionContext().setRoles(authorityRule.findRoles(grantee).stream().map(RoleSubject::toString).collect(Collectors.toList()));
        connection.getDatabaseConnectionManager().getConnectionContext().setHintValueContext(hintValueContext);
    }
    
    private boolean isStatementsCacheable(final RuleMetaData databaseRuleMetaData) {
        return databaseRuleMetaData.getAttributes(StorageConnectorReusableRuleAttribute.class).size() == databaseRuleMetaData.getRules().size() && !HintManager.isInstantiated();
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
            handleAutoCommit(queryContext);
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).executeQuery());
            }
            // SPEX ADDED: BEGIN
            if (executor.getSystemSchemaQueryEngine().decide(queryContext.getSqlStatementContext())) {
                return executeSystemSchemaQuery(queryContext);
            }
            // SPEX ADDED: END
            useFederation = decide(queryContext,
                    metaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getGlobalRuleMetaData());
            if (useFederation) {
                return executeFederationQuery(queryContext);
            }
            // SPEX ADDED: BEGIN
            if (executor.getGlobalIndexEngine().isContainsGlobalIndex(queryContext)) {
                return executeGlobalIndexQuery(queryContext);
            }
            // SPEX ADDED: END
            executionContext = createExecutionContext(queryContext);
            result = doExecuteQuery(executionContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            clearBatch();
        }
        currentResultSet = result;
        return result;
    }
    
    @SphereEx
    private ResultSet executeSystemSchemaQuery(final QueryContext queryContext) throws SQLException {
        return executor.getSystemSchemaQueryEngine().execute(queryContext, this, connection.getDatabaseName());
    }
    
    private ShardingSphereResultSet doExecuteQuery(final ExecutionContext executionContext) throws SQLException {
        List<QueryResult> queryResults = executeQuery0(executionContext);
        MergedResult mergedResult = mergeQuery(queryResults, executionContext.getSqlStatementContext());
        List<ResultSet> resultSets = getResultSets();
        if (null == columnLabelAndIndexMap) {
            columnLabelAndIndexMap = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(sqlStatementContext, selectContainsEnhancedTable, resultSets.get(0).getMetaData());
        }
        return new ShardingSphereResultSet(resultSets, mergedResult, this, selectContainsEnhancedTable, executionContext, columnLabelAndIndexMap);
    }
    
    private boolean decide(final QueryContext queryContext, final ShardingSphereDatabase database, final RuleMetaData globalRuleMetaData) {
        return executor.getSqlFederationEngine().decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, globalRuleMetaData);
    }
    
    private void handleAutoCommit(final QueryContext queryContext) throws SQLException {
        if (AutoCommitUtils.needOpenTransaction(queryContext.getSqlStatementContext().getSqlStatement())) {
            connection.handleAutoCommit();
        }
    }
    
    private JDBCExecutionUnit createTrafficExecutionUnit(final String trafficInstanceId, final QueryContext queryContext) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        ExecutionGroupContext<JDBCExecutionUnit> context =
                prepareEngine.prepare(new RouteContext(), Collections.singleton(executionUnit), new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", "")));
        ShardingSpherePreconditions.checkState(!context.getInputGroups().isEmpty() && !context.getInputGroups().iterator().next().getInputs().isEmpty(), EmptyTrafficExecutionUnitException::new);
        return context.getInputGroups().iterator().next().getInputs().iterator().next();
    }
    
    private Optional<String> getInstanceIdAndSet(final QueryContext queryContext) {
        Optional<String> result = connection.getDatabaseConnectionManager().getConnectionContext().getTrafficInstanceId();
        if (!result.isPresent()) {
            result = getInstanceId(queryContext);
        }
        if (connection.isHoldTransaction() && result.isPresent()) {
            connection.getDatabaseConnectionManager().getConnectionContext().setTrafficInstanceId(result.get());
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
    
    private List<QueryResult> executeQuery0(final ExecutionContext executionContext) throws SQLException {
        if (hasRawExecutionRule()) {
            return executor.getRawExecutor().execute(createRawExecutionGroupContext(executionContext),
                    executionContext.getQueryContext(), new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        return executor.getRegularExecutor().executeQuery(executionGroupContext, executionContext.getQueryContext(),
                new PreparedStatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                        metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement,
                        SQLExecutorExceptionHandler.isExceptionThrown()));
    }
    
    private ResultSet executeFederationQuery(final QueryContext queryContext) {
        PreparedStatementExecuteQueryCallback callback = new PreparedStatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown());
        SQLFederationContext context = new SQLFederationContext(false, queryContext, metaDataContexts.getMetaData(), connection.getProcessId());
        return executor.getSqlFederationEngine().executeQuery(createDriverExecutionPrepareEngine(), callback, context);
    }
    
    private DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> createDriverExecutionPrepareEngine() {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, maxConnectionsSizePerQuery, connection.getDatabaseConnectionManager(), statementManager,
                statementOption, metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits());
    }
    
    @SphereEx
    private ResultSet executeGlobalIndexQuery(final QueryContext queryContext) throws SQLException {
        PreparedStatementExecuteQueryCallback callback = new PreparedStatementExecuteQueryCallback(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement, SQLExecutorExceptionHandler.isExceptionThrown());
        GlobalIndexExecutorContext executorContext = new GlobalIndexExecutorContext(connection.getProcessId());
        List<QueryResult> queryResults = executor.getGlobalIndexEngine().executeGlobalIndex(createDriverExecutionPrepareEngine(), callback,
                connection.getDatabaseConnectionManager().getConnectionContext(), queryContext, executorContext);
        statements.addAll(executorContext.getCachedPreparedStatements());
        executionContext = executorContext.getPrimarySQLExecutionContext().orElseThrow(() -> new IllegalStateException("Can not get global index primary sql execution context."));
        MergedResult mergedResult = mergeQuery(queryResults, executionContext.getSqlStatementContext());
        List<ResultSet> resultSets = getResultSets();
        if (null == columnLabelAndIndexMap) {
            columnLabelAndIndexMap = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(sqlStatementContext, selectContainsEnhancedTable, resultSets.get(0).getMetaData());
        }
        return new ShardingSphereResultSet(resultSets, mergedResult, this, selectContainsEnhancedTable, executionContext, columnLabelAndIndexMap);
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
            handleAutoCommit(queryContext);
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).executeUpdate());
            }
            // SPEX ADDED: BEGIN
            if (executor.getGlobalIndexEngine().isContainsGlobalIndex(queryContext)) {
                return isNeedImplicitCommitTransaction(connection, sqlStatement, true) ? executeWithImplicitCommitTransaction(() -> executeGlobalIndexUpdate(queryContext), connection,
                        metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                        : executeGlobalIndexUpdate(queryContext);
            }
            // SPEX ADDED: END
            executionContext = createExecutionContext(queryContext);
            // SPEX ADDED: BEGIN
            if (new DDLConsistencyDeciderEngine().isDDLSQLStatement(connection.getContextManager(), executionContext, connection.getDatabaseName())) {
                return executeDDLSQLStatementUpdate(executionContext);
            }
            // SPEX ADDED: END
            if (hasRawExecutionRule()) {
                Collection<ExecuteResult> results =
                        executor.getRawExecutor().execute(createRawExecutionGroupContext(executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback());
                return accumulate(results);
            }
            return executeUpdateWithExecutionContext(executionContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    @SphereEx
    private int executeGlobalIndexUpdate(final QueryContext queryContext) throws SQLException {
        GlobalIndexExecutorContext executorContext = new GlobalIndexExecutorContext(connection.getProcessId());
        int result = executor.getGlobalIndexEngine().executeUpdate(createDriverExecutionPrepareEngine(), createExecuteCallback(),
                connection.getDatabaseConnectionManager().getConnectionContext(), queryContext, executorContext);
        statements.addAll(executorContext.getCachedPreparedStatements());
        executionContext = executorContext.getPrimarySQLExecutionContext().orElseThrow(() -> new IllegalStateException("Can not get global index primary sql execution context."));
        return result;
    }
    
    @SphereEx(Type.MODIFY)
    private int useDriverToExecuteUpdate(final ExecutionContext executionContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        return executor.getRegularExecutor().executeUpdate(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), createExecuteUpdateCallback());
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
        return new JDBCExecutorCallback<Integer>(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
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
            handleAutoCommit(queryContext);
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            if (null != trafficInstanceId) {
                JDBCExecutionUnit executionUnit = createTrafficExecutionUnit(trafficInstanceId, queryContext);
                return executor.getTrafficExecutor().execute(executionUnit, (statement, sql) -> ((PreparedStatement) statement).execute());
            }
            // SPEX ADDED: BEGIN
            if (executor.getSystemSchemaQueryEngine().decide(queryContext.getSqlStatementContext())) {
                ResultSet resultSet = executeSystemSchemaQuery(queryContext);
                return null != resultSet;
            }
            // SPEX ADDED: END
            useFederation = decide(queryContext,
                    metaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getGlobalRuleMetaData());
            if (useFederation) {
                ResultSet resultSet = executeFederationQuery(queryContext);
                return null != resultSet;
            }
            // SPEX ADDED: BEGIN
            if (executor.getGlobalIndexEngine().isContainsGlobalIndex(queryContext)) {
                return isNeedImplicitCommitTransaction(connection, sqlStatement, true) ? executeWithImplicitCommitTransaction(() -> executeGlobalIndex(queryContext), connection,
                        metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                        : executeGlobalIndex(queryContext);
            }
            // SPEX ADDED: END
            executionContext = createExecutionContext(queryContext);
            // SPEX ADDED: BEGIN
            if (new DDLConsistencyDeciderEngine().isDDLSQLStatement(connection.getContextManager(), executionContext, connection.getDatabaseName())) {
                return executeDDLSQLStatement(executionContext);
            }
            // SPEX ADDED: END
            if (hasRawExecutionRule()) {
                Collection<ExecuteResult> results =
                        executor.getRawExecutor().execute(createRawExecutionGroupContext(executionContext), executionContext.getQueryContext(), new RawSQLExecutorCallback());
                return results.iterator().next() instanceof QueryResult;
            }
            return executeWithExecutionContext(executionContext);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    @SphereEx
    private boolean executeGlobalIndex(final QueryContext queryContext) throws SQLException {
        GlobalIndexExecutorContext executorContext = new GlobalIndexExecutorContext(connection.getProcessId());
        boolean result = executor.getGlobalIndexEngine().execute(createDriverExecutionPrepareEngine(), createExecuteCallback(), connection.getDatabaseConnectionManager().getConnectionContext(),
                queryContext, executorContext);
        statements.addAll(executorContext.getCachedPreparedStatements());
        executionContext = executorContext.getPrimarySQLExecutionContext().orElseThrow(() -> new IllegalStateException("Can not get global index primary sql execution context."));
        return result;
    }
    
    private boolean hasRawExecutionRule() {
        return !metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty();
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = metaDataContexts.getMetaData().getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", "")));
    }
    
    private boolean executeWithExecutionContext(final ExecutionContext executionContext) throws SQLException {
        return isNeedImplicitCommitTransaction(connection, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1)
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecute(executionContext), connection, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                : useDriverToExecute(executionContext);
    }
    
    private int executeUpdateWithExecutionContext(final ExecutionContext executionContext) throws SQLException {
        return isNeedImplicitCommitTransaction(connection, executionContext.getSqlStatementContext().getSqlStatement(), executionContext.getExecutionUnits().size() > 1)
                ? executeWithImplicitCommitTransaction(() -> useDriverToExecuteUpdate(executionContext), connection, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType())
                : useDriverToExecuteUpdate(executionContext);
    }
    
    @SphereEx(Type.MODIFY)
    private boolean useDriverToExecute(final ExecutionContext executionContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = createExecutionGroupContext(executionContext);
        cacheStatements(executionGroupContext.getInputGroups());
        return executor.getRegularExecutor().execute(executionGroupContext,
                executionContext.getQueryContext(), executionContext.getRouteContext().getRouteUnits(), createExecuteCallback());
    }
    
    @SphereEx
    private boolean executeDDLSQLStatement(final ExecutionContext executionContext) throws SQLException {
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        JDBCExecutorCallback<Boolean> jdbcExecutorCallback = createExecuteCallback();
        DDLExecutionEngine ddlEngine = DDLExecutionEngineFactory.newInstance(executionContext.getQueryContext(), metaDataContexts, connection.getDatabaseName(),
                connection.getContextManager().getInstanceContext(), connection.getProcessId(), jdbcExecutor, connection.getDatabaseConnectionManager().getConnectionContext());
        return ddlEngine.executeDDLSQLStatement(metaDataContexts, executionContext, DDLExecutionTypeEnum.JDBC_EXECUTE,
                connection.getDatabaseConnectionManager().getConnectionContext().getExecutionConnectionContext(), InstanceType.JDBC, createDriverExecutionPrepareEngine(), jdbcExecutorCallback);
    }
    
    @SphereEx
    private int executeDDLSQLStatementUpdate(final ExecutionContext executionContext) throws SQLException {
        JDBCExecutor jdbcExecutor = new JDBCExecutor(connection.getContextManager().getExecutorEngine(), connection.getDatabaseConnectionManager().getConnectionContext());
        JDBCExecutorCallback<Integer> callback = createExecuteUpdateCallback();
        DDLExecutionEngine ddlEngine = DDLExecutionEngineFactory.newInstance(executionContext.getQueryContext(), metaDataContexts, connection.getDatabaseName(),
                connection.getContextManager().getInstanceContext(), connection.getProcessId(), jdbcExecutor, connection.getDatabaseConnectionManager().getConnectionContext());
        return ddlEngine.executeDDLSQLStatement(metaDataContexts, executionContext, DDLExecutionTypeEnum.JDBC_EXECUTE_UPDATE,
                connection.getDatabaseConnectionManager().getConnectionContext().getExecutionConnectionContext(), InstanceType.JDBC, createDriverExecutionPrepareEngine(), callback);
    }
    
    @SphereEx(Type.MODIFY)
    private JDBCExecutorCallback<Boolean> createExecuteCallback() {
        return createExecuteCallback(sqlStatement);
    }
    
    @SphereEx
    private JDBCExecutorCallback<Boolean> createExecuteCallback(final SQLStatement sqlStatement) {
        boolean isExceptionThrown = SQLExecutorExceptionHandler.isExceptionThrown();
        return new JDBCExecutorCallback<Boolean>(metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData(), sqlStatement, isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode, final DatabaseType storageType) throws SQLException {
                return ((PreparedStatement) statement).execute();
            }
            
            @Override
            protected Optional<Boolean> getSaneResult(final SQLStatement sqlStatement, final SQLException ex) {
                return Optional.empty();
            }
        };
    }
    
    private ExecutionGroupContext<JDBCExecutionUnit> createExecutionGroupContext(final ExecutionContext executionContext) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = createDriverExecutionPrepareEngine();
        return prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", "")));
    }
    
    @Override
    public ResultSet getResultSet() throws SQLException {
        if (null != currentResultSet) {
            return currentResultSet;
        }
        if (null != trafficInstanceId) {
            return executor.getTrafficExecutor().getResultSet();
        }
        if (useFederation) {
            return executor.getSqlFederationEngine().getResultSet();
        }
        if (executionContext.getSqlStatementContext() instanceof SelectStatementContext
                || executionContext.getSqlStatementContext().getSqlStatement() instanceof DALStatement) {
            List<ResultSet> resultSets = getResultSets();
            if (resultSets.isEmpty()) {
                return currentResultSet;
            }
            SQLStatementContext sqlStatementContext = executionContext.getSqlStatementContext();
            MergedResult mergedResult = mergeQuery(getQueryResults(resultSets), sqlStatementContext);
            if (null == columnLabelAndIndexMap) {
                columnLabelAndIndexMap = ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(sqlStatementContext, selectContainsEnhancedTable, resultSets.get(0).getMetaData());
            }
            currentResultSet = new ShardingSphereResultSet(resultSets, mergedResult, this, selectContainsEnhancedTable, executionContext, columnLabelAndIndexMap);
        }
        return currentResultSet;
    }
    
    private List<ResultSet> getResultSets() throws SQLException {
        List<ResultSet> result = new ArrayList<>(statements.size());
        for (Statement each : statements) {
            if (null != each.getResultSet()) {
                result.add(each.getResultSet());
            }
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
        RuleMetaData globalRuleMetaData = metaDataContexts.getMetaData().getGlobalRuleMetaData();
        ShardingSphereDatabase currentDatabase = metaDataContexts.getMetaData().getDatabase(databaseName);
        // SPEX CHANGED: BEGIN
        SQLAuditEngine.audit(queryContext.getSqlStatementContext(), queryContext.getParameters(), globalRuleMetaData, currentDatabase, grantee, queryContext.getHintValueContext(),
                queryContext.getSql());
        // SPEX CHANGED: END
        ExecutionContext result = kernelProcessor.generateExecutionContext(
                queryContext, currentDatabase, globalRuleMetaData, metaDataContexts.getMetaData().getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
        findGeneratedKey(result).ifPresent(optional -> generatedValues.addAll(optional.getGeneratedValues()));
        return result;
    }
    
    private ExecutionContext createExecutionContext(final QueryContext queryContext, final String trafficInstanceId) {
        ExecutionUnit executionUnit = new ExecutionUnit(trafficInstanceId, new SQLUnit(queryContext.getSql(), queryContext.getParameters()));
        return new ExecutionContext(queryContext, Collections.singletonList(executionUnit), new RouteContext());
    }
    
    @SphereEx(Type.MODIFY)
    private QueryContext createQueryContext() {
        return createQueryContext(false);
    }
    
    @SphereEx
    private QueryContext createQueryContext(final boolean executeBatch) {
        List<Object> parameters = executeBatch && isSupportBatchPerformance() ? batchPreparedStatementExecutor.getParameters() : getParameters();
        List<Object> params = new ArrayList<>(parameters);
        if (sqlStatementContext instanceof ParameterAware) {
            ((ParameterAware) sqlStatementContext).setUpParameters(params);
        }
        return new QueryContext(sqlStatementContext, sql, params, hintValueContext, true);
    }
    
    private MergedResult mergeQuery(final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(metaDataContexts.getMetaData().getGlobalRuleMetaData(), metaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContexts.getMetaData().getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
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
        if (null != currentBatchGeneratedKeysResultSet) {
            return currentBatchGeneratedKeysResultSet;
        }
        Optional<GeneratedKeyContext> generatedKey = findGeneratedKey(executionContext);
        if (generatedKey.isPresent() && statementOption.isReturnGeneratedKeys() && !generatedValues.isEmpty()) {
            return new GeneratedKeysResultSet(getGeneratedKeysColumnName(generatedKey.get().getColumnName()), generatedValues.iterator(), this);
        }
        for (PreparedStatement each : statements) {
            ResultSet resultSet = each.getGeneratedKeys();
            while (resultSet.next()) {
                generatedValues.add((Comparable<?>) resultSet.getObject(1));
            }
        }
        String columnName = generatedKey.map(GeneratedKeyContext::getColumnName).orElse(null);
        return new GeneratedKeysResultSet(getGeneratedKeysColumnName(columnName), generatedValues.iterator(), this);
    }
    
    private String getGeneratedKeysColumnName(final String columnName) {
        return metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType() instanceof MySQLDatabaseType ? "GENERATED_KEY" : columnName;
    }
    
    @Override
    public void addBatch() throws @SphereEx SQLException {
        try {
            // SPEX ADDED: BEGIN
            connection.getDatabaseConnectionManager().getConnectionContext().setBatchContext(batchContext);
            if (isSupportBatchPerformance()) {
                batchPreparedStatementExecutor.addParameters(getParameters());
                return;
            }
            // SPEX ADDED: END
            QueryContext queryContext = createQueryContext();
            trafficInstanceId = getInstanceIdAndSet(queryContext).orElse(null);
            // SPEX ADDED: BEGIN
            if (executor.getGlobalIndexEngine().isContainsGlobalIndex(queryContext)) {
                addBatchForGlobalIndex(queryContext);
                return;
            }
            // SPEX ADDED: END
            executionContext = null == trafficInstanceId ? createExecutionContext(queryContext) : createExecutionContext(queryContext, trafficInstanceId);
            batchPreparedStatementExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
        } finally {
            currentResultSet = null;
            clearParameters();
            // SPEX ADDED: BEGIN
            connection.getDatabaseConnectionManager().getConnectionContext().resetBatchContext();
            // SPEX ADDED: END
        }
    }
    
    @SphereEx
    private void addBatchForGlobalIndex(final QueryContext queryContext) throws SQLException {
        Collection<GlobalIndexRewriterContext> executionContexts = executor.getGlobalIndexEngine().addBatch(queryContext, connection.getDatabaseConnectionManager().getConnectionContext(),
                createDriverExecutionPrepareEngine(), createExecuteCallback(), new GlobalIndexExecutorContext(connection.getProcessId()));
        for (GlobalIndexRewriterContext each : executionContexts) {
            ShardingSpherePreconditions.checkState(each.getExecutionContext().isPresent(), () -> new UnsupportedOperationException("Execution context is required"));
            globalIndexBatchExecutors
                    .computeIfAbsent(each,
                            unused -> new BatchPreparedStatementExecutor(metaDataContexts, batchPreparedStatementExecutor.getJdbcExecutor(), databaseName, connection.getProcessId(), batchContext))
                    .addBatchForExecutionUnits(each.getExecutionContext().get().getExecutionUnits());
            if (each.isPrimarySQL()) {
                executionContext = each.getExecutionContext().get();
                globalIndexLogicalAddBatchCallTimes.computeIfAbsent(each, unused -> new LinkedList<>()).add(globalIndexBatchCount);
            }
        }
        globalIndexBatchCount++;
    }
    
    @Override
    public int[] executeBatch() throws SQLException {
        // SPEX ADDED: BEGIN
        connection.getDatabaseConnectionManager().getConnectionContext().setBatchContext(batchContext);
        if (isSupportBatchPerformance()) {
            if (0 == batchPreparedStatementExecutor.getBatchCount()) {
                return new int[0];
            }
            QueryContext queryContext = createQueryContext(true);
            batchContext.setOneBatchValuesSize(((InsertStatementContext) queryContext.getSqlStatementContext()).getValueListCount());
            executionContext = createExecutionContext(queryContext);
            batchPreparedStatementExecutor.addBatchForExecutionUnits(executionContext.getExecutionUnits());
        }
        // SPEX ADDED: END
        if (null == executionContext) {
            return new int[0];
        }
        // SPEX ADDED: BEGIN
        if (executor.getGlobalIndexEngine().isContainsGlobalIndex(executionContext.getQueryContext())) {
            int[] results = new int[globalIndexBatchCount];
            for (Entry<GlobalIndexRewriterContext, BatchPreparedStatementExecutor> entry : globalIndexBatchExecutors.entrySet()) {
                int[] eachResult = doExecuteBatch(entry.getValue());
                if (entry.getKey().isPrimarySQL()) {
                    List<Integer> logicalCallTimes = globalIndexLogicalAddBatchCallTimes.get(entry.getKey());
                    for (int i = 0; i < logicalCallTimes.size(); i++) {
                        Integer logicalCallTime = logicalCallTimes.get(i);
                        results[logicalCallTime] += eachResult[i];
                    }
                }
            }
            return results;
        }
        // SPEX ADDED: END
        try {
            // TODO add raw SQL executor
            return doExecuteBatch(batchPreparedStatementExecutor);
            // CHECKSTYLE:OFF
        } catch (final RuntimeException ex) {
            // CHECKSTYLE:ON
            handleExceptionInTransaction(connection, metaDataContexts);
            throw SQLExceptionTransformEngine.toSQLException(ex, metaDataContexts.getMetaData().getDatabase(databaseName).getProtocolType());
        } finally {
            clearBatch();
        }
    }
    
    private int[] doExecuteBatch(final BatchPreparedStatementExecutor batchExecutor) throws SQLException {
        initBatchPreparedStatementExecutor(batchExecutor);
        int[] result = batchExecutor.executeBatch(executionContext.getSqlStatementContext());
        if (statementOption.isReturnGeneratedKeys() && generatedValues.isEmpty()) {
            List<Statement> batchPreparedStatementExecutorStatements = batchExecutor.getStatements();
            for (Statement statement : batchPreparedStatementExecutorStatements) {
                statements.add((PreparedStatement) statement);
            }
            currentBatchGeneratedKeysResultSet = getGeneratedKeys();
            statements.clear();
        }
        return result;
    }
    
    private void initBatchPreparedStatementExecutor(final BatchPreparedStatementExecutor batchExecutor) throws SQLException {
        DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine = new DriverExecutionPrepareEngine<>(JDBCDriverType.PREPARED_STATEMENT, metaDataContexts.getMetaData().getProps()
                .<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY), connection.getDatabaseConnectionManager(), statementManager, statementOption,
                metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules(),
                metaDataContexts.getMetaData().getDatabase(databaseName).getResourceMetaData().getStorageUnits());
        List<ExecutionUnit> executionUnits = new ArrayList<>(batchExecutor.getBatchExecutionUnits().size());
        for (BatchExecutionUnit each : batchExecutor.getBatchExecutionUnits()) {
            ExecutionUnit executionUnit = each.getExecutionUnit();
            executionUnits.add(executionUnit);
        }
        batchExecutor.init(prepareEngine.prepare(executionContext.getRouteContext(), executionUnits, new ExecutionGroupReportContext(connection.getProcessId(), databaseName, new Grantee("", ""))));
        setBatchParametersForStatements(batchExecutor);
    }
    
    private void setBatchParametersForStatements(final BatchPreparedStatementExecutor batchExecutor) throws SQLException {
        for (Statement each : batchExecutor.getStatements()) {
            List<List<Object>> paramSet = batchExecutor.getParameterSet(each);
            for (List<Object> eachParams : paramSet) {
                replaySetParameter((PreparedStatement) each, eachParams);
                ((PreparedStatement) each).addBatch();
            }
        }
    }
    
    @Override
    public void clearBatch() {
        // SPEX ADDED: BEGIN
        if (null != executionContext && executor.getGlobalIndexEngine().isContainsGlobalIndex(executionContext.getQueryContext())) {
            globalIndexBatchExecutors.forEach((key, value) -> value.clear());
            globalIndexBatchExecutors.clear();
            globalIndexLogicalAddBatchCallTimes.clear();
            globalIndexBatchCount = 0;
        }
        // SPEX ADDED: END
        currentResultSet = null;
        batchPreparedStatementExecutor.clear();
        clearParameters();
        batchContext.close();
        // SPEX ADDED: BEGIN
        connection.getDatabaseConnectionManager().getConnectionContext().resetBatchContext();
        // SPEX ADDED: END
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
        for (DataNodeRuleAttribute each : metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getAttributes(DataNodeRuleAttribute.class)) {
            if (each.isNeedAccumulate(executionContext.getSqlStatementContext().getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    @Override
    public Collection<PreparedStatement> getRoutedStatements() {
        return statements;
    }
    
    /**
     * Judge whether support batch performance or not. 
     *
     * @return support batch performance or not
     */
    @SphereEx
    public boolean isSupportBatchPerformance() {
        if (null == batchContext.getSupportBatchPerformance()) {
            Boolean useBatchPerformanceConfig = metaDataContexts.getMetaData().getTemporaryProps().<Boolean>getValue(TemporaryConfigurationPropertyKey.BATCH_PERFORMANCE_ENABLED);
            batchContext.setSupportBatchPerformance(Boolean.TRUE.equals(useBatchPerformanceConfig) && batchPreparedStatementExecutor.isSupportBatchPerformance(sqlStatementContext));
        }
        return batchContext.isSupportBatchPerformance();
    }
}
