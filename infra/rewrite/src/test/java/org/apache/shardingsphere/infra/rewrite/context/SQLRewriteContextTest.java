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

package org.apache.shardingsphere.infra.rewrite.context;

import org.apache.shardingsphere.infra.binder.context.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.context.type.TableAvailable;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.sql.parser.statement.mysql.dml.MySQLInsertStatement;
import org.apache.shardingsphere.sql.parser.statement.postgresql.dml.PostgreSQLInsertStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SQLRewriteContextTest {
    
    @Mock
    private CommonSQLStatementContext sqlStatementContext;
    
    @Mock
    private SQLToken sqlToken;
    
    @Mock
    private OptionalSQLTokenGenerator optionalSQLTokenGenerator;
    
    @Mock
    private CollectionSQLTokenGenerator collectionSQLTokenGenerator;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Collections.singleton(sqlToken));
        when(database.getName()).thenReturn("foo_db");
        when(database.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("test")));
    }
    
    @Test
    void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        QueryContext queryContext = createMockQueryContext(statementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertInsertStatementContextWithMySQLInsertStatement() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        MySQLInsertStatement mySQLInsertStatement = mock(MySQLInsertStatement.class);
        when(statementContext.getSqlStatement()).thenReturn(mySQLInsertStatement);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        QueryContext queryContext = createMockQueryContext(statementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertInsertStatementContextWithPostgresSQLInsertStatement() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        PostgreSQLInsertStatement postgreSQLInsertStatement = mock(PostgreSQLInsertStatement.class);
        when(statementContext.getSqlStatement()).thenReturn(postgreSQLInsertStatement);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        QueryContext queryContext = createMockQueryContext(statementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        QueryContext queryContext = createMockQueryContext(statementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(StandardParameterBuilder.class));
    }
    
    @Test
    void assertGenerateOptionalSQLToken() {
        QueryContext queryContext = createMockQueryContext(sqlStatementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
    
    @Test
    void assertGenerateCollectionSQLToken() {
        QueryContext queryContext = createMockQueryContext(sqlStatementContext, Collections.singletonList(1));
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
    
    private QueryContext createMockQueryContext(final SQLStatementContext statementContext, final List<Object> parameters) {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(parameters);
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        return queryContext;
    }
}
