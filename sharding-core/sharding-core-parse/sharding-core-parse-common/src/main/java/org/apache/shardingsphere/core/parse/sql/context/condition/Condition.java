/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.shardingsphere.core.parse.sql.context.condition;

import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.apache.shardingsphere.core.constant.ShardingOperator;
import org.apache.shardingsphere.core.exception.ShardingException;
import org.apache.shardingsphere.core.parse.sql.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

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
@EqualsAndHashCode(exclude = {"predicateSegment"})
@ToString
public class Condition {
    
    private final Column column;
    
    private final ColumnSegment predicateSegment;
    
    private final ShardingOperator operator;
    
    @Setter
    private String compareOperator;
    
    private final Map<Integer, Comparable<?>> positionValueMap = new LinkedHashMap<>();
    
    private final Map<Integer, Integer> positionIndexMap = new LinkedHashMap<>();
    
    protected Condition() {
        column = null;
        operator = null;
        predicateSegment = null;
    }
    
    public Condition(final Column column, final ColumnSegment predicateSegment, final ExpressionSegment expressionSegment) {
        this(column, predicateSegment, ShardingOperator.EQUAL);
        init(expressionSegment, 0);
    }
    
    public Condition(final Column column, final ColumnSegment predicateSegment, final String compareOperator, final ExpressionSegment expressionSegment) {
        this.column = column;
        this.predicateSegment = predicateSegment;
        this.compareOperator = compareOperator;
        if ("=".equals(compareOperator)) {
            operator = ShardingOperator.EQUAL;
        } else {
            operator = null;
        }
        init(expressionSegment, 0);
    }
    
    public Condition(final Column column, final ColumnSegment predicateSegment, final ExpressionSegment beginExpressionSegment, final ExpressionSegment endExpressionSegment) {
        this(column, predicateSegment, ShardingOperator.BETWEEN);
        init(beginExpressionSegment, 0);
        init(endExpressionSegment, 1);
    }
    
    public Condition(final Column column, final ColumnSegment predicateSegment, final List<ExpressionSegment> expressionSegments) {
        this(column, predicateSegment, ShardingOperator.IN);
        int count = 0;
        for (ExpressionSegment each : expressionSegments) {
            init(each, count);
            count++;
        }
    }
    
    private void init(final ExpressionSegment expressionSegment, final int position) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            positionIndexMap.put(position, ((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            positionValueMap.put(position, (Comparable<?>) ((LiteralExpressionSegment) expressionSegment).getLiterals());
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
