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

package org.apache.shardingsphere.infra.rewrite.sql.token.generator;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.context.ConnectionContext;
import org.apache.shardingsphere.infra.metadata.database.schema.decorator.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class SQLTokenGeneratorsTest {
    
    @Test
    public void assertAddAllWithList() throws Exception {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        Map<Class<?>, SQLTokenGenerator> actualSqlTokenGeneratorsMap = getSqlTokenGeneratorsMap(sqlTokenGenerators);
        SQLTokenGenerator mockSqlTokenGenerator = mock(SQLTokenGenerator.class);
        sqlTokenGenerators.addAll(Collections.singleton(mockSqlTokenGenerator));
        assertThat(actualSqlTokenGeneratorsMap.size(), is(1));
        assertTrue(actualSqlTokenGeneratorsMap.containsKey(mockSqlTokenGenerator.getClass()));
        assertThat(actualSqlTokenGeneratorsMap.get(mockSqlTokenGenerator.getClass()), is(mockSqlTokenGenerator));
    }
    
    @Test
    public void assertAddAllWithSameClass() throws Exception {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        SQLTokenGenerator expectedSqlTokenGenerator = mock(SQLTokenGenerator.class);
        SQLTokenGenerator unexpectedSqlTokenGenerator = mock(SQLTokenGenerator.class);
        Collection<SQLTokenGenerator> collection = new LinkedList<>();
        collection.add(expectedSqlTokenGenerator);
        collection.add(unexpectedSqlTokenGenerator);
        sqlTokenGenerators.addAll(collection);
        Map<Class<?>, SQLTokenGenerator> actualSqlTokenGeneratorsMap = getSqlTokenGeneratorsMap(sqlTokenGenerators);
        assertThat(actualSqlTokenGeneratorsMap.size(), is(1));
        SQLTokenGenerator actualSqlTokenGenerator = actualSqlTokenGeneratorsMap.get(expectedSqlTokenGenerator.getClass());
        assertThat(actualSqlTokenGenerator, is(expectedSqlTokenGenerator));
    }
    
    @Test
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void assertGenerateSQLTokensWithOptionalSQLTokenGenerator() {
        OptionalSQLTokenGenerator<SQLStatementContext> optionalSQLTokenGenerator = mock(OptionalSQLTokenGenerator.class);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(Collections.singleton(optionalSQLTokenGenerator));
        SQLToken expectedToken = mock(SQLToken.class);
        when(optionalSQLTokenGenerator.generateSQLToken(any(SQLStatementContext.class))).thenReturn(expectedToken);
        Collection<SQLToken> actualSqlTokens = sqlTokenGenerators.generateSQLTokens(
                "sharding_db", Collections.singletonMap("test", mock(ShardingSphereSchema.class)), mock(SQLStatementContext.class), Collections.emptyList(), mock(ConnectionContext.class));
        assertThat(actualSqlTokens.size(), is(1));
        assertThat(actualSqlTokens.iterator().next(), is(expectedToken));
    }
    
    @Test
    @SuppressWarnings("unchecked")
    public void assertGenerateSQLTokensWithCollectionSQLTokenGenerator() {
        CollectionSQLTokenGenerator<SQLStatementContext<?>> collectionSQLTokenGenerator = mock(CollectionSQLTokenGenerator.class);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(Collections.singleton(collectionSQLTokenGenerator));
        Collection<SQLToken> expectedSQLTokens = Arrays.asList(mock(SQLToken.class), mock(SQLToken.class));
        doReturn(expectedSQLTokens).when(collectionSQLTokenGenerator).generateSQLTokens(any());
        Collection<SQLToken> actualSQLTokens = sqlTokenGenerators.generateSQLTokens(
                "sharding_db", Collections.singletonMap("test", mock(ShardingSphereSchema.class)), mock(SQLStatementContext.class), Collections.emptyList(), mock(ConnectionContext.class));
        assertThat(actualSQLTokens.size(), is(2));
        assertThat(actualSQLTokens, is(expectedSQLTokens));
    }
    
    @SuppressWarnings("unchecked")
    private Map<Class<?>, SQLTokenGenerator> getSqlTokenGeneratorsMap(final SQLTokenGenerators sqlTokenGenerators) throws NoSuchFieldException, IllegalAccessException {
        Field field = sqlTokenGenerators.getClass().getDeclaredField("sqlTokenGenerators");
        field.setAccessible(true);
        return (Map<Class<?>, SQLTokenGenerator>) field.get(sqlTokenGenerators);
    }
}
