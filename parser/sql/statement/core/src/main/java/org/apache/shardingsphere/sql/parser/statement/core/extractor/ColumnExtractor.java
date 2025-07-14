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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.DatetimeProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.GroupBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ColumnOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.OrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.CollectionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;

/**
 * Column extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ColumnExtractor {
    
    /**
     * Extract column segments from expression segment.
     *
     * @param expression to be extracted expression segment
     * @return column segments
     */
    public static Collection<ColumnSegment> extract(final ExpressionSegment expression) {
        Collection<ColumnSegment> result = new LinkedList<>();
        if (expression instanceof BinaryOperationExpression) {
            extractColumnsInBinaryOperationExpression((BinaryOperationExpression) expression, result);
        }
        if (expression instanceof InExpression) {
            extractColumnsInInExpression((InExpression) expression, result);
        }
        if (expression instanceof BetweenExpression) {
            extractColumnsInBetweenExpression((BetweenExpression) expression, result);
        }
        if (expression instanceof AggregationProjectionSegment) {
            extractColumnsInAggregationProjectionSegment((AggregationProjectionSegment) expression, result);
        }
        if (expression instanceof FunctionSegment) {
            extractColumnsInFunctionSegment((FunctionSegment) expression, result);
        }
        return result;
    }
    
    private static void extractColumnsInInExpression(final InExpression expression, final Collection<ColumnSegment> result) {
        if (expression.getLeft() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getLeft());
        }
        if (expression.getLeft() instanceof RowExpression) {
            extractColumnsInRowExpression((RowExpression) expression.getLeft(), result);
        }
        if (expression.getLeft() instanceof FunctionSegment) {
            extractColumnsInFunctionSegment((FunctionSegment) expression.getLeft(), result);
        }
        result.addAll(extract(expression.getRight()));
    }
    
    private static void extractColumnsInBinaryOperationExpression(final BinaryOperationExpression expression, final Collection<ColumnSegment> result) {
        if (expression.getLeft() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getLeft());
        }
        if (expression.getRight() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getRight());
        }
        if (expression.getLeft() instanceof OuterJoinExpression) {
            result.add(((OuterJoinExpression) expression.getLeft()).getColumnName());
        }
        if (expression.getRight() instanceof OuterJoinExpression) {
            result.add(((OuterJoinExpression) expression.getRight()).getColumnName());
        }
        result.addAll(extract(expression.getLeft()));
        result.addAll(extract(expression.getRight()));
    }
    
    private static void extractColumnsInBetweenExpression(final BetweenExpression expression, final Collection<ColumnSegment> result) {
        if (expression.getLeft() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getLeft());
        }
        if (expression.getBetweenExpr() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getBetweenExpr());
        }
        if (expression.getAndExpr() instanceof ColumnSegment) {
            result.add((ColumnSegment) expression.getAndExpr());
        }
        result.addAll(extract(expression.getLeft()));
        result.addAll(extract(expression.getBetweenExpr()));
        result.addAll(extract(expression.getAndExpr()));
    }
    
    private static void extractColumnsInRowExpression(final RowExpression expression, final Collection<ColumnSegment> result) {
        for (ExpressionSegment each : expression.getItems()) {
            if (each instanceof ColumnSegment) {
                result.add((ColumnSegment) each);
            }
        }
    }
    
    private static void extractColumnsInAggregationProjectionSegment(final AggregationProjectionSegment expression, final Collection<ColumnSegment> result) {
        for (ExpressionSegment each : expression.getParameters()) {
            if (each instanceof ColumnSegment) {
                result.add((ColumnSegment) each);
            } else {
                result.addAll(extract(each));
            }
        }
    }
    
    private static void extractColumnsInFunctionSegment(final FunctionSegment expression, final Collection<ColumnSegment> result) {
        for (ExpressionSegment each : expression.getParameters()) {
            if (each instanceof ColumnSegment) {
                result.add((ColumnSegment) each);
            } else {
                result.addAll(extract(each));
            }
        }
    }
    
    /**
     * Extract column segments.
     *
     * @param whereSegments where segments
     * @return column segments
     */
    public static Collection<ColumnSegment> extractColumnSegments(final Collection<WhereSegment> whereSegments) {
        Collection<ColumnSegment> result = new LinkedList<>();
        for (WhereSegment each : whereSegments) {
            for (ExpressionSegment expression : ExpressionExtractor.extractAllExpressions(each.getExpr())) {
                result.addAll(extract(expression));
            }
        }
        return result;
    }
    
    /**
     * Extract column segments.
     *
     * @param columnSegments column segments
     * @param statement select statement
     * @param containsSubQuery whether contains subquery
     */
    public static void extractFromSelectStatement(final Collection<ColumnSegment> columnSegments, final SelectStatement statement, final boolean containsSubQuery) {
        extractFromProjections(columnSegments, statement.getProjections().getProjections(), containsSubQuery);
        extractFromSelectStatementWithoutProjection(columnSegments, statement, containsSubQuery);
    }
    
    /**
     * Extract from select statement without projection.
     *
     * @param columnSegments column segments
     * @param statement select statement
     * @param containsSubQuery whether contains subquery
     */
    public static void extractFromSelectStatementWithoutProjection(final Collection<ColumnSegment> columnSegments, final SelectStatement statement, final boolean containsSubQuery) {
        statement.getFrom().ifPresent(optional -> extractFromTable(columnSegments, optional, containsSubQuery));
        statement.getWhere().ifPresent(optional -> extractFromWhere(columnSegments, optional, containsSubQuery));
        statement.getGroupBy().ifPresent(optional -> extractFromGroupBy(columnSegments, optional, containsSubQuery));
        statement.getHaving().ifPresent(optional -> extractFromHaving(columnSegments, optional, containsSubQuery));
        statement.getOrderBy().ifPresent(optional -> extractFromOrderBy(columnSegments, optional, containsSubQuery));
        statement.getCombine().ifPresent(optional -> extractFromSelectStatement(columnSegments, optional.getLeft().getSelect(), containsSubQuery));
        statement.getCombine().ifPresent(optional -> extractFromSelectStatement(columnSegments, optional.getRight().getSelect(), containsSubQuery));
    }
    
    /**
     * Extract column segments.
     *
     * @param columnSegments column segments
     * @param projections projection segments
     * @param containsSubQuery contains subquery
     */
    public static void extractFromProjections(final Collection<ColumnSegment> columnSegments, final Collection<ProjectionSegment> projections, final boolean containsSubQuery) {
        for (ProjectionSegment each : projections) {
            if (each instanceof ColumnProjectionSegment) {
                columnSegments.add(((ColumnProjectionSegment) each).getColumn());
            }
            if (each instanceof AggregationProjectionSegment) {
                for (ExpressionSegment parameter : ((AggregationProjectionSegment) each).getParameters()) {
                    columnSegments.addAll(ExpressionExtractor.extractColumns(parameter, containsSubQuery));
                }
            }
            if (each instanceof DatetimeProjectionSegment) {
                columnSegments.addAll(ExpressionExtractor.extractColumns(((DatetimeProjectionSegment) each).getLeft(), containsSubQuery));
                columnSegments.addAll(ExpressionExtractor.extractColumns(((DatetimeProjectionSegment) each).getRight(), containsSubQuery));
            }
            if (each instanceof ExpressionProjectionSegment) {
                columnSegments.addAll(ExpressionExtractor.extractColumns(((ExpressionProjectionSegment) each).getExpr(), containsSubQuery));
            }
            if (each instanceof IntervalExpressionProjection) {
                columnSegments.addAll(ExpressionExtractor.extractColumns(((IntervalExpressionProjection) each).getLeft(), containsSubQuery));
                columnSegments.addAll(ExpressionExtractor.extractColumns(((IntervalExpressionProjection) each).getRight(), containsSubQuery));
                columnSegments.addAll(ExpressionExtractor.extractColumns(((IntervalExpressionProjection) each).getMinus(), containsSubQuery));
            }
            if (each instanceof SubqueryProjectionSegment && containsSubQuery) {
                extractFromSelectStatement(columnSegments, ((SubqueryProjectionSegment) each).getSubquery().getSelect(), true);
            }
        }
    }
    
    private static void extractFromTable(final Collection<ColumnSegment> columnSegments, final TableSegment tableSegment, final boolean containsSubQuery) {
        if (tableSegment instanceof CollectionTableSegment) {
            columnSegments.addAll(ExpressionExtractor.extractColumns(((CollectionTableSegment) tableSegment).getExpressionSegment(), containsSubQuery));
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractFromTable(columnSegments, ((JoinTableSegment) tableSegment).getLeft(), containsSubQuery);
            extractFromTable(columnSegments, ((JoinTableSegment) tableSegment).getRight(), containsSubQuery);
            columnSegments.addAll(ExpressionExtractor.extractColumns(((JoinTableSegment) tableSegment).getCondition(), containsSubQuery));
            columnSegments.addAll(((JoinTableSegment) tableSegment).getUsing());
            columnSegments.addAll(((JoinTableSegment) tableSegment).getDerivedUsing());
        }
        if (tableSegment instanceof SubqueryTableSegment && containsSubQuery) {
            extractFromSelectStatement(columnSegments, ((SubqueryTableSegment) tableSegment).getSubquery().getSelect(), true);
        }
        if (tableSegment instanceof CommonTableExpressionSegment && containsSubQuery) {
            extractFromSelectStatement(columnSegments, ((CommonTableExpressionSegment) tableSegment).getSubquery().getSelect(), true);
        }
    }
    
    /**
     * Extract column segments.
     *
     * @param columnSegments column segments
     * @param whereSegment where segment
     * @param containsSubQuery contains subquery
     */
    public static void extractFromWhere(final Collection<ColumnSegment> columnSegments, final WhereSegment whereSegment, final boolean containsSubQuery) {
        columnSegments.addAll(ExpressionExtractor.extractColumns(whereSegment.getExpr(), containsSubQuery));
    }
    
    private static void extractFromGroupBy(final Collection<ColumnSegment> columnSegments, final GroupBySegment groupBySegment, final boolean containsSubQuery) {
        for (OrderByItemSegment each : groupBySegment.getGroupByItems()) {
            if (each instanceof ColumnOrderByItemSegment) {
                columnSegments.add(((ColumnOrderByItemSegment) each).getColumn());
            }
            if (each instanceof ExpressionOrderByItemSegment) {
                columnSegments.addAll(ExpressionExtractor.extractColumns(((ExpressionOrderByItemSegment) each).getExpr(), containsSubQuery));
            }
        }
    }
    
    private static void extractFromHaving(final Collection<ColumnSegment> columnSegments, final HavingSegment havingSegment, final boolean containsSubQuery) {
        columnSegments.addAll(ExpressionExtractor.extractColumns(havingSegment.getExpr(), containsSubQuery));
    }
    
    private static void extractFromOrderBy(final Collection<ColumnSegment> columnSegments, final OrderBySegment orderBySegment, final boolean containsSubQuery) {
        for (OrderByItemSegment each : orderBySegment.getOrderByItems()) {
            if (each instanceof ColumnOrderByItemSegment) {
                columnSegments.add(((ColumnOrderByItemSegment) each).getColumn());
            }
            if (each instanceof ExpressionOrderByItemSegment) {
                columnSegments.addAll(ExpressionExtractor.extractColumns(((ExpressionOrderByItemSegment) each).getExpr(), containsSubQuery));
            }
        }
    }
}
