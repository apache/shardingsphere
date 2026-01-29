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

package org.apache.shardingsphere.proxy.frontend.protocol;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.config.props.ConfigurationPropertyKey;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.statistics.ShardingSphereStatistics;
import org.apache.shardingsphere.infra.metadata.statistics.builder.ShardingSphereStatisticsFactory;
import org.apache.shardingsphere.infra.spi.ShardingSphereServiceLoader;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.proxy.backend.context.ProxyContext;
import org.apache.shardingsphere.proxy.frontend.spi.DatabaseProtocolFrontendEngine;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ProxyContext.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FrontDatabaseProtocolTypeFactoryTest {
    
    private final DatabaseType databaseType = TypedSPILoader.getService(DatabaseType.class, "FIXTURE");
    
    @Test
    void assertGetDatabaseTypeWhenNoFrontendEngineFound() {
        ContextManager contextManager = mockContextManager(Collections.emptyList(), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DatabaseProtocolFrontendEngine.class)).thenReturn(Collections.emptyList());
            assertThrows(IllegalArgumentException.class, FrontDatabaseProtocolTypeFactory::getDatabaseType);
        }
    }
    
    @Test
    void assertGetDatabaseTypeWhenSingleFrontendEngineAndNoDatabase() {
        DatabaseProtocolFrontendEngine databaseProtocolFrontendEngine = mock(DatabaseProtocolFrontendEngine.class);
        when(databaseProtocolFrontendEngine.getType()).thenReturn(databaseType);
        ContextManager contextManager = mockContextManager(Collections.emptyList(), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DatabaseProtocolFrontendEngine.class)).thenReturn(Collections.singleton(databaseProtocolFrontendEngine));
            assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is(databaseType.getType()));
        }
    }
    
    @Test
    void assertGetDatabaseTypeWhenDatabaseHasStorageType() {
        ContextManager contextManager = mockContextManager(Collections.singleton(mockDatabaseWithStorageType()), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DatabaseProtocolFrontendEngine.class)).thenReturn(Arrays.asList(mock(), mock()));
            assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is("STORAGE"));
        }
    }
    
    @Test
    void assertGetDatabaseTypeWhenNoDatabaseContainsDataSource() {
        ShardingSphereDatabase database = mock(ShardingSphereDatabase.class);
        when(database.getName()).thenReturn("foo_db");
        when(database.getProtocolType()).thenReturn(databaseType);
        ContextManager contextManager = mockContextManager(Collections.singleton(database), new Properties());
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        try (MockedStatic<ShardingSphereServiceLoader> mocked = mockStatic(ShardingSphereServiceLoader.class, CALLS_REAL_METHODS)) {
            mocked.when(() -> ShardingSphereServiceLoader.getServiceInstances(DatabaseProtocolFrontendEngine.class)).thenReturn(Arrays.asList(mock(), mock()));
            assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is("MySQL"));
        }
    }
    
    @Test
    void assertGetDatabaseTypeFromMetaDataContextsProps() {
        ContextManager contextManager = mockContextManager(Collections.emptyList(),
                PropertiesBuilder.build(new Property(ConfigurationPropertyKey.PROXY_FRONTEND_DATABASE_PROTOCOL_TYPE.getKey(), "FIXTURE")));
        when(ProxyContext.getInstance().getContextManager()).thenReturn(contextManager);
        assertThat(FrontDatabaseProtocolTypeFactory.getDatabaseType().getType(), is("FIXTURE"));
    }
    
    private ShardingSphereDatabase mockDatabaseWithStorageType() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class, RETURNS_DEEP_STUBS);
        when(result.getName()).thenReturn("foo_db");
        when(result.getProtocolType()).thenReturn(databaseType);
        when(result.containsDataSource()).thenReturn(true);
        StorageUnit storageUnit = mock(StorageUnit.class, RETURNS_DEEP_STUBS);
        when(storageUnit.getStorageType().getType()).thenReturn("STORAGE");
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        return result;
    }
    
    private ContextManager mockContextManager(final Collection<ShardingSphereDatabase> databases, final Properties props) {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(), mock(), new ConfigurationProperties(props));
        MetaDataContexts metaDataContexts = new MetaDataContexts(metaData, ShardingSphereStatisticsFactory.create(metaData, new ShardingSphereStatistics()));
        return new ContextManager(metaDataContexts, mock(ComputeNodeInstanceContext.class, RETURNS_DEEP_STUBS), mock(), mock());
    }
}
