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

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.binder.type.TableAvailable;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRewriteContextTest {
    
    @Mock
    private SQLStatementContext<?> sqlStatementContext;
    
    @Mock
    private SQLToken sqlToken;
    
    @Mock
    private OptionalSQLTokenGenerator optionalSQLTokenGenerator;
    
    @Mock
    private CollectionSQLTokenGenerator collectionSQLTokenGenerator;
    
    @SuppressWarnings("unchecked")
    @Before
    public void setUp() {
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Collections.singleton(sqlToken));
    }
    
    @Test
    public void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        when(statementContext.getInsertSelectContext()).thenReturn(null);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class));
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    public void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(((TableAvailable) statementContext).getTablesContext().getDatabaseName().isPresent()).thenReturn(false);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), statementContext, "SELECT * FROM tbl WHERE id = ?", Collections.singletonList(1), mock(ConnectionContext.class));
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(StandardParameterBuilder.class));
    }
    
    @Test
    public void assertGenerateOptionalSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class));
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
    
    @Test
    public void assertGenerateCollectionSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(DefaultDatabase.LOGIC_NAME,
                Collections.singletonMap("test", mock(ShardingSphereSchema.class)), sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1), mock(ConnectionContext.class));
        sqlRewriteContext.addSQLTokenGenerators(Collections.singleton(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
}
