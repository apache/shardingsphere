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

package org.apache.shardingsphere.driver.executor;

import lombok.Getter;
import org.apache.shardingsphere.driver.executor.callback.ExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetUtils;
import org.apache.shardingsphere.driver.jdbc.core.statement.StatementReplayCallback;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.database.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.SQLExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.RawSQLExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.raw.callback.RawSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.prepare.raw.RawExecutionPrepareEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.sqlfederation.engine.SQLFederationEngine;
import org.apache.shardingsphere.sqlfederation.executor.context.SQLFederationContext;
import org.apache.shardingsphere.traffic.executor.TrafficExecutor;
import org.apache.shardingsphere.traffic.executor.TrafficExecutorCallback;
import org.apache.shardingsphere.traffic.rule.TrafficRule;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Driver executor.
 */
public final class DriverExecutor implements AutoCloseable {
    
    private final ShardingSphereConnection connection;
    
    @Getter
    private final DriverJDBCExecutor regularExecutor;
    
    @Getter
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private ExecuteType executeType = ExecuteType.REGULAR;
    
    private final KernelProcessor kernelProcessor;
    
    @Getter
    private final List<Statement> statements = new ArrayList<>();
    
    @Getter
    private final List<List<Object>> parameterSets = new ArrayList<>();
    
    public DriverExecutor(final ShardingSphereConnection connection) {
        this.connection = connection;
        MetaDataContexts metaDataContexts = connection.getContextManager().getMetaDataContexts();
        ExecutorEngine executorEngine = connection.getContextManager().getExecutorEngine();
        JDBCExecutor jdbcExecutor = new JDBCExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        regularExecutor = new DriverJDBCExecutor(connection.getDatabaseName(), connection.getContextManager(), jdbcExecutor);
        rawExecutor = new RawExecutor(executorEngine, connection.getDatabaseConnectionManager().getConnectionContext());
        String schemaName = new DatabaseTypeRegistry(metaDataContexts.getMetaData().getDatabase(connection.getDatabaseName()).getProtocolType()).getDefaultSchemaName(connection.getDatabaseName());
        trafficExecutor = new TrafficExecutor();
        sqlFederationEngine = new SQLFederationEngine(connection.getDatabaseName(), schemaName, metaDataContexts.getMetaData(), metaDataContexts.getStatistics(), jdbcExecutor);
        kernelProcessor = new KernelProcessor();
    }
    
