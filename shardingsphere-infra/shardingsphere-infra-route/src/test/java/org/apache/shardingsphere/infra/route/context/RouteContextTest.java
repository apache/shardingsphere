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

import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RouteContextTest {
    
    private static final String DATASOURCE_NAME_0 = "ds0";
    
    private static final String DATASOURCE_NAME_1 = "ds1";
    
    private static final String LOGIC_TABLE = "table";
    
    private static final String ACTUAL_TABLE = "table_0";
    
    private RouteContext singleRouteContext;
    
    private RouteContext multiRouteContext;
    
    @Before
    public void setUp() {
        singleRouteContext = new RouteContext();
        multiRouteContext = new RouteContext();
        multiRouteContext.getRouteUnits().addAll(Arrays.asList(mockRouteUnit(DATASOURCE_NAME_0), mockRouteUnit(DATASOURCE_NAME_1)));
        singleRouteContext.getRouteUnits().add(mockRouteUnit(DATASOURCE_NAME_0));
    }
    
    private RouteUnit mockRouteUnit(final String datasourceName) {
        return new RouteUnit(new RouteMapper(datasourceName, datasourceName), Collections.singletonList(new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE)));
    }
    
    @Test
    public void assertIsSingleRouting() {
        assertTrue(singleRouteContext.isSingleRouting());
        assertFalse(multiRouteContext.isSingleRouting());
    }
    
    @Test
    public void assertGetActualDataSourceNames() {
        Collection<String> actual = singleRouteContext.getActualDataSourceNames();
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(DATASOURCE_NAME_0));
        actual = multiRouteContext.getActualDataSourceNames();
        assertThat(actual.size(), is(2));
        Iterator<String> iterator = actual.iterator();
        assertThat(iterator.next(), is(DATASOURCE_NAME_0));
        assertThat(iterator.next(), is(DATASOURCE_NAME_1));
    }
    
    @Test
    public void assertGetActualTableNameGroups() {
        Set<String> logicTableSet = new HashSet<>();
        logicTableSet.add(LOGIC_TABLE);
        List<Set<String>> actual = multiRouteContext.getActualTableNameGroups(DATASOURCE_NAME_1, logicTableSet);
        assertThat(actual.size(), is(1));
        assertTrue(actual.get(0).contains(ACTUAL_TABLE));
    }
    
    @Test
    public void assertGetDataSourceLogicTablesMap() {
        List<String> dataSources = Lists.newArrayList(DATASOURCE_NAME_0, DATASOURCE_NAME_1);
        Map<String, Set<String>> actual = multiRouteContext.getDataSourceLogicTablesMap(dataSources);
        assertThat(actual.size(), is(2));
        assertThat(actual.get(DATASOURCE_NAME_0).size(), is(1));
        assertThat(actual.get(DATASOURCE_NAME_0).iterator().next(), is(LOGIC_TABLE));
        assertThat(actual.get(DATASOURCE_NAME_1).size(), is(1));
        assertThat(actual.get(DATASOURCE_NAME_1).iterator().next(), is(LOGIC_TABLE));
    }
    
    @Test
    public void assertFindTableMapper() {
        Optional<RouteMapper> actual = multiRouteContext.findTableMapper(DATASOURCE_NAME_1, ACTUAL_TABLE);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE)));
    }
    
    @Test
    public void assertTableMapperNotFound() {
        assertFalse(singleRouteContext.findTableMapper(DATASOURCE_NAME_1, ACTUAL_TABLE).isPresent());
    }
}
