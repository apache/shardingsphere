package com.dangdang.ddframe.rdb.sharding.hint;

import com.dangdang.ddframe.rdb.sharding.api.HintManager;
import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class HintManagerHolderTest {
    
    private HintManager hintManager = HintManager.getInstance();
    
    @After
    public void tearDown() {
        hintManager.close();
    }
    
    @Test(expected = IllegalStateException.class)
    public void assertSetHintManagerTwice() {
        HintManagerHolder.setHintManager(HintManager.getInstance());
    }
    
    @Test
    public void assertGetDatabaseShardingValue() {
        hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
        assertTrue(HintManagerHolder.getDatabaseShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
    }
    
    @Test
    public void assertGetTableShardingValue() {
        hintManager.addTableShardingValue("logicTable", "shardingColumn", 1);
        assertTrue(HintManagerHolder.getTableShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
    }
    
    @Test
    public void assertClear() {
        hintManager.addDatabaseShardingValue("logicTable", "shardingColumn", 1);
        hintManager.close();
        assertFalse(HintManagerHolder.getDatabaseShardingValue(new ShardingKey("logicTable", "shardingColumn")).isPresent());
    }
}
