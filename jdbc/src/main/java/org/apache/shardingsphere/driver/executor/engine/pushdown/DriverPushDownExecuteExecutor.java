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
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteCallbackFactory;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.transaction.DriverImplicitCommitTransactionalExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
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
import org.apache.shardingsphere.mode.metadata.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.transaction.rule.TransactionRule;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Driver push down execute executor.
 */
public final class DriverPushDownExecuteExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final RuleMetaData globalRuleMetaData;
    
    private final ConfigurationProperties props;
    
    private final Grantee grantee;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final RawExecutor rawExecutor;
    
    public DriverPushDownExecuteExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final Grantee grantee,
                                         final JDBCExecutor jdbcExecutor, final RawExecutor rawExecutor) {
        this.connection = connection;
        globalRuleMetaData = metaData.getGlobalRuleMetaData();
        props = metaData.getProps();
        this.grantee = grantee;
        this.jdbcExecutor = jdbcExecutor;
        this.rawExecutor = rawExecutor;
    }
    
    /**
     * Execute.
     * 
     * @param database database
     * @param executionContext execution context
     * @param prepareEngine prepare engine
     * @param executeCallback statement execute callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return is query
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public boolean execute(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                           final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return database.getRuleMetaData().getAttributes(RawExecutionRuleAttribute.class).isEmpty()
                ? executeJDBC(database, executionContext, prepareEngine, executeCallback, addCallback, replayCallback)
                : executeRaw(database, executionContext);
    }
    
    @SuppressWarnings("rawtypes")
    private boolean executeJDBC(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        boolean isImplicitCommitTransaction = globalRuleMetaData.getSingleRule(TransactionRule.class).isImplicitCommitTransaction(
                executionContext, connection.getDatabaseConnectionManager().getConnectionTransaction(), connection.getAutoCommit());
        return isImplicitCommitTransaction
                ? new DriverImplicitCommitTransactionalExecutor(connection).execute(
                        database, () -> doExecuteJDBC(database, executionContext, prepareEngine, executeCallback, addCallback, replayCallback))
                : doExecuteJDBC(database, executionContext, prepareEngine, executeCallback, addCallback, replayCallback);
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private boolean doExecuteJDBC(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                  final StatementExecuteCallback executeCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(
                executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), grantee));
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            addCallback.add(getStatements(each), JDBCDriverType.PREPARED_STATEMENT.equals(prepareEngine.getType()) ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, executionContext.getQueryContext());
            List<Boolean> results = jdbcExecutor.execute(executionGroupContext,
                    new ExecuteCallbackFactory(prepareEngine.getType()).newInstance(database, executeCallback, executionContext.getSqlStatementContext().getSqlStatement()));
            new MetaDataRefreshEngine(connection.getContextManager().getPersistServiceFacade().getMetaDataManagerPersistService(), database, props)
                    .refresh(executionContext.getQueryContext().getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
            return null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
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
    
    private boolean executeRaw(final ShardingSphereDatabase database, final ExecutionContext executionContext) throws SQLException {
        int maxConnectionsSizePerQuery = props.<Integer>getValue(ConfigurationPropertyKey.MAX_CONNECTIONS_SIZE_PER_QUERY);
        ExecutionGroupContext<RawSQLExecutionUnit> executionGroupContext = new RawExecutionPrepareEngine(maxConnectionsSizePerQuery, database.getRuleMetaData().getRules())
                .prepare(executionContext.getRouteContext(), executionContext.getExecutionUnits(), new ExecutionGroupReportContext(connection.getProcessId(), database.getName(), grantee));
        return rawExecutor.execute(executionGroupContext, executionContext.getQueryContext(), new RawSQLExecutorCallback()).iterator().next() instanceof QueryResult;
    }
}
