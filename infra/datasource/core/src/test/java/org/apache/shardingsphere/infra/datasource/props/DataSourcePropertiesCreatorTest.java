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

package org.apache.shardingsphere.infra.datasource.props;

import org.apache.shardingsphere.infra.datasource.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.props.custom.CustomDataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.props.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.test.fixture.jdbc.MockedDataSource;
import org.junit.jupiter.api.Test;

import javax.sql.DataSource;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class DataSourcePropertiesCreatorTest {
    
    @Test
    void assertCreateWithDataSourceConfiguration() {
        assertParameter(DataSourcePropertiesCreator.create(createResourceConfiguration()));
    }
    
    private DataSourceConfiguration createResourceConfiguration() {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration("com.zaxxer.hikari.HikariDataSource", "jdbc:mysql://localhost:3306/demo_ds", "root", "root");
        PoolConfiguration poolConfig = new PoolConfiguration(null, null, null, null, null, null, null);
        return new DataSourceConfiguration(connectionConfig, poolConfig);
    }
    
    private void assertParameter(final DataSourceProperties actual) {
        Map<String, Object> props = actual.getAllLocalProperties();
        assertThat(props.size(), is(10));
        assertThat(props.get("dataSourceClassName"), is("com.zaxxer.hikari.HikariDataSource"));
        assertThat(props.get("url"), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(props.get("username"), is("root"));
        assertThat(props.get("password"), is("root"));
        assertNull(props.get("maximumPoolSize"));
        assertNull(props.get("minimumIdle"));
        assertNull(props.get("connectionTimeout"));
        assertNull(props.get("idleTimeout"));
        assertNull(props.get("maxLifetime"));
    }
    
    @Test
    void assertCreateWithDataSource() {
        assertThat(DataSourcePropertiesCreator.create(createDataSource()), is(new DataSourceProperties(MockedDataSource.class.getName(), createProperties())));
    }
    
    @Test
    void assertCreateConfiguration() {
        DataSourceProperties dataSourceProperties = mock(DataSourceProperties.class);
        ConnectionPropertySynonyms connectionPropertySynonyms = new ConnectionPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        PoolPropertySynonyms poolPropertySynonyms = new PoolPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        CustomDataSourceProperties customDataSourceProperties = new CustomDataSourceProperties(createProperties(),
                Arrays.asList("username", "password", "closed"), Collections.singletonList("closed"), Collections.singletonMap("username", "user"));
        when(dataSourceProperties.getConnectionPropertySynonyms()).thenReturn(connectionPropertySynonyms);
        when(dataSourceProperties.getPoolPropertySynonyms()).thenReturn(poolPropertySynonyms);
        when(dataSourceProperties.getCustomDataSourceProperties()).thenReturn(customDataSourceProperties);
        DataSourcePropertiesCreator.createConfiguration(dataSourceProperties);
    }
    
    private DataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName(MockedDataSource.class.getName());
        result.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new HashMap<>();
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("maximumPoolSize", "-1");
        return result;
    }
    
    private Map<String, Object> createStandardProperties() {
        Map<String, Object> result = new LinkedHashMap<>(6, 1F);
        result.put("connectionTimeoutMilliseconds", "null");
        result.put("idleTimeoutMilliseconds", 180000);
        result.put("maxLifetimeMilliseconds", 180000);
        result.put("maxPoolSize", 30);
        result.put("minPoolSize", 10);
        result.put("readOnly", false);
        return result;
    }
    
    private Map<String, String> createPropertySynonyms() {
        Map<String, String> result = new LinkedHashMap<>(5, 1F);
        result.put("connectionTimeoutMilliseconds", "connectionTimeout");
        result.put("idleTimeoutMilliseconds", "idleTimeout");
        result.put("maxLifetimeMilliseconds", "maxLifetime");
        result.put("maxPoolSize", "maximumPoolSize");
        result.put("minPoolSize", "minimumIdle");
        return result;
    }
}
