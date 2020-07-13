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
import org.apache.shardingsphere.cluster.configuration.config.ClusterConfiguration;
import org.apache.shardingsphere.cluster.configuration.swapper.ClusterConfigurationYamlSwapper;
import org.apache.shardingsphere.cluster.configuration.yaml.YamlClusterConfiguration;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.callback.orchestration.DataSourceCallback;
import org.apache.shardingsphere.infra.callback.orchestration.RuleCallback;
import org.apache.shardingsphere.infra.config.DataSourceConfiguration;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.masterslave.algorithm.config.AlgorithmProvidedMasterSlaveRuleConfiguration;
import org.apache.shardingsphere.masterslave.api.config.MasterSlaveRuleConfiguration;
import org.apache.shardingsphere.metrics.configuration.config.MetricsConfiguration;
import org.apache.shardingsphere.metrics.configuration.swapper.MetricsConfigurationYamlSwapper;
import org.apache.shardingsphere.metrics.configuration.yaml.YamlMetricsConfiguration;
import org.apache.shardingsphere.orchestration.center.ConfigCenterRepository;
import org.apache.shardingsphere.orchestration.core.configuration.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.core.configuration.YamlDataSourceConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
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
        DataSourceCallback.getInstance().register(this::persistDataSourceConfiguration);
        RuleCallback.getInstance().register(this::persistRuleConfigurations);
    }
    
    /**
     * Persist rule configuration.
     *
     * @param shardingSchemaName sharding schema name
     * @param dataSourceConfigs data source configuration map
     * @param ruleConfigurations rule configurations
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistConfigurations(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs,
                                      final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfigurations(shardingSchemaName, ruleConfigurations, isOverwrite);
        // TODO Consider removing the following one.
        persistShardingSchemaName(shardingSchemaName, isOverwrite);
    }
    
    /**
     * Persist global configuration.
     *
     * @param authentication authentication
     * @param props properties
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistGlobalConfiguration(final Authentication authentication, final Properties props, final boolean isOverwrite) {
        persistAuthentication(authentication, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (isOverwrite) {
            persistDataSourceConfiguration(shardingSchemaName, dataSourceConfigurations);
        }
    }
    
    private void persistDataSourceConfiguration(final String shardingSchemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data source in `%s` for orchestration.", shardingSchemaName);
        Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = dataSourceConfigurations.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToYamlConfiguration(entry.getValue())));
        repository.persist(node.getDataSourcePath(shardingSchemaName), YamlEngine.marshal(yamlDataSourceConfigurations));
    }
    
    private void persistRuleConfigurations(final String shardingSchemaName, final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        if (ruleConfigurations.isEmpty() || !isOverwrite) {
            return;
        }
        persistRuleConfigurations(shardingSchemaName, ruleConfigurations);
    }
    
    private void persistRuleConfigurations(final String shardingSchemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<RuleConfiguration> configurations = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration config = (ShardingRuleConfiguration) each;
                Preconditions.checkState(!config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy(),
                        "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedShardingRuleConfiguration) {
                AlgorithmProvidedShardingRuleConfiguration config = (AlgorithmProvidedShardingRuleConfiguration) each;
                Preconditions.checkState(!config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy(),
                        "No available sharding rule configuration in `%s` for orchestration.", shardingSchemaName);
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedMasterSlaveRuleConfiguration) {
                AlgorithmProvidedMasterSlaveRuleConfiguration config = (AlgorithmProvidedMasterSlaveRuleConfiguration) each;
                config.getDataSources().forEach(group -> Preconditions.checkState(
                        !group.getMasterDataSourceName().isEmpty(), "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName));
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedEncryptRuleConfiguration) {
                AlgorithmProvidedEncryptRuleConfiguration config = (AlgorithmProvidedEncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for orchestration.", shardingSchemaName);
                configurations.add(each);
            } else if (each instanceof MasterSlaveRuleConfiguration) {
                MasterSlaveRuleConfiguration config = (MasterSlaveRuleConfiguration) each;
                config.getDataSources().forEach(group -> Preconditions.checkState(
                        !group.getMasterDataSourceName().isEmpty(), "No available master-slave rule configuration in `%s` for orchestration.", shardingSchemaName));
                configurations.add(each);
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration config = (EncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for orchestration.", shardingSchemaName);
                configurations.add(each);
            } else if (each instanceof ShadowRuleConfiguration) {
                ShadowRuleConfiguration config = (ShadowRuleConfiguration) each;
                Preconditions.checkState(!config.getColumn().isEmpty() && null != config.getShadowMappings(), "No available shadow rule configuration in `%s` for orchestration.", shardingSchemaName);
                configurations.add(each);
            }
        }
        YamlRootRuleConfigurations yamlRuleConfigurations = new YamlRootRuleConfigurations();
        yamlRuleConfigurations.setRules(new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(configurations));
        repository.persist(node.getRulePath(shardingSchemaName), YamlEngine.marshal(yamlRuleConfigurations));
    }
    
    /**
     * Persist metrics configuration.
     *
     * @param metricsConfiguration  metrics configuration.
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistMetricsConfiguration(final MetricsConfiguration metricsConfiguration, final boolean isOverwrite) {
        if (null != metricsConfiguration && isOverwrite) {
            repository.persist(node.getMetricsPath(), YamlEngine.marshal(new MetricsConfigurationYamlSwapper().swapToYamlConfiguration(metricsConfiguration)));
        }
    }
    
    /**
     * Persist cluster configuration.
     *
     * @param clusterConfiguration cluster configuration
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistClusterConfiguration(final ClusterConfiguration clusterConfiguration, final boolean isOverwrite) {
        if (null != clusterConfiguration && isOverwrite) {
            repository.persist(node.getClusterPath(), YamlEngine.marshal(new ClusterConfigurationYamlSwapper().swapToYamlConfiguration(clusterConfiguration)));
        }
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && isOverwrite) {
            repository.persist(node.getAuthenticationPath(), YamlEngine.marshal(new AuthenticationYamlSwapper().swapToYamlConfiguration(authentication)));
        }
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (!props.isEmpty() && isOverwrite) {
            repository.persist(node.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    private void persistShardingSchemaName(final String shardingSchemaName, final boolean isOverwrite) {
        if (!isOverwrite) {
            return;
        }
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
     * Load data source configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return data source configurations
     */
    @SuppressWarnings("unchecked")
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String shardingSchemaName) {
        if (!hasDataSourceConfiguration(shardingSchemaName)) {
            return new LinkedHashMap<>();
        }
        Map<String, YamlDataSourceConfiguration> result = (Map) YamlEngine.unmarshal(repository.get(node.getDataSourcePath(shardingSchemaName)));
        return result.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue())));
    }
    
    /**
     * Load rule configurations.
     *
     * @param shardingSchemaName sharding schema name
     * @return rule configurations
     */
    public Collection<RuleConfiguration> loadRuleConfigurations(final String shardingSchemaName) {
        return hasRuleConfiguration(shardingSchemaName) ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(repository.get(node.getRulePath(shardingSchemaName)), YamlRootRuleConfigurations.class).getRules()) : new LinkedList<>();
    }
    
    /**
     * Load metrics configuration.
     *
     * @return metrics configuration
     */
    public MetricsConfiguration loadMetricsConfiguration() {
        return Strings.isNullOrEmpty(repository.get(node.getMetricsPath())) ? null
            : new MetricsConfigurationYamlSwapper().swapToObject(YamlEngine.unmarshal(repository.get(node.getMetricsPath()), YamlMetricsConfiguration.class));
    }
    
    /**
     * Load authentication.
     *
     * @return authentication
     */
    public Authentication loadAuthentication() {
        return hasAuthentication()
                ? new AuthenticationYamlSwapper().swapToObject(YamlEngine.unmarshal(repository.get(node.getAuthenticationPath()), YamlAuthenticationConfiguration.class))
                : new Authentication();
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
     * Load cluster configuration.
     *
     * @return cluster configuration
     */
    public ClusterConfiguration loadClusterConfiguration() {
        return Strings.isNullOrEmpty(repository.get(node.getClusterPath())) ? null
                : new ClusterConfigurationYamlSwapper().swapToObject(YamlEngine.unmarshal(repository.get(node.getClusterPath()), YamlClusterConfiguration.class));
    }
    
    /**
     * Get all sharding schema names.
     * 
     * @return all sharding schema names
     */
    public Collection<String> getAllShardingSchemaNames() {
        String shardingSchemaNames = repository.get(node.getSchemaPath());
        return Strings.isNullOrEmpty(shardingSchemaNames) ? new LinkedList<>() : node.splitShardingSchemaName(shardingSchemaNames);
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
    
    /**
     * Judge whether schema has data source configuration.
     *
     * @param shardingSchemaName shading schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String shardingSchemaName) {
        return !Strings.isNullOrEmpty(repository.get(node.getDataSourcePath(shardingSchemaName)));
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(repository.get(node.getAuthenticationPath()));
    }
}
