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
import org.junit.Test;

import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class StandardPipelineDataSourceConfigurationTest {
    
    private static final String NEW_JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=true";
    
    private static final String JDBC_URL = "jdbc:mysql://127.0.0.1:3306/demo_ds?serverTimezone=UTC&useSSL=false";
    
    private static final String USERNAME = "userName";
    
    private static final String PASSWORD = "password";
    
    @Test
    public void assertCreate() {
        StandardPipelineDataSourceConfiguration actual = new StandardPipelineDataSourceConfiguration(JDBC_URL, USERNAME, PASSWORD);
        assertGetConfig(actual);
        actual = new StandardPipelineDataSourceConfiguration(actual.getParameter());
        assertGetConfig(actual);
        assertAppendJDBCQueryProperties(actual);
    }
    
    private void assertGetConfig(final StandardPipelineDataSourceConfiguration actual) {
        assertThat(actual.getDatabaseType().getName(), is("MySQL"));
        assertThat(actual.getType(), is("JDBC"));
        assertThat(((DataSourceProperties) actual.getDataSourceConfiguration()).getDataSourceClassName(), is("com.zaxxer.hikari.HikariDataSource"));
        assertGetJdbcConfig(actual.getJdbcConfig());
    }
    
    private void assertGetJdbcConfig(final YamlJdbcConfiguration actual) {
        assertThat(actual.getJdbcUrl(), is(JDBC_URL));
        assertThat(actual.getUsername(), is(USERNAME));
        assertThat(actual.getPassword(), is(PASSWORD));
    }
    
    private void assertAppendJDBCQueryProperties(final StandardPipelineDataSourceConfiguration actual) {
        Properties props = new Properties();
        props.setProperty("useSSL", Boolean.TRUE.toString());
        actual.appendJDBCQueryProperties(props);
        assertThat(actual.getJdbcConfig().getJdbcUrl(), is(NEW_JDBC_URL));
    }
}
