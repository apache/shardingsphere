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
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.TableRuleConfiguration;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.relation.segment.table.TablesContext;
import org.apache.shardingsphere.sql.parser.relation.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.sql.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DDLStatement;
import org.apache.shardingsphere.sql.parser.sql.statement.ddl.DropIndexStatement;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetaData;
import org.apache.shardingsphere.underlying.common.metadata.table.TableMetas;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.apache.shardingsphere.underlying.route.context.TableUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingTableBroadcastRoutingEngineTest {
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private TablesContext tablesContext;
    
    @Mock
    private TableMetas tableMetas;
    
    @Mock
    private TableMetaData tableMetaData;
    
    private ShardingTableBroadcastRoutingEngine tableBroadcastRoutingEngine;
    
    private ShardingRule shardingRule;
    
    @Before
    public void setUp() {
        TableRuleConfiguration tableRuleConfig = new TableRuleConfiguration("t_order", "ds${0..1}.t_order_${0..2}");
        ShardingRuleConfiguration shardingRuleConfig = new ShardingRuleConfiguration();
        shardingRuleConfig.getTableRuleConfigs().add(tableRuleConfig);
        when(sqlStatementContext.getTablesContext()).thenReturn(tablesContext);
        when(tablesContext.getTableNames()).thenReturn(Lists.newArrayList("t_order"));
        when(tableMetas.getAllTableNames()).thenReturn(Lists.newArrayList("t_order"));
        when(tableMetas.get("t_order")).thenReturn(tableMetaData);
        when(tableMetaData.containsIndex("index_name")).thenReturn(true);
        tableBroadcastRoutingEngine = new ShardingTableBroadcastRoutingEngine(tableMetas, sqlStatementContext);
        shardingRule = new ShardingRule(shardingRuleConfig, Arrays.asList("ds0", "ds1"));
    }
    
    @Test
    public void assertRouteForNormalDDL() {
        DDLStatement ddlStatement = mock(DDLStatement.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(ddlStatement);
        RouteResult actual = tableBroadcastRoutingEngine.route(shardingRule);
        assertRouteResult(actual);
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertRouteForNonExistDropIndex() {
        DropIndexStatement indexStatement = mock(DropIndexStatement.class);
        IndexSegment indexSegment = mock(IndexSegment.class);
        when(indexSegment.getName()).thenReturn("no_index");
        when(indexStatement.getIndexes()).thenReturn(Lists.newArrayList(indexSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(indexStatement);
        tableBroadcastRoutingEngine.route(shardingRule);
    }
    
    @Test
    public void assertRouteForDropIndex() {
        DropIndexStatement indexStatement = mock(DropIndexStatement.class);
        IndexSegment indexSegment = mock(IndexSegment.class);
        when(indexSegment.getName()).thenReturn("index_name");
        when(indexStatement.getIndexes()).thenReturn(Lists.newArrayList(indexSegment));
        when(sqlStatementContext.getSqlStatement()).thenReturn(indexStatement);
        RouteResult actual = tableBroadcastRoutingEngine.route(shardingRule);
        assertRouteResult(actual);
    }
    
    private void assertRouteResult(final RouteResult actual) {
        assertThat(actual.getDataSourceNames().size(), is(2));
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
        assertThat(routeUnit.getActualDataSourceName(), is(dataSourceName));
        assertThat(routeUnit.getTableUnits().size(), is(1));
        assertThat(routeUnit.getTableUnits().get(0), is(new TableUnit("t_order", actualTableName)));
    }
}
