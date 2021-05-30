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

package org.apache.shardingsphere.integration.scaling.test.mysql.util;

import com.google.common.collect.ImmutableMap;
import lombok.SneakyThrows;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.integration.scaling.test.mysql.env.IntegrationTestEnvironment;
import org.apache.shardingsphere.sharding.yaml.config.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.yaml.config.rule.YamlTableRuleConfiguration;

import javax.sql.DataSource;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

/**
 * Source sharding sphere util.
 */
public final class SourceShardingSphereUtil {
    
    private static final String SOURCE_JDBC_URL = "jdbc:mysql://%s/ds_src?useSSL=false";
    
    private static final Properties ENGINE_ENV_PROPS = IntegrationTestEnvironment.getInstance().getEngineEnvProps();
    
    /**
     * Create docker sharding jdbc configurations.
     *
     * @return yaml root rule configurations
     */
    public static YamlRootRuleConfigurations createDockerConfigurations() {
        return createConfigurations(String.format(SOURCE_JDBC_URL, ENGINE_ENV_PROPS.getProperty("db.host.docker")));
    }
    
    /**
     * Create host sharding jdbc configurations.
     *
     * @return yaml root rule configurations
     */
    public static YamlRootRuleConfigurations createHostConfigurations() {
        return createConfigurations(String.format(SOURCE_JDBC_URL, ENGINE_ENV_PROPS.getProperty("db.host.host")));
    }
    
    private static YamlRootRuleConfigurations createConfigurations(final String jdbcUrl) {
        YamlRootRuleConfigurations result = new YamlRootRuleConfigurations();
        Map dataSources = ImmutableMap.builder().put("ds_src", ImmutableMap.builder()
                .put("dataSourceClassName", "com.zaxxer.hikari.HikariDataSource")
                .put("jdbcUrl", jdbcUrl)
                .put("username", ENGINE_ENV_PROPS.getProperty("db.username"))
                .put("password", ENGINE_ENV_PROPS.getProperty("db.password"))
                .build()).build();
        result.setDataSources(dataSources);
        YamlShardingRuleConfiguration shardingRuleConfiguration = new YamlShardingRuleConfiguration();
        shardingRuleConfiguration.setTables(createTableRules());
        result.setRules(Collections.singleton(shardingRuleConfiguration));
        return result;
    }
    
    private static Map<String, YamlTableRuleConfiguration> createTableRules() {
        Map<String, YamlTableRuleConfiguration> result = new HashMap<>();
        YamlTableRuleConfiguration t1TableRule = new YamlTableRuleConfiguration();
        t1TableRule.setLogicTable("t1");
        t1TableRule.setActualDataNodes("ds_src.t1");
        result.put("t1", t1TableRule);
        return result;
    }
    
    /**
     * Create host sharding jdbc data source.
     *
     * @return data source
     */
    @SneakyThrows
    public static DataSource createHostDataSource() {
        YamlRootRuleConfigurations configurations = createHostConfigurations();
        return new ShardingSphereDataSource(new YamlDataSourceConfigurationSwapper().swapToDataSources(configurations.getDataSources()),
                new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules()), null);
    }
}
