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

package org.apache.shardingsphere.infra.hint;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HintManagerTest {
    
    @Test(expected = IllegalStateException.class)
    public void assertGetInstanceTwice() {
        try {
            HintManager.getInstance();
            HintManager.getInstance();
        } finally {
            HintManager.clear();
        }
    }
    
    @Test
    public void assertSetDatabaseShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            hintManager.setDatabaseShardingValue(3);
            assertTrue(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getDatabaseShardingValues("").size(), is(1));
            assertTrue(HintManager.getDatabaseShardingValues("").contains(3));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            hintManager.addDatabaseShardingValue("logicTable", 3);
            assertThat(HintManager.getDatabaseShardingValues("logicTable").size(), is(2));
            assertTrue(HintManager.getDatabaseShardingValues("logicTable").contains(1));
            assertTrue(HintManager.getDatabaseShardingValues("logicTable").contains(3));
        }
    }
    
    @Test
    public void assertAddTableShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            hintManager.addTableShardingValue("logicTable", 3);
            assertThat(HintManager.getTableShardingValues("logicTable").size(), is(2));
            assertTrue(HintManager.getTableShardingValues("logicTable").contains(1));
            assertTrue(HintManager.getTableShardingValues("logicTable").contains(3));
        }
    }
    
    @Test
    public void assertGetDatabaseShardingValuesWithoutLogicTable() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertThat(HintManager.getDatabaseShardingValues().size(), is(1));
            assertTrue(HintManager.getDatabaseShardingValues().contains(1));
        }
    }
    
    @Test
    public void assertGetDatabaseShardingValuesWithLogicTable() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logic_table", 1);
            assertThat(HintManager.getDatabaseShardingValues("logic_table").size(), is(1));
            assertTrue(HintManager.getDatabaseShardingValues("logic_table").contains(1));
        }
    }
    
    @Test
    public void assertGetTableShardingValues() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logic_table", 1);
            assertThat(HintManager.getTableShardingValues("logic_table").size(), is(1));
            assertTrue(HintManager.getTableShardingValues("logic_table").contains(1));
        }
    }
    
    @Test
    public void assertIsDatabaseShardingOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
        }
    }
    
    @Test
    public void assertIsDatabaseShardingOnlyWithoutSet() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.close();
        assertFalse(HintManager.isDatabaseShardingOnly());
    }
    
    @Test
    public void assertAddDatabaseShardingValueOnlyDatabaseSharding() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
            hintManager.addDatabaseShardingValue("logic_table", 2);
            assertFalse(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getTableShardingValues("logic_table").size(), is(1));
            assertTrue(HintManager.getTableShardingValues("logic_table").contains(2));
            hintManager.close();
        }
    }
    
    @Test
    public void assertAddTableShardingValueOnlyDatabaseSharding() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
            hintManager.addTableShardingValue("logic_table", 2);
            assertFalse(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getTableShardingValues("logic_table").size(), is(1));
            assertTrue(HintManager.getTableShardingValues("logic_table").contains(2));
            hintManager.close();
        }
    }
    
    @Test
    public void assertSetMasterRouteOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setMasterRouteOnly();
            assertTrue(HintManager.isMasterRouteOnly());
        }
    }
    
    @Test
    public void assertIsMasterRouteOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setMasterRouteOnly();
            assertTrue(HintManager.isMasterRouteOnly());
        }
    }
    
    @Test
    public void assertIsMasterRouteOnlyWithoutSet() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.close();
        assertFalse(HintManager.isMasterRouteOnly());
    }
    
    @Test
    public void assertClose() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("logic_table", 1);
        hintManager.addTableShardingValue("logic_table", 1);
        hintManager.close();
        assertTrue(HintManager.getDatabaseShardingValues("logic_table").isEmpty());
        assertTrue(HintManager.getTableShardingValues("logic_table").isEmpty());
    }
}
