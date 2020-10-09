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

package org.apache.shardingsphere.governance.core.config;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.encrypt.algorithm.config.AlgorithmProvidedEncryptRuleConfiguration;
import org.apache.shardingsphere.encrypt.api.config.EncryptRuleConfiguration;
import org.apache.shardingsphere.governance.core.event.model.persist.DataSourcePersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.MetaDataPersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.RulePersistEvent;
import org.apache.shardingsphere.governance.core.event.model.persist.SchemaNamePersistEvent;
import org.apache.shardingsphere.governance.core.event.GovernanceEventBus;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.governance.core.yaml.config.metadata.YamlRuleSchemaMetaData;
import org.apache.shardingsphere.governance.core.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.core.yaml.swapper.RuleSchemaMetaDataYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.infra.auth.Authentication;
import org.apache.shardingsphere.infra.auth.yaml.config.YamlAuthenticationConfiguration;
import org.apache.shardingsphere.infra.auth.yaml.swapper.AuthenticationYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.metadata.schema.RuleSchemaMetaData;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;
import org.apache.shardingsphere.replication.primaryreplica.algorithm.config.AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.PrimaryReplicaReplicationRuleConfiguration;
import org.apache.shardingsphere.replication.primaryreplica.api.config.rule.PrimaryReplicaReplicationDataSourceRuleConfiguration;
import org.apache.shardingsphere.shadow.api.config.ShadowRuleConfiguration;
import org.apache.shardingsphere.sharding.algorithm.config.AlgorithmProvidedShardingRuleConfiguration;
import org.apache.shardingsphere.sharding.api.config.ShardingRuleConfiguration;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Config center.
 */
public final class ConfigCenter {
    
    private final ConfigCenterNode node;
    
    private final ConfigurationRepository repository;
    
