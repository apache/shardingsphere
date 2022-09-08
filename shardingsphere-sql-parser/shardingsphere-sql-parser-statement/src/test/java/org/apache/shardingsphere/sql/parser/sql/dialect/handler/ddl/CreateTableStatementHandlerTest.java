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

package org.apache.shardingsphere.sql.parser.sql.dialect.handler.ddl;

import static org.hamcrest.Matchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertFalse;

import java.util.List;
import java.util.Optional;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.ddl.MySQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.ddl.OracleCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.ddl.PostgreSQLCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.ddl.SQL92CreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.ddl.SQLServerCreateTableStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerSelectStatement;
import org.junit.Test;

public final class CreateTableStatementHandlerTest {
    
    @Test
    public void assertIfNotExists() {
        assertTrue(CreateTableStatementHandler.ifNotExists(new MySQLCreateTableStatement(true)));
        assertFalse(CreateTableStatementHandler.ifNotExists(new MySQLCreateTableStatement(false)));
        assertTrue(CreateTableStatementHandler.ifNotExists(new PostgreSQLCreateTableStatement(true)));
        assertFalse(CreateTableStatementHandler.ifNotExists(new PostgreSQLCreateTableStatement(false)));
        assertTrue(CreateTableStatementHandler.ifNotExists(new OpenGaussCreateTableStatement(true)));
        assertFalse(CreateTableStatementHandler.ifNotExists(new OpenGaussCreateTableStatement(false)));
        assertFalse(CreateTableStatementHandler.ifNotExists(new OracleCreateTableStatement()));
        assertFalse(CreateTableStatementHandler.ifNotExists(new SQLServerCreateTableStatement()));
        assertFalse(CreateTableStatementHandler.ifNotExists(new SQL92CreateTableStatement()));
    }
    
    @Test
    public void assertGetSelectStatement() {
        SQLServerCreateTableStatement sqlServerCreateTableStatement = new SQLServerCreateTableStatement();
        sqlServerCreateTableStatement.setSelectStatement(new SQLServerSelectStatement());
        Optional<SelectStatement> actual = CreateTableStatementHandler.getSelectStatement(sqlServerCreateTableStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(sqlServerCreateTableStatement.getSelectStatement().get()));
        assertFalse(CreateTableStatementHandler.getSelectStatement(new MySQLCreateTableStatement(false)).isPresent());
        assertFalse(CreateTableStatementHandler.getSelectStatement(new OpenGaussCreateTableStatement(false)).isPresent());
        assertFalse(CreateTableStatementHandler.getSelectStatement(new OracleCreateTableStatement()).isPresent());
        assertFalse(CreateTableStatementHandler.getSelectStatement(new PostgreSQLCreateTableStatement(false)).isPresent());
        assertFalse(CreateTableStatementHandler.getSelectStatement(new SQL92CreateTableStatement()).isPresent());
    }
    
    @Test
    public void assertGetColumns() {
        SQLServerCreateTableStatement sqlServerCreateTableStatement = new SQLServerCreateTableStatement();
        sqlServerCreateTableStatement.getColumns().add(new ColumnSegment(0, 1, new IdentifierValue("identifier")));
        List<ColumnSegment> actual = CreateTableStatementHandler.getColumns(sqlServerCreateTableStatement);
        assertThat(actual, is(sqlServerCreateTableStatement.getColumns()));
        assertTrue(CreateTableStatementHandler.getColumns(new MySQLCreateTableStatement(false)).isEmpty());
        assertTrue(CreateTableStatementHandler.getColumns(new OpenGaussCreateTableStatement(false)).isEmpty());
        assertTrue(CreateTableStatementHandler.getColumns(new OracleCreateTableStatement()).isEmpty());
        assertTrue(CreateTableStatementHandler.getColumns(new PostgreSQLCreateTableStatement(false)).isEmpty());
        assertTrue(CreateTableStatementHandler.getColumns(new SQL92CreateTableStatement()).isEmpty());
    }
}
