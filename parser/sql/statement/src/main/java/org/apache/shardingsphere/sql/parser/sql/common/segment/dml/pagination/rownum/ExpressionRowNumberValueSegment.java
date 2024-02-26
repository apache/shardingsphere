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

package org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum;

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import java.util.List;

/**
 * Row number value segment for expression.
 */
public final class ExpressionRowNumberValueSegment extends RowNumberValueSegment {
    
    private final ExpressionSegment expressionSegment;
    
    public ExpressionRowNumberValueSegment(final int startIndex, final int stopIndex, final ExpressionSegment expressionSegment, final boolean boundOpened) {
        super(startIndex, stopIndex, boundOpened);
        this.expressionSegment = expressionSegment;
    }
    
    /**
     * Get value.
     *
     * @param params parameters
     * @return value
     */
    public Long getValue(final List<Object> params) {
        return getValueFromExpression(expressionSegment, params);
    }
    
    private Long getValueFromExpression(final ExpressionSegment expressionSegment, final List<Object> params) {
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return null == params || params.isEmpty() ? 0L : Long.parseLong(params.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex()).toString());
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            return getValueFromBinaryOperationExpression((BinaryOperationExpression) expressionSegment, params);
        }
        if (expressionSegment instanceof LiteralExpressionSegment) {
            return Long.parseLong(expressionSegment.getText());
        }
        throw new UnsupportedOperationException(String.format("Unsupported expression: %s in page expression", expressionSegment.getClass().getName()));
    }
    
    private Long getValueFromBinaryOperationExpression(final BinaryOperationExpression binaryOperationExpression, final List<Object> params) {
        String operator = binaryOperationExpression.getOperator();
        Long leftValue = getValueFromExpression(binaryOperationExpression.getLeft(), params);
        Long rightValue = getValueFromExpression(binaryOperationExpression.getRight(), params);
        switch (operator) {
            case "+":
                return leftValue + rightValue;
            case "-":
                return leftValue - rightValue;
            case "*":
                return leftValue * rightValue;
            case "/":
                return leftValue / rightValue;
            default:
                throw new UnsupportedOperationException(String.format("Unsupported operator: %s in page expression", operator));
        }
    }
}
