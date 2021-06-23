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

package org.apache.shardingsphere.scaling.core.util;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.junit.Test;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public final class JDBCUtilTest {
    
    @Test
    public void assertAppendStandardJDBCDataSourceConfiguration() {
        StandardJDBCDataSourceConfiguration dataSourceConfig = new StandardJDBCDataSourceConfiguration("jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false", null, null);
        JDBCUtil.appendJDBCParameter(dataSourceConfig, ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(dataSourceConfig.getHikariConfig().getJdbcUrl(), is("jdbc:mysql://192.168.0.1:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
    }
    
    @Test
    public void assertAppendShardingSphereJDBCDataSourceConfig() {
        ShardingSphereJDBCDataSourceConfiguration dataSourceConfig = new ShardingSphereJDBCDataSourceConfiguration(mockDataSource(), "");
        JDBCUtil.appendJDBCParameter(dataSourceConfig, ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        ArrayList<DataSourceConfiguration> actual = new ArrayList<>(getDataSourceConfigurations(dataSourceConfig.getRootRuleConfigs().getDataSources()).values());
        assertThat(actual.get(0).getProps().get("url"), is("jdbc:mysql://192.168.0.2:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
        assertThat(actual.get(1).getProps().get("url"), is("jdbc:mysql://192.168.0.1:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
    }
    
    private String mockDataSource() {
        return "dataSources:\n"
                + "  ds_1:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.2:3306/scaling?serverTimezone=UTC&useSSL=false\n"
                + "  ds_0:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    url: jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false\n";
    }
    
    private static Map<String, DataSourceConfiguration> getDataSourceConfigurations(final Map<String, Map<String, Object>> yamlDataSourceConfigs) {
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSourceConfigs.size());
        yamlDataSourceConfigs.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
}
