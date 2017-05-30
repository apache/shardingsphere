package com.dangdang.ddframe.rdb.sharding.parsing.parser.context;

import com.dangdang.ddframe.rdb.sharding.constant.ShardingOperator;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLNumberExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLPlaceholderExpression;
import com.dangdang.ddframe.rdb.sharding.parsing.parser.expression.SQLTextExpression;
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
@RequiredArgsConstructor
@Getter
@EqualsAndHashCode
public final class Condition {
    
    private final Column column;
    
    private final ShardingOperator operator;
    
    private final List<Comparable<?>> values = new LinkedList<>();
    
    private final List<Integer> valueIndices = new LinkedList<>();
    
    public Condition(final Column column, final SQLExpression sqlExpression) {
        this(column, ShardingOperator.EQUAL);
        initSQLExpr(sqlExpression);
    }
    
    public Condition(final Column column, final SQLExpression beginSqlExpression, final SQLExpression endSqlExpression) {
        this(column, ShardingOperator.BETWEEN);
        initSQLExpr(beginSqlExpression);
        initSQLExpr(endSqlExpression);
    }
    
    public Condition(final Column column, final List<SQLExpression> sqlExpressions) {
        this(column, ShardingOperator.IN);
        for (SQLExpression each : sqlExpressions) {
            initSQLExpr(each);
        }
    }
    
    private void initSQLExpr(final SQLExpression sqlExpression) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            valueIndices.add(((SQLPlaceholderExpression) sqlExpression).getIndex());
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
        for (int each : valueIndices) {
            Object parameter = parameters.get(each);
            if (parameter instanceof Comparable<?>) {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
}
