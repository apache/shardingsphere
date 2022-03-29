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

package org.apache.shardingsphere.encrypt.rewrite.token;

import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptOrderByItemTokenGenerator;
import org.apache.shardingsphere.encrypt.rewrite.token.generator.EncryptProjectionTokenGenerator;
import org.apache.shardingsphere.encrypt.rule.EncryptRule;
import org.apache.shardingsphere.encrypt.rule.aware.EncryptRuleAware;
import org.apache.shardingsphere.infra.binder.segment.select.orderby.OrderByItem;
import org.apache.shardingsphere.infra.binder.statement.dml.SelectStatementContext;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.rewrite.sql.token.generator.SQLTokenGenerator;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class EncryptTokenGenerateBuilderTest {
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private EncryptRule encryptRule;
    
    @Before
    public void setup() {
        when(encryptRule.findEncryptTable(anyString()).isPresent()).thenReturn(true);
    }
    
    @Test
    public void assertGetSQLTokenGenerators() throws IllegalAccessException {
        SelectStatementContext selectStatementContext = mock(SelectStatementContext.class, RETURNS_DEEP_STUBS);
        when(selectStatementContext.getAllTables().isEmpty()).thenReturn(false);
        when(selectStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("table"));
        when(selectStatementContext.getOrderByContext().getItems()).thenReturn(Collections.singletonList(mock(OrderByItem.class)));
        when(selectStatementContext.getGroupByContext().getItems()).thenReturn(Collections.emptyList());
        when(selectStatementContext.getWhereSegments()).thenReturn(Collections.emptyList());
        EncryptTokenGenerateBuilder encryptTokenGenerateBuilder = new EncryptTokenGenerateBuilder(
                encryptRule, selectStatementContext, Collections.emptyList(), DefaultSchema.LOGIC_NAME);
        Collection<SQLTokenGenerator> sqlTokenGenerators = encryptTokenGenerateBuilder.getSQLTokenGenerators();
        assertThat(sqlTokenGenerators.size(), is(2));
        Iterator<SQLTokenGenerator> iterator = sqlTokenGenerators.iterator();
        SQLTokenGenerator item1 = iterator.next();
        assertThat(item1, instanceOf(EncryptProjectionTokenGenerator.class));
        assertSqlTokenGenerator(item1);
        SQLTokenGenerator item2 = iterator.next();
        assertThat(item2, instanceOf(EncryptOrderByItemTokenGenerator.class));
        assertSqlTokenGenerator(item2);
    }
    
    private void assertSqlTokenGenerator(final SQLTokenGenerator sqlTokenGenerator) throws IllegalAccessException {
        if (sqlTokenGenerator instanceof EncryptRuleAware) {
            assertField(sqlTokenGenerator, encryptRule, "encryptRule");
        }
    }
    
    private void assertField(final SQLTokenGenerator sqlTokenGenerator, final Object filedInstance, final String fieldName) throws IllegalAccessException {
        Field field = findField(sqlTokenGenerator.getClass(), fieldName, filedInstance.getClass());
        field.setAccessible(true);
        assertNotNull(field.get(sqlTokenGenerator));
        assertThat(field.get(sqlTokenGenerator), is(filedInstance));
    }
    
    private Field findField(final Class<?> clazz, final String fieldName, final Class<?> fieldType) {
        Class<?> searchClass = clazz;
        while (null != searchClass && !Object.class.equals(searchClass)) {
            for (final Field each : searchClass.getDeclaredFields()) {
                if (fieldName.equals(each.getName()) && fieldType.equals(each.getType())) {
                    return each;
                }
            }
            searchClass = searchClass.getSuperclass();
        }
        throw new IllegalStateException("No such field in class");
    }
}
