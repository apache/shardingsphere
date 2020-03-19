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

package org.apache.shardingsphere.orchestration.core.configcenter;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import org.apache.shardingsphere.api.config.masterslave.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.api.config.shadow.ShadowRuleConfiguration;
import org.apache.shardingsphere.api.config.sharding.ShardingRuleConfiguration;
import org.apache.shardingsphere.core.rule.Authentication;
import org.apache.shardingsphere.core.yaml.config.common.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.core.yaml.config.masterslave.YamlMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.shadow.YamlShadowRuleConfiguration;
import org.apache.shardingsphere.core.yaml.config.sharding.YamlShardingRuleConfiguration;
import org.apache.shardingsphere.core.yaml.constructor.YamlRootShardingConfigurationConstructor;
import org.apache.shardingsphere.core.yaml.representer.processor.ShardingTupleProcessorFactory;
import org.apache.shardingsphere.core.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.MasterSlaveRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.ShardingRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.core.yaml.swapper.impl.ShadowRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.encrypt.api.EncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.config.YamlEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.yaml.swapper.EncryptRuleConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.core.configuration.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.underlying.common.config.DataSourceConfiguration;
import org.apache.shardingsphere.underlying.common.config.RuleConfiguration;
import org.apache.shardingsphere.underlying.common.yaml.engine.YamlEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Configuration service.
 */
public final class ConfigCenter {
    
    private final ConfigCenterNode node;
    
    private final ConfigCenterRepository repository;
    
