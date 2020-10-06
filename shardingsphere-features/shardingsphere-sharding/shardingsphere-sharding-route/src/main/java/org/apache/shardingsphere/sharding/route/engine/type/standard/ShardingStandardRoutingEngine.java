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
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.sharding.strategy.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.strategy.value.ListRouteValue;
import org.apache.shardingsphere.sharding.strategy.value.RouteValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
        ShardingStrategy databaseShardingStrategy = createShardingStrategy(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule), shardingRule.getShardingAlgorithms());
        ShardingStrategy tableShardingStrategy = createShardingStrategy(shardingRule.getTableShardingStrategyConfiguration(tableRule), shardingRule.getShardingAlgorithms());
        if (isRoutingByHint(shardingRule, tableRule)) {
            return routeByHint(tableRule, databaseShardingStrategy, tableShardingStrategy);
        }
        if (isRoutingByShardingConditions(shardingRule, tableRule)) {
            return routeByShardingConditions(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
        }
        return routeByMixedConditions(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final TableRule tableRule) {
        return shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration
                && shardingRule.getTableShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration;
    }
    
    private Collection<DataNode> routeByHint(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return route0(tableRule, databaseShardingStrategy, getDatabaseShardingValuesFromHint(), tableShardingStrategy, getTableShardingValuesFromHint());
    }
    
    private boolean isRoutingByShardingConditions(final ShardingRule shardingRule, final TableRule tableRule) {
        return !(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration
                || shardingRule.getTableShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration);
    }
    
    private Collection<DataNode> routeByShardingConditions(final ShardingRule shardingRule, final TableRule tableRule, 
                                                           final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return shardingConditions.getConditions().isEmpty()
                ? route0(tableRule, databaseShardingStrategy, Collections.emptyList(), tableShardingStrategy, Collections.emptyList())
                : routeByShardingConditionsWithCondition(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private Collection<DataNode> routeByShardingConditionsWithCondition(final ShardingRule shardingRule, final TableRule tableRule, 
                                                                        final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(tableRule, 
                    databaseShardingStrategy, getShardingValuesFromShardingConditions(shardingRule, databaseShardingStrategy.getShardingColumns(), each),
                    tableShardingStrategy, getShardingValuesFromShardingConditions(shardingRule, tableShardingStrategy.getShardingColumns(), each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditions(final ShardingRule shardingRule, final TableRule tableRule, 
                                                        final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return shardingConditions.getConditions().isEmpty()
                ? routeByMixedConditionsWithHint(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy)
                : routeByMixedConditionsWithCondition(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private Collection<DataNode> routeByMixedConditionsWithCondition(final ShardingRule shardingRule, final TableRule tableRule, 
                                                                     final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(tableRule, databaseShardingStrategy, 
                    getDatabaseShardingValues(shardingRule, databaseShardingStrategy, each), tableShardingStrategy, getTableShardingValues(shardingRule, tableShardingStrategy, each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditionsWithHint(final ShardingRule shardingRule, final TableRule tableRule, 
                                                                final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        if (shardingRule.getDatabaseShardingStrategyConfiguration(tableRule) instanceof HintShardingStrategyConfiguration) {
            return route0(tableRule, databaseShardingStrategy, getDatabaseShardingValuesFromHint(), tableShardingStrategy, Collections.emptyList());
        }
        return route0(tableRule, databaseShardingStrategy, Collections.emptyList(), tableShardingStrategy, getTableShardingValuesFromHint());
    }
    
    private List<RouteValue> getDatabaseShardingValues(final ShardingRule shardingRule, final ShardingStrategy databaseShardingStrategy, final ShardingCondition shardingCondition) {
        return isGettingShardingValuesFromHint(databaseShardingStrategy)
                ? getDatabaseShardingValuesFromHint() : getShardingValuesFromShardingConditions(shardingRule, databaseShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private List<RouteValue> getTableShardingValues(final ShardingRule shardingRule, final ShardingStrategy tableShardingStrategy, final ShardingCondition shardingCondition) {
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
    
    private Collection<DataNode> route0(final TableRule tableRule, 
                                        final ShardingStrategy databaseShardingStrategy, final List<RouteValue> databaseShardingValues, 
                                        final ShardingStrategy tableShardingStrategy, final List<RouteValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingStrategy, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(tableRule, each, tableShardingStrategy, tableShardingValues));
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy, final List<RouteValue> databaseShardingValues) {
        if (databaseShardingValues.isEmpty()) {
            return tableRule.getActualDatasourceNames();
        }
        Collection<String> result = new LinkedHashSet<>(databaseShardingStrategy.doSharding(tableRule.getActualDatasourceNames(), databaseShardingValues, properties));
        Preconditions.checkState(!result.isEmpty(), "no database route info");
        Preconditions.checkState(tableRule.getActualDatasourceNames().containsAll(result), 
                "Some routed data sources do not belong to configured data sources. routed data sources: `%s`, configured data sources: `%s`", result, tableRule.getActualDatasourceNames());
        return result;
    }
    
    private Collection<DataNode> routeTables(final TableRule tableRule, final String routedDataSource, final ShardingStrategy tableShardingStrategy, final List<RouteValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = new LinkedHashSet<>(tableShardingValues.isEmpty()
                ? availableTargetTables : tableShardingStrategy.doSharding(availableTargetTables, tableShardingValues, properties));
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }
    
    private ShardingStrategy createShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig, final Map<String, ShardingAlgorithm> shardingAlgorithms) {
        return null == shardingStrategyConfig ? new NoneShardingStrategy()
                : ShardingStrategyFactory.newInstance(shardingStrategyConfig, shardingAlgorithms.get(shardingStrategyConfig.getShardingAlgorithmName()));
    }
}
