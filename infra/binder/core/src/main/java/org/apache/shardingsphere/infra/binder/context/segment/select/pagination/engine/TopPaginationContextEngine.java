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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination.engine;

import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.top.TopProjectionSegment;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pagination context engine for top.
 */
public final class TopPaginationContextEngine {
    
    /**
     * Create pagination context.
     *
     * @param topProjectionSegment top projection segment
     * @param expressions expressions
     * @param params SQL parameters
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final TopProjectionSegment topProjectionSegment, final Collection<ExpressionSegment> expressions, final List<Object> params) {
        Collection<ExpressionSegment> allExpressions = expressions.stream().flatMap(each -> ExpressionExtractor.extractAllExpressions(each).stream()).collect(Collectors.toList());
        Optional<ExpressionSegment> rowNumberExpression = expressions.isEmpty() ? Optional.empty() : getRowNumberExpression(allExpressions, topProjectionSegment.getAlias());
        Optional<PaginationValueSegment> offset = rowNumberExpression.isPresent() ? createOffsetWithRowNumber(rowNumberExpression.get()) : Optional.empty();
        PaginationValueSegment rowCount = topProjectionSegment.getTop();
        return new PaginationContext(offset.orElse(null), rowCount, params);
    }
    
    private Optional<ExpressionSegment> getRowNumberExpression(final Collection<ExpressionSegment> allExpressions, final String rowNumberAlias) {
        for (ExpressionSegment each : allExpressions) {
            if (isRowNumberColumn(each, rowNumberAlias) && isCompareCondition(each)) {
                return Optional.of(each);
            }
        }
        return Optional.empty();
    }
    
    private boolean isRowNumberColumn(final ExpressionSegment predicate, final String rowNumberAlias) {
        if (predicate instanceof BinaryOperationExpression) {
            ExpressionSegment left = ((BinaryOperationExpression) predicate).getLeft();
            return left instanceof ColumnSegment && ((ColumnSegment) left).getIdentifier().getValue().equalsIgnoreCase(rowNumberAlias);
        }
        return false;
    }
    
    private boolean isCompareCondition(final ExpressionSegment predicate) {
        if (predicate instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) predicate).getOperator();
            return ">".equals(operator) || ">=".equals(operator);
        }
        return false;
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
                ? new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, Long.parseLong(((LiteralExpressionSegment) expression).getLiterals().toString()), boundOpened)
                : new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
    }
}
