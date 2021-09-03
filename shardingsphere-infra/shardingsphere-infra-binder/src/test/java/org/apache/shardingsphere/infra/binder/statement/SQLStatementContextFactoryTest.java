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

package org.apache.shardingsphere.infra.binder.statement;

import org.apache.shardingsphere.infra.binder.SQLStatementContextFactory;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.AssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.ColumnAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.assignment.SetAssignmentSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.column.ColumnSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.sql.common.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.common.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.MySQLStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.mysql.dml.MySQLSelectStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;

public final class SQLStatementContextFactoryTest {
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(mockMetaDataMap(), Collections.emptyList(), selectStatement, DefaultSchema.LOGIC_NAME);
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof SelectStatementContext);
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfMySQLInsertStatement() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(new ColumnSegment(0, 0, new IdentifierValue("IdentifierValue")));
        AssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columnSegments, null);
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0,
                Collections.singleton(assignment)));
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(insertStatement);
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfOracleInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new OracleInsertStatement());
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfPostgreSQLInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new PostgreSQLInsertStatement());
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSQL92InsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new SQL92InsertStatement());
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSQLServerInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new SQLServerInsertStatement());
    }
    
    private void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(mockMetaDataMap(), Collections.emptyList(), insertStatement, DefaultSchema.LOGIC_NAME);
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof InsertStatementContext);
    }
    
    @Test
    public void assertSQLStatementContextCreatedWhenSQLStatementNotInstanceOfSelectStatementAndInsertStatement() {
        SQLStatementContext<?> sqlStatementContext = SQLStatementContextFactory.newInstance(mockMetaDataMap(), Collections.emptyList(), mock(MySQLStatement.class), DefaultSchema.LOGIC_NAME);
        assertNotNull(sqlStatementContext);
        assertTrue(sqlStatementContext instanceof CommonSQLStatementContext);
    }
    
    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        return Collections.singletonMap(DefaultSchema.LOGIC_NAME, mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS));
    }
}
