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

import org.apache.shardingsphere.sql.parser.statement.core.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.SubqueryType;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CollateExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.RowExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.UnaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.sameInstance;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubqueryExtractorTest {
    
    @Test
    void assertExtractSubquerySegmentsInWhere() {
        SelectStatement subquerySelectStatement = mockSelectStatement();
        when(subquerySelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(73, 99, new IdentifierValue("t_order")))));
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(59, 66);
        when(subquerySelectStatement.getProjections()).thenReturn(subqueryProjections);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(59, 66, new IdentifierValue("order_id"))));
        ColumnSegment subqueryWhereLeft = new ColumnSegment(87, 92, new IdentifierValue("status"));
        LiteralExpressionSegment subqueryWhereRight = new LiteralExpressionSegment(96, 99, "OK");
        WhereSegment subqueryWhereSegment = new WhereSegment(81, 99, new BinaryOperationExpression(87, 99, subqueryWhereLeft, subqueryWhereRight, "=", "status = 'OK'"));
        when(subquerySelectStatement.getWhere()).thenReturn(Optional.of(subqueryWhereSegment));
        SelectStatement selectStatement = mockSelectStatement();
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(21, 32, new IdentifierValue("t_order_item")))));
        when(selectStatement.getProjections()).thenReturn(new ProjectionsSegment(7, 14));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 14, new IdentifierValue("order_id"))));
        ColumnSegment left = new ColumnSegment(40, 47, new IdentifierValue("order_id"));
        SubqueryExpressionSegment right = new SubqueryExpressionSegment(new SubquerySegment(51, 100, subquerySelectStatement, ""));
        WhereSegment whereSegment = new WhereSegment(34, 100, new BinaryOperationExpression(40, 100, left, right, "=", "order_id = (SELECT order_id FROM t_order WHERE status = 'OK')"));
        when(selectStatement.getWhere()).thenReturn(Optional.of(whereSegment));
        Collection<SubquerySegment> actual = SubqueryExtractor.extractSubquerySegments(selectStatement, true);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(right.getSubquery()));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @Test
    void assertExtractSubquerySegmentsInProjection() {
        ColumnSegment left = new ColumnSegment(41, 48, new IdentifierValue("order_id"));
        ColumnSegment right = new ColumnSegment(52, 62, new IdentifierValue("order_id"));
        SelectStatement subquerySelectStatement = mockSelectStatement();
        when(subquerySelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(35, 62, new BinaryOperationExpression(41, 62, left, right, "=", "order_id = oi.order_id"))));
        SubquerySegment subquerySegment = new SubquerySegment(7, 63, subquerySelectStatement, "");
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "(SELECT status FROM t_order WHERE order_id = oi.order_id)");
        SelectStatement selectStatement = mockSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(7, 79);
        when(selectStatement.getProjections()).thenReturn(projections);
        projections.getProjections().add(subqueryProjectionSegment);
        Collection<SubquerySegment> actual = SubqueryExtractor.extractSubquerySegments(selectStatement, true);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(subquerySegment));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @Test
    void assertExtractSubquerySegmentsInFrom1() {
        SelectStatement subquery = mockSelectStatement();
        ColumnSegment left = new ColumnSegment(59, 66, new IdentifierValue("order_id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(70, 70, 1);
        when(subquery.getWhere()).thenReturn(Optional.of(new WhereSegment(53, 70, new BinaryOperationExpression(59, 70, left, right, "=", "order_id = 1"))));
        when(subquery.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(45, 51, new IdentifierValue("t_order")))));
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(31, 38);
        when(subquery.getProjections()).thenReturn(subqueryProjections);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(31, 38, new IdentifierValue("order_id"))));
        SelectStatement selectStatement = mockSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(7, 16);
        when(selectStatement.getProjections()).thenReturn(projections);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 16, new IdentifierValue("order_id"))));
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 0, new SubquerySegment(23, 71, subquery, ""));
        when(selectStatement.getFrom()).thenReturn(Optional.of(subqueryTableSegment));
        Collection<SubquerySegment> actual = SubqueryExtractor.extractSubquerySegments(selectStatement, true);
        assertThat(actual.size(), is(1));
        assertThat(actual.iterator().next(), is(subqueryTableSegment.getSubquery()));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @Test
    void assertExtractSubquerySegmentsInFrom2() {
        SelectStatement subqueryLeftSelectStatement = mockSelectStatement();
        when(subqueryLeftSelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(65, 71, new IdentifierValue("t_order")))));
        ColumnSegment leftColumnSegment = new ColumnSegment(79, 84, new IdentifierValue("status"));
        LiteralExpressionSegment leftLiteralExpressionSegment = new LiteralExpressionSegment(88, 91, "OK");
        BinaryOperationExpression leftStatusCondition = new BinaryOperationExpression(79, 91, leftColumnSegment, leftLiteralExpressionSegment, "=", "status = OK");
        when(subqueryLeftSelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(73, 91, leftStatusCondition)));
        ProjectionsSegment subqueryLeftProjections = new ProjectionsSegment(34, 58);
        when(subqueryLeftSelectStatement.getProjections()).thenReturn(subqueryLeftProjections);
        subqueryLeftProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(34, 41, new IdentifierValue("order_id"))));
        subqueryLeftProjections.getProjections().add(new AggregationProjectionSegment(44, 51, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement subqueryRightSelectStatement = mockSelectStatement();
        when(subqueryRightSelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item")))));
        ColumnSegment rightColumnSegment = new ColumnSegment(162, 167, new IdentifierValue("status"));
        LiteralExpressionSegment rightLiteralExpressionSegment = new LiteralExpressionSegment(171, 174, "OK");
        BinaryOperationExpression rightStatusCondition = new BinaryOperationExpression(162, 174, rightColumnSegment, rightLiteralExpressionSegment, "=", "status = OK");
        when(subqueryRightSelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(156, 174, rightStatusCondition)));
        ProjectionsSegment subqueryRightProjections = new ProjectionsSegment(112, 136);
        when(subqueryRightSelectStatement.getProjections()).thenReturn(subqueryRightProjections);
        subqueryRightProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(112, 119, new IdentifierValue("order_id"))));
        subqueryRightProjections.getProjections().add(new AggregationProjectionSegment(122, 129, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement selectStatement = mockSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(7, 19);
        when(selectStatement.getProjections()).thenReturn(projections);
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 11, new IdentifierValue("cnt"))));
        projections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(14, 19, new IdentifierValue("cnt"))));
        JoinTableSegment from = new JoinTableSegment();
        from.setStartIndex(26);
        from.setStopIndex(213);
        ColumnSegment columnSegment1 = new ColumnSegment(190, 199, new IdentifierValue("order_id"));
        ColumnSegment columnSegment2 = new ColumnSegment(203, 213, new IdentifierValue("order_id"));
        BinaryOperationExpression orderIdCondition = new BinaryOperationExpression(190, 213, columnSegment1, columnSegment2, "=", "o.order_id = oi.order_id");
        from.setCondition(orderIdCondition);
        SubqueryTableSegment leftSubquerySegment = new SubqueryTableSegment(0, 0, new SubquerySegment(26, 92, subqueryLeftSelectStatement, ""));
        SubqueryTableSegment rightSubquerySegment = new SubqueryTableSegment(0, 0, new SubquerySegment(104, 175, subqueryRightSelectStatement, ""));
        from.setLeft(leftSubquerySegment);
        from.setRight(rightSubquerySegment);
        when(selectStatement.getFrom()).thenReturn(Optional.of(from));
        Collection<SubquerySegment> actual = SubqueryExtractor.extractSubquerySegments(selectStatement, true);
        assertThat(actual.size(), is(2));
        Iterator<SubquerySegment> iterator = actual.iterator();
        assertThat(iterator.next(), is(leftSubquerySegment.getSubquery()));
        assertThat(iterator.next(), is(rightSubquerySegment.getSubquery()));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(2));
    }
    
    @Test
    void assertExtractSubquerySegmentsWithMultiNestedSubquery() {
        SelectStatement selectStatement = mockSelectStatement();
        SubquerySegment subquerySelect = createSubquerySegment();
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SubqueryTableSegment(0, 0, subquerySelect)));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(2));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @Test
    void assertExtractSubquerySegmentsWithCombineSegment() {
        SelectStatement selectStatement = mockSelectStatement();
        SubquerySegment left = new SubquerySegment(0, 0, mockSelectStatement(), "");
        SubquerySegment right = createSubquerySegment();
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 0, left, CombineType.UNION, right)));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(3));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(2));
    }
    
    private SubquerySegment createSubquerySegment() {
        SelectStatement selectStatement = mockSelectStatement();
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        SelectStatement nestedSelectStatement = mockSelectStatement();
        SubquerySegment nestedSubquery = new SubquerySegment(0, 0, nestedSelectStatement, "");
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 0, new InExpression(0, 0, left, new SubqueryExpressionSegment(nestedSubquery), false))));
        return new SubquerySegment(0, 0, selectStatement, "");
    }
    
    @Test
    void assertExtractSubquerySegmentsFromProjectionFunctionParams() {
        SelectStatement selectStatement = mockSelectStatement();
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        when(selectStatement.getProjections()).thenReturn(projections);
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "", "");
        functionSegment.getParameters().add(new SubqueryExpressionSegment(new SubquerySegment(0, 0, mockSelectStatement(), "")));
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "", functionSegment);
        projections.getProjections().add(expressionProjectionSegment);
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(1));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @ParameterizedTest(name = "{0}")
    @MethodSource("provideExpressionSubqueries")
    void assertExtractSubquerySegmentsFromExpression(final String name, final SelectStatement selectStatement, final boolean needRecursive,
                                                     final List<SubquerySegment> expected, final List<SubqueryType> expectedTypes) {
        Collection<SubquerySegment> actual = SubqueryExtractor.extractSubquerySegments(selectStatement, needRecursive);
        assertThat(actual.size(), is(expected.size()));
        Iterator<SubquerySegment> actualIterator = actual.iterator();
        for (int index = 0; index < expected.size(); index++) {
            SubquerySegment actualSubquery = actualIterator.next();
            assertThat(actualSubquery, sameInstance(expected.get(index)));
            assertThat(actualSubquery.getSelect().getSubqueryType().orElse(null), is(expectedTypes.get(index)));
        }
    }
    
    private static Stream<Arguments> provideExpressionSubqueries() {
        SubquerySegment first = new SubquerySegment(1, 9, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment second = new SubquerySegment(11, 19, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        CommonExpressionSegment inner = new CommonExpressionSegment(10, 20, "nested", Collections.singleton(new SubqueryExpressionSegment(second)));
        CommonExpressionSegment siblings = new CommonExpressionSegment(0, 21, "indexed", Arrays.asList(new SubqueryExpressionSegment(first), inner));
        SubquerySegment predicate = new SubquerySegment(1, 9, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SelectStatement predicateStatement = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .where(new WhereSegment(0, 10, new CommonExpressionSegment(0, 10, "indexed", Collections.singleton(new SubqueryExpressionSegment(predicate))))).build();
        SubquerySegment directNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment directOuter = createSubqueryWithNestedPredicate(directNested);
        SubquerySegment recursiveNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment recursiveOuter = createSubqueryWithNestedPredicate(recursiveNested);
        SubquerySegment firstRow = new SubquerySegment(1, 9, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment secondRow = new SubquerySegment(11, 19, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        RowExpression innerRow = new RowExpression(10, 20, "ROW");
        innerRow.getItems().add(new SubqueryExpressionSegment(secondRow));
        RowExpression row = new RowExpression(0, 21, "ROW");
        row.getItems().addAll(Arrays.asList(new SubqueryExpressionSegment(firstRow), innerRow));
        SubquerySegment unaryNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment unaryOuter = createSubqueryWithNestedPredicate(unaryNested);
        SelectStatement unaryPredicate = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .where(new WhereSegment(0, 10, new UnaryOperationExpression(0, 10, new SubqueryExpressionSegment(unaryOuter), "~", "~(SELECT)"))).build();
        SubquerySegment collateNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment collateOuter = createSubqueryWithNestedPredicate(collateNested);
        CollateExpression collate = new CollateExpression(0, 15, new LiteralExpressionSegment(12, 14, "C"), new SubqueryExpressionSegment(collateOuter));
        SubquerySegment directNotNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment directNotOuter = createSubqueryWithNestedPredicate(directNotNested);
        SelectStatement directNotPredicate = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .where(new WhereSegment(0, 10, new NotExpression(0, 10, new SubqueryExpressionSegment(directNotOuter), true))).build();
        SubquerySegment recursiveNotNested = new SubquerySegment(2, 8, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        SubquerySegment recursiveNotOuter = createSubqueryWithNestedPredicate(recursiveNotNested);
        SelectStatement recursiveNotPredicate = SelectStatement.builder().projections(new ProjectionsSegment(0, 0))
                .where(new WhereSegment(0, 10, new NotExpression(0, 10, new SubqueryExpressionSegment(recursiveNotOuter), true))).build();
        SubquerySegment nestedNotSubquery = new SubquerySegment(1, 9, SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).build(), "");
        NotExpression innerNot = new NotExpression(0, 10, new SubqueryExpressionSegment(nestedNotSubquery), true);
        SelectStatement nestedNotPredicate = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).where(new WhereSegment(0, 10, new NotExpression(0, 10, innerNot, true))).build();
        return Stream.of(
                Arguments.of("Empty common expression", createSelectWithExpressionProjection(new CommonExpressionSegment(0, 0, "legacy", Collections.emptyList())), false,
                        Collections.emptyList(), Collections.emptyList()),
                Arguments.of("Nested common projection order", createSelectWithExpressionProjection(siblings), false,
                        Arrays.asList(first, second), Arrays.asList(SubqueryType.PROJECTION, SubqueryType.PROJECTION)),
                Arguments.of("Common predicate type", predicateStatement, false, Collections.singletonList(predicate), Collections.singletonList(SubqueryType.PREDICATE)),
                Arguments.of("Common without SELECT recursion", createSelectWithExpressionProjection(
                        new CommonExpressionSegment(0, 10, "indexed", Collections.singleton(new SubqueryExpressionSegment(directOuter)))), false,
                        Collections.singletonList(directOuter), Collections.singletonList(SubqueryType.PROJECTION)),
                Arguments.of("Common with SELECT recursion", createSelectWithExpressionProjection(
                        new CommonExpressionSegment(0, 10, "indexed", Collections.singleton(new SubqueryExpressionSegment(recursiveOuter)))), true,
                        Arrays.asList(recursiveOuter, recursiveNested), Arrays.asList(SubqueryType.PROJECTION, SubqueryType.PREDICATE)),
                Arguments.of("Nested row projection order", createSelectWithExpressionProjection(row), false,
                        Arrays.asList(firstRow, secondRow), Arrays.asList(SubqueryType.PROJECTION, SubqueryType.PROJECTION)),
                Arguments.of("Unary predicate without SELECT recursion", unaryPredicate, false, Collections.singletonList(unaryOuter), Collections.singletonList(SubqueryType.PREDICATE)),
                Arguments.of("Collate with SELECT recursion", createSelectWithExpressionProjection(collate), true,
                        Arrays.asList(collateOuter, collateNested), Arrays.asList(SubqueryType.PROJECTION, SubqueryType.PREDICATE)),
                Arguments.of("Not predicate without SELECT recursion", directNotPredicate, false, Collections.singletonList(directNotOuter), Collections.singletonList(SubqueryType.PREDICATE)),
                Arguments.of("Not predicate with SELECT recursion", recursiveNotPredicate, true,
                        Arrays.asList(recursiveNotOuter, recursiveNotNested), Arrays.asList(SubqueryType.PREDICATE, SubqueryType.PREDICATE)),
                Arguments.of("Nested not predicate", nestedNotPredicate, false, Collections.singletonList(nestedNotSubquery), Collections.singletonList(SubqueryType.PREDICATE)));
    }
    
    private static SubquerySegment createSubqueryWithNestedPredicate(final SubquerySegment nested) {
        SelectStatement selectStatement = SelectStatement.builder().projections(new ProjectionsSegment(0, 0)).where(new WhereSegment(0, 10, new SubqueryExpressionSegment(nested))).build();
        return new SubquerySegment(0, 10, selectStatement, "");
    }
    
    private static SelectStatement createSelectWithExpressionProjection(final ExpressionSegment expression) {
        ProjectionsSegment projections = new ProjectionsSegment(expression.getStartIndex(), expression.getStopIndex());
        projections.getProjections().add(new ExpressionProjectionSegment(expression.getStartIndex(), expression.getStopIndex(), expression.getText(), expression));
        return SelectStatement.builder().projections(projections).build();
    }
    
    @Test
    void assertExtractSubquerySegmentsInDataModifyingCTE() {
        CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_cte")), null, mock(UpdateStatement.class));
        WithSegment withSegment = new WithSegment(0, 0, Collections.singletonList(commonTableExpression), false);
        SelectStatement selectStatement = mockSelectStatement();
        when(selectStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertTrue(SubqueryExtractor.extractSubquerySegments(selectStatement, true).isEmpty());
    }
    
    @Test
    void assertExtractSubquerySegmentsInCTEWithRecursion() {
        SubquerySegment nestedSubquery = new SubquerySegment(0, 0, mockSelectStatement(), "");
        SelectStatement commonTableSelect = mockSelectStatement();
        when(commonTableSelect.getFrom()).thenReturn(Optional.of(new SubqueryTableSegment(0, 0, nestedSubquery)));
        SubquerySegment commonTableSubquery = new SubquerySegment(0, 0, commonTableSelect, "");
        CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_cte")), commonTableSubquery, null);
        WithSegment withSegment = new WithSegment(0, 0, Collections.singletonList(commonTableExpression), false);
        SelectStatement selectStatement = mockSelectStatement();
        when(selectStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true), is(Arrays.asList(commonTableSubquery, nestedSubquery)));
    }
    
    @Test
    void assertExtractSubquerySegmentsInCTEWithoutRecursion() {
        SubquerySegment nestedSubquery = new SubquerySegment(0, 0, mockSelectStatement(), "");
        SelectStatement commonTableSelect = mockSelectStatement();
        when(commonTableSelect.getFrom()).thenReturn(Optional.of(new SubqueryTableSegment(0, 0, nestedSubquery)));
        SubquerySegment commonTableSubquery = new SubquerySegment(0, 0, commonTableSelect, "");
        CommonTableExpressionSegment commonTableExpression = new CommonTableExpressionSegment(0, 0, new AliasSegment(0, 0, new IdentifierValue("foo_cte")), commonTableSubquery, null);
        WithSegment withSegment = new WithSegment(0, 0, Collections.singletonList(commonTableExpression), false);
        SelectStatement selectStatement = mockSelectStatement();
        when(selectStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false), is(Collections.singletonList(commonTableSubquery)));
    }
    
    private SelectStatement mockSelectStatement() {
        SelectStatement result = mock(SelectStatement.class);
        when(result.withSubqueryType(any())).thenReturn(result);
        return result;
    }
}
