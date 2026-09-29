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
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.expr.FunctionSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.FunctionTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

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
    void assertIsCheckWithNonSqlServerDatabase() {
        SelectStatement selectStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        DatabaseType databaseType = mock(DatabaseType.class);
        when(databaseType.getType()).thenReturn("MySQL");
        when(selectStatement.getDatabaseType()).thenReturn(databaseType);
        when(selectStatement.getFrom()).thenReturn(Optional.of(createOpenQueryFunctionTableSegment()));
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithNonOpenQueryFunction() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        FunctionSegment funcSeg = new FunctionSegment(0, 20, "SOME_FUNC", "SOME_FUNC()");
        when(selectStatement.getFrom()).thenReturn(Optional.of(new FunctionTableSegment(0, 20, funcSeg)));
        assertFalse(new EncryptOpenQuerySupportedChecker().isCheck(createSqlStatementContext(selectStatement)));
    }
    
    @Test
    void assertIsCheckWithSimpleTable() {
        SelectStatement selectStatement = mockSqlServerStatement(SelectStatement.class);
        when(selectStatement.getFrom()).thenReturn(Optional.of(new SimpleTableSegment(new TableNameSegment(0, 10, new IdentifierValue("t_order")))));
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
    
    private FunctionTableSegment createOpenQueryFunctionTableSegment() {
        FunctionSegment funcSeg = new FunctionSegment(0, 60, "OPENQUERY", "OPENQUERY(MyLinkedServer, 'SELECT GroupName FROM Department')");
        return new FunctionTableSegment(0, 60, funcSeg);
    }
}
