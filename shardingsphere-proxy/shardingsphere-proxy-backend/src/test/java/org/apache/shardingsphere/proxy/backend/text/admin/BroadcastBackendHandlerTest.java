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

package org.apache.shardingsphere.proxy.backend.text.admin;

import lombok.SneakyThrows;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngine;
import org.apache.shardingsphere.proxy.backend.communication.DatabaseCommunicationEngineFactory;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.ResponseHeader;
import org.apache.shardingsphere.proxy.backend.response.header.update.UpdateResponseHeader;
import org.apache.shardingsphere.sql.parser.sql.common.statement.SQLStatement;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import java.lang.reflect.Field;
import java.sql.SQLException;
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
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public final class BroadcastBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Mock
    private BackendConnection backendConnection;
    
    @Mock
    private DatabaseCommunicationEngineFactory databaseCommunicationEngineFactory;
    
    @Mock
    private DatabaseCommunicationEngine databaseCommunicationEngine;
    
    @Before
    public void setUp() throws IllegalAccessException, NoSuchFieldException {
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(getMetaDataMap(), mock(ExecutorEngine.class), new Authentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
        when(backendConnection.getSchemaName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
    }
    
    @Test
    public void assertExecuteSuccess() throws SQLException {
        mockDatabaseCommunicationEngine(new UpdateResponseHeader());
        BroadcastBackendHandler broadcastBackendHandler = new BroadcastBackendHandler("SET timeout = 1000", mock(SQLStatement.class), backendConnection);
        setBackendHandlerFactory(broadcastBackendHandler);
        ResponseHeader actual = broadcastBackendHandler.execute();
        assertThat(actual, instanceOf(UpdateResponseHeader.class));
        assertThat(((UpdateResponseHeader) actual).getUpdateCount(), is(0L));
        assertThat(((UpdateResponseHeader) actual).getLastInsertId(), is(0L));
        verify(databaseCommunicationEngine, times(10)).execute();
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.isComplete()).thenReturn(true);
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    private void mockDatabaseCommunicationEngine(final ResponseHeader responseHeader) throws SQLException {
        when(databaseCommunicationEngine.execute()).thenReturn(responseHeader);
        when(databaseCommunicationEngineFactory.newTextProtocolInstance(any(), anyString(), any())).thenReturn(databaseCommunicationEngine);
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void setBackendHandlerFactory(final BroadcastBackendHandler schemaBroadcastBackendHandler) {
        Field field = schemaBroadcastBackendHandler.getClass().getDeclaredField("databaseCommunicationEngineFactory");
        field.setAccessible(true);
        field.set(schemaBroadcastBackendHandler, databaseCommunicationEngineFactory);
    }
}
