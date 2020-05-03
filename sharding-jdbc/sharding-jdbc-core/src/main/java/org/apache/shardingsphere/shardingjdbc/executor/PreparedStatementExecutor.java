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

package org.apache.shardingsphere.shardingjdbc.executor;

import org.apache.shardingsphere.shardingjdbc.executor.callback.RuleExecuteExecutorCallback;
import org.apache.shardingsphere.shardingjdbc.executor.callback.RuleExecuteQueryExecutorCallback;
import org.apache.shardingsphere.shardingjdbc.executor.callback.RuleExecuteUpdateExecutorCallback;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.RuntimeContext;
import org.apache.shardingsphere.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.spi.order.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRule;
import org.apache.shardingsphere.underlying.common.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;
import org.apache.shardingsphere.underlying.executor.sql.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.sql.QueryResult;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.executor.impl.DefaultSQLExecutorCallback;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.queryresult.MemoryQueryResult;
import org.apache.shardingsphere.underlying.executor.sql.execute.jdbc.queryresult.StreamQueryResult;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Prepared statement executor.
 */
public final class PreparedStatementExecutor {
    
    static {
        ShardingSphereServiceLoader.register(RuleExecuteQueryExecutorCallback.class);
        ShardingSphereServiceLoader.register(RuleExecuteUpdateExecutorCallback.class);
        ShardingSphereServiceLoader.register(RuleExecuteExecutorCallback.class);
    }
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final RuntimeContext runtimeContext;
    
    private final SQLExecutor sqlExecutor;
    
    public PreparedStatementExecutor(final Map<String, DataSource> dataSourceMap, final RuntimeContext runtimeContext, final SQLExecutor sqlExecutor) {
        this.dataSourceMap = dataSourceMap;
        this.runtimeContext = runtimeContext;
        this.sqlExecutor = sqlExecutor;
    }
    
    /**
     * Execute query.
     *
     * @param inputGroups input groups
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final Collection<InputGroup<StatementExecuteUnit>> inputGroups) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<QueryResult> sqlExecutorCallback = getExecuteQueryExecutorCallback(new DefaultSQLExecutorCallback<QueryResult>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return getQueryResult(statement, connectionMode);
            }
        });
        return sqlExecutor.execute(inputGroups, sqlExecutorCallback);
    }
    
    private SQLExecutorCallback<QueryResult> getExecuteQueryExecutorCallback(final DefaultSQLExecutorCallback callback) {
        Map<ShardingSphereRule, RuleExecuteQueryExecutorCallback> callbackMap = OrderedSPIRegistry.getRegisteredServices(runtimeContext.getRules(), RuleExecuteQueryExecutorCallback.class);
        return callbackMap.isEmpty() ? callback : callbackMap.values().iterator().next();
    }
    
    private QueryResult getQueryResult(final Statement statement, final ConnectionMode connectionMode) throws SQLException {
        PreparedStatement preparedStatement = (PreparedStatement) statement;
        ResultSet resultSet = preparedStatement.executeQuery();
        return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
    }
    
    /**
     * Execute update.
     * 
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Integer> sqlExecutorCallback = getExecuteUpdateExecutorCallback(new DefaultSQLExecutorCallback<Integer>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).executeUpdate();
            }
        });
        List<Integer> results = sqlExecutor.execute(inputGroups, sqlExecutorCallback);
        refreshTableMetaData(runtimeContext, sqlStatementContext);
        return isNeedAccumulate(runtimeContext.getRules().stream().filter(rule -> rule instanceof DataNodeRoutedRule).collect(Collectors.toList()), sqlStatementContext)
                ? accumulate(results) : results.get(0);
    }
    
    private SQLExecutorCallback<Integer> getExecuteUpdateExecutorCallback(final DefaultSQLExecutorCallback callback) {
        Map<ShardingSphereRule, RuleExecuteUpdateExecutorCallback> callbackMap = OrderedSPIRegistry.getRegisteredServices(runtimeContext.getRules(), RuleExecuteUpdateExecutorCallback.class);
        return callbackMap.isEmpty() ? callback : callbackMap.values().iterator().next();
    }
    
    private boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext sqlStatementContext) {
        return rules.stream().anyMatch(each -> ((DataNodeRoutedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames()));
    }
    
    private int accumulate(final List<Integer> results) {
        int result = 0;
        for (Integer each : results) {
            result += null == each ? 0 : each;
        }
        return result;
    }
    
    /**
     * Execute SQL.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Boolean> sqlExecutorCallback = getExecuteExecutorCallback(new DefaultSQLExecutorCallback<Boolean>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).execute();
            }
        });
        List<Boolean> result = sqlExecutor.execute(inputGroups, sqlExecutorCallback);
        refreshTableMetaData(runtimeContext, sqlStatementContext);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    private SQLExecutorCallback<Boolean> getExecuteExecutorCallback(final DefaultSQLExecutorCallback callback) {
        Map<ShardingSphereRule, RuleExecuteExecutorCallback> callbackMap = OrderedSPIRegistry.getRegisteredServices(runtimeContext.getRules(), RuleExecuteExecutorCallback.class);
        return callbackMap.isEmpty() ? callback : callbackMap.values().iterator().next();
    }
    
    @SuppressWarnings("unchecked")
    private void refreshTableMetaData(final RuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            RuleSchemaMetaDataLoader metaDataLoader = new RuleSchemaMetaDataLoader(runtimeContext.getRules());
            refreshStrategy.get().refreshMetaData(runtimeContext.getMetaData(), sqlStatementContext,
                tableName -> metaDataLoader.load(runtimeContext.getDatabaseType(), dataSourceMap, tableName, runtimeContext.getProperties()));
        }
    }
}
