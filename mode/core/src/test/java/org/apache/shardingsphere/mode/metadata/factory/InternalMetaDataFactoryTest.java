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

package org.apache.shardingsphere.mode.metadata.factory;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.core.type.DatabaseType;
import org.apache.shardingsphere.infra.instance.ComputeNodeInstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.unit.StorageUnit;
import org.apache.shardingsphere.infra.spi.type.typed.TypedSPILoader;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class InternalMetaDataFactoryTest {
    
    @Test
    void assertCreateWithDatabaseName() {
        MetaDataPersistService persistService = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(persistService.getDatabaseMetaDataFacade().getSchema().load("foo_db")).thenReturn(Collections.emptyList());
        ShardingSphereDatabase database = InternalMetaDataFactory.create(
                "foo_db", persistService, mock(DatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(ComputeNodeInstanceContext.class));
        assertThat(database.getName(), is("foo_db"));
        assertThat(database.getProtocolType(), is(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        assertTrue(database.getRuleMetaData().getRules().isEmpty());
        assertTrue(database.getAllSchemas().isEmpty());
    }
    
    @Test
    void assertCreateWithDatabasesWithoutStorageUnits() {
        Map<String, ShardingSphereDatabase> databases = InternalMetaDataFactory.create(mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS),
                Collections.singletonMap("foo_db", mock(DatabaseConfiguration.class)), new ConfigurationProperties(new Properties()), mock(ComputeNodeInstanceContext.class));
        assertThat(databases.size(), is(1));
        assertThat(databases.get("foo_db").getName(), is("foo_db"));
        assertThat(databases.get("foo_db").getProtocolType(), is(TypedSPILoader.getService(DatabaseType.class, "MySQL")));
        assertTrue(databases.get("foo_db").getRuleMetaData().getRules().isEmpty());
        assertTrue(databases.get("foo_db").getAllSchemas().isEmpty());
    }
    
    @Test
    void assertCreateWithDatabasesWithStorageUnits() {
        MetaDataPersistService persistService = mock(MetaDataPersistService.class, RETURNS_DEEP_STUBS);
        when(persistService.getDatabaseMetaDataFacade().getSchema().load("foo_db")).thenReturn(Collections.emptyList());
        DatabaseConfiguration databaseConfig = mock(DatabaseConfiguration.class);
        StorageUnit storageUnit = mock(StorageUnit.class);
        when(storageUnit.getDataSource()).thenReturn(new MockedDataSource());
        when(databaseConfig.getStorageUnits()).thenReturn(Collections.singletonMap("foo_ds", storageUnit));
        Map<String, ShardingSphereDatabase> databases = InternalMetaDataFactory.create(
                persistService, Collections.singletonMap("foo_db", databaseConfig), new ConfigurationProperties(new Properties()), mock(ComputeNodeInstanceContext.class));
        assertThat(databases.size(), is(1));
        assertThat(databases.get("foo_db").getName(), is("foo_db"));
        assertThat(databases.get("foo_db").getProtocolType(), is(TypedSPILoader.getService(DatabaseType.class, "FIXTURE")));
        assertTrue(databases.get("foo_db").getRuleMetaData().getRules().isEmpty());
        assertTrue(databases.get("foo_db").getAllSchemas().isEmpty());
    }
}
