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

import com.cedarsoftware.util.CaseInsensitiveSet;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.HintShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.exception.algorithm.MismatchedShardingDataSourceRouteInfoException;
import org.apache.shardingsphere.sharding.exception.algorithm.NoShardingDatabaseRouteInfoException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.sharding.route.strategy.type.hint.HintShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.type.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.rule.BindingTableRule;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Sharding standard route engine.
 */
public final class ShardingStandardRouteEngine implements ShardingRouteEngine {
    
    private final String logicTableName;
    
    private final ShardingConditions shardingConditions;
    
    private final SQLStatementContext sqlStatementContext;
    
    private final ConfigurationProperties props;
    
    private final Collection<Collection<DataNode>> originalDataNodes = new LinkedList<>();
    
    private final HintValueContext hintValueContext;
    
    public ShardingStandardRouteEngine(final String logicTableName, final ShardingConditions shardingConditions, final SQLStatementContext sqlStatementContext,
                                       final HintValueContext hintValueContext, final ConfigurationProperties props) {
        this.logicTableName = logicTableName;
        this.shardingConditions = shardingConditions;
        this.sqlStatementContext = sqlStatementContext;
        this.props = props;
        this.hintValueContext = hintValueContext;
    }
    
    @Override
    public RouteContext route(final ShardingRule shardingRule) {
        RouteContext result = new RouteContext();
        Collection<DataNode> dataNodes = getDataNodes(shardingRule, shardingRule.getShardingTable(logicTableName));
        result.getOriginalDataNodes().addAll(originalDataNodes);
        for (DataNode each : dataNodes) {
            result.getRouteUnits().add(
                    new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singleton(new RouteMapper(logicTableName, each.getTableName()))));
        }
        return result;
    }
    
    private Collection<DataNode> getDataNodes(final ShardingRule shardingRule, final ShardingTable shardingTable) {
        ShardingStrategy databaseShardingStrategy = createShardingStrategy(shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable),
                shardingRule.getShardingAlgorithms(), shardingRule.getDefaultShardingColumn());
        ShardingStrategy tableShardingStrategy = createShardingStrategy(shardingRule.getTableShardingStrategyConfiguration(shardingTable),
                shardingRule.getShardingAlgorithms(), shardingRule.getDefaultShardingColumn());
        if (isRoutingByHint(shardingRule, shardingTable)) {
            return routeByHint(shardingTable, databaseShardingStrategy, tableShardingStrategy);
        }
        if (isRoutingByShardingConditions(shardingRule, shardingTable)) {
            return routeByShardingConditions(shardingRule, shardingTable, databaseShardingStrategy, tableShardingStrategy);
        }
        return routeByMixedConditions(shardingRule, shardingTable, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private boolean isRoutingByHint(final ShardingRule shardingRule, final ShardingTable shardingTable) {
        return shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable) instanceof HintShardingStrategyConfiguration
                && shardingRule.getTableShardingStrategyConfiguration(shardingTable) instanceof HintShardingStrategyConfiguration;
    }
    
    private boolean isRoutingBySQLHint() {
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        for (String each : tableNames) {
            if (hintValueContext.containsHintShardingValue(each)) {
                return true;
            }
        }
        return false;
    }
    
    private Collection<DataNode> routeByHint(final ShardingTable shardingTable, final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return route0(shardingTable, databaseShardingStrategy, getDatabaseShardingValuesFromHint(), tableShardingStrategy, getTableShardingValuesFromHint());
    }
    
    private boolean isRoutingByShardingConditions(final ShardingRule shardingRule, final ShardingTable shardingTable) {
        return !(shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable) instanceof HintShardingStrategyConfiguration
                || shardingRule.getTableShardingStrategyConfiguration(shardingTable) instanceof HintShardingStrategyConfiguration);
    }
    
    private Collection<DataNode> routeByShardingConditions(final ShardingRule shardingRule, final ShardingTable shardingTable,
                                                           final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return shardingConditions.getConditions().isEmpty()
                ? route0(shardingTable, databaseShardingStrategy, Collections.emptyList(), tableShardingStrategy, Collections.emptyList())
                : routeByShardingConditionsWithCondition(shardingRule, shardingTable, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private Collection<DataNode> routeByShardingConditionsWithCondition(final ShardingRule shardingRule, final ShardingTable shardingTable,
                                                                        final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(shardingTable,
                    databaseShardingStrategy, getShardingValuesFromShardingConditions(shardingRule, databaseShardingStrategy.getShardingColumns(), each),
                    tableShardingStrategy, getShardingValuesFromShardingConditions(shardingRule, tableShardingStrategy.getShardingColumns(), each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditions(final ShardingRule shardingRule, final ShardingTable shardingTable,
                                                        final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        return shardingConditions.getConditions().isEmpty()
                ? routeByMixedConditionsWithHint(shardingRule, shardingTable, databaseShardingStrategy, tableShardingStrategy)
                : routeByMixedConditionsWithCondition(shardingRule, shardingTable, databaseShardingStrategy, tableShardingStrategy);
    }
    
    private Collection<DataNode> routeByMixedConditionsWithCondition(final ShardingRule shardingRule, final ShardingTable shardingTable,
                                                                     final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        Collection<DataNode> result = new LinkedList<>();
        for (ShardingCondition each : shardingConditions.getConditions()) {
            Collection<DataNode> dataNodes = route0(shardingTable, databaseShardingStrategy,
                    getDatabaseShardingValues(shardingRule, databaseShardingStrategy, each), tableShardingStrategy, getTableShardingValues(shardingRule, tableShardingStrategy, each));
            result.addAll(dataNodes);
            originalDataNodes.add(dataNodes);
        }
        return result;
    }
    
    private Collection<DataNode> routeByMixedConditionsWithHint(final ShardingRule shardingRule, final ShardingTable shardingTable,
                                                                final ShardingStrategy databaseShardingStrategy, final ShardingStrategy tableShardingStrategy) {
        if (shardingRule.getDatabaseShardingStrategyConfiguration(shardingTable) instanceof HintShardingStrategyConfiguration) {
            return route0(shardingTable, databaseShardingStrategy, getDatabaseShardingValuesFromHint(), tableShardingStrategy, Collections.emptyList());
        }
        return route0(shardingTable, databaseShardingStrategy, Collections.emptyList(), tableShardingStrategy, getTableShardingValuesFromHint());
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
        if (isRoutingBySQLHint()) {
            return getDatabaseShardingValuesFromSQLHint();
        }
        return getShardingConditions(HintManager.isDatabaseShardingOnly() ? HintManager.getDatabaseShardingValues() : HintManager.getDatabaseShardingValues(logicTableName));
    }
    
    private List<ShardingConditionValue> getDatabaseShardingValuesFromSQLHint() {
        Collection<Comparable<?>> shardingValues = new LinkedList<>();
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        for (String each : tableNames) {
            if (each.equals(logicTableName) && hintValueContext.containsHintShardingDatabaseValue(each)) {
                shardingValues.addAll(hintValueContext.getHintShardingDatabaseValue(each));
            }
        }
        return getShardingConditions(shardingValues);
    }
    
    private List<ShardingConditionValue> getTableShardingValuesFromHint() {
        if (isRoutingBySQLHint()) {
            return getTableShardingValuesFromSQLHint();
        }
        return getShardingConditions(HintManager.getTableShardingValues(logicTableName));
    }
    
    private List<ShardingConditionValue> getTableShardingValuesFromSQLHint() {
        Collection<Comparable<?>> shardingValues = new LinkedList<>();
        Collection<String> tableNames = sqlStatementContext.getTablesContext().getTableNames();
        for (String each : tableNames) {
            if (each.equals(logicTableName) && hintValueContext.containsHintShardingTableValue(each)) {
                shardingValues.addAll(hintValueContext.getHintShardingTableValue(each));
            }
        }
        return getShardingConditions(shardingValues);
    }
    
    private List<ShardingConditionValue> getShardingConditions(final Collection<Comparable<?>> shardingValue) {
        return shardingValue.isEmpty() ? Collections.emptyList() : Collections.singletonList(new ListShardingConditionValue<>("", logicTableName, shardingValue));
    }
    
    private List<ShardingConditionValue> getShardingValuesFromShardingConditions(final ShardingRule shardingRule, final Collection<String> shardingColumns, final ShardingCondition shardingCondition) {
        List<ShardingConditionValue> result = new ArrayList<>(shardingColumns.size());
        for (ShardingConditionValue each : shardingCondition.getValues()) {
            Optional<BindingTableRule> bindingTableRule = shardingRule.findBindingTableRule(each.getTableName());
            if ((logicTableName.equalsIgnoreCase(each.getTableName()) || bindingTableRule.isPresent() && bindingTableRule.get().hasLogicTable(logicTableName))
                    && new CaseInsensitiveSet<>(shardingColumns).contains(each.getColumnName())) {
                result.add(each);
            }
        }
        return result;
    }
    
    private Collection<DataNode> route0(final ShardingTable shardingTable,
                                        final ShardingStrategy databaseShardingStrategy, final List<ShardingConditionValue> databaseShardingValues,
                                        final ShardingStrategy tableShardingStrategy, final List<ShardingConditionValue> tableShardingValues) {
        Collection<String> routedDataSources = routeDataSources(shardingTable, databaseShardingStrategy, databaseShardingValues);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTables(shardingTable, each, tableShardingStrategy, tableShardingValues));
        }
        return result;
    }
    
    private Collection<String> routeDataSources(final ShardingTable shardingTable, final ShardingStrategy databaseShardingStrategy, final List<ShardingConditionValue> databaseShardingValues) {
        if (databaseShardingValues.isEmpty()) {
            return shardingTable.getActualDataSourceNames();
        }
        Collection<String> result = databaseShardingStrategy.doSharding(shardingTable.getActualDataSourceNames(), databaseShardingValues, shardingTable.getDataSourceDataNode(), props);
        ShardingSpherePreconditions.checkNotEmpty(result, () -> new NoShardingDatabaseRouteInfoException(shardingTable.getActualDataSourceNames(), databaseShardingValues));
        ShardingSpherePreconditions.checkState(shardingTable.getActualDataSourceNames().containsAll(result),
                () -> new MismatchedShardingDataSourceRouteInfoException(result, shardingTable.getActualDataSourceNames(), databaseShardingValues));
        return result;
    }
    
    private Collection<DataNode> routeTables(final ShardingTable shardingTable, final String routedDataSource,
                                             final ShardingStrategy tableShardingStrategy, final List<ShardingConditionValue> tableShardingValues) {
        Collection<String> availableTargetTables = shardingTable.getActualTableNames(routedDataSource);
        Collection<String> routedTables = tableShardingValues.isEmpty()
                ? availableTargetTables
                : tableShardingStrategy.doSharding(availableTargetTables, tableShardingValues, shardingTable.getTableDataNode(), props);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedTables) {
            result.add(new DataNode(routedDataSource, (String) null, each));
        }
        return result;
    }
    
    private ShardingStrategy createShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig, final Map<String, ShardingAlgorithm> shardingAlgorithms,
                                                    final String defaultShardingColumn) {
        return null == shardingStrategyConfig ? new NoneShardingStrategy()
                : ShardingStrategyFactory.newInstance(shardingStrategyConfig, shardingAlgorithms.get(shardingStrategyConfig.getShardingAlgorithmName()), defaultShardingColumn);
    }
}
