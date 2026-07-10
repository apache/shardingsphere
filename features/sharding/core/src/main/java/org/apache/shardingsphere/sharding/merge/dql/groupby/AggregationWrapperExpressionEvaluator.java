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
     * @return evaluated result
     */
    public static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final MemoryQueryResultRow row) {
        return evaluate(expression, derivedAggregations, row::getCell);
    }
    
    /**
     * Evaluate expression with current row data.
     * @param expression expression segment to evaluate
     * @param derivedAggregations derived aggregations
     * @param currentRow current row data
     * @return evaluated result
     */
    public static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final List<Object> currentRow) {
        return evaluate(expression, derivedAggregations, index -> currentRow.get(index - 1));
    }
    
    private static Object evaluate(final ExpressionSegment expression, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueProvider) {
        if (expression instanceof AggregationProjectionSegment) {
            return getMergedAggregationValue((AggregationProjectionSegment) expression, derivedAggregations, valueProvider);
        }
        if (expression instanceof LiteralExpressionSegment) {
            return ((LiteralExpressionSegment) expression).getLiterals();
        }
        if (expression instanceof FunctionSegment) {
            return evaluateFunction((FunctionSegment) expression, derivedAggregations, valueProvider);
        }
        throw new IllegalArgumentException(String.format("Unsupported aggregation wrapper expression segment type: %s", expression.getClass().getName()));
    }
    
    private static Object evaluateFunction(final FunctionSegment functionSegment, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueProvider) {
        String functionName = functionSegment.getFunctionName();
        if ("IFNULL".equalsIgnoreCase(functionName) || "COALESCE".equalsIgnoreCase(functionName)) {
            for (ExpressionSegment each : functionSegment.getParameters()) {
                Object value = evaluate(each, derivedAggregations, valueProvider);
                if (null != value) {
                    return value;
                }
            }
            return null;
        }
        throw new IllegalArgumentException(String.format("Unsupported aggregation wrapper function: %s", functionName));
    }
    
    private static Object getMergedAggregationValue(final AggregationProjectionSegment segment, final List<AggregationProjection> derivedAggregations, final Function<Integer, Object> valueProvider) {
        for (AggregationProjection each : derivedAggregations) {
            if (each.getExpression().equals(segment.getText())) {
                return valueProvider.apply(each.getIndex());
            }
        }
        throw new IllegalArgumentException(String.format("Cannot find merged aggregation value for expression: %s", segment.getText()));
    }
}
