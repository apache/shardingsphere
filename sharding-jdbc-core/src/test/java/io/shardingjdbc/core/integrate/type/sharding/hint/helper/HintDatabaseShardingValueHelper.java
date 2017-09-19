package io.shardingjdbc.core.integrate.type.sharding.hint.helper;

public class HintDatabaseShardingValueHelper extends HintShardingValueHelper {
    
    public HintDatabaseShardingValueHelper(final int userId) {
        super(userId, 0);
        getHintManager().setDatabaseShardingValue(userId);
    }
}
