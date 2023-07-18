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

import org.apache.shardingsphere.infra.binder.statement.CommonSQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;

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
    
    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Collections.singleton(sqlToken));
    }
    
    @Test
    void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)),
                statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class), hintValueContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)),
                statementContext, "SELECT * FROM tbl WHERE id = ?", Collections.singletonList(1), mock(ConnectionContext.class), hintValueContext);
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(StandardParameterBuilder.class));
    }
    
    @Test
    void assertGenerateOptionalSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)),
                sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class), hintValueContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
    
    @Test
    void assertGenerateCollectionSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME, Collections.singletonMap("test", mock(ShardingSphereSchema.class)),
                sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class), hintValueContext);
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
}
