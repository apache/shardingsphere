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

package org.apache.shardingsphere.proxy.backend.text.distsql.rql;

import com.zaxxer.hikari.HikariDataSource;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceParameter;
import org.junit.Test;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;

public final class DataSourceQueryResultSetConverterTest {
    
    @Test
    public void assertGetDataSourceParameterMap() {
        Map<String, Object> props = new LinkedHashMap<>();
        props.put("maximumPoolSize", 10);
        props.put("connectionTimeout", "30000");
        props.put("readOnly", false);
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = new HashMap<>(2, 1);
        DataSourceConfiguration dataSourceConfiguration0 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration0.getProps().put("url", "jdbc:mysql://localhost:3306/demo_ds_0");
        dataSourceConfiguration0.getProps().putAll(props);
        dataSourceConfigurationMap.put("ds_0", dataSourceConfiguration0);
        DataSourceConfiguration dataSourceConfiguration1 = new DataSourceConfiguration(HikariDataSource.class.getName());
        dataSourceConfiguration1.getProps().put("jdbcUrl", "jdbc:mysql://localhost:3306/demo_ds_1");
        dataSourceConfiguration1.getProps().putAll(props);
        dataSourceConfigurationMap.put("ds_1", dataSourceConfiguration1);
        Map<String, DataSourceParameter> actual = DataSourceQueryResultSetConverter.covert(dataSourceConfigurationMap);
        assertThat(actual.size(), is(2));
        assertThat(actual.get("ds_0").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_0"));
        assertThat(actual.get("ds_0").getMaxPoolSize(), is(10));
        assertThat(actual.get("ds_0").getConnectionTimeoutMilliseconds(), is(30000L));
        assertFalse(actual.get("ds_1").isReadOnly());
        assertThat(actual.get("ds_1").getUrl(), is("jdbc:mysql://localhost:3306/demo_ds_1"));
        assertThat(actual.get("ds_1").getMaxPoolSize(), is(10));
        assertThat(actual.get("ds_1").getConnectionTimeoutMilliseconds(), is(30000L));
        assertFalse(actual.get("ds_1").isReadOnly());
    }
}
