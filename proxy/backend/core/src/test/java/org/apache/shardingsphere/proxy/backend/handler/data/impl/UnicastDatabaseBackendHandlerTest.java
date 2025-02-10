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
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.hint.HintValueContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.session.connection.ConnectionContext;
import org.apache.shardingsphere.infra.session.query.QueryContext;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnector;
import org.apache.shardingsphere.proxy.backend.connector.DatabaseConnectorFactory;
import org.apache.shardingsphere.proxy.backend.connector.ProxyDatabaseConnectionManager;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.internal.configuration.plugins.Plugins;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class UnicastDatabaseBackendHandlerTest {
    
    private static final String EXECUTE_SQL = "SELECT 1 FROM user WHERE id = 1";
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private UnicastDatabaseBackendHandler unicastDatabaseBackendHandler;
    
    @Mock(answer = Answers.RETURNS_DEEP_STUBS)
    private ConnectionSession connectionSession;
    
    @Mock
    private DatabaseConnectorFactory databaseConnectorFactory;
    
    @Mock
    private DatabaseConnector databaseConnector;
    
    @BeforeEach
    void setUp() throws SQLException {
        when(connectionSession.getCurrentDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        mockDatabaseConnector(new UpdateResponseHeader(mock()));
        unicastDatabaseBackendHandler = new UnicastDatabaseBackendHandler(
                new QueryContext(mock(), EXECUTE_SQL, Collections.emptyList(), new HintValueContext(), mockConnectionContext(), mock()), connectionSession);
        setBackendHandlerFactory(unicastDatabaseBackendHandler);
    }
    
    private ConnectionContext mockConnectionContext() {
        ConnectionContext result = mock(ConnectionContext.class);
        when(result.getCurrentDatabaseName()).thenReturn(Optional.of("foo_db"));
        return result;
    }
    
    private void mockDatabaseConnector(final ResponseHeader responseHeader) throws SQLException {
        when(databaseConnector.execute()).thenReturn(responseHeader);
        when(databaseConnectorFactory.newInstance(any(QueryContext.class), any(ProxyDatabaseConnectionManager.class), eq(false))).thenReturn(databaseConnector);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final DatabaseBackendHandler schemaDatabaseBackendHandler) {
        Plugins.getMemberAccessor().set(schemaDatabaseBackendHandler.getClass().getDeclaredField("databaseConnectorFactory"), schemaDatabaseBackendHandler, databaseConnectorFactory);
    }
    
    @Test
    void assertExecuteDatabaseBackendHandler() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ShardingSphereDatabase database = createDatabases().iterator().next();
        when(contextManager.getDatabase("db_0")).thenReturn(database);
        ResponseHeader actual = unicastDatabaseBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    void assertDatabaseUsingStream() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        ShardingSphereDatabase database = createDatabases().iterator().next();
        when(contextManager.getDatabase("db_0")).thenReturn(database);
        unicastDatabaseBackendHandler.execute();
        while (unicastDatabaseBackendHandler.next()) {
            assertThat(unicastDatabaseBackendHandler.getRowData().getData().size(), is(1));
        }
    }
    
    private ContextManager mockContextManager() {
        ContextManager result = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(createDatabases(), mock(), mock(), mock());
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        when(result.getMetaDataContexts()).thenReturn(metaDataContexts);
        return result;
    }
    
    private Collection<ShardingSphereDatabase> createDatabases() {
        return IntStream.range(0, 10).mapToObj(each -> new ShardingSphereDatabase(
                String.format(DATABASE_PATTERN, each), databaseType, mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), mock(), Collections.emptyList())).collect(Collectors.toList());
    }
}
