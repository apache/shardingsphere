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

package org.apache.shardingsphere.infra.datasource.dbcp.creator;

import lombok.SneakyThrows;
import org.apache.commons.dbcp2.BasicDataSource;
import org.apache.shardingsphere.infra.datasource.pool.creator.DataSourcePoolCreator;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.apache.shardingsphere.test.util.PropertiesBuilder;
import org.apache.shardingsphere.test.util.PropertiesBuilder.Property;
import org.junit.jupiter.api.Test;
import org.mockito.internal.configuration.plugins.Plugins;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

class DBCPDataSourcePoolCreatorTest {
    
    @Test
    void assertCreateDataSource() {
        BasicDataSource actual = (BasicDataSource) DataSourcePoolCreator.create(new DataSourceProperties(BasicDataSource.class.getName(), createDataSourceProperties()));
        assertThat(actual.getUrl(), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(getConnectionProperties(actual), is(PropertiesBuilder.build(new Property("foo", "foo_value"), new Property("bar", "bar_value"))));
    }
    
    private Map<String, Object> createDataSourceProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("username", "root");
        result.put("password", "root");
        result.put("connectionProperties", PropertiesBuilder.build(new Property("foo", "foo_value"), new Property("bar", "bar_value")));
        return result;
    }
    
    @SneakyThrows(ReflectiveOperationException.class)
    private Properties getConnectionProperties(final BasicDataSource actual) {
        return (Properties) Plugins.getMemberAccessor().get(BasicDataSource.class.getDeclaredField("connectionProperties"), actual);
    }
}
