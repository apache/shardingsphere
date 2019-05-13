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

package org.apache.shardingsphere.core.parse.antlr.filler.sharding.dml.select;

import com.google.common.base.Optional;
import org.apache.shardingsphere.core.parse.antlr.filler.api.SQLSegmentFiller;
import org.apache.shardingsphere.core.parse.antlr.sql.context.limit.Limit;
import org.apache.shardingsphere.core.parse.antlr.sql.context.limit.LimitValue;
import org.apache.shardingsphere.core.parse.antlr.sql.context.selectitem.SelectItem;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.AndPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.core.parse.antlr.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.SQLStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.statement.dml.SelectStatement;
import org.apache.shardingsphere.core.parse.antlr.sql.token.OffsetToken;
import org.apache.shardingsphere.core.parse.antlr.sql.token.RowCountToken;

import java.util.Collection;
import java.util.LinkedList;
import java.util.TreeSet;

/**
 * Row number predicate filler for sharding.
 *
 * @author zhangliang
 */
public final class ShardingRowNumberPredicateFiller implements SQLSegmentFiller<OrPredicateSegment> {
    
    // TODO recognize database type, only oracle and sqlserver can use row number
    private final Collection<String> rowNumberIdentifiers;
    
    public ShardingRowNumberPredicateFiller() {
        rowNumberIdentifiers = new TreeSet<>(String.CASE_INSENSITIVE_ORDER);
        rowNumberIdentifiers.add("rownum");
        rowNumberIdentifiers.add("ROW_NUMBER");
    }
    
    @Override
    public void fill(final OrPredicateSegment sqlSegment, final SQLStatement sqlStatement) {
        SelectStatement selectStatement = (SelectStatement) sqlStatement;
        Optional<String> rowNumberAlias = findRowNumberAlias(selectStatement);
        Collection<PredicateSegment> rowNumberPredicates = getRowNumberPredicates(sqlSegment, rowNumberAlias.orNull());
        if (!rowNumberPredicates.isEmpty()) {
            fillLimit(selectStatement, rowNumberPredicates);
        }
    }
    
    private Optional<String> findRowNumberAlias(final SelectStatement selectStatement) {
        for (SelectItem each : selectStatement.getItems()) {
            if (rowNumberIdentifiers.contains(each.getExpression())) {
                return each.getAlias();
            }
        }
        return Optional.absent();
    }
    
    private Collection<PredicateSegment> getRowNumberPredicates(final OrPredicateSegment sqlSegment, final String rowNumberAlias) {
        Collection<PredicateSegment> result = new LinkedList<>();
        for (AndPredicateSegment each : sqlSegment.getAndPredicates()) {
            for (PredicateSegment predicate : each.getPredicates()) {
                if (isRowNumberColumn(predicate, rowNumberAlias) && isCompareCondition(predicate)) {
                    result.add(predicate);
                }
            }
        }
        return result;
    }
    
    private boolean isRowNumberColumn(final PredicateSegment predicate, final String rowNumberAlias) {
        return rowNumberIdentifiers.contains(predicate.getColumn().getName()) || predicate.getColumn().getName().equalsIgnoreCase(rowNumberAlias);
    }
    
    private boolean isCompareCondition(final PredicateSegment predicate) {
        if (!(predicate.getRightValue() instanceof PredicateCompareRightValue)) {
            return false;
        }
        String operator = ((PredicateCompareRightValue) predicate.getRightValue()).getOperator();
        return "<".equals(operator) || "<=".equals(operator) || ">".equals(operator) || ">=".equals(operator);
    }
    
    private void fillLimit(final SelectStatement selectStatement, final Collection<PredicateSegment> rowNumberPredicates) {
        Limit limit = new Limit();
        for (PredicateSegment each : rowNumberPredicates) {
            ExpressionSegment expression = ((PredicateCompareRightValue) each.getRightValue()).getExpression();
            switch (((PredicateCompareRightValue) each.getRightValue()).getOperator()) {
                case "<":
                    limit.setRowCount(createLimitValue(expression, false));
                    if (-1 != limit.getRowCount().getValue()) {
                        selectStatement.addSQLToken(new RowCountToken(expression.getStartIndex(), expression.getStopIndex(), limit.getRowCount().getValue()));
                    }
                    break;
                case "<=":
                    limit.setRowCount(createLimitValue(expression, true));
                    if (-1 != limit.getRowCount().getValue()) {
                        selectStatement.addSQLToken(new RowCountToken(expression.getStartIndex(), expression.getStopIndex(), limit.getRowCount().getValue()));
                    }
                    break;
                case ">":
                    limit.setOffset(createLimitValue(expression, false));
                    if (-1 != limit.getOffset().getValue()) {
                        selectStatement.addSQLToken(new OffsetToken(expression.getStartIndex(), expression.getStopIndex(), limit.getOffset().getValue()));
                    }
                    break;
                case ">=":
                    limit.setOffset(createLimitValue(expression, true));
                    if (-1 != limit.getOffset().getValue()) {
                        selectStatement.addSQLToken(new OffsetToken(expression.getStartIndex(), expression.getStopIndex(), limit.getOffset().getValue()));
                    }
                    break;
                default:
                    break;
            }
        }
        selectStatement.setLimit(limit);
    }
    
    private LimitValue createLimitValue(final ExpressionSegment expression, final boolean boundOpened) {
        return expression instanceof ParameterMarkerExpressionSegment ? new LimitValue(-1, ((ParameterMarkerExpressionSegment) expression).getParameterMarkerIndex(), boundOpened)
                : new LimitValue(((Number) ((LiteralExpressionSegment) expression).getLiterals()).intValue(), -1, boundOpened);
    }
}
