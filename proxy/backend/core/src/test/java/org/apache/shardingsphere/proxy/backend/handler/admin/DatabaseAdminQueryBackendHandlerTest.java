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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataMergedResult;
import org.apache.shardingsphere.infra.merge.result.impl.local.LocalDataQueryResultRow;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.util.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.admin.executor.DatabaseAdminQueryExecutor;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryHeaderBuilder;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
class DatabaseAdminQueryBackendHandlerTest {
    
    private DatabaseAdminQueryBackendHandler handler;
    
    @BeforeEach
    void before() throws SQLException {
        ConnectionSession connectionSession = mock(ConnectionSession.class);
        when(connectionSession.getDatabaseName()).thenReturn("foo_db");
        DatabaseAdminQueryExecutor executor = mock(DatabaseAdminQueryExecutor.class, RETURNS_DEEP_STUBS);
        when(executor.getMergedResult()).thenReturn(new LocalDataMergedResult(Collections.singleton(new LocalDataQueryResultRow("ds_0", "ds_1"))));
        when(executor.getQueryResultMetaData().getColumnCount()).thenReturn(1);
        handler = new DatabaseAdminQueryBackendHandler(connectionSession, executor);
    }
    
    @Test
    void assertExecute() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(QueryHeaderBuilder.class, null)).thenReturn(mock(QueryHeaderBuilder.class));
            assertThat(((QueryResponseHeader) handler.execute()).getQueryHeaders().size(), is(1));
        }
    }
    
    @Test
    void assertNext() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(QueryHeaderBuilder.class, null)).thenReturn(mock(QueryHeaderBuilder.class));
            handler.execute();
            assertTrue(handler.next());
            assertFalse(handler.next());
        }
    }
    
    @Test
    void assertGetRowData() throws SQLException {
        ContextManager contextManager = mockContextManager();
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<TypedSPILoader> typedSPILoader = mockStatic(TypedSPILoader.class)) {
            typedSPILoader.when(() -> TypedSPILoader.getService(QueryHeaderBuilder.class, null)).thenReturn(mock(QueryHeaderBuilder.class));
            handler.execute();
            assertTrue(handler.next());
            assertThat(handler.getRowData().getData().size(), is(1));
        }
    }
    
    private ContextManager mockContextManager() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(
                Collections.singletonMap("foo_db", mock(ShardingSphereDatabase.class)), mock(ShardingSphereResourceMetaData.class),
                mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())));
        return new ContextManager(metaDataContexts, mock(InstanceContext.class));
    }
}
