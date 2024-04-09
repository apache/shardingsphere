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

package org.apache.shardingsphere.driver.api;

import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.config.mode.ModeConfiguration;
import org.apache.shardingsphere.infra.database.core.DefaultDatabase;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class ShardingSphereDataSourceFactoryTest {
    
    @Test
    void assertCreateDataSourceWithModeConfiguration() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(new ModeConfiguration("Standalone", null)), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertCreateDataSourceWithDatabaseNameAndModeConfiguration() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", new ModeConfiguration("Standalone", null), new HashMap<>(), null, null), "test_db");
    }
    
    @Test
    void assertCreateDataSourceWithAllParametersForMultipleDataSourcesWithDefaultDatabaseName() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                new ModeConfiguration("Standalone", null), new HashMap<>(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertCreateDataSourceWithAllParametersForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                "test_db", new ModeConfiguration("Standalone", null), new HashMap<>(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @Test
    void assertCreateDataSourceWithAllParametersForSingleDataSourceWithDefaultDatabaseName() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(
                new ModeConfiguration("Standalone", null), new MockedDataSource(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertCreateDataSourceWithAllParametersForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db",
                new ModeConfiguration("Standalone", null), new MockedDataSource(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @Test
    void assertCreateDataSourceWithDefaultModeConfigurationForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource(null), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertCreateDataSourceWithDatabaseNameAndDefaultModeConfigurationForMultipleDataSources() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", null), "test_db");
    }
    
    @Test
    void assertCreateDataSourceWithDefaultModeConfigurationForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource((ModeConfiguration) null, new MockedDataSource(), new LinkedList<>(), new Properties()), DefaultDatabase.LOGIC_NAME);
    }
    
    @Test
    void assertCreateDataSourceWithDatabaseNameAndDefaultModeConfigurationForSingleDataSource() throws SQLException {
        assertDataSource(ShardingSphereDataSourceFactory.createDataSource("test_db", null, new MockedDataSource(), new LinkedList<>(), new Properties()), "test_db");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource actualDataSource, final String expectedDataSourceName) {
        assertThat(Plugins.getMemberAccessor().get(ShardingSphereDataSource.class.getDeclaredField("databaseName"), actualDataSource), is(expectedDataSourceName));
    }
}
