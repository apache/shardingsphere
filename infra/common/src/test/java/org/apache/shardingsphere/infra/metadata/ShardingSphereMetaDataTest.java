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

import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.database.type.DatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.type.dialect.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResources;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;
import org.mockito.MockedStatic;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public final class ShardingSphereMetaDataTest {
    
    @Test
    public void assertAddDatabase() throws SQLException {
        ShardingSphereResources resource = mock(ShardingSphereResources.class);
        DataSource dataSource = new MockedDataSource();
        ResourceHeldRule<?> databaseResourceHeldRule = mock(ResourceHeldRule.class);
        ResourceHeldRule<?> globalResourceHeldRule = mock(ResourceHeldRule.class);
        ShardingSphereDatabase database = mockDatabase(resource, dataSource, databaseResourceHeldRule);
        try (MockedStatic<ShardingSphereDatabase> mockedStatic = mockStatic(ShardingSphereDatabase.class)) {
            DatabaseType databaseType = mock(DatabaseType.class);
            mockedStatic.when(() -> ShardingSphereDatabase.create("foo_db", databaseType)).thenReturn(database);
            Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
            ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases,
                    new ShardingSphereRuleMetaData(Collections.singleton(globalResourceHeldRule)), new ConfigurationProperties(new Properties()));
            metaData.addDatabase("foo_db", databaseType);
            assertThat(metaData.getDatabases(), is(databases));
            verify(globalResourceHeldRule).addResource(database);
        }
    }
    
    @Test
    public void assertDropDatabase() {
        ShardingSphereResources resource = mock(ShardingSphereResources.class);
        DataSource dataSource = new MockedDataSource();
        ResourceHeldRule<?> databaseResourceHeldRule = mock(ResourceHeldRule.class);
        ResourceHeldRule<?> globalResourceHeldRule = mock(ResourceHeldRule.class);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new HashMap<>(Collections.singletonMap("foo_db", mockDatabase(resource, dataSource, databaseResourceHeldRule))),
                new ShardingSphereRuleMetaData(Collections.singleton(globalResourceHeldRule)), new ConfigurationProperties(new Properties()));
        metaData.dropDatabase("foo_db");
        assertTrue(metaData.getDatabases().isEmpty());
        verify(resource).close(dataSource);
        verify(databaseResourceHeldRule).closeStaleResource("foo_db");
        verify(globalResourceHeldRule).closeStaleResource("foo_db");
    }
    
    private ShardingSphereDatabase mockDatabase(final ShardingSphereResources resource, final DataSource dataSource, final ResourceHeldRule<?> databaseResourceHeldRule) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getResources()).thenReturn(resource);
        when(result.getResources().getDataSources()).thenReturn(Collections.singletonMap("foo_db", dataSource));
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(databaseResourceHeldRule)));
        return result;
    }
    
    @Test
    public void assertGetPostgresDefaultSchema() throws SQLException {
        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, databaseType,
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("public"));
    }
    
    @Test
    public void assertGetMySQLDefaultSchema() throws SQLException {
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, databaseType,
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("foo_db"));
    }
}
