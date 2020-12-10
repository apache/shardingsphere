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

package org.apache.shardingsphere.proxy.backend.text.metadata.schema.impl;

import org.apache.shardingsphere.infra.auth.MemoryAuthentication;
import org.apache.shardingsphere.infra.auth.ShardingSphereUser;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.proxy.backend.communication.jdbc.connection.BackendConnection;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.backend.response.header.query.QueryResponseHeader;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ShowTablesBackendHandlerTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    private ShowTablesBackendHandler tablesBackendHandler;
    
    @Before
    public void setUp() throws NoSuchFieldException, IllegalAccessException {
        BackendConnection backendConnection = mock(BackendConnection.class);
        when(backendConnection.getUsername()).thenReturn("root");
        tablesBackendHandler = new ShowTablesBackendHandler(backendConnection);
        Map<String, ShardingSphereMetaData> metaDataMap = getMetaDataMap();
        when(backendConnection.getSchemaName()).thenReturn(String.format(SCHEMA_PATTERN, 0));
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), getAuthentication(), new ConfigurationProperties(new Properties()), new MySQLDatabaseType()));
    }
    
    private Map<String, ShardingSphereMetaData> getMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new HashMap<>(10);
        for (int i = 0; i < 10; i++) {
            ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
            when(metaData.isComplete()).thenReturn(false);
            result.put(String.format(SCHEMA_PATTERN, i), metaData);
        }
        return result;
    }
    
    private MemoryAuthentication getAuthentication() {
        ShardingSphereUser user = new ShardingSphereUser("root", Arrays.asList(String.format(SCHEMA_PATTERN, 0), String.format(SCHEMA_PATTERN, 1)));
        MemoryAuthentication result = new MemoryAuthentication();
        result.getUsers().put("root", user);
        return result;
    }
    
    @Test
    public void assertExecuteShowTablesBackendHandler() throws SQLException {
        QueryResponseHeader actual = (QueryResponseHeader) tablesBackendHandler.execute();
        assertThat(actual, instanceOf(QueryResponseHeader.class));
        assertThat(actual.getQueryHeaders().size(), is(1));
    }
    
    @Test
    public void assertShowTablesUsingStream() throws SQLException {
        tablesBackendHandler.execute();
        while (tablesBackendHandler.next()) {
            assertThat(tablesBackendHandler.getRowData().size(), is(1));
        }
    }
}
