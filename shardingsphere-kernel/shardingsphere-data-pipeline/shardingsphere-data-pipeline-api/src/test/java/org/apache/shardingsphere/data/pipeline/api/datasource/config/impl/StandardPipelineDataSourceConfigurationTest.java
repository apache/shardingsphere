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

package org.apache.shardingsphere.data.pipeline.api.datasource.config.impl;

import org.apache.shardingsphere.data.pipeline.api.datasource.config.yaml.YamlJdbcConfiguration;
import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.yaml.config.swapper.resource.YamlDataSourceConfigurationSwapper;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public final class StandardPipelineDataSourceConfigurationTest {
    
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false";
    
    private static final String USERNAME = "userName";
    
    private static final String PASSWORD = "password";
    
    @Test
    public void assertCreateWithSimpleParameters() {
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(JDBC_URL, USERNAME, PASSWORD);
        assertGetConfig(actual);
        actual = new StandardPipelineDataSourceConfiguration(actual.getParameter());
        assertGetConfig(actual);
    }
    
    @Test
    public void assertCreateWithYamlDataSourceConfiguration() {
        Map<String, Object> yamlDataSourceConfig = new HashMap<>();
        yamlDataSourceConfig.put("url", JDBC_URL);
        yamlDataSourceConfig.put("username", USERNAME);
        yamlDataSourceConfig.put("password", PASSWORD);
        yamlDataSourceConfig.put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource");
        yamlDataSourceConfig.put("minPoolSize", "20");
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(yamlDataSourceConfig);
        assertGetConfig(actual);
    }
    
    private void assertGetConfig(final StandardPipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getType(), is("MySQL"));
        assertThat(actual.getType(), is(StandardPipelineDataSourceConfiguration.TYPE));
        DataSourceProperties dataSourceProps = (DataSourceProperties) actual.getDataSourceConfiguration();
        assertThat(dataSourceProps.getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertGetJdbcConfig(actual.getJdbcConfig());
        assertDataSourceProperties(dataSourceProps);
    }
    
    private void assertGetJdbcConfig(final YamlJdbcConfiguration actual) {
        assertThat(actual.getJdbcUrl(), is(JDBC_URL));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
    }
    
    private void assertDataSourceProperties(final DataSourceProperties dataSourceProps) {
        Map<String, Object> actual = new YamlDataSourceConfigurationSwapper().swapToMap(dataSourceProps);
        assertThat(actual.get("minPoolSize"), is("1"));
    }
}
