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

package org.apache.shardingsphere.infra.datasource.pool.creator;

import com.alibaba.druid.pool.DruidDataSource;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;

class DataSourcePoolCreatorTest {
    
    @Test
    void assertCreateMap() {
        Map<String, DataSource> actual = DataSourcePoolCreator.create(Collections.singletonMap("foo_ds", new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties())), true);
        assertThat(actual.size(), is(1));
        assertDataSource((MockedDataSource) actual.get("foo_ds"));
    }
    
    @Test
    void assertCreate() {
        assertDataSource((MockedDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties())));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(3, 1F);
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
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
    void assertCreateAlibabaDruidDataSource() throws SQLException {
        Map<String, Object> props = new LinkedHashMap<>(4, 1F);
        props.put("url", "jdbc:h2:mem:foo_ds");
        props.put("driverClassName", "org.h2.Driver");
        props.put("username", "root");
        props.put("password", "root");
        props.put("dbType", "h2");
        DruidDataSource dataSource = (DruidDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(DruidDataSource.class.getName(), props));
        dataSource.init();
        assertThat(dataSource.getUrl(), is("jdbc:h2:mem:foo_ds"));
        assertThat(dataSource.getUsername(), is("root"));
        assertThat(dataSource.getPassword(), is("root"));
        dataSource.close();
    }
}
