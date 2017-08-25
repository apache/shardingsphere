package com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.base.Optional;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Conditions collection.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class Conditions {
    
    private final Map<Column, Condition> conditions = new LinkedHashMap<>();
    
    public Conditions(final Conditions conditions) {
        for (Entry<Column, Condition> entry : conditions.conditions.entrySet()) {
            this.conditions.put(entry.getKey(), entry.getValue());
        }
    }
    
    /**
     * Add condition.
     *
     * @param condition condition
     * @param shardingRule databases and tables sharding rule
     */
    // TODO adjust before add condition, eg: if condition exist = operator and include same column, should remove condition (tow equal condition should found nothing)
    public void add(final Condition condition, final ShardingRule shardingRule) {
        // TODO self-join has problem, table name maybe use alias
        if (shardingRule.isShardingColumn(condition.getColumn())) {
            conditions.put(condition.getColumn(), condition);
        }
    }
    
    // TODO should remove, use mockito to replace this method
    @Deprecated
    public void add(final Condition condition) {
        conditions.put(condition.getColumn(), condition);
    }
    
    /**
     * Adjust condition is empty or not.
     * 
     * @return condition is empty or not
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @return found condition
     */
    public Optional<Condition> find(final Column column) {
        return Optional.fromNullable(conditions.get(column));
    }
}
