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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.BinaryOperationExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.ExistsSubqueryExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.InExpression;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.complex.CommonTableExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.simple.LiteralExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubqueryExpressionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.subquery.SubquerySegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.predicate.WhereSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.AliasSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.WithSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
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
    void assertIsCheckWithSelectOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateOpenQuery() {
        UpdateStatement updateStatement = mockSqlServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateFromOpenQuery() {
        UpdateStatement updateStatement = mockSqlServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("t"))));
        when(updateStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithDeleteOpenQuery() {
        DeleteStatement deleteStatement = mockSqlServerStatement(DeleteStatement.class);
        when(deleteStatement.getTable()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(deleteStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertOpenQuery() {
        InsertStatement insertStatement = mockSqlServerStatement(InsertStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.of(funcSeg));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithInsertSelectOpenQuery() {
        InsertStatement insertStatement = mockSqlServerStatement(InsertStatement.class);
        when(insertStatement.getRowSetFunction()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        when(insertStatement.getInsertSelect()).thenReturn(Optional.of(subquerySegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(insertStatement)));
    }
    
    @Test
    void assertIsCheckWithMergeUsingOpenQuery() {
        MergeStatement mergeStatement = mockSqlServerStatement(MergeStatement.class);
        when(mergeStatement.getSource()).thenReturn(createOpenQueryFunctionTableSegment());
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(mergeStatement)));
    }
    
    @Test
    void assertIsCheckWithSubqueryContainingOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        SubqueryTableSegment subqueryTableSegment = new SubqueryTableSegment(0, 80, subquerySegment);
        when(selectStatement.getFrom()).thenReturn(Optional.of(subqueryTableSegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithExistsSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("t")))));
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ExistsSubqueryExpression existsExpr = new ExistsSubqueryExpression(0, 80, subquerySegment);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, existsExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithInSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("t")))));
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        SubqueryExpressionSegment subqueryExprSeg = new SubqueryExpressionSegment(subquerySegment);
        InExpression inExpr = new InExpression(0, 80, new LiteralExpressionSegment(0, 5, "col"), subqueryExprSeg, false);
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, inExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithBinaryExpressionSubqueryOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("t")))));
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        SubqueryExpressionSegment subqueryExprSeg = new SubqueryExpressionSegment(subquerySegment);
        BinaryOperationExpression binaryExpr = new BinaryOperationExpression(0, 80, new LiteralExpressionSegment(0, 5, "col"), subqueryExprSeg, "=", "col = (SELECT ...)");
        when(selectStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, binaryExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithCTEOpenQuery() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("cte")))));
        when(selectStatement.getWhere()).thenReturn(Optional.empty());
        SubquerySegment cteSubquery = mockSubqueryWithOpenQueryFrom();
        CommonTableExpressionSegment cteSeg = new CommonTableExpressionSegment(0, 80, new AliasSegment(0, 3, new IdentifierValue("cte")), cteSubquery);
        WithSegment withSegment = new WithSegment(0, 80, Collections.singletonList(cteSeg));
        when(selectStatement.getWith()).thenReturn(Optional.of(withSegment));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithUpdateWhereSubqueryOpenQuery() {
        UpdateStatement updateStatement = mockSqlServerStatement(UpdateStatement.class);
        when(updateStatement.getTable()).thenReturn(new SimpleTableSegment(new TableNameSegment(0, 5, new IdentifierValue("t"))));
        when(updateStatement.getFrom()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mockSubqueryWithOpenQueryFrom();
        ExistsSubqueryExpression existsExpr = new ExistsSubqueryExpression(0, 80, subquerySegment);
        when(updateStatement.getWhere()).thenReturn(Optional.of(new WhereSegment(0, 80, existsExpr)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(updateStatement)));
    }
    
    @Test
    void assertIsCheckWithMySQL() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(mockNonSqlServerSelectWithOpenQuery("MySQL"))));
    }
    
    @Test
    void assertIsCheckWithPostgreSQL() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(mockNonSqlServerSelectWithOpenQuery("PostgreSQL"))));
    }
    
    @Test
    void assertIsCheckWithOpenGauss() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(mockNonSqlServerSelectWithOpenQuery("openGauss"))));
    }
    
    @Test
    void assertIsCheckWithOracle() {
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(mockNonSqlServerSelectWithOpenQuery("Oracle"))));
    }
    
    @Test
    void assertIsCheckWithNonOpenQueryFunction() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 20, "SOME_FUNC", "SOME_FUNC()");
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 20, funcSeg)));
        when(selectStatement.getWhere()).thenReturn(Optional.empty());
        when(selectStatement.getWith()).thenReturn(Optional.empty());
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSimpleTable() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_order")))));
        when(selectStatement.getWhere()).thenReturn(Optional.empty());
        when(selectStatement.getWith()).thenReturn(Optional.empty());
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithOpenQueryCaseInsensitive() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 50, "openquery", "openquery(Server, 'SELECT 1')");
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 50, funcSeg)));
        assertTrue(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertCheckThrowsException() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        assertThrows(UnsupportedEncryptSQLException.class,
                () -> new EncryptOpenQuerySupportedChecker().check(EncryptGeneratorFixtureBuilder.createEncryptRule(), null, null, sqlStatementContext));
    }
    
    private SelectStatement mockNonSqlServerSelectWithOpenQuery(final String databaseTypeName) {
        SelectStatement selectStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn(databaseTypeName);
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        return selectStatement;
    }
    
    private <T extends SQLStatement> T mockSqlServerStatement(final Class<T> statementClass) {
        T statement = mock(statementClass, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn("SQLServer");
        when(statement.getDatabaseType()).thenReturn(databaseType);
        return statement;
    }
    
    private SQLStatementContext createSqlStatementContext(final SQLStatement sqlStatement) {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        return sqlStatementContext;
    }
    
    private SubquerySegment mockSubqueryWithOpenQueryFrom() {
        SelectStatement innerSelect = mock(SelectStatement.class);
        when(innerSelect.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        when(innerSelect.getWhere()).thenReturn(Optional.empty());
        when(innerSelect.getWith()).thenReturn(Optional.empty());
        SubquerySegment subquerySegment = mock(SubquerySegment.class);
        when(subquerySegment.getSelect()).thenReturn(innerSelect);
        return subquerySegment;
    }
    
    private FunctionTableSegment createOpenQueryFunctionTableSegment() {
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        return new FunctionTableSegment(0, 60, funcSeg);
    }
}
