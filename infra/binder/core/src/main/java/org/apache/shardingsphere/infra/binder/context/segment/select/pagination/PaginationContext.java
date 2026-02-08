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

package org.apache.shardingsphere.infra.binder.context.segment.select.pagination;

import lombok.Getter;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.NumberLiteralPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.ParameterMarkerPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.ExpressionPaginationValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.rownum.ExpressionRowNumberValueSegment;

import java.util.List;
import java.util.Optional;

/**
 * Pagination context.
 */
public final class PaginationContext {
    
    @Getter
    private final boolean hasPagination;
    
    private final PaginationValueSegment offsetSegment;
    
    private final PaginationValueSegment rowCountSegment;
    
    private final Long actualOffset;
    
    private final Long actualRowCount;
    
    public PaginationContext(final PaginationValueSegment offsetSegment, final PaginationValueSegment rowCountSegment, final List<Object> params) {
        hasPagination = null != offsetSegment || null != rowCountSegment;
        this.offsetSegment = offsetSegment;
        this.rowCountSegment = rowCountSegment;
        actualOffset = null == offsetSegment ? Long.valueOf(0L) : getValue(offsetSegment, params);
        actualRowCount = null == rowCountSegment ? null : getValue(rowCountSegment, params);
    }
    
    private Long getValue(final PaginationValueSegment paginationValueSegment, final List<Object> params) {
        if (paginationValueSegment instanceof ParameterMarkerPaginationValueSegment) {
            Object obj = null == params || params.isEmpty() ? 0L : params.get(((ParameterMarkerPaginationValueSegment) paginationValueSegment).getParameterIndex());
            if (null == obj) {
                return null;
            }
            return obj instanceof Long ? (long) obj : (int) obj;
        }
        if (paginationValueSegment instanceof ExpressionRowNumberValueSegment) {
            return ((ExpressionRowNumberValueSegment) paginationValueSegment).getValue(params);
        }
        if (paginationValueSegment instanceof ExpressionPaginationValueSegment) {
            Long result = getValueFromExpression(((ExpressionPaginationValueSegment) paginationValueSegment).getExpression(), params);
            return result;
        }
        return ((NumberLiteralPaginationValueSegment) paginationValueSegment).getValue();
    }
    
    private Long getValueFromExpression(final ExpressionSegment expressionSegment, final List<Object> params) {
        if (expressionSegment instanceof BinaryOperationExpression) {
            return getValueFromBinaryOperationExpression((BinaryOperationExpression) expressionSegment, params);
        }
        if (expressionSegment instanceof CaseWhenExpression) {
            return getValueFromCaseWhenExpression((CaseWhenExpression) expressionSegment, params);
        }
        return null;
    }
    
    private Long getValueFromBinaryOperationExpression(final BinaryOperationExpression binaryOperationExpression, final List<Object> params) {
        Long left = getValueFromExpression(binaryOperationExpression.getLeft(), params);
        Long right = getValueFromExpression(binaryOperationExpression.getRight(), params);
        if (null == left || null == right) {
            return null;
        }
        switch (binaryOperationExpression.getOperator()) {
            case "+":
                return left + right;
            case "-":
                return left - right;
            case "*":
                return left * right;
            case "/":
                return 0L == right ? null : left / right;
            default:
                return null;
        }
    }
    
    private Long getValueFromCaseWhenExpression(final CaseWhenExpression caseWhenExpression, final List<Object> params) {
        if (null != caseWhenExpression.getCaseExpr()) {
            return null;
        }
        java.util.Iterator<ExpressionSegment> whenIterator = caseWhenExpression.getWhenExprs().iterator();
        java.util.Iterator<ExpressionSegment> thenIterator = caseWhenExpression.getThenExprs().iterator();
        while (whenIterator.hasNext() && thenIterator.hasNext()) {
            Boolean whenValue = getBooleanValueFromExpression(whenIterator.next(), params);
            if (Boolean.TRUE.equals(whenValue)) {
                return getValueFromExpression(thenIterator.next(), params);
            }
            thenIterator.next();
        }
        return null == caseWhenExpression.getElseExpr() ? null : getValueFromExpression(caseWhenExpression.getElseExpr(), params);
    }
    
