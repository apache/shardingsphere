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

package org.apache.shardingsphere.core.optimizer;

import com.google.common.base.Optional;
import lombok.SneakyThrows;
import org.apache.shardingsphere.core.parsing.parser.context.condition.Column;
import org.apache.shardingsphere.core.parsing.parser.context.condition.GeneratedKeyCondition;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValue;
import org.apache.shardingsphere.core.parsing.parser.context.insertvalue.InsertValues;
import org.apache.shardingsphere.core.parsing.parser.context.table.Tables;
import org.apache.shardingsphere.core.parsing.parser.sql.dml.insert.InsertStatement;
import org.apache.shardingsphere.core.rule.ShardingRule;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class GeneratedKeyTest {
    
    @Mock
    private ShardingRule shardingRule;
    
    @Mock
    private InsertStatement insertStatement;
    
    @Before
    public void setUp() {
        Tables tables = mock(Tables.class);
        when(insertStatement.getTables()).thenReturn(tables);
    }
    
    @Test
    public void assertGetGenerateKeyWhenCreateWithoutGenerateKeyColumnConfiguration() {
        mockGetGenerateKeyWhenCreate();
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.<String>absent());
        assertFalse(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement).isPresent());
    }
    
    @Test
    public void assertGetGenerateKeyWhenCreateWithGenerateKeyColumnConfiguration() {
        mockGetGenerateKeyWhenCreate();
        when(shardingRule.findGenerateKeyColumnName("tbl")).thenReturn(Optional.of("id"));
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedKeys().size(), is(1));
    }
    
    private void mockGetGenerateKeyWhenCreate() {
        Tables tables = mock(Tables.class);
        when(insertStatement.getTables()).thenReturn(tables);
        when(tables.getSingleTableName()).thenReturn("tbl");
        InsertValues insertValues = new InsertValues();
        insertValues.getInsertValues().add(mock(InsertValue.class));
        when(insertStatement.getInsertValues()).thenReturn(insertValues);
    }
    
    @Test
    public void assertGetGenerateKeyWhenFind() {
        mockGetGenerateKeyWhenFind();
        assertTrue(GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement).isPresent());
    }
    
    @SuppressWarnings("unchecked")
    @SneakyThrows
    private void mockGetGenerateKeyWhenFind() {
        GeneratedKeyCondition generatedKeyCondition = mock(GeneratedKeyCondition.class);
        when(generatedKeyCondition.getIndex()).thenReturn(-1);
        when(generatedKeyCondition.getValue()).thenReturn((Comparable) 100);
        when(generatedKeyCondition.getColumn()).thenReturn(new Column("id", "tbl"));
        when(insertStatement.getGeneratedKeyConditions()).thenReturn(Arrays.asList(generatedKeyCondition, mock(GeneratedKeyCondition.class)));
        when(insertStatement.isContainGenerateKeyColumn(shardingRule)).thenReturn(true);
        Field field = InsertStatement.class.getDeclaredField("columns");
        field.setAccessible(true);
        Optional<GeneratedKey> actual = GeneratedKey.getGenerateKey(shardingRule, Collections.<Object>singletonList(1), insertStatement);
        assertTrue(actual.isPresent());
        assertThat(actual.get().getGeneratedKeys().size(), is(2));
        assertThat(actual.get().getGeneratedKeys().get(0), is((Comparable) 100));
        assertThat(actual.get().getGeneratedKeys().get(1), is((Comparable) 1));
    }
}
