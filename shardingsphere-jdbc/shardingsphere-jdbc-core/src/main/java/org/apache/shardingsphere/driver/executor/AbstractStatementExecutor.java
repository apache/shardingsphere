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
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.schema.SchemaContexts;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.executor.kernel.InputGroup;
import org.apache.shardingsphere.infra.executor.sql.QueryResult;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.StatementExecuteUnit;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutor;
import org.apache.shardingsphere.infra.executor.sql.resourced.jdbc.executor.SQLExecutorCallback;
import org.apache.shardingsphere.infra.schema.model.schema.SchemaMetaDataLoader;
import org.apache.shardingsphere.infra.schema.model.schema.spi.SchemaMetaDataNotifier;
import org.apache.shardingsphere.infra.schema.model.schema.physical.model.schema.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.schema.refresh.MetaDataRefreshStrategy;
import org.apache.shardingsphere.infra.schema.refresh.MetaDataRefreshStrategyFactory;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.rule.DataNodeRoutedRule;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
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
 * Abstract statement executor.
 */
@Getter
@RequiredArgsConstructor
public abstract class AbstractStatementExecutor {
    
    static {
        ShardingSphereServiceLoader.register(SchemaMetaDataNotifier.class);
    }
    
    private final Map<String, DataSource> dataSourceMap;
    
    private final SchemaContexts schemaContexts;
    
    private final SQLExecutor sqlExecutor;
    
    protected final boolean isNeedAccumulate(final Collection<ShardingSphereRule> rules, final SQLStatementContext<?> sqlStatementContext) {
        return rules.stream().anyMatch(each -> ((DataNodeRoutedRule) each).isNeedAccumulate(sqlStatementContext.getTablesContext().getTableNames()));
    }
    
    protected final int accumulate(final List<Integer> results) {
        return results.stream().mapToInt(each -> null == each ? 0 : each).sum();
    }
    
    @SuppressWarnings({"unchecked", "rawtypes"})
    protected final void refreshTableMetaData(final ShardingSphereMetaData metaData, final SQLStatement sqlStatement, final Collection<RouteUnit> routeUnits) throws SQLException {
        if (null == sqlStatement) {
            return;
        }
        Optional<MetaDataRefreshStrategy> refreshStrategy = MetaDataRefreshStrategyFactory.newInstance(sqlStatement);
        if (refreshStrategy.isPresent()) {
            SchemaMetaDataLoader loader = new SchemaMetaDataLoader(metaData.getRules());
            Collection<String> routeDataSourceNames = routeUnits.stream().map(RouteUnit::getDataSourceMapper).map(RouteMapper::getLogicName).collect(Collectors.toList());
            refreshStrategy.get().refreshMetaData(metaData.getSchema(), schemaContexts.getDatabaseType(), routeDataSourceNames, 
                    sqlStatement, tableName -> loader.load(schemaContexts.getDatabaseType(), dataSourceMap, tableName, schemaContexts.getProps()));
            notifyPersistLogicMetaData(DefaultSchema.LOGIC_NAME, metaData.getSchema().getSchemaMetaData());
        }
    }
    
    protected boolean executeAndRefreshMetaData(final Collection<InputGroup<StatementExecuteUnit>> inputGroups, final SQLStatement sqlStatement,
                                                final Collection<RouteUnit> routeUnits, final SQLExecutorCallback<Boolean> sqlExecutorCallback) throws SQLException {
        List<Boolean> result = sqlExecutor.execute(inputGroups, sqlExecutorCallback);
        refreshTableMetaData(schemaContexts.getDefaultMetaData(), sqlStatement, routeUnits);
        return null != result && !result.isEmpty() && null != result.get(0) && result.get(0);
    }
    
    private void notifyPersistLogicMetaData(final String schemaName, final PhysicalSchemaMetaData metaData) {
        OrderedSPIRegistry.getRegisteredServices(Collections.singletonList(metaData), SchemaMetaDataNotifier.class).values().forEach(each -> each.notify(schemaName, metaData));
    }
    
    /**
     * Execute SQL.
     *
     * @param inputGroups input groups
     * @param sqlStatement SQL statement
     * @param routeUnits route units
     * @return return true if is DQL, false if is DML
     * @throws SQLException SQL exception
     */
    public abstract boolean execute(Collection<InputGroup<StatementExecuteUnit>> inputGroups, SQLStatement sqlStatement, Collection<RouteUnit> routeUnits) throws SQLException;
    
    /**
     * Execute query.
     *
     * @param inputGroups input groups
     * @return result set list
     * @throws SQLException SQL exception
     */
    public abstract List<QueryResult> executeQuery(Collection<InputGroup<StatementExecuteUnit>> inputGroups) throws SQLException;
    
    /**
     * Execute update.
     *
     * @param inputGroups input groups
     * @param sqlStatementContext SQL statement context
     * @param routeUnits route units
     * @return effected records count
     * @throws SQLException SQL exception
     */
    public abstract int executeUpdate(Collection<InputGroup<StatementExecuteUnit>> inputGroups, SQLStatementContext<?> sqlStatementContext, Collection<RouteUnit> routeUnits) throws SQLException;
}
