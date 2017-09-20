package io.shardingjdbc.core.integrate.type.sharding.hint.helper;

import io.shardingjdbc.core.api.HintManager;
import io.shardingjdbc.core.constant.ShardingOperator;
import lombok.AccessLevel;
import lombok.Getter;

import java.util.List;

public class HintShardingValueHelper implements AutoCloseable {
    
    @Getter(AccessLevel.PROTECTED)
    private final HintManager hintManager;
    
    public HintShardingValueHelper(final int userId, final int orderId) {
        hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_order", "user_id", userId);
        hintManager.addTableShardingValue("t_order", "order_id", orderId);
    }
    
    public HintShardingValueHelper(final List<Integer> userId, final ShardingOperator userIdOperator, final List<Integer> orderId, final ShardingOperator orderIdOperator) {
        hintManager = HintManager.getInstance();
        hintManager.addDatabaseShardingValue("t_order", "user_id", userIdOperator, userId.toArray(new Comparable[userId.size()]));
        hintManager.addTableShardingValue("t_order", "order_id", orderIdOperator, orderId.toArray(new Comparable[orderId.size()]));
    }
    
    @Override
    public void close() {
        hintManager.close();
    }
}
