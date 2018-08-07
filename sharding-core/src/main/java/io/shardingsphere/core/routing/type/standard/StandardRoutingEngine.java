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
import io.shardingsphere.core.hint.ShardingKey;
import io.shardingsphere.core.optimizer.condition.ShardingCondition;
import io.shardingsphere.core.optimizer.condition.ShardingConditions;
import io.shardingsphere.core.optimizer.insert.InsertShardingCondition;
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
        Collection<String> databaseShardingColumns = shardingRule.getDatabaseShardingStrategy(tableRule).getShardingColumns();
        Collection<String> tableShardingColumns = shardingRule.getTableShardingStrategy(tableRule).getShardingColumns();
        Collection<DataNode> routedDataNodes = new LinkedHashSet<>();
        if (HintManagerHolder.isUseShardingHint()) {
            List<ShardingValue> databaseShardingValues = getDatabaseShardingValuesFromHint(databaseShardingColumns);
            List<ShardingValue> tableShardingValues = getTableShardingValuesFromHint(tableShardingColumns);
            Collection<DataNode> dataNodes = route(tableRule, databaseShardingValues, tableShardingValues);
            for (ShardingCondition each : shardingConditions.getShardingConditions()) {
                if (each instanceof InsertShardingCondition) {
                    ((InsertShardingCondition) each).getDataNodes().addAll(dataNodes);
                }
            }
            routedDataNodes.addAll(dataNodes);
        } else {
            if (shardingConditions.getShardingConditions().isEmpty()) {
                routedDataNodes.addAll(route(tableRule, Collections.<ShardingValue>emptyList(), Collections.<ShardingValue>emptyList()));
            } else {
                for (ShardingCondition each : shardingConditions.getShardingConditions()) {
                    List<ShardingValue> databaseShardingValues = getShardingValues(databaseShardingColumns, each);
                    List<ShardingValue> tableShardingValues = getShardingValues(tableShardingColumns, each);
                    Collection<DataNode> dataNodes = route(tableRule, databaseShardingValues, tableShardingValues);
                    routedDataNodes.addAll(dataNodes);
                    if (each instanceof InsertShardingCondition) {
                        ((InsertShardingCondition) each).getDataNodes().addAll(dataNodes);
                    }
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
