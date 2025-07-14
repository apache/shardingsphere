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

package org.apache.shardingsphere.infra.route.context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteContextTest {
    
    private RouteContext multiRouteContext;
    
    private RouteContext notContainsTableShardingRouteContext;
    
    @BeforeEach
    void setUp() {
        multiRouteContext = new RouteContext();
        multiRouteContext.getRouteUnits().addAll(Arrays.asList(createRouteUnit("foo_ds"), createRouteUnit("bar_ds")));
        notContainsTableShardingRouteContext = new RouteContext();
        notContainsTableShardingRouteContext.getRouteUnits().add(createDatabaseShardingOnlyRouteUnit());
    }
    
    private RouteUnit createRouteUnit(final String datasourceName) {
        return new RouteUnit(new RouteMapper(datasourceName, datasourceName), Collections.singleton(new RouteMapper("logic_tbl", "actual_tbl")));
    }
    
    private RouteUnit createDatabaseShardingOnlyRouteUnit() {
        return new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singleton(new RouteMapper("logic_tbl", "logic_tbl")));
    }
    
    @Test
    void assertIsNotSingleRouting() {
        assertFalse(multiRouteContext.isSingleRouting());
    }
    
    @Test
    void assertIsSingleRouting() {
        assertTrue(notContainsTableShardingRouteContext.isSingleRouting());
    }
    
    @Test
    void assertGetActualDataSourceNames() {
        assertThat(multiRouteContext.getActualDataSourceNames(), is(new HashSet<>(Arrays.asList("foo_ds", "bar_ds"))));
    }
    
    @Test
    void assertGetActualTableNameGroups() {
        assertThat(multiRouteContext.getActualTableNameGroups("bar_ds", Collections.singleton("logic_tbl")), is(Collections.singletonList(Collections.singleton("actual_tbl"))));
    }
    
    @Test
    void assertGetDataSourceLogicTablesMap() {
        List<String> dataSources = Arrays.asList("foo_ds", "bar_ds", "invalid_ds");
        Map<String, Set<String>> actual = multiRouteContext.getDataSourceLogicTablesMap(dataSources);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("foo_ds").size(), is(1));
        assertThat(actual.get("foo_ds").iterator().next(), is("logic_tbl"));
        assertThat(actual.get("bar_ds").size(), is(1));
        assertThat(actual.get("bar_ds").iterator().next(), is("logic_tbl"));
    }
    
    @Test
    void assertFindTableMapper() {
        Optional<RouteMapper> actual = multiRouteContext.findTableMapper("foo_ds", "actual_tbl");
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new RouteMapper("logic_tbl", "actual_tbl")));
    }
    
    @Test
    void assertTableMapperNotFound() {
        assertFalse(notContainsTableShardingRouteContext.findTableMapper("bar_ds", "actual_tbl").isPresent());
    }
    
    @Test
    void assertPutRouteUnitWithExistedDataSourceMapper() {
        multiRouteContext.putRouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singleton(new RouteMapper("foo_tbl", "foo_tbl")));
        assertThat(multiRouteContext.getRouteUnits(),
                is(new LinkedHashSet<>(Arrays.asList(
                        new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), new LinkedHashSet<>(Arrays.asList(new RouteMapper("logic_tbl", "actual_tbl"), new RouteMapper("foo_tbl", "foo_tbl")))),
                        new RouteUnit(new RouteMapper("bar_ds", "bar_ds"), Collections.singleton(new RouteMapper("logic_tbl", "actual_tbl")))))));
    }
    
    @Test
    void assertPutRouteUnitWithNewDataSourceMapper() {
        multiRouteContext.putRouteUnit(new RouteMapper("new_ds", "new_ds"), Collections.singleton(new RouteMapper("new_tbl", "new_tbl")));
        assertThat(multiRouteContext.getRouteUnits(),
                is(new LinkedHashSet<>(Arrays.asList(
                        new RouteUnit(new RouteMapper("foo_ds", "foo_ds"), Collections.singleton(new RouteMapper("logic_tbl", "actual_tbl"))),
                        new RouteUnit(new RouteMapper("bar_ds", "bar_ds"), Collections.singleton(new RouteMapper("logic_tbl", "actual_tbl"))),
                        new RouteUnit(new RouteMapper("new_ds", "new_ds"), Collections.singleton(new RouteMapper("new_tbl", "new_tbl")))))));
    }
    
    @Test
    void assertContainsTableShardingWhenContainsTableSharding() {
        assertTrue(multiRouteContext.containsTableSharding());
    }
    
    @Test
    void assertContainsTableShardingWhenNotContainsTableSharding() {
        assertFalse(notContainsTableShardingRouteContext.containsTableSharding());
    }
}
