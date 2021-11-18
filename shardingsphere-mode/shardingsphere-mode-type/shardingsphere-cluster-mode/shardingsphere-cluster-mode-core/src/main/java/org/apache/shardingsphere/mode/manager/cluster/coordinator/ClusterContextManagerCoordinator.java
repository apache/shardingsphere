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
import lombok.extern.slf4j.Slf4j;
import org.apache.shardingsphere.authority.rule.AuthorityRule;
import org.apache.shardingsphere.infra.eventbus.ShardingSphereEventBus;
import org.apache.shardingsphere.infra.metadata.schema.QualifiedSchema;
import org.apache.shardingsphere.infra.rule.event.impl.DataSourceNameDisabledEvent;
import org.apache.shardingsphere.infra.rule.event.impl.PrimaryDataSourceChangedEvent;
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
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.DisabledStateChangedEvent;
import org.apache.shardingsphere.mode.manager.cluster.coordinator.registry.status.storage.event.PrimaryStateChangedEvent;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;

import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.Optional;

/**
 * Cluster context manager coordinator.
 */
@Slf4j
public final class ClusterContextManagerCoordinator {
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final ContextManager contextManager;
    
    public ClusterContextManagerCoordinator(final MetaDataPersistService metaDataPersistService, final ContextManager contextManager) {
        this.metaDataPersistService = metaDataPersistService;
        this.contextManager = contextManager;
        ShardingSphereEventBus.getInstance().register(this);
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
        contextManager.alterProps(event.getProps());
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
        contextManager.alterSchema(event.getSchemaName(), event.getSchema());
    }
    
    /**
     * Renew rule configurations.
     *
     * @param event rule configurations changed event
     */
    @Subscribe
    public synchronized void renew(final RuleConfigurationsChangedEvent event) {
        contextManager.alterRuleConfiguration(event.getSchemaName(), event.getRuleConfigurations());
    }
    
    /**
     * Renew data source configuration.
     *
     * @param event data source changed event.
     */
    @Subscribe
    public synchronized void renew(final DataSourceChangedEvent event) {
        contextManager.alterDataSourceConfiguration(event.getSchemaName(), event.getDataSourceConfigurations());
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
    
    private void persistSchema(final String schemaName) {
        if (!metaDataPersistService.getDataSourceService().isExisted(schemaName)) {
            metaDataPersistService.getDataSourceService().persist(schemaName, new LinkedHashMap<>());
        }
        if (!metaDataPersistService.getSchemaRuleService().isExisted(schemaName)) {
            metaDataPersistService.getSchemaRuleService().persist(schemaName, new LinkedList<>());
        }
    }
}
