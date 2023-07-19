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
import org.apache.shardingsphere.infra.database.spi.DatabaseType;
import org.apache.shardingsphere.infra.database.mysql.MySQLDatabaseType;
import org.apache.shardingsphere.infra.database.postgresql.PostgreSQLDatabaseType;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.resource.ShardingSphereResourceMetaData;
import org.apache.shardingsphere.infra.metadata.database.rule.ShardingSphereRuleMetaData;
import org.apache.shardingsphere.infra.rule.identifier.type.ResourceHeldRule;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.mock.AutoMockExtension;
import org.apache.shardingsphere.test.mock.StaticMockSettings;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
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
        ShardingSphereDatabase database = mockDatabase(mock(ShardingSphereResourceMetaData.class), new MockedDataSource(), mock(ResourceHeldRule.class));
        DatabaseType databaseType = mock(DatabaseType.class);
        ConfigurationProperties configProps = new ConfigurationProperties(new Properties());
        when(ShardingSphereDatabase.create("foo_db", databaseType, configProps)).thenReturn(database);
        Map<String, ShardingSphereDatabase> databases = new HashMap<>(Collections.singletonMap("foo_db", database));
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(databases, mock(ShardingSphereResourceMetaData.class),
                new ShardingSphereRuleMetaData(Collections.singleton(globalResourceHeldRule)), configProps);
        metaData.addDatabase("foo_db", databaseType, configProps);
        assertThat(metaData.getDatabases(), is(databases));
        verify(globalResourceHeldRule).addResource(database);
    }
    
    @Test
    void assertDropDatabase() {
        ShardingSphereResourceMetaData resourceMetaData = mock(ShardingSphereResourceMetaData.class);
        DataSource dataSource = new MockedDataSource();
        ResourceHeldRule<?> databaseResourceHeldRule = mock(ResourceHeldRule.class);
        ResourceHeldRule<?> globalResourceHeldRule = mock(ResourceHeldRule.class);
        ShardingSphereMetaData metaData = new ShardingSphereMetaData(new HashMap<>(Collections.singletonMap("foo_db", mockDatabase(resourceMetaData, dataSource, databaseResourceHeldRule))),
                mock(ShardingSphereResourceMetaData.class), new ShardingSphereRuleMetaData(Collections.singleton(globalResourceHeldRule)), new ConfigurationProperties(new Properties()));
        metaData.dropDatabase("foo_db");
        assertTrue(metaData.getDatabases().isEmpty());
        verify(resourceMetaData).close(dataSource);
        verify(databaseResourceHeldRule).closeStaleResource("foo_db");
        verify(globalResourceHeldRule).closeStaleResource("foo_db");
    }
    
    private ShardingSphereDatabase mockDatabase(final ShardingSphereResourceMetaData resourceMetaData, final DataSource dataSource, final ResourceHeldRule<?> databaseResourceHeldRule) {
        ShardingSphereDatabase result = mock(ShardingSphereDatabase.class);
        when(result.getName()).thenReturn("foo_db");
        when(result.getResourceMetaData()).thenReturn(resourceMetaData);
        when(result.getResourceMetaData().getDataSources()).thenReturn(Collections.singletonMap("foo_db", dataSource));
        when(result.getRuleMetaData()).thenReturn(new ShardingSphereRuleMetaData(Collections.singleton(databaseResourceHeldRule)));
        return result;
    }
    
    @Test
    void assertGetPostgreSQLDefaultSchema() throws SQLException {
        PostgreSQLDatabaseType databaseType = new PostgreSQLDatabaseType();
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, Collections.singletonMap("", databaseType),
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("public"));
    }
    
    @Test
    void assertGetMySQLDefaultSchema() throws SQLException {
        MySQLDatabaseType databaseType = new MySQLDatabaseType();
        ShardingSphereDatabase actual = ShardingSphereDatabase.create("foo_db", databaseType, Collections.singletonMap("", databaseType),
                mock(DataSourceProvidedDatabaseConfiguration.class), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertNotNull(actual.getSchema("foo_db"));
    }
}
