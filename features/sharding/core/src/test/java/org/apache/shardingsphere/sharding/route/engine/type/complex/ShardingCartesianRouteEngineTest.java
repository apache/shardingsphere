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

package org.apache.shardingsphere.sharding.route.engine.type.complex;

import org.apache.shardingsphere.infra.route.context.RouteContext;
import org.apache.shardingsphere.infra.route.context.RouteMapper;
import org.apache.shardingsphere.infra.route.context.RouteUnit;
import org.apache.shardingsphere.sharding.exception.algorithm.NoShardingTableRouteFactorException;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingCartesianRouteEngineTest {
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("routeArguments")
    void assertRoute(final String name, final Collection<RouteContext> routeContexts, final Collection<RouteUnit> expectedRouteUnits) {
        ShardingCartesianRouteEngine routeEngine = new ShardingCartesianRouteEngine(routeContexts);
        RouteContext actual = routeEngine.route(mock(ShardingRule.class));
        assertThat(actual.getRouteUnits(), is(expectedRouteUnits));
    }
    
    private static Stream<Arguments> routeArguments() {
        return Stream.of(
                Arguments.of("Common data source", Arrays.asList(
                        createRouteContext("foo_ds", "foo_order", "foo_order_0"), createRouteContext("foo_ds", "foo_order_item", "foo_order_item_0")),
                        Collections.singleton(new RouteUnit(new RouteMapper("foo_ds", "foo_ds"),
                                Arrays.asList(new RouteMapper("foo_order", "foo_order_0"), new RouteMapper("foo_order_item", "foo_order_item_0"))))),
                Arguments.of("Disjoint data sources", Arrays.asList(
                        createRouteContext("foo_ds_0", "foo_order", "foo_order_0"), createRouteContext("foo_ds_1", "foo_order_item", "foo_order_item_0"),
                        createRouteContext("foo_ds_0", "foo_user", "foo_user_0")), Collections.emptySet()),
                Arguments.of("Empty route contexts", Collections.emptyList(), Collections.emptySet()));
    }
    
    @Test
    void assertRouteWithMissingTableMapper() {
        RouteContext routeContext = mock(RouteContext.class);
        when(routeContext.getActualDataSourceNames()).thenReturn(Collections.singleton("foo_ds"));
        when(routeContext.getDataSourceLogicTablesMap(anyCollection())).thenReturn(Collections.singletonMap("foo_ds", Collections.singleton("foo_table")));
        when(routeContext.getActualTableNameGroups("foo_ds", Collections.singleton("foo_table"))).thenReturn(Collections.singletonList(Collections.singleton("foo_table_0")));
        when(routeContext.findTableMapper("foo_ds", "foo_table_0")).thenReturn(Optional.empty());
        ShardingCartesianRouteEngine routeEngine = new ShardingCartesianRouteEngine(Collections.singleton(routeContext));
        assertThrows(NoShardingTableRouteFactorException.class, () -> routeEngine.route(mock(ShardingRule.class)));
    }
    
    private static RouteContext createRouteContext(final String dataSourceName, final String logicTableName, final String actualTableName) {
        RouteContext result = new RouteContext();
        result.getRouteUnits().add(new RouteUnit(new RouteMapper(dataSourceName, dataSourceName), Collections.singletonList(new RouteMapper(logicTableName, actualTableName))));
        return result;
    }
}
