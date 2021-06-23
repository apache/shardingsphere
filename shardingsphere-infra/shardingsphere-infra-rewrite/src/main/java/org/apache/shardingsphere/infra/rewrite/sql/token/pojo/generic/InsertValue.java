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

package org.apache.shardingsphere.infra.rewrite.sql.token.pojo.generic;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.ComplexExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;

import java.util.List;

/**
 * Insert value.
 */
@RequiredArgsConstructor
@Getter
public class InsertValue {
    
    private final List<ExpressionSegment> values;
    
    @Override
    public final String toString() {
        StringBuilder result = new StringBuilder();
        result.append("(");
        for (int i = 0; i < values.size(); i++) {
            result.append(getValue(i)).append(", ");
        }
        result.delete(result.length() - 2, result.length()).append(")");
        return result.toString();
    }
    
    private String getValue(final int index) {
        ExpressionSegment expressionSegment = values.get(index);
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            return "?";
        } else if (expressionSegment instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            return literals instanceof String ? String.format("'%s'", ((LiteralExpressionSegment) expressionSegment).getLiterals()) : literals.toString();
        } else if (expressionSegment instanceof BinaryOperationExpression) {
            return ((BinaryOperationExpression) expressionSegment).getText();
        }
        return ((ComplexExpressionSegment) expressionSegment).getText();
    }
}
