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

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HintManagerTest {
    
    @Test
    void assertGetInstanceTwice() {
        try {
            HintManager.getInstance();
            assertThrows(IllegalStateException.class, HintManager::getInstance);
        } finally {
            HintManager.clear();
        }
    }
    
    @Test
    void assertSetDatabaseShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            hintManager.setDatabaseShardingValue(3);
            assertTrue(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getDatabaseShardingValues("").size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getDatabaseShardingValues(""));
            assertThat(shardingValues.get(0), is(3));
        }
    }
    
    @Test
    void assertAddDatabaseShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            hintManager.addDatabaseShardingValue("logicTable", 3);
            assertThat(HintManager.getDatabaseShardingValues("logicTable").size(), is(2));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getDatabaseShardingValues("logicTable"));
            assertThat(shardingValues.get(0), is(1));
            assertThat(shardingValues.get(1), is(3));
        }
    }
    
    @Test
    void assertAddTableShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            hintManager.addTableShardingValue("logicTable", 3);
            assertThat(HintManager.getTableShardingValues("logicTable").size(), is(2));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getTableShardingValues("logicTable"));
            assertThat(shardingValues.get(0), is(1));
            assertThat(shardingValues.get(1), is(3));
        }
    }
    
    @Test
    void assertGetDatabaseShardingValuesWithoutLogicTable() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertThat(HintManager.getDatabaseShardingValues().size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getDatabaseShardingValues());
            assertThat(shardingValues.get(0), is(1));
        }
    }
    
    @Test
    void assertGetDatabaseShardingValuesWithLogicTable() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logic_table", 1);
            assertThat(HintManager.getDatabaseShardingValues("logic_table").size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getDatabaseShardingValues("logic_table"));
            assertThat(shardingValues.get(0), is(1));
        }
    }
    
    @Test
    void assertGetTableShardingValues() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logic_table", 1);
            assertThat(HintManager.getTableShardingValues("logic_table").size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getTableShardingValues("logic_table"));
            assertThat(shardingValues.get(0), is(1));
        }
    }
    
    @Test
    void assertIsDatabaseShardingOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
        }
    }
    
    @Test
    void assertIsDatabaseShardingOnlyWithoutSet() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.close();
        assertFalse(HintManager.isDatabaseShardingOnly());
    }
    
    @Test
    void assertAddDatabaseShardingValueOnlyDatabaseSharding() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
            hintManager.addDatabaseShardingValue("logic_table", 2);
            assertFalse(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getDatabaseShardingValues("logic_table").size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getDatabaseShardingValues("logic_table"));
            assertThat(shardingValues.get(0), is(2));
        }
    }
    
    @Test
    void assertAddTableShardingValueOnlyDatabaseSharding() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue(1);
            assertTrue(HintManager.isDatabaseShardingOnly());
            hintManager.addTableShardingValue("logic_table", 2);
            assertFalse(HintManager.isDatabaseShardingOnly());
            assertThat(HintManager.getTableShardingValues("logic_table").size(), is(1));
            List<Comparable<?>> shardingValues = new ArrayList<>(HintManager.getTableShardingValues("logic_table"));
            assertThat(shardingValues.get(0), is(2));
        }
    }
    
    @Test
    void assertSetWriteRouteOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setWriteRouteOnly();
            assertTrue(HintManager.isWriteRouteOnly());
        }
    }
    
    @Test
    void assertIsWriteRouteOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setWriteRouteOnly();
            assertTrue(HintManager.isWriteRouteOnly());
        }
    }
    
    @Test
    void assertIsWriteRouteOnlyWithoutSet() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.close();
        assertFalse(HintManager.isWriteRouteOnly());
    }
    
    @Test
    void assertSetReadwriteSplittingAuto() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setReadwriteSplittingAuto();
            assertFalse(HintManager.isWriteRouteOnly());
        }
    }
    
    @Test
    void assertClearShardingValues() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("t_order", 1);
            hintManager.addTableShardingValue("t_order", 1);
            hintManager.clearShardingValues();
            assertTrue(HintManager.getDatabaseShardingValues().isEmpty());
            assertTrue(HintManager.getTableShardingValues("t_order").isEmpty());
        }
    }
    
    @Test
    void assertClose() {
        HintManager hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("logic_table", 1);
        hintManager.addTableShardingValue("logic_table", 1);
        hintManager.close();
        assertTrue(HintManager.getDatabaseShardingValues("logic_table").isEmpty());
        assertTrue(HintManager.getTableShardingValues("logic_table").isEmpty());
    }
    
    @Test
    void assertIsInstantiated() {
        assertFalse(HintManager.isInstantiated());
        HintManager hintManager = HintManager.getInstance();
        assertTrue(HintManager.isInstantiated());
        hintManager.close();
        assertFalse(HintManager.isInstantiated());
    }
    
    @Test
    void assertGetDataSourceNameWithValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDataSourceName("foo_ds");
            Optional<String> actual = HintManager.getDataSourceName();
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is("foo_ds"));
        }
    }
    
    @Test
    void assertGetDataSourceNameWithoutValue() {
        try (HintManager ignored = HintManager.getInstance()) {
            assertFalse(HintManager.getDataSourceName().isPresent());
        }
    }
}
