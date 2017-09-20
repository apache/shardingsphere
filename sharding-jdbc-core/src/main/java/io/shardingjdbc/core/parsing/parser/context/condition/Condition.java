package io.shardingjdbc.core.parsing.parser.context.condition;

import io.shardingjdbc.core.api.algorithm.sharding.ShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import io.shardingjdbc.core.parsing.parser.expression.SQLExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingjdbc.core.parsing.parser.expression.SQLTextExpression;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Condition.
 *
 * @author zhangliang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
@ToString
public final class Condition {
    
    @Getter
    private final Column column;
    
    @Getter
    private final ShardingOperator operator;
    
    private final Map<Integer, Comparable<?>> positionValueMap = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    public Condition(final Column column, final SQLExpression sqlExpression) {
        this(column, ShardingOperator.EQUAL);
        init(sqlExpression, 0);
    }
    
    public Condition(final Column column, final SQLExpression beginSQLExpression, final SQLExpression endSQLExpression) {
        this(column, ShardingOperator.BETWEEN);
        init(beginSQLExpression, 0);
        init(endSQLExpression, 1);
    }
    
    public Condition(final Column column, final List<SQLExpression> sqlExpressions) {
        this(column, ShardingOperator.IN);
        int count = 0;
        for (SQLExpression each : sqlExpressions) {
            init(each, count);
            count++;
        }
    }
    
    private void init(final SQLExpression sqlExpression, final int position) {
        if (sqlExpression instanceof SQLPlaceholderExpression) {
            positionIndexMap.put(position, ((SQLPlaceholderExpression) sqlExpression).getIndex());
        } else if (sqlExpression instanceof SQLTextExpression) {
            positionValueMap.put(position, ((SQLTextExpression) sqlExpression).getText());
        } else if (sqlExpression instanceof SQLNumberExpression) {
            positionValueMap.put(position, (Comparable) ((SQLNumberExpression) sqlExpression).getNumber());
        }
    }
    
    /**
     * Get sharding value via condition.
     *
     * @param parameters parameters
     * @return sharding value
     */
    public ShardingValue getShardingValue(final List<Object> parameters) {
        List<Comparable<?>> conditionValues = getValues(parameters);
        switch (operator) {
            case EQUAL:
            case IN:
                return new ListShardingValue<>(column.getTableName(), column.getName(), conditionValues);
            case BETWEEN:
                return new RangeShardingValue<>(column.getTableName(), column.getName(), Range.range(conditionValues.get(0), BoundType.CLOSED, conditionValues.get(1), BoundType.CLOSED));
            default:
                throw new UnsupportedOperationException(operator.getExpression());
        }
    }
    
    private List<Comparable<?>> getValues(final List<Object> parameters) {
        List<Comparable<?>> result = new LinkedList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (!(parameter instanceof Comparable<?>)) {
                throw new ShardingJdbcException("Parameter `%s` should extends Comparable for sharding value.", parameter);
            }
            if (entry.getKey() < result.size()) {
                result.add(entry.getKey(), (Comparable<?>) parameter);
            } else {
                result.add((Comparable<?>) parameter);
            }
        }
        return result;
    }
}
