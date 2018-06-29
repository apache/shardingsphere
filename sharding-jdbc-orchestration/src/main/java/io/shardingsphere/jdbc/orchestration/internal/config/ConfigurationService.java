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
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.jdbc.orchestration.internal.OrchestrationProxyConfiguration;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.DataSourceConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.DataSourceParameterConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.MasterSlaveConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.ProxyConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.internal.yaml.converter.ShardingConfigurationConverter;
import io.shardingsphere.jdbc.orchestration.reg.api.RegistryCenter;

import javax.sql.DataSource;
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
    
    /**
     * Adjust has data source configuration or not in registry center.
     *
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH));
    }
    
    /**
     * Adjust has sharding rule configuration or not in registry center.
     *
     * @return has sharding rule configuration or not
     */
    public boolean hasShardingRuleConfiguration() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH));
    }
    
    /**
     * Adjust has sharding config map or not in registry center.
     *
     * @return has sharding config map or not
     */
    public boolean hasShardingConfigMap() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH));
    }
    
    /**
     * Adjust has sharding properties or not in registry center.
     *
     * @return has sharding properties or not
     */
    public boolean hasShardingProperties() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.SHARDING_PROPS_NODE_PATH));
    }
    
    private void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceConverter.dataSourceMapToYaml(dataSourceMap));
        }
    }
    
    private void persistDataSourceParameterConfiguration(final Map<String, DataSourceParameter> dataSourceParameterMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH), DataSourceParameterConverter.dataSourceParameterMapToYaml(dataSourceParameterMap));
        }
    }
    
    private void persistShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingRuleConfiguration()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH), ShardingConfigurationConverter.shardingRuleConfigToYaml(shardingRuleConfig));
        }
    }
    
    private void persistShardingConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingConfigMap()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH), ShardingConfigurationConverter.configMapToYaml(configMap));
        }
    }
    
    private void persistShardingProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasShardingProperties()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.SHARDING_PROPS_NODE_PATH), ShardingConfigurationConverter.propertiesToYaml(props));
        }
    }
    
    /**
     * Persist master-slave configuration.
     *
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistMasterSlaveConfiguration(
            final Map<String, DataSource> dataSourceMap, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap, final boolean isOverwrite) {
        persistDataSourceConfiguration(dataSourceMap, isOverwrite);
        persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig, isOverwrite);
        persistMasterSlaveConfigMap(configMap, isOverwrite);
    }
    
    /**
     * Adjust has master-slave rule configuration or not in registry center.
     *
     * @return has master-slave rule configuration or not
     */
    public boolean hasMasterSlaveRuleConfiguration() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH));
    }
    
    /**
     * Adjust has master-slave config map or not in registry center.
     *
     * @return has master-slave config map or not
     */
    public boolean hasMasterSlaveConfigMap() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH));
    }
    
    private void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasMasterSlaveRuleConfiguration()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH), MasterSlaveConfigurationConverter.masterSlaveRuleConfigToYaml(masterSlaveRuleConfig));
        }
    }
    
    private void persistMasterSlaveConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasMasterSlaveConfigMap()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_CONFIG_MAP_NODE_PATH), MasterSlaveConfigurationConverter.configMapToYaml(configMap));
        }
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
    
    private boolean hasProxyConfig() {
        return regCenter.isExisted(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH));
    }
    
    private void persistProxyRuleConfiguration(final OrchestrationProxyConfiguration orchestrationProxyConfiguration, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyConfig()) {
            regCenter.persist(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH), ProxyConfigurationConverter.proxyConfigToYaml(orchestrationProxyConfiguration));
        }
    }
    
    /**
     * Load data source configuration.
     * 
     * @return data source configuration map
     */
    public Map<String, DataSource> loadDataSourceMap() {
        return DataSourceConverter.dataSourceMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
    }
    
    /**
     * Load data source parameter.
     *
     * @return data source parameter map
     */
    public Map<String, DataSourceParameter> loadDataSourceParameter() {
        return DataSourceParameterConverter.dataSourceParameterMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.DATA_SOURCE_NODE_PATH)));
    }
    
    /**
     * Load sharding rule configuration.
     * 
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return ShardingConfigurationConverter.shardingRuleConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.SHARDING_RULE_NODE_PATH)));
    }
    
    /**
     * Load sharding config map.
     *
     * @return sharding config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadShardingConfigMap() {
        return ShardingConfigurationConverter.configMapFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.SHARDING_CONFIG_MAP_NODE_PATH)));
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
        return MasterSlaveConfigurationConverter.masterSlaveRuleConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.MASTER_SLAVE_RULE_NODE_PATH)));
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
     * Load proxy configuration.
     *
     * @return proxy configuration
     */
    public OrchestrationProxyConfiguration loadProxyConfiguration() {
        return ProxyConfigurationConverter.proxyConfigFromYaml(regCenter.getDirectly(configNode.getFullPath(ConfigurationNode.PROXY_RULE_NODE_PATH)));
    }
}
