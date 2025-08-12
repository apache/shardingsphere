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

package org.apache.shardingsphere.infra.datasource.pool.hikari.creator;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder;
import org.apache.shardingsphere.infra.util.props.PropertiesBuilder.Property;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class HikariDataSourcePoolCreatorTest {
    
    @Test
    void assertCreateDataSource() {
        HikariDataSource actual = (HikariDataSource) DataSourcePoolCreator.create(new DataSourcePoolProperties(HikariDataSource.class.getName(), createDataSourcePoolProperties()));
        assertThat(actual.getJdbcUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getDriverClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getDataSourceProperties(), is(PropertiesBuilder.build(new Property("foo", "foo_value"), new Property("bar", "bar_value"))));
    }
    
    private Map<String, Object> createDataSourcePoolProperties() {
        Map<String, Object> result = new HashMap<>(5, 1F);
        result.put("jdbcUrl", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("username", "root");
        result.put("password", "root");
        result.put("dataSourceProperties", PropertiesBuilder.build(new Property("foo", "foo_value"), new Property("bar", "bar_value")));
        return result;
    }
}
