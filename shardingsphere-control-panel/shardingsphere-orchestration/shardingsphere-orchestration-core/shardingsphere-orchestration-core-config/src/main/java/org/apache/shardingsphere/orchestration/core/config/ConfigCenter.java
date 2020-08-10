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

package org.apache.shardingsphere.orchestration.core.config;

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
import org.apache.shardingsphere.orchestration.core.common.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.orchestration.core.common.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.orchestration.repository.api.ConfigurationRepository;
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
 * Config center.
 */
public final class ConfigCenter {
    
    private final ConfigCenterNode node;
    
    private final ConfigurationRepository repository;
    
    public ConfigCenter(final String name, final ConfigurationRepository repository) {
        node = new ConfigCenterNode(name);
        this.repository = repository;
        DataSourceCallback.getInstance().register(this::persistDataSourceConfiguration);
        RuleCallback.getInstance().register(this::persistRuleConfigurations);
    }
    
    /**
     * Persist rule configuration.
     *
     * @param schemaName schema name
     * @param dataSourceConfigs data source configuration map
     * @param ruleConfigurations rule configurations
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs,
                                      final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        persistDataSourceConfiguration(schemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfigurations(schemaName, ruleConfigurations, isOverwrite);
        // TODO Consider removing the following one.
        persistSchemaName(schemaName, isOverwrite);
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
    
    private void persistDataSourceConfiguration(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (dataSourceConfigurations.isEmpty() || !isOverwrite) {
            return;
        }
        persistDataSourceConfiguration(schemaName, dataSourceConfigurations);
    }
    
    private void persistDataSourceConfiguration(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data source in `%s` for orchestration.", schemaName);
        Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = dataSourceConfigurations.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToYamlConfiguration(entry.getValue())));
        repository.persist(node.getDataSourcePath(schemaName), YamlEngine.marshal(yamlDataSourceConfigurations));
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        if (ruleConfigurations.isEmpty() || !isOverwrite) {
            return;
        }
        persistRuleConfigurations(schemaName, ruleConfigurations);
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<RuleConfiguration> configurations = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration config = (ShardingRuleConfiguration) each;
                Preconditions.checkState(!config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy(),
                        "No available rule configurations in `%s` for orchestration.", schemaName);
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedShardingRuleConfiguration) {
                AlgorithmProvidedShardingRuleConfiguration config = (AlgorithmProvidedShardingRuleConfiguration) each;
                Preconditions.checkState(!config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy(),
                        "No available rule configurations in `%s` for orchestration.", schemaName);
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedMasterSlaveRuleConfiguration) {
                AlgorithmProvidedMasterSlaveRuleConfiguration config = (AlgorithmProvidedMasterSlaveRuleConfiguration) each;
                config.getDataSources().forEach(group -> Preconditions.checkState(
                        !group.getMasterDataSourceName().isEmpty(), "No available master-slave rule configuration in `%s` for orchestration.", schemaName));
                configurations.add(each);
            } else if (each instanceof AlgorithmProvidedEncryptRuleConfiguration) {
                AlgorithmProvidedEncryptRuleConfiguration config = (AlgorithmProvidedEncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for orchestration.", schemaName);
                configurations.add(each);
            } else if (each instanceof MasterSlaveRuleConfiguration) {
                MasterSlaveRuleConfiguration config = (MasterSlaveRuleConfiguration) each;
                config.getDataSources().forEach(group -> Preconditions.checkState(
                        !group.getMasterDataSourceName().isEmpty(), "No available master-slave rule configuration in `%s` for orchestration.", schemaName));
                configurations.add(each);
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration config = (EncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for orchestration.", schemaName);
                configurations.add(each);
            } else if (each instanceof ShadowRuleConfiguration) {
                ShadowRuleConfiguration config = (ShadowRuleConfiguration) each;
                Preconditions.checkState(!config.getColumn().isEmpty() && null != config.getShadowMappings(), "No available shadow rule configuration in `%s` for orchestration.", schemaName);
                configurations.add(each);
            }
        }
        YamlRootRuleConfigurations yamlRuleConfigurations = new YamlRootRuleConfigurations();
        yamlRuleConfigurations.setRules(new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(configurations));
        repository.persist(node.getRulePath(schemaName), YamlEngine.marshal(yamlRuleConfigurations));
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
    
    private void persistSchemaName(final String schemaName, final boolean isOverwrite) {
        if (!isOverwrite) {
            return;
        }
        String schemaNames = repository.get(node.getSchemaPath());
        if (Strings.isNullOrEmpty(schemaNames)) {
            repository.persist(node.getSchemaPath(), schemaName);
            return;
        }
        List<String> schemaNameList = Splitter.on(",").splitToList(schemaNames);
        if (schemaNameList.contains(schemaName)) {
            return;
        }
        List<String> newArrayList = new ArrayList<>(schemaNameList);
        newArrayList.add(schemaName);
        repository.persist(node.getSchemaPath(), Joiner.on(",").join(newArrayList));
    }
    
    /**
     * Load data source configurations.
     *
     * @param schemaName schema name
     * @return data source configurations
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String schemaName) {
        if (!hasDataSourceConfiguration(schemaName)) {
            return new LinkedHashMap<>();
        }
        Map<String, YamlDataSourceConfiguration> result = (Map) YamlEngine.unmarshal(repository.get(node.getDataSourcePath(schemaName)));
        return result.entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue())));
    }
    
    /**
     * Load rule configurations.
     *
     * @param schemaName schema name
     * @return rule configurations
     */
    public Collection<RuleConfiguration> loadRuleConfigurations(final String schemaName) {
        return hasRuleConfiguration(schemaName) ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(repository.get(node.getRulePath(schemaName)), YamlRootRuleConfigurations.class).getRules()) : new LinkedList<>();
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
     * Get all schema names.
     * 
     * @return all schema names
     */
    public Collection<String> getAllSchemaNames() {
        String schemaNames = repository.get(node.getSchemaPath());
        return Strings.isNullOrEmpty(schemaNames) ? new LinkedList<>() : node.splitSchemaName(schemaNames);
    }
    
    /**
     * Judge whether schema has rule configuration.
     *
     * @param schemaName schema name
     * @return has rule configuration or not
     */
    public boolean hasRuleConfiguration(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(node.getRulePath(schemaName)));
    }
    
    /**
     * Judge whether schema has data source configuration.
     *
     * @param schemaName schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(node.getDataSourcePath(schemaName)));
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(repository.get(node.getAuthenticationPath()));
    }
}
