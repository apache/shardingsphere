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

package org.apache.shardingsphere.proxy.backend.handler.distsql.ral.updatable;

import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.dialect.exception.syntax.database.UnknownDatabaseException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.EmptyStorageUnitException;
import org.apache.shardingsphere.distsql.handler.exception.storageunit.MissingRequiredStorageUnitsException;
import org.apache.shardingsphere.distsql.parser.statement.ral.updatable.RefreshTableMetaDataStatement;
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.distsql.ral.UpdatableRALBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class RefreshTableMetaDataUpdaterTest {
    
    @Test
    void assertNoDatabaseSelected() {
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        UpdatableRALBackendHandler<?> backendHandler = new UpdatableRALBackendHandler<>(new RefreshTableMetaDataStatement(), mock(ConnectionSession.class));
        assertThrows(NoDatabaseSelectedException.class, backendHandler::execute);
    }
    
    @Test
    void assertUnknownDatabaseException() {
        when(ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
        UpdatableRALBackendHandler<?> backendHandler = new UpdatableRALBackendHandler<>(new RefreshTableMetaDataStatement(), mockConnectionSession("not_existed_db"));
        assertThrows(UnknownDatabaseException.class, backendHandler::execute);
    }
    
    @Test
    void assertEmptyResource() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("foo_db")).thenReturn(Collections.emptyMap());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        UpdatableRALBackendHandler<?> backendHandler = new UpdatableRALBackendHandler<>(new RefreshTableMetaDataStatement(), mockConnectionSession("foo_db"));
        assertThrows(EmptyStorageUnitException.class, backendHandler::execute);
    }
    
    @Test
    void assertMissingRequiredResources() {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("foo_db")).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        UpdatableRALBackendHandler<?> backendHandler = new UpdatableRALBackendHandler<>(new RefreshTableMetaDataStatement("t_order", "ds_1", null), mockConnectionSession("foo_db"));
        assertThrows(MissingRequiredStorageUnitsException.class, backendHandler::execute);
    }
    
    @Test
    void assertUpdate() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("foo_db")).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        when(ProxyContext.getInstance().databaseExists("foo_db")).thenReturn(true);
        UpdatableRALBackendHandler<?> backendHandler = new UpdatableRALBackendHandler<>(new RefreshTableMetaDataStatement(), mockConnectionSession("foo_db"));
        ResponseHeader actual = backendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    private ConnectionSession mockConnectionSession(final String databaseName) {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn(databaseName);
        when(result.getProtocolType()).thenReturn(TypedSPILoader.getService(DatabaseType.class, "FIXTURE"));
        return result;
    }
}
