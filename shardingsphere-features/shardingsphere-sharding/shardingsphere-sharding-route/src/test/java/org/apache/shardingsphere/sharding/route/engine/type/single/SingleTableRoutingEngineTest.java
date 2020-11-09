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

package org.apache.shardingsphere.sharding.route.engine.type.single;

import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalSchemaMetaData;
import org.apache.shardingsphere.infra.metadata.schema.model.physical.PhysicalTableMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class SingleTableRoutingEngineTest {
    
    @Test
    public void assertRoute() {
        SingleTableRoutingEngine singleTableRoutingEngine = new SingleTableRoutingEngine(Arrays.asList("t_order", "t_order_item"), createShardingSphereSchema(), null);
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        RouteContext routeContext = new RouteContext();
        singleTableRoutingEngine.route(routeContext, shardingRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(2));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
        RouteMapper tableMapper1 = tableMappers.next();
        assertThat(tableMapper1.getActualName(), is("t_order_item"));
        assertThat(tableMapper1.getLogicName(), is("t_order_item"));
    }
    
    @Test
    public void assertRouteWithoutShardingRule() {
        SingleTableRoutingEngine singleTableRoutingEngine = new SingleTableRoutingEngine(Arrays.asList("t_order", "t_order_item"), createShardingSphereSchema(), new MySQLCreateTableStatement());
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        ShardingRule shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds_0", "ds_1"));
        RouteContext routeContext = new RouteContext();
        singleTableRoutingEngine.route(routeContext, shardingRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(2));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
        RouteMapper tableMapper1 = tableMappers.next();
        assertThat(tableMapper1.getActualName(), is("t_order_item"));
        assertThat(tableMapper1.getLogicName(), is("t_order_item"));
    }
    
    private PhysicalSchemaMetaData createShardingSphereSchema() {
        Map<String, PhysicalTableMetaData> tables = new HashMap<>(2, 1);
        tables.put("t_order", createTableMetaData());
        tables.put("t_order_item", createTableMetaData());
        return new PhysicalSchemaMetaData(tables);
    }
    
    private PhysicalTableMetaData createTableMetaData() {
        PhysicalTableMetaData result = new PhysicalTableMetaData();
        result.getAddressingDataSources().add("ds_0");
        return result;
    }
}
