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

package io.shardingjdbc.core.hint;

import io.shardingjdbc.core.api.HintManager;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class HintManagerHolderTest {
    
    private final HintManager hintManager = HintManager.getInstance();
    
    @After
    public void tearDown() {
        hintManager.close();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSetHintManagerTwice() {
        HintManagerHolder.setHintManager(HintManager.getInstance());
    }
    
    @Test
    public void assertsUseShardingHintWithoutAddShardingColumns() {
        assertFalse(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertsUseShardingHintWithoutSetHintManager() {
        hintManager.close();
        assertFalse(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertsUseShardingHintWithAddShardingColumns() {
        hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
        assertTrue(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertGetDatabaseShardingValue() {
        hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
        assertTrue(HintManagerHolder.getDatabaseShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
        assertTrue(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertGetTableShardingValue() {
        hintManager.addTableShardingValue("logicTable", "shardingColumn", 1);
        assertTrue(HintManagerHolder.getTableShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
        assertTrue(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertIsMasterRouteOnlyWithoutSet() {
        hintManager.close();
        assertFalse(HintManagerHolder.isMasterRouteOnly());
        assertFalse(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertIsMasterRouteOnly() {
        hintManager.setMasterRouteOnly();
        assertTrue(HintManagerHolder.isMasterRouteOnly());
        assertFalse(HintManagerHolder.isUseShardingHint());
    }
    
    @Test
    public void assertClear() {
        hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
        hintManager.close();
        assertFalse(HintManagerHolder.getDatabaseShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
    }
}
