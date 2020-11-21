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
import org.apache.shardingsphere.infra.metadata.resource.DataSourcesMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class ShardingInstanceBroadcastRoutingEngineTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private DataSourcesMetaData dataSourcesMetaData;
    
    private ShardingInstanceBroadcastRoutingEngine shardingInstanceBroadcastRoutingEngine;
    
    @Before
    public void setUp() {
        when(shardingRule.getDataSourceNames()).thenReturn(Collections.singletonList(DATASOURCE_NAME));
        when(dataSourcesMetaData.getAllInstanceDataSourceNames()).thenReturn(Lists.newArrayList(DATASOURCE_NAME));
        shardingInstanceBroadcastRoutingEngine = new ShardingInstanceBroadcastRoutingEngine(dataSourcesMetaData);
    }
    
    @Test
    public void assertRoute() {
        RouteContext routeContext = new RouteContext();
        shardingInstanceBroadcastRoutingEngine.route(routeContext, shardingRule);
        assertThat(routeContext.getRouteUnits().size(), is(1));
        assertThat(routeContext.getRouteUnits().iterator().next().getDataSourceMapper().getActualName(), is(DATASOURCE_NAME));
    }
}
