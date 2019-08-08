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

package org.apache.shardingsphere.core.route.type.standard;

import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.ComplexShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.InlineShardingStrategyConfiguration;
import org.apache.shardingsphere.api.config.sharding.strategy.StandardShardingStrategyConfiguration;
import org.apache.shardingsphere.core.optimize.encrypt.segment.condition.EncryptCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.condition.ShardingCondition;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.fixture.ComplexShardingAlgorithmFixture;
import org.apache.shardingsphere.core.route.fixture.PreciseShardingAlgorithmFixture;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.strategy.route.value.ListRouteValue;
import org.apache.shardingsphere.core.strategy.route.value.RouteValue;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardRoutingEngineTest {

    private SQLStatement sqlStatement;

    private String logicTableName;

    private List<EncryptCondition> encryptConditionList;

    private GroupBy groupBy;

    private OrderBy orderBy;

    private SelectItems selectItems;

    private Pagination pagination;

    @Before
    public void setEngineContext() {
        sqlStatement = new SelectStatement();
        logicTableName = "t_order";
        encryptConditionList = Collections.emptyList();
        groupBy = new GroupBy(Collections.<OrderByItem>emptyList(), 0);
        orderBy = new OrderBy(Collections.<OrderByItem>emptyList(), false);
        selectItems = new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(),
            Collections.<TableSegment>emptyList(), null);
        pagination = new Pagination(null, null, Collections.emptyList());
    }

    @Test
    public void assertRouteInlineStrategyWithShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        RouteValue shardingValue1 = new ListRouteValue<>("user_id", logicTableName, Collections.singleton(1L));
        RouteValue shardingValue2 = new ListRouteValue<>("order_id", logicTableName, Collections.singleton(1L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getRouteValues().add(shardingValue1);
        shardingCondition.getRouteValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }

    @Test
    public void assertRouteInlineStrategyWithoutShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("user_id", "ds_${user_id % 2}"));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new InlineShardingStrategyConfiguration("order_id", "t_order_${order_id % 2}"));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(4));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(2).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(2).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(3).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(3).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }

    @Test
    public void assertRouteComplexStrategyWithShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new ComplexShardingStrategyConfiguration("user_id", new ComplexShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new ComplexShardingStrategyConfiguration("order_id", new ComplexShardingAlgorithmFixture()));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        RouteValue shardingValue1 = new ListRouteValue<>("user_id", logicTableName, Collections.singleton(90L));
        RouteValue shardingValue2 = new ListRouteValue<>("order_id", logicTableName, Collections.singleton(100L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getRouteValues().add(shardingValue1);
        shardingCondition.getRouteValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }

    @Test
    public void assertRouteComplexStrategyWithoutShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new ComplexShardingStrategyConfiguration("user_id", new ComplexShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new ComplexShardingStrategyConfiguration("order_id", new ComplexShardingAlgorithmFixture()));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(4));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(2).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(2).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(3).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(3).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }

    @Test
    public void assertRouteStandardStrategyWithShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("user_id", new PreciseShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("order_id", new PreciseShardingAlgorithmFixture()));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        RouteValue shardingValue1 = new ListRouteValue<>("user_id", logicTableName, Collections.singleton(1L));
        RouteValue shardingValue2 = new ListRouteValue<>("order_id", logicTableName, Collections.singleton(2L));
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingCondition.getRouteValues().add(shardingValue1);
        shardingCondition.getRouteValues().add(shardingValue2);
        shardingConditions.add(shardingCondition);

        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }

    @Test
    public void assertRouteStandardStrategyWithoutShardingValues() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(
            new TableRuleConfiguration(logicTableName, "ds_${0..1}.t_order_${0..1}"));
        shardingRuleConfig.setDefaultDatabaseShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("user_id", new PreciseShardingAlgorithmFixture()));
        shardingRuleConfig.setDefaultTableShardingStrategyConfig(
            new StandardShardingStrategyConfiguration("order_id", new PreciseShardingAlgorithmFixture()));
        List<ShardingCondition> shardingConditions = new ArrayList<>();
        ShardingCondition shardingCondition = new ShardingCondition();
        shardingConditions.add(shardingCondition);
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        StandardRoutingEngine standardRoutingEngine = new StandardRoutingEngine(
            shardingRule, logicTableName,
            new ShardingSelectOptimizedStatement(sqlStatement, shardingConditions, encryptConditionList,
                groupBy, orderBy, selectItems, pagination));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(4));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(2).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(2).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
        assertThat(tableUnitList.get(3).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(3).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getLogicTableName(), is(logicTableName));
    }
}
