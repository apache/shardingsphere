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

package org.apache.shardingsphere.integration.scaling.test.mysql.env.config;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.scaling.core.config.datasource.ShardingSphereJDBCDataSourceConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import javax.sql.DataSource;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Map;
import java.util.Properties;

/**
 * Source sharding jdbc configuration.
 */
public final class SourceConfiguration {
    
    private static final String SOURCE_JDBC_URL = "jdbc:mysql://%s/ds_src?useSSL=false";
    
    private static final Properties ENGINE_ENV_PROPS = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
    
    /**
     * Get docker sharding jdbc configuration.
     *
     * @param tableRules table rules
     * @return sharding jdbc configuration
     */
    public static ShardingSphereJDBCDataSourceConfiguration getDockerConfiguration(final Map<String, YamlTableRuleConfiguration> tableRules) {
        return getConfiguration(String.format(SOURCE_JDBC_URL, ENGINE_ENV_PROPS.getProperty("db.host.docker")), tableRules);
    }
    
    /**
     * Get host sharding jdbc configuration.
     *
     * @param tableRules table rules
     * @return sharding jdbc configuration
     */
    public static ShardingSphereJDBCDataSourceConfiguration getHostConfiguration(final Map<String, YamlTableRuleConfiguration> tableRules) {
        return getConfiguration(String.format(SOURCE_JDBC_URL, ENGINE_ENV_PROPS.getProperty("db.host.host")), tableRules);
    }
    
    private static ShardingSphereJDBCDataSourceConfiguration getConfiguration(final String jdbcUrl, final Map<String, YamlTableRuleConfiguration> tableRules) {
        YamlRootRuleConfigurations shardingSphereConfigurations = getShardingJdbcConfiguration(jdbcUrl, tableRules);
        return new ShardingSphereJDBCDataSourceConfiguration(YamlEngine.marshal(shardingSphereConfigurations));
    }
    
    private static YamlRootRuleConfigurations getShardingJdbcConfiguration(final String jdbcUrl, final Map<String, YamlTableRuleConfiguration> tableRules) {
        YamlRootRuleConfigurations result = new YamlRootRuleConfigurations();
        Map dataSources = ImmutableMap.builder().put("ds_src", ImmutableMap.builder()
                .put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource")
                .put("jdbcUrl", jdbcUrl)
                .put("username", ENGINE_ENV_PROPS.getProperty("db.username"))
                .put("password", ENGINE_ENV_PROPS.getProperty("db.password"))
                .build()).build();
        result.setDataSources(dataSources);
        YamlShardingRuleConfiguration shardingRuleConfiguration = new YamlShardingRuleConfiguration();
        shardingRuleConfiguration.setTables(tableRules);
        result.setRules(Collections.singleton(shardingRuleConfiguration));
        return result;
    }
    
    /**
     * Create host sharding jdbc data source.
     *
     * @param tableRules table rules
     * @return data source
     */
    @SneakyThrows(SQLException.class)
    public static DataSource createHostDataSource(final Map<String, YamlTableRuleConfiguration> tableRules) {
        ShardingSphereJDBCDataSourceConfiguration configuration = getHostConfiguration(tableRules);
        return new ShardingSphereDataSource(new YamlDataSourceConfigurationSwapper().swapToDataSources(configuration.getRootRuleConfigs().getDataSources()),
                new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configuration.getRootRuleConfigs().getRules()), null);
    }
}
