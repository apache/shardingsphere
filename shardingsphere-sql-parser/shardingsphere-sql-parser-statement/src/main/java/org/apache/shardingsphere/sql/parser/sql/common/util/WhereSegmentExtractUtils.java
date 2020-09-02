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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.JoinedTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableFactorSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.TableReferenceSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.value.PredicateInRightValue;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

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
        result.addAll(getSubqueryWhereSegmentsFromTableReferences(selectStatement.getTableReferences()));
        result.addAll(getSubqueryWhereSegmentsFromWhere(selectStatement.getWhere().orElse(null)));
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
            subquerySelect.getWhere().ifPresent(result::add);
            result.addAll(getSubqueryWhereSegments(subquerySelect));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromTableReferences(final Collection<TableReferenceSegment> tableReferences) {
        if (tableReferences.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        for (TableReferenceSegment each : tableReferences) {
            result.addAll(getSubqueryWhereSegmentsFromTableFactor(each.getTableFactor()));
            result.addAll(getSubqueryWhereSegmentsFromJoinedTable(each.getJoinedTables()));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromWhere(final WhereSegment where) {
        if (null == where || where.getAndPredicates().isEmpty()) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        List<PredicateSegment> predicateSegments = where.getAndPredicates().stream().flatMap(andPredicate -> andPredicate.getPredicates().stream()).collect(Collectors.toList());
        for (PredicateSegment each : predicateSegments) {
            if (each.getRightValue() instanceof PredicateBetweenRightValue) {
                result.addAll(getSubqueryWhereSegmentsFromExpression(((PredicateBetweenRightValue) each.getRightValue()).getBetweenExpression()));
                result.addAll(getSubqueryWhereSegmentsFromExpression(((PredicateBetweenRightValue) each.getRightValue()).getAndExpression()));
            }
            if (each.getRightValue() instanceof PredicateCompareRightValue) {
                result.addAll(getSubqueryWhereSegmentsFromExpression(((PredicateCompareRightValue) each.getRightValue()).getExpression()));
            }
            if (each.getRightValue() instanceof PredicateInRightValue) {
                for (ExpressionSegment sqlExpression : ((PredicateInRightValue) each.getRightValue()).getSqlExpressions()) {
                    result.addAll(getSubqueryWhereSegmentsFromExpression(sqlExpression));
                }
            }
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromTableFactor(final TableFactorSegment tableFactor) {
        if (null == tableFactor) {
            return Collections.emptyList();
        }
        return getSubqueryWhereSegmentsFromTableSegment(tableFactor.getTable());
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromJoinedTable(final Collection<JoinedTableSegment> joinedTables) {
        if (joinedTables.isEmpty()) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        for (JoinedTableSegment joinedTable : joinedTables) {
            if (null == joinedTable.getTableFactor()) {
                continue;
            }
            result.addAll(getSubqueryWhereSegmentsFromTableSegment(joinedTable.getTableFactor().getTable()));
        }
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromTableSegment(final TableSegment tableSegment) {
        if (!(tableSegment instanceof SubqueryTableSegment)) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        SelectStatement subquerySelect = ((SubqueryTableSegment) tableSegment).getSubquery().getSelect();
        subquerySelect.getWhere().ifPresent(result::add);
        result.addAll(getSubqueryWhereSegments(subquerySelect));
        return result;
    }
    
    private static Collection<WhereSegment> getSubqueryWhereSegmentsFromExpression(final ExpressionSegment expressionSegment) {
        if (!(expressionSegment instanceof SubqueryExpressionSegment)) {
            return Collections.emptyList();
        }
        Collection<WhereSegment> result = new LinkedList<>();
        SelectStatement subquerySelect = ((SubqueryExpressionSegment) expressionSegment).getSubquery().getSelect();
        subquerySelect.getWhere().ifPresent(result::add);
        result.addAll(getSubqueryWhereSegments(subquerySelect));
        return result;
    }
}
