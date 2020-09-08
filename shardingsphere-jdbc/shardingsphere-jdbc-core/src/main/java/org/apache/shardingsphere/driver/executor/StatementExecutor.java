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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.context.SchemaContext;
import org.apache.shardingsphere.infra.context.SchemaContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.ConnectionMode;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.ExecutorExceptionHandler;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.impl.DefaultSQLExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.MemoryQueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.queryresult.StreamQueryResult;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.metadata.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaDataLoader;
import org.apache.shardingsphere.infra.metadata.schema.spi.RuleMetaDataNotifier;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.TypedSPIRegistry;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;

import javax.sql.DataSource;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Statement executor.
 */
@RequiredArgsConstructor
public final class StatementExecutor {
    
    static {
        ShardingSphereServiceLoader.register(RuleMetaDataNotifier.class);
    }
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final SchemaContexts schemaContexts;
    
    private final SQLExecutor sqlExecutor;
    
    /**
     * Execute query.
     * 
     * @param inputGroups input groups
     * @return result set list
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final Collection<InputGroup<StatementExecuteUnit>> inputGroups) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback<QueryResult> sqlExecutorCallback = new DefaultSQLExecutorCallback<QueryResult>(schemaContexts.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected QueryResult executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return createQueryResult(sql, statement, connectionMode);
            }
            
            private QueryResult createQueryResult(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                ResultSet resultSet = statement.executeQuery(sql);
                return ConnectionMode.MEMORY_STRICTLY == connectionMode ? new StreamQueryResult(resultSet) : new MemoryQueryResult(resultSet);
            }
        };
        return sqlExecutor.execute(inputGroups, sqlExecutorCallback);
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
        return executeUpdate(inputGroups, Statement::executeUpdate, sqlStatementContext);
    }
    
    /**
     * Execute update with auto generated keys.
     * 
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param autoGeneratedKeys auto generated keys' flag
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final int autoGeneratedKeys) throws SQLException {
        return executeUpdate(inputGroups, (statement, sql) -> statement.executeUpdate(sql, autoGeneratedKeys), sqlStatementContext);
    }
    
    /**
     * Execute update with column indexes.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param columnIndexes column indexes
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final int[] columnIndexes) throws SQLException {
        return executeUpdate(inputGroups, (statement, sql) -> statement.executeUpdate(sql, columnIndexes), sqlStatementContext);
    }
    
    /**
     * Execute update with column names.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param columnNames column names
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final String[] columnNames) throws SQLException {
        return executeUpdate(inputGroups, (statement, sql) -> statement.executeUpdate(sql, columnNames), sqlStatementContext);
    }
    
    @SuppressWarnings("unchecked")
    private int executeUpdate(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final Updater updater, final SQLStatementContext sqlStatementContext) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback sqlExecutorCallback = new DefaultSQLExecutorCallback<Integer>(schemaContexts.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Integer executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return updater.executeUpdate(statement, sql);
            }
        };
        List<Integer> results = sqlExecutor.execute(inputGroups, sqlExecutorCallback);
        refreshTableMetaData(schemaContexts.getDefaultSchemaContext(), sqlStatementContext);
        if (isNeedAccumulate(
                schemaContexts.getDefaultSchemaContext().getSchema().getRules().stream().filter(rule -> rule instanceof DataNodeRoutedRule).collect(Collectors.toList()), sqlStatementContext)) {
            return accumulate(results);
        }
        return null == results.get(0) ? 0 : results.get(0);
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
        return execute(inputGroups, Statement::execute, sqlStatementContext);
    }
    
    /**
     * Execute SQL with auto generated keys.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param autoGeneratedKeys auto generated keys' flag
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final int autoGeneratedKeys) throws SQLException {
        return execute(inputGroups, (statement, sql) -> statement.execute(sql, autoGeneratedKeys), sqlStatementContext);
    }
    
    /**
     * Execute SQL with column indexes.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param columnIndexes column indexes
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final int[] columnIndexes) throws SQLException {
        return execute(inputGroups, (statement, sql) -> statement.execute(sql, columnIndexes), sqlStatementContext);
    }
    
    /**
     * Execute SQL with column names.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param columnNames column names
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatementContext sqlStatementContext, final String[] columnNames) throws SQLException {
        return execute(inputGroups, (statement, sql) -> statement.execute(sql, columnNames), sqlStatementContext);
    }
    
    @SuppressWarnings("unchecked")
    private boolean execute(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final Executor executor, final SQLStatementContext sqlStatementContext) throws SQLException {
        boolean isExceptionThrown = ExecutorExceptionHandler.isExceptionThrown();
        SQLExecutorCallback sqlExecutorCallback = new DefaultSQLExecutorCallback<Boolean>(schemaContexts.getDatabaseType(), isExceptionThrown) {
            
            @Override
            protected Boolean executeSQL(final String sql, final Statement statement, final ConnectionMode connectionMode) throws SQLException {
                return executor.execute(statement, sql);
            }
        };
        List<Boolean> result = sqlExecutor.execute(inputGroups, sqlExecutorCallback);
        refreshTableMetaData(schemaContexts.getDefaultSchemaContext(), sqlStatementContext);
        if (null == result || result.isEmpty() || null == result.get(0)) {
            return false;
        }
        return result.get(0);
    }
    
    @SuppressWarnings("unchecked")
    private void refreshTableMetaData(final SchemaContext schemaContext, final SQLStatementContext sqlStatementContext) throws SQLException {
        if (null == sqlStatementContext) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatementContext);
        if (refreshStrategy.isPresent()) {
            RuleSchemaMetaDataLoader metaDataLoader = new RuleSchemaMetaDataLoader(schemaContext.getSchema().getRules());
            refreshStrategy.get().refreshMetaData(schemaContext.getSchema().getMetaData(), schemaContexts.getDatabaseType(), dataSourceMap, sqlStatementContext,
                tableName -> metaDataLoader.load(schemaContexts.getDatabaseType(), dataSourceMap, tableName, schemaContexts.getProps()));
            notifyPersistRuleMetaData(DefaultSchema.LOGIC_NAME, schemaContext.getSchema().getMetaData().getSchema());
        }
    }
    
    private void notifyPersistRuleMetaData(final String schemaName, final RuleSchemaMetaData metaData) {
        RuleMetaDataNotifier notifier = TypedSPIRegistry.getRegisteredService(RuleMetaDataNotifier.class);
        if (null != notifier) {
            notifier.notify(schemaName, metaData);
        }
    }
    
    private interface Updater {
        
        int executeUpdate(Statement statement, String sql) throws SQLException;
    }
    
    private interface Executor {
        
        boolean execute(Statement statement, String sql) throws SQLException;
    }
}
