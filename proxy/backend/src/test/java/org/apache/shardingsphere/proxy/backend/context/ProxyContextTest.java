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

import org.apache.shardingsphere.dialect.exception.syntax.database.NoDatabaseSelectedException;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.dialect.H2DatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.data.ShardingSphereData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.proxy.backend.util.ProxyContextRestorer;
import org.junit.Test;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class ProxyContextTest extends ProxyContextRestorer {
    
    private static final String SCHEMA_PATTERN = "db_%s";
    
    @Test
    public void assertInit() {
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class), new ShardingSphereMetaData(), new ShardingSphereData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(InstanceContext.class, RETURNS_DEEP_STUBS)));
        assertThat(ProxyContext.getInstance().getContextManager().getMetaDataContexts(), is(ProxyContext.getInstance().getContextManager().getMetaDataContexts()));
        assertTrue(ProxyContext.getInstance().getStateContext().isPresent());
        assertThat(ProxyContext.getInstance().getStateContext(), is(ProxyContext.getInstance().getStateContext()));
    }
    
    @Test
    public void assertDatabaseExists() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        assertTrue(ProxyContext.getInstance().databaseExists("db"));
        assertFalse(ProxyContext.getInstance().databaseExists("db_1"));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetDatabaseWithNull() {
        assertNull(ProxyContext.getInstance().getDatabase(null));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetDatabaseWithEmptyString() {
        assertNull(ProxyContext.getInstance().getDatabase(""));
    }
    
    @Test(expected = NoDatabaseSelectedException.class)
    public void assertGetDatabaseWhenNotExisted() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        ProxyContext.getInstance().getDatabase("db1");
    }
    
    @Test
    public void assertGetDatabase() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        assertThat(databases.get("db"), is(ProxyContext.getInstance().getDatabase("db")));
    }
    
    @Test
    public void assertGetAllDatabaseNames() {
        Map<String, ShardingSphereDatabase> databases = createDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = new MetaDataContexts(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ShardingSphereRuleMetaData.class), new ConfigurationProperties(new Properties())), new ShardingSphereData());
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        assertThat(new LinkedHashSet<>(ProxyContext.getInstance().getAllDatabaseNames()), is(databases.keySet()));
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(10, 1);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            when(database.getName()).thenReturn(String.format(SCHEMA_PATTERN, i));
            result.put(String.format(SCHEMA_PATTERN, i), database);
        }
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getResource().getDatabaseType()).thenReturn(new H2DatabaseType());
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(1, 1);
        result.put("db", database);
        return result;
    }
}
