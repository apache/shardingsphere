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

package org.apache.shardingsphere.sharding.merge.dql.groupby;

import org.apache.shardingsphere.infra.binder.context.segment.select.projection.impl.AggregationProjection;
import org.apache.shardingsphere.infra.merge.result.impl.memory.MemoryQueryResultRow;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;

import java.math.BigDecimal;
import java.util.List;
import java.util.function.Function;

/**
 * Evaluates supported aggregation wrapper projections (e.g., IFNULL, COALESCE) using merged aggregation columns.
 */
public final class AggregationWrapperExpressionEvaluator {
    
    /**
     * Evaluate expression with memory query result row.
     * @param expression expression segment to evaluate
     * @param derivedAggregations derived aggregations
     * @param row memory query result row
     * @param targetType the target JDBC class type to coerce to (optional)
     * @return evaluated result
     */
    public static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final MemoryQueryResultRow row, final Class<?> targetType) {
        Object result = evaluate(expression, derivedAggregations, row::getCell);
        return coerce(result, targetType);
    }
    
    /**
     * Evaluate expression with current row data.
     * @param expression expression segment to evaluate
     * @param derivedAggregations derived aggregations
     * @param currentRow current row data
     * @param targetType the target JDBC class type to coerce to (optional)
     * @return evaluated result
     */
    public static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final List<Object> currentRow, final Class<?> targetType) {
        Object result = evaluate(expression, derivedAggregations, index -> currentRow.get(index - 1));
        return coerce(result, targetType);
    }
    
    private static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueExtractor) {
        if (expression instanceof AggregationProjectionSegment) {
            return getMergedAggregationValue((AggregationProjectionSegment) expression, derivedAggregations, valueExtractor);
        }
        if (expression instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expression).getLiterals();
        }
        if (expression instanceof FunctionSegment) {
            return evaluateFunction((FunctionSegment) expression, derivedAggregations, valueExtractor);
        }
        throw new IllegalArgumentException(String.format("Unsupported aggregation wrapper expression segment type: %s", expression.getClass().getName()));
    }
    
    private static Object coerce(final Object value, final Class<?> targetType) {
        if (value == null || targetType == null || value.getClass().equals(targetType)) {
            return value;
        }
        if (value instanceof Number) {
            Number num = (Number) value;
            if (targetType == BigDecimal.class) {
                return new BigDecimal(num.toString());
            }
            if (targetType == Long.class) {
                return num.longValue();
            }
            if (targetType == Integer.class) {
                return num.intValue();
            }
            if (targetType == Double.class) {
                return num.doubleValue();
            }
            if (targetType == Float.class) {
                return num.floatValue();
            }
            if (targetType == Short.class) {
                return num.shortValue();
            }
        }
        return value;
    }
    
    private static Object evaluateFunction(final FunctionSegment functionSegment, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueExtractor) {
        String functionName = functionSegment.getFunctionName();
        if ("IFNULL".equalsIgnoreCase(functionName) || "COALESCE".equalsIgnoreCase(functionName)) {
            for (ExpressionSegment each : functionSegment.getParameters()) {
                Object value = evaluate(each, derivedAggregations, valueExtractor);
                if (null != value) {
                    return value;
                }
            }
            return null;
        }
        throw new IllegalArgumentException(String.format("Unsupported aggregation wrapper function: %s", functionName));
    }
    
    private static Object getMergedAggregationValue(final AggregationProjectionSegment segment, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueExtractor) {
        for (AggregationProjection each : derivedAggregations) {
            if (each.getExpression().equals(segment.getText())) {
                return valueExtractor.apply(each.getIndex());
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find merged aggregation value for expression: %s", segment.getText()));
    }
}
