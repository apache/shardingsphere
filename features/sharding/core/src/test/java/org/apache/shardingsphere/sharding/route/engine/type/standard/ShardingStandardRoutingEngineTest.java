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
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.algorithm.ShardingRouteAlgorithmException;
import org.apache.shardingsphere.sharding.route.engine.condition.ShardingConditions;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.After;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingStandardRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @After
    public void tearDown() {
        HintManager.clear();
    }
    
    @Test
    public void assertRouteByNonConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order",
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)), mock(SQLStatementContext.class));
        RouteContext routeContext = standardRoutingEngine.route(createBasedShardingRule());
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
    public void assertRouteByShardingConditions() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order", createShardingConditions("t_order"), mock(SQLStatementContext.class));
        RouteContext routeContext = standardRoutingEngine.route(createBasedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_order_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_order"));
    }
    
    @Test(expected = ShardingRouteAlgorithmException.class)
    public void assertRouteByErrorShardingTableStrategy() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_order", createErrorShardingConditions("t_order"), mock(SQLStatementContext.class));
        standardRoutingEngine.route(createErrorShardingRule());
    }
    
    @Test
    public void assertRouteByHint() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_test",
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)), mock(SQLStatementContext.class));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_test", 1);
        hintManager.addTableShardingValue("t_hint_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(createHintShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDataSource() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test", createShardingConditions("t_hint_ds_test"), mock(SQLStatementContext.class));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_ds_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_ds_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintDataSourceOnly() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_ds_test",
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)), mock(SQLStatementContext.class));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_hint_ds_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(createMixedShardingRule());
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
    public void assertRouteByMixedWithHintTable() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test", createShardingConditions("t_hint_table_test"), mock(SQLStatementContext.class));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(createMixedShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_hint_table_test_1"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_hint_table_test"));
    }
    
    @Test
    public void assertRouteByMixedWithHintTableOnly() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_hint_table_test",
                new ShardingConditions(Collections.emptyList(), mock(SQLStatementContext.class), mock(ShardingRule.class)), mock(SQLStatementContext.class));
        HintManager hintManager = HintManager.getInstance();
        hintManager.addTableShardingValue("t_hint_table_test", 1);
        RouteContext routeContext = standardRoutingEngine.route(createMixedShardingRule());
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
    public void assertRouteByIntervalTableShardingStrategyOnly() {
        ShardingStandardRoutingEngine standardRoutingEngine = createShardingStandardRoutingEngine("t_interval_test",
                createIntervalShardingConditions("t_interval_test"), mock(SQLStatementContext.class));
        RouteContext routeContext = standardRoutingEngine.route(createIntervalTableShardingRule());
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getActualName(), is("t_interval_test_202101"));
        assertThat(routeUnits.get(0).getTableMappers().iterator().next().getLogicName(), is("t_interval_test"));
    }
    
    private ShardingStandardRoutingEngine createShardingStandardRoutingEngine(final String logicTableName, final ShardingConditions shardingConditions,
                                                                              final SQLStatementContext<?> sqlStatementContext) {
        return new ShardingStandardRoutingEngine(logicTableName, shardingConditions, sqlStatementContext, new ConfigurationProperties(new Properties()));
    }
}
