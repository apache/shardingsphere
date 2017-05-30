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
    
    private final ShardingColumn shardingColumn;
    
    private final ShardingOperator operator;
    
    private final List<Comparable<?>> values = new LinkedList<>();
    
    private final List<Integer> valueIndices = new LinkedList<>();
    
    public Condition(final ShardingColumn shardingColumn, final SQLExpression sqlExpr) {
        this(shardingColumn, ShardingOperator.EQUAL);
        initSQLExpr(sqlExpr);
    }
    
    public Condition(final ShardingColumn shardingColumn, final SQLExpression beginSqlExpr, final SQLExpression endSqlExpr) {
        this(shardingColumn, ShardingOperator.BETWEEN);
        initSQLExpr(beginSqlExpr);
        initSQLExpr(endSqlExpr);
    }
    
    public Condition(final ShardingColumn shardingColumn, final List<SQLExpression> sqlExprs) {
        this(shardingColumn, ShardingOperator.IN);
        for (SQLExpression each : sqlExprs) {
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
