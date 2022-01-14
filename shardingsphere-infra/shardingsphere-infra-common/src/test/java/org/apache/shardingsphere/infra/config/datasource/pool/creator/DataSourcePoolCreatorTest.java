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

package org.apache.shardingsphere.infra.config.datasource.pool.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.config.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.CommonDataSource;
import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;

public final class DataSourcePoolCreatorTest {
    
    @Test
    public void assertCreateMap() {
        Map<String, DataSourceProperties> dataSourcePropsMap = new HashMap<>(1, 1);
        dataSourcePropsMap.put("foo_ds", createDataSourceProperties());
        Map<String, DataSource> actual = DataSourcePoolCreator.create(dataSourcePropsMap);
        assertThat(actual.size(), is(1));
        assertDataSource((MockedDataSource) actual.get("foo_ds"));
    }
    
    @Test
    public void assertCreate() {
        MockedDataSource actual = (MockedDataSource) DataSourcePoolCreator.create(createDataSourceProperties());
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertDataSource(actual);
    }
    
    private DataSourceProperties createDataSourceProperties() {
        DataSourceProperties result = new DataSourceProperties(MockedDataSource.class.getName());
        result.getProps().put("driverClassName", MockedDataSource.class.getName());
        result.getProps().put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.getProps().put("username", "root");
        result.getProps().put("password", "root");
        return result;
    }
    
    private void assertDataSource(final MockedDataSource actual) {
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getMaxPoolSize(), is(100));
        assertNull(actual.getMinPoolSize());
    }
    
    @Test
    public void assertCreateDefaultDataSource() {
        assertThat(DataSourcePoolCreator.create(createDefaultDataSourceProperties()), instanceOf(CommonDataSource.class));
    }
    
    private DataSourceProperties createDefaultDataSourceProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("driverClassName", MockedDataSource.class.getName());
        props.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("username", "root");
        props.put("password", "root");
        DataSourceProperties result = new DataSourceProperties(BasicDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
    
    @Test
    public void assertCreateHikariDataSource() {
        assertThat(DataSourcePoolCreator.create(createHikariDataSourceProperties()), instanceOf(HikariDataSource.class));
    }
    
    private DataSourceProperties createHikariDataSourceProperties() {
        Map<String, Object> props = new HashMap<>();
        props.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        props.put("driverClassName", MockedDataSource.class.getName());
        props.put("username", "root");
        props.put("password", "root");
        DataSourceProperties result = new DataSourceProperties(HikariDataSource.class.getName());
        result.getProps().putAll(props);
        return result;
    }
}
