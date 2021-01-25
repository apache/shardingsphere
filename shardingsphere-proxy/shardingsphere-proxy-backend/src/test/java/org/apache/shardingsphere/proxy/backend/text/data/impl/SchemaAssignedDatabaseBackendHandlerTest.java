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

package org.apache.shardingsphere.proxy.backend.text.data.impl;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.proxy.backend.text.data.DatabaseBackendHandler;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class SchemaAssignedDatabaseBackendHandlerTest {
    
    private static final String EXECUTE_SQL = "USE test";
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private SchemaAssignedDatabaseBackendHandler schemaAssignedDatabaseBackendHandler;
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException, SQLException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), getAuthentication(), new ConfigurationProperties(new Properties())));
        when(backendConnection.getSchemaName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        mockDatabaseCommunicationEngine(new UpdateResponseHeader(mock(SQLStatement.class)));
        schemaAssignedDatabaseBackendHandler = new SchemaAssignedDatabaseBackendHandler(mock(SQLStatement.class), EXECUTE_SQL, backendConnection);
        setBackendHandlerFactory(schemaAssignedDatabaseBackendHandler);
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.isComplete()).thenReturn(true);
            when(metaData.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    private Map<String, DatabaseType> getDatabaseTypes() {
        Map<String, DatabaseType> result = new HashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), new MySQLDatabaseType());
        }
        return result;
    }
    
    private DefaultAuthentication getAuthentication() {
        ShardingSphereUser user = new ShardingSphereUser("root", Arrays.asList(String.format(SCHEMA_PATTERN, 0), String.format(SCHEMA_PATTERN, 1)));
        DefaultAuthentication result = new DefaultAuthentication();
        result.getUsers().put("root", user);
        return result;
    }
    
    private void mockDatabaseCommunicationEngine(final ResponseHeader responseHeader) throws SQLException {
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(any(), anyString(), any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final DatabaseBackendHandler schemaDatabaseBackendHandler) {
        Field field = schemaDatabaseBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaDatabaseBackendHandler, databaseCommunicationEngineFactory);
    }
    
    @Test
    public void assertExecuteDatabaseBackendHandler() throws SQLException {
        ResponseHeader actual = schemaAssignedDatabaseBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
    }
    
    @Test
    public void assertDatabaseUsingStream() throws SQLException {
        schemaAssignedDatabaseBackendHandler.execute();
        while (schemaAssignedDatabaseBackendHandler.next()) {
            assertThat(schemaAssignedDatabaseBackendHandler.getRowData().size(), is(1));
        }
    }
}
