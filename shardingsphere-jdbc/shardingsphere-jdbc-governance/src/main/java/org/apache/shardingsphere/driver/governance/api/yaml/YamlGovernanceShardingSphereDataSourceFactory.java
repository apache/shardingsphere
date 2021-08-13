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

import com.google.common.base.Strings;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.apache.shardingsphere.driver.governance.internal.datasource.GovernanceShardingSphereDataSource;
import org.apache.shardingsphere.driver.governance.internal.util.YamlGovernanceConfigurationSwapperUtil;
import org.apache.shardingsphere.driver.governance.internal.yaml.YamlGovernanceRootRuleConfigurations;
import org.apache.shardingsphere.governance.core.yaml.pojo.YamlGovernanceConfiguration;
import org.apache.shardingsphere.governance.repository.api.config.GovernanceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;

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
        YamlGovernanceRootRuleConfigurations configs = unmarshal(yamlFile);
        return createDataSource(configs.getSchemaName(), new YamlDataSourceConfigurationSwapper().swapToDataSources(configs.getDataSources()), configs, configs.getProps(), configs.getGovernance());
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
        YamlGovernanceRootRuleConfigurations configs = unmarshal(yamlFile);
        return createDataSource(configs.getSchemaName(), dataSourceMap, configs, configs.getProps(), configs.getGovernance());
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
        YamlGovernanceRootRuleConfigurations configs = unmarshal(yamlBytes);
        return createDataSource(configs.getSchemaName(), new YamlDataSourceConfigurationSwapper().swapToDataSources(configs.getDataSources()), configs, configs.getProps(), configs.getGovernance());
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
        YamlGovernanceRootRuleConfigurations configs = unmarshal(yamlBytes);
        return createDataSource(configs.getSchemaName(), dataSourceMap, configs, configs.getProps(), configs.getGovernance());
    }
    
    private static DataSource createDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap, final YamlGovernanceRootRuleConfigurations configs,
                                               final Properties props, final YamlGovernanceConfiguration governance) throws SQLException {
        if (configs.getRules().isEmpty() || dataSourceMap.isEmpty()) {
            return createDataSourceWithoutRules(schemaName, governance);
        } else {
            return createDataSourceWithRules(schemaName, dataSourceMap, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(configs.getRules()), props, governance);
        }
    }
    
    private static DataSource createDataSourceWithoutRules(final String schemaName, final YamlGovernanceConfiguration governance) throws SQLException {
        GovernanceConfiguration governanceConfig = YamlGovernanceConfigurationSwapperUtil.marshal(governance);
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", governanceConfig.getRegistryCenterConfiguration(), governanceConfig.isOverwrite());
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, modeConfig);
    }
    
    private static DataSource createDataSourceWithRules(final String schemaName, final Map<String, DataSource> dataSourceMap, 
                                                        final Collection<RuleConfiguration> ruleConfigs, final Properties props, final YamlGovernanceConfiguration governance) throws SQLException {
        GovernanceConfiguration governanceConfig = YamlGovernanceConfigurationSwapperUtil.marshal(governance);
        ModeConfiguration modeConfig = new ModeConfiguration("Cluster", governanceConfig.getRegistryCenterConfiguration(), governanceConfig.isOverwrite());
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, dataSourceMap, ruleConfigs, props, modeConfig);
    }
    
    private static YamlGovernanceRootRuleConfigurations unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlGovernanceRootRuleConfigurations.class);
    }
    
    private static YamlGovernanceRootRuleConfigurations unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlGovernanceRootRuleConfigurations.class);
    }
}
