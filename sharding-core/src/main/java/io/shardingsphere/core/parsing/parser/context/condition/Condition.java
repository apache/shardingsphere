/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.core.parsing.parser.context.condition;

import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.shardingsphere.api.algorithm.sharding.ListShardingValue;
import io.shardingsphere.api.algorithm.sharding.RangeShardingValue;
import io.shardingsphere.api.algorithm.sharding.ShardingValue;
import io.shardingsphere.core.constant.ShardingOperator;
import io.shardingsphere.core.exception.ShardingException;
import io.shardingsphere.core.parsing.parser.expression.SQLExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLNumberExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLPlaceholderExpression;
import io.shardingsphere.core.parsing.parser.expression.SQLTextExpression;
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
 * @author maxiaoguang
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@Getter
@EqualsAndHashCode
@ToString
public class Condition {
    
    private final Column column;
    
    private final ShardingOperator operator;
    
    private final Map<Integer, Comparable<?>> positionValueMap = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    protected Condition() {
        column = null;
        operator = null;
    }
    
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
     * @deprecated only test call
     */
    @Deprecated
    public ShardingValue getShardingValue(final List<?> parameters) {
        List<Comparable<?>> conditionValues = getConditionValues(parameters);
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
    
    /**
     * Get condition values.
     * 
     * @param parameters parameters
     * @return condition values
     */
    public List<Comparable<?>> getConditionValues(final List<?> parameters) {
        List<Comparable<?>> result = new LinkedList<>(positionValueMap.values());
        for (Entry<Integer, Integer> entry : positionIndexMap.entrySet()) {
            Object parameter = parameters.get(entry.getValue());
            if (!(parameter instanceof Comparable<?>)) {
                throw new ShardingException("Parameter `%s` should extends Comparable for sharding value.", parameter);
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
