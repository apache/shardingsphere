package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedList;
import java.util.List;

/**
 * 条件对象.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public final class Condition {
    
    @Getter
    private final Column column;
    
    @Getter
    private final ShardingOperator operator;
    
    private final List<Comparable<?>> values = new LinkedList<>();
    
    private final List<Integer> indices = new LinkedList<>();
    
    // TODO 增加index位置的list
    
    public Condition(final Column column, final SQLExpression sqlExpression) {
        this(column, ShardingOperator.EQUAL);
        init(sqlExpression);
    }
    
    public Condition(final Column column, final SQLExpression beginSQLExpression, final SQLExpression endSQLExpression) {
        this(column, ShardingOperator.BETWEEN);
        init(beginSQLExpression);
        init(endSQLExpression);
    }
    
    public Condition(final Column column, final List<SQLExpression> sqlExpressions) {
        this(column, ShardingOperator.IN);
        for (SQLExpression each : sqlExpressions) {
            init(each);
        }
    }
    
    private void init(final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            indices.add(((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else if (sqlExpression instanceof SQLTextExpression) {
            values.add(((SQLTextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof SQLNumberExpression) {
            values.add((Comparable) ((SQLNumberExpression) sqlExpression).getNumber());
        }
    }
    
    /**
     * 获取分片值.
     *
     * @param parameters 参数列表
     * @return 分片值
     */
    public List<Comparable<?>> getValues(final List<Object> parameters) {
        List<Comparable<?>> result = new LinkedList<>(values);
        for (int each : indices) {
            Object parameter = parameters.get(each);
            if (parameter instanceof Comparable<?>) {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
}
