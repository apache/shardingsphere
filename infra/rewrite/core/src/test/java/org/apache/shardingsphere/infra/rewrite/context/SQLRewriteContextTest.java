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

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sql.parser.statement.core.statement.SQLStatement;
import org.apache.shardingsphere.sql.parser.statement.core.statement.attribute.SQLStatementAttributes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SQLRewriteContextTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private SQLToken sqlToken;
    
    @SuppressWarnings("rawtypes")
    @Mock
    private OptionalSQLTokenGenerator optionalSQLTokenGenerator;
    
    @SuppressWarnings("rawtypes")
    @Mock
    private CollectionSQLTokenGenerator collectionSQLTokenGenerator;
    
    @Mock
    private HintValueContext hintValueContext;
    
    @Mock
    private ShardingSphereDatabase database;
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(sqlStatementContext.getTablesContext().getDatabaseNames()).thenReturn(Collections.emptyList());
        SQLStatement sqlStatement = mock(SQLStatement.class);
        when(sqlStatement.getDatabaseType()).thenReturn(databaseType);
        when(sqlStatement.getAttributes()).thenReturn(new SQLStatementAttributes());
        when(sqlStatementContext.getSqlStatement()).thenReturn(sqlStatement);
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Collections.singleton(sqlToken));
        when(database.getName()).thenReturn("foo_db");
        when(database.getAllSchemas()).thenReturn(Collections.singleton(new ShardingSphereSchema("test", mock(DatabaseType.class))));
    }
    
    @Test
    void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        when(statementContext.getGroupedParameters()).thenReturn(Collections.emptyList());
        when(statementContext.getOnDuplicateKeyUpdateParameters()).thenReturn(Collections.emptyList());
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), isA(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(statementContext.getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(statementContext);
        when(queryContext.getSql()).thenReturn("SELECT * FROM tbl WHERE id = ?");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), isA(StandardParameterBuilder.class));
    }
    
    @Test
    void assertGenerateOptionalSQLToken() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), isA(SQLToken.class));
    }
    
    @Test
    void assertGenerateCollectionSQLToken() {
        QueryContext queryContext = mock(QueryContext.class, RETURNS_DEEP_STUBS);
        when(queryContext.getSqlStatementContext()).thenReturn(sqlStatementContext);
        when(queryContext.getSql()).thenReturn("INSERT INTO tbl VALUES (?)");
        when(queryContext.getParameters()).thenReturn(Collections.singletonList(1));
        when(queryContext.getHintValueContext()).thenReturn(hintValueContext);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(database, queryContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), isA(SQLToken.class));
    }
}
