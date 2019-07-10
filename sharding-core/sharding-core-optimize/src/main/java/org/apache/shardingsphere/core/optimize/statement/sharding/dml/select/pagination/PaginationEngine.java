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

package org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.pagination;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.optimize.statement.sharding.dml.select.item.SelectItems;
import org.apache.shardingsphere.core.parse.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.NumberLiteralRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.ParameterMarkerRowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.pagination.rownum.RowNumberValueSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.sql.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.TreeSet;

/**
 * Pagination engine.
 *
 * @author zhangliang
 */
public final class PaginationEngine {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private static final Collection<String> ROW_NUMBER_IDENTIFIERS = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
    
    static {
        ROW_NUMBER_IDENTIFIERS.add("rownum");
        ROW_NUMBER_IDENTIFIERS.add("ROW_NUMBER");
    }
    
    /**
     * Create pagination.
     * 
     * @param selectStatement SQL statement
     * @param selectItems select items
     * @param parameters SQL parameters
     * @return pagination
     */
    public Pagination createPagination(final SelectStatement selectStatement, final SelectItems selectItems, final List<Object> parameters) {
        Optional<LimitSegment> limitSegment = selectStatement.findSQLSegment(LimitSegment.class);
        if (limitSegment.isPresent()) {
            return new Pagination(limitSegment.get().getOffset().orNull(), limitSegment.get().getRowCount().orNull(), parameters);
        }
        Optional<OrPredicateSegment> orPredicateSegment = selectStatement.findSQLSegment(OrPredicateSegment.class);
        if (orPredicateSegment.isPresent()) {
            return createPaginationWithRowNumber(orPredicateSegment.get(), selectItems, parameters);
        }
        return new Pagination(null, null, parameters);
    }
    
    private Pagination createPaginationWithRowNumber(final OrPredicateSegment sqlSegment, final SelectItems selectItems, final List<Object> parameters) {
        Optional<String> rowNumberAlias = findRowNumberAlias(selectItems);
        Collection<PredicateSegment> rowNumberPredicates = getRowNumberPredicates(sqlSegment, rowNumberAlias.orNull());
        return rowNumberPredicates.isEmpty() ? new Pagination(null, null, parameters) : createPaginationWithRowNumber(rowNumberPredicates, parameters);
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
        return expression instanceof LiteralExpressionSegment
                ? new NumberLiteralRowNumberValueSegment(expression.getStartIndex(), expression.getStopIndex(), (int) ((LiteralExpressionSegment) expression).getLiterals(), boundOpened)
                : new ParameterMarkerRowNumberValueSegment(
                expression.getStartIndex(), expression.getStopIndex(), ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened);
    }
    
    private Optional<String> findRowNumberAlias(final SelectItems selectItems) {
        for (SelectItem each : selectItems.getItems()) {
            if (ROW_NUMBER_IDENTIFIERS.contains(each.getExpression())) {
                return each.getAlias();
            }
        }
        return Optional.absent();
    }
    
    private Collection<PredicateSegment> getRowNumberPredicates(final OrPredicateSegment sqlSegment, final String rowNumberAlias) {
        Collection<PredicateSegment> result = new LinkedList<>();
        for (AndPredicate each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (isRowNumberColumn(predicate, rowNumberAlias) && isCompareCondition(predicate)) {
                    result.add(predicate);
                }
            }
        }
        return result;
    }
    
    private boolean isRowNumberColumn(final PredicateSegment predicate, final String rowNumberAlias) {
        return ROW_NUMBER_IDENTIFIERS.contains(predicate.getColumn().getName()) || predicate.getColumn().getName().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final PredicateSegment predicate) {
        if (!(predicate.getRightValue() instanceof PredicateCompareRightValue)) {
            return false;
        }
        String operator = ((PredicateCompareRightValue) predicate.getRightValue()).getOperator();
        return "<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator);
    }
}
