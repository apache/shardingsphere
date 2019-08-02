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

package org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.engine;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.PaginationValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.top.TopSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;

import java.util.Collection;
import java.util.List;

/**
 * Pagination engine for top.
 *
 * @author zhangliang
 */
public final class TopPaginationEngine {
    
    /**
     * Create pagination.
     * 
     * @param topSegment top segment
     * @param andPredicates and predicates
     * @param parameters SQL parameters
     * @return pagination
     */
    public Pagination createPagination(final TopSegment topSegment, final Collection<AndPredicate> andPredicates, final List<Object> parameters) {
        Optional<PredicateSegment> rowNumberPredicate = getRowNumberPredicate(andPredicates, topSegment.getRowNumberAlias());
        Optional<PaginationValueSegment> offset = rowNumberPredicate.isPresent() ? createOffsetWithRowNumber(rowNumberPredicate.get()) : Optional.<PaginationValueSegment>absent();
        PaginationValueSegment rowCount = topSegment.getTop();
        return new Pagination(offset.orNull(), rowCount, parameters);
    }
    
    private Optional<PredicateSegment> getRowNumberPredicate(final Collection<AndPredicate> andPredicates, final String rowNumberAlias) {
        for (AndPredicate each : andPredicates) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (isRowNumberColumn(predicate, rowNumberAlias) && isCompareCondition(predicate)) {
                    return Optional.of(predicate);
                }
            }
        }
        return Optional.absent();
    }
    
    private boolean isRowNumberColumn(final PredicateSegment predicate, final String rowNumberAlias) {
        return predicate.getColumn().getName().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final PredicateSegment predicate) {
        if (predicate.getRightValue() instanceof PredicateCompareRightValue) {
            String operator = ((PredicateCompareRightValue) predicate.getRightValue()).getOperator();
            return ">".equals(operator) || ">=".equals(operator);
        }
        return false;
    }
    
    private Optional<PaginationValueSegment> createOffsetWithRowNumber(final PredicateSegment predicateSegment) {
        ExpressionSegment expression = ((PredicateCompareRightValue) predicateSegment.getRightValue()).getExpression();
        switch (((PredicateCompareRightValue) predicateSegment.getRightValue()).getOperator()) {
            case ">":
                return Optional.<PaginationValueSegment>of(createRowNumberValueSegment(expression, false));
            case ">=":
                return Optional.<PaginationValueSegment>of(createRowNumberValueSegment(expression, true));
            default:
                return Optional.absent();
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
