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

package org.apache.shardingsphere.sharding.route.engine.type.complex;

import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sharding.route.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sql.parser.relation.segment.select.groupby.GroupByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.sql.parser.relation.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.Projection;
import org.apache.shardingsphere.sql.parser.relation.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.relation.statement.impl.SelectSQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.underlying.common.constant.properties.ShardingSphereProperties;
import org.apache.shardingsphere.underlying.common.exception.ShardingSphereException;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.junit.Test;

import java.util.Collections;
import java.util.Properties;
import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingComplexRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRoutingForBindingTables() {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(),
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(null, null, Collections.emptyList()));
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Arrays.asList("t_order", "t_order_item"), selectSQLStatementContext,
                createShardingConditions("t_order"), new ShardingSphereProperties(new Properties()));
        RouteResult routeResult = complexRoutingEngine.route(createBindingShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test
    public void assertRoutingForShardingTableJoinBroadcastTable() {
        SelectSQLStatementContext selectSQLStatementContext = new SelectSQLStatementContext(new SelectStatement(),
                new GroupByContext(Collections.<OrderByItem>emptyList(), 0), new OrderByContext(Collections.<OrderByItem>emptyList(), false), 
                new ProjectionsContext(0, 0, false, Collections.<Projection>emptyList(), Collections.<String>emptyList()),
                new PaginationContext(null, null, Collections.emptyList()));
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Arrays.asList("t_order", "t_config"), selectSQLStatementContext,
                createShardingConditions("t_order"), new ShardingSphereProperties(new Properties()));
        RouteResult routeResult = complexRoutingEngine.route(createBroadcastShardingRule());
        List<RouteUnit> tableUnitList = new ArrayList<>(routeResult.getRouteUnits());
        assertThat(routeResult, instanceOf(RouteResult.class));
        assertThat(routeResult.getRouteUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getActualDataSourceName(), is("ds_1"));
        assertThat(tableUnitList.get(0).getTableUnits().size(), is(1));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getActualTableName(), is("t_order_1"));
        assertThat(tableUnitList.get(0).getTableUnits().get(0).getLogicTableName(), is("t_order"));
    }
    
    @Test(expected = ShardingSphereException.class)
    public void assertRoutingForNonLogicTable() {
        ShardingComplexRoutingEngine complexRoutingEngine = new ShardingComplexRoutingEngine(Collections.<String>emptyList(), null,
                createShardingConditions("t_order"), new ShardingSphereProperties(new Properties()));
        complexRoutingEngine.route(mock(ShardingRule.class));
    }
}
