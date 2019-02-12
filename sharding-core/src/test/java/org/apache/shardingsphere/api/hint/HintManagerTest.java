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

package org.apache.shardingsphere.api.hint;

import org.apache.shardingsphere.core.hint.HintManagerHolder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HintManagerTest {
    
    @Test
    public void assertSetDatabaseShardingValue() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue("1");
            assertTrue(HintManagerHolder.isDatabaseShardingOnly());
            assertThat(HintManagerHolder.getDatabaseShardingValues("").iterator().next().toString(), is("1"));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            assertThat(HintManagerHolder.getDatabaseShardingValues("logicTable").iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            hintManager.addDatabaseShardingValue("logicTable", 3);
            hintManager.addDatabaseShardingValue("logicTable", 5);
            assertThat(HintManagerHolder.getDatabaseShardingValues("logicTable").size(), is(3));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            assertThat(HintManagerHolder.getTableShardingValues("logicTable").iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            hintManager.addTableShardingValue("logicTable", 3);
            hintManager.addTableShardingValue("logicTable", 5);
            assertThat(HintManagerHolder.getTableShardingValues("logicTable").size(), is(3));
        }
    }
    
    @Test
    public void assertSetMasterRouteOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setMasterRouteOnly();
            assertTrue(HintManagerHolder.isMasterRouteOnly());
        }
    }
}
