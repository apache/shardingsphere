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

package org.apache.shardingsphere.metadata.factory;

import org.apache.shardingsphere.infra.config.database.DatabaseConfiguration;
import org.apache.shardingsphere.infra.config.database.impl.DataSourceProvidedDatabaseConfiguration;
import org.apache.shardingsphere.infra.config.props.ConfigurationProperties;
import org.apache.shardingsphere.infra.instance.InstanceContext;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

class ExternalMetaDataFactoryTest {
    
    @Test
    void assertCreateSingleDatabase() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList());
        ShardingSphereDatabase actual = ExternalMetaDataFactory.create("foo_db", databaseConfig, new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertThat(actual.getName(), is("foo_db"));
        assertTrue(actual.getResourceMetaData().getDataSources().isEmpty());
    }
    
    @Test
    void assertCreateDatabaseMap() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList());
        Map<String, ShardingSphereDatabase> actual = ExternalMetaDataFactory.create(
                Collections.singletonMap("foo_db", databaseConfig), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertTrue(actual.containsKey("foo_db"));
        assertTrue(actual.get("foo_db").getResourceMetaData().getDataSources().isEmpty());
    }
    
    @Test
    void assertCreateDatabaseMapWhenConfigUppercaseDatabaseName() throws SQLException {
        DatabaseConfiguration databaseConfig = new DataSourceProvidedDatabaseConfiguration(Collections.emptyMap(), Collections.emptyList());
        Map<String, ShardingSphereDatabase> actual = ExternalMetaDataFactory.create(
                Collections.singletonMap("FOO_DB", databaseConfig), new ConfigurationProperties(new Properties()), mock(InstanceContext.class));
        assertTrue(actual.containsKey("foo_db"));
        assertTrue(actual.get("foo_db").getResourceMetaData().getDataSources().isEmpty());
    }
}
