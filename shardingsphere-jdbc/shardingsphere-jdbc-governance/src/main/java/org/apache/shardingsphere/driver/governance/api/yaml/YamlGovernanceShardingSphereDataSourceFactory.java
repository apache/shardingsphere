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
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.database.DefaultSchema;
import org.apache.shardingsphere.infra.mode.config.ModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.YamlRootConfiguration;
import org.apache.shardingsphere.infra.yaml.config.pojo.mode.YamlModeConfiguration;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.config.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.infra.yaml.config.swapper.mode.ModeConfigurationYamlSwapper;
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
        YamlRootConfiguration rootConfig = unmarshal(yamlFile);
        return createDataSource(
                rootConfig.getSchemaName(), new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), rootConfig, rootConfig.getProps(), rootConfig.getMode());
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
        YamlRootConfiguration rootConfig = unmarshal(yamlFile);
        return createDataSource(rootConfig.getSchemaName(), dataSourceMap, rootConfig, rootConfig.getProps(), rootConfig.getMode());
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
        YamlRootConfiguration rootConfig = unmarshal(yamlBytes);
        return createDataSource(
                rootConfig.getSchemaName(), new YamlDataSourceConfigurationSwapper().swapToDataSources(rootConfig.getDataSources()), rootConfig, rootConfig.getProps(), rootConfig.getMode());
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
        YamlRootConfiguration rootConfig = unmarshal(yamlBytes);
        return createDataSource(rootConfig.getSchemaName(), dataSourceMap, rootConfig, rootConfig.getProps(), rootConfig.getMode());
    }
    
    private static DataSource createDataSource(final String schemaName, final Map<String, DataSource> dataSourceMap, final YamlRootConfiguration rootConfig,
                                               final Properties props, final YamlModeConfiguration yamlModeConfig) throws SQLException {
        if (rootConfig.getRules().isEmpty() || dataSourceMap.isEmpty()) {
            return createDataSourceWithoutRules(schemaName, yamlModeConfig);
        }
        return createDataSourceWithRules(schemaName, dataSourceMap, new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(rootConfig.getRules()), props, yamlModeConfig);
    }
    
    private static DataSource createDataSourceWithoutRules(final String schemaName, final YamlModeConfiguration yamlModeConfig) throws SQLException {
        ModeConfiguration modeConfig = getModeConfiguration(yamlModeConfig);
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, modeConfig);
    }
    
    private static DataSource createDataSourceWithRules(final String schemaName, final Map<String, DataSource> dataSourceMap, 
                                                        final Collection<RuleConfiguration> ruleConfigs, final Properties props, final YamlModeConfiguration yamlModeConfig) throws SQLException {
        ModeConfiguration modeConfig = getModeConfiguration(yamlModeConfig);
        return new GovernanceShardingSphereDataSource(Strings.isNullOrEmpty(schemaName) ? DefaultSchema.LOGIC_NAME : schemaName, dataSourceMap, ruleConfigs, props, modeConfig);
    }
    
    private static ModeConfiguration getModeConfiguration(final YamlModeConfiguration yamlModeConfig) {
        return null == yamlModeConfig ? new ModeConfiguration("Memory", null, true) : new ModeConfigurationYamlSwapper().swapToObject(yamlModeConfig);
    }
    
    private static YamlRootConfiguration unmarshal(final File yamlFile) throws IOException {
        return YamlEngine.unmarshal(yamlFile, YamlRootConfiguration.class);
    }
    
    private static YamlRootConfiguration unmarshal(final byte[] yamlBytes) throws IOException {
        return YamlEngine.unmarshal(yamlBytes, YamlRootConfiguration.class);
    }
}
