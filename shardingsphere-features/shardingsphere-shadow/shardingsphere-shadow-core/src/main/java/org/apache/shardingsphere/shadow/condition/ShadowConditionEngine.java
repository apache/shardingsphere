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
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.util.ExpressionBuilder;
import org.apache.shardingsphere.sql.parser.sql.common.util.ColumnExtractor;

import java.util.Collection;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Shadow condition engine.
 */
@RequiredArgsConstructor
public final class ShadowConditionEngine {
    
    private final ShadowRule shadowRule;
    
    /**
     * Create shadow condition.
     *
     * @param sqlStatementContext SQL statement context
     * @return shadow condition
     */
    public Optional<ShadowCondition> createShadowCondition(final SQLStatementContext sqlStatementContext) {
        if (sqlStatementContext instanceof WhereAvailable) {
            WhereAvailable whereAvailable = (WhereAvailable) sqlStatementContext;
            return createShadowConditionInWhereAvailable(whereAvailable);
        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowConditionInWhereAvailable(final WhereAvailable whereAvailable) {
        return whereAvailable.getWhere().flatMap(this::createShadowConditionWhereSegment);
    }
    
    private Optional<ShadowCondition> createShadowConditionWhereSegment(final WhereSegment whereSegment) {
        return createShadowConditionAndPredicates(createAndPredicates(whereSegment));
    }
    
    private Collection<AndPredicate> createAndPredicates(final WhereSegment whereSegment) {
        return new ExpressionBuilder(whereSegment.getExpr()).extractAndPredicates().getAndPredicates();
    }
    
    private Optional<ShadowCondition> createShadowConditionAndPredicates(final Collection<AndPredicate> andPredicates) {
        for (AndPredicate each : new LinkedList<>(andPredicates)) {
            Optional<ShadowCondition> condition = createShadowConditionAndPredicate(each);
            if (condition.isPresent()) {
                return condition;
            }
        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowConditionAndPredicate(final AndPredicate andPredicate) {
        for (ExpressionSegment predicate : andPredicate.getPredicates()) {
            Optional<ColumnSegment> column = ColumnExtractor.extract(predicate);
            if (column.isPresent()) {
                Optional<ShadowCondition> shadowCondition = createShadowConditionColumnSegment(column.get(), predicate);
                if (shadowCondition.isPresent()) {
                    return shadowCondition;
                }
            }
        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowConditionColumnSegment(final ColumnSegment columnSegment, final ExpressionSegment expression) {
        return compareColumnName(columnSegment) ? createShadowConditionExpressionSegment(expression) : Optional.empty();
    }
    
    private boolean compareColumnName(final ColumnSegment columnSegment) {
        return shadowRule.getColumn().equals(columnSegment.getIdentifier().getValue());
    }
    
    private Optional<ShadowCondition> createShadowConditionExpressionSegment(final ExpressionSegment expression) {
        if (expression instanceof InExpression) {
            throw new ShardingSphereException("The SQL clause 'IN...' is unsupported in shadow rule.");
        }
        if (expression instanceof BetweenExpression) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in shadow rule.");
        }
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryOperationExpression = (BinaryOperationExpression) expression;
            return isSupportedOperator(binaryOperationExpression.getOperator()) ? createShadowConditionBinaryOperation((BinaryOperationExpression) expression)
                    : Optional.empty();
        }
        return Optional.empty();
    }
    
    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator);
    }
    
    private Optional<ShadowCondition> createShadowConditionBinaryOperation(final BinaryOperationExpression binaryOperationExpression) {
        ExpressionSegment left = binaryOperationExpression.getLeft();
        ExpressionSegment right = binaryOperationExpression.getRight();
        return left instanceof ColumnSegment && right instanceof SimpleExpressionSegment
                ? Optional.of(new ShadowCondition(((ColumnSegment) left).getIdentifier().getValue(), right.getStartIndex(), binaryOperationExpression.getStopIndex(), right))
                : Optional.empty();
    }
}
