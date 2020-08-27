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
import org.apache.shardingsphere.driver.orchestration.internal.datasource.OrchestrationShardingSphereDataSource;
import org.apache.shardingsphere.driver.orchestration.internal.util.YamlOrchestrationRepositoryConfigurationSwapperUtil;
import org.apache.shardingsphere.driver.orchestration.internal.yaml.YamlOrchestrationRootRuleConfigurations;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlOrchestrationConfiguration;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
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
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(),
                configurations.getOrchestration(), configurations.getMetrics());
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
        return createDataSource(dataSourceMap, configurations, configurations.getProps(),
                configurations.getOrchestration(), configurations.getMetrics());
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
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(),
                configurations.getOrchestration(), configurations.getMetrics());
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
        return createDataSource(dataSourceMap, configurations, configurations.getProps(),
                configurations.getOrchestration(), configurations.getMetrics());
    }
    
    private static DataSource createDataSource(final Map<String, DataSource> dataSourceMap, final YamlOrchestrationRootRuleConfigurations configurations,
                                               final Properties props, final YamlOrchestrationConfiguration orchestration,
                                               final YamlMetricsConfiguration yamlMetricsConfiguration) throws SQLException {
        if (configurations.getRules().isEmpty() || dataSourceMap.isEmpty()) {
            return createDataSourceWithoutRules(orchestration, yamlMetricsConfiguration);
        } else {
            return createDataSourceWithRules(dataSourceMap, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules()),
                    props, orchestration, yamlMetricsConfiguration);
        }
    }
    
    private static DataSource createDataSourceWithoutRules(final YamlOrchestrationConfiguration orchestration,
                                                           final YamlMetricsConfiguration yamlMetricsConfiguration) throws SQLException {
        if (null == yamlMetricsConfiguration) {
            return new OrchestrationShardingSphereDataSource(YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(orchestration));
        } else {
            MetricsConfiguration metricsConfiguration = swap(yamlMetricsConfiguration);
            return new OrchestrationShardingSphereDataSource(YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(orchestration), metricsConfiguration);
        }
    }
    
    private static DataSource createDataSourceWithRules(final Map<String, DataSource> dataSourceMap, final Collection<RuleConfiguration> ruleConfigurations,
                                                        final Properties props, final YamlOrchestrationConfiguration orchestration,
                                                        final YamlMetricsConfiguration yamlMetricsConfiguration) throws SQLException {
        if (null == yamlMetricsConfiguration) {
            return new OrchestrationShardingSphereDataSource(dataSourceMap, ruleConfigurations, props,
                    YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(orchestration));
        } else {
            MetricsConfiguration metricsConfiguration = swap(yamlMetricsConfiguration);
            return new OrchestrationShardingSphereDataSource(dataSourceMap, ruleConfigurations, props,
                    YamlOrchestrationRepositoryConfigurationSwapperUtil.marshal(orchestration), metricsConfiguration);
        }
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlOrchestrationRootRuleConfigurations.class);
    }
    
    private static YamlOrchestrationRootRuleConfigurations unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlOrchestrationRootRuleConfigurations.class);
    }
    
    private static MetricsConfiguration swap(final YamlMetricsConfiguration yamlMetricsConfiguration) {
        return null == yamlMetricsConfiguration ? null : new MetricsConfigurationYamlSwapper().swapToObject(yamlMetricsConfiguration);
    }
}
