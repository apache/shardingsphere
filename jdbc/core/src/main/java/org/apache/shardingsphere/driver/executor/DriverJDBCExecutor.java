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

import org.apache.shardingsphere.driver.executor.callback.ExecuteQueryCallback;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.connection.refresher.MetaDataRefreshEngine;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.process.ProcessEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Driver JDBC executor.
 */
public final class DriverJDBCExecutor {
    
    private final String databaseName;
    
    private final MetaDataContexts metaDataContexts;
    
    private final JDBCExecutor jdbcExecutor;
    
    private final MetaDataRefreshEngine metaDataRefreshEngine;
    
    private final ProcessEngine processEngine = new ProcessEngine();
    
    public DriverJDBCExecutor(final String databaseName, final ContextManager contextManager, final JDBCExecutor jdbcExecutor) {
        this.databaseName = databaseName;
        this.jdbcExecutor = jdbcExecutor;
        metaDataContexts = contextManager.getMetaDataContexts();
        metaDataRefreshEngine = new MetaDataRefreshEngine(contextManager.getInstanceContext().getModeContextManager(),
                metaDataContexts.getMetaData().getDatabase(databaseName), metaDataContexts.getMetaData().getProps());
    }
    
    /**
     * Execute query.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     * @param callback execute query callback
     * @return query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                          final QueryContext queryContext, final ExecuteQueryCallback callback) throws SQLException {
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            return jdbcExecutor.execute(executionGroupContext, callback);
        } finally {
            processEngine.completeSQLExecution();
        }
    }
    
    /**
     * Execute update.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                             final QueryContext queryContext, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Integer> callback) throws SQLException {
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            SQLStatementContext sqlStatementContext = queryContext.getSqlStatementContext();
            List<Integer> results = doExecute(executionGroupContext, sqlStatementContext, routeUnits, callback);
            return isNeedAccumulate(metaDataContexts.getMetaData().getDatabase(databaseName).getRuleMetaData().getRules(), sqlStatementContext) ? accumulate(results) : results.get(0);
        } finally {
            processEngine.completeSQLExecution();
        }
    }
    
    private boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext sqlStatementContext) {
        for (ShardingSphereRule each : rules) {
            if (each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames())) {
                return true;
            }
        }
        return false;
    }
    
    private int accumulate(final List<Integer> updateResults) {
        int result = 0;
        for (Integer each : updateResults) {
            result += null != each ? each : 0;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @param executionGroupContext execution group context
     * @param queryContext query context
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final QueryContext queryContext,
                           final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Boolean> callback) throws SQLException {
        try {
            processEngine.executeSQL(executionGroupContext, queryContext);
            List<Boolean> results = doExecute(executionGroupContext, queryContext.getSqlStatementContext(), routeUnits, callback);
            return null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
        } finally {
            processEngine.completeSQLExecution();
        }
    }
    
    private <T> List<T> doExecute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final SQLStatementContext sqlStatementContext, final Collection<RouteUnit> routeUnits,
                                  final JDBCExecutorCallback<T> callback) throws SQLException {
        List<T> results = jdbcExecutor.execute(executionGroupContext, callback);
        metaDataRefreshEngine.refresh(sqlStatementContext, routeUnits);
        return results;
    }
}
