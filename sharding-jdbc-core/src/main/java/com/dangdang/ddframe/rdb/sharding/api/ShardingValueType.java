package com.dangdang.ddframe.rdb.sharding.api;

/**
 * Sharding value type.
 *
 * @author zhangliang
 */
public enum ShardingValueType {
    
    /**
     * Sharding for {@code =}.
     */
    SINGLE,
    
    /**
     * Sharding for {@code IN}.
     */
    LIST,
    
    /**
     * Sharding for {@code BETWEEN}.
     */
    RANGE
}