    public ConfigCenter(final ConfigurationRepository repository) {
        node = new ConfigCenterNode();
        this.repository = repository;
        GovernanceEventBus.getInstance().register(this);
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
        persistDataSourceConfigurations(schemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfigurations(schemaName, ruleConfigurations, isOverwrite);
        // TODO Consider removing the following one.
        persistSchemaName(schemaName);
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
    
    /**
     * persist data source configurations.
     * @param event Data source event.
     */
    @Subscribe
    public synchronized void renew(final DataSourcePersistEvent event) {
        persistDataSourceConfigurations(event.getSchemaName(), event.getDataSourceConfigurations());
    }
    
    /**
     * Persist rule configurations.
     * 
     * @param event Rule event.
     */
    @Subscribe
    public synchronized void renew(final RulePersistEvent event) {
        persistRuleConfigurations(event.getSchemaName(), event.getRuleConfigurations());
    }
    
    /**
     * Persist schema name.
     * 
     * @param event Schema name event.
     */
    @Subscribe
    public synchronized void renew(final SchemaNamePersistEvent event) {
        persistSchema(event.getSchemaName(), event.isDrop());
    }
    
    /**
     * Persist meta data.
     *
     * @param event Meta data event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataPersistEvent event) {
        persistMetaData(event.getSchemaName(), event.getMetaData());
    }
    
    private void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (!dataSourceConfigurations.isEmpty() && (isOverwrite || !hasDataSourceConfiguration(schemaName))) {
            persistDataSourceConfigurations(schemaName, dataSourceConfigurations);
        }
    }
    
    private void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Preconditions.checkState(null != dataSourceConfigurations && !dataSourceConfigurations.isEmpty(), "No available data source in `%s` for governance.", schemaName);
        Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = dataSourceConfigurations.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToYamlConfiguration(entry.getValue())));
        YamlDataSourceConfigurationWrap yamlDataSourceConfigWrap = new YamlDataSourceConfigurationWrap();
        yamlDataSourceConfigWrap.setDataSources(yamlDataSourceConfigurations);
        repository.persist(node.getDataSourcePath(schemaName), YamlEngine.marshal(yamlDataSourceConfigWrap));
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        if (!ruleConfigurations.isEmpty() && (isOverwrite || !hasRuleConfiguration(schemaName))) {
            persistRuleConfigurations(schemaName, ruleConfigurations);
        }
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigurations) {
            if (each instanceof ShardingRuleConfiguration) {
                ShardingRuleConfiguration config = (ShardingRuleConfiguration) each;
                Preconditions.checkState(hasAvailableTableConfigurations(config),
                        "No available rule configs in `%s` for governance.", schemaName);
                configs.add(each);
            } else if (each instanceof AlgorithmProvidedShardingRuleConfiguration) {
                AlgorithmProvidedShardingRuleConfiguration config = (AlgorithmProvidedShardingRuleConfiguration) each;
                Preconditions.checkState(hasAvailableTableConfigurations(config),
                        "No available rule configs in `%s` for governance.", schemaName);
                configs.add(each);
            } else if (each instanceof AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration) {
                AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration config = (AlgorithmProvidedPrimaryReplicaReplicationRuleConfiguration) each;
                checkDataSources(schemaName, config.getDataSources());
                configs.add(each);
            } else if (each instanceof AlgorithmProvidedEncryptRuleConfiguration) {
                AlgorithmProvidedEncryptRuleConfiguration config = (AlgorithmProvidedEncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for governance.", schemaName);
                configs.add(each);
            } else if (each instanceof PrimaryReplicaReplicationRuleConfiguration) {
                PrimaryReplicaReplicationRuleConfiguration config = (PrimaryReplicaReplicationRuleConfiguration) each;
                checkDataSources(schemaName, config.getDataSources());
                configs.add(each);
            } else if (each instanceof EncryptRuleConfiguration) {
                EncryptRuleConfiguration config = (EncryptRuleConfiguration) each;
                Preconditions.checkState(!config.getEncryptors().isEmpty(), "No available encrypt rule configuration in `%s` for governance.", schemaName);
                configs.add(each);
            } else if (each instanceof ShadowRuleConfiguration) {
                ShadowRuleConfiguration config = (ShadowRuleConfiguration) each;
                boolean isShadow = !config.getColumn().isEmpty() && null != config.getSourceDataSourceNames() && null != config.getShadowDataSourceNames();
                Preconditions.checkState(isShadow, "No available shadow rule configuration in `%s` for governance.", schemaName);
                configs.add(each);
            }
        }
        YamlRootRuleConfigurations yamlRuleConfigs = new YamlRootRuleConfigurations();
        yamlRuleConfigs.setRules(new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(configs));
        repository.persist(node.getRulePath(schemaName), YamlEngine.marshal(yamlRuleConfigs));
    }
    
    private void checkDataSources(String schemaName, Collection<PrimaryReplicaReplicationDataSourceRuleConfiguration> dataSources) {
        dataSources.forEach(each -> Preconditions.checkState(
                !each.getPrimaryDataSourceName().isEmpty(), "No available primary-replica-replication rule configuration in `%s` for governance.", schemaName));
    }
    
    private boolean hasAvailableTableConfigurations(final ShardingRuleConfiguration config) {
        return !config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy() || !config.getAutoTables().isEmpty();
    }
    
    private boolean hasAvailableTableConfigurations(final AlgorithmProvidedShardingRuleConfiguration config) {
        return !config.getTables().isEmpty() || null != config.getDefaultTableShardingStrategy() || !config.getAutoTables().isEmpty();
    }
    
    private void persistAuthentication(final Authentication authentication, final boolean isOverwrite) {
        if (null != authentication && (isOverwrite || !hasAuthentication())) {
            repository.persist(node.getAuthenticationPath(), YamlEngine.marshal(new AuthenticationYamlSwapper().swapToYamlConfiguration(authentication)));
        }
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (!props.isEmpty() && (isOverwrite || !hasProperties())) {
            repository.persist(node.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    private boolean hasProperties() {
        return !Strings.isNullOrEmpty(repository.get(node.getPropsPath()));
    }
    
    private void persistSchemaName(final String schemaName) {
        String schemaNames = repository.get(node.getSchemasPath());
        if (Strings.isNullOrEmpty(schemaNames)) {
            repository.persist(node.getSchemasPath(), schemaName);
            return;
        }
        List<String> schemaNameList = Splitter.on(",").splitToList(schemaNames);
        if (schemaNameList.contains(schemaName)) {
            return;
        }
        List<String> newArrayList = new ArrayList<>(schemaNameList);
        newArrayList.add(schemaName);
        repository.persist(node.getSchemasPath(), Joiner.on(",").join(newArrayList));
    }
    
    private void persistSchema(final String schemaName, final boolean isDrop) {
        String schemaNames = repository.get(node.getSchemasPath());
        Collection<String> schemas = Strings.isNullOrEmpty(schemaNames) ? new LinkedHashSet<>() : new LinkedHashSet<>(Splitter.on(",").splitToList(schemaNames));
        if (isDrop) {
            schemas.remove(schemaName);
        } else if (!schemas.contains(schemaName)) {
            schemas.add(schemaName);
        }
        repository.persist(node.getSchemasPath(), Joiner.on(",").join(schemas));
    }
    
    /**
     * Load data source configurations.
     *
     * @param schemaName schema name
     * @return data source configurations
     */
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String schemaName) {
        if (!hasDataSourceConfiguration(schemaName)) {
            return new LinkedHashMap<>();
        }
        YamlDataSourceConfigurationWrap result = YamlEngine.unmarshal(repository.get(node.getDataSourcePath(schemaName)), YamlDataSourceConfigurationWrap.class);
        return result.getDataSources().entrySet().stream().collect(Collectors.toMap(Entry::getKey, entry -> new DataSourceConfigurationYamlSwapper().swapToObject(entry.getValue())));
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
     * Get all schema names.
     * 
     * @return all schema names
     */
    public Collection<String> getAllSchemaNames() {
        String schemaNames = repository.get(node.getSchemasPath());
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
    
    /**
     * Persist rule schema meta data.
     *
     * @param schemaName schema name
     * @param ruleSchemaMetaData rule schema meta data of the schema
     */
    public void persistMetaData(final String schemaName, final RuleSchemaMetaData ruleSchemaMetaData) {
        repository.persist(node.getTablePath(schemaName), YamlEngine.marshal(new RuleSchemaMetaDataYamlSwapper().swapToYamlConfiguration(ruleSchemaMetaData)));
    }
    
    /**
     * Load rule schema meta data.
     *
     * @param schemaName schema name
     * @return rule schema meta data of the schema
     */
    public Optional<RuleSchemaMetaData> loadMetaData(final String schemaName) {
        String path = repository.get(node.getTablePath(schemaName));
        if (Strings.isNullOrEmpty(path)) {
            return Optional.empty();
        }
        return Optional.of(new RuleSchemaMetaDataYamlSwapper().swapToObject(YamlEngine.unmarshal(path, YamlRuleSchemaMetaData.class)));
    }
    
    /**
     * Delete schema.
     * 
     * @param schemaName schema name
     */
    public void deleteSchema(final String schemaName) {
        repository.delete(node.getSchemaNamePath(schemaName));
    }
    
    private boolean hasAuthentication() {
        return !Strings.isNullOrEmpty(repository.get(node.getAuthenticationPath()));
    }
}
