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
import org.apache.shardingsphere.driver.executor.callback.ExecuteQueryCallback;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.model.ExecutionGroup;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutionUnit;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutor;
import org.apache.shardingsphere.infra.executor.sql.execute.engine.driver.jdbc.JDBCExecutorCallback;
import org.apache.shardingsphere.infra.executor.sql.execute.result.query.QueryResult;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.builder.SchemaBuilderMaterials;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresher;
import org.apache.shardingsphere.infra.metadata.schema.refresher.SchemaRefresherFactory;
import org.apache.shardingsphere.infra.metadata.schema.refresher.spi.SchemaChangedNotifier;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.type.DataNodeContainedRule;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.ordered.OrderedSPIRegistry;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Driver JDBC executor.
 */
@RequiredArgsConstructor
public final class DriverJDBCExecutor {
    
    static {
        ShardingSphereServiceLoader.register(SchemaChangedNotifier.class);
    }
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final MetaDataContexts metaDataContexts;
    
    private final JDBCExecutor jdbcExecutor;
    
    /**
     * Execute query.
     *
     * @param executionGroups execution groups
     * @param callback execute query callback
     * @return query results
     * @throws SQLException SQL exception
     */
    public List<QueryResult> executeQuery(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final ExecuteQueryCallback callback) throws SQLException {
        return jdbcExecutor.execute(executionGroups, callback);
    }
    
    /**
     * Execute update.
     *
     * @param executionGroups execution groups
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public int executeUpdate(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, 
                             final SQLStatementContext<?> sqlStatementContext, final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Integer> callback) throws SQLException {
        List<Integer> results = jdbcExecutor.execute(executionGroups, callback);
        refreshSchema(metaDataContexts.getDefaultMetaData(), sqlStatementContext.getSqlStatement(), routeUnits);
        return isNeedAccumulate(metaDataContexts.getDefaultMetaData().getRuleMetaData().getRules(), sqlStatementContext) ? accumulate(results) : results.get(0);
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    private void refreshSchema(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) throws SQLException {
        if (null == sqlStatement) {
            return;
        }
        Optional<SchemaRefresher> schemaRefresher = SchemaRefresherFactory.newInstance(sqlStatement);
        if (schemaRefresher.isPresent()) {
            Collection<String> routeDataSourceNames = routeUnits.stream().map(each -> each.getDataSourceMapper().getLogicName()).collect(Collectors.toList());
            SchemaBuilderMaterials materials = new SchemaBuilderMaterials(metaDataContexts.getMetaDataMap().get(DefaultSchema.LOGIC_NAME).getResource().getDatabaseType(), 
                    dataSourceMap, metaData.getRuleMetaData().getRules(), metaDataContexts.getProps());
            schemaRefresher.get().refresh(metaData.getSchema(), routeDataSourceNames, sqlStatement, materials);
            notifySchemaChanged(metaData.getSchema());
        }
    }
    
    private void notifySchemaChanged(final ShardingSphereSchema schema) {
        OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(schema), SchemaChangedNotifier.class).values().forEach(each -> each.notify(DefaultSchema.LOGIC_NAME, schema));
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
     * @param executionGroups execution groups
     * @param sqlStatement SQL statement
     * @param routeUnits route units
     * @param callback JDBC executor callback
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public boolean execute(final Collection<ExecutionGroup<JDBCExecutionUnit>> executionGroups, final SQLStatement sqlStatement,
                           final Collection<RouteUnit> routeUnits, final JDBCExecutorCallback<Boolean> callback) throws SQLException {
        List<Boolean> results = jdbcExecutor.execute(executionGroups, callback);
        refreshSchema(metaDataContexts.getDefaultMetaData(), sqlStatement, routeUnits);
        return null != results && !results.isEmpty() && null != results.get(0) && results.get(0);
    }
}
