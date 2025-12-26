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

package org.apache.shardingsphere.proxy.backend.opengauss.handler.admin.factory;

import org.apache.shardingsphere.infra.binder.context.statement.type.dml.SelectStatementContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutor;
import org.apache.shardingsphere.proxy.backend.postgresql.handler.admin.factory.PostgreSQLSelectAdminExecutorFactory;
import org.apache.shardingsphere.sql.parser.statement.core.statement.type.dml.SelectStatement;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class OpenGaussSelectAdminExecutorFactoryTest {
    
    @Test
    void assertNewInstanceWithReturnSystemTableExecutor() {
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        try (
                MockedStatic<OpenGaussSystemTableQueryExecutorFactory> tableFactory = mockStatic(OpenGaussSystemTableQueryExecutorFactory.class);
                MockedStatic<OpenGaussSystemFunctionQueryExecutorFactory> functionFactory = mockStatic(OpenGaussSystemFunctionQueryExecutorFactory.class)) {
            tableFactory.when(() -> OpenGaussSystemTableQueryExecutorFactory.newInstance(any(), any(), any())).thenReturn(Optional.of(expected));
            functionFactory.when(() -> OpenGaussSystemFunctionQueryExecutorFactory.newInstance(any())).thenReturn(Optional.empty());
            Optional<DatabaseAdminExecutor> actual = OpenGaussSelectAdminExecutorFactory.newInstance(mockSelectStatementContext(), "sql", Collections.emptyList());
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is(expected));
        }
    }
    
    @Test
    void assertNewInstanceWithReturnSystemFunctionExecutor() {
        DatabaseAdminExecutor expected = mock(DatabaseAdminExecutor.class);
        try (
                MockedStatic<OpenGaussSystemTableQueryExecutorFactory> tableFactory = mockStatic(OpenGaussSystemTableQueryExecutorFactory.class);
                MockedStatic<OpenGaussSystemFunctionQueryExecutorFactory> functionFactory = mockStatic(OpenGaussSystemFunctionQueryExecutorFactory.class)) {
            tableFactory.when(() -> OpenGaussSystemTableQueryExecutorFactory.newInstance(any(), any(), any())).thenReturn(Optional.empty());
            functionFactory.when(() -> OpenGaussSystemFunctionQueryExecutorFactory.newInstance(any())).thenReturn(Optional.of(expected));
            Optional<DatabaseAdminExecutor> actual = OpenGaussSelectAdminExecutorFactory.newInstance(mockSelectStatementContext(), "sql", Collections.emptyList());
            assertTrue(actual.isPresent());
            assertThat(actual.get(), is(expected));
        }
    }
    
    @Test
    void assertNewInstanceWhenAllFactoriesReturnEmpty() {
        try (
                MockedStatic<OpenGaussSystemTableQueryExecutorFactory> tableFactory = mockStatic(OpenGaussSystemTableQueryExecutorFactory.class);
                MockedStatic<OpenGaussSystemFunctionQueryExecutorFactory> functionFactory = mockStatic(OpenGaussSystemFunctionQueryExecutorFactory.class);
                MockedStatic<PostgreSQLSelectAdminExecutorFactory> postgreFactory = mockStatic(PostgreSQLSelectAdminExecutorFactory.class)) {
            tableFactory.when(() -> OpenGaussSystemTableQueryExecutorFactory.newInstance(any(), any(), any())).thenReturn(Optional.empty());
            functionFactory.when(() -> OpenGaussSystemFunctionQueryExecutorFactory.newInstance(any())).thenReturn(Optional.empty());
            postgreFactory.when(() -> PostgreSQLSelectAdminExecutorFactory.newInstance(any(), any(), any())).thenReturn(Optional.empty());
            assertFalse(OpenGaussSelectAdminExecutorFactory.newInstance(mockSelectStatementContext(), "sql", Collections.emptyList()).isPresent());
        }
    }
    
    private SelectStatementContext mockSelectStatementContext() {
        SelectStatementContext result = mock(SelectStatementContext.class);
        when(result.getSqlStatement()).thenReturn(mock(SelectStatement.class));
        return result;
    }
}