    /**
     * Execute advance query.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param statementReplayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    public ResultSet executeAdvanceQuery(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                         final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final Statement statement,
                                         final Map<String, Integer> columnLabelAndIndexMap, final StatementReplayCallback statementReplayCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, getTrafficExecutorCallback(prepareEngine));
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            return sqlFederationEngine.executeQuery(
                    prepareEngine, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
        }
        return doExecuteQuery(metaData, database, queryContext, prepareEngine, statement, columnLabelAndIndexMap, statementReplayCallback);
    }
    
    private TrafficExecutorCallback<ResultSet> getTrafficExecutorCallback(final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine) {
        return JDBCDriverType.STATEMENT.equals(prepareEngine.getType()) ? Statement::executeQuery : ((statement, sql) -> ((PreparedStatement) statement).executeQuery());
    }
    
    private ExecuteQueryCallback getExecuteQueryCallback(final ShardingSphereDatabase database, final QueryContext queryContext, final String jdbcDriverType) {
        return JDBCDriverType.STATEMENT.equals(jdbcDriverType)
                ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                : new PreparedStatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    private ShardingSphereResultSet doExecuteQuery(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                   final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final Statement statement,
                                                   final Map<String, Integer> columnLabelAndIndexMap, final StatementReplayCallback statementReplayCallback) throws SQLException {
        List<QueryResult> queryResults = executeQuery0(metaData, database, queryContext, prepareEngine, statementReplayCallback);
        MergedResult mergedResult = mergeQuery(metaData, database, queryResults, queryContext.getSqlStatementContext());
        boolean selectContainsEnhancedTable = queryContext.getSqlStatementContext() instanceof SelectStatementContext
                && ((SelectStatementContext) queryContext.getSqlStatementContext()).isContainsEnhancedTable();
        List<ResultSet> resultSets = getResultSets();
        return new ShardingSphereResultSet(resultSets, mergedResult, statement, selectContainsEnhancedTable, queryContext.getSqlStatementContext(),
                null == columnLabelAndIndexMap
                        ? ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(queryContext.getSqlStatementContext(), selectContainsEnhancedTable, resultSets.get(0).getMetaData())
                        : columnLabelAndIndexMap);
    }
    
    private List<QueryResult> executeQuery0(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                            final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                            final StatementReplayCallback statementReplayCallback) throws SQLException {
        ExecutionContext executionContext = createExecutionContext(metaData, database, queryContext);
        if (hasRawExecutionRule(database)) {
            return rawExecutor.execute(createRawExecutionGroupContext(metaData, database, executionContext),
                    queryContext, new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            statements.addAll(getStatements(each));
            if (JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType())) {
                parameterSets.addAll(getParameterSets(each));
            }
        }
        statementReplayCallback.replay(statements, parameterSets);
        return regularExecutor.executeQuery(executionGroupContext, queryContext, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()));
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
    
    private ExecutionContext createExecutionContext(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext) throws SQLException {
        clearStatements();
        RuleMetaData globalRuleMetaData = metaData.getGlobalRuleMetaData();
        SQLAuditEngine.audit(queryContext.getSqlStatementContext(), queryContext.getParameters(), globalRuleMetaData, database, null, queryContext.getHintValueContext());
        return kernelProcessor.generateExecutionContext(queryContext, database, globalRuleMetaData, metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
    }
    
    private void clearStatements() throws SQLException {
        for (Statement each : statements) {
            each.close();
        }
        statements.clear();
    }
    
    private boolean hasRawExecutionRule(final ShardingSphereDatabase database) {
        return !database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty();
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ShardingSphereMetaData metaData,
                                                                                      final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = metaData.getProps().<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules()).prepare(
                executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
    }
    
    private MergedResult mergeQuery(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database,
                                    final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
        MergeEngine mergeEngine = new MergeEngine(metaData.getGlobalRuleMetaData(), database, metaData.getProps(), connection.getDatabaseConnectionManager().getConnectionContext());
        return mergeEngine.merge(queryResults, sqlStatementContext);
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
    
    /**
     * Execute advance update.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param trafficCallback traffic callback
     * @return updated row count
     * @throws SQLException SQL exception
     */
    public Optional<Integer> executeAdvanceUpdate(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database, final QueryContext queryContext,
                                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                                  final TrafficExecutorCallback<Integer> trafficCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, trafficCallback));
        }
        return Optional.empty();
    }
    
    /**
     * Execute advance.
     *
     * @param metaData meta data
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param trafficCallback traffic callback
     * @return execute result
     * @throws SQLException SQL exception
     */
    public Optional<Boolean> executeAdvance(final ShardingSphereMetaData metaData, final ShardingSphereDatabase database,
                                            final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                            final TrafficExecutorCallback<Boolean> trafficCallback) throws SQLException {
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            executeType = ExecuteType.TRAFFIC;
            return Optional.of(trafficExecutor.execute(connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, trafficCallback));
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            executeType = ExecuteType.FEDERATION;
            ResultSet resultSet = sqlFederationEngine.executeQuery(
                    prepareEngine, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
            return Optional.of(null != resultSet);
        }
        return Optional.empty();
    }
    
    /**
     * Get advanced result set.
     *
     * @return advanced result set
     */
    public Optional<ResultSet> getAdvancedResultSet() {
        switch (executeType) {
            case TRAFFIC:
                return Optional.of(trafficExecutor.getResultSet());
            case FEDERATION:
                return Optional.of(sqlFederationEngine.getResultSet());
            default:
                return Optional.empty();
        }
    }
    
    /**
     * Clear.
     */
    public void clear() {
        statements.clear();
        parameterSets.clear();
    }
    
    @Override
    public void close() throws SQLException {
        sqlFederationEngine.close();
        trafficExecutor.close();
    }
    
    public enum ExecuteType {
        
        TRAFFIC, FEDERATION, REGULAR
    }
}
