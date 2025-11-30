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

package org.apache.shardingsphere.infra.datasource.pool.props.creator;

import org.apache.shardingsphere.infra.datasource.pool.config.ConnectionConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.config.PoolConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.custom.CustomDataSourcePoolProperties;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.ConnectionPropertySynonyms;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.synonym.PoolPropertySynonyms;
import org.apache.shardingsphere.test.infra.fixture.jdbc.MockedDataSource;
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

class DataSourcePoolPropertiesCreatorTest {
    
    @Test
    void assertCreateWithDataSourceConfiguration() {
        assertParameter(DataSourcePoolPropertiesCreator.create(createDataSourceConfiguration()));
    }
    
    private DataSourceConfiguration createDataSourceConfiguration() {
        ConnectionConfiguration connectionConfig = new ConnectionConfiguration(MockedDataSource.class.getName(), "jdbc:mock://127.0.0.1/foo_ds", "root", "root");
        PoolConfiguration poolConfig = new PoolConfiguration(null, null, null, null, null, null, null);
        return new DataSourceConfiguration(connectionConfig, poolConfig);
    }
    
    private void assertParameter(final DataSourcePoolProperties actual) {
        Map<String, Object> props = actual.getAllLocalProperties();
        assertThat(props.size(), is(10));
        assertThat(props.get("dataSourceClassName"), is(MockedDataSource.class.getName()));
        assertThat(props.get("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
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
        assertThat(DataSourcePoolPropertiesCreator.create(createDataSource()), is(new DataSourcePoolProperties(MockedDataSource.class.getName(), createProperties())));
    }
    
    private DataSource createDataSource() {
        MockedDataSource result = new MockedDataSource();
        result.setDriverClassName(MockedDataSource.class.getName());
        result.setUrl("jdbc:mock://127.0.0.1/foo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    @Test
    void assertCreateConfiguration() {
        DataSourcePoolProperties props = mock(DataSourcePoolProperties.class);
        ConnectionPropertySynonyms connectionPropSynonyms = new ConnectionPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        PoolPropertySynonyms poolPropSynonyms = new PoolPropertySynonyms(createStandardProperties(), createPropertySynonyms());
        CustomDataSourcePoolProperties customProps = new CustomDataSourcePoolProperties(createProperties(),
                Arrays.asList("username", "password", "closed"), Collections.singletonList("closed"), Collections.singletonMap("username", "user"));
        when(props.getConnectionPropertySynonyms()).thenReturn(connectionPropSynonyms);
        when(props.getPoolPropertySynonyms()).thenReturn(poolPropSynonyms);
        when(props.getCustomProperties()).thenReturn(customProps);
        assertPoolConfiguration(DataSourcePoolPropertiesCreator.createConfiguration(props).getPool());
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
    
    private Map<String, Object> createProperties() {
        Map<String, Object> result = new HashMap<>(5, 1F);
        result.put("driverClassName", MockedDataSource.class.getName());
        result.put("url", "jdbc:mock://127.0.0.1/foo_ds");
        result.put("username", "root");
        result.put("password", "root");
        result.put("maximumPoolSize", "-1");
        return result;
    }
    
    private static void assertPoolConfiguration(final PoolConfiguration actual) {
        assertThat(actual.getIdleTimeoutMilliseconds(), is(180000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(180000L));
        assertThat(actual.getMaxPoolSize(), is(30));
        assertThat(actual.getMinPoolSize(), is(10));
        assertThat(actual.getCustomProperties().getProperty("driverClassName"), is(MockedDataSource.class.getName()));
        assertThat(actual.getCustomProperties().getProperty("url"), is("jdbc:mock://127.0.0.1/foo_ds"));
        assertThat(actual.getCustomProperties().getProperty("maximumPoolSize"), is("-1"));
    }
}
