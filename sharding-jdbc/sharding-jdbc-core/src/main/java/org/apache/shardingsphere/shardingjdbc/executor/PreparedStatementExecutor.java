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

import org.apache.shardingsphere.underlying.executor.sql.executor.SQLExecutor;
import org.apache.shardingsphere.underlying.executor.sql.executor.SQLExecutorCallback;
import org.apache.shardingsphere.underlying.executor.sql.queryresult.impl.MemoryQueryResult;
import org.apache.shardingsphere.underlying.executor.sql.queryresult.impl.StreamQueryResult;
import org.apache.shardingsphere.underlying.executor.sql.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.context.impl.ShardingRuntimeContext;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.underlying.common.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.underlying.common.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.underlying.executor.sql.queryresult.QueryResult;
import org.apache.shardingsphere.underlying.executor.sql.StatementExecuteUnit;
import org.apache.shardingsphere.underlying.executor.sql.connection.ConnectionMode;
import org.apache.shardingsphere.underlying.executor.kernel.InputGroup;

import javax.sql.DataSource;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Prepared statement executor.
 */
public final class PreparedStatementExecutor {
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final ShardingRuntimeContext runtimeContext;
    
    private final SQLExecutor sqlExecutor;
    
    public PreparedStatementExecutor(final Map<String, DataSource> dataSourceMap, final ShardingRuntimeContext runtimeContext, final SQLExecutor sqlExecutor) {
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
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<QueryResult> executeCallback = new SQLExecutorCallback<QueryResult>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return getQueryResult(statement, connectionMode);
            }
        };
        return sqlExecutor.execute(inputGroups, executeCallback);
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
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Integer> executeCallback = new SQLExecutorCallback<Integer>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).executeUpdate();
            }
        };
        List<Integer> results = sqlExecutor.execute(inputGroups, executeCallback);
        refreshTableMetaData(runtimeContext, sqlStatementContext);
        if (!runtimeContext.getRule().isAllBroadcastTables(sqlStatementContext.getTablesContext().getTableNames())) {
            return accumulate(results);
        } else {
            return results.get(0);
        }
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
        final boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<Boolean> executeCallback = new SQLExecutorCallback<Boolean>(runtimeContext.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return ((PreparedStatement) statement).execute();
            }
        };
        List<Boolean> result = sqlExecutor.execute(inputGroups, executeCallback);
        refreshTableMetaData(runtimeContext, sqlStatementContext);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    @SuppressWarnings("unchecked")
    private void refreshTableMetaData(final ShardingRuntimeContext runtimeContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            RuleSchemaMetaDataLoader metaDataLoader = new RuleSchemaMetaDataLoader(runtimeContext.getRule().toRules());
            refreshStrategy.get().refreshMetaData(runtimeContext.getMetaData(), sqlStatementContext,
                tableName -> metaDataLoader.load(runtimeContext.getDatabaseType(), dataSourceMap, tableName, runtimeContext.getProperties()));
        }
    }
}
