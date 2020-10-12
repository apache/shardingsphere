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

package org.apache.shardingsphere.driver.governance.api.yaml;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import org.apache.shardingsphere.driver.api.ShardingSphereDataSourceFactory;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.driver.governance.internal.util.YamlGovernanceRepositoryConfigurationSwapperUtil;
import org.apache.shardingsphere.driver.governance.internal.yaml.YamlGovernanceRootRuleConfigurations;
import org.apache.shardingsphere.governance.core.yaml.config.YamlGovernanceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import javax.sql.DataSource;
import java.io.File;
import java.io.IOException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Governance ShardingSphere data source factory for YAML.
 */
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class YamlGovernanceShardingSphereDataSourceFactory {
    
    /**
     * Create ShardingSphere data source.
     *
     * @param yamlFile YAML file for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final File yamlFile) throws SQLException, IOException {
        YamlGovernanceRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getGovernance());
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
    public static DataSource createDataSource(final Map<String, Map<String, Object>> dataSourceMap, final File yamlFile) throws SQLException, IOException {
        YamlGovernanceRootRuleConfigurations configurations = unmarshal(yamlFile);
        return createDataSource(dataSourceMap, configurations, configurations.getProps(), configurations.getGovernance());
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
        YamlGovernanceRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(configurations.getDataSources(), configurations, configurations.getProps(), configurations.getGovernance());
    }
    
    /**
     * Create ShardingSphere data source.
     *
     * @param dataSourceConfigMap data source configuration properties map
     * @param yamlBytes YAML bytes for rule configurations
     * @return ShardingSphere data source
     * @throws SQLException SQL exception
     * @throws IOException IO exception
     */
    public static DataSource createDataSource(final Map<String, Map<String, Object>> dataSourceConfigMap, final byte[] yamlBytes) throws SQLException, IOException {
        YamlGovernanceRootRuleConfigurations configurations = unmarshal(yamlBytes);
        return createDataSource(dataSourceConfigMap, configurations, configurations.getProps(), configurations.getGovernance());
    }
    
    private static DataSource createDataSource(final Map<String, Map<String, Object>> dataSourceConfigMap, final YamlGovernanceRootRuleConfigurations configurations,
                                               final Properties props, final YamlGovernanceConfiguration governance) throws SQLException {
        if (configurations.getRules().isEmpty() || dataSourceConfigMap.isEmpty()) {
            return createDataSourceWithoutRules(governance);
        } else {
            return createDataSourceWithRules(dataSourceConfigMap, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configurations.getRules()),
                    props, governance);
        }
    }
    
    private static DataSource createDataSourceWithoutRules(final YamlGovernanceConfiguration governance) throws SQLException {
        return new GovernanceShardingSphereDataSource(YamlGovernanceRepositoryConfigurationSwapperUtil.marshal(governance));
    }
    
    private static DataSource createDataSourceWithRules(final Map<String, Map<String, Object>> dataSourceConfigMap, final Collection<RuleConfiguration> ruleConfigurations,
                                                        final Properties props, final YamlGovernanceConfiguration governance) throws SQLException {
        Map<String, DataSource> dataSourceMap = ShardingSphereDataSourceFactory.fromDataSourceConfig(dataSourceConfigMap);
        return new GovernanceShardingSphereDataSource(dataSourceMap, ruleConfigurations, props, 
                YamlGovernanceRepositoryConfigurationSwapperUtil.marshal(governance));
    }
    
    private static YamlGovernanceRootRuleConfigurations unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlGovernanceRootRuleConfigurations.class);
    }
    
    private static YamlGovernanceRootRuleConfigurations unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlGovernanceRootRuleConfigurations.class);
    }
}
