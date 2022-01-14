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

package org.apache.shardingsphere.infra.config.datasource.props;

import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourcePropertiesCreatorTest {
    
    @Test
    public void assertCreateMap() {
        Map<String, DataSource> dataSourceMap = new HashMap<>(2, 1);
        dataSourceMap.put("foo_ds", createDataSource());
        Map<String, DataSourceProperties> actual = DataSourcePropertiesCreator.create(dataSourceMap);
        assertThat(actual.size(), is(1));
        assertThat(actual.get("foo_ds"), is(createDataSourceProperties()));
    }
    
    @Test
    public void assertCreate() {
        assertThat(DataSourcePropertiesCreator.create(createDataSource()), is(createDataSourceProperties()));
    }
    
    private DataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName(MockedDataSource.class.getName());
        result.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private DataSourceProperties createDataSourceProperties() {
        DataSourceProperties result = new DataSourceProperties(MockedDataSource.class.getName());
        result.getProps().put("driverClassName", MockedDataSource.class.getName());
        result.getProps().put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.getProps().put("username", "root");
        result.getProps().put("password", "root");
        result.getProps().put("maximumPoolSize", "-1");
        return result;
    }
}
