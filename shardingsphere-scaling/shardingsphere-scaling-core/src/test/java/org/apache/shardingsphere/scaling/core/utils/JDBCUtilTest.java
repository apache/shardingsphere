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

package org.apache.shardingsphere.scaling.core.utils;

import com.google.common.collect.ImmutableMap;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.scaling.core.config.datasource.StandardJDBCDataSourceConfiguration;
import org.junit.Test;

import java.util.ArrayList;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class JDBCUtilTest {
    
    @Test
    public void assertAppendStandardJDBCDataSourceConfiguration() {
        StandardJDBCDataSourceConfiguration dataSourceConfig = new StandardJDBCDataSourceConfiguration("jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false", null, null);
        JDBCUtil.appendJDBCParameter(dataSourceConfig, ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        assertThat(dataSourceConfig.getJdbcUrl(), is("jdbc:mysql://192.168.0.1:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
    }
    
    @Test
    public void assertAppendShardingSphereJDBCDataSourceConfig() {
        ShardingSphereJDBCDataSourceConfiguration dataSourceConfig = new ShardingSphereJDBCDataSourceConfiguration(mockDataSource(), null);
        JDBCUtil.appendJDBCParameter(dataSourceConfig, ImmutableMap.<String, String>builder().put("rewriteBatchedStatements", "true").build());
        ArrayList<DataSourceConfiguration> actual = new ArrayList<>(ConfigurationYamlConverter.loadDataSourceConfigs(dataSourceConfig.getDataSource()).values());
        assertThat(actual.get(0).getProps().get("url"), is("jdbc:mysql://192.168.0.2:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
        assertThat(actual.get(1).getProps().get("url"), is("jdbc:mysql://192.168.0.1:3306/scaling?rewriteBatchedStatements=true&serverTimezone=UTC&useSSL=false"));
    }
    
    private String mockDataSource() {
        return "dataSources:\n"
                + "  ds_1:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    props:\n"
                + "      url: jdbc:mysql://192.168.0.2:3306/scaling?serverTimezone=UTC&useSSL=false\n"
                + "  ds_0:\n"
                + "    dataSourceClassName: com.zaxxer.hikari.HikariDataSource\n"
                + "    props:\n"
                + "      url: jdbc:mysql://192.168.0.1:3306/scaling?serverTimezone=UTC&useSSL=false\n";
    }
}
