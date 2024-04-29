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

package org.apache.shardingsphere.data.pipeline.api.type;

import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class StandardPipelineDataSourceConfigurationTest {
    
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds?useSSL=false";
    
    private static final String USERNAME = "userName";
    
    private static final String PASSWORD = "password";
    
    @Test
    void assertCreateWithSimpleParameters() {
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(JDBC_URL, USERNAME, PASSWORD);
        assertGetConfig(actual);
        actual = new StandardPipelineDataSourceConfiguration(actual.getParameter());
        assertGetConfig(actual);
    }
    
    @Test
    void assertCreateWithYamlDataSourceConfiguration() {
        Map<String, Object> yamlDataSourceConfig = new HashMap<>();
        yamlDataSourceConfig.put("url", JDBC_URL);
        yamlDataSourceConfig.put("username", USERNAME);
        yamlDataSourceConfig.put("password", PASSWORD);
        yamlDataSourceConfig.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        yamlDataSourceConfig.put("minPoolSize", "20");
        Map<String, Object> backup = new HashMap<>(yamlDataSourceConfig);
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
        assertParameterUnchanged(backup, yamlDataSourceConfig);
        assertGetConfig(actual);
        yamlDataSourceConfig.remove("url");
        yamlDataSourceConfig.put("jdbcUrl", JDBC_URL);
        actual = new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
        assertGetConfig(actual);
    }
    
    private void assertParameterUnchanged(final Map<String, Object> backup, final Map<String, Object> handled) {
        assertThat(handled.size(), is(backup.size()));
        for (Entry<String, Object> entry : backup.entrySet()) {
            Object actual = handled.get(entry.getKey());
            assertNotNull(actual, "value of '" + entry.getKey() + "' doesn't exist");
            assertThat("value of '" + entry.getKey() + "' doesn't match", actual, is(entry.getValue()));
        }
    }
    
    private void assertGetConfig(final StandardPipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("MySQL"));
        assertThat(actual.getType(), is(StandardPipelineDataSourceConfiguration.TYPE));
        DataSourcePoolProperties props = (DataSourcePoolProperties) actual.getDataSourceConfiguration();
        assertThat(props.getPoolClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(actual.getUrl(), is(JDBC_URL));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
        assertDataSourcePoolProperties(props);
    }
    
    private void assertDataSourcePoolProperties(final DataSourcePoolProperties props) {
        Map<String, Object> actual = new YamlDataSourceConfigurationSwapper().swapToMap(props);
        assertThat(actual.get("minPoolSize"), is("1"));
    }
}
