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

package com.dangdang.ddframe.rdb.sharding.routing.type.simple;

import com.dangdang.ddframe.rdb.sharding.api.BaseShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ListShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.ShardingValueType;
import com.dangdang.ddframe.rdb.sharding.api.SingleShardingValue;
import com.dangdang.ddframe.rdb.sharding.api.rule.DataNode;
import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.dangdang.ddframe.rdb.sharding.api.rule.TableRule;
import com.dangdang.ddframe.rdb.sharding.api.strategy.database.DatabaseShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.api.strategy.table.TableShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Column;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition.Condition;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.sql.SQLStatement;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.ShardingStrategy;
import com.dangdang.ddframe.rdb.sharding.routing.strategy.SingleKeyShardingAlgorithm;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingEngine;
import com.dangdang.ddframe.rdb.sharding.routing.type.RoutingResult;
import com.dangdang.ddframe.rdb.sharding.routing.type.TableUnit;
import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Simple routing engine.
 * 
 * @author zhangliang
 */
@RequiredArgsConstructor
public final class SimpleRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final List<Object> parameters;
    
    private final String logicTableName;
    
    private final SQLStatement sqlStatement;
    
    @Override
    public RoutingResult route() {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        List<BaseShardingValue> databaseShardingValues = getDatabaseShardingValues(tableRule);
        List<BaseShardingValue> tableShardingValues = getTableShardingValues(tableRule);
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingValues);
        Map<String, Collection<String>> routedMap = new LinkedHashMap<>(routedDataSources.size());
        for (String each : routedDataSources) {
            routedMap.put(each, routeTables(tableRule, each, tableShardingValues));
        }
        return generateRoutingResult(tableRule, routedMap);
    }
    
    private List<BaseShardingValue> getDatabaseShardingValues(final TableRule tableRule) {
        DatabaseShardingStrategy strategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        return HintManagerHolder.isUseShardingHint() ? getDatabaseShardingValuesFromHint(strategy.getShardingColumns()) : getShardingValues(strategy.getShardingColumns());
    }
    
    private List<BaseShardingValue> getTableShardingValues(final TableRule tableRule) {
        TableShardingStrategy strategy = shardingRule.getTableShardingStrategy(tableRule);
        return HintManagerHolder.isUseShardingHint() ? getTableShardingValuesFromHint(strategy.getShardingColumns()) : getShardingValues(strategy.getShardingColumns());
    }
    
    private List<BaseShardingValue> getDatabaseShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<BaseShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<BaseShardingValue> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<BaseShardingValue> getTableShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<BaseShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<BaseShardingValue> shardingValue = HintManagerHolder.getTableShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<BaseShardingValue> getShardingValues(final Collection<String> shardingColumns) {
        List<BaseShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<Condition> condition = sqlStatement.getConditions().find(new Column(each, logicTableName));
            if (condition.isPresent()) {
                result.add(condition.get().getShardingValue(parameters));
            }
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule, final List<BaseShardingValue> databaseShardingValues) {
        DatabaseShardingStrategy strategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        if (isAccurateSharding(databaseShardingValues, strategy)) {
            Collection<String> result = new LinkedList<>();
            Collection<SingleShardingValue> accurateDatabaseShardingValues = transferToShardingValues((ListShardingValue<?>) databaseShardingValues.get(0));
            for (SingleShardingValue<?> eachDatabaseShardingValue : accurateDatabaseShardingValues) {
                result.add(strategy.doAccurateSharding(tableRule.getActualDatasourceNames(), eachDatabaseShardingValue));
            }
            return result;
        }
        Collection<String> result = shardingRule.getDatabaseShardingStrategy(tableRule).doStaticSharding(tableRule.getActualDatasourceNames(), databaseShardingValues);
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<String> routeTables(final TableRule tableRule, final String routedDataSource, final List<BaseShardingValue> tableShardingValues) {
        TableShardingStrategy strategy = shardingRule.getTableShardingStrategy(tableRule);
        if (isAccurateSharding(tableShardingValues, strategy)) {
            Collection<String> result = new HashSet<>();
            Collection<SingleShardingValue> accurateTableShardingValues = transferToShardingValues((ListShardingValue<?>) tableShardingValues.get(0));
            for (SingleShardingValue<?> eachTableShardingValue : accurateTableShardingValues) {
                result.add(shardingRule.getTableShardingStrategy(tableRule).doAccurateSharding(
                        tableRule.isDynamic() ? Collections.<String>emptyList() : tableRule.getActualTableNames(routedDataSource), eachTableShardingValue));
            }
            return result;
        }
        Collection<String> result = 
                tableRule.isDynamic() ? strategy.doDynamicSharding(tableShardingValues) : strategy.doStaticSharding(tableRule.getActualTableNames(routedDataSource), tableShardingValues);
        Preconditions.checkState(!result.isEmpty(), "no table route info");
        return result;
    }
    
    private boolean isAccurateSharding(final List<BaseShardingValue> shardingValues, final ShardingStrategy shardingStrategy) {
        return 1 == shardingValues.size() && shardingStrategy.getShardingAlgorithm() instanceof SingleKeyShardingAlgorithm && ShardingValueType.RANGE != shardingValues.get(0).getType();
    }
    
    @SuppressWarnings("unchecked")
    private List<SingleShardingValue> transferToShardingValues(final ListShardingValue<?> shardingValue) {
        List<SingleShardingValue> result = new ArrayList<>(shardingValue.getValues().size());
        for (Comparable<?> each : shardingValue.getValues()) {
            result.add(new SingleShardingValue(shardingValue.getLogicTableName(), shardingValue.getColumnName(), each));
        }
        return result;
    }
    
    private RoutingResult generateRoutingResult(final TableRule tableRule, final Map<String, Collection<String>> routedMap) {
        RoutingResult result = new RoutingResult();
        for (Entry<String, Collection<String>> entry : routedMap.entrySet()) {
            Collection<DataNode> dataNodes = tableRule.getActualDataNodes(entry.getKey(), entry.getValue());
            for (DataNode each : dataNodes) {
                result.getTableUnits().getTableUnits().add(new TableUnit(each.getDataSourceName(), logicTableName, each.getTableName()));
            }
        }
        return result;
    }
}
