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

package org.apache.shardingsphere.proxy.config.util;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.context.schema.DataSourceParameter;
import org.junit.Test;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class DataSourceConverterTest {
    
    @Test
    public void assertGetDataSourceParameterMap() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = new HashMap<>(2, 1);
        DataSourceConfiguration dataSourceConfiguration0 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration0.getProps().put("url", "jdbc:mysql://localhost:3306/demo_ds_0");
        dataSourceConfigurationMap.put("ds_0", dataSourceConfiguration0);
        DataSourceConfiguration dataSourceConfiguration1 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration1.getProps().put("jdbcUrl", "jdbc:mysql://localhost:3306/demo_ds_1");
        dataSourceConfigurationMap.put("ds_1", dataSourceConfiguration1);
        Map<String, DataSourceParameter> actual = DataSourceConverter.getDataSourceParameterMap(dataSourceConfigurationMap);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("ds_0").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_0"));
        assertThat(actual.get("ds_1").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_1"));
    }
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, DataSourceParameter> dataSourceParameterMap = new HashMap<>(2, 1);
        dataSourceParameterMap.put("ds_0", crateDataSourceParameter());
        dataSourceParameterMap.put("ds_1", crateDataSourceParameter());
        Map<String, DataSourceConfiguration> actual = DataSourceConverter.getDataSourceConfigurationMap(dataSourceParameterMap);
        assertThat(actual.size(), is(2));
        assertParameter(actual.get("ds_0"));
        assertParameter(actual.get("ds_1"));
    }
    
    private DataSourceParameter crateDataSourceParameter() {
        DataSourceParameter result = new DataSourceParameter();
        result.setUrl("jdbc:mysql://localhost:3306/demo_ds");
        result.setUsername("root");
        result.setPassword("root");
        return result;
    }
    
    private void assertParameter(final DataSourceConfiguration actual) {
        Map<String, Object> props = actual.getProps();
        assertThat(props.get("maxPoolSize"), is(50));
        assertThat(props.get("minPoolSize"), is(1));
        assertThat(props.get("connectionTimeout"), is(30 * 1000L));
        assertThat(props.get("idleTimeout"), is(60 * 1000L));
        assertThat(props.get("maxLifetime"), is(0L));
        assertThat(props.get("maintenanceIntervalMilliseconds"), is(30 * 1000L));
        assertThat(props.get("jdbcUrl"), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(props.get("username"), is("root"));
        assertThat(props.get("password"), is("root"));
    }
    
    private DataSource createDataSource() {
        HikariDataSource hikariDataSource = new HikariDataSource();
        hikariDataSource.setJdbcUrl("jdbc:mysql://localhost:3306/demo_ds");
        hikariDataSource.setUsername("root");
        hikariDataSource.setPassword("root");
        hikariDataSource.setConnectionTimeout(30 * 1000L);
        hikariDataSource.setIdleTimeout(60 * 1000L);
        hikariDataSource.setMaxLifetime(0L);
        hikariDataSource.setMaximumPoolSize(50);
        hikariDataSource.setMinimumIdle(1);
        hikariDataSource.setReadOnly(true);
        return hikariDataSource;
    }
    
    private void assertDataSourceParameter(final DataSourceParameter actual) {
        assertNotNull(actual);
        assertThat(actual.getUrl(), is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(actual.getUsername(), is("root"));
        assertThat(actual.getPassword(), is("root"));
        assertThat(actual.getConnectionTimeoutMilliseconds(), is(30 * 1000L));
        assertThat(actual.getIdleTimeoutMilliseconds(), is(60 * 1000L));
        assertThat(actual.getMaxLifetimeMilliseconds(), is(0L));
        assertThat(actual.getMaintenanceIntervalMilliseconds(), is(30 * 1000L));
        assertThat(actual.getMaxPoolSize(), is(50));
        assertThat(actual.getMinPoolSize(), is(1));
        assertTrue(actual.isReadOnly());
    }
}
