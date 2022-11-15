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

import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.core.TransactionType;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class TransactionRuleTest {
    
    private static final String SHARDING_DB = "sharding_db";
    
    private static final String SHARDING_DB2 = "sharding_db2";
    
    @Test
    public void assertInitTransactionRuleWithMultiDatabaseType() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB, createDatabase()));
        assertNotNull(actual.getResource());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    public void assertAddResource() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB, createDatabase()));
        actual.addResource(createAddDatabase());
        assertNotNull(actual.getResource());
        assertThat(actual.getDatabases().size(), is(2));
        assertTrue(actual.getDatabases().containsKey(SHARDING_DB));
        ShardingSphereResourceMetaData shardingMetadata = actual.getDatabases().get(SHARDING_DB).getResourceMetaData();
        assertThat(shardingMetadata.getDataSources().size(), is(2));
        assertTrue(shardingMetadata.getDataSources().containsKey("ds_0"));
        assertTrue(shardingMetadata.getDataSources().containsKey("ds_1"));
        assertThat(shardingMetadata.getStorageTypes().size(), is(2));
        assertTrue(actual.getDatabases().containsKey(SHARDING_DB2));
        ShardingSphereResourceMetaData shardingMetadata2 = actual.getDatabases().get(SHARDING_DB2).getResourceMetaData();
        assertThat(shardingMetadata2.getDataSources().size(), is(2));
        assertTrue(shardingMetadata2.getDataSources().containsKey("ds_0"));
        assertTrue(shardingMetadata2.getDataSources().containsKey("ds_1"));
        assertThat(shardingMetadata2.getStorageTypes().size(), is(2));
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    public void assertCloseStaleResource() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB, createDatabase()));
        actual.closeStaleResource(SHARDING_DB);
        assertTrue(actual.getDatabases().isEmpty());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    @Test
    public void assertCloseAllStaleResources() {
        TransactionRule actual = new TransactionRule(createTransactionRuleConfiguration(), Collections.singletonMap(SHARDING_DB, createDatabase()));
        actual.closeStaleResource();
        assertTrue(actual.getDatabases().isEmpty());
        assertThat(actual.getResource().getTransactionManager(TransactionType.XA), instanceOf(ShardingSphereTransactionManagerFixture.class));
    }
    
    private static ShardingSphereDatabase createDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereResourceMetaData resourceMetaData = createResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn("sharding_db");
        return result;
    }
    
    private static ShardingSphereResourceMetaData createResourceMetaData() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        when(result.getDataSources()).thenReturn(dataSourceMap);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(2, 1);
        databaseTypes.put("ds_0", new PostgreSQLDatabaseType());
        databaseTypes.put("ds_1", new OpenGaussDatabaseType());
        when(result.getStorageTypes()).thenReturn(databaseTypes);
        return result;
    }
    
    private static ShardingSphereDatabase createAddDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereResourceMetaData resourceMetaData = createAddResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn(SHARDING_DB2);
        return result;
    }
    
    private static ShardingSphereResourceMetaData createAddResourceMetaData() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        when(result.getDataSources()).thenReturn(dataSourceMap);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(2, 1);
        databaseTypes.put("ds_0", new PostgreSQLDatabaseType());
        databaseTypes.put("ds_1", new OpenGaussDatabaseType());
        when(result.getStorageTypes()).thenReturn(databaseTypes);
        return result;
    }
    
    private static TransactionRuleConfiguration createTransactionRuleConfiguration() {
        TransactionRuleConfiguration result = mock(TransactionRuleConfiguration.class);
        when(result.getDefaultType()).thenReturn("XA");
        when(result.getProviderType()).thenReturn("Atomikos");
        return result;
    }
}
