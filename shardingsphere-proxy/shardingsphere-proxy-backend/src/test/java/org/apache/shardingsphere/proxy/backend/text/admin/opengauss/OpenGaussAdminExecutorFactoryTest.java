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

package org.apache.shardingsphere.proxy.backend.text.admin.opengauss;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.text.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.text.admin.postgresql.PostgreSQLAdminExecutorFactory;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class OpenGaussAdminExecutorFactoryTest {
    
    @Mock
    private PostgreSQLAdminExecutorFactory postgreSQLAdminExecutorFactory;
    
    private OpenGaussAdminExecutorFactory openGaussAdminExecutorFactory;
    
    @Before
    @SneakyThrows
    public void setup() {
        Field field = OpenGaussAdminExecutorFactory.class.getDeclaredField("delegated");
        field.setAccessible(true);
        openGaussAdminExecutorFactory = new OpenGaussAdminExecutorFactory();
        field.set(openGaussAdminExecutorFactory, postgreSQLAdminExecutorFactory);
    }
    
    @Test
    public void assertNewInstanceWithSQLStatementContextOnly() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class);
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(postgreSQLAdminExecutorFactory.newInstance(sqlStatementContext)).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.newInstance(sqlStatementContext);
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    public void assertNewInstanceWithSelectDatabase() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("pg_database"));
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.newInstance(sqlStatementContext, "select datcompatibility from pg_database where datname = 'sharding_db'", "");
        assertTrue(actual.get() instanceof OpenGaussSelectDatabaseExecutor);
    }
    
    @Test
    public void assertNewInstanceWithOtherSQL() {
        SQLStatementContext<?> sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(postgreSQLAdminExecutorFactory.newInstance(sqlStatementContext, "", "")).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.newInstance(sqlStatementContext, "", "");
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    public void assertGetType() {
        assertThat(openGaussAdminExecutorFactory.getType(), is("openGauss"));
    }
}
