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
import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.sharding.route.strategy.type.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.hint.SQLHintShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    private final SQLStatementContext<?> sqlStatementContext;
    
    private final ConfigurationProperties properties;
    
    private final Collection<Collection<DataNode>> originalDataNodes = new LinkedList<>();
    
    public ShardingStandardRoutingEngine(final String logicTableName, final ShardingConditions shardingConditions, final ConfigurationProperties properties) {
        this(logicTableName, shardingConditions, null, properties);
    }
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        Collection<DataNode> dataNodes = getDataNodes(shardingRule, shardingRule.getTableRule(logicTableName));
        result.getOriginalDataNodes().addAll(originalDataNodes);
        for (DataNode each : dataNodes) {
            result.getRouteUnits().add(
                    new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singleton(new RouteMapper(logicTableName, each.getTableName()))));
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodes(final ShardingRule shardingRule, final TableRule tableRule) {
        ShardingStrategy databaseShardingStrategy = createShardingStrategy(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule),
                shardingRule.getShardingAlgorithms(), shardingRule.getDefaultShardingColumn());
        ShardingStrategy tableShardingStrategy = createShardingStrategy(shardingRule.getTableShardingStrategyConfiguration(tableRule),
                shardingRule.getShardingAlgorithms(), shardingRule.getDefaultShardingColumn());
        if (isRoutingBySQLHint()) {
            return routeBySQLHint(tableRule, databaseShardingStrategy, tableShardingStrategy);
        }
        if (isRoutingByHint(shardingRule, tableRule)) {
            return routeByHint(tableRule, databaseShardingStrategy, tableShardingStrategy);
        }
        if (isRoutingByShardingConditions(shardingRule, tableRule)) {
            return routeByShardingConditions(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
        }
        return routeByMixedConditions(shardingRule, tableRule, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private boolean isRoutingBySQLHint() {
        boolean result = false;
        if (sqlStatementContext instanceof CommonSQLStatementContext) {
            Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
            for (String each : tableNames) {
                result = result || ((CommonSQLStatementContext<?>) sqlStatementContext).containsHintShardingValue(each);
            }
        }
        return result;
    }
    
    private Collection<DataNode> routeBySQLHint(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy,
                                                final ShardingStrategy tableShardingStrategy) {
        return route0(tableRule, new SQLHintShardingStrategy(databaseShardingStrategy.getShardingAlgorithm()),
                getDatabaseShardingValuesFromSQLHint(), new SQLHintShardingStrategy(tableShardingStrategy.getShardingAlgorithm()), getTableShardingValuesFromSQLHint());
    }
    
    private List<ShardingConditionValue> getDatabaseShardingValuesFromSQLHint() {
        Collection<Comparable<?>> shardingValues = new LinkedList<>();
        if (sqlStatementContext instanceof CommonSQLStatementContext) {
            Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
            for (String each : tableNames) {
                if (each.equals(logicTableName) && ((CommonSQLStatementContext<?>) sqlStatementContext).containsHintShardingDatabaseValue(each)) {
                    shardingValues.add(((CommonSQLStatementContext<?>) sqlStatementContext).getHintShardingDatabaseValue(each));
                }
            }
        }
        return getShardingConditions(shardingValues);
    }
    
    private List<ShardingConditionValue> getTableShardingValuesFromSQLHint() {
        Collection<Comparable<?>> shardingValues = new LinkedList<>();
        if (sqlStatementContext instanceof CommonSQLStatementContext) {
            Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
            for (String each : tableNames) {
                if (each.equals(logicTableName) && ((CommonSQLStatementContext<?>) sqlStatementContext).containsHintShardingTableValue(each)) {
                    shardingValues.add(((CommonSQLStatementContext<?>) sqlStatementContext).getHintShardingTableValue(each));
                }
            }
        }
        return getShardingConditions(shardingValues);
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
    
    private List<ShardingConditionValue> getDatabaseShardingValues(final ShardingRule shardingRule, final ShardingStrategy databaseShardingStrategy, final ShardingCondition shardingCondition) {
        return isGettingShardingValuesFromHint(databaseShardingStrategy)
                ? getDatabaseShardingValuesFromHint()
                : getShardingValuesFromShardingConditions(shardingRule, databaseShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private List<ShardingConditionValue> getTableShardingValues(final ShardingRule shardingRule, final ShardingStrategy tableShardingStrategy, final ShardingCondition shardingCondition) {
        return isGettingShardingValuesFromHint(tableShardingStrategy)
                ? getTableShardingValuesFromHint()
                : getShardingValuesFromShardingConditions(shardingRule, tableShardingStrategy.getShardingColumns(), shardingCondition);
    }
    
    private boolean isGettingShardingValuesFromHint(final ShardingStrategy shardingStrategy) {
        return shardingStrategy instanceof HintShardingStrategy;
    }
    
    private List<ShardingConditionValue> getDatabaseShardingValuesFromHint() {
        return getShardingConditions(HintManager.isDatabaseShardingOnly() ? HintManager.getDatabaseShardingValues() : HintManager.getDatabaseShardingValues(logicTableName));
    }
    
    private List<ShardingConditionValue> getTableShardingValuesFromHint() {
        return getShardingConditions(HintManager.getTableShardingValues(logicTableName));
    }
    
    private List<ShardingConditionValue> getShardingConditions(final Collection<Comparable<?>> shardingValue) {
        return shardingValue.isEmpty() ? Collections.emptyList() : Collections.singletonList(new ListShardingConditionValue<>("", logicTableName, shardingValue));
    }
    
    private List<ShardingConditionValue> getShardingValuesFromShardingConditions(final ShardingRule shardingRule, final Collection<String> shardingColumns, final ShardingCondition shardingCondition) {
        List<ShardingConditionValue> result = new ArrayList<>(shardingColumns.size());
        for (ShardingConditionValue each : shardingCondition.getValues()) {
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each.getTableName());
            if ((logicTableName.equals(each.getTableName()) || bindingTableRule.isPresent() && bindingTableRule.get().hasLogicTable(logicTableName))
                    && shardingColumns.contains(each.getColumnName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<DataNode> route0(final TableRule tableRule,
                                        final ShardingStrategy databaseShardingStrategy, final List<ShardingConditionValue> databaseShardingValues,
                                        final ShardingStrategy tableShardingStrategy, final List<ShardingConditionValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(tableRule, databaseShardingStrategy, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(tableRule, each, tableShardingStrategy, tableShardingValues));
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy, final List<ShardingConditionValue> databaseShardingValues) {
        if (databaseShardingValues.isEmpty()) {
            return tableRule.getActualDataSourceNames();
        }
        Collection<String> result = databaseShardingStrategy.doSharding(tableRule.getActualDataSourceNames(), databaseShardingValues, tableRule.getDataSourceDataNode(), properties);
        Preconditions.checkState(!result.isEmpty(), "No database route info");
        Preconditions.checkState(tableRule.getActualDataSourceNames().containsAll(result),
                "Some routed data sources do not belong to configured data sources. routed data sources: `%s`, configured data sources: `%s`", result, tableRule.getActualDataSourceNames());
        return result;
    }
    
    private Collection<DataNode> routeTables(final TableRule tableRule, final String routedDataSource,
                                             final ShardingStrategy tableShardingStrategy, final List<ShardingConditionValue> tableShardingValues) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        Collection<String> routedTables = tableShardingValues.isEmpty()
                ? availableTargetTables
                : tableShardingStrategy.doSharding(availableTargetTables, tableShardingValues, tableRule.getTableDataNode(), properties);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }
    
    private ShardingStrategy createShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig, final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                    final String defaultShardingColumn) {
        return null == shardingStrategyConfig ? new NoneShardingStrategy()
                : ShardingStrategyFactory.newInstance(shardingStrategyConfig, shardingAlgorithms.get(shardingStrategyConfig.getShardingAlgorithmName()), defaultShardingColumn);
    }
}
