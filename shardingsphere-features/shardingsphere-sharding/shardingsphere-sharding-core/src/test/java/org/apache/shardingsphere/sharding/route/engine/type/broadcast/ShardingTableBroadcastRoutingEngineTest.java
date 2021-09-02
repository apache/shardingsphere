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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.ddl.DropIndexStatementContext;
import org.apache.shardingsphere.infra.binder.type.IndexAvailable;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.rule.ShardingTableRuleConfiguration;
import org.apache.shardingsphere.sharding.route.engine.fixture.AbstractRoutingEngineTest;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.sql.common.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.ddl.DropIndexStatement;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.junit.MockitoJUnitRunner;

import javax.sql.DataSource;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingTableBroadcastRoutingEngineTest extends AbstractRoutingEngineTest {
    
    @Test
    public void assertRouteForEmptyTable() {
        Collection<String> tableNames = Collections.emptyList();
        ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine = 
                new ShardingTableBroadcastRoutingEngine(mock(ShardingSphereSchema.class), createSQLStatementContext(tableNames), tableNames);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, createShardingRule(false));
        assertRouteUnitWithoutTables(routeContext);
    }
    
    @Test
    public void assertRouteForNormalTable() {
        Collection<String> tableNames = Collections.singletonList("t_order");
        ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine = 
                new ShardingTableBroadcastRoutingEngine(mock(ShardingSphereSchema.class), createSQLStatementContext(tableNames), tableNames);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, createShardingRule(false));
        assertThat(routeContext.getActualDataSourceNames().size(), is(2));
        assertThat(routeContext.getRouteUnits().size(), is(4));
        Iterator<RouteUnit> routeUnits = routeContext.getRouteUnits().iterator();
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_1");
    }
    
    @Test
    public void assertRouteForBroadcastTable() {
        Collection<String> tableNames = Collections.singletonList("t_order");
        ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine = 
                new ShardingTableBroadcastRoutingEngine(mock(ShardingSphereSchema.class), createSQLStatementContext(tableNames), tableNames);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, createShardingRule(true));
        assertThat(routeContext.getActualDataSourceNames().size(), is(2));
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> routeUnits = routeContext.getRouteUnits().iterator();
        assertRouteUnit(routeUnits.next(), "ds0", "t_order");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order");
    }
    
    @Test
    public void assertRouteForDropIndexStatement() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.getAllTableNames()).thenReturn(Collections.singleton("t_order"));
        when(schema.get(anyString()).getIndexes().containsKey(anyString())).thenReturn(true);
        IndexSegment segment = mock(IndexSegment.class, RETURNS_DEEP_STUBS);
        when(segment.getIdentifier().getValue()).thenReturn("t_order");
        SQLStatementContext<DropIndexStatement> sqlStatementContext = mock(DropIndexStatementContext.class, RETURNS_DEEP_STUBS);
        Collection<String> tableNames = Collections.emptyList();
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        when(((IndexAvailable) sqlStatementContext).getIndexes()).thenReturn(Collections.singletonList(segment));
        ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine = new ShardingTableBroadcastRoutingEngine(schema, sqlStatementContext, tableNames);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, createShardingRule(false));
        assertThat(routeContext.getActualDataSourceNames().size(), is(2));
        Iterator<RouteUnit> routeUnits = routeContext.getRouteUnits().iterator();
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds0", "t_order_1");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_0");
        assertRouteUnit(routeUnits.next(), "ds1", "t_order_1");
    }
    
    @Test
    public void assertRouteForDropIndexStatementDoNotFoundTables() {
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class, RETURNS_DEEP_STUBS);
        when(schema.get(anyString()).getIndexes().containsKey(anyString())).thenReturn(false);
        IndexSegment segment = mock(IndexSegment.class, RETURNS_DEEP_STUBS);
        when(segment.getIdentifier().getValue()).thenReturn("t_order");
        SQLStatementContext<DropIndexStatement> sqlStatementContext = mock(DropIndexStatementContext.class, RETURNS_DEEP_STUBS);
        Collection<String> tableNames = Collections.emptyList();
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(tableNames);
        ShardingTableBroadcastRoutingEngine shardingTableBroadcastRoutingEngine = new ShardingTableBroadcastRoutingEngine(schema, sqlStatementContext, tableNames);
        RouteContext routeContext = new RouteContext();
        shardingTableBroadcastRoutingEngine.route(routeContext, createShardingRule(false));
        assertRouteUnitWithoutTables(routeContext);
    }
    
    private ShardingRule createShardingRule(final boolean isContainBroadcastTable) {
        ShardingRuleConfiguration shardingRuleConfiguration = new ShardingRuleConfiguration();
        shardingRuleConfiguration.getTables().add(new ShardingTableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..1}"));
        if (isContainBroadcastTable) {
            shardingRuleConfiguration.getBroadcastTables().add("t_order");
        }
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("ds0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        dataSourceMap.put("ds1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return new ShardingRule(shardingRuleConfiguration, dataSourceMap);
    }

    private SQLStatementContext<?> createSQLStatementContext(final Collection<String> tableNames) {
        SQLStatementContext<?> result = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getTablesContext().getTableNames()).thenReturn(tableNames);
        return result;
    }
    
    private void assertRouteUnitWithoutTables(final RouteContext routeContext) {
        assertThat(routeContext.getActualDataSourceNames().size(), is(2));
        assertThat(routeContext.getRouteUnits().size(), is(2));
        Iterator<RouteUnit> routeUnits = routeContext.getRouteUnits().iterator();
        RouteUnit routeUnit0 = routeUnits.next();
        assertThat(routeUnit0.getDataSourceMapper().getActualName(), is("ds0"));
        assertThat(routeUnit0.getTableMappers().size(), is(1));
        assertThat(routeUnit0.getTableMappers().iterator().next(), is(new RouteMapper("", "")));
        RouteUnit routeUnit1 = routeUnits.next();
        assertThat(routeUnit1.getDataSourceMapper().getActualName(), is("ds1"));
        assertThat(routeUnit1.getTableMappers().size(), is(1));
        assertThat(routeUnit1.getTableMappers().iterator().next(), is(new RouteMapper("", "")));
    }
    
    private void assertRouteUnit(final RouteUnit routeUnit, final String dataSourceName, final String actualTableName) {
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(dataSourceName));
        assertThat(routeUnit.getTableMappers().size(), is(1));
        assertThat(routeUnit.getTableMappers().iterator().next(), is(new RouteMapper("t_order", actualTableName)));
    }
}
