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
import org.apache.shardingsphere.sql.parser.binder.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.rownum.RowNumberValueSegment;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Pagination context engine for row number.
 */
public final class RowNumberPaginationContextEngine {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private static final Collection<String> ROW_NUMBER_IDENTIFIERS = new HashSet<>();
    
    static {
        ROW_NUMBER_IDENTIFIERS.add("rownum");
        ROW_NUMBER_IDENTIFIERS.add("ROW_NUMBER");
    }
    
    /**
     * Create pagination context.
     * 
     * @param where where condition
     * @param projectionsContext projections context
     * @param parameters SQL parameters
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final ExpressionSegment where, final ProjectionsContext projectionsContext, final List<Object> parameters) {
        Optional<String> rowNumberAlias = isRowNumberAlias(projectionsContext);
        if (!rowNumberAlias.isPresent()) {
            return new PaginationContext(null, null, parameters);
        }
        Collection<BinaryOperationExpression> rowNumberPredicates = getRowNumberPredicates(where, rowNumberAlias.get());
        return rowNumberPredicates.isEmpty() ? new PaginationContext(null, null, parameters) : createPaginationWithRowNumber(rowNumberPredicates, parameters);
    }
    
    private Collection<BinaryOperationExpression> getRowNumberPredicates(final ExpressionSegment where, final String rowNumberAlias) {
        List<BinaryOperationExpression> result = new LinkedList<>();
        if (!(where instanceof BinaryOperationExpression)) {
            return result;
        }
        String operator = ((BinaryOperationExpression) where).getOperator();
        if (((BinaryOperationExpression) where).getLeft() instanceof ColumnSegment && isRowNumberColumn((ColumnSegment) ((BinaryOperationExpression) where).getLeft(), rowNumberAlias)
                && isCompareCondition(operator)) {
            result.add((BinaryOperationExpression) where);
            return result;
        }
        if ("and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator) || "or".equalsIgnoreCase(operator)) {
            Collection<BinaryOperationExpression> left = getRowNumberPredicates(((BinaryOperationExpression) where).getLeft(), rowNumberAlias);
            if (!left.isEmpty()) {
                return left;
            }
            Collection<BinaryOperationExpression> right = getRowNumberPredicates(((BinaryOperationExpression) where).getRight(), rowNumberAlias);
            if (!left.isEmpty()) {
                return right;
            }
        }
        return result;
    }
    
    private Optional<String> isRowNumberAlias(final ProjectionsContext projectionsContext) {
        for (String each : ROW_NUMBER_IDENTIFIERS) {
            Optional<String> result = projectionsContext.findAlias(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.empty();
    }
    
    private boolean isRowNumberColumn(final ColumnSegment column, final String rowNumberAlias) {
        return ROW_NUMBER_IDENTIFIERS.contains(column.getIdentifier().getValue()) || column.getIdentifier().getValue().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final String operator) {
        return "<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator);
    }
    
    private PaginationContext createPaginationWithRowNumber(final Collection<BinaryOperationExpression> rowNumberPredicates, final List<Object> parameters) {
        RowNumberValueSegment offset = null;
        RowNumberValueSegment rowCount = null;
        for (BinaryOperationExpression each : rowNumberPredicates) {
            String operator = each.getOperator();
            switch (operator) {
                case ">":
                    offset = createRowNumberValueSegment(each.getRight(), false);
                    break;
                case ">=":
                    offset = createRowNumberValueSegment(each.getRight(), true);
                    break;
                case "<":
                    rowCount = createRowNumberValueSegment(each.getRight(), false);
                    break;
                case "<=":
                    rowCount = createRowNumberValueSegment(each.getRight(), true);
                    break;
                default:
                    break;
            }
        }
        return new PaginationContext(offset, rowCount, parameters);
    }
    
    private RowNumberValueSegment createRowNumberValueSegment(final ExpressionSegment expression, final boolean boundOpened) {
        int startIndex = expression.getStartIndex();
        int stopIndex = expression.getStopIndex();
        return expression instanceof LiteralExpressionSegment
                ? new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, (int) ((LiteralExpressionSegment) expression).getLiterals(), boundOpened)
                : new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
    }
}
