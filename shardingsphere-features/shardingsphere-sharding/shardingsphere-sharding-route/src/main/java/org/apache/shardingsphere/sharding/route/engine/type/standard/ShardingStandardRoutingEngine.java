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

package org.apache.shardingsphere.sharding.route.engine.type.standard;

import com.google.common.base.Preconditions;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Sharding standard routing engine.
 */
@RequiredArgsConstructor
public final class ShardingStandardRoutingEngine implements ShardingRouteEngine {
    
    private final String logicTableName;
    
    private final ShardingConditions shardingConditions;
    
    private final ConfigurationProperties properties;
    
    private final Collection<Collection<DataNode>> originalDataNodes = new LinkedList<>();
    
    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        Collection<DataNode> dataNodes = getDataNodes(shardingRule, shardingRule.getTableRule(logicTableName));
        routeContext.getOriginalDataNodes().addAll(originalDataNodes);
        for (DataNode each : dataNodes) {
            routeContext.getRouteUnits().add(
                    new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singletonList(new RouteMapper(logicTableName, each.getTableName()))));
        }
    }
    
    private Collection<DataNode> getDataNodes(final ShardingRule shardingRule, final TableRule tableRule) {
        if (isRoutingByHint(shardingRule, tableRule)) {
            return routeByHint(shardingRule, tableRule);
        }
        if (isRoutingByShardingConditions(shardingRule, tableRule)) {
            return routeByShardingConditions(shardingRule, tableRule);
        }
        return routeByMixedConditions(shardingRule, tableRule);
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy && shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy;
    }
    
    private Collection<DataNode> routeByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return route0(shardingRule, tableRule, getDatabaseShardingValuesFromHint(), getTableShardingValuesFromHint());
    }
    
    private boolean isRoutingByShardingConditions(final ShardingRule shardingRule, final TableRule tableRule) {
        return !(shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy || shardingRule.getTableShardingStrategy(tableRule) instanceof HintShardingStrategy);
    }
    
    private Collection<DataNode> routeByShardingConditions(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingConditions.getConditions().isEmpty()
                ? route0(shardingRule, tableRule, Collections.emptyList(), Collections.emptyList()) : routeByShardingConditionsWithCondition(shardingRule, tableRule);
    }
    
    private Collection<DataNode> routeByShardingConditionsWithCondition(final ShardingRule shardingRule, final TableRule tableRule) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(shardingRule, tableRule, 
                    getShardingValuesFromShardingConditions(shardingRule, shardingRule.getDatabaseShardingStrategy(tableRule).getShardingColumns(), each),
                    getShardingValuesFromShardingConditions(shardingRule, shardingRule.getTableShardingStrategy(tableRule).getShardingColumns(), each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditions(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingConditions.getConditions().isEmpty() ? routeByMixedConditionsWithHint(shardingRule, tableRule) : routeByMixedConditionsWithCondition(shardingRule, tableRule);
    }
    
    private Collection<DataNode> routeByMixedConditionsWithCondition(final ShardingRule shardingRule, final TableRule tableRule) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(shardingRule, tableRule, getDatabaseShardingValues(shardingRule, tableRule, each), getTableShardingValues(shardingRule, tableRule, each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditionsWithHint(final ShardingRule shardingRule, final TableRule tableRule) {
        if (shardingRule.getDatabaseShardingStrategy(tableRule) instanceof HintShardingStrategy) {
            return route0(shardingRule, tableRule, getDatabaseShardingValuesFromHint(), Collections.emptyList());
        }
        return route0(shardingRule, tableRule, Collections.emptyList(), getTableShardingValuesFromHint());
    }
    
    private List<RouteValue> getDatabaseShardingValues(final ShardingRule shardingRule, final TableRule tableRule, final ShardingCondition shardingCondition) {
        ShardingStrategy dataBaseShardingStrategy = shardingRule.getDatabaseShardingStrategy(tableRule);
        return isGettingShardingValuesFromHint(dataBaseShardingStrategy)
                ? getDatabaseShardingValuesFromHint() : getShardingValuesFromShardingConditions(shardingRule, dataBaseShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private List<RouteValue> getTableShardingValues(final ShardingRule shardingRule, final TableRule tableRule, final ShardingCondition shardingCondition) {
        ShardingStrategy tableShardingStrategy = shardingRule.getTableShardingStrategy(tableRule);
        return isGettingShardingValuesFromHint(tableShardingStrategy)
                ? getTableShardingValuesFromHint() : getShardingValuesFromShardingConditions(shardingRule, tableShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private boolean isGettingShardingValuesFromHint(final ShardingStrategy shardingStrategy) {
        return shardingStrategy instanceof HintShardingStrategy;
    }
    
    private List<RouteValue> getDatabaseShardingValuesFromHint() {
        return getRouteValues(HintManager.isDatabaseShardingOnly() ? HintManager.getDatabaseShardingValues() : HintManager.getDatabaseShardingValues(logicTableName));
    }
    
    private List<RouteValue> getTableShardingValuesFromHint() {
        return getRouteValues(HintManager.getTableShardingValues(logicTableName));
    }
    
    private List<RouteValue> getRouteValues(final Collection<Comparable<?>> shardingValue) {
        return shardingValue.isEmpty() ? Collections.emptyList() : Collections.singletonList(new ListRouteValue<>("", logicTableName, shardingValue));
    }
    
    private List<RouteValue> getShardingValuesFromShardingConditions(final ShardingRule shardingRule, final Collection<String> shardingColumns, final ShardingCondition shardingCondition) {
        List<RouteValue> result = new ArrayList<>(shardingColumns.size());
        for (RouteValue each : shardingCondition.getRouteValues()) {
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(logicTableName);
            if ((logicTableName.equals(each.getTableName()) || bindingTableRule.isPresent() && bindingTableRule.get().hasLogicTable(logicTableName)) 
                    && shardingColumns.contains(each.getColumnName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<DataNode> route0(final ShardingRule shardingRule, final TableRule tableRule, final List<RouteValue> databaseShardingValues, final List<RouteValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(shardingRule, tableRule, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(shardingRule, tableRule, each, tableShardingValues));
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final ShardingRule shardingRule, final TableRule tableRule, final List<RouteValue> databaseShardingValues) {
        if (databaseShardingValues.isEmpty()) {
            return tableRule.getActualDatasourceNames();
        }
        Collection<String> result = new LinkedHashSet<>(shardingRule.getDatabaseShardingStrategy(tableRule).doSharding(tableRule.getActualDatasourceNames(), databaseShardingValues, properties));
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        Preconditions.checkState(tableRule.getActualDatasourceNames().containsAll(result), 
                "Some routed data sources do not belong to configured data sources. routed data sources: `%s`, configured data sources: `%s`", result, tableRule.getActualDatasourceNames());
        return result;
    }
    
    private Collection<DataNode> routeTables(final ShardingRule shardingRule, final TableRule tableRule, final String routedDataSource, final List<RouteValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = new LinkedHashSet<>(tableShardingValues.isEmpty() ? availableTargetTables
                : shardingRule.getTableShardingStrategy(tableRule).doSharding(availableTargetTables, tableShardingValues, properties));
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }
}
