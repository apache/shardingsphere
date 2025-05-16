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
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.ddl.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.apache.shardingsphere.sql.parser.statement.opengauss.ddl.OpenGaussCloseStatement;
import org.apache.shardingsphere.sql.parser.statement.opengauss.ddl.OpenGaussCursorStatement;
import org.apache.shardingsphere.sql.parser.statement.opengauss.ddl.OpenGaussFetchStatement;
import org.apache.shardingsphere.sql.parser.statement.opengauss.ddl.OpenGaussMoveStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.sql92.dml.SQL92SelectStatement;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLStatementContextFactoryTest {
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SQL92SelectStatement();
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(selectStatement, Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(SelectStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstance() {
        InsertStatement insertStatement = new SQL92InsertStatement();
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(insertStatement, Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(InsertStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementNotInstanceOfSelectStatementAndInsertStatement() {
        AlterDatabaseStatement alterDatabaseStatement = mock(AlterDatabaseStatement.class);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(alterDatabaseStatement, Collections.emptyList());
        assertThat(sqlStatementContext, instanceOf(CommonSQLStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatement() {
        OpenGaussCursorStatement cursorStatement = mock(OpenGaussCursorStatement.class, RETURNS_DEEP_STUBS);
        SelectStatement selectStatement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(selectStatement.getProjections().isDistinctRow()).thenReturn(false);
        when(selectStatement.getProjections().getProjections()).thenReturn(Collections.emptyList());
        when(selectStatement.getCommentSegments()).thenReturn(Collections.emptyList());
        when(cursorStatement.getSelect()).thenReturn(selectStatement);
        when(cursorStatement.getCommentSegments()).thenReturn(Collections.emptyList());
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(cursorStatement, Collections.emptyList());
        assertThat(actual, instanceOf(CursorStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCloseStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new OpenGaussCloseStatement(), Collections.emptyList());
        assertThat(actual, instanceOf(CloseStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForMoveStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new OpenGaussMoveStatement(), Collections.emptyList());
        assertThat(actual, instanceOf(MoveStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForFetchStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new OpenGaussFetchStatement(), Collections.emptyList());
        assertThat(actual, instanceOf(FetchStatementContext.class));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getName()).thenReturn("foo_db");
        when(database.containsSchema("foo_db")).thenReturn(true);
        when(database.containsSchema("public")).thenReturn(true);
        when(database.getSchema("foo_db").containsTable("tbl")).thenReturn(true);
        when(database.getSchema("public").containsTable("tbl")).thenReturn(true);
        when(database.containsSchema("dbo")).thenReturn(true);
        when(database.getSchema("dbo").containsTable("tbl")).thenReturn(true);
        return new ShardingSphereMetaData(Collections.singleton(database), mock(ResourceMetaData.class), mock(RuleMetaData.class), mock(ConfigurationProperties.class));
    }
}
