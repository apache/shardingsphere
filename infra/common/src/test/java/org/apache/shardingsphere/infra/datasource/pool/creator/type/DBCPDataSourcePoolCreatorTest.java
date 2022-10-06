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

package org.apache.shardingsphere.infra.datasource.pool.creator.type;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class DBCPDataSourcePoolCreatorTest {
    
    @Test
    public void assertCreateDataSource() {
        BasicDataSource actual = (BasicDataSource) DataSourcePoolCreator.create(new DataSourceProperties(BasicDataSource.class.getName(), createDataSourceProperties()));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(getConnectionProperties(actual), is(createJdbcUrlProperties()));
    }
    
    private Map<String, Object> createDataSourceProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("username", "root");
        result.put("password", "root");
        result.put("connectionProperties", createJdbcUrlProperties());
        return result;
    }
    
    private Properties createJdbcUrlProperties() {
        Properties result = new Properties();
        result.put("foo", "foo_value");
        result.put("bar", "bar_value");
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Properties getConnectionProperties(final BasicDataSource actual) {
        Field field = actual.getClass().getDeclaredField("connectionProperties");
        field.setAccessible(true);
        return (Properties) field.get(actual);
    }
}
