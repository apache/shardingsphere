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

package org.apache.shardingsphere.infra.binder.segment.select.having.engine;

import org.apache.shardingsphere.infra.binder.segment.select.having.HavingColumn;
import org.apache.shardingsphere.infra.binder.segment.select.having.HavingContext;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.util.SQLUtil;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Having context engine.
 */
public final class HavingContextEngine {
    
    /**
     * Create having context.
     *
     * @param selectStatement select statement
     * @return having context
     */
    public HavingContext createHavingContext(final SelectStatement selectStatement) {
        Optional<HavingSegment> havingSegment = selectStatement.getHaving();
        if (!havingSegment.isPresent()) {
            return new HavingContext(null, false, new LinkedList<>());
        }
        Collection<HavingColumn> columns = new LinkedList<>();
        ExpressionSegment segment = havingSegment.get().getExpr();
        extractHavingColumn(segment, columns);
        String havingExpression = segment instanceof BinaryOperationExpression ? ((BinaryOperationExpression) segment).getText() : "";
        return new HavingContext(havingExpression, !columns.isEmpty(), columns);
    }
    
    private void extractHavingColumn(final ExpressionSegment expression, final Collection<HavingColumn> columns) {
        if (expression instanceof BinaryOperationExpression) {
            extractHavingColumnFromBinaryOperationExpression(((BinaryOperationExpression) expression).getLeft(), columns);
            extractHavingColumnFromBinaryOperationExpression(((BinaryOperationExpression) expression).getRight(), columns);
        }
    }
    
    private void extractHavingColumnFromBinaryOperationExpression(final ExpressionSegment expression, final Collection<HavingColumn> columns) {
        if (expression instanceof BinaryOperationExpression) {
            extractHavingColumn(expression, columns);
        }
        if (expression instanceof AggregationProjectionSegment) {
            extractHavingColumnFromAggregationProjectionSegment((AggregationProjectionSegment) expression, columns);
        }
        if (expression instanceof ExpressionProjectionSegment) {
            extractHavingColumnFromExpressionProjectionSegment((ExpressionProjectionSegment) expression, columns);
        }
        if (expression instanceof ColumnSegment) {
            extractHavingColumnFromColumnSegment((ColumnSegment) expression, columns);
        }
    }
    
    private void extractHavingColumnFromColumnSegment(final ColumnSegment segment, final Collection<HavingColumn> columns) {
        columns.add(new HavingColumn(segment));
    }
    
    private void extractHavingColumnFromExpressionProjectionSegment(final ExpressionProjectionSegment segment, final Collection<HavingColumn> columns) {
        columns.add(new HavingColumn(new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), new IdentifierValue(segment.getText()))));
    }
    
    private void extractHavingColumnFromAggregationProjectionSegment(final AggregationProjectionSegment segment, final Collection<HavingColumn> columns) {
        String columnLabel = SQLUtil.getExactlyValue(segment.getType().name() + segment.getInnerExpression());
        columns.add(new HavingColumn(new ColumnSegment(segment.getStartIndex(), segment.getStopIndex(), new IdentifierValue(columnLabel))));
    }
}
