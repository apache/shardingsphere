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

package org.apache.shardingsphere.singletable.route;

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.singletable.config.SingleTableRuleConfiguration;
import org.apache.shardingsphere.singletable.rule.SingleTableRule;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class SingleTableRouteEngineTest {
    
    @Test
    public void assertRouteInSameDataSource() {
        SingleTableRouteEngine singleTableRouteEngine = new SingleTableRouteEngine(Arrays.asList("t_order", "t_order_item"), null);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class),
                createDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        singleTableRule.getSingleTableDataNodes().put("t_order", Collections.singletonList(new DataNode("ds_0", "t_order")));
        singleTableRule.getSingleTableDataNodes().put("t_order_item", Collections.singletonList(new DataNode("ds_0", "t_order_item")));
        RouteContext routeContext = new RouteContext();
        singleTableRouteEngine.route(routeContext, singleTableRule);
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
        assertFalse(routeContext.isFederated());
    }
    
    @Test
    public void assertRouteInDifferentDataSource() {
        SingleTableRouteEngine singleTableRouteEngine = new SingleTableRouteEngine(Arrays.asList("t_order", "t_order_item"), null);
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class), 
                createDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        singleTableRule.getSingleTableDataNodes().put("t_order", Collections.singletonList(new DataNode("ds_0", "t_order")));
        singleTableRule.getSingleTableDataNodes().put("t_order_item", Collections.singletonList(new DataNode("ds_1", "t_order_item")));
        RouteContext routeContext = new RouteContext();
        singleTableRouteEngine.route(routeContext, singleTableRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(2));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        RouteMapper tableMapper0 = routeUnits.get(0).getTableMappers().iterator().next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
        assertThat(routeUnits.get(1).getDataSourceMapper().getActualName(), is("ds_1"));
        assertThat(routeUnits.get(1).getTableMappers().size(), is(1));
        RouteMapper tableMapper1 = routeUnits.get(1).getTableMappers().iterator().next();
        assertThat(tableMapper1.getActualName(), is("t_order_item"));
        assertThat(tableMapper1.getLogicName(), is("t_order_item"));
        assertTrue(routeContext.isFederated());
    }
    
    @Test
    public void assertRouteWithoutSingleTableRule() {
        SingleTableRouteEngine singleTableRouteEngine = new SingleTableRouteEngine(Arrays.asList("t_order", "t_order_item"), new MySQLCreateTableStatement());
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration(), mock(DatabaseType.class), 
                createDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        singleTableRouteEngine.route(routeContext, singleTableRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
    }
    
    @Test
    public void assertRouteWithDefaultSingleTableRule() {
        SingleTableRouteEngine singleTableRouteEngine = new SingleTableRouteEngine(Arrays.asList("t_order", "t_order_item"), new MySQLCreateTableStatement());
        SingleTableRule singleTableRule = new SingleTableRule(new SingleTableRuleConfiguration("ds_0"), mock(DatabaseType.class), 
                createDataSourceMap(), Collections.emptyList(), new ConfigurationProperties(new Properties()));
        RouteContext routeContext = new RouteContext();
        singleTableRouteEngine.route(routeContext, singleTableRule);
        List<RouteUnit> routeUnits = new ArrayList<>(routeContext.getRouteUnits());
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("ds_0"));
        assertThat(routeUnits.get(0).getTableMappers().size(), is(1));
        Iterator<RouteMapper> tableMappers = routeUnits.get(0).getTableMappers().iterator();
        RouteMapper tableMapper0 = tableMappers.next();
        assertThat(tableMapper0.getActualName(), is("t_order"));
        assertThat(tableMapper0.getLogicName(), is("t_order"));
    }
    
    private Map<String, DataSource> createDataSourceMap() {
        Map<String, DataSource> result = new HashMap<>(2, 1);
        result.put("ds_0", mock(DataSource.class, RETURNS_DEEP_STUBS));
        result.put("ds_1", mock(DataSource.class, RETURNS_DEEP_STUBS));
        return result;
    }
}
