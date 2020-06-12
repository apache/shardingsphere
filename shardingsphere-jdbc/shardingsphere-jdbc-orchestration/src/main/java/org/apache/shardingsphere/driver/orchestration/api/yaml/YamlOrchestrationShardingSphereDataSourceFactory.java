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

package org.apache.shardingsphere.driver.orchestration.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.driver.jdbc.core.datasource.ShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.util.YamlCenterRepositoryConfigurationSwapperUtil;
import org.apache.shardingsphere.driver.orchestration.internal.yaml.YamlOrchestrationRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.orchestration.center.config.OrchestrationConfiguration;
import org.apache.shardingsphere.orchestration.center.yaml.config.YamlCenterRepositoryConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Map;
import java.util.Properties;

/**
 * Orchestration ShardingSphere data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlOrchestrationShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getOrchestration(), configurations.getCluster());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, configurations, configurations.getProps(), configurations.getOrchestration(), configurations.getCluster());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getOrchestration(), configurations.getCluster());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceMap data source map
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlOrchestrationRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(dataSourceMap, configurations, configurations.getProps(), configurations.getOrchestration(), configurations.getCluster());
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlOrchestrationRootRuleConfigurations configurations,
                                               final Properties props, final Map<String, YamlCenterRepositoryConfiguration> yamlInstanceConfigurationMap,
                                               final YamlClusterConfiguration yamlClusterConfiguration) throws SQLException {
        if (configurations.getRules().isEmpty() || dataSourceMap.isEmpty()) {
            return null == yamlClusterConfiguration
                    ? new OrchestrationShardingSphereDataSource(new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)))
                    : new OrchestrationShardingSphereDataSource(new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)),
                    new ClusterConfigurationYamlSwapper().swap(yamlClusterConfiguration));
        } else {
            ShardingSphereDataSource shardingSphereDataSource = new ShardingSphereDataSource(dataSourceMap,
                    new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules()), props);
            return null == yamlClusterConfiguration ? new OrchestrationShardingSphereDataSource(shardingSphereDataSource,
                    new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)))
                    : new OrchestrationShardingSphereDataSource(shardingSphereDataSource,
                    new OrchestrationConfiguration(YamlCenterRepositoryConfigurationSwapperUtil.marshal(yamlInstanceConfigurationMap)),
                    new ClusterConfigurationYamlSwapper().swap(yamlClusterConfiguration));
        }
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlOrchestrationRootRuleConfigurations.class);
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlOrchestrationRootRuleConfigurations.class);
    }
}
