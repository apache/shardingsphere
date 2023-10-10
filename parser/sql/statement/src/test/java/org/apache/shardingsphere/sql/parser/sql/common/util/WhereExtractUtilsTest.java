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

import com.google.common.base.Preconditions;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
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

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WhereExtractUtilsTest {
    
    @Test
    void assertGetJoinWhereSegmentsWithEmptySelectStatement() {
        assertTrue(WhereExtractUtils.getJoinWhereSegments(new MySQLSelectStatement()).isEmpty());
    }
    
    @Test
    void assertGetJoinWhereSegments() {
        JoinTableSegment tableSegment = new JoinTableSegment();
        ColumnSegment left = new ColumnSegment(57, 67, new IdentifierValue("order_id"));
        ColumnSegment right = new ColumnSegment(71, 80, new IdentifierValue("order_id"));
        tableSegment.setCondition(new BinaryOperationExpression(1, 31, left, right, "=", "oi.order_id = o.order_id"));
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(tableSegment);
        Collection<WhereSegment> joinWhereSegments = WhereExtractUtils.getJoinWhereSegments(selectStatement);
        assertThat(joinWhereSegments.size(), is(1));
        WhereSegment actual = joinWhereSegments.iterator().next();
        assertThat(actual.getExpr(), is(tableSegment.getCondition()));
    }
    
    @Test
    void assertGetSubqueryWhereSegmentsFromSubqueryTableSegment() {
        MySQLSelectStatement subQuerySelectStatement = new MySQLSelectStatement();
        ColumnSegment left = new ColumnSegment(41, 48, new IdentifierValue("order_id"));
        ColumnSegment right = new ColumnSegment(52, 62, new IdentifierValue("order_id"));
        WhereSegment where = new WhereSegment(35, 62, new BinaryOperationExpression(41, 62, left, right, "=", "order_id = oi.order_id"));
        subQuerySelectStatement.setWhere(where);
        ProjectionsSegment projections = new ProjectionsSegment(7, 79);
        projections.getProjections().add(new SubqueryProjectionSegment(new SubquerySegment(7, 63, subQuerySelectStatement, ""), "(SELECT status FROM t_order WHERE order_id = oi.order_id)"));
        SelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setProjections(projections);
        Collection<WhereSegment> subqueryWhereSegments = WhereExtractUtils.getSubqueryWhereSegments(selectStatement);
        WhereSegment actual = subqueryWhereSegments.iterator().next();
        Preconditions.checkState(subQuerySelectStatement.getWhere().isPresent());
        assertThat(actual.getExpr(), is(subQuerySelectStatement.getWhere().get().getExpr()));
    }
    
    @Test
    void assertGetWhereSegmentsFromSubQueryJoin() {
        JoinTableSegment joinTableSegment = new JoinTableSegment();
        joinTableSegment.setLeft(new SimpleTableSegment(new TableNameSegment(37, 39, new IdentifierValue("t_order"))));
        joinTableSegment.setRight(new SimpleTableSegment(new TableNameSegment(54, 56, new IdentifierValue("t_order_item"))));
        joinTableSegment.setJoinType("INNER");
        joinTableSegment.setCondition(new BinaryOperationExpression(63, 83, new ColumnSegment(63, 71, new IdentifierValue("order_id")),
                new ColumnSegment(75, 83, new IdentifierValue("order_id")), "=", "oi.order_id = o.order_id"));
        MySQLSelectStatement subQuerySelectStatement = new MySQLSelectStatement();
        subQuerySelectStatement.setFrom(joinTableSegment);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setFrom(new SubqueryTableSegment(new SubquerySegment(20, 84, subQuerySelectStatement, "")));
        Collection<WhereSegment> subqueryWhereSegments = WhereExtractUtils.getSubqueryWhereSegments(selectStatement);
        WhereSegment actual = subqueryWhereSegments.iterator().next();
        assertThat(actual.getExpr(), is(((JoinTableSegment) subQuerySelectStatement.getFrom()).getCondition()));
    }
}
