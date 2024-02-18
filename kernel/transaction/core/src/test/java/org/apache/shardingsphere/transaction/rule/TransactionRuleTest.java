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

package org.apache.shardingsphere.transaction.rule;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.resource.node.StorageNode;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class TransactionRuleTest {
    
    private static final String SHARDING_DB_1 = "sharding_db_1";
    
    private static final String SHARDING_DB_2 = "sharding_db_2";
    
    @Test
    void assertInitTransactionRuleWithMultiDatabaseType() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB_1, createDatabase()));
        assertNotNull(actual.getResource());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertAddResource() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB_1, createDatabase()));
        actual.addResource(createAddDatabase());
        assertNotNull(actual.getResource());
        assertThat(actual.getDatabases().size(), is(2));
        assertTrue(actual.getDatabases().containsKey(SHARDING_DB_1));
        ResourceMetaData resourceMetaData1 = actual.getDatabases().get(SHARDING_DB_1).getResourceMetaData();
        assertThat(resourceMetaData1.getStorageUnits().size(), is(2));
        assertTrue(resourceMetaData1.getStorageUnits().containsKey("ds_0"));
        assertTrue(resourceMetaData1.getStorageUnits().containsKey("ds_1"));
        assertTrue(actual.getDatabases().containsKey(SHARDING_DB_2));
        ResourceMetaData resourceMetaData2 = actual.getDatabases().get(SHARDING_DB_2).getResourceMetaData();
        assertThat(resourceMetaData2.getStorageUnits().size(), is(2));
        assertTrue(resourceMetaData2.getStorageUnits().containsKey("ds_0"));
        assertTrue(resourceMetaData2.getStorageUnits().containsKey("ds_1"));
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertCloseStaleResource() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB_1, createDatabase()));
        actual.closeStaleResource(SHARDING_DB_1);
        assertTrue(actual.getDatabases().isEmpty());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    void assertCloseAllStaleResources() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB_1, createDatabase()));
        actual.closeStaleResource();
        assertTrue(actual.getDatabases().isEmpty());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    private ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ResourceMetaData resourceMetaData = createResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn("sharding_db");
        return result;
    }
    
    private ResourceMetaData createResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_0"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_1"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    private ShardingSphereDatabase createAddDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ResourceMetaData resourceMetaData = createAddResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn(SHARDING_DB_2);
        return result;
    }
    
    private ResourceMetaData createAddResourceMetaData() {
        Map<String, StorageUnit> storageUnits = new HashMap<>(2, 1F);
        DataSourcePoolProperties dataSourcePoolProps0 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps0.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_0"));
        storageUnits.put("ds_0", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps0, new MockedDataSource()));
        DataSourcePoolProperties dataSourcePoolProps1 = mock(DataSourcePoolProperties.class, RETURNS_DEEP_STUBS);
        when(dataSourcePoolProps1.getConnectionPropertySynonyms().getStandardProperties()).thenReturn(Collections.singletonMap("url", "jdbc:mock://127.0.0.1/ds_1"));
        storageUnits.put("ds_1", new StorageUnit(mock(StorageNode.class), dataSourcePoolProps1, new MockedDataSource()));
        ResourceMetaData result = mock(ResourceMetaData.class, RETURNS_DEEP_STUBS);
        when(result.getStorageUnits()).thenReturn(storageUnits);
        return result;
    }
    
    private TransactionRuleConfiguration createTransactionRuleConfiguration() {
        TransactionRuleConfiguration result = mock(TransactionRuleConfiguration.class);
        when(result.getDefaultType()).thenReturn("XA");
        when(result.getProviderType()).thenReturn("Atomikos");
        return result;
    }
}
