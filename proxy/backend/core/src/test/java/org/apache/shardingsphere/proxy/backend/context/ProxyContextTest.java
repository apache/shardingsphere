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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.state.cluster.ClusterState;
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ProxyContextTest {
    
    private static final String SCHEMA_PATTERN = "db_%s";
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    private ContextManager currentContextManager;
    
    @BeforeEach
    void recordCurrentContextManager() {
        currentContextManager = ProxyContext.getInstance().getContextManager();
    }
    
    @AfterEach
    void restorePreviousContextManager() {
        ProxyContext.init(currentContextManager);
    }
    
    @Test
    void assertInit() {
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(mock(MetaDataPersistService.class), new ShardingSphereMetaData());
        ProxyContext.init(new ContextManager(metaDataContexts, mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS), mock(PersistRepository.class)));
        assertThat(ProxyContext.getInstance().getContextManager().getStateContext(), is(ProxyContext.getInstance().getContextManager().getStateContext()));
        assertThat(ProxyContext.getInstance().getContextManager().getStateContext().getCurrentClusterState(), is(ClusterState.OK));
        assertThat(ProxyContext.getInstance().getContextManager().getMetaDataContexts(), is(ProxyContext.getInstance().getContextManager().getMetaDataContexts()));
        assertTrue(ProxyContext.getInstance().getInstanceStateContext().isPresent());
        assertThat(ProxyContext.getInstance().getInstanceStateContext(), is(ProxyContext.getInstance().getInstanceStateContext()));
    }
    
    @Test
    void assertDatabaseExists() {
        Map<String, ShardingSphereDatabase> databases = mockDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        assertTrue(ProxyContext.getInstance().databaseExists("db"));
        assertFalse(ProxyContext.getInstance().databaseExists("db_1"));
    }
    
    @Test
    void assertGetAllDatabaseNames() {
        Map<String, ShardingSphereDatabase> databases = createDatabases();
        ContextManager contextManager = mock(ContextManager.class, RETURNS_DEEP_STUBS);
        MetaDataContexts metaDataContexts = MetaDataContextsFactory.create(mock(MetaDataPersistService.class),
                new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), mock(RuleMetaData.class), new ConfigurationProperties(new Properties())));
        when(contextManager.getMetaDataContexts()).thenReturn(metaDataContexts);
        ProxyContext.init(contextManager);
        assertThat(new LinkedHashSet<>(ProxyContext.getInstance().getAllDatabaseNames()), is(databases.keySet()));
    }
    
    private Map<String, ShardingSphereDatabase> createDatabases() {
        Map<String, ShardingSphereDatabase> result = new LinkedHashMap<>(10, 1F);
        for (int i = 0; i < 10; i++) {
            ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
            String databaseName = String.format(SCHEMA_PATTERN, i);
            when(database.getName()).thenReturn(databaseName);
            when(database.getProtocolType()).thenReturn(databaseType);
            result.put(databaseName, database);
        }
        return result;
    }
    
    private Map<String, ShardingSphereDatabase> mockDatabases() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(database.getProtocolType()).thenReturn(databaseType);
        return Collections.singletonMap("db", database);
    }
}
