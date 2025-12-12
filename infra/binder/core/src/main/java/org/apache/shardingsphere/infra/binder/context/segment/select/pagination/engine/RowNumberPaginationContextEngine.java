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

import com.google.common.base.Strings;
import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.database.connector.core.metadata.database.metadata.option.pagination.DialectPaginationOption;
import org.apache.shardingsphere.infra.binder.context.segment.select.pagination.PaginationContext;
import org.apache.shardingsphere.infra.binder.context.segment.select.projection.ProjectionsContext;
import org.apache.shardingsphere.sql.parser.statement.core.extractor.ExpressionExtractor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ExpressionRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.RowNumberValueSegment;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Pagination context engine for row number.
 */
@RequiredArgsConstructor
public final class RowNumberPaginationContextEngine {
    
    private final DialectPaginationOption option;
    
    /**
     * Create pagination context.
     *
     * @param expressions expressions
     * @param projectionsContext projections context
     * @param params SQL parameters
     * @return pagination context
     */
    public PaginationContext createPaginationContext(final Collection<ExpressionSegment> expressions, final ProjectionsContext projectionsContext, final List<Object> params) {
        Optional<String> rowNumberAlias = findRowNumberAlias(projectionsContext);
        if (!rowNumberAlias.isPresent()) {
            return new PaginationContext(null, null, params);
        }
        Collection<ExpressionSegment> allExpressions = expressions.stream().flatMap(each -> ExpressionExtractor.extractAllExpressions(each).stream()).collect(Collectors.toList());
        Collection<BinaryOperationExpression> rowNumberExpressions = getRowNumberExpressions(allExpressions, rowNumberAlias.get());
        return rowNumberExpressions.isEmpty() ? new PaginationContext(null, null, params) : createPaginationWithRowNumber(rowNumberExpressions, params);
    }
    
    private Collection<BinaryOperationExpression> getRowNumberExpressions(final Collection<ExpressionSegment> allExpressions, final String rowNumberAlias) {
        List<BinaryOperationExpression> result = new LinkedList<>();
        for (ExpressionSegment each : allExpressions) {
            if (isRowNumberColumn(each, rowNumberAlias) && isCompareCondition(each)) {
                result.add((BinaryOperationExpression) each);
            }
        }
        return result;
    }
    
    private Optional<String> findRowNumberAlias(final ProjectionsContext projectionsContext) {
        String rowNumberColumnName = option.getRowNumberColumnName();
        if (Strings.isNullOrEmpty(rowNumberColumnName)) {
            return Optional.empty();
        }
        Optional<String> result = projectionsContext.findAlias(rowNumberColumnName);
        return result.isPresent() ? result : Optional.of(rowNumberColumnName);
    }
    
    private boolean isRowNumberColumn(final ExpressionSegment predicate, final String rowNumberAlias) {
        if (predicate instanceof BinaryOperationExpression) {
            ExpressionSegment left = ((BinaryOperationExpression) predicate).getLeft();
            if (left instanceof ColumnSegment) {
                String leftColumnValue = ((ColumnSegment) left).getIdentifier().getValue();
                return leftColumnValue.equalsIgnoreCase(option.getRowNumberColumnName()) || leftColumnValue.equalsIgnoreCase(rowNumberAlias);
            }
            return false;
        }
        return false;
    }
    
    private boolean isCompareCondition(final ExpressionSegment predicate) {
        if (predicate instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) predicate).getOperator();
            return "<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator);
        }
        return false;
    }
    
    private PaginationContext createPaginationWithRowNumber(final Collection<BinaryOperationExpression> rowNumberPredicates, final List<Object> params) {
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
        return new PaginationContext(offset, rowCount, params);
    }
    
    private RowNumberValueSegment createRowNumberValueSegment(final ExpressionSegment expression, final boolean boundOpened) {
        int startIndex = expression.getStartIndex();
        int stopIndex = expression.getStopIndex();
        if (expression instanceof LiteralExpressionSegment) {
            return new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, Long.parseLong(((LiteralExpressionSegment) expression).getLiterals().toString()), boundOpened);
        }
        if (expression instanceof ParameterMarkerExpressionSegment) {
            return new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
        }
        return new ExpressionRowNumberValueSegment(startIndex, stopIndex, expression, boundOpened);
    }
}
