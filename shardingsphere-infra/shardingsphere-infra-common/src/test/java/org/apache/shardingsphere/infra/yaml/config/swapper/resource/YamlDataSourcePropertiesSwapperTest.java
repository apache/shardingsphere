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

package org.apache.shardingsphere.infra.yaml.config.swapper.resource;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.test.mock.MockedDataSource;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlDataSourcePropertiesSwapperTest {
    
    private final YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();
    
    @Test
    public void assertSwapToDataSources() {
        Map<String, Map<String, Object>> yamlConfig = createYamlConfig();
        Map<String, DataSource> dataSources = swapper.swapToDataSources(yamlConfig);
        HikariDataSource actual0 = (HikariDataSource) dataSources.get("ds_0");
        assertThat(actual0.getJdbcUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        assertThat(actual0.getUsername(), is("root"));
        assertThat(actual0.getPassword(), is("root"));
        HikariDataSource actual1 = (HikariDataSource) dataSources.get("ds_1");
        assertThat(actual1.getJdbcUrl(), is("jdbc:mock://127.0.0.1/ds_1"));
        assertThat(actual1.getUsername(), is("root"));
        assertThat(actual1.getPassword(), is("root"));
    }
    
    @Test
    public void assertSwapToDataSourceProperties() {
        Map<String, Object> yamlConfig = new HashMap<>(3, 1);
        yamlConfig.put("dataSourceClassName", MockedDataSource.class.getName());
        yamlConfig.put("url", "xx:xxx");
        yamlConfig.put("username", "root");
        DataSourceProperties actual = swapper.swapToDataSourceProperties(yamlConfig);
        assertThat(actual.getAllLocalProperties().size(), is(2));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("root"));
    }
    
    @Test
    public void assertSwapToMap() {
        Map<String, Object> actual = swapper.swapToMap(new DataSourceProperties(MockedDataSource.class.getName(), createProperties()));
        assertThat(actual.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.get("url").toString(), is("xx:xxx"));
        assertThat(actual.get("username").toString(), is("root"));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1);
        result.put("url", "xx:xxx");
        result.put("username", "root");
        return result;
    }
    
    private Map<String, Map<String, Object>> createYamlConfig() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>(2, 1);
        result.put("ds_0", createPropertyMap("ds_0"));
        result.put("ds_1", createPropertyMap("ds_1"));
        return result;
    }
    
    private Map<String, Object> createPropertyMap(final String name) {
        Map<String, Object> result = new LinkedHashMap<>(5, 1);
        result.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        result.put("jdbcUrl", String.format("jdbc:mock://127.0.0.1/%s", name));
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
}
