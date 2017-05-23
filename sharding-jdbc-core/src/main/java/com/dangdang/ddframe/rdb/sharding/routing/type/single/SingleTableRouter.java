/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.routing.type.single;

import com.dangdang.ddframe.rdb.sharding.api.ShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataNode;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataSourceRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.NoneDatabaseShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.NoneTableShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.ConditionContext;
import com.dangdang.ddframe.rdb.sharding.constant.SQLType;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.extern.slf4j.Slf4j;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * 单逻辑表的库表路由.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@Slf4j
public final class SingleTableRouter {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final String logicTable;
    
    private final ConditionContext conditionContext;
    
    private final TableRule tableRule;
    
    private final SQLType sqlType;
    
    public SingleTableRouter(final ShardingRule shardingRule, final List<Object> parameters, final String logicTable, final ConditionContext conditionContext, final SQLType sqlType) {
        this.shardingRule = shardingRule;
        this.parameters = parameters;
        this.logicTable = logicTable;
        this.conditionContext = conditionContext;
        this.sqlType = sqlType;
        Optional<TableRule> tableRuleOptional = shardingRule.tryFindTableRule(logicTable);
        if (tableRuleOptional.isPresent()) {
            tableRule = tableRuleOptional.get();
        } else if (shardingRule.getDataSourceRule().getDefaultDataSource().isPresent()) {
            tableRule = createTableRuleWithDefaultDataSource(logicTable, shardingRule.getDataSourceRule());
        } else {
            throw new IllegalArgumentException(String.format("Cannot find table rule and default data source with logic table: '%s'", logicTable));
        }
    }
    
    private TableRule createTableRuleWithDefaultDataSource(final String logicTable, final DataSourceRule defaultDataSourceRule) {
        Map<String, DataSource> defaultDataSourceMap = new HashMap<>(1);
        defaultDataSourceMap.put(defaultDataSourceRule.getDefaultDataSourceName(), defaultDataSourceRule.getDefaultDataSource().get());
        return TableRule.builder(logicTable)
                .dataSourceRule(new DataSourceRule(defaultDataSourceMap))
                .databaseShardingStrategy(new DatabaseShardingStrategy("", new NoneDatabaseShardingAlgorithm()))
                .tableShardingStrategy(new TableShardingStrategy("", new NoneTableShardingAlgorithm())).build();
    }
    
    /**
     * 路由.
     * 
     * @return 路由结果
     */
    public SingleRoutingResult route() {
        Collection<String> routedDataSources = routeDataSources();
        Collection<String> routedTables = routeTables(routedDataSources);
        return generateRoutingResult(routedDataSources, routedTables);
    }
    
    private Collection<String> routeDataSources() {
        DatabaseShardingStrategy strategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        List<ShardingValue<?>> shardingValues;
        if (HintManagerHolder.isUseShardingHint()) {
            shardingValues = getDatabaseShardingValuesFromHint(strategy.getShardingColumns());
        } else {
            shardingValues = getShardingValues(strategy.getShardingColumns());
        }
        logBeforeRoute("database", logicTable, tableRule.getActualDatasourceNames(), strategy.getShardingColumns(), shardingValues);
        Collection<String> result = new HashSet<>(strategy.doStaticSharding(sqlType, tableRule.getActualDatasourceNames(), shardingValues));
        logAfterRoute("database", logicTable, result);
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<String> routeTables(final Collection<String> routedDataSources) {
        TableShardingStrategy strategy = shardingRule.getTableShardingStrategy(tableRule);
        List<ShardingValue<?>> shardingValues;
        if (HintManagerHolder.isUseShardingHint()) {
            shardingValues = getTableShardingValuesFromHint(strategy.getShardingColumns());
        } else {
            shardingValues = getShardingValues(strategy.getShardingColumns());
        }
        logBeforeRoute("table", logicTable, tableRule.getActualTables(), strategy.getShardingColumns(), shardingValues);
        Collection<String> result;
        if (tableRule.isDynamic()) {
            result = new HashSet<>(strategy.doDynamicSharding(shardingValues));
        } else {
            result = new HashSet<>(strategy.doStaticSharding(sqlType, tableRule.getActualTableNames(routedDataSources), shardingValues));    
        }
        logAfterRoute("table", logicTable, result);
        Preconditions.checkState(!result.isEmpty(), "no table route info");
        return result;
    }
    
    private List<ShardingValue<?>> getDatabaseShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue<?>> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(logicTable, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue<?>> getTableShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue<?>> shardingValue = HintManagerHolder.getTableShardingValue(new ShardingKey(logicTable, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue<?>> getShardingValues(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ConditionContext.Condition> condition = conditionContext.find(logicTable, each);
            if (condition.isPresent()) {
                result.add(SingleRouterUtil.convertConditionToShardingValue(condition.get(), parameters));
            }
        }
        return result;
    }
    
    private void logBeforeRoute(final String type, final String logicTable, final Collection<?> targets, final Collection<String> shardingColumns, final List<ShardingValue<?>> shardingValues) {
        log.debug("Before {} sharding {} routes db names: {} sharding columns: {} sharding values: {}", type, logicTable, targets, shardingColumns, shardingValues);
    }
    
    private void logAfterRoute(final String type, final String logicTable, final Collection<String> shardingResults) {
        log.debug("After {} sharding {} result: {}", type, logicTable, shardingResults);
    }
    
    private SingleRoutingResult generateRoutingResult(final Collection<String> routedDataSources, final Collection<String> routedTables) {
        SingleRoutingResult result = new SingleRoutingResult();
        for (DataNode each : tableRule.getActualDataNodes(routedDataSources, routedTables)) {
            result.put(each.getDataSourceName(), new SingleRoutingTableFactor(logicTable, each.getTableName()));
        }
        return result;
    }
}
