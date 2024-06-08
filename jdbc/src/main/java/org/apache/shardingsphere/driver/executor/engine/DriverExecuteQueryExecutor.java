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
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.PreparedStatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.impl.StatementExecuteQueryCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetUtils;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.audit.SQLAuditEngine;
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
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.merge.MergeEngine;
import org.apache.shardingsphere.infra.merge.result.MergedResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.query.QueryContext;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Driver execute query executor.
 */
@RequiredArgsConstructor
public final class DriverExecuteQueryExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final ShardingSphereMetaData metaData;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final TrafficExecutor trafficExecutor;
    
    private final SQLFederationEngine sqlFederationEngine;
    
    private final Collection<Statement> statements = new LinkedList<>();
    
    /**
     * Execute query.
     *
     * @param database database
     * @param queryContext query context
     * @param prepareEngine prepare engine
     * @param statement statement
     * @param columnLabelAndIndexMap column label and index map
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return result set
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public ResultSet executeQuery(final ShardingSphereDatabase database, final QueryContext queryContext,
                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final Statement statement, final Map<String, Integer> columnLabelAndIndexMap,
                                  final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        statements.clear();
        Optional<String> trafficInstanceId = connection.getTrafficInstanceId(metaData.getGlobalRuleMetaData().getSingleRule(TrafficRule.class), queryContext);
        if (trafficInstanceId.isPresent()) {
            return trafficExecutor.execute(
                    connection.getProcessId(), database.getName(), trafficInstanceId.get(), queryContext, prepareEngine, getTrafficExecuteQueryCallback(prepareEngine.getType()));
        }
        if (sqlFederationEngine.decide(queryContext.getSqlStatementContext(), queryContext.getParameters(), database, metaData.getGlobalRuleMetaData())) {
            return sqlFederationEngine.executeQuery(
                    prepareEngine, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()), new SQLFederationContext(false, queryContext, metaData, connection.getProcessId()));
        }
        List<QueryResult> queryResults = executePushDownQuery(database, queryContext, prepareEngine, addCallback, replayCallback);
        MergedResult mergedResult = mergeQuery(database, queryResults, queryContext.getSqlStatementContext());
        boolean selectContainsEnhancedTable = queryContext.getSqlStatementContext() instanceof SelectStatementContext
                && ((SelectStatementContext) queryContext.getSqlStatementContext()).isContainsEnhancedTable();
        List<ResultSet> resultSets = getResultSets();
        return new ShardingSphereResultSet(resultSets, mergedResult, statement, selectContainsEnhancedTable, queryContext.getSqlStatementContext(),
                null == columnLabelAndIndexMap
                        ? ShardingSphereResultSetUtils.createColumnLabelAndIndexMap(queryContext.getSqlStatementContext(), selectContainsEnhancedTable, resultSets.get(0).getMetaData())
                        : columnLabelAndIndexMap);
    }
    
    private TrafficExecutorCallback<ResultSet> getTrafficExecuteQueryCallback(final String jdbcDriverType) {
        return JDBCDriverType.STATEMENT.equals(jdbcDriverType) ? ((sql, statement) -> statement.executeQuery(sql)) : ((sql, statement) -> ((PreparedStatement) statement).executeQuery());
    }
    
    private ExecuteQueryCallback getExecuteQueryCallback(final ShardingSphereDatabase database, final QueryContext queryContext, final String jdbcDriverType) {
        return JDBCDriverType.STATEMENT.equals(jdbcDriverType)
                ? new StatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown())
                : new PreparedStatementExecuteQueryCallback(database.getProtocolType(), database.getResourceMetaData(),
                        queryContext.getSqlStatementContext().getSqlStatement(), SQLExecutorExceptionHandler.isExceptionThrown());
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<QueryResult> executePushDownQuery(final ShardingSphereDatabase database, final QueryContext queryContext,
                                                   final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                                   final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        ExecutionContext executionContext = createExecutionContext(database, queryContext);
        if (hasRawExecutionRule(database)) {
            return rawExecutor.execute(
                    createRawExecutionGroupContext(database, executionContext), queryContext, new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
        }
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), new Grantee("", "")));
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            Collection<Statement> statements = getStatements(each);
            this.statements.addAll(statements);
            addCallback.add(statements, JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType()) ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        return executePushDownQuery(executionGroupContext, queryContext, getExecuteQueryCallback(database, queryContext, prepareEngine.getType()));
    }
    
    private List<QueryResult> executePushDownQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                                   final QueryContext queryContext, final ExecuteQueryCallback callback) throws SQLException {
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            return jdbcExecutor.execute(executionGroupContext, callback);
        } finally {
            processEngine.completeSQLExecution(executionGroupContext.getReportContext().getProcessId());
        }
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
    
    private MergedResult mergeQuery(final ShardingSphereDatabase database, final List<QueryResult> queryResults, final SQLStatementContext sqlStatementContext) throws SQLException {
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
}
