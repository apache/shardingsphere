package com.dangdang.ddframe.rdb.integrate.hint.helper;

public class DynamicDatabaseShardingValueHelper extends DynamicShardingValueHelper {
    
    public DynamicDatabaseShardingValueHelper(final int userId) {
        super(userId, 0);
        getHintManager().setDatabaseShardingValue(userId);
    }
}
