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

import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Optional;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RouteUnitTest {
    
    private static final String LOGIC_DATA_SOURCE = "logic_ds";
    
    private static final String ACTUAL_DATA_SOURCE = "actual_ds";
    
    private static final String LOGIC_TABLE = "tbl";
    
    private static final String ACTUAL_TABLE_0 = "tbl_0";
    
    private static final String ACTUAL_TABLE_1 = "tbl_1";
    
    private final RouteUnit routeUnit = new RouteUnit(
            new RouteMapper(LOGIC_DATA_SOURCE, ACTUAL_DATA_SOURCE), Arrays.asList(new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE_0), new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE_1)));
    
    @Test
    void assertGetLogicTableNames() {
        Set<String> actual = routeUnit.getLogicTableNames();
        assertThat(actual.size(), is(1));
        assertTrue(actual.contains(LOGIC_TABLE));
    }
    
    @Test
    void assertGetActualTableNames() {
        Set<String> actual = routeUnit.getActualTableNames(LOGIC_TABLE);
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains(ACTUAL_TABLE_0));
        assertTrue(actual.contains(ACTUAL_TABLE_1));
    }
    
    @Test
    void assertFindTableMapper() {
        Optional<RouteMapper> actual = routeUnit.findTableMapper(LOGIC_DATA_SOURCE, ACTUAL_TABLE_0);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getLogicName(), is(LOGIC_TABLE));
        assertThat(actual.get().getActualName(), is(ACTUAL_TABLE_0));
    }
    
    @Test
    void assertTableMapperNotFound() {
        assertFalse(routeUnit.findTableMapper("invalid_ds", "invalid_tbl").isPresent());
    }
    
    @Test
    void assertTableMapperIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RouteUnit(new RouteMapper(LOGIC_DATA_SOURCE, ACTUAL_DATA_SOURCE), null));
    }
    
    @Test
    void assertDataSourceMapperIsNull() {
        assertThrows(IllegalArgumentException.class, () -> new RouteUnit(null, Arrays.asList(new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE_0), new RouteMapper(LOGIC_TABLE, ACTUAL_TABLE_1))));
    }
}
