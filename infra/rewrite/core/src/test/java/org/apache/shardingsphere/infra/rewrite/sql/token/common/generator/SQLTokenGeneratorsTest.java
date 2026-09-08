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

package org.apache.shardingsphere.infra.rewrite.sql.token.common.generator;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.generator.aware.SchemaMetaDataAware;
import org.apache.shardingsphere.infra.rewrite.sql.token.common.pojo.SQLToken;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

class SQLTokenGeneratorsTest {
    
    @Test
    @SuppressWarnings("unchecked")
    void assertGenerateSQLTokensWithOptionalSQLTokenGenerator() {
        OptionalSQLTokenGenerator<SQLStatementContext> generator = mock(OptionalSQLTokenGenerator.class, withSettings().extraInterfaces(SchemaMetaDataAware.class));
        SQLToken expectedToken = mock(SQLToken.class);
        when(generator.generateSQLToken(any(SQLStatementContext.class))).thenReturn(expectedToken);
        SQLTokenGenerators generators = new SQLTokenGenerators();
        generators.addAll(Collections.singleton(generator));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        ShardingSphereSchema defaultSchema = new ShardingSphereSchema("test", mock(DatabaseType.class));
        when(database.getAllSchemas()).thenReturn(Collections.singleton(defaultSchema));
        when(database.findDefaultSchema()).thenReturn(Optional.of(defaultSchema));
        Collection<SQLToken> actualSqlTokens = generators.generateSQLTokens(database, mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), mock(ConnectionContext.class));
        assertThat(actualSqlTokens.size(), is(1));
        assertThat(actualSqlTokens.iterator().next(), is(expectedToken));
        verify((SchemaMetaDataAware) generator).setSchemas(Collections.singletonMap("test", defaultSchema));
        verify((SchemaMetaDataAware) generator).setDefaultSchema(defaultSchema);
    }
    
    @Test
    @SuppressWarnings("unchecked")
    void assertGenerateSQLTokensWithCollectionSQLTokenGenerator() {
        CollectionSQLTokenGenerator<SQLStatementContext> generator = mock(CollectionSQLTokenGenerator.class);
        Collection<SQLToken> expectedTokens = Arrays.asList(mock(SQLToken.class), mock(SQLToken.class));
        doReturn(expectedTokens).when(generator).generateSQLTokens(any());
        SQLTokenGenerators generators = new SQLTokenGenerators();
        generators.addAll(Collections.singleton(generator));
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("sharding_db");
        Collection<SQLToken> actualSQLTokens = generators.generateSQLTokens(database, mock(SQLStatementContext.class, RETURNS_DEEP_STUBS), Collections.emptyList(), mock(ConnectionContext.class));
        assertThat(actualSQLTokens.size(), is(2));
        assertThat(actualSQLTokens, is(expectedTokens));
    }
}
