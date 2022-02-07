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

import org.junit.Test;
import org.junit.runner.RunWith;

import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.pojo.SQLToken;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
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
        Collection<SQLTokenGenerator> collection = new ArrayList<>();
        collection.add(expectedSqlTokenGenerator);
        collection.add(unexpectedSqlTokenGenerator);
        sqlTokenGenerators.addAll(collection);
        Map<Class<?>, SQLTokenGenerator> actualSqlTokenGeneratorsMap = getSqlTokenGeneratorsMap(sqlTokenGenerators);
        assertThat(actualSqlTokenGeneratorsMap.size(), is(1));
        SQLTokenGenerator actualSqlTokenGenerator = actualSqlTokenGeneratorsMap.get(expectedSqlTokenGenerator.getClass());
        assertThat(actualSqlTokenGenerator, is(expectedSqlTokenGenerator));
    }

    @Test
    public void assertAddAllWithEmptyList() throws Exception {
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(Collections.emptyList());
        Map<Class<?>, SQLTokenGenerator> actualSqlTokenGeneratorsMap = getSqlTokenGeneratorsMap(sqlTokenGenerators);
        assertNotNull(actualSqlTokenGeneratorsMap);
        assertTrue(actualSqlTokenGeneratorsMap.isEmpty());
    }

    @Test
    @SuppressWarnings("unchecked")
    public void assertGenerateSQLTokensWithOptionalSQLTokenGenerator() {
        OptionalSQLTokenGenerator<SQLStatementContext> optionalSQLTokenGenerator = mock(OptionalSQLTokenGenerator.class);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(Collections.singleton(optionalSQLTokenGenerator));
        SQLToken expectedToken = mock(SQLToken.class);
        when(optionalSQLTokenGenerator.generateSQLToken(any(SQLStatementContext.class))).thenReturn(expectedToken);
        Collection<SQLToken> actualSqlTokens = sqlTokenGenerators.generateSQLTokens(mock(SQLStatementContext.class), Collections.emptyList(), mock(ShardingSphereSchema.class));
        assertNotNull(actualSqlTokens);
        assertThat(actualSqlTokens.size(), is(1));
        assertThat(actualSqlTokens.iterator().next(), is(expectedToken));
    }

    @Test
    @SuppressWarnings("unchecked")
    public void assertGenerateSQLTokensWithCollectionSQLTokenGenerator() {
        CollectionSQLTokenGenerator<SQLStatementContext> collectionSQLTokenGenerator = mock(CollectionSQLTokenGenerator.class);
        SQLTokenGenerators sqlTokenGenerators = new SQLTokenGenerators();
        sqlTokenGenerators.addAll(Collections.singleton(collectionSQLTokenGenerator));
        List<SQLToken> expectedCollection = Arrays.asList(mock(SQLToken.class), mock(SQLToken.class));
        doReturn(expectedCollection).when(collectionSQLTokenGenerator).generateSQLTokens(any());
        List<SQLToken> actualSqlTokens = sqlTokenGenerators.generateSQLTokens(mock(SQLStatementContext.class), Collections.emptyList(), mock(ShardingSphereSchema.class));
        assertNotNull(actualSqlTokens);
        assertThat(actualSqlTokens.size(), is(2));
        assertThat(actualSqlTokens, is(expectedCollection));
    }

    private Map<Class<?>, SQLTokenGenerator> getSqlTokenGeneratorsMap(final SQLTokenGenerators sqlTokenGenerators) throws NoSuchFieldException, IllegalAccessException {
        Field field = sqlTokenGenerators.getClass().getDeclaredField("sqlTokenGenerators");
        field.setAccessible(true);
        return (Map<Class<?>, SQLTokenGenerator>) field.get(sqlTokenGenerators);
    }
}
