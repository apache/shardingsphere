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

package io.shardingsphere.core.api;

import io.shardingsphere.core.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.hint.HintManagerHolder;
import io.shardingsphere.core.hint.ShardingKey;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class HintManagerTest {
    
    @Test
    public void assertAddDatabaseShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((ListShardingValue<? extends Comparable>) hintManager.getDatabaseShardingValue(shardingKey)).getValues().iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", ShardingOperator.IN, 1, 3, 5);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((ListShardingValue) hintManager.getDatabaseShardingValue(shardingKey)).getValues().size(), is(3));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForBetween() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", ShardingOperator.BETWEEN, 1, 10);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((RangeShardingValue) hintManager.getDatabaseShardingValue(shardingKey)).getValueRange().lowerEndpoint(), is((Comparable) 1));
            assertThat(((RangeShardingValue) hintManager.getDatabaseShardingValue(shardingKey)).getValueRange().upperEndpoint(), is((Comparable) 10));
        }
    }
    
    @SuppressWarnings("unchecked")
    @Test
    public void assertAddTableShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", 1);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((ListShardingValue<? extends Comparable>) hintManager.getTableShardingValue(shardingKey)).getValues().iterator().next(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", ShardingOperator.IN, 1, 3, 5);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((ListShardingValue) hintManager.getTableShardingValue(shardingKey)).getValues().size(), is(3));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForBetween() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", ShardingOperator.BETWEEN, 1, 10);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(((RangeShardingValue) hintManager.getTableShardingValue(shardingKey)).getValueRange().lowerEndpoint(), is((Comparable) 1));
            assertThat(((RangeShardingValue) hintManager.getTableShardingValue(shardingKey)).getValueRange().upperEndpoint(), is((Comparable) 10));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingOnly() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.setDatabaseShardingValue("1");
            assertTrue(hintManager.isDatabaseShardingOnly());
            assertTrue(hintManager.isShardingHint());
            assertThat((String) ((ListShardingValue) 
                    hintManager.getDatabaseShardingValue(new ShardingKey(HintManagerHolder.DB_TABLE_NAME, HintManagerHolder.DB_COLUMN_NAME))).getValues().iterator().next(), is("1"));
        }
    }
}
