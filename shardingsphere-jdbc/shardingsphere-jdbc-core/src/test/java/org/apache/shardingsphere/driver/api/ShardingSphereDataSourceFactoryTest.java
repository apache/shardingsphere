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
import org.apache.shardingsphere.infra.database.DefaultDatabase;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.lang.reflect.Field;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSphereDataSourceFactoryTest {
    
    private final ModeConfiguration modeConfig = new ModeConfiguration("Standalone", null, false);
    
    @Test
    public void assertCreateDataSourceWithDatabaseName() throws SQLException {
        DataSource testDataSource = ShardingSphereDataSourceFactory.createDataSource("test_db", null);
        assertTrue(testDataSource instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource), is("test_db"));
        DataSource testDataSource1 = ShardingSphereDataSourceFactory.createDataSource(modeConfig);
        assertTrue(testDataSource1 instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource1), is(DefaultDatabase.LOGIC_NAME));
        DataSource testDataSource2 = ShardingSphereDataSourceFactory.createDataSource("", null);
        assertTrue(testDataSource2 instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource2), is(DefaultDatabase.LOGIC_NAME));
        DataSource testDataSource3 = ShardingSphereDataSourceFactory.createDataSource(new HashMap<>(), new LinkedList<>(), new Properties());
        assertThat(getDatabaseName(testDataSource3), is(DefaultDatabase.LOGIC_NAME));
        DataSource testDataSource4 = ShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), new LinkedList<>(), new Properties());
        assertThat(getDatabaseName(testDataSource4), is(DefaultDatabase.LOGIC_NAME));
        DataSource testDataSource5 = ShardingSphereDataSourceFactory.createDataSource("test_db5", new MockedDataSource(), new LinkedList<>(), new Properties());
        assertTrue(testDataSource5 instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource5), is("test_db5"));
        DataSource testDataSource6 = ShardingSphereDataSourceFactory.createDataSource("test_db6", new HashMap<>(), new LinkedList<>(), new Properties());
        assertTrue(testDataSource6 instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource6), is("test_db6"));
        DataSource testDataSource7 = ShardingSphereDataSourceFactory.createDataSource("test_db6", modeConfig, new HashMap<>(), null, null);
        assertTrue(testDataSource7 instanceof ShardingSphereDataSource);
        assertThat(getDatabaseName(testDataSource7), is("test_db6"));
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private static String getDatabaseName(final DataSource dataSource) {
        Field field = ShardingSphereDataSource.class.getDeclaredField("databaseName");
        field.setAccessible(true);
        return (String) field.get(dataSource);
    }
}
