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

package org.apache.shardingsphere.driver.executor.engine.pushdown.jdbc;

import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.transaction.DialectTransactionOption;
import org.apache.shardingsphere.database.connector.core.type.DatabaseTypeRegistry;
import org.apache.shardingsphere.driver.executor.callback.add.StatementAddCallback;
import org.apache.shardingsphere.driver.executor.callback.execute.ExecuteUpdateCallbackFactory;
import org.apache.shardingsphere.driver.executor.callback.execute.StatementExecuteUpdateCallback;
import org.apache.shardingsphere.driver.executor.callback.replay.StatementReplayCallback;
import org.apache.shardingsphere.driver.executor.engine.transaction.DriverTransactionalExecutor;
import org.apache.shardingsphere.driver.jdbc.core.connection.ShardingSphereConnection;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupReportContext;
import org.apache.shardingsphere.infra.executor.sql.context.ExecutionContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.DriverExecutionPrepareEngine;
import org.apache.shardingsphere.infra.executor.sql.prepare.driver.jdbc.JDBCDriverType;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.datanode.DataNodeRuleAttribute;
import org.apache.shardingsphere.mode.metadata.refresher.pushdown.PushDownMetaDataRefreshEngine;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.DDLStatement;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Driver JDBC push down execute update executor.
 */
public final class DriverJDBCPushDownExecuteUpdateExecutor {
    
    private final ShardingSphereConnection connection;
    
    private final String processId;
    
    private final ConfigurationProperties props;
    
    private final JDBCExecutor jdbcExecutor;
    
    public DriverJDBCPushDownExecuteUpdateExecutor(final ShardingSphereConnection connection, final ShardingSphereMetaData metaData, final JDBCExecutor jdbcExecutor) {
        this.connection = connection;
        processId = connection.getProcessId();
        props = metaData.getProps();
        this.jdbcExecutor = jdbcExecutor;
    }
    
    /**
     * Execute update.
     *
     * @param database database
     * @param executionContext execution context
     * @param prepareEngine prepare engine
     * @param updateCallback statement execute update callback
     * @param addCallback statement add callback
     * @param replayCallback statement replay callback
     * @return affected rows
     * @throws SQLException SQL exception
     */
    @SuppressWarnings("rawtypes")
    public int executeUpdate(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                             final StatementExecuteUpdateCallback updateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        return new DriverTransactionalExecutor(connection).execute(
                database, executionContext, () -> doExecuteUpdate(database, executionContext, prepareEngine, updateCallback, addCallback, replayCallback));
    }
    
    @SuppressWarnings({"rawtypes", "unchecked"})
    private int doExecuteUpdate(final ShardingSphereDatabase database, final ExecutionContext executionContext, final DriverExecutionPrepareEngine<JDBCExecutionUnit, Connection> prepareEngine,
                                final StatementExecuteUpdateCallback updateCallback, final StatementAddCallback addCallback, final StatementReplayCallback replayCallback) throws SQLException {
        ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext = prepareEngine.prepare(database.getName(), executionContext, executionContext.getExecutionUnits(),
                new ExecutionGroupReportContext(processId, database.getName(), connection.getDatabaseConnectionManager().getConnectionContext().getGrantee()));
        for (ExecutionGroup<JDBCExecutionUnit> each : executionGroupContext.getInputGroups()) {
            addCallback.add(getStatements(each), JDBCDriverType.PREPARED_STATEMENT == prepareEngine.getType() ? getParameterSets(each) : Collections.emptyList());
        }
        replayCallback.replay();
        ProcessEngine processEngine = new ProcessEngine();
        try {
            processEngine.executeSQL(executionGroupContext, executionContext.getQueryContext());
            JDBCExecutorCallback<Integer> callback = new ExecuteUpdateCallbackFactory(prepareEngine.getType())
                    .newInstance(database, executionContext.getSqlStatementContext().getSqlStatement(), updateCallback);
            List<Integer> updateCounts = jdbcExecutor.execute(executionGroupContext, callback);
            PushDownMetaDataRefreshEngine pushDownMetaDataRefreshEngine =
                    new PushDownMetaDataRefreshEngine(connection.getContextManager().getPersistServiceFacade().getModeFacade().getMetaDataManagerService(), database, props);
            if (pushDownMetaDataRefreshEngine.isNeedRefresh(executionContext.getSqlStatementContext())) {
                if (isNeedImplicitCommit(executionContext.getSqlStatementContext().getSqlStatement())) {
                    connection.commit();
                }
                pushDownMetaDataRefreshEngine.refresh(executionContext.getSqlStatementContext(), executionContext.getRouteContext().getRouteUnits());
            }
            return isNeedAccumulate(database.getRuleMetaData().getRules(), executionContext.getSqlStatementContext()) ? accumulate(updateCounts) : updateCounts.get(0);
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
    
    private boolean isNeedImplicitCommit(final SQLStatement sqlStatement) {
        DialectTransactionOption transactionOption = new DatabaseTypeRegistry(sqlStatement.getDatabaseType()).getDialectDatabaseMetaData().getTransactionOption();
        return !connection.getAutoCommit() && sqlStatement instanceof DDLStatement && transactionOption.isDDLNeedImplicitCommit();
    }
    
    private boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext sqlStatementContext) {
        for (ShardingSphereRule each : rules) {
            Optional<DataNodeRuleAttribute> ruleAttribute = each.getAttributes().findAttribute(DataNodeRuleAttribute.class);
            if (ruleAttribute.isPresent() && ruleAttribute.get().isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames())) {
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
}
