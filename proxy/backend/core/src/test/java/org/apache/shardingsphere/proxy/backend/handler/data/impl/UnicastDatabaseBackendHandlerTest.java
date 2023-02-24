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

package org.apache.shardingsphere.proxy.backend.handler.data.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.binder.QueryContext;
import org.apache.shardingsphere.infra.binder.statement.SQLStatementContext;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.connector.BackendConnection;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.internal.configuration.plugins.Plugins;
import org.mockito.junit.MockitoJUnitRunner;

import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UnicastDatabaseBackendHandlerTest {
    
    private static final String EXECUTE_SQL = "SELECT 1 FROM user WHERE id = 1";
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private UnicastDatabaseBackendHandler unicastDatabaseBackendHandler;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private DatabaseConnectorFactory databaseConnectorFactory;
    
    @Mock
    private DatabaseConnector databaseConnector;
    
    @Before
    public void setUp() throws SQLException {
        when(connectionSession.getDefaultDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        when(connectionSession.getBackendConnection()).thenReturn(mock(BackendConnection.class));
        mockDatabaseConnector(new UpdateResponseHeader(mock(SQLStatement.class)));
        unicastDatabaseBackendHandler = new UnicastDatabaseBackendHandler(new QueryContext(mock(SQLStatementContext.class), EXECUTE_SQL, Collections.emptyList()), connectionSession);
        setBackendHandlerFactory(unicastDatabaseBackendHandler);
    }
    
    private void mockDatabaseConnector(final ResponseHeader responseHeader) throws SQLException {
        when(databaseConnector.execute()).thenReturn(responseHeader);
        when(databaseConnectorFactory.newInstance(any(QueryContext.class), any(BackendConnection.class), eq(false))).thenReturn(databaseConnector);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final DatabaseBackendHandler schemaDatabaseBackendHandler) {
        Plugins.getMemberAccessor()
                .set(schemaDatabaseBackendHandler.getClass().getDeclaredField("databaseConnectorFactory"), schemaDatabaseBackendHandler, databaseConnectorFactory);
    }
    
    @Test
    public void assertExecuteDatabaseBackendHandler() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            ShardingSphereDatabase database = getDatabases().get("db_0");
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("db_0")).thenReturn(database);
            ResponseHeader actual = unicastDatabaseBackendHandler.execute();
            assertThat(actual, instanceOf(UpdateResponseHeader.class));
        }
    }
    
    @Test
    public void assertDatabaseUsingStream() throws SQLException {
        ContextManager contextManager = mockContextManager();
        try (MockedStatic<ProxyContext> proxyContext = mockStatic(ProxyContext.class, RETURNS_DEEP_STUBS)) {
            proxyContext.when(() -> ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
            ShardingSphereDatabase database = getDatabases().get("db_0");
            proxyContext.when(() -> ProxyContext.getInstance().getDatabase("db_0")).thenReturn(database);
            unicastDatabaseBackendHandler.execute();
            while (unicastDatabaseBackendHandler.next()) {
                assertThat(unicastDatabaseBackendHandler.getRowData().getData().size(), is(1));
            }
        }
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.containsDataSource()).thenReturn(true);
            when(database.getProtocolType()).thenReturn(new H2DatabaseType());
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
}
