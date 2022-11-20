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

package org.apache.shardingsphere.shadow.route.engine.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Shadow extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ShadowExtractor {
    
    /**
     * Get column in expression segment.
     *
     * @param expressionSegment expression segment
     * @return column segment
     */
    public static Optional<ColumnSegment> extractColumn(final ExpressionSegment expressionSegment) {
        Collection<ColumnSegment> columnSegments = ColumnExtractor.extract(expressionSegment);
        return 1 == columnSegments.size() ? Optional.of(columnSegments.iterator().next()) : Optional.empty();
    }
    
    /**
     * Get values in expression segment.
     *
     * @param expression expression segment
     * @param params parameters
     * @return values
     */
    public static Optional<Collection<Comparable<?>>> extractValues(final ExpressionSegment expression, final List<Object> params) {
        Collection<Comparable<?>> result = new LinkedList<>();
        if (expression instanceof BinaryOperationExpression) {
            extractValues(((BinaryOperationExpression) expression).getRight(), params).ifPresent(result::addAll);
        }
        if (expression instanceof InExpression) {
            extractValues(((InExpression) expression).getRight(), params).ifPresent(result::addAll);
        }
        if (expression instanceof ListExpression) {
            ((ListExpression) expression).getItems().forEach(each -> extractValueInSimpleExpressionSegment(each, params).ifPresent(result::add));
        }
        if (expression instanceof SimpleExpressionSegment) {
            extractValueInSimpleExpressionSegment(expression, params).ifPresent(result::add);
        }
        return result.isEmpty() ? Optional.empty() : Optional.of(result);
    }
    
    private static Optional<Comparable<?>> extractValueInSimpleExpressionSegment(final ExpressionSegment expression, final List<Object> params) {
        if (expression instanceof LiteralExpressionSegment) {
            return extractValueInLiteralExpressionSegment((LiteralExpressionSegment) expression);
        }
        if (expression instanceof ParameterMarkerExpressionSegment) {
            return extractValueInParameterMarkerExpressionSegment((ParameterMarkerExpressionSegment) expression, params);
        }
        return Optional.empty();
    }
    
    private static Optional<Comparable<?>> extractValueInParameterMarkerExpressionSegment(final ParameterMarkerExpressionSegment expression, final List<Object> params) {
        return castToComparable(params.get(expression.getParameterMarkerIndex()));
    }
    
    private static Optional<Comparable<?>> extractValueInLiteralExpressionSegment(final LiteralExpressionSegment expression) {
        return castToComparable(expression.getLiterals());
    }
    
    private static Optional<Comparable<?>> castToComparable(final Object object) {
        return object instanceof Comparable<?> ? Optional.of((Comparable<?>) object) : Optional.empty();
    }
}
