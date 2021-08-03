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

import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.OrPredicateSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.junit.Test;

import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class ExpressionBuilderTest {

    @Test
    public void assertExtractAndPredicates() {
        ColumnSegment left = new ColumnSegment(26, 33, new IdentifierValue("order_id"));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(35, 35, 0);
        ExpressionSegment expressionSegment = new BinaryOperationExpression(26, 35, left, right, "=", "order_id=?");
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expressionSegment);
        OrPredicateSegment result = expressionBuilder.extractAndPredicates();
        assertThat(result.getAndPredicates().size(), is(1));
        assertThat(result.getAndPredicates().iterator().next().getPredicates().iterator().next(), is(expressionSegment));
    }

    @Test
    public void assertExtractAndPredicatesAndCondition() {
        ColumnSegment columnSegment1 = new ColumnSegment(28, 35, new IdentifierValue("order_id"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(39, 39, 0);
        ExpressionSegment leftExpressionSegment = new BinaryOperationExpression(28, 39, columnSegment1, parameterMarkerExpressionSegment1, "=", "order_id=?");
        ColumnSegment columnSegment2 = new ColumnSegment(45, 50, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment2 = new ParameterMarkerExpressionSegment(54, 54, 1);
        ExpressionSegment rightExpressionSegment = new BinaryOperationExpression(28, 39, columnSegment2, parameterMarkerExpressionSegment2, "=", "status=?");
        BinaryOperationExpression expression = new BinaryOperationExpression(28, 54, leftExpressionSegment, rightExpressionSegment, "AND", "order_id=? AND status=?");
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expression);
        OrPredicateSegment result = expressionBuilder.extractAndPredicates();
        assertThat(result.getAndPredicates().size(), is(1));
        AndPredicate andPredicate = result.getAndPredicates().iterator().next();
        assertThat(andPredicate.getPredicates().size(), is(2));
        Iterator<ExpressionSegment> iterator = andPredicate.getPredicates().iterator();
        assertThat(iterator.next(), is(leftExpressionSegment));
        assertThat(iterator.next(), is(rightExpressionSegment));
    }

    @Test
    public void assertExtractAndPredicatesOrCondition() {
        ColumnSegment columnSegment1 = new ColumnSegment(28, 33, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(35, 35, 0);
        ExpressionSegment expressionSegment1 = new BinaryOperationExpression(28, 39, columnSegment1, parameterMarkerExpressionSegment1, "=", "status=?");
        ColumnSegment columnSegment2 = new ColumnSegment(40, 45, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment2 = new ParameterMarkerExpressionSegment(47, 47, 1);
        ExpressionSegment expressionSegment2 = new BinaryOperationExpression(40, 47, columnSegment2, parameterMarkerExpressionSegment2, "=", "status=?");
        BinaryOperationExpression expression = new BinaryOperationExpression(28, 47, expressionSegment1, expressionSegment2, "OR", "status=? OR status=?");
        ExpressionBuilder expressionBuilder = new ExpressionBuilder(expression);
        OrPredicateSegment result = expressionBuilder.extractAndPredicates();
        assertThat(result.getAndPredicates().size(), is(2));
        Iterator<AndPredicate> andPredicateIterator = result.getAndPredicates().iterator();
        AndPredicate andPredicate1 = andPredicateIterator.next();
        AndPredicate andPredicate2 = andPredicateIterator.next();
        assertThat(andPredicate1.getPredicates().iterator().next(), is(expressionSegment1));
        assertThat(andPredicate2.getPredicates().iterator().next(), is(expressionSegment2));

    }
}
