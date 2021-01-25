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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

/**
 * Where segment extract utility class.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class WhereSegmentExtractUtils {
    
    /**
     * Get subquery where segment from SelectStatement.
     *
     * @param selectStatement SelectStatement.
     * @return subquery where segment collection.
     */
    public static Collection<WhereSegment> getSubqueryWhereSegments(final SelectStatement selectStatement) {
        Collection<WhereSegment> result = new LinkedList<>();
        result.addAll(getSubqueryWhereSegmentsFromProjections(selectStatement.getProjections()));
        result.addAll(getSubqueryWhereSegmentsFromTableSegment(selectStatement.getFrom()));
        if (selectStatement.getWhere().isPresent()) {
            result.addAll(getSubqueryWhereSegmentsFromExpression(selectStatement.getWhere().get().getExpr()));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromProjections(final ProjectionsSegment projections) {
        if (null == projections || projections.getProjections().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections.getProjections()) {
            if (!(each instanceof SubqueryProjectionSegment)) {
                continue;
            }
            SelectStatement subquerySelect = ((SubqueryProjectionSegment) each).getSubquery().getSelect();
            result.addAll(getSubqueryWhereSegmentsFromSubquery(subquerySelect));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromSubquery(final SelectStatement subquerySelect) {
        Collection<WhereSegment> result = new LinkedList<>();
        subquerySelect.getWhere().ifPresent(result::add);
        result.addAll(getSubqueryWhereSegments(subquerySelect));
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromTableSegment(final TableSegment tableSegment) {
        if (null == tableSegment) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        if (tableSegment instanceof SubqueryTableSegment) {
            SelectStatement subquerySelect = ((SubqueryTableSegment) tableSegment).getSubquery().getSelect();
            result.addAll(getSubqueryWhereSegmentsFromSubquery(subquerySelect));
        }
        if (tableSegment instanceof JoinTableSegment) {
            result.addAll(getSubqueryWhereSegmentsFromTableSegment(((JoinTableSegment) tableSegment).getLeft()));
            result.addAll(getSubqueryWhereSegmentsFromTableSegment(((JoinTableSegment) tableSegment).getRight()));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromExpression(final ExpressionSegment expressionSegment) {
        Collection<WhereSegment> result = new LinkedList<>();
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            result.addAll(getSubqueryWhereSegmentsFromSubquery(((SubqueryExpressionSegment) expressionSegment).getSubquery().getSelect()));
        }
        if (expressionSegment instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expressionSegment).getItems()) {
                result.addAll(getSubqueryWhereSegmentsFromExpression(each));
            }
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            result.addAll(getSubqueryWhereSegmentsFromExpression(((BinaryOperationExpression) expressionSegment).getLeft()));
            result.addAll(getSubqueryWhereSegmentsFromExpression(((BinaryOperationExpression) expressionSegment).getRight()));
        }
        return result;
    }
}
