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

package org.apache.shardingsphere.infra.route.engine.tableless.type.broadcast;

import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TablelessInstanceBroadcastRouteEngineTest {
    
    @Test
    void assertRoute() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getAllInstanceDataSourceNames()).thenReturn(Collections.singleton("foo_ds_1"));
        RouteContext actual = new TablelessInstanceBroadcastRouteEngine(database).route(mock(RuleMetaData.class), Arrays.asList("foo_ds_1", "foo_ds_2"));
        assertThat(actual.getRouteUnits().size(), is(1));
        List<RouteUnit> routeUnits = new ArrayList<>(actual.getRouteUnits());
        assertThat(routeUnits.get(0).getDataSourceMapper().getLogicName(), is("foo_ds_1"));
        assertThat(routeUnits.get(0).getDataSourceMapper().getActualName(), is("foo_ds_1"));
        assertTrue(routeUnits.get(0).getTableMappers().isEmpty());
    }
}
