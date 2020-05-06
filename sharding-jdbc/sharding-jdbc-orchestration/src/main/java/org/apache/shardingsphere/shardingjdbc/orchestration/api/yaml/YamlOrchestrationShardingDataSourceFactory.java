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

package org.apache.shardingsphere.shardingjdbc.orchestration.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.core.yaml.swapper.root.RuleRootConfigurationsYamlSwapper;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;
import org.apache.shardingsphere.shardingjdbc.jdbc.core.datasource.ShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.datasource.OrchestrationShardingDataSource;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.util.YamlCenterRepositoryConfigurationSwapperUtil;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.yaml.YamlOrchestrationRootRuleConfigurations;
import org.apache.shardingsphere.shardingjdbc.orchestration.internal.yaml.constructor.YamlOrchestrationShardingRuleConfigurationConstructor;
import org.apache.shardingsphere.underlying.common.rule.ShardingSphereRulesBuilder;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration sharding data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationShardingDataSourceFactory {
    
    private static final RuleRootConfigurationsYamlSwapper SWAPPER = new RuleRootConfigurationsYamlSwapper();
    
    /**
     * Create sharding data source.
     *
     * @param yamlFile YAML file for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getOrchestration());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, configurations, configurations.getProps(), configurations.getOrchestration());
    }
    
    /**
     * Create sharding data source.
     *
     * @param yamlBytes YAML bytes for rule configuration of databases and tables sharding with data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getOrchestration());
    }
    
    /**
     * Create sharding data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes YAML bytes for rule configuration of databases and tables sharding without data sources
     * @return sharding data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(dataSourceMap, configurations, configurations.getProps(), configurations.getOrchestration());
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlOrchestrationRootRuleConfigurations configurations, 
                                               final Properties props, final Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap) throws SQLException {
        if (null == configurations) {
            return new OrchestrationShardingDataSource(new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)));
        } else {
            ShardingDataSource shardingDataSource = new ShardingDataSource(
                    dataSourceMap, ShardingSphereRulesBuilder.build(SWAPPER.swap(configurations), dataSourceMap.keySet()), props);
            return new OrchestrationShardingDataSource(shardingDataSource, new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)));
        }
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlOrchestrationRootRuleConfigurations.class, new YamlOrchestrationShardingRuleConfigurationConstructor());
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlOrchestrationRootRuleConfigurations.class, new YamlOrchestrationShardingRuleConfigurationConstructor());
    }
}
