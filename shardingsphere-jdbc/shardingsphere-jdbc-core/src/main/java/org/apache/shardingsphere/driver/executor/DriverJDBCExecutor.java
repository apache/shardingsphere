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
import org.apache.shardingsphere.infra.binder.LogicSQL;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.identifier.type.DataNodeContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Driver JDBC executor.
 */
public final class DriverJDBCExecutor {
    
    private final String schemaName;
    
    private final ContextManager contextManager;
    
    @Getter
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCLockEngine jdbcLockEngine;
    
    public DriverJDBCExecutor(final String schemaName, final ContextManager contextManager, final JDBCExecutor jdbcExecutor) {
        this.schemaName = schemaName;
        this.contextManager = contextManager;
        this.jdbcExecutor = jdbcExecutor;
        jdbcLockEngine = new JDBCLockEngine(schemaName, contextManager, jdbcExecutor);
    }
    
    /**
     * Execute query.
     *
     * @param executionGroupContext execution group context
     * @param logicSQL logic SQL
     * @param callback execute query callback
     * @return query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                                          final LogicSQL logicSQL, final ExecuteQueryCallback callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, contextManager.getMetaDataContexts().getProps());
            List<QueryResult> result = jdbcExecutor.execute(executionGroupContext, callback);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
    
    /**
     * Execute update.
     *
     * @param executionGroupContext execution group context
     * @param logicSQL logic SQL
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                             final LogicSQL logicSQL, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Integer> callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, contextManager.getMetaDataContexts().getProps());
            SQLStatementContext<?> sqlStatementContext = logicSQL.getSqlStatementContext();
            List<Integer> results = jdbcLockEngine.execute(executionGroupContext, sqlStatementContext, routeUnits, callback);
            int result = isNeedAccumulate(contextManager.getMetaDataContexts().getMetaData(schemaName).getRuleMetaData().getRules(), sqlStatementContext) ? accumulate(results) : results.get(0);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
    
    private boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext<?> sqlStatementContext) {
        return rules.stream().anyMatch(each -> each instanceof DataNodeContainedRule && ((DataNodeContainedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames()));
    }
    
    private int accumulate(final List<Integer> updateResults) {
        return updateResults.stream().mapToInt(each -> null == each ? 0 : each).sum();
    }
    
    /**
     * Execute SQL.
     *
     * @param executionGroupContext execution group context
     * @param logicSQL logic SQL
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final LogicSQL logicSQL,
                           final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Boolean> callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(logicSQL, executionGroupContext, contextManager.getMetaDataContexts().getProps());
            List<Boolean> results = jdbcLockEngine.execute(executionGroupContext, logicSQL.getSqlStatementContext(), routeUnits, callback);
            boolean result = null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
}
