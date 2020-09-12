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

import lombok.RequiredArgsConstructor;
import org.apache.shardingsphere.sql.parser.sql.common.constant.LogicalOperator;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.OrPredicateSegment;

import java.util.Optional;

@RequiredArgsConstructor
public final class ExpressionBuildUtil {
    
    private final ExpressionSegment expression;
    
    /**
     * Extract andPredicates.
     *
     * @return OrPredicateSegment.
     */
    public OrPredicateSegment extractAndPredicates() {
        OrPredicateSegment orPredicate = new OrPredicateSegment();
        if (expression instanceof BinaryOperationExpression) {
            String operator = ((BinaryOperationExpression) expression).getOperator();
            Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(operator);
            if (logicalOperator.isPresent() && LogicalOperator.OR == logicalOperator.get()) {
                ExpressionBuildUtil leftUtil = new ExpressionBuildUtil(((BinaryOperationExpression) expression).getLeft());
                ExpressionBuildUtil rightUtil = new ExpressionBuildUtil(((BinaryOperationExpression) expression).getRight());
                orPredicate.getAndPredicates().addAll(leftUtil.extractAndPredicates().getAndPredicates());
                orPredicate.getAndPredicates().addAll(rightUtil.extractAndPredicates().getAndPredicates());
            } else if (logicalOperator.isPresent() && LogicalOperator.AND == logicalOperator.get()) {
                ExpressionBuildUtil leftUtil = new ExpressionBuildUtil(((BinaryOperationExpression) expression).getLeft());
                ExpressionBuildUtil rightUtil = new ExpressionBuildUtil(((BinaryOperationExpression) expression).getRight());
                for (AndPredicate eachLeft : leftUtil.extractAndPredicates().getAndPredicates()) {
                    for (AndPredicate eachRight : rightUtil.extractAndPredicates().getAndPredicates()) {
                        orPredicate.getAndPredicates().add(createAndPredicate(eachLeft, eachRight));
                    }
                }
            } else {
                AndPredicate andPredicate = new AndPredicate();
                andPredicate.getPredicates().add(expression);
                orPredicate.getAndPredicates().add(andPredicate);
            }
        } else {
            AndPredicate andPredicate = new AndPredicate();
            andPredicate.getPredicates().add(expression);
            orPredicate.getAndPredicates().add(andPredicate);
        }
        return orPredicate;
    }
    
    private AndPredicate createAndPredicate(final AndPredicate left, final AndPredicate right) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().addAll(left.getPredicates());
        result.getPredicates().addAll(right.getPredicates());
        return result;
    }
}
