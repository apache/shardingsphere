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

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.core.yaml.representer.processor.ShardingTupleProcessorFactory;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.engine.YamlEngine;
import org.apache.shardingsphere.core.yaml.swapper.impl.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.api.ConfigCenter;
import org.apache.shardingsphere.orchestration.internal.registry.config.node.ConfigurationNode;
import org.apache.shardingsphere.orchestration.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.yaml.swapper.DataSourceConfigurationYamlSwapper;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Configuration service.
 *
 * @author caohao
 * @author zhangliang
 * @author panjuan
 * @author wangguangyuan
 */
public final class ConfigurationService {
    
    private final ConfigurationNode configNode;
    
    private final ConfigCenter configCenter;
    
    public ConfigurationService(final String name, final ConfigCenter configCenter) {
        configNode = new ConfigurationNode(name);
        this.configCenter = configCenter;
    }
    
    /**
     * Persist rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @param dataSourceConfigs data source configuration map
     * @param ruleConfig rule configuration
     * @param authentication authentication
     * @param props sharding properties
     * @param isOverwrite is overwrite registry center's configuration
     */
    public void persistConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs, final RuleConfiguration ruleConfig,
                                     final Authentication authentication, final Properties props, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfiguration(shardingSchemaName, ruleConfig, isOverwrite);
        persistAuthentication(authentication, isOverwrite);
        persistProperties(props, isOverwrite);
        persistShardingSchemaName(shardingSchemaName);
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (isOverwrite || !hasDataSourceConfiguration(shardingSchemaName)) {
            Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data source in `%s` for orchestration.", shardingSchemaName);
            Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = Maps.transformValues(dataSourceConfigurations,
                    new Function<DataSourceConfiguration, YamlDataSourceConfiguration>() {
                        
                        @Override
                        public YamlDataSourceConfiguration apply(final DataSourceConfiguration input) {
                            return new DataSourceConfigurationYamlSwapper().swap(input);
                        }
                    }
            );
            configCenter.persist(configNode.getDataSourcePath(shardingSchemaName), YamlEngine.marshal(yamlDataSourceConfigurations));
        }
    }
    
    /**
     * Judge whether schema has data source configuration.
     * 
     * @param shardingSchemaName shading schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(configCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistRuleConfiguration(final String shardingSchemaName, final RuleConfiguration ruleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            if (ruleConfig instanceof ShardingRuleConfiguration) {
                persistShardingRuleConfiguration(shardingSchemaName, (ShardingRuleConfiguration) ruleConfig);
            } else if (ruleConfig instanceof EncryptRuleConfiguration) {
                persistEncryptRuleConfiguration(shardingSchemaName, (EncryptRuleConfiguration) ruleConfig);
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
        return !Strings.isNullOrEmpty(configCenter.get(configNode.getRulePath(shardingSchemaName)));
    }
    
    private void persistShardingRuleConfiguration(final String shardingSchemaName, final ShardingRuleConfiguration shardingRuleConfiguration) {
        Preconditions.checkState(null != shardingRuleConfiguration && !shardingRuleConfiguration.getTableRuleConfigs().isEmpty(),
                "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
        configCenter.persist(configNode.getRulePath(shardingSchemaName),
            YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration), ShardingTupleProcessorFactory.newInstance()));
    }
    
    private void persistEncryptRuleConfiguration(final String shardingSchemaName, final EncryptRuleConfiguration encryptRuleConfiguration) {
        Preconditions.checkState(null != encryptRuleConfiguration && !encryptRuleConfiguration.getEncryptors().isEmpty(),
            "No available encrypt rule configuration in `%s` for orchestration.", shardingSchemaName);
        configCenter.persist(configNode.getRulePath(shardingSchemaName), YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration)));
    }
    
    private void persistMasterSlaveRuleConfiguration(final String shardingSchemaName, final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        Preconditions.checkState(null != masterSlaveRuleConfiguration && !masterSlaveRuleConfiguration.getMasterDataSourceName().isEmpty(),
                "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName);
        configCenter.persist(configNode.getRulePath(shardingSchemaName), YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration)));
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && (isOverwrite || !hasAuthentication())) {
            configCenter.persist(configNode.getAuthenticationPath(), YamlEngine.marshal(new AuthenticationYamlSwapper().swap(authentication)));
        }
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(configCenter.get(configNode.getAuthenticationPath()));
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasProperties()) {
            configCenter.persist(configNode.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(configCenter.get(configNode.getPropsPath()));
    }
    
    private void persistShardingSchemaName(final String shardingSchemaName) {
        String shardingSchemaNames = configCenter.get(configNode.getSchemaPath());
        if (Strings.isNullOrEmpty(shardingSchemaNames)) {
            configCenter.persist(configNode.getSchemaPath(), shardingSchemaName);
            return;
        }
        List<String> schemaNameList = Splitter.on(",").splitToList(shardingSchemaNames);
        if (schemaNameList.contains(shardingSchemaName)) {
            return;
        }
        List<String> newArrayList = Lists.newArrayList(schemaNameList);
        newArrayList.add(shardingSchemaName);
        configCenter.persist(configNode.getSchemaPath(), Joiner.on(",").join(newArrayList));
    }
    
    /**
     * Judge is sharding rule or master-slave rule.
     *
     * @param shardingSchemaName sharding schema name
     * @return is sharding rule or not
     */
    public boolean isShardingRule(final String shardingSchemaName) {
        if (configCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("encryptRule:\n")) {
            return true;
        }
        if (configCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("tables:\n")
            && !regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("encryptors:\n")) {
            return true;
        }
        return false;
    }
    
    /**
     * Judge is encrypt rule or not.
     * @param shardingSchemaName sharding schema name
     * @return is encrypt rule or not
     */
    public boolean isEncryptRule(final String shardingSchemaName) {
        return !configCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("encryptRule:\n")
                && configCenter.getDirectly(configNode.getRulePath(shardingSchemaName)).contains("encryptors:\n");
    }
    
    /**
     * Load data source configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return data source configurations
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String shardingSchemaName) {
        Map<String, YamlDataSourceConfiguration> result = (Map) YamlEngine.unmarshal(configCenter.get(configNode.getDataSourcePath(shardingSchemaName)));
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load for orchestration.");
        return Maps.transformValues(result, new Function<YamlDataSourceConfiguration, DataSourceConfiguration>() {
            
            @Override
            public DataSourceConfiguration apply(final YamlDataSourceConfiguration input) {
                return new DataSourceConfigurationYamlSwapper().swap(input);
            }
        });
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration(final String shardingSchemaName) {
        return new ShardingRuleConfigurationYamlSwapper().swap(
            YamlEngine.unmarshal(regCenter.getDirectly(configNode.getRulePath(shardingSchemaName)), YamlShardingRuleConfiguration.class, new YamlRootShardingConfigurationConstructor()));
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String shardingSchemaName) {
        return new MasterSlaveRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(configCenter.get(configNode.getRulePath(shardingSchemaName)), YamlMasterSlaveRuleConfiguration.class));
    }
    
    /**
     * Load encrypt rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return encrypt rule configuration
     */
    public EncryptRuleConfiguration loadEncryptRuleConfiguration(final String shardingSchemaName) {
        return new EncryptRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(configCenter.get(configNode.getRulePath(shardingSchemaName)), YamlEncryptRuleConfiguration.class));
    }
    
    /**
     * Load authentication.
     *
     * @return authentication
     */
    public Authentication loadAuthentication() {
        return new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(configCenter.get(configNode.getAuthenticationPath()), YamlAuthenticationConfiguration.class));
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        return YamlEngine.unmarshalProperties(configCenter.get(configNode.getPropsPath()));
    }
    
    /**
     * Get all sharding schema names.
     * 
     * @return all sharding schema names
     */
    public Collection<String> getAllShardingSchemaNames() {
        String shardingSchemaNames = configCenter.get(configNode.getSchemaPath());
        if (Strings.isNullOrEmpty(shardingSchemaNames)) {
            return Collections.emptyList();
        }
        return Splitter.on(",").splitToList(shardingSchemaNames);
    }
}
