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
import org.apache.shardingsphere.sql.parser.sql.common.constant.LogicalOperator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;

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
public final class ExpressionExtractUtil {
    
    /**
     * Get and predicate collection.
     * 
     * @param expression expression segment
     * @return and predicate collection
     */
    public static Collection<AndPredicate> getAndPredicates(final ExpressionSegment expression) {
        if (!(expression instanceof BinaryOperationExpression)) {
            return Collections.singletonList(createAndPredicate(expression));
        }
        BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
        Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(binaryExpression.getOperator());
        Collection<AndPredicate> result = new LinkedList<>();
        if (logicalOperator.isPresent() && LogicalOperator.OR == logicalOperator.get()) {
            result.addAll(getAndPredicates(binaryExpression.getLeft()));
            result.addAll(getAndPredicates(binaryExpression.getRight()));
        } else if (logicalOperator.isPresent() && LogicalOperator.AND == logicalOperator.get()) {
            Collection<AndPredicate> predicates = getAndPredicates(binaryExpression.getRight());
            for (AndPredicate each : getAndPredicates(binaryExpression.getLeft())) {
                result.addAll(getCombinedAndPredicates(each, predicates));
            }
        } else {
            result.add(createAndPredicate(expression));
        }
        return result;
    }
    
    private static Collection<AndPredicate> getCombinedAndPredicates(final AndPredicate current, final Collection<AndPredicate> predicates) {
        Collection<AndPredicate> result = new LinkedList<>();
        for (AndPredicate each : predicates) {
            AndPredicate predicate = new AndPredicate();
            predicate.getPredicates().addAll(current.getPredicates());
            predicate.getPredicates().addAll(each.getPredicates());
            result.add(predicate);
        }
        return result;
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
        for (ExpressionSegment each : expressions) {
            if (each instanceof ParameterMarkerExpressionSegment) {
                result.add((ParameterMarkerExpressionSegment) each);
            }
            // TODO support more expression type if necessary 
            if (each instanceof BinaryOperationExpression) {
                result.addAll(getParameterMarkerExpressions(Collections.singletonList(((BinaryOperationExpression) each).getLeft())));
                result.addAll(getParameterMarkerExpressions(Collections.singletonList(((BinaryOperationExpression) each).getRight())));
            }
        }
        return result;
    }
}
