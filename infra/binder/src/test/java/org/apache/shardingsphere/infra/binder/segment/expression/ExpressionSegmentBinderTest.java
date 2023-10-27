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

package org.apache.shardingsphere.infra.binder.segment.expression;

import org.apache.shardingsphere.infra.binder.enums.SegmentType;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementBinderContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.ExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.test.fixture.database.MockedDatabaseType;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ExpressionSegmentBinderTest {
    
    @Test
    void testBindWithBinaryOperationExpression() {
        BinaryOperationExpression binaryOperationExpression = new BinaryOperationExpression(0, 0, null, null, "+", "text");
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), Collections.emptyList());
        ExpressionSegment actual = ExpressionSegmentBinder.bind(binaryOperationExpression, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof BinaryOperationExpression);
        assertEquals(binaryOperationExpression.getLeft(), ((BinaryOperationExpression) actual).getLeft());
        assertEquals(binaryOperationExpression.getOperator(), ((BinaryOperationExpression) actual).getOperator());
        assertEquals(binaryOperationExpression.getRight(), ((BinaryOperationExpression) actual).getRight());
    }
    
    @Test
    void testBindWithExistsSubqueryExpression() {
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        mySQLSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        ExistsSubqueryExpression existsSubqueryExpression = new ExistsSubqueryExpression(0, 0, new SubquerySegment(0, 0, mySQLSelectStatement, "test"));
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), Collections.emptyList());
        ExpressionSegment actual = ExpressionSegmentBinder.bind(existsSubqueryExpression, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof ExistsSubqueryExpression);
        assertEquals(existsSubqueryExpression.getSubquery().getClass(), ((ExistsSubqueryExpression) actual).getSubquery().getClass());
    }
    
    @Test
    void testBindWithSubqueryExpressionSegment() {
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        mySQLSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        SubqueryExpressionSegment subqueryExpressionSegment = new SubqueryExpressionSegment(new SubquerySegment(0, 0, mySQLSelectStatement, "test"));
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), Collections.emptyList());
        ExpressionSegment actual = ExpressionSegmentBinder.bind(subqueryExpressionSegment, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof SubqueryExpressionSegment);
        assertEquals(subqueryExpressionSegment.getSubquery().getClass(), ((SubqueryExpressionSegment) actual).getSubquery().getClass());
    }
    
    @Test
    void testBindWithInExpression() {
        Collection<String> variables = new ArrayList<>();
        variables.add("t_order");
        MySQLSelectStatement mySQLSelectStatement = new MySQLSelectStatement();
        mySQLSelectStatement.setProjections(new ProjectionsSegment(0, 0));
        InExpression inExpression = new InExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("t_order")), null, true);
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), variables);
        ExpressionSegment actual = ExpressionSegmentBinder.bind(inExpression, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof InExpression);
        assertEquals(inExpression.getStopIndex(), ((InExpression) actual).getStopIndex());
    }
    
    @Test
    void testBindWithNotExpression() {
        Collection<String> variables = new ArrayList<>();
        variables.add("t_order");
        NotExpression notExpression = new NotExpression(0, 0, new ColumnSegment(0, 0, new IdentifierValue("t_order")), null);
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), variables);
        ExpressionSegment actual = ExpressionSegmentBinder.bind(notExpression, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof NotExpression);
        assertEquals(notExpression.getExpression().getClass(), ((NotExpression) actual).getExpression().getClass());
    }
    
    @Test
    void testBindWithColumnSegment() {
        Collection<String> variables = new ArrayList<>();
        variables.add("t_order");
        ColumnSegment columnSegment = new ColumnSegment(0, 0, new IdentifierValue("t_order"));
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), variables);
        ExpressionSegment actual = ExpressionSegmentBinder.bind(columnSegment, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof ColumnSegment);
        assertEquals(columnSegment.getIdentifier().getValue(), ((ColumnSegment) actual).getIdentifier().getValue());
    }
    
    @Test
    void testBindWithFunctionSegment() {
        FunctionSegment functionSegment = new FunctionSegment(0, 0, "SUM(*)", "text");
        SQLStatementBinderContext statementBinderContext = new SQLStatementBinderContext(new ShardingSphereMetaData(), "db", new MockedDatabaseType(), Collections.emptyList());
        ExpressionSegment actual = ExpressionSegmentBinder.bind(functionSegment, SegmentType.INSERT_COLUMNS, statementBinderContext, Collections.emptyMap(), Collections.emptyMap());
        assertTrue(actual instanceof FunctionSegment);
        assertEquals(functionSegment.getFunctionName(), ((FunctionSegment) actual).getFunctionName());
        assertEquals(functionSegment.getParameters().size(), ((FunctionSegment) actual).getParameters().size());
    }
}
