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

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class YamlDataSourceConfigurationSwapperTest {
    
    private final YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();
    
    @Test
    void assertSwapToDataSources() {
        Map<String, Map<String, Object>> yamlConfig = createYamlConfig();
        Map<String, DataSource> dataSources = swapper.swapToDataSources(yamlConfig);
        MockedDataSource actual0 = (MockedDataSource) dataSources.get("ds_0");
        assertThat(actual0.getUrl(), is("jdbc:mock://127.0.0.1/ds_0"));
        assertThat(actual0.getUsername(), is("root"));
        assertThat(actual0.getPassword(), is("root"));
        MockedDataSource actual1 = (MockedDataSource) dataSources.get("ds_1");
        assertThat(actual1.getUrl(), is("jdbc:mock://127.0.0.1/ds_1"));
        assertThat(actual1.getUsername(), is("root"));
        assertThat(actual1.getPassword(), is("root"));
    }
    
    @Test
    void assertSwapToDataSourcePoolProperties() {
        Map<String, Object> yamlConfig = new HashMap<>(3, 1F);
        yamlConfig.put("dataSourceClassName", MockedDataSource.class.getName());
        yamlConfig.put("url", "xx:xxx");
        yamlConfig.put("username", "root");
        DataSourcePoolProperties actual = swapper.swapToDataSourcePoolProperties(yamlConfig);
        assertThat(actual.getAllLocalProperties().size(), is(3));
        assertThat(actual.getAllLocalProperties().get("dataSourceClassName").toString(), is("org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource"));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("root"));
    }
    
    @Test
    void assertSwapToMap() {
        Map<String, Object> actual = swapper.swapToMap(new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties()));
        assertThat(actual.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.get("url").toString(), is("xx:xxx"));
        assertThat(actual.get("username").toString(), is("root"));
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new LinkedHashMap<>(2, 1F);
        result.put("url", "xx:xxx");
        result.put("username", "root");
        return result;
    }
    
    private Map<String, Map<String, Object>> createYamlConfig() {
        Map<String, Map<String, Object>> result = new LinkedHashMap<>(2, 1F);
        result.put("ds_0", createPropertyMap("ds_0"));
        result.put("ds_1", createPropertyMap("ds_1"));
        return result;
    }
    
    private Map<String, Object> createPropertyMap(final String name) {
        Map<String, Object> result = new LinkedHashMap<>(4, 1F);
        result.put("dataSourceClassName", MockedDataSource.class.getName());
        result.put("url", String.format("jdbc:mock://127.0.0.1/%s", name));
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
    
    @Test
    void assertGetDataSourcePoolPropertiesMap() {
        Map<String, Map<String, Object>> dataSources = new LinkedHashMap<>(2, 1F);
        dataSources.put("ds_1", createPropertyMap("ds_1"));
        dataSources.put("ds_2", createPropertyMap("ds_2"));
        YamlRootConfiguration yamlRootConfig = new YamlRootConfiguration();
        yamlRootConfig.setDataSources(dataSources);
        Map<String, DataSourcePoolProperties> actual = swapper.getDataSourcePoolPropertiesMap(yamlRootConfig);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("ds_1").getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.get("ds_2").getPoolClassName(), is(MockedDataSource.class.getName()));
    }
    
    @Test
    void assertSwapToDataSourcePoolPropertiesWithHikariDataSource() {
        Map<String, Object> yamlConfig = new HashMap<>(4, 1F);
        yamlConfig.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        yamlConfig.put("url", "jdbc:h2:mem:test");
        yamlConfig.put("username", "sa");
        yamlConfig.put("password", "");
        DataSourcePoolProperties actual = swapper.swapToDataSourcePoolProperties(yamlConfig);
        assertThat(actual.getPoolClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getAllLocalProperties().containsKey("dataSourceClassName"), is(false));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("jdbc:h2:mem:test"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("sa"));
    }
    
    @Test
    void assertSwapToDataSourcePoolPropertiesWithCustomPoolProps() {
        DataSourcePoolProperties actual = swapper.swapToDataSourcePoolProperties(createYamlConfiguration());
        assertThat(actual.getPoolClassName(), is(MockedDataSource.class.getName()));
        assertThat(actual.getAllLocalProperties().get("url").toString(), is("jdbc:test:memory:"));
        assertThat(actual.getAllLocalProperties().get("username").toString(), is("test"));
        assertThat(actual.getAllLocalProperties().get("customKey1").toString(), is("customValue1"));
        assertThat(actual.getAllLocalProperties().get("customKey2").toString(), is("customValue2"));
        assertFalse(actual.getAllLocalProperties().containsKey("customPoolProps"));
    }
    
    private Map<String, Object> createYamlConfiguration() {
        Map<String, Object> result = new HashMap<>(5, 1F);
        result.put("dataSourceClassName", MockedDataSource.class.getName());
        result.put("url", "jdbc:test:memory:");
        result.put("username", "test");
        result.put("password", "test");
        Map<String, Object> customProps = new HashMap<>(2, 1F);
        customProps.put("customKey1", "customValue1");
        customProps.put("customKey2", "customValue2");
        result.put("customPoolProps", customProps);
        return result;
    }
}
