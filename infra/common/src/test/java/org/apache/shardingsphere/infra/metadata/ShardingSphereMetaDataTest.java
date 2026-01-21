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

package org.apache.shardingsphere.infra.metadata;

import org.apache.groovy.util.Maps;
import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabaseFactory;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.attribute.datasource.StaticDataSourceRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.infra.framework.extension.mock.AutoMockExtension;
import org.apache.shardingsphere.test.infra.framework.extension.mock.StaticMockSettings;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.withSettings;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereDatabaseFactory.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereMetaDataTest {
    
    @SuppressWarnings("resource")
    @Test
    void assertAddDatabase() {
        GlobalRule globalRule = mock(GlobalRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        DatabaseType databaseType = database.getProtocolType();
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        when(ShardingSphereDatabaseFactory.create("foo_db", databaseType, configProps)).thenReturn(database);
        Collection<ShardingSphereDatabase> databases = new LinkedList<>(Collections.singleton(database));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        metaData.addDatabase("foo_db", databaseType, configProps);
        assertThat(metaData.getDatabase("foo_db"), is(database));
    }
    
    @Test
    void assertDropDatabase() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        GlobalRule globalRule = mock(GlobalRule.class);
        MockedDataSource dataSource = new MockedDataSource();
        StaticDataSourceRuleAttribute staticDataSourceRuleAttribute = mock(StaticDataSourceRuleAttribute.class);
        ShardingSphereRule databaseRule1 = mock(ShardingSphereRule.class);
        when(databaseRule1.getAttributes()).thenReturn(new RuleAttributes(staticDataSourceRuleAttribute));
        ShardingSphereRule databaseRule2 = mock(ShardingSphereRule.class, withSettings().extraInterfaces(AutoCloseable.class));
        when(databaseRule2.getAttributes()).thenReturn(new RuleAttributes());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new LinkedList<>(Collections.singleton(mockDatabase(resourceMetaData, dataSource, databaseRule1, databaseRule2))),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), new ConfigurationProperties(new Properties()));
        metaData.dropDatabase("foo_db");
        assertTrue(metaData.getAllDatabases().isEmpty());
        Awaitility.await().pollDelay(10L, TimeUnit.MILLISECONDS).until(dataSource::isClosed);
        assertTrue(dataSource.isClosed());
        verify(globalRule).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED);
        verify(staticDataSourceRuleAttribute).cleanStorageNodeDataSources();
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertContainsDatabase() {
        ShardingSphereRule globalRule = mock(ShardingSphereRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        Collection<ShardingSphereDatabase> databases = new LinkedList<>(Collections.singleton(database));
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        assertTrue(metaData.containsDatabase("foo_db"));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertNotContainsDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        assertFalse(metaData.containsDatabase("foo_db"));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertGetDatabase() {
        ShardingSphereRule globalRule = mock(ShardingSphereRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        Collection<ShardingSphereDatabase> databases = new LinkedList<>(Collections.singleton(database));
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        assertThat(metaData.getDatabase("foo_db"), is(database));
    }
    
    @SuppressWarnings("resource")
    @Test
    void assertPutDatabase() {
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(Collections.emptyList(), new ResourceMetaData(Collections.emptyMap()),
                new RuleMetaData(Collections.emptyList()), new ConfigurationProperties(new Properties()));
        ShardingSphereDatabase database = mockDatabase(mock(), new MockedDataSource(), mock(ShardingSphereRule.class));
        metaData.putDatabase(database);
        assertThat(metaData.getDatabase("foo_db"), is(database));
    }
    
    @Test
    void assertCloseClosesAllRules() throws Exception {
        GlobalRule closableGlobalRule = mock(GlobalRule.class, withSettings().extraInterfaces(AutoCloseable.class));
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class), new MockedDataSource(), mock(ShardingSphereRule.class));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new LinkedList<>(Collections.singleton(database)),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(closableGlobalRule)), new ConfigurationProperties(new Properties()));
        metaData.close();
        verify((AutoCloseable) closableGlobalRule).close();
    }
    
    private ShardingSphereDatabase mockDatabase(final ResourceMetaData resourceMetaData, final DataSource dataSource, final ShardingSphereRule... rules) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Maps.of("url", "jdbc:mock://127.0.0.1/foo_ds", "username", "test"));
        StorageUnit storageUnit = new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, dataSource);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_db", storageUnit));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Arrays.asList(rules)));
        when(result.getProtocolType()).thenReturn(storageUnit.getStorageType());
        return result;
    }
}
