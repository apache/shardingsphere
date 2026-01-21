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

package org.apache.shardingsphere.infra.metadata.database;

import org.apache.shardingsphere.database.connector.core.type.DatabaseType;
import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.DatabaseTypeEngine;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ShardingSphereDatabasesFactoryTest {
    
    @Test
    void assertCreateDatabasesWithSchemas() throws SQLException {
        Map<String, DatabaseConfiguration> databaseConfigs = new LinkedHashMap<>(2, 1F);
        databaseConfigs.put("empty_db", new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList()));
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1/foo_ds");
        databaseConfigs.put("foo_db", new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_ds", new MockedDataSource(connection)), Collections.emptyList()));
        Map<String, Collection<ShardingSphereSchema>> schemas = new LinkedHashMap<>(2, 1F);
        schemas.put("empty_db", Collections.singleton(new ShardingSphereSchema("empty_schema")));
        schemas.put("foo_db", Collections.singleton(new ShardingSphereSchema("foo_schema")));
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigs, props);
        Collection<ShardingSphereDatabase> actual = ShardingSphereDatabasesFactory.create(databaseConfigs, schemas, props, mock(), protocolType);
        assertThat(actual.size(), is(2));
        assertTrue(actual.stream().anyMatch(each -> "empty_db".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "foo_db".equals(each.getName())));
    }
    
    @Test
    void assertCreateDatabasesWithoutSchemas() throws SQLException {
        Map<String, DatabaseConfiguration> databaseConfigs = new LinkedHashMap<>(3, 1F);
        Connection connection = mock(Connection.class, RETURNS_DEEP_STUBS);
        when(connection.getMetaData().getURL()).thenReturn("jdbc:mysql://127.0.0.1/foo_ds");
        MockedDataSource mockedDataSource = new MockedDataSource(connection);
        databaseConfigs.put("foo_db", new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList()));
        databaseConfigs.put("bar_db", new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_ds", mockedDataSource), Collections.emptyList()));
        databaseConfigs.put("sys", new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList()));
        databaseConfigs.put("shardingsphere", new DataSourceProvidedDatabaseConfiguration(Collections.singletonMap("foo_ds", mockedDataSource), Collections.emptyList()));
        ConfigurationProperties props = new ConfigurationProperties(new Properties());
        DatabaseType protocolType = DatabaseTypeEngine.getProtocolType(databaseConfigs, props);
        Collection<ShardingSphereDatabase> actual = ShardingSphereDatabasesFactory.create(databaseConfigs, props, mock(), protocolType);
        assertThat(actual.size(), is(7));
        assertTrue(actual.stream().anyMatch(each -> "foo_db".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "bar_db".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "information_schema".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "performance_schema".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "sys".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "mysql".equals(each.getName())));
        assertTrue(actual.stream().anyMatch(each -> "shardingsphere".equals(each.getName())));
    }
}
