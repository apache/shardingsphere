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

package org.apache.shardingsphere.mode.manager.standalone.persist;

import com.google.common.base.Strings;
import org.apache.groovy.util.Maps;
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
import org.apache.shardingsphere.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.metadata.persist.service.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.metadata.persist.service.database.DatabaseMetaDataPersistService;
import org.apache.shardingsphere.mode.event.DataChangedEvent;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.event.builder.RuleConfigurationEventBuilder;
import org.apache.shardingsphere.mode.event.dispatch.DispatchEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.alter.AlterRuleItemEvent;
import org.apache.shardingsphere.mode.event.dispatch.rule.drop.DropRuleItemEvent;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.refresher.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.PersistRepository;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;

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
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final MetaDataContextManager metaDataContextManager;
    
    public StandaloneMetaDataManagerPersistService(final PersistRepository repository, final MetaDataContextManager metaDataContextManager) {
        metaDataPersistService = new MetaDataPersistService(repository);
        this.metaDataContextManager = metaDataContextManager;
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataContextManager.getSchemaMetaDataManager().addDatabase(databaseName);
        metaDataPersistService.getDatabaseMetaDataService().addDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        metaDataContextManager.getSchemaMetaDataManager().dropDatabase(databaseName);
        metaDataPersistService.getDatabaseMetaDataService().dropDatabase(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        ShardingSphereSchema schema = new ShardingSphereSchema();
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        database.addSchema(schemaName, schema);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        metaDataPersistService.getDatabaseMetaDataService().addSchema(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final AlterSchemaPOJO alterSchemaPOJO) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(alterSchemaPOJO.getDatabaseName());
        putSchemaMetaData(database, alterSchemaPOJO.getSchemaName(), alterSchemaPOJO.getRenameSchemaName(), alterSchemaPOJO.getLogicDataSourceName());
        removeSchemaMetaData(database, alterSchemaPOJO.getSchemaName());
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        DatabaseMetaDataPersistService databaseMetaDataService = metaDataPersistService.getDatabaseMetaDataService();
        ShardingSphereSchema alteredSchema = database.getSchema(alterSchemaPOJO.getRenameSchemaName());
        String databaseName = alterSchemaPOJO.getDatabaseName();
        String alteredSchemaName = alterSchemaPOJO.getRenameSchemaName();
        if (alteredSchema.isEmpty()) {
            databaseMetaDataService.addSchema(databaseName, alteredSchemaName);
        }
        databaseMetaDataService.getTableMetaDataPersistService().persist(databaseName, alteredSchemaName, alteredSchema.getTables());
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, alteredSchemaName, alteredSchema.getViews());
        databaseMetaDataService.dropSchema(databaseName, alterSchemaPOJO.getSchemaName());
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
            Optional.ofNullable(toBeAddedTables.get(entry.getKey().toLowerCase())).ifPresent(optional -> database.getSchema(schemaName).putTable(entry.getKey(), optional));
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
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
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
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        Map<String, ShardingSphereTable> tables = alterSchemaMetaDataPOJO.getAlteredTables().stream().collect(Collectors.toMap(ShardingSphereTable::getName, table -> table));
        Map<String, ShardingSphereView> views = alterSchemaMetaDataPOJO.getAlteredViews().stream().collect(Collectors.toMap(ShardingSphereView::getName, view -> view));
        addDataNode(database, alterSchemaMetaDataPOJO.getLogicDataSourceName(), schemaName, tables, views);
        removeDataNode(database, schemaName, alterSchemaMetaDataPOJO.getDroppedTables(), alterSchemaMetaDataPOJO.getDroppedViews());
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        DatabaseMetaDataPersistService databaseMetaDataService = metaDataPersistService.getDatabaseMetaDataService();
        databaseMetaDataService.getTableMetaDataPersistService().persist(databaseName, schemaName, tables);
        databaseMetaDataService.getViewMetaDataPersistService().persist(databaseName, schemaName, views);
        alterSchemaMetaDataPOJO.getDroppedTables().forEach(each -> databaseMetaDataService.getTableMetaDataPersistService().delete(databaseName, schemaName, each));
        alterSchemaMetaDataPOJO.getDroppedViews().forEach(each -> databaseMetaDataService.getViewMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByRegisterStorageUnit(metaDataContextManager.getMetaDataContexts().get()
                .getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisteredProps);
        Map<String, ShardingSphereDatabase> changedDatabases = MetaDataContextsFactory.createChangedDatabases(databaseName, false, switchingResource, null,
                metaDataContextManager.getMetaDataContexts().get(), metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
        metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabases().putAll(changedDatabases);
        metaDataContextManager.getMetaDataContexts().get().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName).getSchemas()
                .forEach((schemaName, schema) -> {
                    if (schema.isEmpty()) {
                        metaDataPersistService.getDatabaseMetaDataService().addSchema(databaseName, schemaName);
                    }
                    metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService().persist(databaseName, schemaName, schema.getTables());
                });
        DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeRegisteredProps));
        clearServiceCache();
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(metaDataContextManager.getMetaDataContexts().get().getMetaData()
                .getDatabase(databaseName).getResourceMetaData(), toBeUpdatedProps);
        Map<String, ShardingSphereDatabase> changedDatabases = MetaDataContextsFactory.createChangedDatabases(databaseName, true, switchingResource, null,
                metaDataContextManager.getMetaDataContexts().get(), metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
        metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabases().putAll(changedDatabases);
        metaDataContextManager.getMetaDataContexts().get().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persistConfigurations(databaseName, toBeUpdatedProps));
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByUnregisterStorageUnit(metaDataContextManager.getMetaDataContexts().get().getMetaData()
                .getDatabase(databaseName).getResourceMetaData(), toBeDroppedStorageUnitNames);
        MetaDataContexts reloadMetaDataContexts = MetaDataContextsFactory.createBySwitchResource(databaseName, false, switchingResource,
                metaDataContextManager.getMetaDataContexts().get(), metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext());
        metaDataPersistService.persistReloadDatabaseByDrop(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName));
        metaDataContextManager.deletedSchemaNames(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName));
        metaDataContextManager.renewMetaDataContexts(reloadMetaDataContexts);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final Collection<RuleConfiguration> ruleConfigs) throws SQLException {
        ruleConfigs.removeIf(each -> !each.getClass().isAssignableFrom(SingleRuleConfiguration.class));
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService()
                .persistConfigurations(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName).getName(), ruleConfigs);
        metaDataPersistService.getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
        metaDataContextManager.getDatabaseRuleConfigurationManager().alterRuleConfiguration(databaseName, ruleConfigs.iterator().next());
        clearServiceCache();
    }
    
    @Override
    public void alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) throws SQLException {
        if (null != toBeAlteredRuleConfig) {
            Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService()
                    .persistConfigurations(metaDataContextManager.getMetaDataContexts().get().getMetaData().getDatabase(databaseName).getName(), Collections.singletonList(toBeAlteredRuleConfig));
            metaDataPersistService.getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
            for (MetaDataVersion each : metaDataVersions) {
                Optional<DispatchEvent> ruleItemEvent = buildAlterRuleItemEvent(databaseName, each, Type.UPDATED);
                if (ruleItemEvent.isPresent() && ruleItemEvent.get() instanceof AlterRuleItemEvent) {
                    metaDataContextManager.getRuleItemManager().alterRuleItem((AlterRuleItemEvent) ruleItemEvent.get());
                }
            }
            clearServiceCache();
        }
    }
    
    private Optional<DispatchEvent> buildAlterRuleItemEvent(final String databaseName, final MetaDataVersion metaDataVersion, final Type type) {
        return ruleConfigurationEventBuilder.build(databaseName, new DataChangedEvent(metaDataVersion.getActiveVersionNodePath(), metaDataVersion.getNextActiveVersion(), type));
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) throws SQLException {
        if (null != toBeRemovedRuleConfig) {
            Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService()
                    .deleteConfigurations(databaseName, Collections.singleton(toBeRemovedRuleConfig));
            for (MetaDataVersion metaDataVersion : metaDataVersions) {
                Optional<DispatchEvent> ruleItemEvent = buildAlterRuleItemEvent(databaseName, metaDataVersion, Type.DELETED);
                if (ruleItemEvent.isPresent() && ruleItemEvent.get() instanceof DropRuleItemEvent) {
                    metaDataContextManager.getRuleItemManager().dropRuleItem((DropRuleItemEvent) ruleItemEvent.get());
                }
            }
            clearServiceCache();
        }
    }
    
    @Override
    public void removeRuleConfiguration(final String databaseName, final String ruleName) {
        metaDataPersistService.getDatabaseRulePersistService().delete(databaseName, ruleName);
        clearServiceCache();
    }
    
    @Override
    public void alterGlobalRuleConfiguration(final RuleConfiguration toBeAlteredRuleConfig) {
        metaDataContextManager.getGlobalConfigurationManager().alterGlobalRuleConfiguration(toBeAlteredRuleConfig);
        metaDataPersistService.getGlobalRuleService().persist(Collections.singleton(toBeAlteredRuleConfig));
        clearServiceCache();
    }
    
    @Override
    public void alterProperties(final Properties props) {
        metaDataContextManager.getGlobalConfigurationManager().alterProperties(props);
        metaDataPersistService.getPropsService().persist(props);
        clearServiceCache();
    }
    
    @Override
    public void createTable(final String databaseName, final String schemaName, final ShardingSphereTable table, final String logicDataSourceName) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        addTableToDataNode(database, schemaName, logicDataSourceName, table);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService().persist(databaseName, schemaName, Maps.of(table.getName(), table));
    }
    
    @Override
    public void dropTables(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().get().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        removeTablesToDataNode(database, schemaName, tableNames);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        tableNames.forEach(each -> metaDataPersistService.getDatabaseMetaDataService().getTableMetaDataPersistService().delete(databaseName, schemaName, each));
    }
    
    private void addTableToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName, final ShardingSphereTable table) {
        if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(table.getName(), database)) {
            database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, table.getName()));
        }
        database.getSchema(schemaName).putTable(table.getName(), table);
    }
    
    private void clearServiceCache() {
        OrderedServicesCache.clearCache();
    }
}
