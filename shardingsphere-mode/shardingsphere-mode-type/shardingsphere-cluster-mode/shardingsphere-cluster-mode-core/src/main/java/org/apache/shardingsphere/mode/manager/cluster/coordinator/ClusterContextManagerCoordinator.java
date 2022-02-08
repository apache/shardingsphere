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

package org.apache.shardingsphere.mode.manager.cluster.coordinator;

import com.google.common.eventbus.Subscribe;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
import org.apache.shardingsphere.infra.rule.identifier.type.InstanceAwareRule;
import org.apache.shardingsphere.infra.rule.identifier.type.StatusContainedRule;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.authority.event.AuthorityChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.datasource.DataSourceChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.props.PropertiesChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.GlobalRuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.rule.RuleConfigurationsChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.config.event.schema.SchemaChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaAddedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.metadata.event.SchemaDeletedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.LabelsEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.StateEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.compute.event.WorkerIdEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.StorageNodeStatus;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Cluster context manager coordinator.
 */
public final class ClusterContextManagerCoordinator {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ContextManager contextManager;
    
    private final RegistryCenter registryCenter;
    
    public ClusterContextManagerCoordinator(final MetaDataPersistService metaDataPersistService, final ContextManager contextManager, final RegistryCenter registryCenter) {
        this.metaDataPersistService = metaDataPersistService;
        this.contextManager = contextManager;
        this.registryCenter = registryCenter;
        ShardingSphereEventBus.getInstance().register(this);
        buildSpecialRules();
    }
    
    /**
     * Renew to persist meta data.
     *
     * @param event schema added event
     * @throws SQLException SQL exception
     */
    @Subscribe
    public synchronized void renew(final SchemaAddedEvent event) throws SQLException {
        persistSchema(event.getSchemaName());
        contextManager.addSchema(event.getSchemaName());
    }
    
    /**
     * Renew to delete schema.
     *
     * @param event schema delete event
     */
    @Subscribe
    public synchronized void renew(final SchemaDeletedEvent event) {
        contextManager.deleteSchema(event.getSchemaName());
    }
    
    /**
     * Renew properties.
     *
     * @param event properties changed event
     */
    @Subscribe
    public synchronized void renew(final PropertiesChangedEvent event) {
        contextManager.alterProperties(event.getProps());
    }
    
    /**
     * Renew authority.
     *
     * @param event authority changed event
     */
    @Subscribe
    public synchronized void renew(final AuthorityChangedEvent event) {
        Optional<AuthorityRule> rule = contextManager.getMetaDataContexts().getGlobalRuleMetaData().getRules().stream()
                .filter(each -> each instanceof AuthorityRule).findAny().map(each -> (AuthorityRule) each);
        rule.ifPresent(optional -> optional.refresh(contextManager.getMetaDataContexts().getMetaDataMap(), event.getUsers()));
    }
    
    /**
     * Renew meta data of the schema.
     *
     * @param event meta data changed event
     */
    @Subscribe
    public synchronized void renew(final SchemaChangedEvent event) {
        contextManager.alterSchema(event.getSchemaName(), event.getChangedTableMetaData(), event.getDeletedTable());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        contextManager.alterRuleConfiguration(event.getSchemaName(), event.getRuleConfigurations());
        buildSpecialRules();
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) {
        contextManager.alterDataSourceConfiguration(event.getSchemaName(), event.getDataSourcePropertiesMap());
        buildSpecialRules();
    }
    
    /**
     * Renew disabled data source names.
     *
     * @param event disabled state changed event
     */
    @Subscribe
    public synchronized void renew(final DisabledStateChangedEvent event) {
        QualifiedSchema qualifiedSchema = event.getQualifiedSchema();
        contextManager.getMetaDataContexts().getMetaDataMap().get(qualifiedSchema.getSchemaName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each)
                        .updateStatus(new DataSourceNameDisabledEvent(qualifiedSchema.getDataSourceName(), event.isDisabled())));
    }
    
    /**
     * Renew primary data source names.
     *
     * @param event primary state changed event
     */
    @Subscribe
    public synchronized void renew(final PrimaryStateChangedEvent event) {
        QualifiedSchema qualifiedSchema = event.getQualifiedSchema();
        contextManager.getMetaDataContexts().getMetaDataMap().get(qualifiedSchema.getSchemaName()).getRuleMetaData().getRules()
                .stream()
                .filter(each -> each instanceof StatusContainedRule)
                .forEach(each -> ((StatusContainedRule) each)
                        .updateStatus(new PrimaryDataSourceChangedEvent(qualifiedSchema.getSchemaName(), qualifiedSchema.getDataSourceName(), event.getPrimaryDataSourceName())));
    }
    
    /**
     * Renew global rule configurations.
     *
     * @param event global rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final GlobalRuleConfigurationsChangedEvent event) {
        contextManager.alterGlobalRuleConfiguration(event.getRuleConfigurations());
    }
    
    /**
     * Renew instance status.
     *
     * @param event state event
     */
    @Subscribe
    public synchronized void renew(final StateEvent event) {
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().getId().equals(event.getInstanceId())) {
            contextManager.getInstanceContext().updateInstanceStatus(event.getStatus());
        }
    }
    
    /**
     * Renew instance worker id.
     *
     * @param event worker id event
     */
    @Subscribe
    public synchronized void renew(final WorkerIdEvent event) {
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().getId().equals(event.getInstanceId())) {
            contextManager.getInstanceContext().updateWorkerId(event.getWorkerId());
        }
    }
    
    /**
     * Renew instance labels.
     * 
     * @param event label event
     */
    @Subscribe
    public synchronized void renew(final LabelsEvent event) {
        if (contextManager.getInstanceContext().getInstance().getInstanceDefinition().getInstanceId().getId().equals(event.getInstanceId())) {
            contextManager.getInstanceContext().updateLabel(event.getLabels());
        }
    }
    
    private void persistSchema(final String schemaName) {
        if (!metaDataPersistService.getDataSourceService().isExisted(schemaName)) {
            metaDataPersistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!metaDataPersistService.getSchemaRuleService().isExisted(schemaName)) {
            metaDataPersistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
    
    private void buildSpecialRules() {
        contextManager.getMetaDataContexts().getMetaDataMap().forEach((key, value) -> value.getRuleMetaData().getRules().forEach(each -> {
            if (each instanceof StatusContainedRule) {
                disableDataSources(key, (StatusContainedRule) each);
            } else if (each instanceof InstanceAwareRule) {
                ((InstanceAwareRule) each).setInstanceContext(contextManager.getInstanceContext());
            }
        }));
    }
    
    private void disableDataSources(final String schemaName, final StatusContainedRule rule) {
        Collection<String> disabledDataSources = registryCenter.getStorageNodeStatusService().loadStorageNodes(schemaName, StorageNodeStatus.DISABLE);
        disabledDataSources.stream().map(this::getDataSourceName).forEach(each -> rule.updateStatus(new DataSourceNameDisabledEvent(each, true)));
    }
    
    private String getDataSourceName(final String disabledDataSource) {
        return new QualifiedSchema(disabledDataSource).getDataSourceName();
    }
}
