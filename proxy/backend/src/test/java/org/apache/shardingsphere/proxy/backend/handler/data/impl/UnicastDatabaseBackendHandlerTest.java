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
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.communication.BackendConnection;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.handler.data.DatabaseBackendHandler;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.session.ConnectionSession;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
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
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class UnicastDatabaseBackendHandlerTest extends ProxyContextRestorer {
    
    private static final String EXECUTE_SQL = "SELECT 1 FROM user WHERE id = 1";
    
    private static final String DATABASE_PATTERN = "db_%s";
    
    private UnicastDatabaseBackendHandler unicastDatabaseBackendHandler;
    
    @Mock
    private ConnectionSession connectionSession;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws SQLException {
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(getDatabases(), mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        when(connectionSession.getDefaultDatabaseName()).thenReturn(String.format(DATABASE_PATTERN, 0));
        when(connectionSession.getBackendConnection()).thenReturn(mock(BackendConnection.class));
        mockDatabaseCommunicationEngine(new UpdateResponseHeader(mock(SQLStatement.class)));
        unicastDatabaseBackendHandler = new UnicastDatabaseBackendHandler(new QueryContext(mock(SQLStatementContext.class), EXECUTE_SQL, Collections.emptyList()), connectionSession);
        setBackendHandlerFactory(unicastDatabaseBackendHandler);
    }
    
    private Map<String, ShardingSphereDatabase> getDatabases() {
        Map<String, ShardingSphereDatabase> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.containsDataSource()).thenReturn(true);
            when(database.getResourceMetaData().getDatabaseType()).thenReturn(new H2DatabaseType());
            result.put(String.format(DATABASE_PATTERN, i), database);
        }
        return result;
    }
    
    private void mockDatabaseCommunicationEngine(final ResponseHeader responseHeader) throws SQLException {
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngineFactory.newDatabaseCommunicationEngine(any(QueryContext.class), any(BackendConnection.class), eq(false))).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final DatabaseBackendHandler schemaDatabaseBackendHandler) {
        Field field = schemaDatabaseBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaDatabaseBackendHandler, databaseCommunicationEngineFactory);
    }
    
    @Test
    public void assertExecuteDatabaseBackendHandler() throws SQLException {
        ResponseHeader actual = unicastDatabaseBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertDatabaseUsingStream() throws SQLException {
        unicastDatabaseBackendHandler.execute();
        while (unicastDatabaseBackendHandler.next()) {
            assertThat(unicastDatabaseBackendHandler.getRowData().getData().size(), is(1));
        }
    }
}
