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

import com.google.common.collect.Lists;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.GroupedParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.parameter.builder.impl.StandardParameterBuilder;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.CollectionSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.OptionalSQLTokenGenerator;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.apache.shardingsphere.infra.metadata.model.schema.model.schema.SchemaMetaData;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.InsertStatementContext;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SQLRewriteContextTest {
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Mock
    private SchemaMetaData schemaMetaData;
    
    @Mock
    private SQLToken sqlToken;
    
    @Mock
    private OptionalSQLTokenGenerator optionalSQLTokenGenerator;
    
    @Mock
    private CollectionSQLTokenGenerator collectionSQLTokenGenerator;
    
    @Before
    public void setUp() {
        when(optionalSQLTokenGenerator.generateSQLToken(sqlStatementContext)).thenReturn(sqlToken);
        when(optionalSQLTokenGenerator.isGenerateSQLToken(sqlStatementContext)).thenReturn(true);
        when(collectionSQLTokenGenerator.generateSQLTokens(sqlStatementContext)).thenReturn(Lists.newArrayList(sqlToken));
        when(collectionSQLTokenGenerator.isGenerateSQLToken(sqlStatementContext)).thenReturn(true);
    }
    
    @Test
    public void assertInsertStatementContext() {
        InsertStatementContext statementContext = mock(InsertStatementContext.class);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(GroupedParameterBuilder.class));
    }
    
    @Test
    public void assertNotInsertStatementContext() {
        SelectStatementContext statementContext = mock(SelectStatementContext.class);
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(mock(SchemaMetaData.class), statementContext, "SELECT * FROM tbl WHERE id = ?", Collections.singletonList(1));
        assertThat(sqlRewriteContext.getParameterBuilder(), instanceOf(StandardParameterBuilder.class));
    }
    
    @Test
    public void assertGenerateOptionalSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(schemaMetaData, sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        sqlRewriteContext.addSQLTokenGenerators(Lists.newArrayList(optionalSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
    
    @Test
    public void assertGenerateCollectionSQLToken() {
        SQLRewriteContext sqlRewriteContext = new SQLRewriteContext(schemaMetaData, sqlStatementContext, "INSERT INTO tbl VALUES (?)", Collections.singletonList(1));
        sqlRewriteContext.addSQLTokenGenerators(Lists.newArrayList(collectionSQLTokenGenerator));
        sqlRewriteContext.generateSQLTokens();
        assertFalse(sqlRewriteContext.getSqlTokens().isEmpty());
        assertThat(sqlRewriteContext.getSqlTokens().get(0), instanceOf(SQLToken.class));
    }
}
