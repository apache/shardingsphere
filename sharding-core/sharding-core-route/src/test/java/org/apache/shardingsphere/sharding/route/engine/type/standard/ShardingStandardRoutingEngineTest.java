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

import org.apache.shardingsphere.api.hint.HintManager;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingCondition;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShardingStandardRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @After
    public void tearDown() {
        HintManager.clear();
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertRouteByUnsupported() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(new InsertStatement());
        TablesContext tablesContext = mock(TablesContext.class);
        when(tablesContext.isSingleTable()).thenReturn(false);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        ShardingStandardRoutingEngine standardRoutingEngine = new ShardingStandardRoutingEngine(null, sqlStatementContext, null, new ShardingSphereProperties(new Properties()));
        standardRoutingEngine.route(mock(ShardingRule.class));
    }
    
    @Test
    public void assertRouteByNonConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        RouteResult routeResult = standardRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(4));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(1).getActualDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(2).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(2).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getActualTableName(), is("t_order_0"));
        assertThat(tableUnitList.get(2).getTableUnits().get(0).getLogicTableName(), is("t_order"));
        assertThat(tableUnitList.get(3).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(3).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(3).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRouteByShardingConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order", createShardingConditions("t_order"));
        RouteResult routeResult = standardRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRouteByHint() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        RouteResult routeResult = standardRoutingEngine.route(createHintShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDatasource() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test", createShardingConditions("t_hint_ds_test"));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteResult routeResult = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDatasourceOnly() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteResult routeResult = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(2));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_0"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
        assertThat(tableUnitList.get(1).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_hint_ds_test_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_hint_ds_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintTable() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test", createShardingConditions("t_hint_table_test"));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteResult routeResult = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintTableOnly() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test", new ShardingConditions(Collections.<ShardingCondition>emptyList()));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteResult routeResult = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(2));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_0"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
        assertThat(tableUnitList.get(1).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(1).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getActualTableName(), is("t_hint_table_test_1"));
        assertThat(tableUnitList.get(1).getTableUnits().get(0).getLogicTableName(), is("t_hint_table_test"));
    }
    
    private ShardingStandardRoutingEngine createShardingStandardRoutingEngine(final String logicTableName, final ShardingConditions shardingConditions) {
        return new ShardingStandardRoutingEngine(logicTableName, new SelectSQLStatementContext(new SelectStatement(),
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false),
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(null, null, Collections.emptyList())), shardingConditions, new ShardingSphereProperties(new Properties()));
    }
}
