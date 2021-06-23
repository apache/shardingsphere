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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroupContext;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.process.ExecuteProcessEngine;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.List;

/**
 * Driver JDBC executor.
 */
public final class DriverJDBCExecutor {
    
    private final MetaDataContexts metaDataContexts;
    
    @Getter
    private final JDBCExecutor jdbcExecutor;
    
    private final JDBCLockEngine jdbcLockEngine;
    
    public DriverJDBCExecutor(final MetaDataContexts metaDataContexts, final JDBCExecutor jdbcExecutor) {
        this.metaDataContexts = metaDataContexts;
        this.jdbcExecutor = jdbcExecutor;
        jdbcLockEngine = new JDBCLockEngine(metaDataContexts, jdbcExecutor);
    }
    
    /**
     * Execute query.
     *
     * @param executionGroupContext execution group context
     * @param sqlStatementContext SQL statement context
     * @param callback execute query callback
     * @return query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, 
                                          final SQLStatementContext<?> sqlStatementContext, final ExecuteQueryCallback callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(sqlStatementContext, executionGroupContext, metaDataContexts.getProps());
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
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext,
                             final SQLStatementContext<?> sqlStatementContext, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Integer> callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(sqlStatementContext, executionGroupContext, metaDataContexts.getProps());
            List<Integer> results = jdbcLockEngine.execute(executionGroupContext, sqlStatementContext, routeUnits, callback);
            int result = isNeedAccumulate(metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules(), sqlStatementContext) ? accumulate(results) : results.get(0);
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
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final ExecutionGroupContext<JDBCExecutionUnit> executionGroupContext, final SQLStatementContext<?> sqlStatementContext,
                           final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Boolean> callback) throws SQLException {
        try {
            ExecuteProcessEngine.initialize(sqlStatementContext, executionGroupContext, metaDataContexts.getProps());
            List<Boolean> results = jdbcLockEngine.execute(executionGroupContext, sqlStatementContext, routeUnits, callback);
            boolean result = null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
            ExecuteProcessEngine.finish(executionGroupContext.getExecutionID());
            return result;
        } finally {
            ExecuteProcessEngine.clean();
        }
    }
}
