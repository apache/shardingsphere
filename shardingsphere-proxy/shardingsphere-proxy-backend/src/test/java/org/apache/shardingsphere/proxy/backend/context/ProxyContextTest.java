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

package org.apache.shardingsphere.proxy.backend.context;

import org.apache.shardingsphere.infra.auth.builtin.DefaultAuthentication;
import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.infra.context.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.context.metadata.impl.StandardMetaDataContexts;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.jdbc.test.MockedDataSource;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxyContextTest {
    
    private static final String SCHEMA_PATTERN = "schema_%s";
    
    @Test
    public void assertGetDataSourceSample() throws NoSuchFieldException, IllegalAccessException {
        Map<String, DataSource> mockDataSourceMap = new HashMap<>(2, 1);
        mockDataSourceMap.put("ds_1", new MockedDataSource());
        mockDataSourceMap.put("ds_2", new MockedDataSource());
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(mockMetaDataMap(mockDataSourceMap), mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
        Optional<DataSource> actual = ProxyContext.getInstance().getDataSourceSample();
        assertThat(actual, is(Optional.of(mockDataSourceMap.get("ds_1"))));
    }
    
    @Test
    public void assertInit() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        ProxyContext proxyContext = ProxyContext.getInstance();
        proxyContext.init(metaDataContexts, transactionContexts);
        assertEquals(metaDataContexts, proxyContext.getMetaDataContexts());
        assertEquals(transactionContexts, proxyContext.getTransactionContexts());
    }
    
    @Test
    public void assertSchemaExists() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap(Collections.emptyMap());
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
        assertTrue(ProxyContext.getInstance().schemaExists("schema"));
        assertFalse(ProxyContext.getInstance().schemaExists("schema_2"));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetSchemaWithNull() {
        assertNull(ProxyContext.getInstance().getMetaData(null));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetSchemaWithEmptyString() {
        assertNull(ProxyContext.getInstance().getMetaData(""));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetSchemaWhenNotExisted() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap(Collections.emptyMap());
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
        ProxyContext.getInstance().getMetaData("schema1");
    }
    
    @Test
    public void assertGetSchema() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap(Collections.emptyMap());
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
        assertThat(metaDataMap.get("schema"), is(ProxyContext.getInstance().getMetaData("schema")));
    }
    
    @Test
    public void assertGetAllSchemaNames() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = createMetaDataMap();
        Field metaDataContexts = ProxyContext.getInstance().getClass().getDeclaredField("metaDataContexts");
        metaDataContexts.setAccessible(true);
        metaDataContexts.set(ProxyContext.getInstance(), 
                new StandardMetaDataContexts(metaDataMap, mock(ExecutorEngine.class), new DefaultAuthentication(), new ConfigurationProperties(new Properties())));
        assertThat(new LinkedHashSet<>(ProxyContext.getInstance().getAllSchemaNames()), is(metaDataMap.keySet()));
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(ShardingSphereMetaData.class));
        }
        return result;
    }
    
    private Map<String, ShardingSphereMetaData> mockMetaDataMap(final Map<String, DataSource> mockDataSourceMap) {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getName()).thenReturn("schema");
        when(metaData.getResource().getDataSources()).thenReturn(mockDataSourceMap);
        return Collections.singletonMap("schema", metaData);
    }
}
