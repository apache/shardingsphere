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

import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.ShardingSphereRule;
import org.apache.shardingsphere.infra.rule.attribute.RuleAttributes;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(AutoMockExtension.class)
@StaticMockSettings(ShardingSphereDatabase.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ShardingSphereMetaDataTest {
    
    @Test
    void assertAddDatabase() {
        GlobalRule globalRule = mock(GlobalRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        DatabaseType databaseType = mock(DatabaseType.class);
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        when(ShardingSphereDatabase.create("foo_db", databaseType, configProps)).thenReturn(database);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        metaData.addDatabase("foo_db", databaseType, configProps);
        assertThat(metaData.getDatabases(), is(databases));
        verify(globalRule).refresh(databases, GlobalRuleChangedType.DATABASE_CHANGED);
    }
    
    @Test
    void assertDropDatabase() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        GlobalRule globalRule = mock(GlobalRule.class);
        MockedDataSource dataSource = new MockedDataSource();
        ShardingSphereRule databaseRule = mock(ShardingSphereRule.class);
        when(databaseRule.getAttributes()).thenReturn(new RuleAttributes());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new HashMap<>(Collections.singletonMap("foo_db", mockDatabase(resourceMetaData, dataSource, databaseRule))),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), new ConfigurationProperties(new Properties()));
        metaData.dropDatabase("foo_db");
        assertTrue(metaData.getDatabases().isEmpty());
        Awaitility.await().pollDelay(10L, TimeUnit.MILLISECONDS).until(dataSource::isClosed);
        assertTrue(dataSource.isClosed());
        verify(globalRule).refresh(metaData.getDatabases(), GlobalRuleChangedType.DATABASE_CHANGED);
    }
    
    @Test
    void assertContainsDatabase() {
        ShardingSphereRule globalRule = mock(ShardingSphereRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        assertTrue(metaData.containsDatabase("foo_db"));
    }
    
    @Test
    void assertGetDatabase() {
        ShardingSphereRule globalRule = mock(ShardingSphereRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), globalRule);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalRule)), configProps);
        assertThat(metaData.getDatabase("foo_db"), is(database));
    }
    
    private ShardingSphereDatabase mockDatabase(final ResourceMetaData resourceMetaData, final DataSource dataSource, final ShardingSphereRule rule) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        DataSourcePoolProperties dataSourcePoolProps = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/foo_ds"));
        StorageUnit storageUnit = new StorageUnit(mock(StorageNode.class), dataSourcePoolProps, dataSource);
        when(result.getResourceMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_db", storageUnit));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(rule)));
        return result;
    }
}
