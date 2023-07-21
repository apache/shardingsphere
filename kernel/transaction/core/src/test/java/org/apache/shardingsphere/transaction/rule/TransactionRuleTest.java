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

import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.opengauss.OpenGaussDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.transaction.config.TransactionRuleConfiguration;
import org.apache.shardingsphere.transaction.api.TransactionType;
import org.apache.shardingsphere.transaction.core.fixture.ShardingSphereTransactionManagerFixture;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        ShardingSphereResourceMetaData resourceMetaData1 = actual.getDatabases().get(SHARDING_DB_1).getResourceMetaData();
        assertThat(resourceMetaData1.getDataSources().size(), is(2));
        assertTrue(resourceMetaData1.getDataSources().containsKey("ds_0"));
        assertTrue(resourceMetaData1.getDataSources().containsKey("ds_1"));
        assertThat(resourceMetaData1.getStorageTypes().size(), is(2));
        assertTrue(actual.getDatabases().containsKey(SHARDING_DB_2));
        ShardingSphereResourceMetaData resourceMetaData2 = actual.getDatabases().get(SHARDING_DB_2).getResourceMetaData();
        assertThat(resourceMetaData2.getDataSources().size(), is(2));
        assertTrue(resourceMetaData2.getDataSources().containsKey("ds_0"));
        assertTrue(resourceMetaData2.getDataSources().containsKey("ds_1"));
        assertThat(resourceMetaData2.getStorageTypes().size(), is(2));
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
        ShardingSphereResourceMetaData resourceMetaData = createResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn("sharding_db");
        return result;
    }
    
    private ShardingSphereResourceMetaData createResourceMetaData() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        when(result.getDataSources()).thenReturn(dataSourceMap);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(2, 1F);
        databaseTypes.put("ds_0", new PostgreSQLDatabaseType());
        databaseTypes.put("ds_1", new OpenGaussDatabaseType());
        when(result.getStorageTypes()).thenReturn(databaseTypes);
        return result;
    }
    
    private ShardingSphereDatabase createAddDatabase() {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        ShardingSphereResourceMetaData resourceMetaData = createAddResourceMetaData();
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getName()).thenReturn(SHARDING_DB_2);
        return result;
    }
    
    private ShardingSphereResourceMetaData createAddResourceMetaData() {
        ShardingSphereResourceMetaData result = mock(ShardingSphereResourceMetaData.class);
        Map<String, DataSource> dataSourceMap = new LinkedHashMap<>(2, 1F);
        dataSourceMap.put("ds_0", new MockedDataSource());
        dataSourceMap.put("ds_1", new MockedDataSource());
        when(result.getDataSources()).thenReturn(dataSourceMap);
        Map<String, DatabaseType> databaseTypes = new LinkedHashMap<>(2, 1F);
        databaseTypes.put("ds_0", new PostgreSQLDatabaseType());
        databaseTypes.put("ds_1", new OpenGaussDatabaseType());
        when(result.getStorageTypes()).thenReturn(databaseTypes);
        return result;
    }
    
    private TransactionRuleConfiguration createTransactionRuleConfiguration() {
        TransactionRuleConfiguration result = mock(TransactionRuleConfiguration.class);
        when(result.getDefaultType()).thenReturn("XA");
        when(result.getProviderType()).thenReturn("Atomikos");
        return result;
    }
}
