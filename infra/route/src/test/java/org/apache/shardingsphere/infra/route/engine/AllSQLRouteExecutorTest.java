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

package org.apache.shardingsphere.infra.route.engine;

import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.infra.route.engine.impl.AllSQLRouteExecutor;
import org.junit.Test;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class AllSQLRouteExecutorTest {
    
    @Test
    public void assertRouteSuccess() {
        String name = "test";
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResourceMetaData().getDataSources().keySet()).thenReturn(Stream.of(name).collect(Collectors.toSet()));
        AllSQLRouteExecutor allSQLRouteExecutor = new AllSQLRouteExecutor();
        RouteContext actual = allSQLRouteExecutor.route(new ConnectionContext(), mock(QueryContext.class), database);
        assertThat(actual.getRouteUnits().size(), is(1));
        RouteUnit routeUnit = actual.getRouteUnits().iterator().next();
        assertThat(routeUnit.getDataSourceMapper().getLogicName(), is(name));
        assertThat(routeUnit.getDataSourceMapper().getActualName(), is(name));
        assertTrue(routeUnit.getTableMappers().isEmpty());
    }
}
