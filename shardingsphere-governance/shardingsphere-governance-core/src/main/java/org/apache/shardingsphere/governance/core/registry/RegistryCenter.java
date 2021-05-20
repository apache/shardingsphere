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
import lombok.Getter;
import org.apache.shardingsphere.authority.api.config.AuthorityRuleConfiguration;
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
import org.apache.shardingsphere.governance.core.registry.service.config.impl.DataSourceRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.GlobalRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.state.LockRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.PropertiesRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.SchemaRuleRegistryService;
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
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlDataSourceConfigurationSwapper;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

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
 * Registry center.
 */
public final class RegistryCenter {
    
    private final String instanceId;
    
    private final RegistryCenterRepository repository;
    
    private final RegistryCenterNode node;
    
    private final RegistryCacheManager registryCacheManager;
    
    @Getter
    private final DataSourceRegistryService dataSourceService;
    
    @Getter
    private final SchemaRuleRegistryService schemaRuleService;
    
    @Getter
    private final GlobalRuleRegistryService globalRuleService;
    
    @Getter
    private final PropertiesRegistryService propsService;
    
    @Getter
    private final LockRegistryService lockService;
    
    public RegistryCenter(final RegistryCenterRepository repository) {
        instanceId = GovernanceInstance.getInstance().getId();
        this.repository = repository;
        node = new RegistryCenterNode();
        registryCacheManager = new RegistryCacheManager(repository, node);
        dataSourceService = new DataSourceRegistryService(repository);
        schemaRuleService = new SchemaRuleRegistryService(repository);
        globalRuleService = new GlobalRuleRegistryService(repository);
        propsService = new PropertiesRegistryService(repository);
        lockService = new LockRegistryService(repository);
        ShardingSphereEventBus.getInstance().register(this);
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
        dataSourceService.persist(schemaName, dataSourceConfigs, isOverwrite);
        schemaRuleService.persist(schemaName, ruleConfigs, isOverwrite);
        // TODO persistSchemaName is for etcd to get SchemaName which is for impl details, should move the logic into etcd repository, just keep reg center clear and high abstract. 
        persistSchemaName(schemaName);
    }
    
    private void persistSchemaName(final String schemaName) {
        String schemaNamesStr = repository.get(node.getMetadataNodePath());
        if (Strings.isNullOrEmpty(schemaNamesStr)) {
            repository.persist(node.getMetadataNodePath(), schemaName);
            return;
        }
        Collection<String> schemaNames = Splitter.on(",").splitToList(schemaNamesStr);
        if (schemaNames.contains(schemaName)) {
            return;
        }
        Collection<String> newSchemaNames = new ArrayList<>(schemaNames);
        newSchemaNames.add(schemaName);
        repository.persist(node.getMetadataNodePath(), String.join(",", newSchemaNames));
    }
    
    /**
     * Persist global configuration.
     *
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite is overwrite config center's configuration
     */
    public void persistGlobalConfiguration(final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
    }
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(registryCacheManager.loadCache(node.getRulePath(schemaName), ruleConfigCacheId), Collection.class));
    }
    
    private void addDataSourceConfigurations(final String schemaName, final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        Map<String, DataSourceConfiguration> dataSourceConfigMap = dataSourceService.load(schemaName);
        dataSourceConfigMap.putAll(dataSourceConfigs);
        repository.persist(node.getMetadataDataSourcePath(schemaName), YamlEngine.marshal(createYamlDataSourceConfiguration(dataSourceConfigMap)));
    }
    
    private Map<String, Map<String, Object>> createYamlDataSourceConfiguration(final Map<String, DataSourceConfiguration> dataSourceConfigs) {
        return dataSourceConfigs.entrySet().stream()
                .collect(Collectors.toMap(Entry::getKey, entry -> new YamlDataSourceConfigurationSwapper().swapToMap(entry.getValue()), (oldValue, currentValue) -> oldValue, LinkedHashMap::new));
    }
    
    private void persistChangedPrivilege(final Collection<ShardingSphereUser> users) {
        if (!users.isEmpty()) {
            repository.persist(node.getPrivilegeNodePath(), YamlEngine.marshal(YamlUsersConfigurationConverter.convertYamlUserConfigurations(users)));
        }
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
        dataSourceService.persist(event.getSchemaName(), event.getDataSourceConfigurations());
    }
    
    /**
     * Persist rule configurations.
     *
     * @param event rule configurations altered event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsAlteredEvent event) {
        schemaRuleService.persist(event.getSchemaName(), event.getRuleConfigurations());
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
        schemaRuleService.persist(event.getSchemaName(), loadCachedRuleConfigurations(event.getSchemaName(), event.getRuleConfigurationCacheId()));
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
     * Renew create user statement.
     *
     * @param event create user statement event
     */
    @Subscribe
    public synchronized void renew(final CreateUserStatementEvent event) {
        Collection<RuleConfiguration> globalRuleConfigs = globalRuleService.load();
        Optional<AuthorityRuleConfiguration> authorityRuleConfig = globalRuleConfigs.stream().filter(each -> each instanceof AuthorityRuleConfiguration)
                .findAny().map(each -> (AuthorityRuleConfiguration) each);
        Preconditions.checkState(authorityRuleConfig.isPresent());
        refreshAuthorityRuleConfiguration(authorityRuleConfig.get(), event.getUsers());
        globalRuleService.persist(globalRuleConfigs, true);
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
}
