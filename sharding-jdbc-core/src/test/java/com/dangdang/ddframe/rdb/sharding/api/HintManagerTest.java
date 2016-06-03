/*
 * Copyright 1999-2015 dangdang.com.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package com.dangdang.ddframe.rdb.sharding.api;

import com.dangdang.ddframe.rdb.sharding.hint.HintManagerHolder;
import com.dangdang.ddframe.rdb.sharding.hint.ShardingKey;
import com.dangdang.ddframe.rdb.sharding.parser.result.router.Condition;
import org.junit.Test;

import static junit.framework.TestCase.assertTrue;
import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public final class HintManagerTest {
    
    @Test
    public void assertAddDatabaseShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.SINGLE));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getValue(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", Condition.BinaryOperator.IN, 1, 3, 5);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.LIST));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getValues().size(), is(3));
        }
    }
    
    @Test
    public void assertAddDatabaseShardingValueForBetween() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", Condition.BinaryOperator.BETWEEN, 1, 10);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getDatabaseShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.RANGE));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getValueRange().lowerEndpoint(), is((Comparable) 1));
            assertThat(hintManager.getDatabaseShardingValue(shardingKey).getValueRange().upperEndpoint(), is((Comparable) 10));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForEquals() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", 1);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getTableShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.SINGLE));
            assertThat(hintManager.getTableShardingValue(shardingKey).getValue(), is((Comparable) 1));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForIn() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", Condition.BinaryOperator.IN, 1, 3, 5);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getTableShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.LIST));
            assertThat(hintManager.getTableShardingValue(shardingKey).getValues().size(), is(3));
        }
    }
    
    @Test
    public void assertAddTableShardingValueForBetween() {
        try (HintManager hintManager = HintManager.getInstance()) {
            hintManager.addTableShardingValue("logicTable", "shardingColumn", Condition.BinaryOperator.BETWEEN, 1, 10);
            ShardingKey shardingKey = new ShardingKey("logicTable", "shardingColumn");
            assertTrue(HintManagerHolder.getTableShardingValue(shardingKey).isPresent());
            assertThat(hintManager.getTableShardingValue(shardingKey).getColumnName(), is("shardingColumn"));
            assertThat(hintManager.getTableShardingValue(shardingKey).getType(), is(ShardingValue.ShardingValueType.RANGE));
            assertThat(hintManager.getTableShardingValue(shardingKey).getValueRange().lowerEndpoint(), is((Comparable) 1));
            assertThat(hintManager.getTableShardingValue(shardingKey).getValueRange().upperEndpoint(), is((Comparable) 10));
        }
    }
}
