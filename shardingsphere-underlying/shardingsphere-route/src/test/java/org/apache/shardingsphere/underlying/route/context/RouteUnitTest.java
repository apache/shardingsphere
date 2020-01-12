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

package org.apache.shardingsphere.underlying.route.context;

import com.google.common.base.Optional;
import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class RouteUnitTest {
    
    private static final String DATASOURCE_NAME = "ds";
    
    private static final String LOGIC_TABLE = "table";
    
    private static final String SHARD_TABLE_0 = "table_0";
    
    private static final String SHARD_TABLE_1 = "table_1";
    
    private RouteUnit routeUnit;
    
    @Before
    public void setUp() {
        routeUnit = new RouteUnit(DATASOURCE_NAME);
        routeUnit.getTableUnits().addAll(mockTableUnits());
    }
    
    private Collection<TableUnit> mockTableUnits() {
        List<TableUnit> result = new ArrayList<>();
        result.add(new TableUnit(LOGIC_TABLE, SHARD_TABLE_0));
        result.add(new TableUnit(LOGIC_TABLE, SHARD_TABLE_1));
        return result;
    }
    
    @Test
    public void assertGetTableUnit() {
        Optional<TableUnit> actual = routeUnit.getTableUnit(DATASOURCE_NAME, SHARD_TABLE_0);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getLogicTableName(), is(LOGIC_TABLE));
        assertThat(actual.get().getActualTableName(), is(SHARD_TABLE_0));
    }
    
    @Test
    public void assertGetTableUnitNonExist() {
        Optional<TableUnit> actual = routeUnit.getTableUnit(DATASOURCE_NAME, "");
        assertFalse(actual.isPresent());
    }
    
    @Test
    public void assertGetActualTableNames() {
        Set<String> actual = routeUnit.getActualTableNames(LOGIC_TABLE);
        assertThat(actual.size(), is(2));
        assertTrue(actual.contains(SHARD_TABLE_0));
        assertTrue(actual.contains(SHARD_TABLE_1));
    }
    
    @Test
    public void assertGetLogicTableNames() {
        Set<String> actual = routeUnit.getLogicTableNames();
        assertThat(actual.size(), is(1));
        assertTrue(actual.contains(LOGIC_TABLE));
    }
    
    @Test
    public void assertGetDataSourceName() {
        assertThat(routeUnit.getActualDataSourceName(), is(DATASOURCE_NAME));
    }
    
    @Test
    public void assertGetMasterSlaveLogicDataSourceName() {
        assertThat(routeUnit.getLogicDataSourceName(), is(DATASOURCE_NAME));
    }
    
    @Test
    public void assertEquals() {
        RouteUnit expected = new RouteUnit(DATASOURCE_NAME, DATASOURCE_NAME);
        expected.getTableUnits().addAll(mockTableUnits());
        assertTrue(expected.equals(routeUnit));
    }
    
    @Test
    public void assertToString() {
        assertThat(routeUnit.toString(), is(String.format(
            "RouteUnit(logicDataSourceName=%s, actualDataSourceName=%s, tableUnits=[TableUnit(logicTableName=%s, actualTableName=%s), TableUnit(logicTableName=%s, actualTableName=%s)])",
            DATASOURCE_NAME, DATASOURCE_NAME, LOGIC_TABLE, SHARD_TABLE_0, LOGIC_TABLE, SHARD_TABLE_1)));
    }
}
