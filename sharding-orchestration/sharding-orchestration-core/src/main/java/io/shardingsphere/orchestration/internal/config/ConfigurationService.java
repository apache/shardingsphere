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
import io.shardingsphere.core.config.DataSourceConfiguration;
import io.shardingsphere.core.rule.Authentication;
import io.shardingsphere.core.yaml.masterslave.YamlMasterSlaveRuleConfiguration;
import io.shardingsphere.core.yaml.sharding.YamlShardingRuleConfiguration;
import io.shardingsphere.orchestration.internal.yaml.representer.DefaultRepresenter;
import io.shardingsphere.orchestration.reg.api.RegistryCenter;
import org.yaml.snakeyaml.Yaml;

import java.util.Collection;
import java.util.LinkedHashMap;
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
     * @param shardingSchemaName sharding schema name
     * @param dataSourceConfigurations data source configuration map
     * @param shardingRuleConfig sharding rule configuration
     * @param authentication authentication
     * @param configMap config map
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final ShardingRuleConfiguration shardingRuleConfig,
                                     final Authentication authentication, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigurations, isOverwrite);
        persistShardingRuleConfiguration(shardingSchemaName, shardingRuleConfig, isOverwrite);
        persistAuthentication(authentication, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    /**
     * Persist master-slave configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @param dataSourceConfigurations data source configuration map
     * @param masterSlaveRuleConfig master-slave rule configuration
     * @param authentication authentication
     * @param configMap config map
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final MasterSlaveRuleConfiguration masterSlaveRuleConfig,
                                     final Authentication authentication, final Map<String, Object> configMap, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigurations, isOverwrite);
        persistMasterSlaveRuleConfiguration(shardingSchemaName, masterSlaveRuleConfig, isOverwrite);
        persistAuthentication(authentication, isOverwrite);
        persistConfigMap(configMap, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data source in `%s` for orchestration.", shardingSchemaName);
            regCenter.persist(configNode.getDataSourcePath(shardingSchemaName), new Yaml(new DefaultRepresenter()).dumpAsMap(dataSourceConfigurations));
        }
    }
    
    private boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistShardingRuleConfiguration(final String shardingSchemaName, final ShardingRuleConfiguration shardingRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != shardingRuleConfig && !shardingRuleConfig.getTableRuleConfigs().isEmpty(), 
                    "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
            regCenter.persist(configNode.getRulePath(shardingSchemaName), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlShardingRuleConfiguration(shardingRuleConfig)));
        }
    }
    
    private void persistMasterSlaveRuleConfiguration(final String shardingSchemaName, final MasterSlaveRuleConfiguration masterSlaveRuleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != masterSlaveRuleConfig && !masterSlaveRuleConfig.getMasterDataSourceName().isEmpty(), 
                    "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName);
            regCenter.persist(configNode.getRulePath(shardingSchemaName), new Yaml(new DefaultRepresenter()).dumpAsMap(new YamlMasterSlaveRuleConfiguration(masterSlaveRuleConfig)));
        }
    }
    
    private boolean hasRuleConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getRulePath(shardingSchemaName)));
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && (isOverwrite || !hasAuthentication())) {
            regCenter.persist(configNode.getAuthenticationPath(), new Yaml(new DefaultRepresenter()).dumpAsMap(authentication));
        }
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(regCenter.get(configNode.getAuthenticationPath()));
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
     * Load data sources.
     *
     * @param shardingSchemaName sharding schema name
     * @return data sources map
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String shardingSchemaName) {
        Map<String, DataSourceConfiguration> result = (Map) new Yaml().load(regCenter.getDirectly(configNode.getDataSourcePath(shardingSchemaName)));
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load in `%s` for orchestration.", shardingSchemaName);
        return result;
    }
    
    /**
     * Adjust is sharding rule or master-slave rule.
     *
     * @param shardingSchemaName sharding schema name
     * @return is sharding rule or not
     */
    public boolean isShardingRule(final String shardingSchemaName) {
        return regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("tables:\n");
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
     * Load authentication.
     *
     * @return authentication
     */
    public Authentication loadAuthentication() {
        Authentication result = new Yaml().loadAs(regCenter.getDirectly(configNode.getAuthenticationPath()), Authentication.class);
        Preconditions.checkState(!Strings.isNullOrEmpty(result.getUsername()), "Authority configuration is invalid.");
        return result;
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
     * Get all sharding schema names.
     * 
     * @return all sharding schema names
     */
    public Collection<String> getAllShardingSchemaNames() {
        return regCenter.getChildrenKeys(configNode.getSchemaPath());
    }
}
