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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin;

import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.PostgreSQLAdminExecutorCreator;
import org.apache.shardingsphere.sql.parser.sql.common.statement.dml.SelectStatement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.instanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OpenGaussAdminExecutorFactoryTest {
    
    @Mock
    private PostgreSQLAdminExecutorCreator postgreSQLAdminExecutorFactory;
    
    private OpenGaussAdminExecutorCreator openGaussAdminExecutorFactory;
    
    @BeforeEach
    void setup() throws ReflectiveOperationException {
        openGaussAdminExecutorFactory = new OpenGaussAdminExecutorCreator();
        Plugins.getMemberAccessor().set(OpenGaussAdminExecutorCreator.class.getDeclaredField("delegated"), openGaussAdminExecutorFactory, postgreSQLAdminExecutorFactory);
    }
    
    @Test
    void assertNewInstanceWithSQLStatementContextOnly() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class);
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(postgreSQLAdminExecutorFactory.create(sqlStatementContext)).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.create(sqlStatementContext);
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
    }
    
    @Test
    void assertNewInstanceWithSelectDatabase() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        SelectStatement statement = mock(SelectStatement.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getSqlStatement()).thenReturn(statement);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.singletonList("pg_database"));
        String sql = "select datcompatibility from pg_database where datname = 'sharding_db'";
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.create(sqlStatementContext, sql, "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), instanceOf(OpenGaussSystemCatalogAdminQueryExecutor.class));
    }
    
    @Test
    void assertNewInstanceWithOtherSQL() {
        SQLStatementContext sqlStatementContext = mock(SQLStatementContext.class, RETURNS_DEEP_STUBS);
        when(sqlStatementContext.getTablesContext().getTableNames()).thenReturn(Collections.emptyList());
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        when(postgreSQLAdminExecutorFactory.create(sqlStatementContext, "", "", Collections.emptyList())).thenReturn(Optional.of(expected));
        Optional<DatabaseAdminExecutor> actual = openGaussAdminExecutorFactory.create(sqlStatementContext, "", "", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), is(expected));
    }
}
