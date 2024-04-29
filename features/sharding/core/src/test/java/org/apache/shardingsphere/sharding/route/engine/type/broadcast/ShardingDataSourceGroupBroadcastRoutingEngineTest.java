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

import org.apache.shardingsphere.infra.datanode.DataNode;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sharding.rule.ShardingTable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.hasItems;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ShardingDataSourceGroupBroadcastRoutingEngineTest {
    
    private final ShardingDataSourceGroupBroadcastRoutingEngine shardingDataSourceGroupBroadcastRoutingEngine = new ShardingDataSourceGroupBroadcastRoutingEngine();
    
    @Mock
    private ShardingRule shardingRule;
    
    private Map<String, ShardingTable> mockShardingTables(final List<List<String>> shards) {
        Map<String, ShardingTable> result = new LinkedHashMap<>();
        int index = 0;
        for (List<String> each : shards) {
            result.put("table_" + index++, mockShardingTable(each));
        }
        return result;
    }
    
    private ShardingTable mockShardingTable(final List<String> dataSources) {
        ShardingTable result = mock(ShardingTable.class);
        Map<String, List<DataNode>> dataNodeGroups = new HashMap<>(dataSources.size(), 1F);
        for (String each : dataSources) {
            dataNodeGroups.put(each, null);
        }
        when(result.getDataNodeGroups()).thenReturn(dataNodeGroups);
        return result;
    }
    
    @Test
    void assertRoute() {
        List<List<String>> shards = new LinkedList<>();
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        shards.add(Arrays.asList("ds1", "ds2", "ds3"));
        Map<String, ShardingTable> shardingTables = mockShardingTables(shards);
        when(shardingRule.getShardingTables()).thenReturn(shardingTables);
        RouteContext routeContext = shardingDataSourceGroupBroadcastRoutingEngine.route(shardingRule);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        Iterator<RouteUnit> iterator = routeContext.getRouteUnits().iterator();
        assertThat(Arrays.asList("ds1", "ds2", "ds3"), hasItems(iterator.next().getDataSourceMapper().getActualName()));
    }
}
