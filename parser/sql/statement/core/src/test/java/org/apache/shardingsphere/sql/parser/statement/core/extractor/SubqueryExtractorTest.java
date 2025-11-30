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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SubqueryExtractorTest {
    
    @Test
    void assertExtractSubquerySegmentsInWhere() {
        SelectStatement subquerySelectStatement = mock(SelectStatement.class);
        when(subquerySelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(73, 99, new IdentifierValue("t_order")))));
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(59, 66);
        when(subquerySelectStatement.getProjections()).thenReturn(subqueryProjections);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(59, 66, new IdentifierValue("order_id"))));
        ColumnSegment subqueryWhereLeft = new ColumnSegment(87, 92, new IdentifierValue("status"));
        LiteralExpressionSegment subqueryWhereRight = new LiteralExpressionSegment(96, 99, "OK");
        WhereSegment subqueryWhereSegment = new WhereSegment(81, 99, new BinaryOperationExpression(87, 99, subqueryWhereLeft, subqueryWhereRight, "=", "status = 'OK'"));
        when(subquerySelectStatement.getWhere()).thenReturn(Optional.of(subqueryWhereSegment));
        SelectStatement selectStatement = mock(SelectStatement.class);
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
        SelectStatement subquerySelectStatement = mock(SelectStatement.class);
        when(subquerySelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(35, 62, new BinaryOperationExpression(41, 62, left, right, "=", "order_id = oi.order_id"))));
        SubquerySegment subquerySegment = new SubquerySegment(7, 63, subquerySelectStatement, "");
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "(SELECT status FROM t_order WHERE order_id = oi.order_id)");
        SelectStatement selectStatement = mock(SelectStatement.class);
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
        SelectStatement subquery = mock(SelectStatement.class);
        ColumnSegment left = new ColumnSegment(59, 66, new IdentifierValue("order_id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(70, 70, 1);
        when(subquery.getWhere()).thenReturn(Optional.of(new WhereSegment(53, 70, new BinaryOperationExpression(59, 70, left, right, "=", "order_id = 1"))));
        when(subquery.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(45, 51, new IdentifierValue("t_order")))));
        ProjectionsSegment subqueryProjections = new ProjectionsSegment(31, 38);
        when(subquery.getProjections()).thenReturn(subqueryProjections);
        subqueryProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(31, 38, new IdentifierValue("order_id"))));
        SelectStatement selectStatement = mock(SelectStatement.class);
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
        SelectStatement subqueryLeftSelectStatement = mock(SelectStatement.class);
        when(subqueryLeftSelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(65, 71, new IdentifierValue("t_order")))));
        ColumnSegment leftColumnSegment = new ColumnSegment(79, 84, new IdentifierValue("status"));
        LiteralExpressionSegment leftLiteralExpressionSegment = new LiteralExpressionSegment(88, 91, "OK");
        BinaryOperationExpression leftStatusCondition = new BinaryOperationExpression(79, 91, leftColumnSegment, leftLiteralExpressionSegment, "=", "status = OK");
        when(subqueryLeftSelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(73, 91, leftStatusCondition)));
        ProjectionsSegment subqueryLeftProjections = new ProjectionsSegment(34, 58);
        when(subqueryLeftSelectStatement.getProjections()).thenReturn(subqueryLeftProjections);
        subqueryLeftProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(34, 41, new IdentifierValue("order_id"))));
        subqueryLeftProjections.getProjections().add(new AggregationProjectionSegment(44, 51, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement subqueryRightSelectStatement = mock(SelectStatement.class);
        when(subqueryRightSelectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item")))));
        ColumnSegment rightColumnSegment = new ColumnSegment(162, 167, new IdentifierValue("status"));
        LiteralExpressionSegment rightLiteralExpressionSegment = new LiteralExpressionSegment(171, 174, "OK");
        BinaryOperationExpression rightStatusCondition = new BinaryOperationExpression(162, 174, rightColumnSegment, rightLiteralExpressionSegment, "=", "status = OK");
        when(subqueryRightSelectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(156, 174, rightStatusCondition)));
        ProjectionsSegment subqueryRightProjections = new ProjectionsSegment(112, 136);
        when(subqueryRightSelectStatement.getProjections()).thenReturn(subqueryRightProjections);
        subqueryRightProjections.getProjections().add(new ColumnProjectionSegment(new ColumnSegment(112, 119, new IdentifierValue("order_id"))));
        subqueryRightProjections.getProjections().add(new AggregationProjectionSegment(122, 129, AggregationType.COUNT, "COUNT(*)"));
        SelectStatement selectStatement = mock(SelectStatement.class);
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
        SelectStatement selectStatement = mock(SelectStatement.class);
        SubquerySegment subquerySelect = createSubquerySegment();
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SubqueryTableSegment(0, 0, subquerySelect)));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(2));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
    
    @Test
    void assertExtractSubquerySegmentsWithCombineSegment() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        SubquerySegment left = new SubquerySegment(0, 0, mock(SelectStatement.class), "");
        SubquerySegment right = createSubquerySegment();
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 0, left, CombineType.UNION, right)));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(3));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(2));
    }
    
    private SubquerySegment createSubquerySegment() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        when(selectStatement.getWhere()).thenReturn(
                Optional.of(new WhereSegment(0, 0, new InExpression(0, 0, left, new SubqueryExpressionSegment(new SubquerySegment(0, 0, mock(SelectStatement.class), "")), false))));
        return new SubquerySegment(0, 0, selectStatement, "");
    }
    
    @Test
    void assertExtractSubquerySegmentsFromProjectionFunctionParams() {
        SelectStatement selectStatement = mock(SelectStatement.class);
        ProjectionsSegment projections = new ProjectionsSegment(0, 0);
        when(selectStatement.getProjections()).thenReturn(projections);
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "", "");
        functionSegment.getParameters().add(new SubqueryExpressionSegment(new SubquerySegment(0, 0, mock(SelectStatement.class), "")));
        ExpressionProjectionSegment expressionProjectionSegment = new ExpressionProjectionSegment(0, 0, "", functionSegment);
        projections.getProjections().add(expressionProjectionSegment);
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, true).size(), is(1));
        assertThat(SubqueryExtractor.extractSubquerySegments(selectStatement, false).size(), is(1));
    }
}
