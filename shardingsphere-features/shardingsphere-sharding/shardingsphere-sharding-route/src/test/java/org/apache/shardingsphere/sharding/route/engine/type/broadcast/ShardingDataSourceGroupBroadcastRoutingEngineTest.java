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
import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.TableRule;
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
    public void assertRoute() {
        List<List<String>> shards = new LinkedList<>();
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        List<TableRule> tableRules = mockTableRules(shards);
        when(shardingRule.getTableRules()).thenReturn(tableRules);
        RouteContext actual = new RouteContext();
        shardingDataSourceGroupBroadcastRoutingEngine.route(actual, shardingRule);
        assertThat(actual.getRouteUnits().size(), is(1));
        Iterator<RouteUnit> iterator = actual.getRouteUnits().iterator();
        assertThat(Arrays.asList("ds1", "ds2", "ds3"), hasItems(iterator.next().getDataSourceMapper().getActualName()));
    }
}
