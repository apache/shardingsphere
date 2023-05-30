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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintManager;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingRouteAlgorithmException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.fixture.ShardingRoutingEngineFixtureBuilder;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingStandardRoutingEngineTest {
    
    @AfterEach
    void tearDown() {
        HintManager.clear();
    }
    
    @Test
    void assertRouteByNonConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order",
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)), mock(SQLStatementContext.class), new HintValueContext());
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(4));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_0"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(2).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(2).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(2).getTableMappers().iterator().next().getActualName(), is("t_order_0"));
        assertThat(routeUnits.get(2).getTableMappers().iterator().next().getLogicName(), is("t_order"));
        assertThat(routeUnits.get(3).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(3).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(3).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(3).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    void assertRouteByShardingConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order",
                ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_order"), mock(SQLStatementContext.class), new HintValueContext());
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test
    void assertRouteByErrorShardingTableStrategy() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order", ShardingRoutingEngineFixtureBuilder.createErrorShardingConditions("t_order"),
                mock(SQLStatementContext.class), new HintValueContext());
        assertThrows(ShardingRouteAlgorithmException.class, () -> standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createErrorShardingRule()));
    }
    
    @Test
    void assertRouteByHint() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_hint_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_test",
                new ShardingConditions(Collections.emptyList(), sqlStatementContext, mock(ShardingRule.class)), sqlStatementContext, new HintValueContext());
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createHintShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_test"));
    }
    
    @Test
    void assertRouteByMixedWithHintDataSource() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_hint_ds_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test",
                ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_hint_ds_test"), sqlStatementContext, new HintValueContext());
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_ds_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_ds_test"));
    }
    
    @Test
    void assertRouteByMixedWithHintDataSourceOnly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_hint_ds_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test",
                new ShardingConditions(Collections.emptyList(), sqlStatementContext, mock(ShardingRule.class)), sqlStatementContext, new HintValueContext());
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_ds_test_0"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_ds_test"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getActualName(), is("t_hint_ds_test_1"));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getLogicName(), is("t_hint_ds_test"));
    }
    
    @Test
    void assertRouteByMixedWithHintTable() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_hint_table_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test",
                ShardingRoutingEngineFixtureBuilder.createShardingConditions("t_hint_table_test"), sqlStatementContext, new HintValueContext());
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_table_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_table_test"));
    }
    
    @Test
    void assertRouteByMixedWithHintTableOnly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_hint_table_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test",
                new ShardingConditions(Collections.emptyList(), sqlStatementContext, mock(ShardingRule.class)), sqlStatementContext, new HintValueContext());
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_table_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_table_test"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getActualName(), is("t_hint_table_test_1"));
        assertThat(routeUnits.get(1).getTableMappers().iterator().next().getLogicName(), is("t_hint_table_test"));
    }
    
    @Test
    void assertRouteByIntervalTableShardingStrategyOnly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("t_interval_test"));
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_interval_test",
                ShardingRoutingEngineFixtureBuilder.createIntervalShardingConditions("t_interval_test"), sqlStatementContext, new HintValueContext());
        RouteContext routeContext = standardRoutingEngine.route(ShardingRoutingEngineFixtureBuilder.createIntervalTableShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_interval_test_202101"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_interval_test"));
    }
    
    private ShardingStandardRoutingEngine createShardingStandardRoutingEngine(final String logicTableName, final ShardingConditions shardingConditions,
                                                                              final SQLStatementContext sqlStatementContext, final HintValueContext hintValueContext) {
        return new ShardingStandardRoutingEngine(logicTableName, shardingConditions, sqlStatementContext, hintValueContext, new ConfigurationProperties(new Properties()));
    }
}
