/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.api;

import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.hint.HintManagerHolder;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HintManagerTest {
    
    @Test
    public void assertAddDatabaseShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            assertTrue(HintManagerHolder.getDatabaseShardingValue("logicTable").isPresent());
            assertThat(HintManagerHolder.getDatabaseShardingValue("logicTable").get().getColumnName(), is(HintManagerHolder.DB_COLUMN_NAME));
            assertThat(((ListShardingValue<? extends Comparable>) HintManagerHolder.getDatabaseShardingValue("logicTable").get()).getValues().iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", 1);
            hintManager.addDatabaseShardingValue("logicTable", 3);
            hintManager.addDatabaseShardingValue("logicTable", 5);
            assertTrue(HintManagerHolder.getDatabaseShardingValue("logicTable").isPresent());
            assertThat(HintManagerHolder.getDatabaseShardingValue("logicTable").get().getColumnName(), is(HintManagerHolder.DB_COLUMN_NAME));
            assertThat(((ListShardingValue) HintManagerHolder.getDatabaseShardingValue("logicTable").get()).getValues().size(), is(3));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertAddTableShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            assertTrue(HintManagerHolder.getTableShardingValue("logicTable").isPresent());
            assertThat(HintManagerHolder.getTableShardingValue("logicTable").get().getColumnName(), is(HintManagerHolder.DB_COLUMN_NAME));
            assertThat(((ListShardingValue<? extends Comparable>) HintManagerHolder.getTableShardingValue("logicTable").get()).getValues().iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", 1);
            hintManager.addTableShardingValue("logicTable", 3);
            hintManager.addTableShardingValue("logicTable", 5);
            assertTrue(HintManagerHolder.getTableShardingValue("logicTable").isPresent());
            assertThat(HintManagerHolder.getTableShardingValue("logicTable").get().getColumnName(), is(HintManagerHolder.DB_COLUMN_NAME));
            assertThat(((ListShardingValue) HintManagerHolder.getTableShardingValue("logicTable").get()).getValues().size(), is(3));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue("1");
            assertTrue(HintManagerHolder.isDatabaseShardingOnly());
            assertThat((String) ((ListShardingValue)
                    HintManagerHolder.getDatabaseShardingValue(HintManagerHolder.DB_TABLE_NAME).get()).getValues().iterator().next(), is("1"));
        }
    }
}
