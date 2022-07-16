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

public final class ShardingSphereDataSourceFactoryTest {
    
    @Test
    public void assertCreateDataSourceWithDatabaseName() throws SQLException {
        DataSource testDataSource0 = ShardingSphereDataSourceFactory.createDataSource("test_db", null);
        assertDataSource(testDataSource0, "test_db");
        DataSource testDataSource1 = ShardingSphereDataSourceFactory.createDataSource(new ModeConfiguration("Standalone", null, false));
        assertDataSource(testDataSource1, DefaultDatabase.LOGIC_NAME);
        DataSource testDataSource2 = ShardingSphereDataSourceFactory.createDataSource("", null);
        assertDataSource(testDataSource2, DefaultDatabase.LOGIC_NAME);
        DataSource testDataSource3 = ShardingSphereDataSourceFactory.createDataSource(new HashMap<>(), new LinkedList<>(), new Properties());
        assertDataSource(testDataSource3, DefaultDatabase.LOGIC_NAME);
        DataSource testDataSource4 = ShardingSphereDataSourceFactory.createDataSource(new MockedDataSource(), new LinkedList<>(), new Properties());
        assertDataSource(testDataSource4, DefaultDatabase.LOGIC_NAME);
        DataSource testDataSource5 = ShardingSphereDataSourceFactory.createDataSource("test_db5", new MockedDataSource(), new LinkedList<>(), new Properties());
        assertDataSource(testDataSource5, "test_db5");
        DataSource testDataSource6 = ShardingSphereDataSourceFactory.createDataSource("test_db6", new HashMap<>(), new LinkedList<>(), new Properties());
        assertDataSource(testDataSource6, "test_db6");
        DataSource testDataSource7 = ShardingSphereDataSourceFactory.createDataSource("test_db7", new ModeConfiguration("Standalone", null, false), new HashMap<>(), null, null);
        assertDataSource(testDataSource7, "test_db7");
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private void assertDataSource(final DataSource actualDataSource, final String expectedDataSourceName) {
        Field field = ShardingSphereDataSource.class.getDeclaredField("databaseName");
        field.setAccessible(true);
        assertThat((String) field.get(actualDataSource), is(expectedDataSourceName));
    }
}
