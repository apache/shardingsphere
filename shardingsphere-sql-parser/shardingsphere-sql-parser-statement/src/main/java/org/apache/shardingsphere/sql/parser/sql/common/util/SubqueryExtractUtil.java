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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;

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
        Collection<SubquerySegment> result = new LinkedList<>();
        result.addAll(getSubquerySegmentsFromProjections(selectStatement.getProjections()));
        result.addAll(getSubquerySegmentsFromTableSegment(selectStatement.getFrom()));
        if (selectStatement.getWhere().isPresent()) {
            result.addAll(getSubquerySegmentsFromExpression(selectStatement.getWhere().get().getExpr()));
        }
        return result;
    }

    private static Collection<SubquerySegment> getSubquerySegmentsFromProjections(final ProjectionsSegment projections) {
        if (null == projections || projections.getProjections().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<SubquerySegment> result = new LinkedList<>();
        for (ProjectionSegment each : projections.getProjections()) {
            if (!(each instanceof SubqueryProjectionSegment)) {
                continue;
            }
            result.add(((SubqueryProjectionSegment) each).getSubquery());
        }
        return result;
    }
    
    private static Collection<SubquerySegment> getSubquerySegmentsFromTableSegment(final TableSegment tableSegment) {
        if (null == tableSegment) {
            return Collections.emptyList();
        }
        Collection<SubquerySegment> result = new LinkedList<>();
        if (tableSegment instanceof SubqueryTableSegment) {
            result.add(((SubqueryTableSegment) tableSegment).getSubquery());
        }
        if (tableSegment instanceof JoinTableSegment) {
            result.addAll(getSubquerySegmentsFromTableSegment(((JoinTableSegment) tableSegment).getLeft()));
            result.addAll(getSubquerySegmentsFromTableSegment(((JoinTableSegment) tableSegment).getRight()));
        }
        return result;
    }
    
    private static Collection<SubquerySegment> getSubquerySegmentsFromExpression(final ExpressionSegment expressionSegment) {
        Collection<SubquerySegment> result = new LinkedList<>();
        if (expressionSegment instanceof SubqueryExpressionSegment) {
            result.add(((SubqueryExpressionSegment) expressionSegment).getSubquery());
        }
        if (expressionSegment instanceof ListExpression) {
            for (ExpressionSegment each : ((ListExpression) expressionSegment).getItems()) {
                result.addAll(getSubquerySegmentsFromExpression(each));
            }
        }
        if (expressionSegment instanceof BinaryOperationExpression) {
            result.addAll(getSubquerySegmentsFromExpression(((BinaryOperationExpression) expressionSegment).getLeft()));
            result.addAll(getSubquerySegmentsFromExpression(((BinaryOperationExpression) expressionSegment).getRight()));
        }
        return result;
    }
}
