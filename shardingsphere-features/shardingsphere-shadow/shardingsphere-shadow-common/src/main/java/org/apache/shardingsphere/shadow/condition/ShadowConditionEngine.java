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

package org.apache.shardingsphere.shadow.condition;

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuildUtil;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractFromExpression;

import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Shadow condition engine.
 */
@RequiredArgsConstructor
public final class ShadowConditionEngine {
    
    private final ShadowRule shadowRule;
    
    /**
     * Create shadow conditions.
     *
     * @param sqlStatementContext SQL statement context
     * @return shadow condition
     */
    public Optional<ShadowCondition> createShadowCondition(final SQLStatementContext sqlStatementContext) {
        if (!(sqlStatementContext instanceof WhereAvailable)) {
            return Optional.empty();
        }
        Optional<WhereSegment> whereSegment = ((WhereAvailable) sqlStatementContext).getWhere();
        if (!whereSegment.isPresent()) {
            return Optional.empty();
        }
        Collection<AndPredicate> andPredicates = new LinkedList<>();
        ExpressionSegment expression = ((WhereAvailable) sqlStatementContext).getWhere().get().getExpr();
        ExpressionBuildUtil util = new ExpressionBuildUtil(expression);
        andPredicates.addAll(util.extractAndPredicates().getAndPredicates());
        for (AndPredicate each : andPredicates) {
            Optional<ShadowCondition> condition = createShadowCondition(each);
            if (condition.isPresent()) {
                return condition;
            }
        }
        // FIXME process subquery
//        for (SubqueryPredicateSegment each : sqlStatementContext.getSqlStatement().findSQLSegments(SubqueryPredicateSegment.class)) {
//            for (AndPredicate andPredicate : each.getAndPredicates()) {
//                Optional<ShadowCondition> condition = createShadowCondition(andPredicate);
//                if (condition.isPresent()) {
//                    return condition;
//                }
//            }
//        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowCondition(final AndPredicate andPredicate) {
        for (ExpressionSegment predicate : andPredicate.getPredicates()) {
            Optional<ColumnSegment> column = ColumnExtractFromExpression.extract(predicate);
            if (!column.isPresent()) {
                continue;
            }
            Collection<Integer> stopIndexes = new HashSet<>();
            if (stopIndexes.add(predicate.getStopIndex())) {
                Optional<ShadowCondition> condition = shadowRule.getColumn().equals(column.get().getIdentifier().getValue())
                        ? createShadowCondition(predicate) : Optional.empty();
                if (condition.isPresent()) {
                    return condition;
                }
            }
        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowCondition(final ExpressionSegment expression) {
        if (expression instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) expression).getOperator();
            boolean logical = "and".equalsIgnoreCase(operator) || "&&".equalsIgnoreCase(operator) || "OR".equalsIgnoreCase(operator) || "||".equalsIgnoreCase(operator);
            if (!logical) {
                return isSupportedOperator(operator) ? createCompareShadowCondition((BinaryOperationExpression) expression) : Optional.empty();
            }
        }
        if (expression instanceof InExpression) {
            throw new ShardingSphereException("The SQL clause 'IN...' is unsupported in shadow rule.");
        }
        if (expression instanceof BetweenExpression) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in shadow rule.");
        }
        return Optional.empty();
    }
    
    private static Optional<ShadowCondition> createCompareShadowCondition(final BinaryOperationExpression expression) {
        if (!(expression.getLeft() instanceof ColumnSegment)) {
            return Optional.empty();
        }
        return expression.getRight() instanceof SimpleExpressionSegment
                ? Optional.of(new ShadowCondition(((ColumnSegment) expression.getLeft()).getIdentifier().getValue(), expression.getRight().getStartIndex(),
                expression.getStopIndex(), expression.getRight()))
                : Optional.empty();
    }
    
    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator);
    }
}
