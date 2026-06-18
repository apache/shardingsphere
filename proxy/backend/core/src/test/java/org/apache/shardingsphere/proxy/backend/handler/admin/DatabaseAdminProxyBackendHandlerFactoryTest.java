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

package org.apache.shardingsphere.proxy.backend.handler.admin;

import org.apache.shardingsphere.database.connector.core.spi.DatabaseTypedSPILoader;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.binder.context.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.ProxyBackendHandler;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminExecutorCreator;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminUpdateExecutor;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;

import java.util.Collections;
import java.util.Optional;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings({DatabaseTypedSPILoader.class, ProxyContext.class})
class DatabaseAdminProxyBackendHandlerFactoryTest {
    
    private static final DatabaseType DATABASE_TYPE = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private SQLStatementContext sqlStatementContext;
    
    @Test
    void assertNewInstanceWhenExecutorCreatorNotFound() {
        when(DatabaseTypedSPILoader.findService(DatabaseAdminExecutorCreator.class, DATABASE_TYPE)).thenReturn(Optional.empty());
        assertFalse(DatabaseAdminProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, sqlStatementContext, connectionSession, "sql", Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertNewInstanceWhenExecutorNotFound() {
        DatabaseAdminExecutorCreator executorCreator = mock(DatabaseAdminExecutorCreator.class);
        when(executorCreator.create(sqlStatementContext, "sql", "logic_db", Collections.emptyList())).thenReturn(Optional.empty());
        when(DatabaseTypedSPILoader.findService(DatabaseAdminExecutorCreator.class, DATABASE_TYPE)).thenReturn(Optional.of(executorCreator));
        when(connectionSession.getUsedDatabaseName()).thenReturn("logic_db");
        assertFalse(DatabaseAdminProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, sqlStatementContext, connectionSession, "sql", Collections.emptyList()).isPresent());
    }
    
    @Test
    void assertNewInstanceWhenCreateQueryBackendHandler() {
        DatabaseAdminExecutorCreator executorCreator = mock(DatabaseAdminExecutorCreator.class);
        when(DatabaseTypedSPILoader.findService(DatabaseAdminExecutorCreator.class, DATABASE_TYPE)).thenReturn(Optional.of(executorCreator));
        when(executorCreator.create(sqlStatementContext, "sql", "logic_db", Collections.emptyList())).thenReturn(Optional.of(mock(DatabaseAdminQueryExecutor.class)));
        when(connectionSession.getUsedDatabaseName()).thenReturn("logic_db");
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class));
        Optional<ProxyBackendHandler> actual = DatabaseAdminProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, sqlStatementContext, connectionSession, "sql", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseAdminQueryProxyBackendHandler.class));
    }
    
    @Test
    void assertNewInstanceWhenCreateUpdateBackendHandler() {
        DatabaseAdminExecutorCreator executorCreator = mock(DatabaseAdminExecutorCreator.class);
        when(DatabaseTypedSPILoader.findService(DatabaseAdminExecutorCreator.class, DATABASE_TYPE)).thenReturn(Optional.of(executorCreator));
        when(connectionSession.getUsedDatabaseName()).thenReturn("logic_db");
        when(executorCreator.create(sqlStatementContext, "sql", "logic_db", Collections.emptyList())).thenReturn(Optional.of(mock(DatabaseAdminUpdateExecutor.class)));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class));
        Optional<ProxyBackendHandler> actual = DatabaseAdminProxyBackendHandlerFactory.newInstance(DATABASE_TYPE, sqlStatementContext, connectionSession, "sql", Collections.emptyList());
        assertTrue(actual.isPresent());
        assertThat(actual.get(), isA(DatabaseAdminUpdateProxyBackendHandler.class));
    }
}
