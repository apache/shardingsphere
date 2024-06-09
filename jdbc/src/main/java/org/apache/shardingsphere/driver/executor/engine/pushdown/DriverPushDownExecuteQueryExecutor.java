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

package org.apache.shardingsphere.driver.executor.engine.pushdown;

import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteQueryCallbackFactory;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSet;
import org.apache.shardingsphere.driver.jdbc.core.resultset.ShardingSphereResultSetFactory;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.connection.kernel.KernelProcessor;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
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
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.user.Grantee;
import org.apache.shardingsphere.infra.rule.attribute.raw.RawExecutionRuleAttribute;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Driver push down execute query executor.
 */
public final class DriverPushDownExecuteQueryExecutor {
    
    private final ConnectionContext connectionContext;
    
    private final String processId;
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    private final Collection<Statement> statements;
    
    public DriverPushDownExecuteQueryExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor) {
        connectionContext = connection.getDatabaseConnectionManager().getConnectionContext();
        processId = connection.getProcessId();
        globalRuleMetaData = metaData.getGlobalRuleMetaData();
        props = metaData.getProps();
        this.jdbcExecutor = jdbcExecutor;
        this.rawExecutor = rawExecutor;
        statements = new LinkedList<>();
    }
    
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
    public ShardingSphereResultSet executeQuery(final ShardingSphereDatabase database, final QueryContext queryContext,
                                                final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine, final Statement statement,
                                                final Map<String, Integer> columnLabelAndIndexMap,
                                                final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        List<QueryResult> queryResults = getQueryResults(database, queryContext, prepareEngine, addCallback, replayCallback);
        boolean isContainsEnhancedTable = queryContext.getSqlStatementContext() instanceof SelectStatementContext
                && ((SelectStatementContext) queryContext.getSqlStatementContext()).isContainsEnhancedTable();
        return new ShardingSphereResultSetFactory(connectionContext, globalRuleMetaData, props, statements)
                .newInstance(database, queryContext, queryResults, statement, columnLabelAndIndexMap, isContainsEnhancedTable);
    }
    
    @SuppressWarnings("rawtypes")
    private List<QueryResult> getQueryResults(final ShardingSphereDatabase database, final QueryContext queryContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                              final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        statements.clear();
        ExecutionContext executionContext = new KernelProcessor().generateExecutionContext(queryContext, database, globalRuleMetaData, props, connectionContext);
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? getJDBCQueryResults(database, queryContext, prepareEngine, addCallback, replayCallback, executionContext)
                : getRawQueryResults(database, queryContext, executionContext);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private List<QueryResult> getJDBCQueryResults(final ShardingSphereDatabase database, final QueryContext queryContext,
                                                  final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                                  final StatementAddCallback addCallback, final StatementReplayCallback replayCallback,
                                                  final ExecutionContext executionContext) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(processId, database.getName(), new Grantee("", "")));
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            Collection<Statement> statements = getStatements(each);
            this.statements.addAll(statements);
            addCallback.add(statements, JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType()) ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            return jdbcExecutor.execute(executionGroupContext, new ExecuteQueryCallbackFactory(prepareEngine.getType()).newInstance(database, queryContext));
        } finally {
            processEngine.completeSQLExecution(executionGroupContext.getReportContext().getProcessId());
        }
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
    
    private List<QueryResult> getRawQueryResults(final ShardingSphereDatabase database, final QueryContext queryContext, final ExecutionContext executionContext) throws SQLException {
        return rawExecutor.execute(
                createRawExecutionGroupContext(database, executionContext), queryContext, new RawSQLExecutorCallback()).stream().map(QueryResult.class::cast).collect(Collectors.toList());
    }
    
    private ExecutionGroupContext<RawSQLExecutionUnit> createRawExecutionGroupContext(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = props.<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        return new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules()).prepare(
                executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(processId, database.getName(), new Grantee("", "")));
    }
}
