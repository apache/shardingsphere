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

import com.google.common.collect.Maps;
import org.apache.shardingsphere.core.rule.DataNode;
import org.apache.shardingsphere.core.rule.ShardingDataSourceNames;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.apache.shardingsphere.core.rule.TableRule;
import org.apache.shardingsphere.underlying.route.context.RouteResult;
import org.apache.shardingsphere.underlying.route.context.RouteUnit;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingDataSourceGroupBroadcastRoutingEngineTest {
    
    private final ShardingDataSourceGroupBroadcastRoutingEngine shardingDataSourceGroupBroadcastRoutingEngine = new ShardingDataSourceGroupBroadcastRoutingEngine();
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private ShardingDataSourceNames shardingDataSourceNames;
    
    @Before
    public void setUp() {
        when(shardingRule.getShardingDataSourceNames()).thenReturn(shardingDataSourceNames);
    }
    
    private List<TableRule> mockTableRules(final List<List<String>> shards) {
        List<TableRule> result = new LinkedList<>();
        for (List<String> each : shards) {
            result.add(mockTableRule(each));
        }
        return result;
    }
    
    private TableRule mockTableRule(final List<String> dataSources) {
        TableRule result = mock(TableRule.class);
        Map<String, List<DataNode>> dataNodeGroups = Maps.newHashMap();
        for (String each : dataSources) {
            dataNodeGroups.put(each, null);
        }
        when(result.getDataNodeGroups()).thenReturn(dataNodeGroups);
        return result;
    }
    
    @Test
    public void assertRouteWithDefaultDataSource() {
        List<List<String>> shards = new LinkedList<>();
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds3", "ds4", "ds5"));
        shards.add(Arrays.asList("ds9", "ds10", "ds11"));
        List<TableRule> tableRules = mockTableRules(shards);
        when(shardingRule.getTableRules()).thenReturn(tableRules);
        when(shardingDataSourceNames.getDefaultDataSourceName()).thenReturn("default");
        RouteResult actual = shardingDataSourceGroupBroadcastRoutingEngine.route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(3));
        Iterator<RouteUnit> iterator = actual.getRouteUnits().iterator();
        assertThat(iterator.next().getActualDataSourceName(), is("ds3"));
        assertThat(Arrays.asList("ds9", "ds10", "ds11"), hasItems(iterator.next().getActualDataSourceName()));
        assertThat(iterator.next().getActualDataSourceName(), is("default"));
    }
    
    @Test
    public void assertRouteWithoutDefaultDataSource() {
        List<List<String>> shards = new LinkedList<>();
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        List<TableRule> tableRules = mockTableRules(shards);
        when(shardingRule.getTableRules()).thenReturn(tableRules);
        RouteResult actual = shardingDataSourceGroupBroadcastRoutingEngine.route(shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
        Iterator<RouteUnit> iterator = actual.getRouteUnits().iterator();
        assertThat(Arrays.asList("ds1", "ds2", "ds3"), hasItems(iterator.next().getActualDataSourceName()));
    }
}
