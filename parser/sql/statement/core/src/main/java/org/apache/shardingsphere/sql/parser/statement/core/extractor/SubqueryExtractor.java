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
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.datetime.DatetimeExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.match.MatchAgainstExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Subquery extractor.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryExtractor {
    
    /**
     * Extract subquery segment from select statement.
     *
     * @param selectStatement select statement
     * @param needRecursive need recursive
     * @return subquery segments
     */
    public static Collection<SubquerySegment> extractSubquerySegments(final SelectStatement selectStatement, final boolean needRecursive) {
        List<SubquerySegment> result = new LinkedList<>();
        SubqueryType parentSubqueryType = selectStatement.getSubqueryType().orElse(null);
        extractSubquerySegments(result, selectStatement, needRecursive, parentSubqueryType);
        return result;
    }
    
    private static void extractSubquerySegments(final List<SubquerySegment> result, final SelectStatement selectStatement, final boolean needRecursive, final SubqueryType parentSubqueryType) {
        extractSubquerySegmentsFromProjections(result, selectStatement.getProjections(), needRecursive);
        selectStatement.getFrom().ifPresent(optional -> extractSubquerySegmentsFromTableSegment(result, optional, needRecursive));
        if (selectStatement.getWhere().isPresent()) {
            extractSubquerySegmentsFromWhere(result, selectStatement.getWhere().get().getExpr(), needRecursive);
        }
        if (selectStatement.getCombine().isPresent()) {
            extractSubquerySegmentsFromCombine(result, selectStatement.getCombine().get(), needRecursive, parentSubqueryType);
        }
        if (selectStatement.getWith().isPresent()) {
            extractSubquerySegmentsFromCTEs(result, selectStatement.getWith().get().getCommonTableExpressions(), needRecursive);
        }
    }
    
    private static void extractSubquerySegmentsFromCTEs(final List<SubquerySegment> result, final Collection<CommonTableExpressionSegment> withSegment, final boolean needRecursive) {
        for (CommonTableExpressionSegment each : withSegment) {
            each.getSubquery().getSelect().setSubqueryType(SubqueryType.WITH);
            result.add(each.getSubquery());
            extractRecursive(needRecursive, result, each.getSubquery().getSelect(), SubqueryType.TABLE);
        }
    }
    
    private static void extractRecursive(final boolean needRecursive, final List<SubquerySegment> result, final SelectStatement select, final SubqueryType parentSubqueryType) {
        if (needRecursive) {
            extractSubquerySegments(result, select, true, parentSubqueryType);
        }
    }
    
    private static void extractSubquerySegmentsFromProjections(final List<SubquerySegment> result, final ProjectionsSegment projections, final boolean needRecursive) {
        if (null == projections || projections.getProjections().isEmpty()) {
            return;
        }
        for (ProjectionSegment each : projections.getProjections()) {
            if (each instanceof SubqueryProjectionSegment) {
                SubquerySegment subquery = ((SubqueryProjectionSegment) each).getSubquery();
                subquery.getSelect().setSubqueryType(SubqueryType.PROJECTION);
                result.add(subquery);
                extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
            } else if (each instanceof ExpressionProjectionSegment) {
                extractSubquerySegmentsFromExpression(result, ((ExpressionProjectionSegment) each).getExpr(), SubqueryType.PROJECTION, needRecursive);
            }
        }
    }
    
    private static void extractSubquerySegmentsFromTableSegment(final List<SubquerySegment> result, final TableSegment tableSegment, final boolean needRecursive) {
        if (tableSegment instanceof SubqueryTableSegment) {
            extractSubquerySegmentsFromSubqueryTableSegment(result, (SubqueryTableSegment) tableSegment, needRecursive);
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getLeft(), needRecursive);
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getRight(), needRecursive);
        }
    }
    
    private static void extractSubquerySegmentsFromJoinTableSegment(final List<SubquerySegment> result, final TableSegment tableSegment, final boolean needRecursive) {
        if (tableSegment instanceof SubqueryTableSegment) {
            SubquerySegment subquery = ((SubqueryTableSegment) tableSegment).getSubquery();
            subquery.getSelect().setSubqueryType(SubqueryType.JOIN);
            result.add(subquery);
            extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
        } else if (tableSegment instanceof JoinTableSegment) {
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getLeft(), needRecursive);
            extractSubquerySegmentsFromJoinTableSegment(result, ((JoinTableSegment) tableSegment).getRight(), needRecursive);
        }
    }
    
    private static void extractSubquerySegmentsFromSubqueryTableSegment(final List<SubquerySegment> result, final SubqueryTableSegment subqueryTableSegment, final boolean needRecursive) {
        SubquerySegment subquery = subqueryTableSegment.getSubquery();
        subquery.getSelect().setSubqueryType(SubqueryType.TABLE);
        result.add(subquery);
        extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
    }
    
    private static void extractSubquerySegmentsFromWhere(final List<SubquerySegment> result, final ExpressionSegment expressionSegment, final boolean needRecursive) {
        extractSubquerySegmentsFromExpression(result, expressionSegment, SubqueryType.PREDICATE, needRecursive);
    }
    
    private static void extractSubquerySegmentsFromExpression(final List<SubquerySegment> result, final ExpressionSegment expressionSegment, final SubqueryType subqueryType,
                                                              final boolean needRecursive) {
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            SubquerySegment subquery = ((SubqueryExpressionSegment) expressionSegment).getSubquery();
            subquery.getSelect().setSubqueryType(subqueryType);
            result.add(subquery);
            extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
        }
        if (expressionSegment instanceof QuantifySubqueryExpression) {
            SubquerySegment subquery = ((QuantifySubqueryExpression) expressionSegment).getSubquery();
            subquery.getSelect().setSubqueryType(subqueryType);
            result.add(subquery);
            extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
        }
        if (expressionSegment instanceof ExistsSubqueryExpression) {
            SubquerySegment subquery = ((ExistsSubqueryExpression) expressionSegment).getSubquery();
            subquery.getSelect().setSubqueryType(subqueryType);
            result.add(subquery);
            extractRecursive(needRecursive, result, subquery.getSelect(), SubqueryType.TABLE);
        }
        if (expressionSegment instanceof ListExpression) {
            ((ListExpression) expressionSegment).getItems().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType, needRecursive));
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getLeft(), subqueryType, needRecursive);
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getRight(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof InExpression) {
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getLeft(), subqueryType, needRecursive);
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getRight(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof BetweenExpression) {
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getBetweenExpr(), subqueryType, needRecursive);
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getAndExpr(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof NotExpression) {
            extractSubquerySegmentsFromExpression(result, ((NotExpression) expressionSegment).getExpression(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof FunctionSegment) {
            ((FunctionSegment) expressionSegment).getParameters().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType, needRecursive));
        }
        if (expressionSegment instanceof MatchAgainstExpression) {
            extractSubquerySegmentsFromExpression(result, ((MatchAgainstExpression) expressionSegment).getExpr(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof CaseWhenExpression) {
            extractSubquerySegmentsFromCaseWhenExpression(result, (CaseWhenExpression) expressionSegment, subqueryType, needRecursive);
        }
        if (expressionSegment instanceof CollateExpression) {
            extractSubquerySegmentsFromExpression(result, ((CollateExpression) expressionSegment).getCollateName(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof DatetimeExpression) {
            extractSubquerySegmentsFromExpression(result, ((DatetimeExpression) expressionSegment).getLeft(), subqueryType, needRecursive);
            extractSubquerySegmentsFromExpression(result, ((DatetimeExpression) expressionSegment).getRight(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof NotExpression) {
            extractSubquerySegmentsFromExpression(result, ((NotExpression) expressionSegment).getExpression(), subqueryType, needRecursive);
        }
        if (expressionSegment instanceof TypeCastExpression) {
            extractSubquerySegmentsFromExpression(result, ((TypeCastExpression) expressionSegment).getExpression(), subqueryType, needRecursive);
        }
    }
    
    private static void extractSubquerySegmentsFromCaseWhenExpression(final List<SubquerySegment> result, final CaseWhenExpression expressionSegment, final SubqueryType subqueryType,
                                                                      final boolean needRecursive) {
        extractSubquerySegmentsFromExpression(result, expressionSegment.getCaseExpr(), subqueryType, needRecursive);
        expressionSegment.getWhenExprs().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType, needRecursive));
        expressionSegment.getThenExprs().forEach(each -> extractSubquerySegmentsFromExpression(result, each, subqueryType, needRecursive));
        extractSubquerySegmentsFromExpression(result, expressionSegment.getElseExpr(), subqueryType, needRecursive);
    }
    
    private static void extractSubquerySegmentsFromCombine(final List<SubquerySegment> result, final CombineSegment combineSegment, final boolean needRecursive,
                                                           final SubqueryType parentSubqueryType) {
        combineSegment.getLeft().getSelect().setSubqueryType(parentSubqueryType);
        combineSegment.getRight().getSelect().setSubqueryType(parentSubqueryType);
        result.add(combineSegment.getLeft());
        result.add(combineSegment.getRight());
        extractRecursive(needRecursive, result, combineSegment.getLeft().getSelect(), parentSubqueryType);
        extractRecursive(needRecursive, result, combineSegment.getRight().getSelect(), parentSubqueryType);
    }
}
