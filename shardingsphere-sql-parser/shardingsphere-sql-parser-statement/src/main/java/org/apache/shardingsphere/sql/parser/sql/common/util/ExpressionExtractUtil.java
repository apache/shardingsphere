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
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Collection<AndPredicate> result = new LinkedList<>();
        if (expression instanceof BinaryOperationExpression) {
            BinaryOperationExpression binaryExpression = (BinaryOperationExpression) expression;
            Optional<LogicalOperator> logicalOperator = LogicalOperator.valueFrom(binaryExpression.getOperator());
            if (logicalOperator.isPresent() && LogicalOperator.OR == logicalOperator.get()) {
                result.addAll(getAndPredicates(binaryExpression.getLeft()));
                result.addAll(getAndPredicates(binaryExpression.getRight()));
            } else if (logicalOperator.isPresent() && LogicalOperator.AND == logicalOperator.get()) {
                Collection<ExpressionSegment> expressions = new LinkedList<>();
                expressions.addAll(getAndPredicates(binaryExpression.getLeft()).stream().flatMap(each -> each.getPredicates().stream()).collect(Collectors.toList()));
                expressions.addAll(getAndPredicates(binaryExpression.getRight()).stream().flatMap(each -> each.getPredicates().stream()).collect(Collectors.toList()));
                result.add(createAndPredicate(expressions));
            } else {
                result.add(createAndPredicate(Collections.singletonList(expression)));
            }
        } else {
            result.add(createAndPredicate(Collections.singletonList(expression)));
        }
        return result;
    }
    
    private static AndPredicate createAndPredicate(final Collection<ExpressionSegment> expressions) {
        AndPredicate result = new AndPredicate();
        result.getPredicates().addAll(expressions);
        return result;
    }
}
