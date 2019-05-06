/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.shardingproxy.util;

import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.DataSourceParameter;
import org.hamcrest.CoreMatchers;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public final class DataSourceConverterTest {
    
    @Test
    public void assertGetDataSourceParameterMap() {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = new HashMap<>(2, 1);
        DataSourceConfiguration dataSourceConfiguration0 = mock(DataSourceConfiguration.class);
        DataSourceParameter dataSourceParameter0 = new DataSourceParameter();
        when(dataSourceConfiguration0.createDataSourceParameter()).thenReturn(dataSourceParameter0);
        dataSourceConfigurationMap.put("ds_0", dataSourceConfiguration0);
        DataSourceConfiguration dataSourceConfiguration1 = mock(DataSourceConfiguration.class);
        DataSourceParameter dataSourceParameter1 = new DataSourceParameter();
        when(dataSourceConfiguration1.createDataSourceParameter()).thenReturn(dataSourceParameter1);
        dataSourceConfigurationMap.put("ds_1", dataSourceConfiguration1);
        Map<String, DataSourceParameter> actual = DataSourceConverter.getDataSourceParameterMap(dataSourceConfigurationMap);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("ds_0"), is(dataSourceParameter0));
        assertThat(actual.get("ds_1"), is(dataSourceParameter1));
    }
    
    @Test
    public void assertGetDataSourceConfigurationMap() {
        Map<String, DataSourceParameter> dataSourceParameterMap = new HashMap<>(2, 1);
        dataSourceParameterMap.put("ds_0", crateDataSourceParameter());
        dataSourceParameterMap.put("ds_1", crateDataSourceParameter());
        Map<String, DataSourceConfiguration> actual = DataSourceConverter.getDataSourceConfigurationMap(dataSourceParameterMap);
        assertThat(actual.size(), is(2));
        assertNotNull(actual.get("ds_0"));
        assertNotNull(actual.get("ds_1"));
        assertThatParameter(actual.get("ds_0"));
        assertThatParameter(actual.get("ds_1"));
    }
    
    private DataSourceParameter crateDataSourceParameter() {
        DataSourceParameter result = new DataSourceParameter();
        result.setUsername("root");
        result.setUrl("jdbc:mysql://localhost:3306/demo_ds");
        result.setPassword("root");
        return result;
    }
    
    private void assertThatParameter(final DataSourceConfiguration actual) {
        Map<String, Object> properties = actual.getProperties();
        assertThat(properties.get("maxPoolSize"), CoreMatchers.<Object>is(50));
        assertThat(properties.get("minPoolSize"), CoreMatchers.<Object>is(1));
        assertThat(properties.get("connectionTimeoutMilliseconds"), CoreMatchers.<Object>is(30 * 1000L));
        assertThat(properties.get("idleTimeoutMilliseconds"), CoreMatchers.<Object>is(60 * 1000L));
        assertThat(properties.get("maxLifetimeMilliseconds"), CoreMatchers.<Object>is(0L));
        assertThat(properties.get("maintenanceIntervalMilliseconds"), CoreMatchers.<Object>is(30 * 1000L));
        assertThat(properties.get("url"), CoreMatchers.<Object>is("jdbc:mysql://localhost:3306/demo_ds"));
        assertThat(properties.get("username"), CoreMatchers.<Object>is("root"));
        assertThat(properties.get("password"), CoreMatchers.<Object>is("root"));
    }
}
