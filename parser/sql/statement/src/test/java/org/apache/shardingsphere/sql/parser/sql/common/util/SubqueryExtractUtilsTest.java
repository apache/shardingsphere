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

import org.apache.shardingsphere.sql.parser.sql.common.enums.AggregationType;
import org.apache.shardingsphere.sql.parser.sql.common.enums.CombineType;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.AggregationProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ColumnProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class SubqueryExtractUtilsTest {
    
    @Test
    void assertGetSubquerySegmentsInWhere() {
        MySQLSelectStatement subquerySelectStatement = new MySQLSelectStatement();
        subquerySelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(73, 99, new IdentifierValue("t_order"))));
        subquerySelectStatement.setProjections(new ProjectionsSegment(59, 66));
        subquerySelectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(59, 66, new IdentifierValue("order_id"))));
        ColumnSegment subqueryWhereLeft = new ColumnSegment(87, 92, new IdentifierValue("status"));
        LiteralExpressionSegment subqueryWhereRight = new LiteralExpressionSegment(96, 99, "OK");
        WhereSegment subqueryWhereSegment = new WhereSegment(81, 99, new BinaryOperationExpression(87, 99, subqueryWhereLeft, subqueryWhereRight, "=", "status = 'OK'"));
        subquerySelectStatement.setWhere(subqueryWhereSegment);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(21, 32, new IdentifierValue("t_order_item"))));
        selectStatement.setProjections(new ProjectionsSegment(7, 14));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 14, new IdentifierValue("order_id"))));
        ColumnSegment left = new ColumnSegment(40, 47, new IdentifierValue("order_id"));
        SubqueryExpressionSegment right = new SubqueryExpressionSegment(new SubquerySegment(51, 100, subquerySelectStatement));
        WhereSegment whereSegment = new WhereSegment(34, 100, new BinaryOperationExpression(40, 100, left, right, "=", "order_id = (SELECT order_id FROM t_order WHERE status = 'OK')"));
        selectStatement.setWhere(whereSegment);
        Collection<SubquerySegment> result = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next(), is(right.getSubquery()));
    }
    
    @Test
    void assertGetSubquerySegmentsInProjection() {
        ColumnSegment left = new ColumnSegment(41, 48, new IdentifierValue("order_id"));
        ColumnSegment right = new ColumnSegment(52, 62, new IdentifierValue("order_id"));
        MySQLSelectStatement subquerySelectStatement = new MySQLSelectStatement();
        subquerySelectStatement.setWhere(new WhereSegment(35, 62, new BinaryOperationExpression(41, 62, left, right, "=", "order_id = oi.order_id")));
        SubquerySegment subquerySegment = new SubquerySegment(7, 63, subquerySelectStatement);
        SubqueryProjectionSegment subqueryProjectionSegment = new SubqueryProjectionSegment(subquerySegment, "(SELECT status FROM t_order WHERE order_id = oi.order_id)");
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(7, 79));
        selectStatement.getProjections().getProjections().add(subqueryProjectionSegment);
        Collection<SubquerySegment> result = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next(), is(subquerySegment));
    }
    
    @Test
    void assertGetSubquerySegmentsInFrom1() {
        MySQLSelectStatement subquery = new MySQLSelectStatement();
        ColumnSegment left = new ColumnSegment(59, 66, new IdentifierValue("order_id"));
        LiteralExpressionSegment right = new LiteralExpressionSegment(70, 70, 1);
        subquery.setWhere(new WhereSegment(53, 70, new BinaryOperationExpression(59, 70, left, right, "=", "order_id = 1")));
        subquery.setFrom(new SimpleTableSegment(new TableNameSegment(45, 51, new IdentifierValue("t_order"))));
        subquery.setProjections(new ProjectionsSegment(31, 38));
        subquery.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(31, 38, new IdentifierValue("order_id"))));
        
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(7, 16));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 16, new IdentifierValue("order_id"))));
        selectStatement.setFrom(new SubqueryTableSegment(new SubquerySegment(23, 71, subquery)));
        
        Collection<SubquerySegment> result = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(result.size(), is(1));
        assertThat(result.iterator().next(), is(((SubqueryTableSegment) selectStatement.getFrom()).getSubquery()));
    }
    
    @Test
    void assertGetSubquerySegmentsInFrom2() {
        MySQLSelectStatement subqueryLeftSelectStatement = new MySQLSelectStatement();
        subqueryLeftSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(65, 71, new IdentifierValue("t_order"))));
        ColumnSegment leftColumnSegment = new ColumnSegment(79, 84, new IdentifierValue("status"));
        LiteralExpressionSegment leftLiteralExpressionSegment = new LiteralExpressionSegment(88, 91, "OK");
        BinaryOperationExpression leftStatusCondition = new BinaryOperationExpression(79, 91, leftColumnSegment, leftLiteralExpressionSegment, "=", "status = OK");
        subqueryLeftSelectStatement.setWhere(new WhereSegment(73, 91, leftStatusCondition));
        subqueryLeftSelectStatement.setProjections(new ProjectionsSegment(34, 58));
        subqueryLeftSelectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(34, 41, new IdentifierValue("order_id"))));
        subqueryLeftSelectStatement.getProjections().getProjections().add(new AggregationProjectionSegment(44, 51, AggregationType.COUNT, "COUNT(*)"));
        MySQLSelectStatement subqueryRightSelectStatement = new MySQLSelectStatement();
        subqueryRightSelectStatement.setFrom(new SimpleTableSegment(new TableNameSegment(143, 154, new IdentifierValue("t_order_item"))));
        ColumnSegment rightColumnSegment = new ColumnSegment(162, 167, new IdentifierValue("status"));
        LiteralExpressionSegment rightLiteralExpressionSegment = new LiteralExpressionSegment(171, 174, "OK");
        BinaryOperationExpression rightStatusCondition = new BinaryOperationExpression(162, 174, rightColumnSegment, rightLiteralExpressionSegment, "=", "status = OK");
        subqueryRightSelectStatement.setWhere(new WhereSegment(156, 174, rightStatusCondition));
        subqueryRightSelectStatement.setProjections(new ProjectionsSegment(112, 136));
        subqueryRightSelectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(112, 119, new IdentifierValue("order_id"))));
        subqueryRightSelectStatement.getProjections().getProjections().add(new AggregationProjectionSegment(122, 129, AggregationType.COUNT, "COUNT(*)"));
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(new ProjectionsSegment(7, 19));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(7, 11, new IdentifierValue("cnt"))));
        selectStatement.getProjections().getProjections().add(new ColumnProjectionSegment(new ColumnSegment(14, 19, new IdentifierValue("cnt"))));
        JoinTableSegment from = new JoinTableSegment();
        from.setStartIndex(26);
        from.setStopIndex(213);
        ColumnSegment columnSegment1 = new ColumnSegment(190, 199, new IdentifierValue("order_id"));
        ColumnSegment columnSegment2 = new ColumnSegment(203, 213, new IdentifierValue("order_id"));
        BinaryOperationExpression orderIdCondition = new BinaryOperationExpression(190, 213, columnSegment1, columnSegment2, "=", "o.order_id = oi.order_id");
        from.setCondition(orderIdCondition);
        SubqueryTableSegment leftSubquerySegment = new SubqueryTableSegment(new SubquerySegment(26, 92, subqueryLeftSelectStatement));
        SubqueryTableSegment rightSubquerySegment = new SubqueryTableSegment(new SubquerySegment(104, 175, subqueryRightSelectStatement));
        from.setLeft(leftSubquerySegment);
        from.setRight(rightSubquerySegment);
        selectStatement.setFrom(from);
        Collection<SubquerySegment> result = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(result.size(), is(2));
        Iterator<SubquerySegment> iterator = result.iterator();
        assertThat(iterator.next(), is(leftSubquerySegment.getSubquery()));
        assertThat(iterator.next(), is(rightSubquerySegment.getSubquery()));
    }
    
    @Test
    void assertGetSubquerySegmentsWithMultiNestedSubquery() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SubqueryTableSegment(createSubquerySegmentForFrom()));
        Collection<SubquerySegment> result = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(result.size(), is(2));
    }
    
    private SubquerySegment createSubquerySegmentForFrom() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        selectStatement.setWhere(new WhereSegment(0, 0, new InExpression(0, 0,
                left, new SubqueryExpressionSegment(new SubquerySegment(0, 0, new MySQLSelectStatement())), false)));
        return new SubquerySegment(0, 0, selectStatement);
    }
    
    @Test
    void assertGetSubquerySegmentsWithCombineSegment() {
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setCombine(new CombineSegment(0, 0, new MySQLSelectStatement(), CombineType.UNION, createSelectStatementForCombineSegment()));
        Collection<SubquerySegment> actual = SubqueryExtractUtils.getSubquerySegments(selectStatement);
        assertThat(actual.size(), is(1));
    }
    
    private SelectStatement createSelectStatementForCombineSegment() {
        SelectStatement result = new MySQLSelectStatement();
        ExpressionSegment left = new ColumnSegment(0, 0, new IdentifierValue("order_id"));
        result.setWhere(new WhereSegment(0, 0, new InExpression(0, 0,
                left, new SubqueryExpressionSegment(new SubquerySegment(0, 0, new MySQLSelectStatement())), false)));
        return result;
    }
}
