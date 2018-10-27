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
import io.shardingsphere.core.exception.ShardingConfigurationException;
import io.shardingsphere.core.rule.DataSourceParameter;
import io.shardingsphere.core.yaml.YamlRuleConfiguration;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.other.YamlServerConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.orchestration.internal.yaml.representer.DefaultRepresenter;
import io.shardingsphere.orchestration.internal.yaml.representer.SimpleTypeRepresenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.yaml.snakeyaml.Yaml;

import javax.sql.DataSource;
import java.util.Collection;
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
     * @param shardingSchemaName sharding schema name
     * @param dataSourceMap data source map
     * @param shardingRuleConfig sharding rule configuration
     * @param configMap config map
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistShardingConfiguration(final String shardingSchemaName, final Map<String, DataSource> dataSourceMap, 
                                             final ShardingRuleConfiguration shardingRuleConfig, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceMap, isOverwrite);
        persistShardingRuleConfiguration(shardingSchemaName, shardingRuleConfig, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    /**
     * Persist master-slave configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @param dataSourceMap data source map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param configMap config map
     * @param props props
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistMasterSlaveConfiguration(final String shardingSchemaName, final Map<String, DataSource> dataSourceMap,
                                                final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceMap, isOverwrite);
        persistMasterSlaveRuleConfiguration(shardingSchemaName, masterSlaveRuleConfig, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSource> dataSourceMap, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != dataSourceMap && !dataSourceMap.isEmpty(), "No available data source configuration for orchestration.");
            regCenter.persist(configNode.getDataSourcePath(shardingSchemaName), new Yaml(new SimpleTypeRepresenter("loginTimeout")).dumpAsMap(dataSourceMap));
        }
    }
    
    private boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistShardingRuleConfiguration(final String shardingSchemaName, final ShardingRuleConfiguration shardingRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), "No available sharding rule configuration for orchestration.");
            regCenter.persist(configNode.getRulePath(shardingSchemaName), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlShardingRuleConfiguration(shardingRuleConfig)));
        }
    }
    
    private void persistMasterSlaveRuleConfiguration(final String shardingSchemaName, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != masterSlaveRuleConfig && !masterSlaveRuleConfig.getMasterDataSourceName().isEmpty(), "No available master slave configuration for orchestration.");
            regCenter.persist(configNode.getRulePath(shardingSchemaName), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlMasterSlaveRuleConfiguration(masterSlaveRuleConfig)));
        }
    }
    
    private boolean hasRuleConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(shardingSchemaName)));
    }
    
    private void persistConfigMap(final Map<String, Object> configMap, final boolean isOverwrite) {
        if (isOverwrite || !hasConfigMap()) {
            regCenter.persist(configNode.getConfigMapPath(), new Yaml(new DefaultRepresenter()).dumpAsMap(configMap));
        }
    }
    
    private boolean hasConfigMap() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getConfigMapPath()));
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasProperties()) {
            regCenter.persist(configNode.getPropsPath(), new Yaml(new DefaultRepresenter()).dumpAsMap(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getPropsPath()));
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
        for (Entry<String, Map<String, DataSourceParameter>> entry : schemaDataSourceMap.entrySet()) {
            if (isOverwrite || !hasProxyDataSourceConfiguration(entry.getKey())) {
                Preconditions.checkState(null != entry.getValue() || !entry.getValue().isEmpty(), String.format("No available data source configuration in `%s` for orchestration.", entry.getKey()));
                regCenter.persist(configNode.getDataSourcePath(entry.getKey()), new Yaml(new DefaultRepresenter()).dumpAsMap(schemaDataSourceMap));
            }
        }
    }
    
    private boolean hasProxyDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistProxyRuleConfiguration(final Map<String, YamlRuleConfiguration> schemaRuleMap, final boolean isOverwrite) {
        for (Entry<String, YamlRuleConfiguration> entry : schemaRuleMap.entrySet()) {
            if (isOverwrite || !hasProxyRuleConfig(entry.getKey())) {
                Preconditions.checkState(null != entry.getValue().getShardingRule() || null != entry.getValue().getMasterSlaveRule(),
                        String.format("No available proxy rule configuration in `%s` for Orchestration.", entry.getKey()));
                regCenter.persist(configNode.getRulePath(entry.getKey()), new Yaml(new DefaultRepresenter()).dumpAsMap(schemaRuleMap));
            }
        }
    }
    
    private boolean hasProxyRuleConfig(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(shardingSchemaName)));
    }
    
    private boolean hasProxyServerConfig() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getServerPath()));
    }
    
    private void persistProxyServerConfiguration(final YamlServerConfiguration serverConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasProxyServerConfig()) {
            regCenter.persist(configNode.getServerPath(), new Yaml(new DefaultRepresenter()).dumpAsMap(serverConfig));
        }
    }
    
    /**
     * Load data source configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return data source configuration map
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSource> loadDataSourceMap(final String shardingSchemaName) {
        try {
            Map<String, DataSource> result = (Map) new Yaml().load(regCenter.getDirectly(configNode.getDataSourcePath(shardingSchemaName)));
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
    @SuppressWarnings("unchecked")
    public Map<String, Map<String, DataSourceParameter>> loadProxyDataSources() {
        try {
            // TODO spit data source
            Collection<String> shardingSchemaNames = regCenter.getChildrenKeys(configNode.getRootPath());
            Map<String, Map<String, DataSourceParameter>> schemaDataSourceMap = (Map) new Yaml().load(regCenter.getDirectly(configNode.getDataSourcePath(shardingSchemaNames.iterator().next())));
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
     * Adjust is sharding rule or master-slave rule.
     * 
     * @param shardingSchemaName sharding schema name
     * @return is sharding rule or not
     */
    public boolean isShardingRule(final String shardingSchemaName) {
        return regCenter.getDirectly(shardingSchemaName).contains("tables:\n");
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration(final String shardingSchemaName) {
        return new Yaml().loadAs(regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)), YamlShardingRuleConfiguration.class).getShardingRuleConfiguration();
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String shardingSchemaName) {
        return new Yaml().loadAs(regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)), YamlMasterSlaveRuleConfiguration.class).getMasterSlaveRuleConfiguration();
    }
    
    /**
     * Load config map.
     *
     * @return config map
     */
    @SuppressWarnings("unchecked")
    public Map<String, Object> loadConfigMap() {
        String data = regCenter.getDirectly(configNode.getConfigMapPath());
        return Strings.isNullOrEmpty(data) ? new LinkedHashMap<String, Object>() : (Map) new Yaml().load(data);
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        String data = regCenter.getDirectly(configNode.getPropsPath());
        return Strings.isNullOrEmpty(data) ? new Properties() : new Yaml().loadAs(data, Properties.class);
    }
    
    /**
     * Load proxy configuration.
     *
     * @return proxy configuration
     */
    @SuppressWarnings("unchecked")
    public Map<String, YamlRuleConfiguration> loadProxyConfiguration() {
        try {
            // TODO spit config
            Collection<String> shardingSchemaNames = regCenter.getChildrenKeys(configNode.getRootPath());
            Map<String, YamlRuleConfiguration> result = (Map) new Yaml().load(regCenter.getDirectly(configNode.getRulePath(shardingSchemaNames.iterator().next())));
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
            YamlServerConfiguration result = new Yaml().loadAs(regCenter.getDirectly(configNode.getServerPath()), YamlServerConfiguration.class);
            Preconditions.checkState(!Strings.isNullOrEmpty(result.getAuthentication().getUsername()), "Authority configuration is invalid.");
            return result;
            // CHECKSTYLE:OFF
        } catch (final Exception ex) {
            // CHECKSTYLE:ON
            throw new ShardingConfigurationException("No available proxy server configuration to load.");
        }
    }
}
