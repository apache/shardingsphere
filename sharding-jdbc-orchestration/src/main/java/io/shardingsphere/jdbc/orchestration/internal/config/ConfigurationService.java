/*
 * Copyright 2016-2018 shardingsphere.io.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * </p>
 */

package io.shardingsphere.jdbc.orchestration.internal.config;

import com.google.common.base.Strings;
import io.shardingsphere.core.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.core.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.DataSourceConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.DataSourceParameterConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.MasterSlaveConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.ProxyConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.ShardingConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;

import javax.sql.DataSource;
import java.util.HashMap;
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
     * Persist sharding configuration.
     *
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param configMap config map
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistShardingConfiguration(
            final Map<String, DataSource> dataSourceMap, final ShardingRuleConfiguration shardingRuleConfig, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(dataSourceMap, isOverwrite);
        persistShardingRuleConfiguration(shardingRuleConfig, isOverwrite);
        persistShardingConfigMap(configMap, isOverwrite);
        persistShardingProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration()) {
            if (dataSourceMap.isEmpty()) {
                throw new ShardingConfigurationException("No available data source configuration for Orchestration.");
            }
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceConverter.dataSourceMapToYaml(dataSourceMap));
        }
    }
    
    private boolean hasDataSourceConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
    }
    
    private void persistShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingRuleConfiguration()) {
            if (shardingRuleConfig.getTableRuleConfigs().isEmpty()) {
                throw new ShardingConfigurationException("No available sharding rule configuration for Orchestration.");
            }
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH), ShardingConfigurationConverter.shardingRuleConfigToYaml(shardingRuleConfig));
        }
    }
    
    private boolean hasShardingRuleConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH)));
    }
    
    private void persistShardingConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingConfigMap()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH), ShardingConfigurationConverter.configMapToYaml(configMap));
        }
    }
    
    private boolean hasShardingConfigMap() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH)));
    }
    
    private void persistShardingProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingProperties()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_PROPS_NODE_PATH), ShardingConfigurationConverter.propertiesToYaml(props));
        }
    }
    
    private boolean hasShardingProperties() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.SHARDING_PROPS_NODE_PATH)));
    }
    
    /**
     * Persist master-slave configuration.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param props props
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistMasterSlaveConfiguration(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap,
            final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(dataSourceMap, isOverwrite);
        persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig, isOverwrite);
        persistMasterSlaveConfigMap(configMap, isOverwrite);
        persistMasterSlaveProperties(props, isOverwrite);
    }
    
    private void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasMasterSlaveRuleConfiguration()) {
            if (masterSlaveRuleConfig.getMasterDataSourceName().isEmpty()) {
                throw new ShardingConfigurationException("No available master slave configuration for Orchestration.");
            }
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH), MasterSlaveConfigurationConverter.masterSlaveRuleConfigToYaml(masterSlaveRuleConfig));
        }
    }
    
    private boolean hasMasterSlaveRuleConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH)));
    }
    
    private void persistMasterSlaveConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasMasterSlaveConfigMap()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH), MasterSlaveConfigurationConverter.configMapToYaml(configMap));
        }
    }
    
    private boolean hasMasterSlaveConfigMap() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH)));
    }
    
    private void persistMasterSlaveProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasMasterSlaveProperties()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_PROPS_NODE_PATH), MasterSlaveConfigurationConverter.propertiesToYaml(props));
        }
    }
    
    private boolean hasMasterSlaveProperties() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_PROPS_NODE_PATH)));
    }
    
    /**
     * Persist proxy configuration.
     *
     * @param orchestrationProxyConfiguration orchestration proxy configuration
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistProxyConfiguration(final OrchestrationProxyConfiguration orchestrationProxyConfiguration, final boolean isOverwrite) {
        persistDataSourceParameterConfiguration(orchestrationProxyConfiguration.getDataSources(), isOverwrite);
        persistProxyRuleConfiguration(orchestrationProxyConfiguration, isOverwrite);
    }
    
    private void persistDataSourceParameterConfiguration(final Map<String, DataSourceParameter> dataSourceParameterMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration()) {
            if (dataSourceParameterMap.isEmpty()) {
                throw new ShardingConfigurationException("No available data source configuration for Orchestration.");
            }
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceParameterConverter.dataSourceParameterMapToYaml(dataSourceParameterMap));
        }
    }
    
    private boolean hasProxyConfig() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH)));
    }
    
    private void persistProxyRuleConfiguration(final OrchestrationProxyConfiguration orchestrationProxyConfiguration, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyConfig()) {
            if (orchestrationProxyConfiguration.getShardingRule().getTables().isEmpty() && orchestrationProxyConfiguration.getMasterSlaveRule().getMasterDataSourceName().isEmpty()) {
                throw new ShardingConfigurationException("No available proxy rule configuration for Orchestration.");
            }
            regCenter.persist(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH), ProxyConfigurationConverter.proxyConfigToYaml(orchestrationProxyConfiguration));
        }
    }
    
    /**
     * Load data source configuration.
     * 
     * @return data source configuration map
     */
    public Map<String, DataSource> loadDataSourceMap() {
        try {
            return DataSourceConverter.dataSourceMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
        } catch (final Exception ex) {
            throw new ShardingConfigurationException("No available data source configuration to load.");
        }
    }
    
    /**
     * Load data sources.
     *
     * @return data sources map
     */
    public Map<String, DataSourceParameter> loadDataSources() {
        try {
            return DataSourceParameterConverter.dataSourceParameterMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
        } catch (final Exception ex) {
            throw new ShardingConfigurationException("No available data source configuration to load.");
        }
    }
    
    /**
     * Load sharding rule configuration.
     * 
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        try {
            return ShardingConfigurationConverter.shardingRuleConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH)));
        } catch (final Exception ex) {
            throw new ShardingConfigurationException("No available sharding rule configuration to load.");
        }
    }
    
    /**
     * Load sharding config map.
     *
     * @return sharding config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadShardingConfigMap() {
        String data = regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH));
        return Strings.isNullOrEmpty(data) ? new HashMap<String, Object>() : ShardingConfigurationConverter.configMapFromYaml(data);
    }
    
    /**
     * Load sharding properties configuration.
     * 
     * @return sharding properties
     */
    public Properties loadShardingProperties() {
        String data = regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.SHARDING_PROPS_NODE_PATH));
        return Strings.isNullOrEmpty(data) ? new Properties() : ShardingConfigurationConverter.propertiesFromYaml(data);
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        try {
            return MasterSlaveConfigurationConverter.masterSlaveRuleConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH)));
        } catch (final Exception ex) {
            throw new ShardingConfigurationException("No available master slave rule configuration to load.");
        }
    }
    
    /**
     * Load master-slave config map.
     *
     * @return master-slave config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadMasterSlaveConfigMap() {
        return MasterSlaveConfigurationConverter.configMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH)));
    }
    
    /**
     * Load sharding properties configuration.
     *
     * @return sharding properties
     */
    public Properties loadMasterSlaveProperties() {
        String data = regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH));
        return Strings.isNullOrEmpty(data) ? new Properties() : MasterSlaveConfigurationConverter.propertiesFromYaml(data);
    }
    
    /**
     * Load proxy configuration.
     *
     * @return proxy configuration
     */
    public OrchestrationProxyConfiguration loadProxyConfiguration() {
        try {
            return ProxyConfigurationConverter.proxyConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH)));
        } catch (final Exception ex) {
            throw new ShardingConfigurationException("No available proxy configuration to load.");
        }
    }
}
