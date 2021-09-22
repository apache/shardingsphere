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

import org.apache.shardingsphere.infra.config.properties.ConfigurationProperties;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.executor.kernel.ExecutorEngine;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.optimize.context.OptimizerContextFactory;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.exception.NoDatabaseSelectedException;
import org.apache.shardingsphere.transaction.context.TransactionContexts;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
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
    public void assertInit() {
        MetaDataContexts metaDataContexts = mock(MetaDataContexts.class);
        TransactionContexts transactionContexts = mock(TransactionContexts.class);
        ProxyContext proxyContext = ProxyContext.getInstance();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        when(contextManager.getTransactionContexts()).thenReturn(transactionContexts);
        proxyContext.init(contextManager);
        assertThat(proxyContext.getContextManager().getMetaDataContexts(), is(metaDataContexts));
        assertThat(proxyContext.getContextManager().getTransactionContexts(), is(transactionContexts));
    }
    
    @Test
    public void assertSchemaExists() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                metaDataMap, mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
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
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                metaDataMap, mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        ProxyContext.getInstance().getMetaData("schema1");
    }
    
    @Test
    public void assertGetSchema() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = mockMetaDataMap();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                metaDataMap, mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        assertThat(metaDataMap.get("schema"), is(ProxyContext.getInstance().getMetaData("schema")));
    }
    
    @Test
    public void assertGetAllSchemaNames() throws NoSuchFieldException, IllegalAccessException {
        Map<String, ShardingSphereMetaData> metaDataMap = createMetaDataMap();
        Field contextManagerField = ProxyContext.getInstance().getClass().getDeclaredField("contextManager");
        contextManagerField.setAccessible(true);
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                metaDataMap, mock(ShardingSphereRuleMetaData.class), mock(ExecutorEngine.class), new ConfigurationProperties(new Properties()), mock(OptimizerContextFactory.class));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        contextManagerField.set(ProxyContext.getInstance(), contextManager);
        assertThat(new LinkedHashSet<>(ProxyContext.getInstance().getAllSchemaNames()), is(metaDataMap.keySet()));
    }
    
    private Map<String, ShardingSphereMetaData> createMetaDataMap() {
        Map<String, ShardingSphereMetaData> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            result.put(String.format(SCHEMA_PATTERN, i), mock(ShardingSphereMetaData.class));
        }
        return result;
    }
    
    private Map<String, ShardingSphereMetaData> mockMetaDataMap() {
        ShardingSphereMetaData metaData = mock(ShardingSphereMetaData.class, RETURNS_DEEP_STUBS);
        when(metaData.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        return Collections.singletonMap("schema", metaData);
    }
}
