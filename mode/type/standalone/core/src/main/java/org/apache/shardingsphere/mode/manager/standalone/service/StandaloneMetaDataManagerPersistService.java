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

package org.apache.shardingsphere.mode.manager.standalone.service;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaMetaDataPOJO;
import org.apache.shardingsphere.infra.metadata.database.schema.pojo.AlterSchemaPOJO;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.manager.ContextManager;
import org.apache.shardingsphere.mode.manager.switcher.ResourceSwitchManager;
import org.apache.shardingsphere.mode.manager.switcher.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.builder.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.service.manager.ConfigurationManagerService;
import org.apache.shardingsphere.mode.service.persist.MetaDataManagerPersistService;
import org.apache.shardingsphere.single.api.config.SingleRuleConfiguration;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standalone meta data manager persist service.
 */
public final class StandaloneMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final RuleConfigurationEventBuilder ruleConfigurationEventBuilder = new RuleConfigurationEventBuilder();
    
    private final ContextManager contextManager;
    
    public StandaloneMetaDataManagerPersistService(final ContextManager contextManager) {
        this.contextManager = contextManager;
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().addDatabase(databaseName);
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().addDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        contextManager.getManagerServiceFacade().getResourceMetaDataManagerService().dropDatabase(databaseName);
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().dropDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        database.addSchema(schemaName, schema);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService().persistByAlterConfiguration(databaseName, schemaName, schema);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(alterSchemaPOJO.getDatabaseName());
        putSchemaMetaData(database, alterSchemaPOJO.getSchemaName(), alterSchemaPOJO.getRenameSchemaName(), alterSchemaPOJO.getLogicDataSourceName());
        removeSchemaMetaData(database, alterSchemaPOJO.getSchemaName());
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        DatabaseMetaDataPersistService databaseMetaDataService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService();
        databaseMetaDataService.persistByAlterConfiguration(alterSchemaPOJO.getDatabaseName(), alterSchemaPOJO.getRenameSchemaName(), database.getSchema(alterSchemaPOJO.getRenameSchemaName()));
        databaseMetaDataService.getViewMetaDataPersistService().persist(alterSchemaPOJO.getDatabaseName(), alterSchemaPOJO.getRenameSchemaName(),
                database.getSchema(alterSchemaPOJO.getRenameSchemaName()).getViews());
        databaseMetaDataService.dropSchema(alterSchemaPOJO.getDatabaseName(), alterSchemaPOJO.getSchemaName());
    }
    
    private void putSchemaMetaData(final ShardingSphereDatabase database, final String schemaName, final String renameSchemaName, final String logicDataSourceName) {
        ShardingSphereSchema schema = database.getSchema(schemaName);
        database.addSchema(renameSchemaName, schema);
        addDataNode(database, logicDataSourceName, schemaName, schema.getAllTableNames());
    }
    
    private void addDataNode(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName, final Collection<String> tobeAddedTableNames) {
        tobeAddedTableNames.forEach(each -> {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(each, database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, each));
            }
        });
    }
    
    private void addDataNode(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName, final Map<String, ShardingSphereTable> toBeAddedTables,
                             final Map<String, ShardingSphereView> toBeAddedViews) {
        addTablesToDataNode(database, schemaName, logicDataSourceName, toBeAddedTables);
        addViewsToDataNode(database, schemaName, logicDataSourceName, toBeAddedTables, toBeAddedViews);
    }
    
    private void addTablesToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName, final Map<String, ShardingSphereTable> toBeAddedTables) {
        for (Entry<String, ShardingSphereTable> entry : toBeAddedTables.entrySet()) {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(entry.getKey(), database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, entry.getKey()));
            }
            database.getSchema(schemaName).putTable(entry.getKey(), entry.getValue());
        }
    }
    
    private void addViewsToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName,
                                    final Map<String, ShardingSphereTable> toBeAddedTables, final Map<String, ShardingSphereView> toBeAddedViews) {
        for (Entry<String, ShardingSphereView> entry : toBeAddedViews.entrySet()) {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(entry.getKey(), database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, entry.getKey()));
            }
            database.getSchema(schemaName).putTable(entry.getKey(), toBeAddedTables.get(entry.getKey().toLowerCase()));
            database.getSchema(schemaName).putView(entry.getKey(), entry.getValue());
        }
    }
    
    private void removeSchemaMetaData(final ShardingSphereDatabase database, final String schemaName) {
        ShardingSphereSchema schema = new ShardingSphereSchema(database.getSchema(schemaName).getTables(), database.getSchema(schemaName).getViews());
        database.dropSchema(schemaName);
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), Collections.singletonList(schemaName), schema.getAllTableNames());
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRuleAttribute> ruleAttributes, final Collection<String> schemaNames, final Collection<String> tobeRemovedTables) {
        tobeRemovedTables.forEach(each -> ruleAttributes.forEach(rule -> rule.remove(schemaNames, each)));
    }
    
    private void removeDataNode(final ShardingSphereDatabase database, final String schemaName, final Collection<String> tobeRemovedTables, final Collection<String> tobeRemovedViews) {
        removeTablesToDataNode(database, schemaName, tobeRemovedTables);
        removeViewsToDataNode(database, schemaName, tobeRemovedTables, tobeRemovedViews);
    }
    
    private void removeDataNode(final Collection<MutableDataNodeRuleAttribute> ruleAttributes, final String schemaName, final Collection<String> tobeRemovedTables) {
        tobeRemovedTables.forEach(each -> ruleAttributes.forEach(rule -> rule.remove(schemaName, each)));
    }
    
    private void removeTablesToDataNode(final ShardingSphereDatabase database, final String schemaName, final Collection<String> toBeDroppedTables) {
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), schemaName, toBeDroppedTables);
        toBeDroppedTables.forEach(each -> database.getSchema(schemaName).removeTable(each));
    }
    
    private void removeViewsToDataNode(final ShardingSphereDatabase database, final String schemaName, final Collection<String> toBeDroppedTables, final Collection<String> toBeDroppedViews) {
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), schemaName, toBeDroppedViews);
        ShardingSphereSchema schema = database.getSchema(schemaName);
        toBeDroppedTables.forEach(schema::removeTable);
        toBeDroppedViews.forEach(schema::removeView);
    }
    
    @Override
    public void dropSchema(final String databaseName, final Collection<String> schemaNames) {
        Collection<String> tobeRemovedTables = new LinkedList<>();
        Collection<String> tobeRemovedSchemas = new LinkedList<>();
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        for (String each : schemaNames) {
            ShardingSphereSchema schema = new ShardingSphereSchema(database.getSchema(each).getTables(), database.getSchema(each).getViews());
            database.dropSchema(each);
            Optional.of(schema).ifPresent(optional -> tobeRemovedTables.addAll(optional.getAllTableNames()));
            tobeRemovedSchemas.add(each.toLowerCase());
        }
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), new HashSet<>(tobeRemovedSchemas), new HashSet<>(tobeRemovedTables));
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    @Override
    public void alterSchemaMetaData(final AlterSchemaMetaDataPOJO alterSchemaMetaDataPOJO) {
        String databaseName = alterSchemaMetaDataPOJO.getDatabaseName();
        String schemaName = alterSchemaMetaDataPOJO.getSchemaName();
        ShardingSphereMetaData metaData = contextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        Map<String, ShardingSphereTable> tables = alterSchemaMetaDataPOJO.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = alterSchemaMetaDataPOJO.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        addDataNode(database, alterSchemaMetaDataPOJO.getLogicDataSourceName(), schemaName, tables, views);
        removeDataNode(database, schemaName, alterSchemaMetaDataPOJO.getDroppedTables(), alterSchemaMetaDataPOJO.getDroppedViews());
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        DatabaseMetaDataPersistService databaseMetaDataService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService();
        databaseMetaDataService.getTableMetaDataPersistService().persist(databaseName, schemaName, tables);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, schemaName, views);
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) throws SQLException {
        SwitchingResource switchingResource =
                new ResourceSwitchManager().registerStorageUnit(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisteredProps);
        contextManager.getMetaDataContexts().getMetaData().getDatabases().putAll(contextManager.getManagerServiceFacade().getConfigurationManagerService()
                .createChangedDatabases(databaseName, false, switchingResource, null));
        contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(contextManager.getMetaDataContexts().getMetaData().getDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getSchemas()
                .forEach((schemaName, schema) -> contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseMetaDataService()
                        .persistByAlterConfiguration(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName(), schemaName, schema));
        DataSourceUnitPersistService dataSourceService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService();
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeRegisteredProps));
        clearServiceCache();
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException {
        SwitchingResource switchingResource =
                new ResourceSwitchManager().alterStorageUnit(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeUpdatedProps);
        contextManager.getMetaDataContexts().getMetaData().getDatabases().putAll(contextManager.getManagerServiceFacade().getConfigurationManagerService()
                .createChangedDatabases(databaseName, true, switchingResource, null));
        contextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(contextManager.getMetaDataContexts().getMetaData().getDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        DataSourceUnitPersistService dataSourceService = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDataSourceUnitService();
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeUpdatedProps));
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        SwitchingResource switchingResource =
                new ResourceSwitchManager().unregisterStorageUnit(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeDroppedStorageUnitNames);
        ConfigurationManagerService configurationManagerService = contextManager.getManagerServiceFacade().getConfigurationManagerService();
        contextManager.getMetaDataContexts().getMetaData().getDatabases()
                .putAll(configurationManagerService.renewDatabase(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName), switchingResource));
        MetaDataContexts reloadMetaDataContexts = configurationManagerService.createMetaDataContexts(databaseName, false, switchingResource, null);
        configurationManagerService.alterSchemaMetaData(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName), true);
        contextManager.deletedSchemaNames(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName), contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName));
        contextManager.renewMetaDataContexts(reloadMetaDataContexts);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) {
        ruleConfigs.removeIf(each -> !each.getClass().isAssignableFrom(SingleRuleConfiguration.class));
        Collection<MetaDataVersion> metaDataVersions = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService()
                .persistConfigurations(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName(), ruleConfigs);
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
        contextManager.getManagerServiceFacade().getConfigurationManagerService().alterRuleConfiguration(databaseName, ruleConfigs.iterator().next());
        clearServiceCache();
    }
    
    @Override
    public Collection<MetaDataVersion> alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) {
        if (null != toBeAlteredRuleConfig) {
            Collection<MetaDataVersion> metaDataVersions = contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService()
                    .persistConfigurations(contextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName(), Collections.singletonList(toBeAlteredRuleConfig));
            contextManager.getPersistServiceFacade().getMetaDataPersistService().getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
            sendDatabaseRuleChangedEvent(databaseName, metaDataVersions);
            clearServiceCache();
        }
        return Collections.emptyList();
    }
    
    private void sendDatabaseRuleChangedEvent(final String databaseName, final Collection<MetaDataVersion> metaDataVersions) {
        for (MetaDataVersion each : metaDataVersions) {
            sendDatabaseRuleChangedEvent(databaseName, each);
        }
    }
    
    private void sendDatabaseRuleChangedEvent(final String databaseName, final MetaDataVersion metaDataVersion) {
        ruleConfigurationEventBuilder.build(databaseName, new DataChangedEvent(metaDataVersion.getActiveVersionNodePath(), metaDataVersion.getNextActiveVersion(), Type.UPDATED))
                .ifPresent(optional -> contextManager.getComputeNodeInstanceContext().getEventBusContext().post(optional));
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) {
        if (null != toBeRemovedRuleConfig) {
            sendDatabaseRuleChangedEvent(databaseName,
                    contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService()
                            .deleteConfigurations(databaseName, Collections.singleton(toBeRemovedRuleConfig)));
            clearServiceCache();
        }
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final String ruleName) {
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getDatabaseRulePersistService().delete(databaseName, ruleName);
        clearServiceCache();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        contextManager.getManagerServiceFacade().getConfigurationManagerService().alterGlobalRuleConfiguration(toBeAlteredRuleConfig);
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
        clearServiceCache();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        contextManager.getManagerServiceFacade().getConfigurationManagerService().alterProperties(props);
        contextManager.getPersistServiceFacade().getMetaDataPersistService().getPropsService().persist(props);
        clearServiceCache();
    }
    
    private void clearServiceCache() {
        OrderedServicesCache.clearCache();
    }
}
