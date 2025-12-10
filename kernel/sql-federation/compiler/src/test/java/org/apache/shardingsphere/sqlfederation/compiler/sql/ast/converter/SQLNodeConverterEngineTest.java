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

package org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter;

import org.apache.calcite.sql.SqlNode;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.DALStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dal.ExplainStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DMLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.DeleteStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.MergeStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.UpdateStatement;
import org.apache.shardingsphere.sqlfederation.compiler.exception.SQLFederationSQLNodeConvertException;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.delete.DeleteStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.explain.ExplainStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.insert.InsertStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.merge.MergeStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.select.SelectStatementConverter;
import org.apache.shardingsphere.sqlfederation.compiler.sql.ast.converter.statement.update.UpdateStatementConverter;
import org.junit.jupiter.api.Test;
import org.mockito.MockedConstruction;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockConstruction;
import static org.mockito.Mockito.when;

class SQLNodeConverterEngineTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertConvertDMLStatements() {
        SqlNode selectSqlNode = mock(SqlNode.class);
        try (MockedConstruction<SelectStatementConverter> ignored = mockConstruction(SelectStatementConverter.class,
                (mock, context) -> when(mock.convert(any(SelectStatement.class))).thenReturn(selectSqlNode))) {
            assertThat(SQLNodeConverterEngine.convert(new SelectStatement(databaseType)), is(selectSqlNode));
        }
        SqlNode deleteSqlNode = mock(SqlNode.class);
        try (MockedConstruction<DeleteStatementConverter> ignored = mockConstruction(DeleteStatementConverter.class,
                (mock, context) -> when(mock.convert(any(DeleteStatement.class))).thenReturn(deleteSqlNode))) {
            assertThat(SQLNodeConverterEngine.convert(new DeleteStatement(databaseType)), is(deleteSqlNode));
        }
        SqlNode updateSqlNode = mock(SqlNode.class);
        try (MockedConstruction<UpdateStatementConverter> ignored = mockConstruction(UpdateStatementConverter.class,
                (mock, context) -> when(mock.convert(any(UpdateStatement.class))).thenReturn(updateSqlNode))) {
            assertThat(SQLNodeConverterEngine.convert(new UpdateStatement(databaseType)), is(updateSqlNode));
        }
        SqlNode insertSqlNode = mock(SqlNode.class);
        try (MockedConstruction<InsertStatementConverter> ignored = mockConstruction(InsertStatementConverter.class,
                (mock, context) -> when(mock.convert(any(InsertStatement.class))).thenReturn(insertSqlNode))) {
            InsertStatement insertStatement = new InsertStatement(databaseType);
            SqlNode actual = SQLNodeConverterEngine.convert(insertStatement);
            assertThat(actual, is(insertSqlNode));
        }
        SqlNode mergeSqlNode = mock(SqlNode.class);
        try (MockedConstruction<MergeStatementConverter> ignored = mockConstruction(MergeStatementConverter.class,
                (mock, context) -> when(mock.convert(any(MergeStatement.class))).thenReturn(mergeSqlNode))) {
            assertThat(SQLNodeConverterEngine.convert(new MergeStatement(databaseType)), is(mergeSqlNode));
        }
    }
    
    @Test
    void assertConvertExplainStatement() {
        SqlNode explainSqlNode = mock(SqlNode.class);
        ExplainStatement explainStatement = new ExplainStatement(databaseType, mock(SQLStatement.class));
        try (MockedConstruction<ExplainStatementConverter> ignored = mockConstruction(ExplainStatementConverter.class,
                (mock, context) -> when(mock.convert(any(ExplainStatement.class))).thenReturn(explainSqlNode))) {
            assertThat(SQLNodeConverterEngine.convert(explainStatement), is(explainSqlNode));
        }
    }
    
    @Test
    void assertConvertUnsupportedDMLThrowsException() {
        assertThrows(SQLFederationSQLNodeConvertException.class, () -> SQLNodeConverterEngine.convert(mock(DMLStatement.class)));
    }
    
    @Test
    void assertConvertUnsupportedDALThrowsException() {
        assertThrows(SQLFederationSQLNodeConvertException.class, () -> SQLNodeConverterEngine.convert(mock(DALStatement.class)));
    }
    
    @Test
    void assertConvertUnsupportedSQLStatementThrowsException() {
        assertThrows(SQLFederationSQLNodeConvertException.class, () -> SQLNodeConverterEngine.convert(mock(SQLStatement.class)));
    }
}
