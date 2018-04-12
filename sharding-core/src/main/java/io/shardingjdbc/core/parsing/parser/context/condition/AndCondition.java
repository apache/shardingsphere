/*
 * Copyright 1999-2015 dangdang.com.
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

package io.shardingjdbc.core.parsing.parser.context.condition;

import com.google.common.base.Objects;
import com.google.common.base.Optional;
import com.google.common.collect.BoundType;
import com.google.common.collect.Range;
import io.shardingjdbc.core.api.algorithm.sharding.ListShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.RangeShardingValue;
import io.shardingjdbc.core.api.algorithm.sharding.ShardingValueUnit;
import io.shardingjdbc.core.constant.ShardingOperator;
import io.shardingjdbc.core.exception.ShardingJdbcException;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * And conditions.
 *
 * @author maxiaoguang
 */
@RequiredArgsConstructor
@Getter
@ToString
public final class AndCondition {
    
    private final List<Condition> conditions = new ArrayList<>();
    
    /**
     * Add condition.
     *
     * @param condition condition
     */
    public void add(final Condition condition) {
        conditions.add(condition);
    }
    
    /**
     * Find condition via column.
     *
     * @param column column
     * @return found condition
     */
    public Optional<Condition> find(final Column column) {
        Condition result = null;
        for (Condition each : conditions) {
            if (Objects.equal(each.getColumn(), column)) {
                result = each;
            }
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Get condition via index.
     *
     * @param index index of conditions
     * @return found condition
     */
    public Optional<Condition> get(final int index) {
        Condition result = null;
        if (size() > index) {
            result = conditions.get(index);
        }
        return Optional.fromNullable(result);
    }
    
    /**
     * Adjust conditions is empty or not.
     *
     * @return conditions is empty or not
     */
    public boolean isEmpty() {
        return conditions.isEmpty();
    }
    
    /**
     * Returns the number of conditions in this.
     *
     * @return the number of conditions in this
     */
    public int size() {
        return conditions.size();
    }
    
    /**
     * Get sharding values via conditions.
     *
     * @param parameters parameters
     * @return sharding value unit
     */
    public ShardingValueUnit getShardingValueUnit(final List<Object> parameters) {
        ShardingValueUnit result = new ShardingValueUnit();
        if (conditions.isEmpty()) {
            return result;
        }
        Map<Column, List<Condition>> conditionsMap = getConditionsMap();
        for (Map.Entry<Column, List<Condition>> entry : conditionsMap.entrySet()) {
            List<Comparable<?>> listValue = null;
            Range<Comparable<?>> rangeValue = null;
            for (Condition each : entry.getValue()) {
                List<Comparable<?>> values = each.getValues(parameters);
                if (Objects.equal(each.getOperator(), ShardingOperator.EQUAL) || Objects.equal(each.getOperator(), ShardingOperator.IN)) {
                    listValue = mergeValue(values, listValue);
                    if (null == listValue) {
                        return null;
                    }
                }
                if (Objects.equal(each.getOperator(), ShardingOperator.BETWEEN)) {
                    try {
                        rangeValue = mergeValue(Range.range(values.get(0), BoundType.CLOSED, values.get(1), BoundType.CLOSED), rangeValue);
                    } catch (IllegalArgumentException e) {
                        return null;
                    } catch (ClassCastException e) {
                        throw new ShardingJdbcException("Found different java type for sharding value `%s`.", each.getColumn());
                    }
                }
            }
            if (null == listValue) {
                result.add(new RangeShardingValue<>(entry.getKey().getTableName(), entry.getKey().getName(), rangeValue));
            } else {
                if (null != rangeValue) {
                    listValue = mergeValue(listValue, rangeValue);
                    if (null == listValue) {
                        return null;
                    }
                }
                result.add(new ListShardingValue<>(entry.getKey().getTableName(), entry.getKey().getName(), listValue));
            }
        }
        return result;
    }
    
    private Map<Column, List<Condition>> getConditionsMap() {
        Map<Column, List<Condition>> result = new LinkedHashMap<>();
        for (Condition each : conditions) {
            if (null == result.get(each.getColumn())) {
                result.put(each.getColumn(), new LinkedList<Condition>());
            }
            result.get(each.getColumn()).add(each);
        }
        return result;
    }
    
    private List<Comparable<?>> mergeValue(List<Comparable<?>> listValue1, List<Comparable<?>> listValue2) {
        if (null == listValue1) {
            return listValue2;
        }
        if (null == listValue2) {
            return listValue1;
        }
        listValue1.retainAll(listValue2);
        if (listValue1.isEmpty()) {
            return null;
        }
        return listValue1;
    }
    
    private Range<Comparable<?>> mergeValue(Range<Comparable<?>> rangeValue1, Range<Comparable<?>> rangeValue2) {
        if (null == rangeValue1) {
            return rangeValue2;
        }
        if (null == rangeValue2) {
            return rangeValue1;
        }
        try {
            return rangeValue1.intersection(rangeValue2);
        } catch (IllegalArgumentException e) {
            return null;
        } catch (ClassCastException e) {
            throw new ShardingJdbcException("Found different java type for same sharding value.");
        }
    }
    
    private List<Comparable<?>> mergeValue(List<Comparable<?>> listValue, Range<Comparable<?>> rangeValue) {
        List<Comparable<?>> result = new LinkedList<>();
        for (Comparable<?> each : listValue) {
            if (rangeValue.contains(each)) {
                result.add(each);
            }
        }
        if (result.isEmpty()) {
            return null;
        }
        return result;
    }
}
