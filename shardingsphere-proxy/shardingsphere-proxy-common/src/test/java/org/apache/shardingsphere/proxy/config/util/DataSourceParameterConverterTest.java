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
import org.apache.shardingsphere.proxy.config.yaml.YamlDataSourceParameter;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class DataSourceParameterConverterTest {
    
    @Test
    public void assertGetDataSourceParameterMap() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = new HashMap<>(2, 1);
        DataSourceConfiguration dataSourceConfiguration0 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration0.getProps().put("url", "jdbc:mysql://localhost:3306/demo_ds_0");
        dataSourceConfigurationMap.put("ds_0", dataSourceConfiguration0);
        DataSourceConfiguration dataSourceConfiguration1 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration1.getProps().put("jdbcUrl", "jdbc:mysql://localhost:3306/demo_ds_1");
        dataSourceConfigurationMap.put("ds_1", dataSourceConfiguration1);
        Map<String, DataSourceParameter> actual = DataSourceParameterConverter.getDataSourceParameterMap(dataSourceConfigurationMap);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("ds_0").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_0"));
        assertThat(actual.get("ds_1").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_1"));
    }
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, DataSourceParameter> dataSourceParameterMap = new HashMap<>(2, 1);
        dataSourceParameterMap.put("ds_0", crateDataSourceParameter());
        dataSourceParameterMap.put("ds_1", crateDataSourceParameter());
        Map<String, DataSourceConfiguration> actual = DataSourceParameterConverter.getDataSourceConfigurationMap(dataSourceParameterMap);
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
    
    @Test
    public void assertGetDataSourceParameterMapFromYamlConfiguration() {
        Map<String, YamlDataSourceParameter> yamlDataSourceParameterMap = new HashMap<>();
        YamlDataSourceParameter yamlDataSourceParameter0 = new YamlDataSourceParameter();
        yamlDataSourceParameter0.setUrl("jdbc:mysql://localhost:3306/t_order");
        setYamlDataSourceParameterPropertyWithoutUrl(yamlDataSourceParameter0);
        yamlDataSourceParameterMap.put("ds_0", yamlDataSourceParameter0);
        YamlDataSourceParameter yamlDataSourceParameter1 = new YamlDataSourceParameter();
        yamlDataSourceParameter1.setUrl("jdbc:mysql://localhost:3306/t_order_item");
        setYamlDataSourceParameterPropertyWithoutUrl(yamlDataSourceParameter1);
        yamlDataSourceParameterMap.put("ds_1", yamlDataSourceParameter1);
        Map<String, DataSourceParameter> actualDataSourceParameterMap = DataSourceParameterConverter.getDataSourceParameterMapFromYamlConfiguration(yamlDataSourceParameterMap);
        assertThat(actualDataSourceParameterMap.size(), is(2));
        assertThat(actualDataSourceParameterMap.get("ds_0").getUrl(), is("jdbc:mysql://localhost:3306/t_order"));
        assertThat(actualDataSourceParameterMap.get("ds_1").getUrl(), is("jdbc:mysql://localhost:3306/t_order_item"));
        assertDataSourceParameter(actualDataSourceParameterMap.get("ds_0"));
        assertDataSourceParameter(actualDataSourceParameterMap.get("ds_1"));
    }
    
    private void setYamlDataSourceParameterPropertyWithoutUrl(final YamlDataSourceParameter yamlDataSourceParameter) {
        yamlDataSourceParameter.setMaxPoolSize(50);
        yamlDataSourceParameter.setMinPoolSize(1);
        yamlDataSourceParameter.setConnectionTimeoutMilliseconds(30 * 1000L);
        yamlDataSourceParameter.setIdleTimeoutMilliseconds(60 * 1000L);
        yamlDataSourceParameter.setMaxLifetimeMilliseconds(0L);
        yamlDataSourceParameter.setMaintenanceIntervalMilliseconds(30 * 1000L);
        yamlDataSourceParameter.setUsername("root");
        yamlDataSourceParameter.setPassword("root");
    }
    
    private void assertDataSourceParameter(final DataSourceParameter dataSourceParameter) {
        assertThat(dataSourceParameter.getMaxPoolSize(), is(50));
        assertThat(dataSourceParameter.getMinPoolSize(), is(1));
        assertThat(dataSourceParameter.getConnectionTimeoutMilliseconds(), is(30 * 1000L));
        assertThat(dataSourceParameter.getIdleTimeoutMilliseconds(), is(60 * 1000L));
        assertThat(dataSourceParameter.getMaxLifetimeMilliseconds(), is(0L));
        assertThat(dataSourceParameter.getMaintenanceIntervalMilliseconds(), is(30 * 1000L));
        assertThat(dataSourceParameter.getUsername(), is("root"));
        assertThat(dataSourceParameter.getPassword(), is("root"));
    }
}
