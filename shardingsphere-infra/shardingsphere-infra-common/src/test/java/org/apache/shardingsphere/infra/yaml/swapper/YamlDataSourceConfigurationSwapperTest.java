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

package org.apache.shardingsphere.infra.yaml.swapper;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class YamlDataSourceConfigurationSwapperTest {
    
    private final YamlDataSourceConfigurationSwapper swapper = new YamlDataSourceConfigurationSwapper();

    @Test
    public void assertSwapToDataSources() {
        Map<String, Map<String, Object>> yamlConfig = createYamlConfig();
        Map<String, DataSource> dataSources = swapper.swapToDataSources(yamlConfig);
        HikariDataSource actual0 = (HikariDataSource) dataSources.get("ds_0");
        assertThat(actual0.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual0.getJdbcUrl(), is("jdbc:h2:mem:test_ds_0;MODE=MySQL"));
        assertThat(actual0.getUsername(), is("root"));
        assertThat(actual0.getPassword(), is("root"));
        HikariDataSource actual1 = (HikariDataSource) dataSources.get("ds_1");
        assertThat(actual1.getDriverClassName(), is("org.h2.Driver"));
        assertThat(actual1.getJdbcUrl(), is("jdbc:h2:mem:test_ds_1;MODE=MySQL"));
        assertThat(actual1.getUsername(), is("root"));
        assertThat(actual1.getPassword(), is("root"));
    }

    @Test
    public void assertSwapToDataSourceConfiguration() {
        Map<String, Object> yamlConfig = new HashMap<>(3, 1);
        yamlConfig.put("dataSourceClassName", "xxx.jdbc.driver");
        yamlConfig.put("url", "xx:xxx");
        yamlConfig.put("username", "root");
        DataSourceConfiguration actual = swapper.swapToDataSourceConfiguration(yamlConfig);
        assertThat(actual.getDataSourceClassName(), is("xxx.jdbc.driver"));
        assertThat(actual.getProps().size(), is(2));
        assertThat(actual.getProps().get("url").toString(), is("xx:xxx"));
        assertThat(actual.getProps().get("username").toString(), is("root"));
    }
    
    @Test
    public void assertSwapToMap() {
        DataSourceConfiguration dataSourceConfig = new DataSourceConfiguration("xxx.jdbc.driver");
        dataSourceConfig.getProps().put("url", "xx:xxx");
        dataSourceConfig.getProps().put("username", "root");
        Map<String, Object> actual = swapper.swapToMap(dataSourceConfig);
        assertThat(actual.get("dataSourceClassName"), is("xxx.jdbc.driver"));
        assertThat(actual.get("url").toString(), is("xx:xxx"));
        assertThat(actual.get("username").toString(), is("root"));
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
        result.put("driverClassName", "org.h2.Driver");
        result.put("jdbcUrl", String.format("jdbc:h2:mem:test_%s;MODE=MySQL", name));
        result.put("username", "root");
        result.put("password", "root");
        return result;
    }
}
