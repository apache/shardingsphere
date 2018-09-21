/*
 * Copyright 2016-2018 shardingsphere.io.
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

package io.shardingsphere.core.routing.type.standard;

import com.google.common.base.Optional;
import com.google.common.base.Preconditions;
import io.shardingsphere.core.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.optimizer.insert.InsertShardingCondition;
import io.shardingsphere.core.routing.strategy.ShardingStrategy;
import io.shardingsphere.core.routing.strategy.hint.HintShardingStrategy;
import io.shardingsphere.core.routing.type.RoutingEngine;
import io.shardingsphere.core.routing.type.RoutingResult;
import io.shardingsphere.core.routing.type.RoutingTable;
import io.shardingsphere.core.routing.type.TableUnit;
import io.shardingsphere.core.rule.DataNode;
import io.shardingsphere.core.rule.ShardingRule;
import io.shardingsphere.core.rule.TableRule;
import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
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
        TableRule tableRule = shardingRule.getTableRuleByLogicTableName(logicTableName);
        Collection<DataNode> dataNodes = new LinkedList<>();
        if (isRoutingUsingHint(tableRule)) {
            List<ShardingValue> databaseShardingValues = getDatabaseShardingValuesFromHint();
            List<ShardingValue> tableShardingValues = getTableShardingValuesFromHint();
            dataNodes.addAll(route(tableRule, databaseShardingValues, tableShardingValues));
        } else {
            if (shardingConditions.getShardingConditions().isEmpty()) {
                dataNodes.addAll(route(tableRule, Collections.<ShardingValue>emptyList(), Collections.<ShardingValue>emptyList()));
            } else {
                ShardingStrategy dataBaseShardingStrategy = shardingRule.getDatabaseShardingStrategy(tableRule);
                ShardingStrategy tableShardingStrategy = shardingRule.getTableShardingStrategy(tableRule);
                for (ShardingCondition each : shardingConditions.getShardingConditions()) {
                    List<ShardingValue> databaseShardingValues = isGettingShardingValuesFromHint(dataBaseShardingStrategy) ? getDatabaseShardingValuesFromHint() : getShardingValues(dataBaseShardingStrategy.getShardingColumns(), each);
                    List<ShardingValue> tableShardingValues = isGettingShardingValuesFromHint(tableShardingStrategy) ? getTableShardingValuesFromHint() : getShardingValues(tableShardingStrategy.getShardingColumns(), each);
                    dataNodes.addAll(route(tableRule, databaseShardingValues, tableShardingValues));
                }
            }
        }
        for (ShardingCondition each : shardingConditions.getShardingConditions()) {
            if (each instanceof InsertShardingCondition) {
                ((InsertShardingCondition) each).getDataNodes().addAll(dataNodes);
            }
        }
        return generateRoutingResult(dataNodes);
    }
    
    private boolean isRoutingUsingHint(final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy && shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy;
    }
    
    private boolean isGettingShardingValuesFromHint(final ShardingStrategy shardingStrategy) {
        return shardingStrategy instanceof HintShardingStrategy;
    }
    
    private Collection<DataNode> route(final TableRule tableRule, final List<ShardingValue> databaseShardingValues, final List<ShardingValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(tableRule, each, tableShardingValues));
        }
        return result;
    }
    
    private List<ShardingValue> getDatabaseShardingValuesFromHint() {
        Optional<ShardingValue> shardingValueOptional = HintManagerHolder.getDatabaseShardingValue(logicTableName);
        return shardingValueOptional.isPresent() ? Collections.singletonList(shardingValueOptional.get()) : Collections.<ShardingValue>emptyList();
    }
    
    private List<ShardingValue> getTableShardingValuesFromHint() {
        Optional<ShardingValue> shardingValueOptional = HintManagerHolder.getTableShardingValue(logicTableName);
        return shardingValueOptional.isPresent() ? Collections.singletonList(shardingValueOptional.get()) : Collections.<ShardingValue>emptyList();
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
        Collection<String> result = new LinkedHashSet<>(shardingRule.getDatabaseShardingStrategy(tableRule).doSharding(availableTargetDatabases, databaseShardingValues));
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        return result;
    }
    
    private Collection<DataNode> routeTables(final TableRule tableRule, final String routedDataSource, final List<ShardingValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = new LinkedHashSet<>(tableShardingValues.isEmpty() ? availableTargetTables
                : shardingRule.getTableShardingStrategy(tableRule).doSharding(availableTargetTables, tableShardingValues));
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
