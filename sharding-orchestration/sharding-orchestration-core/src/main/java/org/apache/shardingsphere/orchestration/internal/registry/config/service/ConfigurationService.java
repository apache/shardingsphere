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

package org.apache.shardingsphere.orchestration.internal.registry.config.service;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import org.apache.shardingsphere.api.config.rule.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.rule.RuleConfiguration;
import org.apache.shardingsphere.api.config.rule.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.AuthenticationYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.ConfigMapYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.DataSourceConfigurationsYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.MasterSlaveRuleConfigurationYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.PropertiesYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.dumper.impl.ShardingRuleConfigurationYamlDumper;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.AuthenticationYamlLoader;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.ConfigMapYamlLoader;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.DataSourceConfigurationsYamlLoader;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.MasterSlaveRuleConfigurationYamlLoader;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.PropertiesYamlLoader;
import org.apache.shardingsphere.orchestration.yaml.loader.impl.ShardingRuleConfigurationYamlLoader;

import java.util.Collection;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration service.
 *
 * @author caohao
 * @author zhangliang
 * @author panjuan
 */
public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final RegistryCenter regCenter;
    
    public ConfigurationService(final String name, final RegistryCenter regCenter) {
        configNode = new ConfigurationNode(name);
        this.regCenter = regCenter;
    }
    
    /**
     * Persist rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @param dataSourceConfigs data source configuration map
     * @param ruleConfig rule configuration
     * @param authentication authentication
     * @param configMap config map
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs, final RuleConfiguration ruleConfig,
                                     final Authentication authentication, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfiguration(shardingSchemaName, ruleConfig, isOverwrite);
        persistAuthentication(authentication, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != dataSourceConfigs && !dataSourceConfigs.isEmpty(), "No available data source in `%s` for orchestration.", shardingSchemaName);
            regCenter.persist(configNode.getDataSourcePath(shardingSchemaName), new DataSourceConfigurationsYamlDumper().dump(dataSourceConfigs));
        }
    }
    
    /**
     * Judge whether schema has data source configuration.
     * 
     * @param shardingSchemaName shading schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistRuleConfiguration(final String shardingSchemaName, final RuleConfiguration ruleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            if (ruleConfig instanceof ShardingRuleConfiguration) {
                persistShardingRuleConfiguration(shardingSchemaName, (ShardingRuleConfiguration) ruleConfig);
            } else {
                persistMasterSlaveRuleConfiguration(shardingSchemaName, (MasterSlaveRuleConfiguration) ruleConfig);
            }
        }
    }
    
    /**
     * Judge whether schema has rule configuration.
     * 
     * @param shardingSchemaName sharding schema name
     * @return has rule configuration or not
     */
    public boolean hasRuleConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(shardingSchemaName)));
    }
    
    private void persistShardingRuleConfiguration(final String shardingSchemaName, final ShardingRuleConfiguration shardingRuleConfig) {
        Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(),
                "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
        regCenter.persist(configNode.getRulePath(shardingSchemaName), new ShardingRuleConfigurationYamlDumper().dump(shardingRuleConfig));
    }
    
    private void persistMasterSlaveRuleConfiguration(final String shardingSchemaName, final MasterSlaveRuleConfiguration masterSlaveRuleConfig) {
        Preconditions.checkState(null != masterSlaveRuleConfig && !masterSlaveRuleConfig.getMasterDataSourceName().isEmpty(),
                "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName);
        regCenter.persist(configNode.getRulePath(shardingSchemaName), new MasterSlaveRuleConfigurationYamlDumper().dump(masterSlaveRuleConfig));
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && (isOverwrite || !hasAuthentication())) {
            regCenter.persist(configNode.getAuthenticationPath(), new AuthenticationYamlDumper().dump(authentication));
        }
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getAuthenticationPath()));
    }
    
    private void persistConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasConfigMap()) {
            regCenter.persist(configNode.getConfigMapPath(), new ConfigMapYamlDumper().dump(configMap));
        }
    }
    
    private boolean hasConfigMap() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getConfigMapPath()));
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasProperties()) {
            regCenter.persist(configNode.getPropsPath(), new PropertiesYamlDumper().dump(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getPropsPath()));
    }
    
    /**
     * Judge is sharding rule or master-slave rule.
     *
     * @param shardingSchemaName sharding schema name
     * @return is sharding rule or not
     */
    public boolean isShardingRule(final String shardingSchemaName) {
        return regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("tables:\n");
    }
    
    /**
     * Load data source configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return data source configurations
     */
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String shardingSchemaName) {
        return new DataSourceConfigurationsYamlLoader().load(regCenter.getDirectly(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration(final String shardingSchemaName) {
        return new ShardingRuleConfigurationYamlLoader().load(regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)));
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String shardingSchemaName) {
        return new MasterSlaveRuleConfigurationYamlLoader().load(regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)));
    }
    
    /**
     * Load authentication.
     *
     * @return authentication
     */
    public Authentication loadAuthentication() {
        return new AuthenticationYamlLoader().load(regCenter.getDirectly(configNode.getAuthenticationPath()));
    }
    
    /**
     * Load config map.
     *
     * @return config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadConfigMap() {
        return new ConfigMapYamlLoader().load(regCenter.getDirectly(configNode.getConfigMapPath()));
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        return new PropertiesYamlLoader().load(regCenter.getDirectly(configNode.getPropsPath()));
    }
    
    /**
     * Get all sharding schema names.
     * 
     * @return all sharding schema names
     */
    public Collection<String> getAllShardingSchemaNames() {
        return regCenter.getChildrenKeys(configNode.getSchemaPath());
    }
}
