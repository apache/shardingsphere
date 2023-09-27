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
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
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
        ResourceHeldRule<?> globalResourceHeldRule = mock(ResourceHeldRule.class);
        ShardingSphereDatabase database = mockDatabase(mock(ResourceMetaData.class, RETURNS_DEEP_STUBS), new MockedDataSource(), mock(ResourceHeldRule.class));
        DatabaseType databaseType = mock(DatabaseType.class);
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        when(ShardingSphereDatabase.create("foo_db", databaseType, configProps)).thenReturn(database);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ResourceMetaData.class),
                new RuleMetaData(Collections.singleton(globalResourceHeldRule)), configProps);
        metaData.addDatabase("foo_db", databaseType, configProps);
        assertThat(metaData.getDatabases(), is(databases));
        verify(globalResourceHeldRule).addResource(database);
    }
    
    @Test
    void assertDropDatabase() {
        ResourceMetaData resourceMetaData = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        MockedDataSource dataSource = new MockedDataSource();
        ResourceHeldRule<?> databaseResourceHeldRule = mock(ResourceHeldRule.class);
        ResourceHeldRule<?> globalResourceHeldRule = mock(ResourceHeldRule.class);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new HashMap<>(Collections.singletonMap("foo_db", mockDatabase(resourceMetaData, dataSource, databaseResourceHeldRule))),
                mock(ResourceMetaData.class), new RuleMetaData(Collections.singleton(globalResourceHeldRule)), new ConfigurationProperties(new Properties()));
        metaData.dropDatabase("foo_db");
        assertTrue(metaData.getDatabases().isEmpty());
        Awaitility.await().pollDelay(10L, TimeUnit.MILLISECONDS).until(dataSource::isClosed);
        assertTrue(dataSource.isClosed());
        verify(databaseResourceHeldRule).closeStaleResource("foo_db");
        verify(globalResourceHeldRule).closeStaleResource("foo_db");
    }
    
    private ShardingSphereDatabase mockDatabase(final ResourceMetaData resourceMetaData, final DataSource dataSource, final ResourceHeldRule<?> databaseResourceHeldRule) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(dataSource);
        when(result.getResourceMetaData().getStorageUnitMetaData().getStorageUnits()).thenReturn(Collections.singletonMap("foo_db", storageUnit));
        when(result.getRuleMetaData()).thenReturn(new RuleMetaData(Collections.singleton(databaseResourceHeldRule)));
        return result;
    }
}
