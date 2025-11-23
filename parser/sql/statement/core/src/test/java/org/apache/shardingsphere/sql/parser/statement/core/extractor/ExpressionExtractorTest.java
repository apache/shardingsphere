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

package org.apache.shardingsphere.sql.parser.statement.core.extractor;

import org.apache.shardingsphere.sql.parser.statement.core.enums.ParameterMarkerType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ListExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.TypeCastExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.ParameterMarkerExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.AndPredicate;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ExpressionExtractorTest {
    
    @Test
    void assertExtractAndPredicates() {
        ColumnSegment left = new ColumnSegment(26, 33, new IdentifierValue("order_id"));
        ParameterMarkerExpressionSegment right = new ParameterMarkerExpressionSegment(35, 35, 0);
        ExpressionSegment expressionSegment = new BinaryOperationExpression(26, 35, left, right, "=", "order_id=?");
        Collection<AndPredicate> actual = ExpressionExtractor.extractAndPredicates(expressionSegment);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next().getPredicates().iterator().next(), is(expressionSegment));
    }
    
    @Test
    void assertExtractAndPredicatesAndCondition() {
        ColumnSegment columnSegment1 = new ColumnSegment(28, 35, new IdentifierValue("order_id"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(39, 39, 0);
        ExpressionSegment leftExpressionSegment = new BinaryOperationExpression(28, 39, columnSegment1, parameterMarkerExpressionSegment1, "=", "order_id=?");
        ColumnSegment columnSegment2 = new ColumnSegment(45, 50, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment2 = new ParameterMarkerExpressionSegment(54, 54, 1);
        ExpressionSegment rightExpressionSegment = new BinaryOperationExpression(28, 39, columnSegment2, parameterMarkerExpressionSegment2, "=", "status=?");
        BinaryOperationExpression expression = new BinaryOperationExpression(28, 54, leftExpressionSegment, rightExpressionSegment, "AND", "order_id=? AND status=?");
        Collection<AndPredicate> actual = ExpressionExtractor.extractAndPredicates(expression);
        assertThat(actual.size(), is(1));
        AndPredicate andPredicate = actual.iterator().next();
        assertThat(andPredicate.getPredicates().size(), is(2));
        Iterator<ExpressionSegment> iterator = andPredicate.getPredicates().iterator();
        assertThat(iterator.next(), is(leftExpressionSegment));
        assertThat(iterator.next(), is(rightExpressionSegment));
    }
    
    @Test
    void assertExtractAndPredicatesOrCondition() {
        ColumnSegment columnSegment1 = new ColumnSegment(28, 33, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment1 = new ParameterMarkerExpressionSegment(35, 35, 0);
        ExpressionSegment expressionSegment1 = new BinaryOperationExpression(28, 39, columnSegment1, parameterMarkerExpressionSegment1, "=", "status=?");
        ColumnSegment columnSegment2 = new ColumnSegment(40, 45, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment parameterMarkerExpressionSegment2 = new ParameterMarkerExpressionSegment(47, 47, 1);
        ExpressionSegment expressionSegment2 = new BinaryOperationExpression(40, 47, columnSegment2, parameterMarkerExpressionSegment2, "=", "status=?");
        BinaryOperationExpression expression = new BinaryOperationExpression(28, 47, expressionSegment1, expressionSegment2, "OR", "status=? OR status=?");
        Collection<AndPredicate> actual = ExpressionExtractor.extractAndPredicates(expression);
        assertThat(actual.size(), is(2));
        Iterator<AndPredicate> andPredicateIterator = actual.iterator();
        AndPredicate andPredicate1 = andPredicateIterator.next();
        AndPredicate andPredicate2 = andPredicateIterator.next();
        assertThat(andPredicate1.getPredicates().iterator().next(), is(expressionSegment1));
        assertThat(andPredicate2.getPredicates().iterator().next(), is(expressionSegment2));
    }
    
    @Test
    void assertExtractAndPredicatesOrAndCondition() {
        ColumnSegment statusColumn = new ColumnSegment(0, 0, new IdentifierValue("status"));
        ParameterMarkerExpressionSegment statusParameterExpression = new ParameterMarkerExpressionSegment(0, 0, 0);
        Collection<AndPredicate> actual = ExpressionExtractor.extractAndPredicates(createBinaryOperationExpression(statusColumn, statusParameterExpression));
        assertThat(actual.size(), is(2));
        Iterator<AndPredicate> iterator = actual.iterator();
        AndPredicate andPredicate1 = iterator.next();
        AndPredicate andPredicate2 = iterator.next();
        assertThat(andPredicate1.getPredicates().size(), is(1));
        assertThat(andPredicate2.getPredicates().size(), is(2));
    }
    
    private BinaryOperationExpression createBinaryOperationExpression(final ColumnSegment statusColumn, final ParameterMarkerExpressionSegment statusParameterExpression) {
        ExpressionSegment leftExpression = new BinaryOperationExpression(0, 0, statusColumn, statusParameterExpression, "=", "status=?");
        ColumnSegment countColumn = new ColumnSegment(0, 0, new IdentifierValue("count"));
        BinaryOperationExpression rightExpression = createBinaryOperationExpression(statusColumn, statusParameterExpression, countColumn);
        return new BinaryOperationExpression(0, 0, leftExpression, rightExpression, "OR", "status=? OR status=? AND count=?");
    }
    
    private BinaryOperationExpression createBinaryOperationExpression(final ColumnSegment statusColumn,
                                                                      final ParameterMarkerExpressionSegment statusParameterExpression, final ColumnSegment countColumn) {
        ParameterMarkerExpressionSegment countParameterExpression = new ParameterMarkerExpressionSegment(0, 0, 1);
        ExpressionSegment subLeftExpression = new BinaryOperationExpression(0, 0, statusColumn, statusParameterExpression, "=", "status=?");
        ExpressionSegment subRightExpression = new BinaryOperationExpression(0, 0, countColumn, countParameterExpression, "=", "count=?");
        return new BinaryOperationExpression(0, 0, subLeftExpression, subRightExpression, "AND", "status=? AND count=?");
    }
    
    @Test
    void assertExtractAllExpressionsWithAndOperation() {
        BinaryOperationExpression expression1 =
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("order_id")), new LiteralExpressionSegment(10, 11, "1"), "=", "order_id=1");
        BinaryOperationExpression expression2 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("status")), new LiteralExpressionSegment(22, 23, "2"), "=", "status=2");
        BinaryOperationExpression andExpression = new BinaryOperationExpression(0, 0, expression1, expression2, "AND", "order_id=1 AND status=2");
        Collection<ExpressionSegment> actual = ExpressionExtractor.extractAllExpressions(andExpression);
        assertThat(actual.size(), is(2));
        Iterator<ExpressionSegment> iterator = actual.iterator();
        assertThat(iterator.next(), is(expression1));
        assertThat(iterator.next(), is(expression2));
    }
    
    @Test
    void assertExtractAllExpressionsWithOrOperation() {
        BinaryOperationExpression expression1 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("age")),
                new LiteralExpressionSegment(8, 9, "5"), ">", "age>5");
        BinaryOperationExpression expression2 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("score")),
                new LiteralExpressionSegment(19, 21, "10"), "<", "score<10");
        BinaryOperationExpression orExpression = new BinaryOperationExpression(0, 0, expression1, expression2, "OR", "age>5 OR score<10");
        Collection<ExpressionSegment> actual = ExpressionExtractor.extractAllExpressions(orExpression);
        assertThat(actual.size(), is(2));
        Iterator<ExpressionSegment> iterator = actual.iterator();
        assertThat(iterator.next(), is(expression1));
        assertThat(iterator.next(), is(expression2));
    }
    
    @Test
    void assertExtractAllExpressionsWithNestedOperations() {
        BinaryOperationExpression expression1 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("A")),
                new LiteralExpressionSegment(0, 0, "1"), "=", "A=1");
        BinaryOperationExpression expression2 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("B")),
                new LiteralExpressionSegment(0, 0, "2"), "=", "B=2");
        BinaryOperationExpression andExpression = new BinaryOperationExpression(0, 0, expression1, expression2, "AND", "A=1 AND B=2");
        BinaryOperationExpression expression3 = new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("C")),
                new LiteralExpressionSegment(0, 0, "3"), "=", "C=3");
        BinaryOperationExpression fullExpression = new BinaryOperationExpression(0, 0, andExpression, expression3, "OR", "A=1 AND B=2 OR C=3");
        Collection<ExpressionSegment> actual = ExpressionExtractor.extractAllExpressions(fullExpression);
        assertThat(actual.size(), is(3));
        Iterator<ExpressionSegment> iterator = actual.iterator();
        assertThat(iterator.next(), is(expression1));
        assertThat(iterator.next(), is(expression2));
        assertThat(iterator.next(), is(expression3));
    }
    
    @Test
    void assertExtractAllExpressionsWithComplexNestedExpressions() {
        BinaryOperationExpression expression1 = new BinaryOperationExpression(0, 0, new ColumnSegment(21, 22, new IdentifierValue("A")),
                new LiteralExpressionSegment(0, 0, "1"), "=", "A=1");
        BinaryOperationExpression expression2 = new BinaryOperationExpression(0, 0, new ColumnSegment(14, 15, new IdentifierValue("B")),
                new LiteralExpressionSegment(0, 0, "2"), "=", "B=2");
        BinaryOperationExpression expression3 = new BinaryOperationExpression(0, 0, new ColumnSegment(7, 8, new IdentifierValue("C")),
                new LiteralExpressionSegment(0, 0, "3"), "=", "C=3");
        BinaryOperationExpression expression4 = new BinaryOperationExpression(0, 0,
                new ColumnSegment(0, 0, new IdentifierValue("D")),
                new LiteralExpressionSegment(0, 0, "4"), "=", "D=4");
        BinaryOperationExpression orExpression = new BinaryOperationExpression(0, 0, expression2, expression3, "OR", "B=2 OR C=3");
        BinaryOperationExpression innerAndExpression = new BinaryOperationExpression(0, 0, expression1, orExpression, "AND", "A=1 AND (B=2 OR C=3)");
        BinaryOperationExpression topLevelExpression = new BinaryOperationExpression(0, 0, innerAndExpression, expression4, "AND", "(A=1 AND (B=2 OR C=3)) AND D=4");
        Collection<ExpressionSegment> actual = ExpressionExtractor.extractAllExpressions(topLevelExpression);
        assertThat(actual.size(), is(4));
        Iterator<ExpressionSegment> iterator = actual.iterator();
        assertThat(iterator.next(), is(expression1));
        assertThat(iterator.next(), is(expression2));
        assertThat(iterator.next(), is(expression3));
        assertThat(iterator.next(), is(expression4));
    }
    
    @Test
    void assertExtractGetParameterMarkerExpressions() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "IF", "IF(number + 1 <= ?, 1, -1)");
        BinaryOperationExpression param1 = new BinaryOperationExpression(0, 0,
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("number")), new LiteralExpressionSegment(0, 0, 1), "+", "number + 1"),
                new ParameterMarkerExpressionSegment(0, 0, 2), "<=", "number + 1 <= ?");
        CommonExpressionSegment param2 = new CommonExpressionSegment(0, 0, "1");
        CommonExpressionSegment param3 = new CommonExpressionSegment(0, 0, "-1");
        functionSegment.getParameters().add(param1);
        functionSegment.getParameters().add(param2);
        functionSegment.getParameters().add(param3);
        assertThat(ExpressionExtractor.getParameterMarkerExpressions(Collections.singleton(functionSegment)).size(), is(1));
    }
    
    @Test
    void assertGetParameterMarkerExpressionsFromTypeCastExpression() {
        ParameterMarkerExpressionSegment expected = new ParameterMarkerExpressionSegment(0, 0, 1, ParameterMarkerType.DOLLAR);
        Collection<ExpressionSegment> input = Collections.singleton(new TypeCastExpression(0, 0, "$2::varchar", expected, "varchar"));
        List<ParameterMarkerExpressionSegment> actual = ExpressionExtractor.getParameterMarkerExpressions(input);
        assertThat(actual.size(), is(1));
        assertThat(actual.get(0), is(expected));
    }
    
    @Test
    void assertGetParameterMarkerExpressionsFromInExpression() {
        ListExpression listExpression = new ListExpression(0, 0);
        listExpression.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 1, ParameterMarkerType.QUESTION));
        listExpression.getItems().add(new ParameterMarkerExpressionSegment(0, 0, 2, ParameterMarkerType.QUESTION));
        Collection<ExpressionSegment> inExpressions = Collections.singleton(new InExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("order_id")), listExpression, false));
        List<ParameterMarkerExpressionSegment> actual = ExpressionExtractor.getParameterMarkerExpressions(inExpressions);
        assertThat(actual.size(), is(2));
    }
    
    @Test
    void assertExtractJoinConditions() {
        Collection<BinaryOperationExpression> actual = new LinkedList<>();
        BinaryOperationExpression binaryExpression =
                new BinaryOperationExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("order_id")), new ColumnSegment(0, 0, new IdentifierValue("order_id")), "=", "");
        ExpressionExtractor.extractJoinConditions(actual, Collections.singleton(new WhereSegment(0, 0, binaryExpression)));
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(binaryExpression));
    }
}
