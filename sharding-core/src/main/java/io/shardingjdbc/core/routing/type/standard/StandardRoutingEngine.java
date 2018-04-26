/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingjdbc.core.routing.type.standard;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.hint.HintManagerHolder;
import io.shardingjdbc.core.hint.ShardingKey;
import io.shardingjdbc.core.optimizer.condition.ShardingCondition;
import io.shardingjdbc.core.optimizer.condition.ShardingConditions;
import io.shardingjdbc.core.routing.type.RoutingEngine;
import io.shardingjdbc.core.routing.type.RoutingResult;
import io.shardingjdbc.core.routing.type.RoutingTable;
import io.shardingjdbc.core.routing.type.TableUnit;
import io.shardingjdbc.core.rule.DataNode;
import io.shardingjdbc.core.rule.ShardingRule;
import io.shardingjdbc.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Standard routing engine.
 * 
 * @author zhangliang
 * @author maxiaoguang
 */
@RequiredArgsConstructor
public final class StandardRoutingEngine implements RoutingEngine {
    
    private final ShardingRule shardingRule;
    
    private final String logicTableName;
    
    private final ShardingConditions shardingConditions;
    
    @Override
    public RoutingResult route() {
        TableRule tableRule = shardingRule.getTableRule(logicTableName);
        Collection<String> databaseShardingColumns = shardingRule.getDatabaseShardingStrategy(tableRule).getShardingColumns();
        Collection<String> tableShardingColumns = shardingRule.getTableShardingStrategy(tableRule).getShardingColumns();
        Collection<DataNode> routedDataNodes = new LinkedList<>();
        if (HintManagerHolder.isUseShardingHint()) {
            List<ShardingValue> databaseShardingValues = getDatabaseShardingValuesFromHint(databaseShardingColumns);
            List<ShardingValue> tableShardingValues = getTableShardingValuesFromHint(tableShardingColumns);
            routedDataNodes.addAll(route(tableRule, databaseShardingValues, tableShardingValues));
        } else {
            if (shardingConditions.getShardingConditions().isEmpty()) {
                routedDataNodes.addAll(route(tableRule, Collections.<ShardingValue>emptyList(), Collections.<ShardingValue>emptyList()));
            } else {
                for (ShardingCondition each : shardingConditions.getShardingConditions()) {
                    List<ShardingValue> databaseShardingValues = getShardingValues(databaseShardingColumns, each);
                    List<ShardingValue> tableShardingValues = getShardingValues(tableShardingColumns, each);
                    routedDataNodes.addAll(route(tableRule, databaseShardingValues, tableShardingValues));
                }
            }
        }
        return generateRoutingResult(routedDataNodes);
    }
    
    private Collection<DataNode> route(final TableRule tableRule, final List<ShardingValue> databaseShardingValues, final List<ShardingValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(tableRule, each, tableShardingValues));
        }
        return result;
    }
    
    private List<ShardingValue> getDatabaseShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue> shardingValue = HintManagerHolder.getDatabaseShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue> getTableShardingValuesFromHint(final Collection<String> shardingColumns) {
        List<ShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (String each : shardingColumns) {
            Optional<ShardingValue> shardingValue = HintManagerHolder.getTableShardingValue(new ShardingKey(logicTableName, each));
            if (shardingValue.isPresent()) {
                result.add(shardingValue.get());
            }
        }
        return result;
    }
    
    private List<ShardingValue> getShardingValues(final Collection<String> shardingColumns, final ShardingCondition shardingCondition) {
        List<ShardingValue> result = new ArrayList<>(shardingColumns.size());
        for (ShardingValue each : shardingCondition.getShardingValues()) {
            if (logicTableName.equals(each.getLogicTableName()) && shardingColumns.contains(each.getColumnName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule, final List<ShardingValue> databaseShardingValues) {
        Collection<String> availableTargetDatabases = tableRule.getActualDatasourceNames();
        if (databaseShardingValues.isEmpty()) {
            return availableTargetDatabases;
        }
        Collection<String> result = shardingRule.getDatabaseShardingStrategy(tableRule).doSharding(availableTargetDatabases, databaseShardingValues);
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<DataNode> routeTables(final TableRule tableRule, final String routedDataSource, final List<ShardingValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = tableShardingValues.isEmpty() ? availableTargetTables
                : shardingRule.getTableShardingStrategy(tableRule).doSharding(availableTargetTables, tableShardingValues);
        Preconditions.checkState(!routedTables.isEmpty(), "no table route info");
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }
    
    private RoutingResult generateRoutingResult(final Collection<DataNode> routedDataNodes) {
        RoutingResult result = new RoutingResult();
        for (DataNode each : routedDataNodes) {
            TableUnit tableUnit = new TableUnit(each.getDataSourceName());
            tableUnit.getRoutingTables().add(new RoutingTable(logicTableName, each.getTableName()));
            result.getTableUnits().getTableUnits().add(tableUnit);
        }
        return result;
    }
}
