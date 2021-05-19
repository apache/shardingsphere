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

package org.apache.shardingsphere.governance.core.registry;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Splitter;
import com.google.common.base.Strings;
import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
import org.apache.shardingsphere.governance.core.lock.node.LockAck;
import org.apache.shardingsphere.governance.core.lock.node.LockNode;
import org.apache.shardingsphere.governance.core.registry.checker.RuleConfigurationChecker;
import org.apache.shardingsphere.governance.core.registry.checker.RuleConfigurationCheckerFactory;
import org.apache.shardingsphere.governance.core.registry.instance.GovernanceInstance;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAddedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.datasource.DataSourceAlteredEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.invocation.ExecuteProcessReportEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.invocation.ExecuteProcessSummaryReportEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.invocation.ExecuteProcessUnitReportEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.invocation.ShowProcessListRequestEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.invocation.ShowProcessListResponseEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataCreatedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.metadata.MetaDataDroppedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationCachedEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.RuleConfigurationsAlteredEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.rule.SwitchRuleConfigurationEvent;
import org.apache.shardingsphere.governance.core.registry.listener.event.scaling.StartScalingEvent;
import org.apache.shardingsphere.governance.core.yaml.schema.pojo.YamlSchema;
import org.apache.shardingsphere.governance.core.yaml.schema.swapper.SchemaYamlSwapper;
import org.apache.shardingsphere.governance.repository.spi.RegistryCenterRepository;
import org.apache.shardingsphere.infra.config.RuleConfiguration;
import org.apache.shardingsphere.infra.config.datasource.DataSourceConfiguration;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessConstants;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.ExecuteProcessUnit;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessContext;
import org.apache.shardingsphere.infra.executor.sql.process.model.yaml.YamlExecuteProcessUnit;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.CreateUserStatementEvent;
import org.apache.shardingsphere.infra.metadata.mapper.event.dcl.impl.GrantStatementEvent;
import org.apache.shardingsphere.infra.metadata.schema.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceEvent;
import org.apache.shardingsphere.infra.yaml.config.YamlRuleConfiguration;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
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
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Registry center.
 */
public final class RegistryCenter {
    
    private static final int CHECK_ACK_MAXIMUM = 5;
    
    private static final int CHECK_ACK_INTERVAL_SECONDS = 1;
    
    private final String instanceId;
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node;
    
    private final LockNode lockNode;
    
    private final RegistryCacheManager registryCacheManager;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        this.repository = repository;
        node = new RegistryCenterNode();
        lockNode = new LockNode();
        initLockNode();
        registryCacheManager = new RegistryCacheManager(repository, node);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    private void initLockNode() {
        repository.persist(lockNode.getLockRootNodePath(), "");
        repository.persist(lockNode.getLockedAckRootNodePah(), "");
    }
    
