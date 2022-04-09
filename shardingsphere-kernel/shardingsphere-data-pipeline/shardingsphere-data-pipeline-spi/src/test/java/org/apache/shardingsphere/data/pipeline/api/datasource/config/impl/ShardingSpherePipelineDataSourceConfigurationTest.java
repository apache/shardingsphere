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

import org.apache.shardingsphere.infra.datasource.props.DataSourceProperties;
import org.apache.shardingsphere.infra.datasource.props.InvalidDataSourcePropertiesException;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

public final class ShardingSpherePipelineDataSourceConfigurationTest {
    
    @Test
    public void assertAppendJDBCParameters() {
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(getDataSourceYaml());
        Properties queryProps = new Properties();
        queryProps.setProperty("rewriteBatchedStatements", Boolean.TRUE.toString());
        dataSourceConfig.appendJDBCQueryProperties(queryProps);
        List<DataSourceProperties> actual = new ArrayList<>(getDataSourcePropertiesMap(dataSourceConfig.getRootConfig().getDataSources()).values());
        assertThat(actual.get(0).getAllLocalProperties().get("jdbcUrl"), is("jdbc:mysql://192.168.0.2:3306/scaling?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true"));
        assertThat(actual.get(1).getAllLocalProperties().get("jdbcUrl"), is("jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false&rewriteBatchedStatements=true"));
    }
    
    private String getDataSourceYaml() {
        return "dataSources:\n"
                + "  ds_1:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.2:3306/scaling?serverTimezone=UTC&useSSL=false\n"
                + "  ds_0:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false\n";
    }
    
    private static Map<String, DataSourceProperties> getDataSourcePropertiesMap(final Map<String, Map<String, Object>> yamlDataSourceConfigs) {
        Map<String, DataSourceProperties> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceProperties(value)));
        return result;
    }

    @Test
    public void assertDataSourceCanBeAggregation() throws InvalidDataSourcePropertiesException {
        ShardingSpherePipelineDataSourceConfiguration dataSourceConfig = new ShardingSpherePipelineDataSourceConfiguration(getDataSourceAggregationYaml());
        List<DataSourceProperties> actual = new ArrayList<>(getDataSourcePropertiesMap(dataSourceConfig.getRootConfig().getDataSources()).values());
        assertTrue(actual.get(0).isInSameDatabaseInstance(actual.get(0).getAllLocalProperties().get("jdbcUrl").toString(), actual.get(1).getAllLocalProperties().get("jdbcUrl").toString()));
        actual.get(0).checkToBeAggregatedDataSources(actual.get(1));
    }

    private String getDataSourceAggregationYaml() {
        return "dataSources:\n"
                + "  ds_1:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false\n"
                + "    username: root\n"
                + "    password:\n"
                + "    connectionTimeoutMilliseconds: 30000\n"
                + "  ds_0:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.1:3306/test?serverTimezone=UTC&useSSL=false\n"
                + "    username: root\n"
                + "    password: \n"
                + "    connectionTimeoutMilliseconds: 30000\n";
    }
}
