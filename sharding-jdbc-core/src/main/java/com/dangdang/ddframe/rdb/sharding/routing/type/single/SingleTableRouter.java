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
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.statement.SQLStatement;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 单逻辑表的库表路由.
 * 
 * @author gaohongtao
 * @author zhangliang
 */
@RequiredArgsConstructor
@Slf4j
public final class SingleTableRouter {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final String logicTableName;
    
    private final SQLStatement sqlStatement;
    
    /**
     * 路由.
     * 
     * @return 路由结果
     */
    public SingleRoutingResult route() {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        Collection<String> routedDataSources = routeDataSources(tableRule);
        Collection<String> routedTables = routeTables(tableRule, routedDataSources);
        return generateRoutingResult(tableRule, routedDataSources, routedTables);
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule) {
        DatabaseShardingStrategy strategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        List<ShardingValue<?>> shardingValues = HintManagerHolder.isUseShardingHint() ? getDatabaseShardingValuesFromHint(strategy.getShardingColumns())
                : getShardingValues(strategy.getShardingColumns());
        logBeforeRoute("database", logicTableName, tableRule.getActualDatasourceNames(), strategy.getShardingColumns(), shardingValues);
        Collection<String> result = strategy.doStaticSharding(sqlStatement.getType(), tableRule.getActualDatasourceNames(), shardingValues);
        logAfterRoute("database", logicTableName, result);
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<String> routeTables(final TableRule tableRule, final Collection<String> routedDataSources) {
        TableShardingStrategy strategy = shardingRule.getTableShardingStrategy(tableRule);
        List<ShardingValue<?>> shardingValues = HintManagerHolder.isUseShardingHint() ? getTableShardingValuesFromHint(strategy.getShardingColumns())
                : getShardingValues(strategy.getShardingColumns());
        logBeforeRoute("table", logicTableName, tableRule.getActualTables(), strategy.getShardingColumns(), shardingValues);
        Collection<String> result = tableRule.isDynamic() ? strategy.doDynamicSharding(shardingValues)
                : strategy.doStaticSharding(sqlStatement.getType(), tableRule.getActualTableNames(routedDataSources), shardingValues);
        logAfterRoute("table", logicTableName, result);
        Preconditions.checkState(!result.isEmpty(), "no table route info");
        return result;
    }
    
    private List<ShardingValue<?>> getDatabaseShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue<?>> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue<?>> getTableShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue<?>> shardingValue = HintManagerHolder.getTableShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue<?>> getShardingValues(final Collection<String> shardingColumns) {
        List<ShardingValue<?>> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<Condition> condition = sqlStatement.getConditions().find(new Column(each, logicTableName));
            if (condition.isPresent()) {
                result.add(condition.get().getShardingValue(parameters));
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
    
    private SingleRoutingResult generateRoutingResult(final TableRule tableRule, final Collection<String> routedDataSources, final Collection<String> routedTables) {
        SingleRoutingResult result = new SingleRoutingResult();
        for (DataNode each : tableRule.getActualDataNodes(routedDataSources, routedTables)) {
            result.put(each.getDataSourceName(), new SingleRoutingTableFactor(logicTableName, each.getTableName()));
        }
        return result;
    }
}
