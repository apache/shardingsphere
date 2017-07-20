package com.dangdang.ddframe.rdb.sharding.parsing.parser.context.condition;

import com.dangdang.ddframe.rdb.sharding.api.rule.ShardingRule;
import com.google.common.base.Optional;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 条件对象集合.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor
@ToString
public final class Conditions {
    
    private final Map<Column, Condition> conditions = new LinkedHashMap<>();
    
    /**
     * 添加条件对象.
     *
     * @param condition 条件对象
     * @param shardingRule 分库分表规则配置对象
     */
    // TODO 添加condition时进行判断, 比如:如果以存在 等于操作 的condition, 而已存在包含 =符号 的相同column的condition, 则不添加现有的condition, 而且删除原有condition
    public void add(final Condition condition, final ShardingRule shardingRule) {
        // TODO 自关联有问题，表名可考虑使用别名对应
        if (shardingRule.isShardingColumn(condition.getColumn())) {
            conditions.put(condition.getColumn(), condition);
        }
    }
    
    // TODO 引入mockito时去掉该方法
    public void add(final Condition condition) {
        conditions.put(condition.getColumn(), condition);
    }
    
    /**
     * 查找条件对象.
     *
     * @param column 列对象
     * @return 条件对象
     */
    public Optional<Condition> find(final Column column) {
        return Optional.fromNullable(conditions.get(column));
    }
}
