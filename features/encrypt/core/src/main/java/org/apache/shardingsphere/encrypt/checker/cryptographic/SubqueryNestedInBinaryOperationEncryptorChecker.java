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

package org.apache.shardingsphere.encrypt.checker.cryptographic;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.encrypt.rewrite.token.comparator.EncryptorComparator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.infra.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.infra.exception.generic.UnsupportedSQLOperationException;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.bound.ColumnSegmentBoundInfo;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Subquery nested in binary operation encryptor checker.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class SubqueryNestedInBinaryOperationEncryptorChecker {
    
    /**
     * Check whether binary operations with subquery use same encryptor.
     *
     * @param left left expression
     * @param right right expression
     * @param encryptRule encrypt rule
     * @param scenario scenario
     */
    public static void checkIsSame(final ExpressionSegment left, final ExpressionSegment right, final EncryptRule encryptRule, final String scenario) {
        if (isNotColumnAndSubquery(left) || isNotColumnAndSubquery(right)) {
            return;
        }
        if (left instanceof RowExpression) {
            checkRowExpressionEncryptor((RowExpression) left, right, encryptRule, scenario);
            return;
        }
        if (right instanceof RowExpression) {
            checkRowExpressionEncryptor((RowExpression) right, left, encryptRule, scenario);
            return;
        }
        ColumnSegmentBoundInfo leftColumnInfo = left instanceof ColumnSegment ? ((ColumnSegment) left).getColumnBoundInfo() : getSubqueryColumnBoundInfo(left);
        ColumnSegmentBoundInfo rightColumnInfo = right instanceof ColumnSegment ? ((ColumnSegment) right).getColumnBoundInfo() : getSubqueryColumnBoundInfo(right);
        checkEncryptorIsSame(leftColumnInfo, rightColumnInfo, encryptRule, scenario);
    }
    
    private static void checkRowExpressionEncryptor(final RowExpression rowExpression, final ExpressionSegment otherExpression, final EncryptRule encryptRule, final String scenario) {
        List<ColumnSegmentBoundInfo> rowColumnInfos = getRowExpressionColumnBoundInfos(rowExpression);
        List<ColumnSegmentBoundInfo> otherColumnInfos = getExpressionColumnBoundInfos(otherExpression, rowColumnInfos.size());
        for (int i = 0; i < rowColumnInfos.size(); i++) {
            checkEncryptorIsSame(rowColumnInfos.get(i), otherColumnInfos.get(i), encryptRule, scenario);
        }
    }
    
    private static List<ColumnSegmentBoundInfo> getRowExpressionColumnBoundInfos(final RowExpression rowExpression) {
        List<ColumnSegmentBoundInfo> result = new ArrayList<>();
        for (ExpressionSegment each : rowExpression.getItems()) {
            if (each instanceof ColumnSegment) {
                result.add(((ColumnSegment) each).getColumnBoundInfo());
            }
        }
        return result;
    }
    
    private static List<ColumnSegmentBoundInfo> getExpressionColumnBoundInfos(final ExpressionSegment expression, final int expectedSize) {
        if (expression instanceof SubqueryExpressionSegment || expression instanceof SubquerySegment) {
            return getSubqueryColumnBoundInfos(expression, expectedSize);
        }
        throw new UnsupportedSQLOperationException("Row expression can only compare with subquery");
    }
    
    private static List<ColumnSegmentBoundInfo> getSubqueryColumnBoundInfos(final ExpressionSegment expression, final int expectedSize) {
        ShardingSpherePreconditions.checkState(expression instanceof SubqueryExpressionSegment || expression instanceof SubquerySegment,
                () -> new UnsupportedSQLOperationException(String.format("only support subquery expression or subquery segment, but got %s", expression.getClass().getName())));
        SubquerySegment subquerySegment = expression instanceof SubquerySegment ? (SubquerySegment) expression : ((SubqueryExpressionSegment) expression).getSubquery();
        Collection<ProjectionSegment> projections = subquerySegment.getSelect().getProjections().getProjections();
        ShardingSpherePreconditions.checkState(projections.size() == expectedSize,
                () -> new UnsupportedSQLOperationException(String.format("Subquery column count %d does not match row expression column count %d", projections.size(), expectedSize)));
        List<ColumnSegmentBoundInfo> result = new ArrayList<>();
        for (ProjectionSegment each : projections) {
            result.add(each instanceof ColumnProjectionSegment
                    ? ((ColumnProjectionSegment) each).getColumn().getColumnBoundInfo()
                    : new ColumnSegmentBoundInfo(new IdentifierValue(each.getColumnLabel())));
        }
        return result;
    }
    
    private static ColumnSegmentBoundInfo getSubqueryColumnBoundInfo(final ExpressionSegment expression) {
        ShardingSpherePreconditions.checkState(expression instanceof SubqueryExpressionSegment || expression instanceof SubquerySegment,
                () -> new UnsupportedSQLOperationException(String.format("only support subquery expression or subquery segment, but got %s", expression.getClass().getName())));
        SubquerySegment subquerySegment = expression instanceof SubquerySegment ? (SubquerySegment) expression : ((SubqueryExpressionSegment) expression).getSubquery();
        ProjectionSegment projection = subquerySegment.getSelect().getProjections().getProjections().iterator().next();
        return projection instanceof ColumnProjectionSegment
                ? ((ColumnProjectionSegment) projection).getColumn().getColumnBoundInfo()
                : new ColumnSegmentBoundInfo(new IdentifierValue(projection.getColumnLabel()));
    }
    
    private static boolean isNotColumnAndSubquery(final ExpressionSegment expression) {
        return !(expression instanceof ColumnSegment) && !(expression instanceof RowExpression) && !(expression instanceof SubqueryExpressionSegment) && !(expression instanceof SubquerySegment);
    }
    
    private static void checkEncryptorIsSame(final ColumnSegmentBoundInfo leftColumnInfo, final ColumnSegmentBoundInfo rightColumnInfo, final EncryptRule encryptRule, final String scenario) {
        if (EncryptorComparator.isSame(encryptRule, leftColumnInfo, rightColumnInfo)) {
            return;
        }
        String reason = "Can not use different encryptor for " + leftColumnInfo + " and " + rightColumnInfo + " in " + scenario;
        throw new UnsupportedSQLOperationException(reason);
    }
}
