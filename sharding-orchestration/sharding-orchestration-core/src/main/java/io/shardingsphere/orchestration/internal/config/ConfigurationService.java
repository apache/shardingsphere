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

package io.shardingsphere.orchestration.internal.config;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import io.shardingsphere.api.config.MasterSlaveRuleConfiguration;
import io.shardingsphere.api.config.ShardingRuleConfiguration;
import io.shardingsphere.core.constant.ShardingConstant;
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.orchestration.internal.yaml.converter.DataSourceConverter;
import io.shardingsphere.orchestration.internal.yaml.converter.DataSourceParameterConverter;
import io.shardingsphere.orchestration.internal.yaml.converter.ProxyConfigurationConverter;
import io.shardingsphere.orchestration.internal.yaml.representer.DefaultRepresenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
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
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
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
    public void persistMasterSlaveConfiguration(final Map<String, DataSource> dataSourceMap,
                                                final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(dataSourceMap, isOverwrite);
        persistMasterSlaveRuleConfiguration(masterSlaveRuleConfig, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration()) {
            Preconditions.checkState(null != dataSourceMap && !dataSourceMap.isEmpty(), "No available data source configuration for orchestration.");
            regCenter.persist(configNode.getDataSourcePath(ShardingConstant.LOGIC_SCHEMA_NAME), DataSourceConverter.dataSourceMapToYaml(dataSourceMap));
        }
    }
    
    private boolean hasDataSourceConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(ShardingConstant.LOGIC_SCHEMA_NAME)));
    }
    
    private void persistShardingRuleConfiguration(final ShardingRuleConfiguration shardingRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration()) {
            Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), "No available sharding rule configuration for orchestration.");
            regCenter.persist(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlShardingRuleConfiguration(shardingRuleConfig)));
        }
    }
    
    private void persistMasterSlaveRuleConfiguration(final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration()) {
            Preconditions.checkState(null != masterSlaveRuleConfig && !masterSlaveRuleConfig.getMasterDataSourceName().isEmpty(), "No available master slave configuration for orchestration.");
            regCenter.persist(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlMasterSlaveRuleConfiguration(masterSlaveRuleConfig)));
        }
    }
    
    private boolean hasRuleConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME)));
    }
    
    private void persistConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasConfigMap()) {
            regCenter.persist(configNode.getConfigMapPath(ShardingConstant.LOGIC_SCHEMA_NAME), new Yaml(new DefaultRepresenter()).dumpAsMap(configMap));
        }
    }
    
    private boolean hasConfigMap() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getConfigMapPath(ShardingConstant.LOGIC_SCHEMA_NAME)));
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasProperties()) {
            regCenter.persist(configNode.getPropsPath(ShardingConstant.LOGIC_SCHEMA_NAME), new Yaml(new DefaultRepresenter()).dumpAsMap(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getPropsPath(ShardingConstant.LOGIC_SCHEMA_NAME)));
    }
    
    /**
     * Persist proxy configuration.
     *
     * @param serverConfig server configuration
     * @param schemaDataSourceMap schema data source map
     * @param schemaRuleMap schema rule map
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistProxyConfiguration(final YamlServerConfiguration serverConfig, 
                                          final Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap, final Map<String, YamlRuleConfiguration> schemaRuleMap, final boolean isOverwrite) {
        persistProxyDataSourceParameterConfiguration(schemaDataSourceMap, isOverwrite);
        persistProxyRuleConfiguration(schemaRuleMap, isOverwrite);
        persistProxyServerConfiguration(serverConfig, isOverwrite);
    }
    
    private void persistProxyDataSourceParameterConfiguration(final Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyDataSourceConfiguration()) {
            Preconditions.checkState(null != schemaDataSourceMap && !schemaDataSourceMap.isEmpty(), "No available schema data source configuration for orchestration.");
            for (Entry<String, Map<String, DataSourceParameter>> entry : schemaDataSourceMap.entrySet()) {
                Preconditions.checkState(null != entry.getValue() || !entry.getValue().isEmpty(), String.format("No available data source configuration in `%s` for orchestration.", entry.getKey()));
            }
            regCenter.persist(configNode.getDataSourcePath(ConfigurationNode.PROXY_NODE), DataSourceParameterConverter.dataSourceParameterMapToYaml(schemaDataSourceMap));
        }
    }
    
    private boolean hasProxyDataSourceConfiguration() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(ConfigurationNode.PROXY_NODE)));
    }
    
    private boolean hasProxyRuleConfig() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(ConfigurationNode.PROXY_NODE)));
    }
    
    private void persistProxyRuleConfiguration(final Map<String, YamlRuleConfiguration> schemaRuleMap, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyRuleConfig()) {
            Preconditions.checkState(null != schemaRuleMap && !schemaRuleMap.isEmpty(), "No available schema sharding rule configuration for orchestration.");
            for (Entry<String, YamlRuleConfiguration> entry : schemaRuleMap.entrySet()) {
                Preconditions.checkState(null != entry.getValue().getShardingRule() || null != entry.getValue().getMasterSlaveRule(),
                        String.format("No available proxy rule configuration in `%s` for Orchestration.", entry.getKey()));
            }
            regCenter.persist(configNode.getRulePath(ConfigurationNode.PROXY_NODE), ProxyConfigurationConverter.proxyRuleConfigToYaml(schemaRuleMap));
        }
    }
    
    private boolean hasProxyServerConfig() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getServerPath(ConfigurationNode.PROXY_NODE)));
    }
    
    private void persistProxyServerConfiguration(final YamlServerConfiguration serverConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyServerConfig()) {
            regCenter.persist(configNode.getServerPath(ConfigurationNode.PROXY_NODE), ProxyConfigurationConverter.proxyServerConfigToYaml(serverConfig));
        }
    }
    
    /**
     * Load data source configuration.
     *
     * @return data source configuration map
     */
    public Map<String, DataSource> loadDataSourceMap() {
        try {
            Map<String, DataSource> result = DataSourceConverter.dataSourceMapFromYaml(regCenter.getDirectly(configNode.getDataSourcePath(ShardingConstant.LOGIC_SCHEMA_NAME)));
            Preconditions.checkState(null != result && !result.isEmpty(), "No available data source configuration to load.");
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingConfigurationException("No available data source configuration to load.");
        }
    }
    
    /**
     * Load data sources.
     *
     * @return data sources map
     */
    public Map<String, Map<String, DataSourceParameter>> loadProxyDataSources() {
        try {
            Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap = DataSourceParameterConverter.dataSourceParameterMapFromYaml(
                    regCenter.getDirectly(configNode.getDataSourcePath(ConfigurationNode.PROXY_NODE)));
            Preconditions.checkState(null != schemaDataSourceMap && !schemaDataSourceMap.isEmpty(), "No available schema data source configuration to load.");
            for (Entry<String, Map<String, DataSourceParameter>> entry : schemaDataSourceMap.entrySet()) {
                Preconditions.checkState(null != entry.getValue() || !entry.getValue().isEmpty(), "No available data source configuration.");
            }
            return schemaDataSourceMap;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingConfigurationException("No available data source configuration to load.");
        }
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration() {
        return new Yaml(new DefaultRepresenter()).loadAs(
                regCenter.getDirectly(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME)), YamlShardingRuleConfiguration.class).getShardingRuleConfiguration();
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration() {
        return new Yaml(new DefaultRepresenter()).loadAs(
                regCenter.getDirectly(configNode.getRulePath(ShardingConstant.LOGIC_SCHEMA_NAME)), YamlMasterSlaveRuleConfiguration.class).getMasterSlaveRuleConfiguration();
    }
    
    /**
     * Load config map.
     *
     * @return config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadConfigMap() {
        String data = regCenter.getDirectly(configNode.getConfigMapPath(ShardingConstant.LOGIC_SCHEMA_NAME));
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, Object>() : (Map<String, Object>) new Yaml(new DefaultRepresenter()).load(data);
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        String data = regCenter.getDirectly(configNode.getPropsPath(ShardingConstant.LOGIC_SCHEMA_NAME));
        return Strings.isNullOrEmpty(data) ? new Properties() : new Yaml(new DefaultRepresenter()).loadAs(data, Properties.class);
    }
    
    /**
     * Load proxy configuration.
     *
     * @return proxy configuration
     */
    public Map<String, YamlRuleConfiguration> loadProxyConfiguration() {
        try {
            Map<String, YamlRuleConfiguration> result = ProxyConfigurationConverter.proxyRuleConfigFromYaml(regCenter.getDirectly(configNode.getRulePath(ConfigurationNode.PROXY_NODE)));
            Preconditions.checkState(null != result && !result.isEmpty(), "No available schema sharding rule configuration to load.");
            for (Entry<String, YamlRuleConfiguration> entry : result.entrySet()) {
                Preconditions.checkState(null != entry.getValue().getShardingRule() || null != entry.getValue().getMasterSlaveRule(), "Sharding rule or Master slave rule can not be both null.");
            }
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingConfigurationException("No available proxy rule configuration to load.");
        }
    }
    
    /**
     * Load yaml server configuration.
     * 
     * @return server configuration for yaml
     */
    public YamlServerConfiguration loadYamlServerConfiguration() {
        try {
            YamlServerConfiguration result = ProxyConfigurationConverter.proxyServerConfigFromYaml(regCenter.getDirectly(configNode.getServerPath(ConfigurationNode.PROXY_NODE)));
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getAuthentication().getUsername()), "Authority configuration is invalid.");
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingConfigurationException("No available proxy server configuration to load.");
        }
    }
}
