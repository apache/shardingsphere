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

package org.apache.shardingsphere.sharding.route.engine.type.broadcast;

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingTableBroadcastRoutingEngineTest {
    
    private ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine;
    
    private ShardingRule shardingRule;

    private void setUp(final String sqlStateTableName, final boolean bContainBroadCastTable) {
        shardingTableBroadcastRoutingEngine = new ShardingTableBroadcastRoutingEngine(mock(ShardingSphereSchema.class), mockSQLStatementContext(sqlStateTableName));
        shardingRule = new ShardingRule(createShardingRuleConfiguration(bContainBroadCastTable), mock(DatabaseType.class), createDataSourceMap());
    }

    private SQLStatementContext<?> mockSQLStatementContext(final String sqlStateTableName) {
        SQLStatementContext<?> result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(sqlStateTableName.isEmpty() ? Lists.newArrayList() : Lists.newArrayList(sqlStateTableName));
        return result;
    }
    
    private ShardingRuleConfiguration createShardingRuleConfiguration(final boolean bContainBroadCastTable) {
        ShardingRuleConfiguration result = new ShardingRuleConfiguration();
        result.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}"));
        if (!bContainBroadCastTable) {
            result.getBroadcastTables().add("t_order");
        }
        return result;
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
    
    @Test
    public void assertRouteForNormalDDLOfEmptyList() {
        setUp("", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContextOfEmptyList(routeContext);
    }
    
    @Test
    public void assertRouteForNonExistDropIndexOfEmptyList() {
        setUp("", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
    }
    
    @Test
    public void assertRouteForDropIndexOfEmptyList() {
        setUp("", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContextOfEmptyList(routeContext);
    }

    private void assertRouteUnitOfEmptyList(final RouteUnit routeUnit, final String dataSourceName, final String actualTableName) {
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(dataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next(), is(new RouteMapper("", actualTableName)));
    }

    private void assertRouteContextOfEmptyList(final RouteContext actual) {
        assertThat(actual.getActualDataSourceNames().size(), is(2));
        assertThat(actual.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> routeUnits = actual.getRouteUnits().iterator();
        assertRouteUnitOfEmptyList(routeUnits.next(), "ds0", "");
        assertRouteUnitOfEmptyList(routeUnits.next(), "ds1", "");
    }

    @Test
    public void assertRouteForNormalDDL() {
        setUp("t_order", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContext(routeContext);
    }

    @Test
    public void assertRouteForNonExistDropIndex() {
        setUp("t_order", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
    }

    @Test
    public void assertRouteForDropIndex() {
        setUp("t_order", true);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContext(routeContext);
    }

    private void assertRouteContext(final RouteContext actual) {
        assertThat(actual.getActualDataSourceNames().size(), is(2));
        assertThat(actual.getRouteUnits().size(), is(6));
        Iterator<RouteUnit> routeUnits = actual.getRouteUnits().iterator();
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_2");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_2");
    }

    private void assertRouteUnit(final RouteUnit routeUnit, final String dataSourceName, final String actualTableName) {
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(dataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next(), is(new RouteMapper("t_order", actualTableName)));
    }

    @Test
    public void assertRouteForNormalDDLNoContain() {
        setUp("t_order", false);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContextOfEmptyListNoContain(routeContext);
    }

    @Test
    public void assertRouteForNonExistDropIndexNoContain() {
        setUp("t_order", false);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
    }

    @Test
    public void assertRouteForDropIndexNoContain() {
        setUp("t_order", false);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertRouteContextOfEmptyListNoContain(routeContext);
    }

    private void assertRouteUnitOfEmptyListNoContain(final RouteUnit routeUnit, final String dataSourceName, final String actualTableName) {
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(dataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next(), is(new RouteMapper("t_order", actualTableName)));
    }

    private void assertRouteContextOfEmptyListNoContain(final RouteContext actual) {
        assertThat(actual.getActualDataSourceNames().size(), is(2));
        assertThat(actual.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> routeUnits = actual.getRouteUnits().iterator();
        assertRouteUnitOfEmptyListNoContain(routeUnits.next(), "ds0", "t_order");
        assertRouteUnitOfEmptyListNoContain(routeUnits.next(), "ds1", "t_order");
    }
}
