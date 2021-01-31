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

package org.apache.shardingsphere.sharding.route.engine.type.hint;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import com.google.common.base.Preconditions;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.strategy.sharding.ShardingStrategyConfiguration;
import org.apache.shardingsphere.sharding.route.engine.condition.generator.ConditionValue;
import org.apache.shardingsphere.sharding.route.engine.condition.value.ListShardingConditionValue;
import org.apache.shardingsphere.sharding.route.engine.type.ShardingRouteEngine;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategy;
import org.apache.shardingsphere.sharding.route.strategy.ShardingStrategyFactory;
import org.apache.shardingsphere.sharding.route.strategy.type.none.NoneShardingStrategy;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
import org.apache.shardingsphere.sharding.spi.ShardingAlgorithm;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.hint.ShardingHintValueSegment;

/**
 * ShardingSqlHintRoutingEngine.
 */

@RequiredArgsConstructor
public class ShardingHintRoutingEngine implements ShardingRouteEngine {

    private final String logicTableName;

    private final ConfigurationProperties properties;

    private final SelectStatementContext selectStatementContext;

    @Override
    public void route(final RouteContext routeContext, final ShardingRule shardingRule) {
        Collection<DataNode> dataNodes = getDataNodes(shardingRule, shardingRule.getTableRule(logicTableName), selectStatementContext);
        for (DataNode each : dataNodes) {
            routeContext.getRouteUnits().add(
                    new RouteUnit(new RouteMapper(each.getDataSourceName(), each.getDataSourceName()), Collections.singletonList(new RouteMapper(logicTableName, each.getTableName()))));
        }

    }

    private Collection<DataNode> route(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy, final ShardingHintValueSegment shardingDatabaseHintValueSegment,
                                       final ShardingStrategy tableShardingStrategy, final ShardingHintValueSegment shardingTableHintValueSegment) {
        Collection<String> routedDataSources = routeDataSourcesByHint(tableRule, databaseShardingStrategy, shardingDatabaseHintValueSegment);
        Collection<DataNode> result = new LinkedList<>();
        for (String each : routedDataSources) {
            result.addAll(routeTablesByHint(tableRule, each, tableShardingStrategy, shardingTableHintValueSegment));
        }
        return result;
    }

    private Collection<DataNode> getDataNodes(final ShardingRule shardingRule, final TableRule tableRule, final SelectStatementContext selectStatementContext) {
        ShardingStrategy databaseShardingStrategy = createShardingStrategy(shardingRule.getDatabaseShardingStrategyConfiguration(tableRule), shardingRule.getShardingAlgorithms());
        ShardingStrategy tableShardingStrategy = createShardingStrategy(shardingRule.getTableShardingStrategyConfiguration(tableRule), shardingRule.getShardingAlgorithms());
        return route(tableRule, databaseShardingStrategy, selectStatementContext.getHintContext().getShardingHintSegment().getShardingDatabaseHintValueSegment(),
                tableShardingStrategy, selectStatementContext.getHintContext().getShardingHintSegment().getShardingTableHintValueSegment());
    }

    private Collection<String> routeDataSourcesByHint(final TableRule tableRule, final ShardingStrategy databaseShardingStrategy, final ShardingHintValueSegment shardingDatabaseHintValueSegment) {
        Collection<String> datasourceNames = tableRule.getActualDatasourceNames();
        if (shardingDatabaseHintValueSegment != null) {
            List<ExpressionSegment> databaseHintValueSegmentValues = shardingDatabaseHintValueSegment.getValues();
            ListShardingConditionValue<Comparable<?>> listShardingValue = getListShardingValueFromHint(databaseHintValueSegmentValues, databaseShardingStrategy);
            datasourceNames = new LinkedHashSet<>(databaseShardingStrategy.doSharding(datasourceNames, Collections.singletonList(listShardingValue), properties));
        } 
        Preconditions.checkState(!datasourceNames.isEmpty(), "no database route info");
        Preconditions.checkState(tableRule.getActualDatasourceNames().containsAll(datasourceNames),
                "Some routed data sources do not belong to configured data sources. routed data sources: `%s`, configured data sources: `%s`", datasourceNames, tableRule.getActualDatasourceNames());
        return datasourceNames;
    }

    private ListShardingConditionValue<Comparable<?>> getListShardingValueFromHint(final List<ExpressionSegment> databaseHintValueSegmentValues, final ShardingStrategy shardingStrategy) {
        Collection<Comparable<?>> shardingConditionValues = new LinkedHashSet<>();
        for (ExpressionSegment expressionSegment : databaseHintValueSegmentValues) {
            new ConditionValue(expressionSegment, Collections.emptyList()).getValue().ifPresent(shardingConditionValues::add);
        }
        return new ListShardingConditionValue<>(shardingStrategy.getShardingColumns().iterator().next(), logicTableName, shardingConditionValues);
    }

    private Collection<? extends DataNode> routeTablesByHint(final TableRule tableRule, final String routedDataSource,
                                                             final ShardingStrategy tableShardingStrategy, final ShardingHintValueSegment shardingTableHintValueSegment) {
        Collection<String> availableTargetTables = tableRule.getActualTableNames(routedDataSource);
        if (shardingTableHintValueSegment != null) {
            ListShardingConditionValue<Comparable<?>> listShardingValue = getListShardingValueFromHint(shardingTableHintValueSegment.getValues(), tableShardingStrategy);
            availableTargetTables = tableShardingStrategy.doSharding(availableTargetTables, Collections.singletonList(listShardingValue), properties);
        }
        Collection<DataNode> result = new LinkedList<>();
        for (String each : availableTargetTables) {
            result.add(new DataNode(routedDataSource, each));
        }
        return result;
    }

    private ShardingStrategy createShardingStrategy(final ShardingStrategyConfiguration shardingStrategyConfig, final Map<String, ShardingAlgorithm> shardingAlgorithms) {
        return null == shardingStrategyConfig ? new NoneShardingStrategy()
                : ShardingStrategyFactory.newInstance(shardingStrategyConfig, shardingAlgorithms.get(shardingStrategyConfig.getShardingAlgorithmName()));
    }

}