    public ConfigCenter(final String name, final ConfigCenterRepository configCenterRepository) {
        this.node = new ConfigCenterNode(name);
        this.repository = configCenterRepository;
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
            Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = dataSourceConfigurations.entrySet().stream()
                    .collect(Collectors.toMap(Map.Entry::getKey, e -> new DataSourceConfigurationYamlSwapper().swap(e.getValue())));
            repository.persist(node.getDataSourcePath(shardingSchemaName), YamlEngine.marshal(yamlDataSourceConfigurations));
        }
    }
    
    /**
     * Judge whether schema has data source configuration.
     *
     * @param shardingSchemaName shading schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(repository.get(node.getDataSourcePath(shardingSchemaName)));
    }
    
    private void persistRuleConfiguration(final String shardingSchemaName, final RuleConfiguration ruleConfig, final boolean isOverwrite) {
        if (isOverwrite || !hasRuleConfiguration(shardingSchemaName)) {
            if (ruleConfig instanceof ShardingRuleConfiguration) {
                persistShardingRuleConfiguration(shardingSchemaName, (ShardingRuleConfiguration) ruleConfig);
            } else if (ruleConfig instanceof EncryptRuleConfiguration) {
                persistEncryptRuleConfiguration(shardingSchemaName, (EncryptRuleConfiguration) ruleConfig);
            } else if (ruleConfig instanceof ShadowRuleConfiguration) {
                persistShadowRuleConfiguration(shardingSchemaName, (ShadowRuleConfiguration) ruleConfig);
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
        return !Strings.isNullOrEmpty(repository.get(node.getRulePath(shardingSchemaName)));
    }
    
    private void persistShardingRuleConfiguration(final String shardingSchemaName, final ShardingRuleConfiguration shardingRuleConfiguration) {
        Preconditions.checkState(null != shardingRuleConfiguration
                        && (!shardingRuleConfiguration.getTableRuleConfigs().isEmpty() || shardingRuleConfiguration.getDefaultTableShardingStrategyConfig() != null),
                "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
        repository.persist(node.getRulePath(shardingSchemaName),
            YamlEngine.marshal(new ShardingRuleConfigurationYamlSwapper().swap(shardingRuleConfiguration), ShardingTupleProcessorFactory.newInstance()));
    }
    
    private void persistEncryptRuleConfiguration(final String shardingSchemaName, final EncryptRuleConfiguration encryptRuleConfiguration) {
        Preconditions.checkState(null != encryptRuleConfiguration && !encryptRuleConfiguration.getEncryptors().isEmpty(),
            "No available encrypt rule configuration in `%s` for orchestration.", shardingSchemaName);
        repository.persist(node.getRulePath(shardingSchemaName), YamlEngine.marshal(new EncryptRuleConfigurationYamlSwapper().swap(encryptRuleConfiguration)));
    }
    
    private void persistShadowRuleConfiguration(final String shardingSchemaName, final ShadowRuleConfiguration shadowRuleConfiguration) {
        Preconditions.checkState(null != shadowRuleConfiguration && !shadowRuleConfiguration.getColumn().isEmpty() && null != shadowRuleConfiguration.getShadowMappings(),
                "No available shadow rule configuration in `%s` for orchestration.", shardingSchemaName);
        repository.persist(node.getRulePath(shardingSchemaName), YamlEngine.marshal(new ShadowRuleConfigurationYamlSwapper().swap(shadowRuleConfiguration)));
    }
    
    private void persistMasterSlaveRuleConfiguration(final String shardingSchemaName, final MasterSlaveRuleConfiguration masterSlaveRuleConfiguration) {
        Preconditions.checkState(null != masterSlaveRuleConfiguration && !masterSlaveRuleConfiguration.getMasterDataSourceName().isEmpty(),
                "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName);
        repository.persist(node.getRulePath(shardingSchemaName), YamlEngine.marshal(new MasterSlaveRuleConfigurationYamlSwapper().swap(masterSlaveRuleConfiguration)));
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && (isOverwrite || !hasAuthentication())) {
            repository.persist(node.getAuthenticationPath(), YamlEngine.marshal(new AuthenticationYamlSwapper().swap(authentication)));
        }
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(repository.get(node.getAuthenticationPath()));
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (isOverwrite || !hasProperties()) {
            repository.persist(node.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(repository.get(node.getPropsPath()));
    }
    
    private void persistShardingSchemaName(final String shardingSchemaName) {
        String shardingSchemaNames = repository.get(node.getSchemaPath());
        if (Strings.isNullOrEmpty(shardingSchemaNames)) {
            repository.persist(node.getSchemaPath(), shardingSchemaName);
            return;
        }
        List<String> schemaNameList = Splitter.on(",").splitToList(shardingSchemaNames);
        if (schemaNameList.contains(shardingSchemaName)) {
            return;
        }
        List<String> newArrayList = new ArrayList<>(schemaNameList);
        newArrayList.add(shardingSchemaName);
        repository.persist(node.getSchemaPath(), Joiner.on(",").join(newArrayList));
    }
    
    /**
     * Judge is sharding rule or master-slave rule.
     *
     * @param shardingSchemaName sharding schema name
     * @return is sharding rule or not
     */
    public boolean isShardingRule(final String shardingSchemaName) {
        if (repository.get(node.getRulePath(shardingSchemaName)).contains("encryptRule:\n")) {
            return true;
        }
        if (repository.get(node.getRulePath(shardingSchemaName)).contains("tables:\n")
            && !repository.get(node.getRulePath(shardingSchemaName)).contains("encryptors:\n")) {
            return true;
        }
        if (repository.get(node.getRulePath(shardingSchemaName)).contains("defaultTableStrategy:\n")) {
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
        return !repository.get(node.getRulePath(shardingSchemaName)).contains("encryptRule:\n")
                && repository.get(node.getRulePath(shardingSchemaName)).contains("encryptors:\n");
    }
    
    /**
     * Judge is shadow rule or not.
     * @param shardingSchemaName sharding schema name
     * @return is shadow rule or not
     */
    public boolean isShadowRule(final String shardingSchemaName) {
        return !repository.get(node.getRulePath(shardingSchemaName)).contains("shadowRule:\n")
                && repository.get(node.getRulePath(shardingSchemaName)).contains("shadowMappings:\n");
    }
    
    /**
     * Load data source configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return data source configurations
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String shardingSchemaName) {
        Map<String, YamlDataSourceConfiguration> result = (Map) YamlEngine.unmarshal(repository.get(node.getDataSourcePath(shardingSchemaName)));
        Preconditions.checkState(null != result && !result.isEmpty(), "No available data sources to load for orchestration.");
        return result.entrySet().stream().collect(Collectors.toMap(Map.Entry::getKey, e -> new DataSourceConfigurationYamlSwapper().swap(e.getValue())));
    }
    
    /**
     * Load sharding rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return sharding rule configuration
     */
    public ShardingRuleConfiguration loadShardingRuleConfiguration(final String shardingSchemaName) {
        return new ShardingRuleConfigurationYamlSwapper().swap(
            YamlEngine.unmarshal(repository.get(node.getRulePath(shardingSchemaName)), YamlShardingRuleConfiguration.class, new YamlRootShardingConfigurationConstructor()));
    }
    
    /**
     * Load master-slave rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return master-slave rule configuration
     */
    public MasterSlaveRuleConfiguration loadMasterSlaveRuleConfiguration(final String shardingSchemaName) {
        return new MasterSlaveRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(repository.get(node.getRulePath(shardingSchemaName)), YamlMasterSlaveRuleConfiguration.class));
    }
    
    /**
     * Load encrypt rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return encrypt rule configuration
     */
    public EncryptRuleConfiguration loadEncryptRuleConfiguration(final String shardingSchemaName) {
        return new EncryptRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(repository.get(node.getRulePath(shardingSchemaName)), YamlEncryptRuleConfiguration.class));
    }
    
    /**
     * Load shadow rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @return shadow rule configuration
     */
    public ShadowRuleConfiguration loadShadowRuleConfiguration(final String shardingSchemaName) {
        return new ShadowRuleConfigurationYamlSwapper().swap(YamlEngine.unmarshal(repository.get(node.getRulePath(shardingSchemaName)), YamlShadowRuleConfiguration.class));
    }
    
    /**
     * Load authentication.
     *
     * @return authentication
     */
    public Authentication loadAuthentication() {
        return new AuthenticationYamlSwapper().swap(YamlEngine.unmarshal(repository.get(node.getAuthenticationPath()), YamlAuthenticationConfiguration.class));
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        return YamlEngine.unmarshalProperties(repository.get(node.getPropsPath()));
    }
    
    /**
     * Get all sharding schema names.
     * 
     * @return all sharding schema names
     */
    public Collection<String> getAllShardingSchemaNames() {
        String shardingSchemaNames = repository.get(node.getSchemaPath());
        return node.splitShardingSchemaName(shardingSchemaNames);
    }
}
