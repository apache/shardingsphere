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

package org.apache.shardingsphere.encrypt.checker.sql.openquery;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.encrypt.exception.syntax.UnsupportedEncryptSQLException;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.fixture.EncryptGeneratorFixtureBuilder;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.sql.parser.statement.core.enums.CombineType;
import org.apache.shardingsphere.sql.parser.statement.core.enums.OrderDirection;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.InsertValuesSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.combine.CombineSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BetweenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.CaseWhenExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExpressionWithParamsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.KeyValueSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.NotExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.QuantifySubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ExpressionProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.SubqueryProjectionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.merge.MergeWhenAndThenSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.OrderBySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.order.item.ExpressionOrderByItemSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.HavingSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.DeleteMultiTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.JoinTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SubqueryTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

final class EncryptOpenQuerySupportedCheckerTest {
    
    @Test
    void assertIsCheckWithSelectFromOpenQuery() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithFunctionTableNestedOpenQuery() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        FunctionSegment innerFunc = new FunctionSegment(0, 80, "OPENJSON", "OPENJSON((SELECT ...))");
        innerFunc.getParameters().add(new SubqueryExpressionSegment(subquerySegment));
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 80, innerFunc)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectWhereExistsOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, new ExistsSubqueryExpression(0, 80, subquerySegment))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectCTEOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        WithSegment withSegment = createWithSegmentContainingOpenQuery();
        when(selectStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectCombineOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment rightSubquery = mockSubqueryWithOpenQueryFrom();
        SubquerySegment leftSubquery = mockSubqueryWithEmptySelect();
        when(selectStatement.getCombine()).thenReturn(Optional.of(new CombineSegment(0, 80, leftSubquery, CombineType.UNION_ALL, rightSubquery)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectProjectionSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ProjectionsSegment projections = new ProjectionsSegment(0, 80);
        projections.getProjections().add(new SubqueryProjectionSegment(subquerySegment, "(SELECT ...)"));
        when(selectStatement.getProjections()).thenReturn(projections);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectHavingOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        SubqueryExpressionSegment subqueryExpr = new SubqueryExpressionSegment(subquerySegment);
        when(selectStatement.getHaving()).thenReturn(Optional.of(new HavingSegment(0, 80,
                new BinaryOperationExpression(0, 80, new LiteralExpressionSegment(0, 1, 1), subqueryExpr, ">", "1 > (SELECT ...)"))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSelectSubqueryTableOpenQuery() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SubqueryTableSegment(0, 80, subquerySegment)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithInSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        InExpression inExpr = new InExpression(0, 80, new LiteralExpressionSegment(0, 2, "id"), new SubqueryExpressionSegment(subquerySegment), false);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, inExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithBinarySubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        BinaryOperationExpression binaryExpr = new BinaryOperationExpression(0, 80, new LiteralExpressionSegment(0, 2, "col"),
                new SubqueryExpressionSegment(subquerySegment), "=", "col = (SELECT ...)");
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, binaryExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithNotExistsOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        NotExpression notExpr = new NotExpression(0, 80, new ExistsSubqueryExpression(0, 80, subquerySegment), false);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, notExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithQuantifySubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        QuantifySubqueryExpression quantifyExpr = new QuantifySubqueryExpression(0, 80, subquerySegment, "ALL");
        BinaryOperationExpression binaryExpr = new BinaryOperationExpression(0, 80, new LiteralExpressionSegment(0, 2, "col"), quantifyExpr, ">", "col > ALL (SELECT ...)");
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, binaryExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithBetweenSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        BetweenExpression betweenExpr = new BetweenExpression(0, 80, new LiteralExpressionSegment(0, 2, "id"),
                new SubqueryExpressionSegment(subquerySegment), new LiteralExpressionSegment(0, 3, 100), false);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, betweenExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithBetweenLeftSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        BetweenExpression betweenExpr = new BetweenExpression(0, 80, new SubqueryExpressionSegment(subquerySegment),
                new LiteralExpressionSegment(0, 1, 0), new LiteralExpressionSegment(0, 1, 2), false);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, betweenExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithOrderBySubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubqueryExpressionSegment subqueryExpr = new SubqueryExpressionSegment(mockSubqueryWithOpenQueryFrom());
        ExpressionOrderByItemSegment orderByItem = new ExpressionOrderByItemSegment(0, 80, "(SELECT ...)", OrderDirection.ASC, null, subqueryExpr);
        when(selectStatement.getOrderBy()).thenReturn(Optional.of(new OrderBySegment(0, 80, Collections.singletonList(orderByItem))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithCaseWhenSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        CaseWhenExpression caseWhenExpr = new CaseWhenExpression(0, 80, null,
                Collections.singletonList(new ExistsSubqueryExpression(0, 80, subquerySegment)),
                Collections.singletonList(new LiteralExpressionSegment(0, 1, 1)), new LiteralExpressionSegment(0, 1, 0), "CASE WHEN ...");
        ProjectionsSegment projections = new ProjectionsSegment(0, 80);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 80, "CASE WHEN ...", caseWhenExpr));
        when(selectStatement.getProjections()).thenReturn(projections);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithFunctionParamSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        FunctionSegment funcSeg = new FunctionSegment(0, 80, "ISNULL", "ISNULL((SELECT ...), 0)");
        funcSeg.getParameters().add(new SubqueryExpressionSegment(subquerySegment));
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80,
                new BinaryOperationExpression(0, 80, funcSeg, new LiteralExpressionSegment(0, 1, 0), ">", "ISNULL(...) > 0"))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithJoinConditionSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_t1"))));
        joinTable.setRight(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_t2"))));
        joinTable.setCondition(new ExistsSubqueryExpression(0, 80, subquerySegment));
        when(selectStatement.getFrom()).thenReturn(Optional.of(joinTable));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateTableOpenQuery() {
        UpdateStatement updateStatement = mockSQLServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateFromOpenQuery() {
        UpdateStatement updateStatement = mockSQLServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(updateStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateSetSubqueryOpenQuery() {
        UpdateStatement updateStatement = mockSQLServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(updateStatement.getFrom()).thenReturn(Optional.empty());
        when(updateStatement.getWhere()).thenReturn(Optional.empty());
        when(updateStatement.getWith()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ColumnSegment column = new ColumnSegment(0, 3, new IdentifierValue("foo_col"));
        ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(0, 80, Collections.singletonList(column), new SubqueryExpressionSegment(subquerySegment));
        when(updateStatement.getAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 80, Collections.singletonList(assignment))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateWhereSubqueryOpenQuery() {
        UpdateStatement updateStatement = mockSQLServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(updateStatement.getFrom()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        when(updateStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, new ExistsSubqueryExpression(0, 80, subquerySegment))));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithDeleteTableOpenQuery() {
        DeleteStatement deleteStatement = mockSQLServerStatement(DeleteStatement.class);
        when(deleteStatement.getTable()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(deleteStatement)));
    }
    
    @Test
    void assertIsCheckWithDeleteMultiTableOpenQuery() {
        DeleteStatement deleteStatement = mockSQLServerStatement(DeleteStatement.class);
        DeleteMultiTableSegment multiTableSegment = new DeleteMultiTableSegment();
        JoinTableSegment joinTable = new JoinTableSegment();
        joinTable.setLeft(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        joinTable.setRight(createOpenQueryFunctionTableSegment());
        multiTableSegment.setRelationTable(joinTable);
        when(deleteStatement.getTable()).thenReturn(multiTableSegment);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(deleteStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertOpenQuery() {
        InsertStatement insertStatement = mockSQLServerStatement(InsertStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.of(funcSeg));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertSelectOpenQuery() {
        InsertStatement insertStatement = mockSQLServerStatement(InsertStatement.class);
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        when(insertStatement.getInsertSelect()).thenReturn(Optional.of(subquerySegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertCTEOpenQuery() {
        InsertStatement insertStatement = mockSQLServerStatement(InsertStatement.class);
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.empty());
        when(insertStatement.getInsertSelect()).thenReturn(Optional.empty());
        WithSegment withSegment = createWithSegmentContainingOpenQuery();
        when(insertStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertValuesSubqueryOpenQuery() {
        InsertStatement insertStatement = mockSQLServerStatement(InsertStatement.class);
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.empty());
        when(insertStatement.getInsertSelect()).thenReturn(Optional.empty());
        when(insertStatement.getWith()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        InsertValuesSegment valuesSegment = new InsertValuesSegment(0, 80, Arrays.asList(new SubqueryExpressionSegment(subquerySegment)));
        when(insertStatement.getValues()).thenReturn(Collections.singletonList(valuesSegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithMergeSourceOpenQuery() {
        MergeStatement mergeStatement = mockSQLServerStatement(MergeStatement.class);
        when(mergeStatement.getSource()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mergeStatement)));
    }
    
    @Test
    void assertIsCheckWithMergeOnConditionOpenQuery() {
        MergeStatement mergeStatement = mockSQLServerStatement(MergeStatement.class);
        when(mergeStatement.getTarget()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(mergeStatement.getSource()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_src"))));
        when(mergeStatement.getWith()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ExpressionWithParamsSegment onClause = new ExpressionWithParamsSegment(0, 80, new ExistsSubqueryExpression(0, 80, subquerySegment));
        when(mergeStatement.getExpression()).thenReturn(onClause);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mergeStatement)));
    }
    
    @Test
    void assertIsCheckWithMergeWhenConditionOpenQuery() {
        MergeStatement mergeStatement = mockSQLServerStatement(MergeStatement.class);
        when(mergeStatement.getTarget()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(mergeStatement.getSource()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_src"))));
        when(mergeStatement.getWith()).thenReturn(Optional.empty());
        when(mergeStatement.getExpression()).thenReturn(null);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        MergeWhenAndThenSegment whenAndThen = new MergeWhenAndThenSegment(0, 80, "WHEN MATCHED AND EXISTS (...) THEN DELETE");
        whenAndThen.setAndExpr(new ExistsSubqueryExpression(0, 80, subquerySegment));
        when(mergeStatement.getWhenAndThens()).thenReturn(Collections.singletonList(whenAndThen));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mergeStatement)));
    }
    
    @Test
    void assertIsCheckWithMergeWhenUpdateSetOpenQuery() {
        MergeStatement mergeStatement = mockSQLServerStatement(MergeStatement.class);
        when(mergeStatement.getTarget()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(mergeStatement.getSource()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_src"))));
        when(mergeStatement.getWith()).thenReturn(Optional.empty());
        when(mergeStatement.getExpression()).thenReturn(null);
        UpdateStatement innerUpdate = mock(UpdateStatement.class, RETURNS_DEEP_STUBS);
        when(innerUpdate.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table"))));
        when(innerUpdate.getFrom()).thenReturn(Optional.empty());
        when(innerUpdate.getWhere()).thenReturn(Optional.empty());
        when(innerUpdate.getWith()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ColumnSegment column = new ColumnSegment(0, 3, new IdentifierValue("foo_col"));
        ColumnAssignmentSegment assignment = new ColumnAssignmentSegment(0, 80, Collections.singletonList(column), new SubqueryExpressionSegment(subquerySegment));
        when(innerUpdate.getAssignment()).thenReturn(Optional.of(new SetAssignmentSegment(0, 80, Collections.singletonList(assignment))));
        MergeWhenAndThenSegment whenAndThen = new MergeWhenAndThenSegment(0, 80, "WHEN MATCHED THEN UPDATE SET ...");
        whenAndThen.setUpdate(innerUpdate);
        when(mergeStatement.getWhenAndThens()).thenReturn(Collections.singletonList(whenAndThen));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mergeStatement)));
    }
    
    @Test
    void assertIsCheckWithMySQL() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mockNonSQLServerSelectWithOpenQuery("MySQL"))));
    }
    
    @Test
    void assertIsCheckWithPostgreSQL() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mockNonSQLServerSelectWithOpenQuery("PostgreSQL"))));
    }
    
    @Test
    void assertIsCheckWithOpenGauss() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mockNonSQLServerSelectWithOpenQuery("openGauss"))));
    }
    
    @Test
    void assertIsCheckWithOracle() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(mockNonSQLServerSelectWithOpenQuery("Oracle"))));
    }
    
    @Test
    void assertIsCheckWithNonOpenQueryFunction() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 20, "SOME_FUNC", "SOME_FUNC()");
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 20, funcSeg)));
        stubSelectNegativePaths(selectStatement);
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSimpleTable() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithOpenQueryCaseInsensitive() {
        SelectStatement selectStatement = mockSQLServerStatement(SelectStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 50, "openquery", "openquery(Server, 'SELECT 1')");
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 50, funcSeg)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithKeyValueValueSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        KeyValueSegment keyValueSegment = new KeyValueSegment(0, 80, "JSON_OBJECT(...)");
        keyValueSegment.setValue(new SubqueryExpressionSegment(subquerySegment));
        FunctionSegment funcSeg = new FunctionSegment(0, 80, "JSON_OBJECT", "JSON_OBJECT(...)");
        funcSeg.getParameters().add(keyValueSegment);
        ProjectionsSegment projections = new ProjectionsSegment(0, 80);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 80, "JSON_OBJECT(...)", funcSeg));
        when(selectStatement.getProjections()).thenReturn(projections);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithKeyValueKeySubqueryOpenQuery() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        KeyValueSegment keyValueSegment = new KeyValueSegment(0, 80, "JSON_OBJECT(...)");
        keyValueSegment.setKey(new SubqueryExpressionSegment(subquerySegment));
        keyValueSegment.setValue(new LiteralExpressionSegment(0, 5, "foo_value"));
        FunctionSegment funcSeg = new FunctionSegment(0, 80, "JSON_OBJECT", "JSON_OBJECT(...)");
        funcSeg.getParameters().add(keyValueSegment);
        ProjectionsSegment projections = new ProjectionsSegment(0, 80);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 80, "JSON_OBJECT(...)", funcSeg));
        when(selectStatement.getProjections()).thenReturn(projections);
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithOrdinaryKeyValue() {
        SelectStatement selectStatement = mockSQLServerSelectWithSimpleFrom();
        stubSelectNegativePaths(selectStatement);
        KeyValueSegment keyValueSegment = new KeyValueSegment(0, 80, "JSON_OBJECT(...)");
        keyValueSegment.setKey(new LiteralExpressionSegment(0, 5, "foo_key"));
        keyValueSegment.setValue(new LiteralExpressionSegment(0, 5, "foo_value"));
        FunctionSegment funcSeg = new FunctionSegment(0, 80, "JSON_OBJECT", "JSON_OBJECT(...)");
        funcSeg.getParameters().add(keyValueSegment);
        ProjectionsSegment projections = new ProjectionsSegment(0, 80);
        projections.getProjections().add(new ExpressionProjectionSegment(0, 80, "JSON_OBJECT(...)", funcSeg));
        when(selectStatement.getProjections()).thenReturn(projections);
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSQLStatementContext(selectStatement)));
    }
    
    @Test
    void assertCheck() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> new EncryptOpenQuerySupportedChecker().check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, sqlStatementContext));
    }
    
    private <T extends SQLStatement> T mockSQLServerStatement(final Class<T> statementClass) {
        T result = mock(statementClass, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn("SQLServer");
        when(result.getDatabaseType()).thenReturn(databaseType);
        return result;
    }
    
    private FunctionTableSegment createOpenQueryFunctionTableSegment() {
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        return new FunctionTableSegment(0, 60, funcSeg);
    }
    
    private SQLStatementContext createSQLStatementContext(final SQLStatement sqlStatement) {
        SQLStatementContext result = mock(SQLStatementContext.class);
        when(result.getSqlStatement()).thenReturn(sqlStatement);
        return result;
    }
    
    private SubquerySegment mockSubqueryWithOpenQueryFrom() {
        SelectStatement innerSelect = mock(SelectStatement.class);
        when(innerSelect.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        stubSelectNegativePaths(innerSelect);
        SubquerySegment result = mock(SubquerySegment.class);
        when(result.getSelect()).thenReturn(innerSelect);
        return result;
    }
    
    private SelectStatement mockSQLServerSelectWithSimpleFrom() {
        SelectStatement result = mockSQLServerStatement(SelectStatement.class);
        when(result.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table")))));
        return result;
    }
    
    private void stubSelectNegativePaths(final SelectStatement selectStatement) {
        when(selectStatement.getWhere()).thenReturn(Optional.empty());
        when(selectStatement.getWith()).thenReturn(Optional.empty());
        when(selectStatement.getCombine()).thenReturn(Optional.empty());
        when(selectStatement.getHaving()).thenReturn(Optional.empty());
        when(selectStatement.getOrderBy()).thenReturn(Optional.empty());
        when(selectStatement.getProjections()).thenReturn(new ProjectionsSegment(0, 0));
    }
    
    private WithSegment createWithSegmentContainingOpenQuery() {
        SubquerySegment cteSubquery = mockSubqueryWithOpenQueryFrom();
        CommonTableExpressionSegment cteSeg = new CommonTableExpressionSegment(0, 80, new AliasSegment(0, 3, new IdentifierValue("foo_cte")), cteSubquery);
        return new WithSegment(0, 80, Collections.singletonList(cteSeg));
    }
    
    private SubquerySegment mockSubqueryWithEmptySelect() {
        SelectStatement innerSelect = mock(SelectStatement.class);
        when(innerSelect.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("foo_table")))));
        stubSelectNegativePaths(innerSelect);
        SubquerySegment result = mock(SubquerySegment.class);
        when(result.getSelect()).thenReturn(innerSelect);
        return result;
    }
    
    private SelectStatement mockNonSQLServerSelectWithOpenQuery(final String databaseTypeName) {
        SelectStatement result = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn(databaseTypeName);
        when(result.getDatabaseType()).thenReturn(databaseType);
        when(result.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        return result;
    }
}
