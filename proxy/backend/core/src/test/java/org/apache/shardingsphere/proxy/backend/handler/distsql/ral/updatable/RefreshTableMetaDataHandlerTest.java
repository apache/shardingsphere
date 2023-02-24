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
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.Test;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

public final class RefreshTableMetaDataHandlerTest {
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertNoDatabaseSelected() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), mock(ConnectionSession.class));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
            backendHandler.execute();
        }
    }
    
    @Test(expected = UnknownDatabaseException.class)
    public void assertUnknownDatabaseException() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), mockConnectionSession("not_existed_db"));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(mock(ContextManager.class, RETURNS_DEEP_STUBS));
            backendHandler.execute();
        }
    }
    
    @Test(expected = EmptyStorageUnitException.class)
    public void assertEmptyResource() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), mockConnectionSession("sharding_db"));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(Collections.emptyMap());
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().databaseExists("sharding_db")).thenReturn(true);
            backendHandler.execute();
        }
    }
    
    @Test(expected = MissingRequiredStorageUnitsException.class)
    public void assertMissingRequiredResources() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement("t_order", "ds_1", null), mockConnectionSession("sharding_db"));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().databaseExists("sharding_db")).thenReturn(true);
            backendHandler.execute();
        }
    }
    
    @Test
    public void assertUpdate() throws SQLException {
        RefreshTableMetaDataHandler backendHandler = new RefreshTableMetaDataHandler();
        backendHandler.init(new RefreshTableMetaDataStatement(), mockConnectionSession("sharding_db"));
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getDataSourceMap("sharding_db")).thenReturn(Collections.singletonMap("ds_0", new MockedDataSource()));
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            proxyContext.when(() -> ProxyContext.getInstance().databaseExists("sharding_db")).thenReturn(true);
            ResponseHeader actual = backendHandler.execute();
            assertThat(actual, instanceOf(UpdateResponseHeader.class));
        }
    }
    
    private static ConnectionSession mockConnectionSession(final String databaseName) {
        ConnectionSession result = mock(ConnectionSession.class);
        when(result.getDatabaseName()).thenReturn(databaseName);
        return result;
    }
}
