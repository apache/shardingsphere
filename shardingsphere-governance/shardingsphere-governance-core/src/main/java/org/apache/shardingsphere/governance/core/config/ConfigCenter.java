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
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.governance.core.config.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.governance.core.config.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceAddedEvent;
import org.apache.shardingsphere.governance.core.event.model.datasource.DataSourceAlteredEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataCreatedEvent;
import org.apache.shardingsphere.governance.core.event.model.metadata.MetaDataDroppedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.event.model.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.event.model.scaling.StartScalingEvent;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.governance.core.yaml.config.YamlConfigurationConverter;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfiguration;
import org.apache.shardingsphere.governance.core.yaml.config.YamlDataSourceConfigurationWrap;
import org.apache.shardingsphere.governance.core.yaml.config.schema.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.swapper.DataSourceConfigurationYamlSwapper;
import org.apache.shardingsphere.governance.core.yaml.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.api.ConfigurationRepository;
import org.apache.shardingsphere.infra.metadata.auth.model.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.auth.builtin.yaml.swapper.UserRuleYamlSwapper;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.yaml.config.YamlRootRuleConfigurations;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
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
    
    private final ConfigCacheManager configCacheManager;
    
    public ConfigCenter(final ConfigurationRepository repository) {
        node = new ConfigCenterNode();
        this.repository = repository;
        configCacheManager = new ConfigCacheManager(repository, node);
        ShardingSphereEventBus.getInstance().register(this);
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
     * @param users user
     * @param props properties
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistGlobalConfiguration(final Collection<ShardingSphereUser> users, final Properties props, final boolean isOverwrite) {
        persistAuthentication(users, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    /**
     * persist data source configurations.
     * 
     * @param event Data source added event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceAddedEvent event) {
        addDataSourceConfigurations(event.getSchemaName(), event.getDataSourceConfigurations());
    }

    /**
     * Change data source configurations.
     * 
     * @param event Data source altered event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceAlteredEvent event) {
        persistDataSourceConfigurations(event.getSchemaName(), event.getDataSourceConfigurations());
    }
    
    /**
     * Persist rule configurations.
     * 
     * @param event rule configurations altered event.
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsAlteredEvent event) {
        //TODO
        persistRuleConfigurations(event.getSchemaName(), event.getRuleConfigurations());
    }
    
    /**
     * Persist meta data.
     * 
     * @param event meta data created event.
     */
    @Subscribe
    public synchronized void renew(final MetaDataCreatedEvent event) {
        String schemaNames = repository.get(node.getMetadataNodePath());
        Collection<String> schemas = Strings.isNullOrEmpty(schemaNames) ? new LinkedHashSet<>() : new LinkedHashSet<>(Splitter.on(",").splitToList(schemaNames));
        if (!schemas.contains(event.getSchemaName())) {
            schemas.add(event.getSchemaName());
            repository.persist(node.getMetadataNodePath(), Joiner.on(",").join(schemas));
        }
    }
    
    /**
     * Delete meta data.
     *
     * @param event meta data dropped event
     */
    @Subscribe
    public synchronized void renew(final MetaDataDroppedEvent event) {
        String schemaNames = repository.get(node.getMetadataNodePath());
        Collection<String> schemas = Strings.isNullOrEmpty(schemaNames) ? new LinkedHashSet<>() : new LinkedHashSet<>(Splitter.on(",").splitToList(schemaNames));
        if (schemas.contains(event.getSchemaName())) {
            schemas.remove(event.getSchemaName());
            repository.persist(node.getMetadataNodePath(), Joiner.on(",").join(schemas));
        }
    }
    
    /**
     * Persist schema.
     *
     * @param event schema altered event.
     */
    @Subscribe
    public synchronized void renew(final SchemaAlteredEvent event) {
        persistSchema(event.getSchemaName(), event.getSchema());
    }
    
    /**
     * Switch rule configuration.
     * 
     * @param event switch rule configuration event
     */
    @Subscribe
    public synchronized void renew(final SwitchRuleConfigurationEvent event) {
        persistRuleConfigurations(event.getSchemaName(), loadCachedRuleConfigurations(event.getSchemaName(), event.getRuleConfigurationCacheId()));
        configCacheManager.deleteCache(node.getRulePath(event.getSchemaName()), event.getRuleConfigurationCacheId());
    }
    
    /**
     * Rule configuration cached.
     * 
     * @param event rule configuration cached event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationCachedEvent event) {
        StartScalingEvent startScalingEvent = new StartScalingEvent(event.getSchemaName(),
                repository.get(node.getDataSourcePath(event.getSchemaName())),
                repository.get(node.getRulePath(event.getSchemaName())),
                configCacheManager.loadCache(node.getRulePath(event.getSchemaName()), event.getCacheId()), event.getCacheId());
        ShardingSphereEventBus.getInstance().post(startScalingEvent);
    }
    
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigurationCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(configCacheManager.loadCache(node.getRulePath(schemaName), ruleConfigurationCacheId), YamlRootRuleConfigurations.class).getRules());
    }
    
    private void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations, final boolean isOverwrite) {
        if (!dataSourceConfigurations.isEmpty() && (isOverwrite || !hasDataSourceConfiguration(schemaName))) {
            persistDataSourceConfigurations(schemaName, dataSourceConfigurations);
        }
    }
    
    /**
     * Persist data source configurations.
     *
     * @param schemaName schema name
     * @param dataSourceConfigurations data source configurations
     */
    public void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        repository.persist(node.getDataSourcePath(schemaName), YamlEngine.marshal(createYamlDataSourceConfigurationWrap(dataSourceConfigurations)));
    }
    
    private void addDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Map<String, DataSourceConfiguration> dataSourceConfigurationMap = loadDataSourceConfigurations(schemaName);
        dataSourceConfigurationMap.putAll(dataSourceConfigurations);
        repository.persist(node.getDataSourcePath(schemaName), YamlEngine.marshal(createYamlDataSourceConfigurationWrap(dataSourceConfigurationMap)));
    }
    
    private YamlDataSourceConfigurationWrap createYamlDataSourceConfigurationWrap(final Map<String, DataSourceConfiguration> dataSourceConfigurations) {
        Map<String, YamlDataSourceConfiguration> yamlDataSourceConfigurations = dataSourceConfigurations.entrySet().stream().collect(Collectors.toMap(Entry::getKey, 
            entry -> new DataSourceConfigurationYamlSwapper().swapToYamlConfiguration(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
        YamlDataSourceConfigurationWrap result = new YamlDataSourceConfigurationWrap();
        result.setDataSources(yamlDataSourceConfigurations);
        return result;
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations, final boolean isOverwrite) {
        if (!ruleConfigurations.isEmpty() && (isOverwrite || !hasRuleConfiguration(schemaName))) {
            persistRuleConfigurations(schemaName, ruleConfigurations);
        }
    }
    
    /**
     * Persist rule configurations.
     *
     * @param schemaName schema name
     * @param ruleConfigurations rule configurations
     */
    public void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        repository.persist(node.getRulePath(schemaName), YamlEngine.marshal(createYamlRootRuleConfigurations(schemaName, ruleConfigurations)));
    }
    
    private void cacheRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        String cacheId = configCacheManager.cache(node.getRulePath(schemaName), YamlEngine.marshal(createYamlRootRuleConfigurations(schemaName, ruleConfigurations)));
        StartScalingEvent event = new StartScalingEvent(schemaName,
                repository.get(node.getDataSourcePath(schemaName)),
                repository.get(node.getRulePath(schemaName)),
                configCacheManager.loadCache(node.getRulePath(schemaName), cacheId), cacheId);
        ShardingSphereEventBus.getInstance().post(event);
    }
    
    private YamlRootRuleConfigurations createYamlRootRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigurations) {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigurations) {
            Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.newInstance(each);
            if (checker.isPresent()) {
                checker.get().check(schemaName, each);
                configs.add(each);
            }
        }
        YamlRootRuleConfigurations result = new YamlRootRuleConfigurations();
        result.setRules(new YamlRuleConfigurationSwapperEngine().swapToYamlConfigurations(configs));
        return result;
    }
    
    private void persistAuthentication(final Collection<ShardingSphereUser> users, final boolean isOverwrite) {
        if (!users.isEmpty() && (isOverwrite || !hasAuthentication())) {
            repository.persist(node.getAuthenticationPath(),
                    YamlEngine.marshal(new UserRuleYamlSwapper().swapToYamlConfiguration(users)));
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
        String schemaNames = repository.get(node.getMetadataNodePath());
        if (Strings.isNullOrEmpty(schemaNames)) {
            repository.persist(node.getMetadataNodePath(), schemaName);
            return;
        }
        List<String> schemaNameList = Splitter.on(",").splitToList(schemaNames);
        if (schemaNameList.contains(schemaName)) {
            return;
        }
        List<String> newArrayList = new ArrayList<>(schemaNameList);
        newArrayList.add(schemaName);
        repository.persist(node.getMetadataNodePath(), Joiner.on(",").join(newArrayList));
    }
    
    /**
     * Load data source configurations.
     *
     * @param schemaName schema name
     * @return data source configurations
     */
    public Map<String, DataSourceConfiguration> loadDataSourceConfigurations(final String schemaName) {
        return hasDataSourceConfiguration(schemaName) 
                ? YamlConfigurationConverter.convertDataSourceConfigurations(repository.get(node.getDataSourcePath(schemaName))) : new LinkedHashMap<>();
    }
    
    /**
     * Load rule configurations.
     *
     * @param schemaName schema name
     * @return rule configurations
     */
    public Collection<RuleConfiguration> loadRuleConfigurations(final String schemaName) {
        return hasRuleConfiguration(schemaName) 
                ? YamlConfigurationConverter.convertRuleConfigurations(repository.get(node.getRulePath(schemaName))) : new LinkedList<>();
    }
    
    /**
     * Load user rule.
     *
     * @return authentication
     */
    public Collection<ShardingSphereUser> loadUserRule() {
        return hasAuthentication()
                ? YamlConfigurationConverter.convertUserRule(repository.get(node.getAuthenticationPath()))
                : Collections.emptyList();
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        return Strings.isNullOrEmpty(repository.get(node.getPropsPath())) ? new Properties() 
                : YamlConfigurationConverter.convertProperties(repository.get(node.getPropsPath()));
    }
    
    /**
     * Get all schema names.
     * 
     * @return all schema names
     */
    public Collection<String> getAllSchemaNames() {
        String schemaNames = repository.get(node.getMetadataNodePath());
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
     * Persist ShardingSphere schema.
     *
     * @param schemaName schema name
     * @param schema ShardingSphere schema
     */
    public void persistSchema(final String schemaName, final ShardingSphereSchema schema) {
        repository.persist(node.getSchemaPath(schemaName), YamlEngine.marshal(new SchemaYamlSwapper().swapToYamlConfiguration(schema)));
    }
    
    /**
     * Load ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public Optional<ShardingSphereSchema> loadSchema(final String schemaName) {
        String path = repository.get(node.getSchemaPath(schemaName));
        if (Strings.isNullOrEmpty(path)) {
            return Optional.empty();
        }
        return Optional.of(new SchemaYamlSwapper().swapToObject(YamlEngine.unmarshal(path, YamlSchema.class)));
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
