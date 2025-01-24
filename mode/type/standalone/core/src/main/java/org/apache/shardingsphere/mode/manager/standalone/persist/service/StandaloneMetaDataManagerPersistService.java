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

package org.apache.shardingsphere.mode.manager.standalone.persist.service;

import com.google.common.base.Strings;
import org.apache.shardingsphere.infra.config.rule.RuleConfiguration;
import org.apache.shardingsphere.infra.datasource.pool.props.domain.DataSourcePoolProperties;
import org.apache.shardingsphere.infra.metadata.ShardingSphereMetaData;
import org.apache.shardingsphere.infra.metadata.database.ShardingSphereDatabase;
import org.apache.shardingsphere.infra.metadata.database.rule.RuleMetaData;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereSchema;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereTable;
import org.apache.shardingsphere.infra.metadata.database.schema.model.ShardingSphereView;
import org.apache.shardingsphere.infra.metadata.version.MetaDataVersion;
import org.apache.shardingsphere.infra.rule.attribute.datanode.MutableDataNodeRuleAttribute;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule;
import org.apache.shardingsphere.infra.rule.scope.GlobalRule.GlobalRuleChangedType;
import org.apache.shardingsphere.infra.spi.type.ordered.cache.OrderedServicesCache;
import org.apache.shardingsphere.mode.event.DataChangedEvent.Type;
import org.apache.shardingsphere.mode.metadata.MetaDataContextManager;
import org.apache.shardingsphere.mode.metadata.MetaDataContexts;
import org.apache.shardingsphere.mode.metadata.factory.MetaDataContextsFactory;
import org.apache.shardingsphere.mode.metadata.manager.RuleItemChangedBuilder;
import org.apache.shardingsphere.mode.metadata.manager.SwitchingResource;
import org.apache.shardingsphere.mode.metadata.persist.MetaDataPersistService;
import org.apache.shardingsphere.mode.metadata.persist.config.database.DataSourceUnitPersistService;
import org.apache.shardingsphere.mode.metadata.refresher.metadata.util.TableRefreshUtils;
import org.apache.shardingsphere.mode.persist.service.MetaDataManagerPersistService;
import org.apache.shardingsphere.mode.spi.rule.item.RuleChangedItem;
import org.apache.shardingsphere.mode.spi.rule.item.alter.AlterRuleItem;
import org.apache.shardingsphere.mode.spi.rule.item.drop.DropRuleItem;
import org.apache.shardingsphere.single.config.SingleRuleConfiguration;
import org.apache.shardingsphere.single.rule.SingleRule;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.stream.Collectors;

/**
 * Standalone meta data manager persist service.
 */
public final class StandaloneMetaDataManagerPersistService implements MetaDataManagerPersistService {
    
    private final MetaDataContextManager metaDataContextManager;
    
    private final MetaDataPersistService metaDataPersistService;
    
    private final RuleItemChangedBuilder ruleItemChangedBuilder;
    
    public StandaloneMetaDataManagerPersistService(final MetaDataContextManager metaDataContextManager) {
        this.metaDataContextManager = metaDataContextManager;
        metaDataPersistService = metaDataContextManager.getMetaDataPersistService();
        ruleItemChangedBuilder = new RuleItemChangedBuilder();
    }
    
