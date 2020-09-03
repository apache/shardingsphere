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

package org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.engine;

import org.apache.shardingsphere.sql.parser.binder.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.top.TopProjectionSegment;

import java.util.List;
import java.util.Optional;

/**
 * Pagination context engine for top.
 */
public final class TopPaginationContextEngine {
    
    /**
     * Create pagination context.
     * 
     * @param topProjectionSegment top projection segment
     * @param where where condition
     * @param parameters SQL parameters
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final TopProjectionSegment topProjectionSegment, final ExpressionSegment where, final List<Object> parameters) {
        Optional<ExpressionSegment> rowNumberPredicate = null != where ? getRowNumberPredicate(where, topProjectionSegment.getAlias()) : Optional.empty();
        Optional<PaginationValueSegment> offset = rowNumberPredicate.isPresent() ? createOffsetWithRowNumber(rowNumberPredicate.get()) : Optional.empty();
        PaginationValueSegment rowCount = topProjectionSegment.getTop();
        return new PaginationContext(offset.orElse(null), rowCount, parameters);
    }
    
    private Optional<ExpressionSegment> getRowNumberPredicate(final ExpressionSegment where, final String rowNumberAlias) {
        if (!(where instanceof BinaryOperationExpression)) {
            return Optional.empty();
        }
        String operator = ((BinaryOperationExpression) where).getOperator();
        if (((BinaryOperationExpression) where).getLeft() instanceof ColumnSegment && isRowNumberColumn((ColumnSegment) ((BinaryOperationExpression) where).getLeft(), rowNumberAlias)
            && isCompareCondition(operator)) {
            return Optional.of(where);
        }
        if ("and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator) || "or".equalsIgnoreCase(operator)) {
            Optional<ExpressionSegment> left = getRowNumberPredicate(((BinaryOperationExpression) where).getLeft(), rowNumberAlias);
            if (left.isPresent()) {
                return left;
            }
            Optional<ExpressionSegment> right = getRowNumberPredicate(((BinaryOperationExpression) where).getRight(), rowNumberAlias);
            if (right.isPresent()) {
                return right;
            }
        }
        return Optional.empty();
    }
    
    private boolean isRowNumberColumn(final ColumnSegment columnSegment, final String rowNumberAlias) {
        return columnSegment.getIdentifier().getValue().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final String operator) {
        return ">".equals(operator) || ">=".equals(operator);
    }
    
    private Optional<PaginationValueSegment> createOffsetWithRowNumber(final ExpressionSegment predicateSegment) {
        if (!(predicateSegment instanceof BinaryOperationExpression)) {
            return Optional.empty();
        }
        String operator = ((BinaryOperationExpression) predicateSegment).getOperator();
        switch (operator) {
            case ">":
                return Optional.of(createRowNumberValueSegment(((BinaryOperationExpression) predicateSegment).getRight(), false));
            case ">=":
                return Optional.of(createRowNumberValueSegment(((BinaryOperationExpression) predicateSegment).getRight(), true));
            default:
                return Optional.empty();
        }
    }
    
    private RowNumberValueSegment createRowNumberValueSegment(final ExpressionSegment expression, final boolean boundOpened) {
        int startIndex = expression.getStartIndex();
        int stopIndex = expression.getStopIndex();
        return expression instanceof LiteralExpressionSegment
                ? new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, (int) ((LiteralExpressionSegment) expression).getLiterals(), boundOpened)
                : new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
    }
}
