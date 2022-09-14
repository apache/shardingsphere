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

package org.apache.shardingsphere.sharding.route.engine.type.unicast;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.exception.metadata.ShardingRuleNotFoundException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;

public final class ShardingUnicastRoutingEngineTest {
    
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds_${0..1}.t_order_${0..2}"));
        shardingRuleConfig.getBroadcastTables().add("t_config");
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1", "ds_2"), mock(InstanceContext.class));
    }
    
    @Test
    public void assertRoutingForShardingTable() {
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), Collections.singleton("t_order")).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
        assertFalse("ds_2".equalsIgnoreCase(actual.getRouteUnits().iterator().next().getDataSourceMapper().getLogicName()));
    }
    
    @Test
    public void assertRoutingForBroadcastTable() {
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), Collections.singleton("t_config")).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    public void assertRoutingForNoTable() {
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), Collections.emptyList()).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    public void assertRoutingForShardingTableAndBroadcastTable() {
        Set<String> tables = new HashSet<>();
        tables.add("t_order");
        tables.add("t_config");
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), tables).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test(expected = ShardingRuleNotFoundException.class)
    public void assertRouteForWithNoIntersection() {
        Set<String> tables = new HashSet<>(3, 1);
        tables.add("t_order");
        tables.add("t_config");
        tables.add("t_product");
        new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), tables).route(shardingRule);
    }
    
    @Test
    public void assertRoutingForTableWithoutTableRule() {
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(SQLStatementContext.class), Collections.singleton("t_other")).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
    }
    
    @Test
    public void assertRoutingForBroadcastTableWithCursorStatement() {
        RouteContext actual = new ShardingUnicastRoutingEngine(mock(CursorStatementContext.class), Collections.singleton("t_config")).route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
        assertThat(actual.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is("ds_0"));
    }
}
