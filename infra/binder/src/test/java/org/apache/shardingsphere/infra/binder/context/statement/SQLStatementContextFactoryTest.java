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

package org.apache.shardingsphere.infra.binder.context.statement;

import org.apache.shardingsphere.infra.binder.context.statement.ddl.CloseStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.FetchStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.ddl.MoveStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
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
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCloseStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.opengauss.ddl.OpenGaussMoveStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.oracle.dml.OracleInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.sql.dialect.statement.sqlserver.dml.SQLServerInsertStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLStatementContextFactoryTest {
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        MySQLSelectStatement selectStatement = new MySQLSelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(selectStatement, Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(SelectStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfMySQLInsertStatement() {
        MySQLInsertStatement insertStatement = new MySQLInsertStatement();
        List<ColumnSegment> columnSegments = new LinkedList<>();
        columnSegments.add(new ColumnSegment(0, 0, new IdentifierValue("IdentifierValue")));
        AssignmentSegment assignment = new ColumnAssignmentSegment(0, 0, columnSegments, null);
        insertStatement.setSetAssignment(new SetAssignmentSegment(0, 0,
                Collections.singleton(assignment)));
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(insertStatement);
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfOracleInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new OracleInsertStatement());
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfPostgreSQLInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new PostgreSQLInsertStatement());
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSQL92InsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new SQL92InsertStatement());
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSQLServerInsertStatement() {
        assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(new SQLServerInsertStatement());
    }
    
    private void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfInsertStatement(final InsertStatement insertStatement) {
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(insertStatement, Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(InsertStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementNotInstanceOfSelectStatementAndInsertStatement() {
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(mock(MySQLStatement.class), Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(CommonSQLStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatement() {
        OpenGaussCursorStatement cursorStatement = mock(OpenGaussCursorStatement.class, RETURNS_DEEP_STUBS);
        MySQLSelectStatement selectStatement = mock(MySQLSelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getProjections().isDistinctRow()).thenReturn(false);
        when(selectStatement.getCommentSegments()).thenReturn(Collections.emptyList());
        when(cursorStatement.getSelect()).thenReturn(selectStatement);
        when(cursorStatement.getCommentSegments()).thenReturn(Collections.emptyList());
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(cursorStatement, Collections.emptyList());
        assertThat(actual, instanceOf(CursorStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCloseStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(mock(OpenGaussCloseStatement.class), Collections.emptyList());
        assertThat(actual, instanceOf(CloseStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForMoveStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(mock(OpenGaussMoveStatement.class), Collections.emptyList());
        assertThat(actual, instanceOf(MoveStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForFetchStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), DefaultDatabase.LOGIC_NAME).bind(mock(OpenGaussFetchStatement.class), Collections.emptyList());
        assertThat(actual, instanceOf(FetchStatementContext.class));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        Map<String, ShardingSphereDatabase> databases = Collections.singletonMap(DefaultDatabase.LOGIC_NAME, mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS));
        return new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class), mock(ShardingSphereRuleMetaData.class), mock(ConfigurationProperties.class));
    }
}
