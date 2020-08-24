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
import org.apache.shardingsphere.shadow.rule.ShadowRule;
import org.apache.shardingsphere.sql.parser.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.binder.type.WhereAvailable;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.expr.simple.SimpleExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.PredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateBetweenRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateCompareRightValue;
import org.apache.shardingsphere.sql.parser.sql.segment.dml.predicate.value.PredicateInRightValue;
import org.apache.shardingsphere.infra.exception.ShardingSphereException;

import java.util.Collection;
import java.util.HashSet;
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
        for (AndPredicate each : whereSegment.get().getAndPredicates()) {
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
        for (PredicateSegment predicate : andPredicate.getPredicates()) {
            Collection<Integer> stopIndexes = new HashSet<>();
            if (stopIndexes.add(predicate.getStopIndex())) {
                Optional<ShadowCondition> condition = shadowRule.getColumn().equals(predicate.getColumn().getIdentifier().getValue())
                        ? createShadowCondition(predicate) : Optional.empty();
                if (condition.isPresent()) {
                    return condition;
                }
            }
        }
        return Optional.empty();
    }
    
    private Optional<ShadowCondition> createShadowCondition(final PredicateSegment predicateSegment) {
        if (predicateSegment.getRightValue() instanceof PredicateCompareRightValue) {
            PredicateCompareRightValue compareRightValue = (PredicateCompareRightValue) predicateSegment.getRightValue();
            return isSupportedOperator(compareRightValue.getOperator()) ? createCompareShadowCondition(predicateSegment, compareRightValue) : Optional.empty();
        }
        if (predicateSegment.getRightValue() instanceof PredicateInRightValue) {
            throw new ShardingSphereException("The SQL clause 'IN...' is unsupported in shadow rule.");
        }
        if (predicateSegment.getRightValue() instanceof PredicateBetweenRightValue) {
            throw new ShardingSphereException("The SQL clause 'BETWEEN...AND...' is unsupported in shadow rule.");
        }
        return Optional.empty();
    }
    
    private static Optional<ShadowCondition> createCompareShadowCondition(final PredicateSegment predicateSegment, final PredicateCompareRightValue compareRightValue) {
        return compareRightValue.getExpression() instanceof SimpleExpressionSegment
                ? Optional.of(new ShadowCondition(predicateSegment.getColumn().getIdentifier().getValue(), compareRightValue.getExpression().getStartIndex(),
                predicateSegment.getStopIndex(), compareRightValue.getExpression()))
                : Optional.empty();
    }
    
    private boolean isSupportedOperator(final String operator) {
        return "=".equals(operator);
    }
}
