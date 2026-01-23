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

package org.apache.shardingsphere.infra.binder.context;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorHeldSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.CursorStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.engine.SQLBindEngine;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.item.ProjectionsSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.dml.pagination.limit.LimitSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.SimpleTableSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.table.TableNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CloseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.CursorStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.FetchStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.MoveStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.ddl.database.AlterDatabaseStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.InsertStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SQLStatementContextFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstanceOfSelectStatement() {
        ProjectionsSegment projectionsSegment = new ProjectionsSegment(0, 0);
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setLimit(new LimitSegment(0, 10, null, null));
        selectStatement.setProjections(projectionsSegment);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(selectStatement);
        assertThat(sqlStatementContext, isA(SelectStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementInstance() {
        InsertStatement insertStatement = new InsertStatement(databaseType);
        insertStatement.setTable(new SimpleTableSegment(new TableNameSegment(0, 0, new IdentifierValue("tbl"))));
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(insertStatement);
        assertThat(sqlStatementContext, isA(InsertStatementContext.class));
    }
    
    @Test
    void assertSQLStatementContextCreatedWhenSQLStatementNotInstanceOfSelectStatementAndInsertStatement() {
        AlterDatabaseStatement alterDatabaseStatement = new AlterDatabaseStatement(databaseType);
        SQLStatementContext sqlStatementContext = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(alterDatabaseStatement);
        assertThat(sqlStatementContext, isA(CommonSQLStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCursorStatement() {
        SelectStatement selectStatement = new SelectStatement(databaseType);
        selectStatement.setProjections(new ProjectionsSegment(0, 0));
        CursorStatement cursorStatement = new CursorStatement(databaseType, null, selectStatement);
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(cursorStatement);
        assertThat(actual, isA(CursorStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForCloseStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new CloseStatement(databaseType, null, false));
        assertThat(actual, isA(CursorHeldSQLStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForMoveStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new MoveStatement(databaseType, null, null));
        assertThat(actual, isA(CursorHeldSQLStatementContext.class));
    }
    
    @Test
    void assertNewInstanceForFetchStatement() {
        SQLStatementContext actual = new SQLBindEngine(mockMetaData(), "foo_db", new HintValueContext()).bind(new FetchStatement(databaseType, null, null));
        assertThat(actual, isA(CursorHeldSQLStatementContext.class));
    }
    
    private ShardingSphereMetaData mockMetaData() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
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