    /**
     * Persist rule configuration.
     *
     * @param schemaName schema name
     * @param dataSourceConfigs data source configuration map
     * @param ruleConfigs rule configurations
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistConfigurations(final String schemaName, 
                                      final Map<String, DataSourceConfiguration> dataSourceConfigs, final Collection<RuleConfiguration> ruleConfigs, final boolean isOverwrite) {
        persistDataSourceConfigurations(schemaName, dataSourceConfigs, isOverwrite);
        persistRuleConfigurations(schemaName, ruleConfigs, isOverwrite);
        // TODO Consider removing the following one.
        persistSchemaName(schemaName);
    }
    
    private void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs, final boolean isOverwrite) {
        if (!dataSourceConfigs.isEmpty() && (isOverwrite || !hasDataSourceConfiguration(schemaName))) {
            persistDataSourceConfigurations(schemaName, dataSourceConfigs);
        }
    }
    
    /**
     * Persist data source configurations.
     *
     * @param schemaName schema name
     * @param dataSourceConfigs data source configurations
     */
    public void persistDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        repository.persist(node.getMetadataDataSourcePath(schemaName), YamlEngine.marshal(createYamlDataSourceConfiguration(dataSourceConfigs)));
    }
    
    private void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigs, final boolean isOverwrite) {
        if (!ruleConfigs.isEmpty() && (isOverwrite || !hasRuleConfiguration(schemaName))) {
            persistRuleConfigurations(schemaName, ruleConfigs);
        }
    }
    
    /**
     * Persist rule configurations.
     *
     * @param schemaName schema name
     * @param ruleConfigs rule configurations
     */
    public void persistRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigs) {
        repository.persist(node.getRulePath(schemaName), YamlEngine.marshal(createYamlRuleConfigurations(schemaName, ruleConfigs)));
    }
    
    /**
     * Persist global configuration.
     *
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistGlobalConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        persistGlobalRuleConfigurations(globalRuleConfigs, isOverwrite);
        persistProperties(props, isOverwrite);
    }
    
    private void persistGlobalRuleConfigurations(final Collection<RuleConfiguration> globalRuleConfigs, final boolean isOverwrite) {
        if (!globalRuleConfigs.isEmpty() && (isOverwrite || !hasGlobalRuleConfigurations())) {
            repository.persist(node.getGlobalRuleNode(), YamlEngine.marshal(new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(globalRuleConfigs)));
        }
    }
    
    private void persistProperties(final Properties props, final boolean isOverwrite) {
        if (!props.isEmpty() && (isOverwrite || !hasProperties())) {
            repository.persist(node.getPropsPath(), YamlEngine.marshal(props));
        }
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(registryCacheManager.loadCache(node.getRulePath(schemaName), ruleConfigCacheId), Collection.class));
    }
    
    private void addDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = loadDataSourceConfigurations(schemaName);
        dataSourceConfigMap.putAll(dataSourceConfigs);
        repository.persist(node.getMetadataDataSourcePath(schemaName), YamlEngine.marshal(createYamlDataSourceConfiguration(dataSourceConfigMap)));
    }
    
    private Map<String, Map<String, Object>> createYamlDataSourceConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private Collection<YamlRuleConfiguration> createYamlRuleConfigurations(final String schemaName, final Collection<RuleConfiguration> ruleConfigs) {
        Collection<RuleConfiguration> configs = new LinkedList<>();
        for (RuleConfiguration each : ruleConfigs) {
            Optional<RuleConfigurationChecker> checker = RuleConfigurationCheckerFactory.newInstance(each);
            if (checker.isPresent()) {
                checker.get().check(schemaName, each);
                configs.add(each);
            }
        }
        return new YamlRuleConfigurationSwapperEngine().swapToYamlRuleConfigurations(configs);
    }
    
    private void persistChangedPrivilege(final Collection<ShardingSphereUser> users) {
        if (!users.isEmpty()) {
            repository.persist(node.getPrivilegeNodePath(), YamlEngine.marshal(YamlUsersConfigurationConverter.convertYamlUserConfigurations(users)));
        }
    }
    
    private boolean hasGlobalRuleConfigurations() {
        return !Strings.isNullOrEmpty(repository.get(node.getGlobalRuleNode()));
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
        return hasDataSourceConfiguration(schemaName) ? getDataSourceConfigurations(repository.get(node.getMetadataDataSourcePath(schemaName))) : new LinkedHashMap<>();
    }
    
    @SuppressWarnings("unchecked")
    private static Map<String, DataSourceConfiguration> getDataSourceConfigurations(final String yamlContent) {
        Map<String, Map<String, Object>> yamlDataSources = YamlEngine.unmarshal(yamlContent, Map.class);
        if (yamlDataSources.isEmpty()) {
            return new LinkedHashMap<>();
        }
        Map<String, DataSourceConfiguration> result = new LinkedHashMap<>(yamlDataSources.size());
        yamlDataSources.forEach((key, value) -> result.put(key, new YamlDataSourceConfigurationSwapper().swapToDataSourceConfiguration(value)));
        return result;
    }
    
    /**
     * Load rule configurations.
     *
     * @param schemaName schema name
     * @return rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> loadRuleConfigurations(final String schemaName) {
        return hasRuleConfiguration(schemaName)
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(node.getRulePath(schemaName)), Collection.class)) : new LinkedList<>();
    }
    
    /**
     * Load global rule configurations.
     * 
     * @return global rule configurations
     */
    @SuppressWarnings("unchecked")
    public Collection<RuleConfiguration> loadGlobalRuleConfigurations() {
        return hasGlobalRuleConfigurations()
                ? new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(YamlEngine.unmarshal(repository.get(node.getGlobalRuleNode()), Collection.class)) : Collections.emptyList();
    }
    
    /**
     * Load properties configuration.
     *
     * @return properties
     */
    public Properties loadProperties() {
        return Strings.isNullOrEmpty(repository.get(node.getPropsPath())) ? new Properties() : YamlEngine.unmarshal(repository.get(node.getPropsPath()), Properties.class);
    }
    
    /**
     * Load all schema names.
     *
     * @return all schema names
     */
    public Collection<String> loadAllSchemaNames() {
        String schemaNames = repository.get(node.getMetadataNodePath());
        return Strings.isNullOrEmpty(schemaNames) ? new LinkedList<>() : node.splitSchemaName(schemaNames);
    }
    
    /**
     * Judge whether schema has data source configuration.
     *
     * @param schemaName schema name
     * @return has data source configuration or not
     */
    public boolean hasDataSourceConfiguration(final String schemaName) {
        return !Strings.isNullOrEmpty(repository.get(node.getMetadataDataSourcePath(schemaName)));
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
     * Persist ShardingSphere schema.
     *
     * @param schemaName schema name
     * @param schema ShardingSphere schema
     */
    public void persistSchema(final String schemaName, final ShardingSphereSchema schema) {
        repository.persist(node.getMetadataSchemaPath(schemaName), YamlEngine.marshal(new SchemaYamlSwapper().swapToYamlConfiguration(schema)));
    }
    
    /**
     * Load ShardingSphere schema.
     *
     * @param schemaName schema name
     * @return ShardingSphere schema
     */
    public Optional<ShardingSphereSchema> loadSchema(final String schemaName) {
        String path = repository.get(node.getMetadataSchemaPath(schemaName));
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
    
    /**
     * Persist data source disabled state.
     *
     * @param event data source disabled event
     */
    @Subscribe
    public synchronized void renew(final DataSourceDisabledEvent event) {
        String value = event.isDisabled() ? RegistryCenterNodeStatus.DISABLED.toString() : "";
        repository.persist(node.getDataSourcePath(event.getSchemaName(), event.getDataSourceName()), value);
    }
    
    /**
     * Persist primary data source state.
     *
     * @param event primary data source event
     */
    @Subscribe
    public synchronized void renew(final PrimaryDataSourceEvent event) {
        repository.persist(node.getPrimaryDataSourcePath(event.getSchemaName(), event.getGroupName()), event.getDataSourceName());
    }
    
    /**
     * persist data source configurations.
     *
     * @param event Data source added event
     */
    @Subscribe
    public synchronized void renew(final DataSourceAddedEvent event) {
        addDataSourceConfigurations(event.getSchemaName(), event.getDataSourceConfigurations());
    }
    
    /**
     * Change data source configurations.
     *
     * @param event Data source altered event
     */
    @Subscribe
    public synchronized void renew(final DataSourceAlteredEvent event) {
        persistDataSourceConfigurations(event.getSchemaName(), event.getDataSourceConfigurations());
    }
    
    /**
     * Persist rule configurations.
     *
     * @param event rule configurations altered event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsAlteredEvent event) {
        //TODO
        persistRuleConfigurations(event.getSchemaName(), event.getRuleConfigurations());
    }
    
    /**
     * Persist meta data.
     *
     * @param event meta data created event
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
     * @param event schema altered event
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
        registryCacheManager.deleteCache(node.getRulePath(event.getSchemaName()), event.getRuleConfigurationCacheId());
    }
    
    /**
     * Rule configuration cached.
     *
     * @param event rule configuration cached event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationCachedEvent event) {
        StartScalingEvent startScalingEvent = new StartScalingEvent(event.getSchemaName(),
                repository.get(node.getMetadataDataSourcePath(event.getSchemaName())),
                repository.get(node.getRulePath(event.getSchemaName())),
                registryCacheManager.loadCache(node.getRulePath(event.getSchemaName()), event.getCacheId()), event.getCacheId());
        ShardingSphereEventBus.getInstance().post(startScalingEvent);
    }
    
    /**
     * User configuration event.
     *
     * @param event user configuration event
     */
    @Subscribe
    public synchronized void renew(final CreateUserStatementEvent event) {
        Collection<RuleConfiguration> globalRuleConfigs = loadGlobalRuleConfigurations();
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .findAny().map(each -> (AuthorityRuleConfiguration) each);
        Preconditions.checkState(authorityRuleConfig.isPresent());
        refreshAuthorityRuleConfiguration(authorityRuleConfig.get(), event.getUsers());
        persistGlobalRuleConfigurations(globalRuleConfigs, true);
    }
    
    /**
     * User with changed privilege event.
     *
     * @param event grant event
     */
    @Subscribe
    public synchronized void renew(final GrantStatementEvent event) {
        persistChangedPrivilege(event.getUsers());
    }
    
    private void refreshAuthorityRuleConfiguration(final AuthorityRuleConfiguration authRuleConfig, final Collection<ShardingSphereUser> createUsers) {
        Collection<ShardingSphereUser> oldUsers = authRuleConfig.getUsers();
        Collection<ShardingSphereUser> newUsers = oldUsers.isEmpty() ? createUsers : getChangedShardingSphereUsers(oldUsers, createUsers);
        authRuleConfig.getUsers().removeAll(oldUsers);
        authRuleConfig.getUsers().addAll(newUsers);
    }
    
    private Collection<ShardingSphereUser> getChangedShardingSphereUsers(final Collection<ShardingSphereUser> oldUsers, final Collection<ShardingSphereUser> newUsers) {
        Collection<ShardingSphereUser> result = new LinkedList<>(oldUsers);
        ShardingSphereUsers shardingSphereUsers = new ShardingSphereUsers(oldUsers);
        for (ShardingSphereUser each : newUsers) {
            Optional<ShardingSphereUser> oldUser = shardingSphereUsers.findUser(each.getGrantee());
            if (oldUser.isPresent()) {
                result.remove(oldUser);
            }
            result.add(each);
        }
        return result;
    }
    
    /**
     * Load show process list data.
     *
     * @param event get children request event.
     */
    @Subscribe
    public void loadShowProcessListData(final ShowProcessListRequestEvent event) {
        List<String> childrenKeys = repository.getChildrenKeys(node.getExecutionNodesPath());
        Collection<String> processListData = childrenKeys.stream().map(key -> repository.get(node.getExecutionPath(key))).collect(Collectors.toList());
        ShardingSphereEventBus.getInstance().post(new ShowProcessListResponseEvent(processListData));
    }
    
    /**
     * Report execute process summary.
     *
     * @param event execute process summary report event.
     */
    @Subscribe
    public void reportExecuteProcessSummary(final ExecuteProcessSummaryReportEvent event) {
        ExecuteProcessContext executeProcessContext = event.getExecuteProcessContext();
        repository.persist(node.getExecutionPath(executeProcessContext.getExecutionID()), YamlEngine.marshal(new YamlExecuteProcessContext(executeProcessContext)));
    }
    
    /**
     * Report execute process unit.
     *
     * @param event execute process unit report event.
     */
    @Subscribe
    public void reportExecuteProcessUnit(final ExecuteProcessUnitReportEvent event) {
        // TODO lock on the same jvm
        String executionPath = node.getExecutionPath(event.getExecutionID());
        YamlExecuteProcessContext yamlExecuteProcessContext = YamlEngine.unmarshal(repository.get(executionPath), YamlExecuteProcessContext.class);
        ExecuteProcessUnit executeProcessUnit = event.getExecuteProcessUnit();
        for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
            if (unit.getUnitID().equals(executeProcessUnit.getUnitID())) {
                unit.setStatus(executeProcessUnit.getStatus());
            }
        }
        repository.persist(executionPath, YamlEngine.marshal(yamlExecuteProcessContext));
    }
    
    /**
     * Report execute process.
     *
     * @param event execute process report event.
     */
    @Subscribe
    public void reportExecuteProcess(final ExecuteProcessReportEvent event) {
        String executionPath = node.getExecutionPath(event.getExecutionID());
        YamlExecuteProcessContext yamlExecuteProcessContext = YamlEngine.unmarshal(repository.get(executionPath), YamlExecuteProcessContext.class);
        for (YamlExecuteProcessUnit unit : yamlExecuteProcessContext.getUnitStatuses()) {
            if (unit.getStatus() != ExecuteProcessConstants.EXECUTE_STATUS_DONE) {
                return;
            }
        }
        repository.delete(executionPath);
    }
    
    /**
     * Persist instance online.
     */
    public void persistInstanceOnline() {
        repository.persistEphemeral(node.getProxyNodePath(instanceId), "");
    }
    
    /**
     * Initialize data nodes.
     */
    public void persistDataNodes() {
        repository.persist(node.getDataNodesPath(), "");
    }
    
    /**
     * Initialize primary nodes.
     */
    public void persistPrimaryNodes() {
        repository.persist(node.getPrimaryNodesPath(), "");
    }
    
    /**
     * Persist instance data.
     * 
     * @param instanceData instance data
     */
    public void persistInstanceData(final String instanceData) {
        repository.persist(node.getProxyNodePath(instanceId), instanceData);
    }
    
    /**
     * Load instance data.
     * 
     * @return instance data
     */
    public String loadInstanceData() {
        return repository.get(node.getProxyNodePath(instanceId));
    }
    
    /**
     * Load all instances.
     *
     * @return collection of all instances
     */
    public Collection<String> loadAllInstances() {
        return repository.getChildrenKeys(node.getProxyNodesPath());
    }
    
    /**
     * Load disabled data sources.
     * 
     * @param schemaName schema name
     * @return Collection of disabled data sources
     */
    public Collection<String> loadDisabledDataSources(final String schemaName) {
        return loadDataSourcesBySchemaName(schemaName).stream().filter(each -> !Strings.isNullOrEmpty(getDataSourceNodeData(schemaName, each))
                && RegistryCenterNodeStatus.DISABLED.toString().equalsIgnoreCase(getDataSourceNodeData(schemaName, each))).collect(Collectors.toList());
    }
    
    private Collection<String> loadDataSourcesBySchemaName(final String schemaName) {
        return repository.getChildrenKeys(node.getSchemaPath(schemaName));
    }
    
    private String getDataSourceNodeData(final String schemaName, final String dataSourceName) {
        return repository.get(node.getDataSourcePath(schemaName, dataSourceName));
    }
    
    /**
     * Try to get lock.
     *
     * @param lockName lock name
     * @param timeout the maximum time in milliseconds to acquire lock
     * @return true if get the lock, false if not
     */
    public boolean tryLock(final String lockName, final long timeout) {
        return repository.tryLock(lockNode.getLockNodePath(lockName), timeout, TimeUnit.MILLISECONDS);
    }
    
    /**
     * Release lock.
     * 
     * @param lockName lock name
     */
    public void releaseLock(final String lockName) {
        repository.releaseLock(lockNode.getLockNodePath(lockName));
    }
    
    /**
     * Ack lock.
     * 
     * @param lockName lock name
     */
    public void ackLock(final String lockName) {
        repository.persistEphemeral(lockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)), LockAck.LOCKED.getValue());
    }
    
    /**
     * Ack unlock.
     * 
     * @param lockName lock name
     */
    public void ackUnlock(final String lockName) {
        repository.persistEphemeral(lockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)), LockAck.UNLOCKED.getValue());
    }
    
    /**
     * Delete lock ack.
     * 
     * @param lockName lock name
     */
    public void deleteLockAck(final String lockName) {
        repository.delete(lockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName)));
    }
    
    /**
     * Check lock ack.
     * 
     * @param lockName lock name
     * @return true if all instances ack lock, false if not
     */
    public boolean checkLockAck(final String lockName) {
        boolean result = checkAck(loadAllInstances(), lockName, LockAck.LOCKED.getValue());
        if (!result) {
            releaseLock(lockName);
        }
        return result;
    }
    
    /**
     * Check unlock ack.
     * 
     * @param lockName lock name
     * @return true if all instances ack unlock, false if not
     */
    public boolean checkUnlockAck(final String lockName) {
        return checkAck(loadAllInstances(), lockName, LockAck.UNLOCKED.getValue());
    }
    
    private boolean checkAck(final Collection<String> instanceIds, final String lockName, final String ackValue) {
        for (int i = 0; i < CHECK_ACK_MAXIMUM; i++) {
            if (check(instanceIds, lockName, ackValue)) {
                return true;
            }
            try {
                Thread.sleep(CHECK_ACK_INTERVAL_SECONDS * 1000L);
                // CHECKSTYLE:OFF
            } catch (final InterruptedException ex) {
                // CHECKSTYLE:ON
            }
        }
        return false;
    }
    
    private boolean check(final Collection<String> instanceIds, final String lockName, final String ackValue) {
        for (String each : instanceIds) {
            if (!ackValue.equalsIgnoreCase(loadLockAck(each, lockName))) {
                return false;
            }
        }
        return true;
    }
    
    private String loadLockAck(final String instanceId, final String lockName) {
        return Strings.nullToEmpty(repository.get(lockNode.getLockedAckNodePath(Joiner.on("-").join(instanceId, lockName))));
    }
}
