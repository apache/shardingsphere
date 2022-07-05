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
import org.apache.shardingsphere.sql.parser.sql.common.constant.SubqueryType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 * Subquery extract utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryExtractUtil {
    
    /**
     * Get subquery segment from SelectStatement.
     *
     * @param selectStatement SelectStatement
     * @return subquery segment collection
     */
    public static Collection<SubquerySegment> getSubquerySegments(final SelectStatement selectStatement) {
        List<SubquerySegment> result = new LinkedList<>();
        extractSubquerySegments(result, selectStatement);
        return result;
    }
    
    private static void extractSubquerySegments(final List<SubquerySegment> result, final SelectStatement selectStatement) {
        extractSubquerySegmentsFromProjections(result, selectStatement.getProjections());
        extractSubquerySegmentsFromTableSegment(result, selectStatement.getFrom());
        if (selectStatement.getWhere().isPresent()) {
            extractSubquerySegmentsFromExpression(result, selectStatement.getWhere().get().getExpr());
        }
        extractSubquerySegmentsFromCombines(result, selectStatement.getCombines());
    }
    
    private static void extractSubquerySegmentsFromCombines(final List<SubquerySegment> result, final Collection<CombineSegment> combineSegments) {
        for (CombineSegment each : combineSegments) {
            extractSubquerySegments(result, each.getSelectStatement());
        }
    }
    
    private static void extractSubquerySegmentsFromProjections(final List<SubquerySegment> result, final ProjectionsSegment projections) {
        if (null == projections || projections.getProjections().isEmpty()) {
            return;
        }
        for (ProjectionSegment each : projections.getProjections()) {
            if (!(each instanceof SubqueryProjectionSegment)) {
                continue;
            }
            SubquerySegment subquery = ((SubqueryProjectionSegment) each).getSubquery();
            subquery.setSubqueryType(SubqueryType.PROJECTION_SUBQUERY);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
    }
    
    private static void extractSubquerySegmentsFromTableSegment(final List<SubquerySegment> result, final TableSegment tableSegment) {
        if (null == tableSegment) {
            return;
        }
        if (tableSegment instanceof SubqueryTableSegment) {
            SubquerySegment subquery = ((SubqueryTableSegment) tableSegment).getSubquery();
            subquery.setSubqueryType(SubqueryType.TABLE_SUBQUERY);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
        if (tableSegment instanceof JoinTableSegment) {
            extractSubquerySegmentsFromTableSegment(result, ((JoinTableSegment) tableSegment).getLeft());
            extractSubquerySegmentsFromTableSegment(result, ((JoinTableSegment) tableSegment).getRight());
        }
    }
    
    private static void extractSubquerySegmentsFromExpression(final List<SubquerySegment> result, final ExpressionSegment expressionSegment) {
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            SubquerySegment subquery = ((SubqueryExpressionSegment) expressionSegment).getSubquery();
            subquery.setSubqueryType(SubqueryType.PREDICATE_SUBQUERY);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
        if (expressionSegment instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expressionSegment).getItems()) {
                extractSubquerySegmentsFromExpression(result, each);
            }
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getLeft());
            extractSubquerySegmentsFromExpression(result, ((BinaryOperationExpression) expressionSegment).getRight());
        }
        if (expressionSegment instanceof InExpression) {
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getLeft());
            extractSubquerySegmentsFromExpression(result, ((InExpression) expressionSegment).getRight());
        }
        if (expressionSegment instanceof BetweenExpression) {
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getBetweenExpr());
            extractSubquerySegmentsFromExpression(result, ((BetweenExpression) expressionSegment).getAndExpr());
        }
        if (expressionSegment instanceof ExistsSubqueryExpression) {
            SubquerySegment subquery = ((ExistsSubqueryExpression) expressionSegment).getSubquery();
            subquery.setSubqueryType(SubqueryType.EXISTS_SUBQUERY);
            result.add(subquery);
            extractSubquerySegments(result, subquery.getSelect());
        }
    }
}
