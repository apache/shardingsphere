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
import org.apache.shardingsphere.governance.core.registry.service.config.impl.PropertiesRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.config.impl.SchemaRuleRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.schema.SchemaRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.state.DataSourceStatusRegistryService;
import org.apache.shardingsphere.governance.core.registry.service.state.LockRegistryService;
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
import org.apache.shardingsphere.infra.metadata.schema.refresher.event.SchemaAlteredEvent;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUser;
import org.apache.shardingsphere.infra.metadata.user.ShardingSphereUsers;
import org.apache.shardingsphere.infra.metadata.user.yaml.config.YamlUsersConfigurationConverter;
import org.apache.shardingsphere.infra.yaml.engine.YamlEngine;
import org.apache.shardingsphere.infra.yaml.swapper.YamlRuleConfigurationSwapperEngine;

import java.util.ArrayList;
import java.util.Collection;
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
    private final SchemaRegistryService schemaService;
    
    @Getter
    private final DataSourceStatusRegistryService dataSourceStatusService;
    
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
        schemaService = new SchemaRegistryService(repository);
        dataSourceStatusService = new DataSourceStatusRegistryService(repository);
        lockService = new LockRegistryService(repository);
        ShardingSphereEventBus.getInstance().register(this);
    }
    
    /**
     * Persist configurations.
     *
     * @param dataSourceConfigs schema and data source configuration map
     * @param schemaRuleConfigs schema and rule configuration map
     * @param globalRuleConfigs global rule configurations
     * @param props properties
     * @param isOverwrite whether overwrite registry center's configuration if existed
     */
    public void persistConfigurations(final Map<String, Map<String, DataSourceConfiguration>> dataSourceConfigs, final Map<String, Collection<RuleConfiguration>> schemaRuleConfigs, 
                                      final Collection<RuleConfiguration> globalRuleConfigs, final Properties props, final boolean isOverwrite) {
        globalRuleService.persist(globalRuleConfigs, isOverwrite);
        propsService.persist(props, isOverwrite);
        for (Entry<String, Map<String, DataSourceConfiguration>> entry : dataSourceConfigs.entrySet()) {
            String schemaName = entry.getKey();
            dataSourceService.persist(schemaName, dataSourceConfigs.get(schemaName), isOverwrite);
            schemaRuleService.persist(schemaName, schemaRuleConfigs.get(schemaName), isOverwrite);
            // TODO persistSchemaName is for etcd to get SchemaName which is for impl details, should move the logic into etcd repository, just keep reg center clear and high abstract.
            persistSchemaName(entry.getKey());
        }
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
    
    @SuppressWarnings("unchecked")
    private Collection<RuleConfiguration> loadCachedRuleConfigurations(final String schemaName, final String ruleConfigCacheId) {
        return new YamlRuleConfigurationSwapperEngine().swapToRuleConfigurations(
                YamlEngine.unmarshal(registryCacheManager.loadCache(node.getRulePath(schemaName), ruleConfigCacheId), Collection.class));
    }
    
    /**
     * Persist rule configurations.
     *
     * @param event rule configurations altered event
     */
    @Subscribe
    public void renew(final RuleConfigurationsAlteredEvent event) {
        schemaRuleService.persist(event.getSchemaName(), event.getRuleConfigurations());
    }
    
    /**
     * Persist meta data.
     *
     * @param event meta data created event
     */
    @Subscribe
    public void renew(final MetaDataCreatedEvent event) {
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
    public void renew(final MetaDataDroppedEvent event) {
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
    public void renew(final SchemaAlteredEvent event) {
        schemaService.persist(event.getSchemaName(), event.getSchema());
    }
    
    /**
     * Switch rule configuration.
     *
     * @param event switch rule configuration event
     */
    @Subscribe
    public void renew(final SwitchRuleConfigurationEvent event) {
        schemaRuleService.persist(event.getSchemaName(), loadCachedRuleConfigurations(event.getSchemaName(), event.getRuleConfigurationCacheId()));
        registryCacheManager.deleteCache(node.getRulePath(event.getSchemaName()), event.getRuleConfigurationCacheId());
    }
    
    /**
     * Rule configuration cached.
     *
     * @param event rule configuration cached event
     */
    @Subscribe
    public void renew(final RuleConfigurationCachedEvent event) {
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
    public void renew(final CreateUserStatementEvent event) {
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
    public void renew(final GrantStatementEvent event) {
        if (!event.getUsers().isEmpty()) {
            repository.persist(node.getPrivilegeNodePath(), YamlEngine.marshal(YamlUsersConfigurationConverter.convertYamlUserConfigurations(event.getUsers())));
        }
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
            shardingSphereUsers.findUser(each.getGrantee()).ifPresent(result::remove);
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
     * Register instance online.
     */
    public void registerInstanceOnline() {
        repository.persistEphemeral(node.getProxyNodePath(instanceId), "");
    }
    
    /**
     * Initialize nodes.
     */
    public void initNodes() {
        repository.persist(node.getDataNodesPath(), "");
        repository.persist(node.getPrimaryNodesPath(), "");
    }
}
