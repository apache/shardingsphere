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

package org.apache.shardingsphere.sharding.rewrite.token.generator.impl;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.binder.context.statement.type.ddl.AlterIndexStatementContext;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.sharding.rewrite.token.pojo.IndexToken;
import org.apache.shardingsphere.sharding.rule.ShardingRule;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexNameSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.ddl.index.IndexSegment;
import org.apache.shardingsphere.sql.parser.statement.core.segment.generic.OwnerSegment;
import org.apache.shardingsphere.sql.parser.statement.core.value.identifier.IdentifierValue;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingIndexTokenGeneratorTest {
    
    private final ShardingIndexTokenGenerator generator = new ShardingIndexTokenGenerator(mock(ShardingRule.class));
    
    @Test
    void assertIsNotGenerateSQLTokenWithNotIndexContextAvailable() {
        assertFalse(generator.isGenerateSQLToken(mock(SQLStatementContext.class)));
    }
    
    @Test
    void assertIsNotGenerateSQLTokenWithEmptyIndex() {
        AlterIndexStatementContext sqlStatementContext = mock(AlterIndexStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getIndexes().isEmpty()).thenReturn(true);
        assertFalse(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertIsGenerateSQLToken() {
        AlterIndexStatementContext sqlStatementContext = mock(AlterIndexStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "PostgreSQL"));
        assertTrue(generator.isGenerateSQLToken(sqlStatementContext));
    }
    
    @Test
    void assertGenerateSQLTokensWithNotIndexContextAvailable() {
        Collection<SQLToken> actual = generator.generateSQLTokens(mock(SQLStatementContext.class));
        assertTrue(actual.isEmpty());
    }
    
    @Test
    void assertGenerateSQLTokensWithSchemaOwner() throws ReflectiveOperationException {
        IndexSegment indexSegment = new IndexSegment(1, 3, new IndexNameSegment(1, 3, mock(IdentifierValue.class)));
        indexSegment.setOwner(new OwnerSegment(0, 0, new IdentifierValue("foo_schema")));
        AlterIndexStatementContext sqlStatementContext = mockAlterIndexStatementContext(indexSegment);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        generator.setSchemas(Collections.singletonMap("foo_schema", schema));
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertTokens(actual, schema);
    }
    
    @Test
    void assertGenerateSQLTokensWithoutSchemaOwner() throws ReflectiveOperationException {
        IndexSegment indexSegment = new IndexSegment(1, 3, new IndexNameSegment(1, 3, mock(IdentifierValue.class)));
        AlterIndexStatementContext sqlStatementContext = mockAlterIndexStatementContext(indexSegment);
        ShardingSphereSchema schema = mock(ShardingSphereSchema.class);
        generator.setDefaultSchema(schema);
        Collection<SQLToken> actual = generator.generateSQLTokens(sqlStatementContext);
        assertTokens(actual, schema);
    }
    
    private AlterIndexStatementContext mockAlterIndexStatementContext(final IndexSegment indexSegment) {
        AlterIndexStatementContext result = mock(AlterIndexStatementContext.class, RETURNS_DEEP_STUBS);
        when(result.getIndexes()).thenReturn(Collections.singleton(indexSegment));
        when(result.getDatabaseType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        when(result.getTablesContext().getSchemaName()).thenReturn(Optional.empty());
        return result;
    }
    
    private void assertTokens(final Collection<SQLToken> actual, final ShardingSphereSchema schema) throws ReflectiveOperationException {
        assertThat(actual.size(), is(1));
        IndexToken actualToken = (IndexToken) new ArrayList<>(actual).get(0);
        assertThat(actualToken.getStartIndex(), is(1));
        assertThat(actualToken.getStopIndex(), is(3));
        assertThat(schema, is((ShardingSphereSchema) Plugins.getMemberAccessor().get(IndexToken.class.getDeclaredField("schema"), actualToken)));
    }
}
