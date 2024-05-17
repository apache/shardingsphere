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

package org.apache.shardingsphere.sql.parser.sql.common.util;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.enums.LogicalOperator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ValuesExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.IntervalExpressionProjection;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.join.OuterJoinExpression;
import org.apache.shardingsphere.sql.parser.sql.dialect.segment.oracle.multiset.MultisetExpression;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

/**
 * Expression extract utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class ExpressionExtractUtils {
    
    /**
     * Get and predicate collection.
     * 
     * @param expression expression segment
     * @return and predicate collection
     */
    public static Collection<AndPredicate> getAndPredicates(final ExpressionSegment expression) {
        Collection<AndPredicate> result = new LinkedList<>();
        extractAndPredicates(result, expression);
        return result;
    }
    
    private static void extractAndPredicates(final Collection<AndPredicate> result, final ExpressionSegment expression) {
        if (!(expression instanceof BinaryOperationExpression)) {
            result.add(createAndPredicate(expression));
            return;
        }
        BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(binaryExpression.getOperator());
        if (logicalOperator.isPresent() && LogicalOperator.OR == logicalOperator.get()) {
            extractAndPredicates(result, binaryExpression.getLeft());
            extractAndPredicates(result, binaryExpression.getRight());
        } else if (logicalOperator.isPresent() && LogicalOperator.AND == logicalOperator.get()) {
            Collection<AndPredicate> predicates = getAndPredicates(binaryExpression.getRight());
            for (AndPredicate each : getAndPredicates(binaryExpression.getLeft())) {
                extractCombinedAndPredicates(result, each, predicates);
            }
        } else {
            result.add(createAndPredicate(expression));
        }
    }
    
    private static void extractCombinedAndPredicates(final Collection<AndPredicate> result, final AndPredicate current, final Collection<AndPredicate> predicates) {
        for (AndPredicate each : predicates) {
            AndPredicate predicate = new AndPredicate();
            predicate.getPredicates().addAll(current.getPredicates());
            predicate.getPredicates().addAll(each.getPredicates());
            result.add(predicate);
        }
    }
    
    private static AndPredicate createAndPredicate(final ExpressionSegment expression) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().add(expression);
        return result;
    }
    
    /**
     * Get parameter marker expression collection.
     * 
     * @param expressions expression collection
     * @return parameter marker expression collection
     */
    public static List<ParameterMarkerExpressionSegment> getParameterMarkerExpressions(final Collection<ExpressionSegment> expressions) {
        List<ParameterMarkerExpressionSegment> result = new ArrayList<>();
        extractParameterMarkerExpressions(result, expressions);
        return result;
    }
    
    private static void extractParameterMarkerExpressions(final List<ParameterMarkerExpressionSegment> segments, final Collection<ExpressionSegment> expressions) {
        for (ExpressionSegment each : expressions) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                segments.add((ParameterMarkerExpressionSegment) each);
            }
            // TODO support more expression type if necessary
            if (each instanceof BinaryOperationExpression) {
                extractParameterMarkerExpressions(segments, Collections.singleton(((BinaryOperationExpression) each).getLeft()));
                extractParameterMarkerExpressions(segments, Collections.singleton(((BinaryOperationExpression) each).getRight()));
            }
            if (each instanceof FunctionSegment) {
                extractParameterMarkerExpressions(segments, ((FunctionSegment) each).getParameters());
            }
            if (each instanceof TypeCastExpression) {
                extractParameterMarkerExpressions(segments, Collections.singleton(((TypeCastExpression) each).getExpression()));
            }
            if (each instanceof InExpression) {
                extractParameterMarkerExpressions(segments, ((InExpression) each).getExpressionList());
            }
        }
    }
    
    /**
     * Extract join conditions.
     * 
     * @param joinConditions join conditions
     * @param whereSegments where segments
     */
    public static void extractJoinConditions(final Collection<BinaryOperationExpression> joinConditions, final Collection<WhereSegment> whereSegments) {
        for (WhereSegment each : whereSegments) {
            if (each.getExpr() instanceof BinaryOperationExpression && ((BinaryOperationExpression) each.getExpr()).getLeft() instanceof ColumnSegment
                    && ((BinaryOperationExpression) each.getExpr()).getRight() instanceof ColumnSegment) {
                joinConditions.add((BinaryOperationExpression) each.getExpr());
            }
        }
    }
    
    /**
     * Extract columns.
     *
     * @param expression expression
     * @param containsSubQuery contains sub query or not
     * @return columns
     */
    public static Collection<ColumnSegment> extractColumns(final ExpressionSegment expression, final boolean containsSubQuery) {
        if (expression instanceof ColumnSegment) {
            return Collections.singletonList((ColumnSegment) expression);
        }
        Collection<ColumnSegment> result = new LinkedList<>();
        if (expression instanceof AggregationProjectionSegment) {
            for (ExpressionSegment each : ((AggregationProjectionSegment) expression).getParameters()) {
                result.addAll(extractColumns(each, containsSubQuery));
            }
        }
        if (expression instanceof BetweenExpression) {
            result.addAll(extractColumns(((BetweenExpression) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((BetweenExpression) expression).getBetweenExpr(), containsSubQuery));
            result.addAll(extractColumns(((BetweenExpression) expression).getAndExpr(), containsSubQuery));
        }
        if (expression instanceof BinaryOperationExpression) {
            result.addAll(extractColumns(((BinaryOperationExpression) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((BinaryOperationExpression) expression).getRight(), containsSubQuery));
        }
        if (expression instanceof CaseWhenExpression) {
            result.addAll(extractColumns(((CaseWhenExpression) expression).getCaseExpr(), containsSubQuery));
            result.addAll(extractColumns(((CaseWhenExpression) expression).getElseExpr(), containsSubQuery));
            ((CaseWhenExpression) expression).getWhenExprs().forEach(each -> result.addAll(extractColumns(each, containsSubQuery)));
            ((CaseWhenExpression) expression).getThenExprs().forEach(each -> result.addAll(extractColumns(each, containsSubQuery)));
        }
        if (expression instanceof OuterJoinExpression) {
            result.add(((OuterJoinExpression) expression).getColumnName());
        }
        if (expression instanceof CommonTableExpressionSegment) {
            result.addAll(((CommonTableExpressionSegment) expression).getColumns());
        }
        if (expression instanceof DatetimeExpression) {
            result.addAll(extractColumns(((DatetimeExpression) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((DatetimeExpression) expression).getRight(), containsSubQuery));
        }
        if (expression instanceof ExpressionProjectionSegment) {
            result.addAll(extractColumns(((ExpressionProjectionSegment) expression).getExpr(), containsSubQuery));
        }
        if (expression instanceof FunctionSegment) {
            for (ExpressionSegment each : ((FunctionSegment) expression).getParameters()) {
                result.addAll(extractColumns(each, containsSubQuery));
            }
        }
        if (expression instanceof InExpression) {
            result.addAll(extractColumns(((InExpression) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((InExpression) expression).getRight(), containsSubQuery));
        }
        if (expression instanceof IntervalExpressionProjection) {
            result.addAll(extractColumns(((IntervalExpressionProjection) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((IntervalExpressionProjection) expression).getRight(), containsSubQuery));
            result.addAll(extractColumns(((IntervalExpressionProjection) expression).getMinus(), containsSubQuery));
        }
        if (expression instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expression).getItems()) {
                result.addAll(extractColumns(each, containsSubQuery));
            }
        }
        if (expression instanceof MatchAgainstExpression) {
            result.addAll(((MatchAgainstExpression) expression).getColumns());
            result.addAll(extractColumns(((MatchAgainstExpression) expression).getExpr(), containsSubQuery));
        }
        if (expression instanceof MultisetExpression) {
            result.addAll(extractColumns(((MultisetExpression) expression).getLeft(), containsSubQuery));
            result.addAll(extractColumns(((MultisetExpression) expression).getRight(), containsSubQuery));
        }
        if (expression instanceof NotExpression) {
            result.addAll(extractColumns(((NotExpression) expression).getExpression(), containsSubQuery));
        }
        if (expression instanceof ValuesExpression) {
            for (InsertValuesSegment each : ((ValuesExpression) expression).getRowConstructorList()) {
                each.getValues().forEach(value -> result.addAll(extractColumns(value, containsSubQuery)));
            }
        }
        if (expression instanceof SubquerySegment && containsSubQuery) {
            ColumnExtractor.extractFromSelectStatement(result, ((SubquerySegment) expression).getSelect(), true);
        }
        if (expression instanceof SubqueryExpressionSegment && containsSubQuery) {
            ColumnExtractor.extractFromSelectStatement(result, ((SubqueryExpressionSegment) expression).getSubquery().getSelect(), true);
        }
        return result;
    }
}
