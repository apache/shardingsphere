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

import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.groupby.GroupBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderBy;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.optimize.sharding.statement.dml.ShardingSelectOptimizedStatement;
import org.apache.shardingsphere.core.parse.sql.segment.generic.TableSegment;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingCondition;
import org.apache.shardingsphere.core.route.router.sharding.condition.ShardingConditions;
import org.apache.shardingsphere.core.route.type.RoutingResult;
import org.apache.shardingsphere.core.route.type.RoutingUnit;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @After
    public void tearDown() {
        HintManager.clear();
    }
    
    @Test
    public void assertRouteByNonConditions() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createBasedShardingRule(), "t_order", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(4));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(2).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(2).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(3).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(3).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRouteByShardingConditions() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createBasedShardingRule(), "t_order", createShardingConditions("t_order"));
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRouteByHint() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createHintShardingRule(), "t_hint_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDatasource() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createMixedShardingRule(), "t_hint_ds_test", createShardingConditions("t_hint_ds_test"));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDatasourceOnly() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createMixedShardingRule(), "t_hint_ds_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(2));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintTable() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createMixedShardingRule(), "t_hint_table_test", createShardingConditions("t_hint_table_test"));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintTableOnly() {
        StandardRoutingEngine standardRoutingEngine = createStandardRoutingEngine(createMixedShardingRule(), "t_hint_table_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RoutingResult routingResult = standardRoutingEngine.route();
        List<RoutingUnit> tableUnitList = new ArrayList<>(routingResult.getRoutingUnits());
        assertThat(routingResult, instanceOf(RoutingResult.class));
        assertThat(routingResult.getRoutingUnits().size(), is(2));
        assertThat(tableUnitList.get(0).getDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
        assertThat(tableUnitList.get(1).getDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
    }
    
    private StandardRoutingEngine createStandardRoutingEngine(final ShardingRule shardingRule, final String logicTableName, final ShardingConditions shardingConditions) {
        return new StandardRoutingEngine(shardingRule, logicTableName, new ShardingSelectOptimizedStatement(new SelectStatement(),
            new GroupBy(Collections.<OrderByItem>emptyList(), 0), new OrderBy(Collections.<OrderByItem>emptyList(), false),
            new SelectItems(0, 0, false, Collections.<SelectItem>emptyList(), Collections.<TableSegment>emptyList(), null),
            new Pagination(null, null, Collections.emptyList())), shardingConditions);
    }
}