    @Override
    public void createDatabase(final String databaseName) {
        metaDataContextManager.getSchemaMetaDataManager().addDatabase(databaseName);
        metaDataPersistService.getDatabaseMetaDataFacade().getDatabase().add(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void dropDatabase(final String databaseName) {
        metaDataContextManager.getSchemaMetaDataManager().dropDatabase(databaseName);
        metaDataPersistService.getDatabaseMetaDataFacade().getDatabase().drop(databaseName);
        clearServiceCache();
    }
    
    @Override
    public void createSchema(final String databaseName, final String schemaName) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        database.addSchema(new ShardingSphereSchema(schemaName));
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(databaseName, schemaName);
    }
    
    @Override
    public void alterSchema(final String databaseName, final String schemaName, final String logicDataSourceName,
                            final Collection<ShardingSphereTable> alteredTables, final Collection<ShardingSphereView> alteredViews,
                            final Collection<String> droppedTables, final Collection<String> droppedViews) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        addDataNode(database, logicDataSourceName, schemaName, alteredTables, alteredViews);
        removeDataNode(database, schemaName, droppedTables, droppedViews);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, schemaName, alteredTables);
        metaDataPersistService.getDatabaseMetaDataFacade().getView().persist(databaseName, schemaName, alteredViews);
        droppedTables.forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getTable().drop(databaseName, schemaName, each));
        droppedViews.forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getView().delete(databaseName, schemaName, each));
    }
    
    @Override
    public void alterSchemaName(final String databaseName, final String schemaName, final String renameSchemaName, final String logicDataSourceName) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        putSchemaMetaData(database, schemaName, renameSchemaName, logicDataSourceName);
        removeSchemaMetaData(database, schemaName);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        ShardingSphereSchema alteredSchema = database.getSchema(renameSchemaName);
        if (alteredSchema.isEmpty()) {
            metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(databaseName, renameSchemaName);
        }
        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, renameSchemaName, alteredSchema.getAllTables());
        metaDataPersistService.getDatabaseMetaDataFacade().getView().persist(databaseName, renameSchemaName, alteredSchema.getAllViews());
        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().drop(databaseName, schemaName);
    }
    
    private void putSchemaMetaData(final ShardingSphereDatabase database, final String schemaName, final String renamedSchemaName, final String logicDataSourceName) {
        ShardingSphereSchema schema = database.getSchema(schemaName);
        ShardingSphereSchema renamedSchema = new ShardingSphereSchema(renamedSchemaName, schema.getAllTables(), schema.getAllViews());
        database.addSchema(renamedSchema);
        addDataNode(database, logicDataSourceName, schemaName, schema.getAllTables());
    }
    
    private void addDataNode(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName, final Collection<ShardingSphereTable> tobeAddedTables) {
        for (ShardingSphereTable each : tobeAddedTables) {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(each.getName(), database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, each.getName()));
            }
        }
    }
    
    private void addDataNode(final ShardingSphereDatabase database, final String logicDataSourceName, final String schemaName, final Collection<ShardingSphereTable> toBeAddedTables,
                             final Collection<ShardingSphereView> toBeAddedViews) {
        addTablesToDataNode(database, schemaName, logicDataSourceName, toBeAddedTables);
        addViewsToDataNode(database, schemaName, logicDataSourceName, toBeAddedTables, toBeAddedViews);
    }
    
    private void addTablesToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName, final Collection<ShardingSphereTable> toBeAddedTables) {
        for (ShardingSphereTable each : toBeAddedTables) {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(each.getName(), database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, each.getName()));
            }
            database.getSchema(schemaName).putTable(each);
        }
    }
    
    private void addViewsToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName,
                                    final Collection<ShardingSphereTable> toBeAddedTables, final Collection<ShardingSphereView> toBeAddedViews) {
        for (ShardingSphereView view : toBeAddedViews) {
            if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(view.getName(), database)) {
                database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(each -> each.put(logicDataSourceName, schemaName, view.getName()));
            }
            toBeAddedTables.stream().filter(each -> each.getName().toLowerCase().equals(view.getName())).findFirst().ifPresent(optional -> database.getSchema(schemaName).putTable(optional));
            database.getSchema(schemaName).putView(view);
        }
    }
    
    private void removeSchemaMetaData(final ShardingSphereDatabase database, final String schemaName) {
        ShardingSphereSchema schema = new ShardingSphereSchema(schemaName, database.getSchema(schemaName).getAllTables(), database.getSchema(schemaName).getAllViews());
        database.dropSchema(schemaName);
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), Collections.singleton(schemaName),
                schema.getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toSet()));
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
        schemaNames.forEach(schemaName -> dropSchema(databaseName, schemaName));
    }
    
    private void dropSchema(final String databaseName, final String schemaName) {
        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().drop(databaseName, schemaName);
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        database.dropSchema(schemaName);
        Collection<String> tobeRemovedTables = database.getSchema(schemaName).getAllTables().stream().map(ShardingSphereTable::getName).collect(Collectors.toSet());
        removeDataNode(database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class), Collections.singleton(schemaName.toLowerCase()), tobeRemovedTables);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
    }
    
    @Override
    public void createTable(final String databaseName, final String schemaName, final ShardingSphereTable table, final String logicDataSourceName) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        addTableToDataNode(database, schemaName, logicDataSourceName, table);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, schemaName, Collections.singleton(table));
    }
    
    @Override
    public void dropTables(final String databaseName, final String schemaName, final Collection<String> tableNames) {
        ShardingSphereMetaData metaData = metaDataContextManager.getMetaDataContexts().getMetaData();
        ShardingSphereDatabase database = metaData.getDatabase(databaseName);
        removeTablesToDataNode(database, schemaName, tableNames);
        metaData.getGlobalRuleMetaData().getRules().forEach(each -> ((GlobalRule) each).refresh(metaData.getAllDatabases(), GlobalRuleChangedType.SCHEMA_CHANGED));
        tableNames.forEach(each -> metaDataPersistService.getDatabaseMetaDataFacade().getTable().drop(databaseName, schemaName, each));
    }
    
    private void addTableToDataNode(final ShardingSphereDatabase database, final String schemaName, final String logicDataSourceName, final ShardingSphereTable table) {
        if (!Strings.isNullOrEmpty(logicDataSourceName) && TableRefreshUtils.isSingleTable(table.getName(), database)) {
            database.getRuleMetaData().getAttributes(MutableDataNodeRuleAttribute.class).forEach(rule -> rule.put(logicDataSourceName, schemaName, table.getName()));
        }
        database.getSchema(schemaName).putTable(table);
    }
    
    @Override
    public void registerStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeRegisteredProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByRegisterStorageUnit(metaDataContextManager.getMetaDataContexts()
                .getMetaData().getDatabase(databaseName).getResourceMetaData(), toBeRegisteredProps);
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext()).createChangedDatabase(
                databaseName, false, switchingResource, null, metaDataContextManager.getMetaDataContexts());
        metaDataContextManager.getMetaDataContexts().getMetaData().putDatabase(changedDatabase);
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getAllSchemas()
                .forEach(each -> {
                    if (each.isEmpty()) {
                        metaDataPersistService.getDatabaseMetaDataFacade().getSchema().add(databaseName, each.getName());
                    }
                    metaDataPersistService.getDatabaseMetaDataFacade().getTable().persist(databaseName, each.getName(), each.getAllTables());
                });
        DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persist(databaseName, toBeRegisteredProps));
        clearServiceCache();
    }
    
    @Override
    public void alterStorageUnits(final String databaseName, final Map<String, DataSourcePoolProperties> toBeUpdatedProps) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByAlterStorageUnit(metaDataContextManager.getMetaDataContexts().getMetaData()
                .getDatabase(databaseName).getResourceMetaData(), toBeUpdatedProps);
        ShardingSphereDatabase changedDatabase = new MetaDataContextsFactory(metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext()).createChangedDatabase(
                databaseName, true, switchingResource, null, metaDataContextManager.getMetaDataContexts());
        metaDataContextManager.getMetaDataContexts().getMetaData().putDatabase(changedDatabase);
        metaDataContextManager.getMetaDataContexts().getMetaData().getGlobalRuleMetaData().getRules()
                .forEach(each -> ((GlobalRule) each).refresh(metaDataContextManager.getMetaDataContexts().getMetaData().getAllDatabases(), GlobalRuleChangedType.DATABASE_CHANGED));
        DataSourceUnitPersistService dataSourceService = metaDataPersistService.getDataSourceUnitService();
        metaDataPersistService.getMetaDataVersionPersistService()
                .switchActiveVersion(dataSourceService.persist(databaseName, toBeUpdatedProps));
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void unregisterStorageUnits(final String databaseName, final Collection<String> toBeDroppedStorageUnitNames) throws SQLException {
        SwitchingResource switchingResource = metaDataContextManager.getResourceSwitchManager().switchByUnregisterStorageUnit(metaDataContextManager.getMetaDataContexts().getMetaData()
                .getDatabase(databaseName).getResourceMetaData(), toBeDroppedStorageUnitNames);
        MetaDataContexts reloadMetaDataContexts = new MetaDataContextsFactory(metaDataPersistService, metaDataContextManager.getComputeNodeInstanceContext()).createBySwitchResource(
                databaseName, false, switchingResource, metaDataContextManager.getMetaDataContexts());
        metaDataPersistService.persistReloadDatabaseByDrop(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName));
        metaDataContextManager.dropSchemas(databaseName, reloadMetaDataContexts.getMetaData().getDatabase(databaseName),
                metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName));
        metaDataContextManager.getMetaDataContexts().update(reloadMetaDataContexts);
        switchingResource.closeStaleDataSources();
        clearServiceCache();
    }
    
    @Override
    public void alterSingleRuleConfiguration(final String databaseName, final RuleMetaData ruleMetaData) throws SQLException {
        SingleRuleConfiguration singleRuleConfig = ruleMetaData.getSingleRule(SingleRule.class).getConfiguration();
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService()
                .persist(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName(), Collections.singleton(singleRuleConfig));
        metaDataPersistService.getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
        metaDataContextManager.getDatabaseRuleConfigurationManager().alterRuleConfiguration(databaseName, singleRuleConfig);
        clearServiceCache();
    }
    
    @Override
    public void alterRuleConfiguration(final String databaseName, final RuleConfiguration toBeAlteredRuleConfig) throws SQLException {
        if (null == toBeAlteredRuleConfig) {
            return;
        }
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService()
                .persist(metaDataContextManager.getMetaDataContexts().getMetaData().getDatabase(databaseName).getName(), Collections.singleton(toBeAlteredRuleConfig));
        metaDataPersistService.getMetaDataVersionPersistService().switchActiveVersion(metaDataVersions);
        for (MetaDataVersion each : metaDataVersions) {
            // TODO double check here, when ruleItemEvent not existed or not AlterRuleItemEvent @haoran
            Optional<RuleChangedItem> ruleItemChanged = buildAlterRuleItemChanged(databaseName, each, Type.UPDATED);
            if (ruleItemChanged.isPresent() && ruleItemChanged.get() instanceof AlterRuleItem) {
                metaDataContextManager.getRuleItemManager().alterRuleItem((AlterRuleItem) ruleItemChanged.get());
            }
        }
        clearServiceCache();
    }
    
    private Optional<RuleChangedItem> buildAlterRuleItemChanged(final String databaseName, final MetaDataVersion metaDataVersion, final Type type) {
        return ruleItemChangedBuilder.build(databaseName, metaDataVersion.getActiveVersionNodePath(), metaDataVersion.getNextActiveVersion(), type);
    }
    
    @Override
    public void removeRuleConfigurationItem(final String databaseName, final RuleConfiguration toBeRemovedRuleConfig) throws SQLException {
        if (null == toBeRemovedRuleConfig) {
            return;
        }
        Collection<MetaDataVersion> metaDataVersions = metaDataPersistService.getDatabaseRulePersistService().delete(databaseName, Collections.singleton(toBeRemovedRuleConfig));
        for (MetaDataVersion metaDataVersion : metaDataVersions) {
            Optional<RuleChangedItem> ruleItemChanged = buildAlterRuleItemChanged(databaseName, metaDataVersion, Type.DELETED);
            // TODO double check here, when ruleItemEvent not existed or not AlterRuleItemEvent @haoran
            if (ruleItemChanged.isPresent() && ruleItemChanged.get() instanceof DropRuleItem) {
                metaDataContextManager.getRuleItemManager().dropRuleItem((DropRuleItem) ruleItemChanged.get());
            }
        }
        clearServiceCache();
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
    
    private void clearServiceCache() {
        OrderedServicesCache.clearCache();
    }
}