    private Boolean getBooleanValueFromExpression(final ExpressionSegment expressionSegment, final List<Object> params) {
        if (expressionSegment instanceof LiteralExpressionSegment) {
            Object literals = ((LiteralExpressionSegment) expressionSegment).getLiterals();
            if (literals instanceof Boolean) {
                return (Boolean) literals;
            }
            return null == literals ? null : Boolean.parseBoolean(literals.toString());
        }
        if (expressionSegment instanceof ParameterMarkerExpressionSegment) {
            Object obj = null == params || params.isEmpty() ? null : params.get(((ParameterMarkerExpressionSegment) expressionSegment).getParameterMarkerIndex());
            return null == obj ? null : Boolean.parseBoolean(obj.toString());
        }
        return null;
    }
    
    /**
     * Get offset segment.
     *
     * @return offset segment
     */
    public Optional<PaginationValueSegment> getOffsetSegment() {
        return Optional.ofNullable(offsetSegment);
    }
    
    /**
     * Get row count segment.
     *
     * @return row count segment
     */
    public Optional<PaginationValueSegment> getRowCountSegment() {
        return Optional.ofNullable(rowCountSegment);
    }
    
    /**
     * Get actual offset.
     *
     * @return actual offset
     */
    public long getActualOffset() {
        if (null == offsetSegment) {
            return 0L;
        }
        return offsetSegment.isBoundOpened() ? actualOffset - 1 : actualOffset;
    }
    
    /**
     * Get actual row count.
     *
     * @return actual row count
     */
    public Optional<Long> getActualRowCount() {
        if (null == rowCountSegment) {
            return Optional.empty();
        }
        return Optional.of(rowCountSegment.isBoundOpened() ? actualRowCount + 1L : actualRowCount);
    }
    
    /**
     * Get offset parameter index.
     *
     * @return offset parameter index
     */
    public Optional<Integer> getOffsetParameterIndex() {
        // TODO handle offsetSegment instance of ExpressionRowNumberValueSegment
        return offsetSegment instanceof ParameterMarkerPaginationValueSegment ? Optional.of(((ParameterMarkerPaginationValueSegment) offsetSegment).getParameterIndex()) : Optional.empty();
    }
    
    /**
     * Get row count parameter index.
     *
     * @return row count parameter index
     */
    public Optional<Integer> getRowCountParameterIndex() {
        // TODO handle offsetSegment instance of ExpressionRowNumberValueSegment
        return rowCountSegment instanceof ParameterMarkerPaginationValueSegment
                ? Optional.of(((ParameterMarkerPaginationValueSegment) rowCountSegment).getParameterIndex())
                : Optional.empty();
    }
    
    /**
     * Get revised offset.
     *
     * @return revised offset
     */
    public long getRevisedOffset() {
        return 0L;
    }
    
    /**
     * Get revised row count.
     *
     * @param selectStatementContext select statement context
     * @return revised row count
     */
    public long getRevisedRowCount(final SelectStatementContext selectStatementContext) {
        if (isMaxRowCount(selectStatementContext)) {
            return Integer.MAX_VALUE;
        }
        return rowCountSegment instanceof LimitValueSegment ? actualOffset + actualRowCount : actualRowCount;
    }
    
    private boolean isMaxRowCount(final SelectStatementContext selectStatementContext) {
        return (!selectStatementContext.getGroupByContext().getItems().isEmpty()
                || !selectStatementContext.getProjectionsContext().getAggregationProjections().isEmpty()) && !selectStatementContext.isSameGroupByAndOrderByItems();
    }
}
