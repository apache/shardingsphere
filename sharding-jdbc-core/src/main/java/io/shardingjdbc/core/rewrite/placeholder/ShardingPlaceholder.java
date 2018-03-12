package io.shardingjdbc.core.rewrite.placeholder;

/**
 * Sharding placeholder for rewrite.
 *
 * @author zhangliang
 */
public interface ShardingPlaceholder {
    
    /**
     * Get logic table name.
     * 
     * @return logic table name
     */
    String getLogicTableName();
}
