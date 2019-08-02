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
import org.apache.shardingsphere.core.optimize.sharding.segment.select.item.SelectItems;
import org.apache.shardingsphere.core.optimize.sharding.segment.select.pagination.Pagination;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

/**
 * Pagination engine for row number.
 *
 * @author zhangliang
 */
public final class RowNumberPaginationEngine {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private static final Collection<String> ROW_NUMBER_IDENTIFIERS = new HashSet<>();
    
    static {
        ROW_NUMBER_IDENTIFIERS.add("rownum");
        ROW_NUMBER_IDENTIFIERS.add("ROW_NUMBER");
    }
    
    /**
     * Create pagination.
     * 
     * @param andPredicates and predicates
     * @param selectItems select items
     * @param parameters SQL parameters
     * @return pagination
     */
    public Pagination createPagination(final Collection<AndPredicate> andPredicates, final SelectItems selectItems, final List<Object> parameters) {
        Optional<String> rowNumberAlias = isRowNumberAlias(selectItems);
        if (!rowNumberAlias.isPresent()) {
            return new Pagination(null, null, parameters);
        }
        Collection<PredicateSegment> rowNumberPredicates = getRowNumberPredicates(andPredicates, rowNumberAlias.get());
        return rowNumberPredicates.isEmpty() ? new Pagination(null, null, parameters) : createPaginationWithRowNumber(rowNumberPredicates, parameters);
    }
    
    private Collection<PredicateSegment> getRowNumberPredicates(final Collection<AndPredicate> andPredicates, final String rowNumberAlias) {
        Collection<PredicateSegment> result = new LinkedList<>();
        for (AndPredicate each : andPredicates) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (isRowNumberColumn(predicate, rowNumberAlias) && isCompareCondition(predicate)) {
                    result.add(predicate);
                }
            }
        }
        return result;
    }
    
    private Optional<String> isRowNumberAlias(final SelectItems selectItems) {
        for (String each : ROW_NUMBER_IDENTIFIERS) {
            Optional<String> result = selectItems.findAlias(each);
            if (result.isPresent()) {
                return result;
            }
        }
        return Optional.absent();
    }
    
    private boolean isRowNumberColumn(final PredicateSegment predicate, final String rowNumberAlias) {
        return ROW_NUMBER_IDENTIFIERS.contains(predicate.getColumn().getName()) || predicate.getColumn().getName().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final PredicateSegment predicate) {
        if (predicate.getRightValue() instanceof PredicateCompareRightValue) {
            String operator = ((PredicateCompareRightValue) predicate.getRightValue()).getOperator();
            return "<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator);
        }
        return false;
    }
    
    private Pagination createPaginationWithRowNumber(final Collection<PredicateSegment> rowNumberPredicates, final List<Object> parameters) {
        RowNumberValueSegment offset = null;
        RowNumberValueSegment rowCount = null;
        for (PredicateSegment each : rowNumberPredicates) {
            ExpressionSegment expression = ((PredicateCompareRightValue) each.getRightValue()).getExpression();
            switch (((PredicateCompareRightValue) each.getRightValue()).getOperator()) {
                case ">":
                    offset = createRowNumberValueSegment(expression, false);
                    break;
                case ">=":
                    offset = createRowNumberValueSegment(expression, true);
                    break;
                case "<":
                    rowCount = createRowNumberValueSegment(expression, false);
                    break;
                case "<=":
                    rowCount = createRowNumberValueSegment(expression, true);
                    break;
                default:
                    break;
            }
        }
        return new Pagination(offset, rowCount, parameters);
    }
    
    private RowNumberValueSegment createRowNumberValueSegment(final ExpressionSegment expression, final boolean boundOpened) {
        int startIndex = expression.getStartIndex();
        int stopIndex = expression.getStopIndex();
        return expression instanceof LiteralExpressionSegment
                ? new NumberLiteralRowNumberValueSegment(startIndex, stopIndex, (int) ((LiteralExpressionSegment) expression).getLiterals(), boundOpened)
                : new ParameterMarkerRowNumberValueSegment(startIndex, stopIndex, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
    }
}
